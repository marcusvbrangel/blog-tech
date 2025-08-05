package com.blog.api.entity;

/**
 * Enum defining the different types of newsletter tokens.
 * Each type has specific use cases and different expiration periods.
 * 
 * @author AI-Driven Development
 * @version 1.0
 * @since 2025-08-05
 */
public enum NewsletterTokenType {
    /**
     * Token used to confirm email subscription to newsletter.
     * Typically expires in 24-48 hours.
     */
    CONFIRMATION,
    
    /**
     * Token used to unsubscribe from newsletter via email link.
     * Typically does not expire or has very long expiration.
     */
    UNSUBSCRIBE,
    
    /**
     * Token used for LGPD data request operations.
     * Typically expires in 7 days for security.
     */
    DATA_REQUEST
}