package com.blog.api.dto;

import com.blog.api.entity.NewsletterSubscriber;
import com.blog.api.entity.SubscriptionStatus;

import java.time.LocalDateTime;

/**
 * Data Transfer Object for newsletter subscription responses.
 * Contains essential information about the subscription result.
 * 
 * @param id The unique identifier of the subscription
 * @param email The subscriber's email address
 * @param status The current subscription status
 * @param createdAt When the subscription was created
 * @param message A human-readable message about the subscription result
 * 
 * @author AI-Driven Development
 * @version 1.0
 * @since 2025-08-05
 */
public record NewsletterSubscriptionResponse(
    Long id,
    String email,
    SubscriptionStatus status,
    LocalDateTime createdAt,
    String message
) {
    /**
     * Create a response from a NewsletterSubscriber entity.
     * 
     * @param subscriber the subscriber entity
     * @param message custom message for the response
     * @return NewsletterSubscriptionResponse
     */
    public static NewsletterSubscriptionResponse fromEntity(NewsletterSubscriber subscriber, String message) {
        return new NewsletterSubscriptionResponse(
            subscriber.getId(),
            subscriber.getEmail(),
            subscriber.getStatus(),
            subscriber.getCreatedAt(),
            message
        );
    }

    /**
     * Create a successful subscription response.
     * 
     * @param subscriber the subscriber entity
     * @return NewsletterSubscriptionResponse with success message
     */
    public static NewsletterSubscriptionResponse success(NewsletterSubscriber subscriber) {
        return fromEntity(subscriber, 
            "Subscription successful! Please check your email to confirm your subscription.");
    }

    /**
     * Create a resubscription response.
     * 
     * @param subscriber the subscriber entity
     * @return NewsletterSubscriptionResponse with resubscription message
     */
    public static NewsletterSubscriptionResponse resubscribed(NewsletterSubscriber subscriber) {
        return fromEntity(subscriber, 
            "Welcome back! Your subscription has been reactivated. Please check your email to confirm.");
    }

    /**
     * Create an already subscribed response.
     * 
     * @param subscriber the subscriber entity
     * @return NewsletterSubscriptionResponse with already subscribed message
     */
    public static NewsletterSubscriptionResponse alreadySubscribed(NewsletterSubscriber subscriber) {
        String statusMessage = switch (subscriber.getStatus()) {
            case CONFIRMED -> "You are already subscribed and confirmed to our newsletter.";
            case PENDING -> "You are already subscribed. Please check your email to confirm your subscription.";
            default -> "You are already subscribed to our newsletter.";
        };
        
        return fromEntity(subscriber, statusMessage);
    }

    /**
     * Check if the subscription requires email confirmation.
     * 
     * @return true if status is PENDING
     */
    public boolean requiresConfirmation() {
        return status == SubscriptionStatus.PENDING;
    }

    /**
     * Check if the subscription is active.
     * 
     * @return true if status is CONFIRMED or PENDING
     */
    public boolean isActive() {
        return status == SubscriptionStatus.CONFIRMED || status == SubscriptionStatus.PENDING;
    }
}