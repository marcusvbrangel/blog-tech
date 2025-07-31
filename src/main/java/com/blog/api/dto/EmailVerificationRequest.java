package com.blog.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Request DTO for email verification operations
 */
public record EmailVerificationRequest(
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    String email
) {
    
    /**
     * Factory method to create from email string
     */
    public static EmailVerificationRequest of(String email) {
        return new EmailVerificationRequest(email);
    }
}