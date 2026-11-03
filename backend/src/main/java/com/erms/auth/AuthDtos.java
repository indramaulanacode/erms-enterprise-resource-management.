package com.erms.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public final class AuthDtos {
    private AuthDtos() {}

    public record RegisterRequest(
            @NotBlank @Size(max = 160) String organizationName,
            @NotBlank @Pattern(regexp = "[a-zA-Z0-9][a-zA-Z0-9-]{2,99}") String organizationSlug,
            @NotBlank @Email @Size(max = 254) String email,
            @NotBlank @Size(min = 12, max = 72) String password,
            @NotBlank @Size(max = 160) String fullName
    ) {}

    public record LoginRequest(
            @NotBlank @Email @Size(max = 254) String email,
            @NotBlank @Size(max = 72) String password,
            @NotBlank @Size(max = 100) String organizationSlug
    ) {}

    public record RefreshRequest(@NotBlank @Size(max = 256) String refreshToken) {}
    public record LogoutRequest(@NotBlank @Size(max = 256) String refreshToken) {}

    public record TokenResponse(
            String tokenType,
            String accessToken,
            long accessTokenExpiresInSeconds,
            String refreshToken,
            String organizationId,
            String userId
    ) {}
}