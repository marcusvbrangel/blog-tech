package com.blog.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Data Transfer Object for newsletter subscription requests.
 * Contains LGPD compliance fields for consent tracking.
 * 
 * @param email The subscriber's email address
 * @param consentToReceiveEmails Explicit consent to receive newsletter emails (LGPD compliance)
 * @param privacyPolicyVersion Version of privacy policy accepted by the user
 * @param ipAddress IP address of the user making the request (captured for audit)
 * @param userAgent User-Agent header from the request (captured for audit)
 * 
 * @author AI-Driven Development
 * @version 1.0
 * @since 2025-08-05
 */
public record NewsletterSubscriptionRequest(
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    @Size(max = 255, message = "Email must not exceed 255 characters")
    String email,

    @NotNull(message = "Consent to receive emails is required for LGPD compliance")
    Boolean consentToReceiveEmails,

    @NotBlank(message = "Privacy policy version is required for LGPD compliance")
    @Size(max = 20, message = "Privacy policy version must not exceed 20 characters")
    String privacyPolicyVersion,

    @Size(max = 45, message = "IP address must not exceed 45 characters")
    String ipAddress,

    @Size(max = 500, message = "User-Agent must not exceed 500 characters")
    String userAgent
) {
    /**
     * Compact constructor with validation and normalization.
     * Automatically normalizes email to lowercase and validates LGPD compliance.
     */
    public NewsletterSubscriptionRequest {
        // Normalize email to lowercase if not null
        if (email != null) {
            email = email.trim().toLowerCase();
        }

        // LGPD Compliance Validation
        if (consentToReceiveEmails != null && !consentToReceiveEmails) {
            throw new IllegalArgumentException("User must explicitly consent to receive emails for LGPD compliance");
        }

        // Validate privacy policy version format
        if (privacyPolicyVersion != null && !privacyPolicyVersion.trim().isEmpty()) {
            privacyPolicyVersion = privacyPolicyVersion.trim();
            if (!privacyPolicyVersion.matches("^[0-9]+\\.[0-9]+$")) {
                throw new IllegalArgumentException("Privacy policy version must be in format 'major.minor' (e.g., '1.0')");
            }
        }

        // Trim metadata fields
        if (ipAddress != null) {
            ipAddress = ipAddress.trim();
        }
        if (userAgent != null) {
            userAgent = userAgent.trim();
        }
    }

    /**
     * Convenience constructor for basic subscription without metadata.
     * Metadata fields (IP and User-Agent) will be null and should be set by the controller.
     * 
     * @param email The subscriber's email
     * @param consentToReceiveEmails Explicit consent for LGPD compliance
     * @param privacyPolicyVersion Version of privacy policy accepted
     */
    public NewsletterSubscriptionRequest(String email, Boolean consentToReceiveEmails, String privacyPolicyVersion) {
        this(email, consentToReceiveEmails, privacyPolicyVersion, null, null);
    }

    /**
     * Factory method to create a subscription request with consent.
     * 
     * @param email The subscriber's email
     * @param privacyPolicyVersion Version of privacy policy
     * @return NewsletterSubscriptionRequest with consent set to true
     */
    public static NewsletterSubscriptionRequest withConsent(String email, String privacyPolicyVersion) {
        return new NewsletterSubscriptionRequest(email, true, privacyPolicyVersion);
    }

    /**
     * Factory method to create a subscription request with full metadata.
     * 
     * @param email The subscriber's email
     * @param privacyPolicyVersion Version of privacy policy
     * @param ipAddress Request IP address
     * @param userAgent Request User-Agent
     * @return NewsletterSubscriptionRequest with all fields
     */
    public static NewsletterSubscriptionRequest withMetadata(String email, String privacyPolicyVersion, 
                                                           String ipAddress, String userAgent) {
        return new NewsletterSubscriptionRequest(email, true, privacyPolicyVersion, ipAddress, userAgent);
    }

    /**
     * Check if the request has LGPD compliant consent.
     * 
     * @return true if user has explicitly consented to receive emails
     */
    public boolean hasValidConsent() {
        return Boolean.TRUE.equals(consentToReceiveEmails);
    }

    /**
     * Check if the request has audit metadata (IP and User-Agent).
     * 
     * @return true if both IP address and User-Agent are present
     */
    public boolean hasAuditMetadata() {
        return ipAddress != null && !ipAddress.trim().isEmpty() &&
               userAgent != null && !userAgent.trim().isEmpty();
    }

    /**
     * Check if the request is valid for processing.
     * 
     * @return true if email is present, consent is given, and privacy policy version is specified
     */
    public boolean isValid() {
        return email != null && !email.trim().isEmpty() &&
               hasValidConsent() &&
               privacyPolicyVersion != null && !privacyPolicyVersion.trim().isEmpty();
    }
}