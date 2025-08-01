package com.blog.api.dto;

import com.blog.api.util.PasswordPolicyValidator;
import jakarta.validation.constraints.NotBlank;

/**
 * Request DTO for password reset confirmation
 */
public record PasswordResetConfirmRequest(
    @NotBlank(message = "Token is required")
    String token,
    
    @NotBlank(message = "New password is required")
    String newPassword
) {
    
    public PasswordResetConfirmRequest {
        PasswordPolicyValidator.ValidationResult result = PasswordPolicyValidator.validate(newPassword);
        if (!result.isValid()) {
            throw new IllegalArgumentException("Password policy violation: " + result.getErrorMessage());
        }
    }
    
    /**
     * Factory method to create from token and password
     */
    public static PasswordResetConfirmRequest of(String token, String newPassword) {
        return new PasswordResetConfirmRequest(token, newPassword);
    }
}