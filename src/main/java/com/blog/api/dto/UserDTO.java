package com.blog.api.dto;

import com.blog.api.entity.User;

import java.io.Serializable;
import java.time.LocalDateTime;

public record UserDTO(
    Long id,
    String username,
    String email,
    User.Role role,
    LocalDateTime createdAt,
    Boolean emailVerified,
    LocalDateTime emailVerifiedAt,
    LocalDateTime lastLogin,
    String termsAcceptedVersion,
    Boolean hasAcceptedTerms
) implements Serializable {
    public static UserDTO fromEntity(User user) {
        return new UserDTO(
            user.getId(),
            user.getUsername(),
            user.getEmail(),
            user.getRole(),
            user.getCreatedAt(),
            user.getEmailVerified(),
            user.getEmailVerifiedAt(),
            user.getLastLogin(),
            user.getTermsAcceptedVersion(),
            user.hasAcceptedTerms()
        );
    }
}