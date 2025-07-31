package com.blog.api.dto;

import java.time.LocalDateTime;

/**
 * Response DTO for verification operations
 */
public record VerificationResponse(
    boolean success,
    String message,
    LocalDateTime timestamp,
    Object data
) {
    
    /**
     * Create success response with data
     */
    public static VerificationResponse success(String message, Object data) {
        return new VerificationResponse(true, message, LocalDateTime.now(), data);
    }
    
    /**
     * Create success response without data
     */
    public static VerificationResponse success(String message) {
        return new VerificationResponse(true, message, LocalDateTime.now(), null);
    }
    
    /**
     * Create error response
     */
    public static VerificationResponse error(String message) {
        return new VerificationResponse(false, message, LocalDateTime.now(), null);
    }
}