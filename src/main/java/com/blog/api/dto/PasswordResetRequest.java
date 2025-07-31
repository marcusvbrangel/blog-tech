package com.blog.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Request DTO for password reset operations
 */
public record PasswordResetRequest(
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    String email
) {
    
    /**
     * Factory method to create from email string
     */
    public static PasswordResetRequest of(String email) {
        return new PasswordResetRequest(email);
    }
}