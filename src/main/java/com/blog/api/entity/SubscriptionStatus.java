package com.blog.api.entity;

/**
 * Enum representing the possible states of a newsletter subscription.
 * 
 * @author AI-Driven Development
 * @version 1.0
 * @since 2025-08-05
 */
public enum SubscriptionStatus {
    /**
     * Initial state when user subscribes but hasn't confirmed email yet
     */
    PENDING,
    
    /**
     * User has confirmed their email subscription
     */
    CONFIRMED,
    
    /**
     * User has opted out from the newsletter
     */
    UNSUBSCRIBED,
    
    /**
     * Data has been deleted for LGPD compliance
     */
    DELETED
}