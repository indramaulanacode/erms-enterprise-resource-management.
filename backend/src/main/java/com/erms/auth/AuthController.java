package com.erms.auth;

import com.erms.auth.AuthDtos.LoginRequest;
import com.erms.auth.AuthDtos.LogoutRequest;
import com.erms.auth.AuthDtos.RefreshRequest;
import com.erms.auth.AuthDtos.RegisterRequest;
import com.erms.auth.AuthDtos.TokenResponse;
import jakarta.validation.Valid;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public TokenResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public TokenResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/refresh")
    public TokenResponse refresh(@Valid @RequestBody RefreshRequest request) {
        TokenResponse response = authService.refresh(request.refreshToken());
        if (response == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid, expired, or reused refresh token");
        }
        return response;
    }

    @PostMapping("/logout")
    public Map<String, String> logout(@Valid @RequestBody LogoutRequest request) {
        authService.logout(request.refreshToken());
        return Map.of("message", "Refresh token revoked");
    }
}