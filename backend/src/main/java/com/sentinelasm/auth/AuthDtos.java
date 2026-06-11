package com.sentinelasm.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class AuthDtos {
    public record RegisterRequest(
            @NotBlank String name,
            @Email String email,
            @Size(min = 8) String password,
            String company
    ) {}

    public record LoginRequest(@Email String email, @NotBlank String password) {}

    public record AuthResponse(String token, UserProfile user) {}

    public record UserProfile(String id, String name, String email, String company, String role, String apiKey) {}

    public record ChangePasswordRequest(@NotBlank String currentPassword, @Size(min = 8) String newPassword) {}

    public record ForgotPasswordRequest(@Email String email) {}
}
