package com.blog.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for password reset confirmation
 */
public record PasswordResetConfirmRequest(
    @NotBlank(message = "Token is required")
    String token,
    
    @NotBlank(message = "New password is required")
    @Size(min = 6, message = "Password must be at least 6 characters long")
    String newPassword
) {
    
    /**
     * Factory method to create from token and password
     */
    public static PasswordResetConfirmRequest of(String token, String newPassword) {
        return new PasswordResetConfirmRequest(token, newPassword);
    }
}