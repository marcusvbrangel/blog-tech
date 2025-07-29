package com.blog.api.dto;

import com.blog.api.entity.User;

import java.io.Serializable;
import java.time.LocalDateTime;

public record UserDTO(
    Long id,
    String username,
    String email,
    User.Role role,
    LocalDateTime createdAt
) implements Serializable {
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