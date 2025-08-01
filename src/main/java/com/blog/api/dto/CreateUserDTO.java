package com.blog.api.dto;

import com.blog.api.entity.User;
import com.blog.api.util.PasswordPolicyValidator;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateUserDTO(
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    String username,

    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    String email,

    @NotBlank(message = "Password is required")
    String password,

    User.Role role
) {
    public CreateUserDTO(String username, String email, String password) {
        this(username, email, password, User.Role.USER);
    }

    public CreateUserDTO {
        PasswordPolicyValidator.ValidationResult result = PasswordPolicyValidator.validate(password);
        if (!result.isValid()) {
            throw new IllegalArgumentException("Password policy violation: " + result.getErrorMessage());
        }
    }
}