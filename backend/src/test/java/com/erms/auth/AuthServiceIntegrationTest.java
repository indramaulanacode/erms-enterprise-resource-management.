package com.erms.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.erms.auth.AuthDtos.LoginRequest;
import com.erms.auth.AuthDtos.RegisterRequest;
import com.erms.auth.AuthDtos.TokenResponse;
import com.erms.security.JwtService;
import java.util.Base64;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.server.ResponseStatusException;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Testcontainers(disabledWithoutDocker = true)
class AuthServiceIntegrationTest {
    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("security.jwt.secret", () -> Base64.getEncoder().encodeToString(new byte[48]));
        registry.add("security.jwt.issuer", () -> "erms-integration-test");
    }

    @Autowired AuthService authService;
    @Autowired JwtService jwtService;

    @Test
    void registrationCreatesOrganizationAdminAndValidAccessToken() {
        String slug = "org-" + UUID.randomUUID().toString().substring(0, 8);
        TokenResponse result = authService.register(new RegisterRequest(
                "Integration Company", slug, "admin@example.test",
                "a-very-strong-password", "Admin User"));

        assertNotNull(result.accessToken());
        assertNotNull(result.refreshToken());
        assertEquals(result.organizationId(),
                jwtService.parseAndValidate(result.accessToken()).organizationId().toString());
        assertEquals(java.util.List.of("ORG_ADMIN"),
                jwtService.parseAndValidate(result.accessToken()).roles());
    }

    @Test
    void loginRejectsIncorrectPassword() {
        String slug = "login-" + UUID.randomUUID().toString().substring(0, 8);
        authService.register(new RegisterRequest(
                "Login Company", slug, "login@example.test",
                "a-very-strong-password", "Login User"));

        assertThrows(ResponseStatusException.class, () -> authService.login(
                new LoginRequest("login@example.test", "incorrect-password", slug)));
    }

    @Test
    void refreshRotatesTokenAndReplayRevokesTheWholeFamily() {
        String slug = "refresh-" + UUID.randomUUID().toString().substring(0, 8);
        TokenResponse initial = authService.register(new RegisterRequest(
                "Refresh Company", slug, "refresh@example.test",
                "a-very-strong-password", "Refresh User"));

        TokenResponse rotated = authService.refresh(initial.refreshToken());
        assertNotNull(rotated);
        assertNotEquals(initial.refreshToken(), rotated.refreshToken());

        assertNull(authService.refresh(initial.refreshToken()));
        assertNull(authService.refresh(rotated.refreshToken()));
    }

    @Test
    void logoutRevokesRefreshToken() {
        String slug = "logout-" + UUID.randomUUID().toString().substring(0, 8);
        TokenResponse session = authService.register(new RegisterRequest(
                "Logout Company", slug, "logout@example.test",
                "a-very-strong-password", "Logout User"));

        authService.logout(session.refreshToken());

        assertNull(authService.refresh(session.refreshToken()));
    }
}
