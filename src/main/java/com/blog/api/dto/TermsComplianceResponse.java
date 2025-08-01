package com.blog.api.dto;

import java.io.Serializable;

/**
 * DTO for terms compliance status response
 */
public record TermsComplianceResponse(
    boolean success,
    String message,
    TermsInfoDTO termsInfo,
    TermsAcceptanceResponse acceptance
) implements Serializable {
    
    /**
     * Create success response after terms acceptance
     */
    public static TermsComplianceResponse success(String message, 
                                                 TermsInfoDTO termsInfo,
                                                 TermsAcceptanceResponse acceptance) {
        return new TermsComplianceResponse(true, message, termsInfo, acceptance);
    }
    
    /**
     * Create error response for terms requirement
     */
    public static TermsComplianceResponse requiresAcceptance(String message, TermsInfoDTO termsInfo) {
        return new TermsComplianceResponse(false, message, termsInfo, null);
    }
    
    /**
     * Create standard success response
     */
    public static TermsComplianceResponse accepted(TermsAcceptanceResponse acceptance) {
        return success(
            "Terms accepted successfully",
            null,
            acceptance
        );
    }
    
    /**
     * Create standard requirement response
     */
    public static TermsComplianceResponse required(TermsInfoDTO termsInfo) {
        return requiresAcceptance(
            "Terms acceptance is required to continue using the service",
            termsInfo
        );
    }
}