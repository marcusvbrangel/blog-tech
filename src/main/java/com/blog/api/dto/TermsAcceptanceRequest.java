package com.blog.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.io.Serializable;

/**
 * DTO for terms acceptance request
 */
public record TermsAcceptanceRequest(
    @NotBlank(message = "Terms version is required")
    @Size(max = 10, message = "Terms version cannot exceed 10 characters")
    String termsVersion,
    
    Boolean acceptPrivacyPolicy,
    Boolean acceptTermsOfService,
    Boolean acceptCookiePolicy
) implements Serializable {
    
    /**
     * Validate that all required acceptances are true
     */
    public boolean isValidAcceptance() {
        return Boolean.TRUE.equals(acceptPrivacyPolicy) && 
               Boolean.TRUE.equals(acceptTermsOfService) && 
               Boolean.TRUE.equals(acceptCookiePolicy);
    }
    
    /**
     * Create request for current terms version
     */
    public static TermsAcceptanceRequest acceptAll(String termsVersion) {
        return new TermsAcceptanceRequest(
            termsVersion,
            true,
            true,
            true
        );
    }
}