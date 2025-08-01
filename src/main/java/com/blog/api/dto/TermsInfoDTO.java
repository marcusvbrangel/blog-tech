package com.blog.api.dto;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * DTO containing information about current terms and user status
 */
public record TermsInfoDTO(
    String currentVersion,
    boolean acceptanceRequired,
    boolean userHasAccepted,
    String userAcceptedVersion,
    LocalDateTime lastAcceptedAt,
    String termsUrl,
    String privacyPolicyUrl,
    String cookiePolicyUrl
) implements Serializable {
    
    /**
     * Create terms info for user who needs to accept
     */
    public static TermsInfoDTO requiresAcceptance(String currentVersion) {
        return new TermsInfoDTO(
            currentVersion,
            true,
            false,
            null,
            null,
            "/terms-of-service",
            "/privacy-policy", 
            "/cookie-policy"
        );
    }
    
    /**
     * Create terms info for user who has already accepted
     */
    public static TermsInfoDTO alreadyAccepted(String currentVersion, 
                                              String userAcceptedVersion,
                                              LocalDateTime lastAcceptedAt) {
        return new TermsInfoDTO(
            currentVersion,
            false,
            true,
            userAcceptedVersion,
            lastAcceptedAt,
            "/terms-of-service",
            "/privacy-policy",
            "/cookie-policy"
        );
    }
    
    /**
     * Create terms info when acceptance is not required
     */
    public static TermsInfoDTO notRequired(String currentVersion) {
        return new TermsInfoDTO(
            currentVersion,
            false,
            true,
            currentVersion,
            null,
            "/terms-of-service",
            "/privacy-policy",
            "/cookie-policy"
        );
    }
}