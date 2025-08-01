package com.blog.api.dto;

import com.blog.api.entity.TermsAcceptance;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * DTO for terms acceptance response
 */
public record TermsAcceptanceResponse(
    Long id,
    Long userId,
    String username,
    String termsVersion,
    LocalDateTime acceptedAt,
    String ipAddress,
    String userAgent
) implements Serializable {
    
    /**
     * Create response from entity
     */
    public static TermsAcceptanceResponse fromEntity(TermsAcceptance acceptance) {
        return new TermsAcceptanceResponse(
            acceptance.getId(),
            acceptance.getUser().getId(),
            acceptance.getUser().getUsername(),
            acceptance.getTermsVersion(),
            acceptance.getAcceptedAt(),
            acceptance.getIpAddress(),
            acceptance.getUserAgent()
        );
    }
    
    /**
     * Create response from entity without sensitive information
     */
    public static TermsAcceptanceResponse fromEntitySafe(TermsAcceptance acceptance) {
        return new TermsAcceptanceResponse(
            acceptance.getId(),
            acceptance.getUser().getId(),
            acceptance.getUser().getUsername(),
            acceptance.getTermsVersion(),
            acceptance.getAcceptedAt(),
            null, // Hide IP address
            null  // Hide user agent
        );
    }
}