package com.erms.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.jsonwebtoken.JwtException;
import java.time.Duration;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class JwtServiceTest {
    private static final String SECRET = java.util.Base64.getEncoder()
            .encodeToString(new byte[48]);

    @Test
    void issuesAndValidatesTokenWithOrganizationAndRoles() {
        JwtService service = new JwtService(SECRET, "erms-test", Duration.ofMinutes(10));
        UUID userId = UUID.randomUUID();
        UUID organizationId = UUID.randomUUID();
        UserPrincipal expected = new UserPrincipal(userId, organizationId, "admin@company.test",
                List.of("ORG_ADMIN", "HR_MANAGER"));

        UserPrincipal actual = service.parseAndValidate(service.issueAccessToken(expected));

        assertEquals(userId, actual.userId());
        assertEquals(organizationId, actual.organizationId());
        assertEquals(expected.username(), actual.username());
        assertEquals(expected.roles(), actual.roles());
    }

    @Test
    void rejectsTokenSignedByAnotherKey() {
        JwtService issuer = new JwtService(SECRET, "erms-test", Duration.ofMinutes(10));
        String otherSecret = java.util.Base64.getEncoder().encodeToString(new byte[64]);
        JwtService verifier = new JwtService(otherSecret, "erms-test", Duration.ofMinutes(10));
        UserPrincipal principal = new UserPrincipal(UUID.randomUUID(), UUID.randomUUID(),
                "user@company.test", List.of("EMPLOYEE"));

        assertThrows(JwtException.class,
                () -> verifier.parseAndValidate(issuer.issueAccessToken(principal)));
    }
}