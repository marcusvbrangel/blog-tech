package com.blog.api.dto;

public record JwtResponse(
    String token,
    String type,
    UserDTO user
) {
    public JwtResponse(String token, UserDTO user) {
        this(token, "Bearer", user);
    }
}