package com.blog.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO for refresh token responses.
 */
public record RefreshTokenResponse(
    @JsonProperty("access_token")
    String accessToken,
    
    @JsonProperty("refresh_token") 
    String refreshToken,
    
    @JsonProperty("token_type")
    String tokenType,
    
    @JsonProperty("expires_in")
    Long expiresIn
) {
    public RefreshTokenResponse(String accessToken, String refreshToken) {
        this(accessToken, refreshToken, "Bearer", 900L); // 15 minutes default
    }
}