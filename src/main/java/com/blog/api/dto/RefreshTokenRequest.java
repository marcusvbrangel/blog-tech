package com.blog.api.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO for refresh token requests.
 */
public record RefreshTokenRequest(
    @NotBlank(message = "Refresh token is required")
    String refreshToken
) {}