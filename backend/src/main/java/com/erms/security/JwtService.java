package com.erms.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtService {
    private final SecretKey signingKey;
    private final String issuer;
    private final Duration accessTokenTtl;

    public JwtService(
            @Value("${security.jwt.secret}") String base64Secret,
            @Value("${security.jwt.issuer}") String issuer,
            @Value("${security.jwt.access-token-ttl}") Duration accessTokenTtl
    ) {
        byte[] keyBytes = Decoders.BASE64.decode(base64Secret);
        this.signingKey = Keys.hmacShaKeyFor(keyBytes);
        this.issuer = issuer;
        this.accessTokenTtl = accessTokenTtl;
    }

    public String issueAccessToken(UserPrincipal principal) {
        Instant now = Instant.now();
        return Jwts.builder()
                .issuer(issuer)
                .subject(principal.userId().toString())
                .claim("org", principal.organizationId().toString())
                .claim("username", principal.username())
                .claim("roles", principal.roles())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(accessTokenTtl)))
                .signWith(signingKey)
                .compact();
    }

    public UserPrincipal parseAndValidate(String token) {
        Jws<Claims> parsed = Jwts.parser()
                .verifyWith(signingKey)
                .requireIssuer(issuer)
                .build()
                .parseSignedClaims(token);

        Claims claims = parsed.getPayload();
        UUID userId = UUID.fromString(claims.getSubject());
        UUID organizationId = UUID.fromString(claims.get("org", String.class));
        String username = claims.get("username", String.class);
        List<?> rawRoles = claims.get("roles", List.class);
        List<String> roles = rawRoles == null ? List.of() : rawRoles.stream()
                .filter(String.class::isInstance)
                .map(String.class::cast)
                .toList();

        if (username == null || username.isBlank() || organizationId == null) {
            throw new IllegalArgumentException("Required JWT claims are missing");
        }
        return new UserPrincipal(userId, organizationId, username, roles);
    }
}