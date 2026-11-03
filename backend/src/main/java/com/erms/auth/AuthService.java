package com.erms.auth;

import com.erms.auth.AuthDtos.LoginRequest;
import com.erms.auth.AuthDtos.RegisterRequest;
import com.erms.auth.AuthDtos.TokenResponse;
import com.erms.security.JwtService;
import com.erms.security.UserPrincipal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {
    private static final SecureRandom RANDOM = new SecureRandom();
    private final JdbcTemplate jdbc;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final Duration accessTtl;
    private final Duration refreshTtl;

    public AuthService(
            JdbcTemplate jdbc,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            @Value("${security.jwt.access-token-ttl}") Duration accessTtl,
            @Value("${security.jwt.refresh-token-ttl}") Duration refreshTtl
    ) {
        this.jdbc = jdbc;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.accessTtl = accessTtl;
        this.refreshTtl = refreshTtl;
    }

    @Transactional
    public TokenResponse register(RegisterRequest request) {
        UUID organizationId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID roleId = UUID.randomUUID();
        String email = normalizeEmail(request.email());
        String slug = request.organizationSlug().trim().toLowerCase();

        try {
            jdbc.update("INSERT INTO organizations (id, name, slug) VALUES (?, ?, ?)",
                    organizationId, request.organizationName().trim(), slug);
            jdbc.update("""
                INSERT INTO app_users (id, organization_id, email, password_hash, full_name)
                VALUES (?, ?, ?, ?, ?)
                """, userId, organizationId, email, passwordEncoder.encode(request.password()),
                    request.fullName().trim());
            jdbc.update("""
                INSERT INTO app_roles (id, organization_id, code, name, is_system)
                VALUES (?, ?, 'ORG_ADMIN', 'Organization Administrator', TRUE)
                """, roleId, organizationId);
            jdbc.update("""
                INSERT INTO app_user_roles (user_id, organization_id, role_id) VALUES (?, ?, ?)
                """, userId, organizationId, roleId);
        } catch (DuplicateKeyException exception) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Organization slug or account already exists");
        }

        return createSession(userId, organizationId, email, List.of("ORG_ADMIN"), UUID.randomUUID());
    }

    @Transactional
    public TokenResponse login(LoginRequest request) {
        String email = normalizeEmail(request.email());
        var users = jdbc.query("""
            SELECT u.id, u.organization_id, u.email, u.password_hash
            FROM app_users u
            JOIN organizations o ON o.id = u.organization_id
            WHERE u.email = ? AND o.slug = ? AND u.status = 'ACTIVE' AND o.status = 'ACTIVE'
            """, (rs, row) -> new LoginUser(
                rs.getObject("id", UUID.class),
                rs.getObject("organization_id", UUID.class),
                rs.getString("email"),
                rs.getString("password_hash")
            ), email, request.organizationSlug().trim().toLowerCase());

        if (users.isEmpty() || !passwordEncoder.matches(request.password(), users.getFirst().passwordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email, password, or organization");
        }

        LoginUser user = users.getFirst();
        List<String> roles = loadRoles(user.id(), user.organizationId());
        return createSession(user.id(), user.organizationId(), user.email(), roles, UUID.randomUUID());
    }

    @Transactional
    public TokenResponse refresh(String rawToken) {
        String tokenHash = sha256(rawToken);
        var rows = jdbc.query("""
            SELECT id, user_id, family_id, expires_at, revoked_at
            FROM refresh_tokens WHERE token_hash = ? FOR UPDATE
            """, (rs, row) -> new RefreshRow(
                rs.getObject("id", UUID.class),
                rs.getObject("user_id", UUID.class),
                rs.getObject("family_id", UUID.class),
                rs.getTimestamp("expires_at").toInstant(),
                rs.getTimestamp("revoked_at") == null ? null : rs.getTimestamp("revoked_at").toInstant()
            ), tokenHash);

        if (rows.isEmpty()) return null;
        RefreshRow old = rows.getFirst();

        if (old.revokedAt() != null) {
            jdbc.update("UPDATE refresh_tokens SET revoked_at = NOW() WHERE family_id = ? AND revoked_at IS NULL",
                    old.familyId());
            return null;
        }
        if (!old.expiresAt().isAfter(Instant.now())) {
            jdbc.update("UPDATE refresh_tokens SET revoked_at = NOW() WHERE id = ?", old.id());
            return null;
        }

        var users = jdbc.query("""
            SELECT u.id, u.organization_id, u.email
            FROM app_users u JOIN organizations o ON o.id = u.organization_id
            WHERE u.id = ? AND u.status = 'ACTIVE' AND o.status = 'ACTIVE'
            """, (rs, row) -> new LoginUser(
                rs.getObject("id", UUID.class),
                rs.getObject("organization_id", UUID.class),
                rs.getString("email"),
                ""
            ), old.userId());

        if (users.isEmpty()) {
            jdbc.update("UPDATE refresh_tokens SET revoked_at = NOW() WHERE family_id = ? AND revoked_at IS NULL",
                    old.familyId());
            return null;
        }

        LoginUser user = users.getFirst();
        List<String> roles = loadRoles(user.id(), user.organizationId());
        String replacement = newRefreshToken();
        UUID replacementId = UUID.randomUUID();
        jdbc.update("""
            INSERT INTO refresh_tokens (id, user_id, family_id, token_hash, expires_at)
            VALUES (?, ?, ?, ?, ?)
            """, replacementId, user.id(), old.familyId(), sha256(replacement), Instant.now().plus(refreshTtl));
        jdbc.update("""
            UPDATE refresh_tokens SET revoked_at = NOW(), replaced_by_token_id = ? WHERE id = ?
            """, replacementId, old.id());

        return tokenResponse(user.id(), user.organizationId(), user.email(), roles, replacement);
    }

    @Transactional
    public void logout(String rawToken) {
        jdbc.update("UPDATE refresh_tokens SET revoked_at = NOW() WHERE token_hash = ? AND revoked_at IS NULL",
                sha256(rawToken));
    }

    private List<String> loadRoles(UUID userId, UUID organizationId) {
        return jdbc.query("""
            SELECT r.code FROM app_user_roles ur
            JOIN app_roles r ON r.id = ur.role_id AND r.organization_id = ur.organization_id
            WHERE ur.user_id = ? AND ur.organization_id = ?
            """, (rs, row) -> rs.getString(1), userId, organizationId);
    }

    private TokenResponse createSession(UUID userId, UUID organizationId, String email,
                                        List<String> roles, UUID familyId) {
        String refreshToken = newRefreshToken();
        jdbc.update("""
            INSERT INTO refresh_tokens (id, user_id, family_id, token_hash, expires_at)
            VALUES (?, ?, ?, ?, ?)
            """, UUID.randomUUID(), userId, familyId, sha256(refreshToken), Instant.now().plus(refreshTtl));
        return tokenResponse(userId, organizationId, email, roles, refreshToken);
    }

    private TokenResponse tokenResponse(UUID userId, UUID organizationId, String email,
                                        List<String> roles, String refreshToken) {
        UserPrincipal principal = new UserPrincipal(userId, organizationId, email, roles);
        return new TokenResponse("Bearer", jwtService.issueAccessToken(principal),
                accessTtl.toSeconds(), refreshToken, organizationId.toString(), userId.toString());
    }

    private static String normalizeEmail(String email) {
        return email.trim().toLowerCase();
    }

    private static String newRefreshToken() {
        byte[] bytes = new byte[64];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private static String sha256(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8));
            return java.util.HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is not available", exception);
        }
    }

    private record LoginUser(UUID id, UUID organizationId, String email, String passwordHash) {}
    private record RefreshRow(UUID id, UUID userId, UUID familyId, Instant expiresAt, Instant revokedAt) {}
}