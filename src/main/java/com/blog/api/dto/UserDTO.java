package com.blog.api.dto;

import com.blog.api.entity.User;

import java.time.LocalDateTime;

public record UserDTO(
    Long id,
    String username,
    String email,
    User.Role role,
    LocalDateTime createdAt
) {
    public static UserDTO fromEntity(User user) {
        return new UserDTO(
            user.getId(),
            user.getUsername(),
            user.getEmail(),
            user.getRole(),
            user.getCreatedAt()
        );
    }
}