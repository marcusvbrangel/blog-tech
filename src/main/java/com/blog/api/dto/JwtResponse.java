package com.blog.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record JwtResponse(
    String token,
    String type,
    UserDTO user,
    String refreshToken
) {
    public JwtResponse(String token, UserDTO user) {
        this(token, "Bearer", user, null);
    }
    
    public JwtResponse(String token, UserDTO user, String refreshToken) {
        this(token, "Bearer", user, refreshToken);
    }
}