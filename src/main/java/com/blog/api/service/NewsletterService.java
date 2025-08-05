package com.blog.api.service;

import com.blog.api.dto.NewsletterSubscriptionRequest;
import com.blog.api.dto.NewsletterSubscriptionResponse;
import com.blog.api.entity.NewsletterSubscriber;
import com.blog.api.entity.SubscriptionStatus;
import com.blog.api.exception.BadRequestException;
import com.blog.api.repository.NewsletterSubscriberRepository;
import io.micrometer.core.annotation.Timed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service class for newsletter subscription management.
 * Handles subscription logic, LGPD compliance, and email confirmations.
 * 
 * @author AI-Driven Development
 * @version 1.0
 * @since 2025-08-05
 */
@Service
@Transactional
public class NewsletterService {

    private static final Logger logger = LoggerFactory.getLogger(NewsletterService.class);

    @Autowired
    private NewsletterSubscriberRepository subscriberRepository;

    @Autowired
    private EmailService emailService;

    /**
     * Process a newsletter subscription request.
     * Handles new subscriptions, duplicate email validation, and resubscriptions.
     * 
     * @param request the subscription request with LGPD consent data
     * @return NewsletterSubscriptionResponse with subscription result
     * @throws BadRequestException if email already exists with active subscription
     */
    @Timed(value = "newsletter.subscription.time", description = "Time taken to process newsletter subscription")
    public NewsletterSubscriptionResponse subscribe(NewsletterSubscriptionRequest request) {
        logger.info("Processing newsletter subscription for email: {}", request.email());

        // Validate LGPD consent
        if (!request.hasValidConsent()) {
            logger.warn("Newsletter subscription rejected - no valid LGPD consent for email: {}", request.email());
            throw new BadRequestException("Explicit consent is required for LGPD compliance");
        }

        // Check for existing subscription
        Optional<NewsletterSubscriber> existingSubscriber = subscriberRepository.findByEmail(request.email());
        
        if (existingSubscriber.isPresent()) {
            return handleExistingSubscriber(existingSubscriber.get(), request);
        }

        // Create new subscription
        return createNewSubscription(request);
    }

    /**
     * Handle existing subscriber scenarios.
     * 
     * @param existing the existing subscriber
     * @param request the new subscription request
     * @return appropriate response based on current status
     */
    private NewsletterSubscriptionResponse handleExistingSubscriber(NewsletterSubscriber existing, NewsletterSubscriptionRequest request) {
        switch (existing.getStatus()) {
            case CONFIRMED:
                logger.info("Email {} already confirmed, returning existing subscription", request.email());
                return NewsletterSubscriptionResponse.alreadySubscribed(existing);
                
            case PENDING:
                logger.info("Email {} already pending confirmation, resending confirmation email", request.email());
                sendConfirmationEmailAsync(existing);
                return NewsletterSubscriptionResponse.alreadySubscribed(existing);
                
            case UNSUBSCRIBED:
                logger.info("Reactivating unsubscribed email: {}", request.email());
                return reactivateSubscription(existing, request);
                
            case DELETED:
                logger.info("Creating new subscription for previously deleted email: {}", request.email());
                // Soft-deleted subscriber can resubscribe as new
                return createNewSubscription(request);
                
            default:
                logger.warn("Unknown subscription status {} for email: {}", existing.getStatus(), request.email());
                throw new BadRequestException("Invalid subscription status");
        }
    }

    /**
     * Create a new newsletter subscription.
     * 
     * @param request the subscription request
     * @return NewsletterSubscriptionResponse for the new subscription
     */
    private NewsletterSubscriptionResponse createNewSubscription(NewsletterSubscriptionRequest request) {
        try {
            // Create new subscriber with LGPD compliance data
            NewsletterSubscriber subscriber = NewsletterSubscriber.withConsent(
                    request.email(),
                    request.ipAddress(),
                    request.userAgent(),
                    request.privacyPolicyVersion()
                )
                .status(SubscriptionStatus.PENDING)
                .build();

            // Save to database
            subscriber = subscriberRepository.save(subscriber);
            
            logger.info("Created new newsletter subscription with ID: {} for email: {}", 
                       subscriber.getId(), subscriber.getEmail());

            // Send confirmation email asynchronously
            sendConfirmationEmailAsync(subscriber);

            // Log LGPD consent
            logConsentGiven(subscriber, request);

            // Invalidate confirmed subscribers cache
            invalidateConfirmedSubscribersCache();

            return NewsletterSubscriptionResponse.success(subscriber);

        } catch (DataIntegrityViolationException e) {
            logger.error("Database constraint violation while creating subscription for email: {}", request.email(), e);
            throw new BadRequestException("Email already exists in our system");
        } catch (Exception e) {
            logger.error("Unexpected error while creating subscription for email: {}", request.email(), e);
            throw new RuntimeException("Failed to process subscription request", e);
        }
    }

    /**
     * Reactivate an unsubscribed newsletter subscription.
     * 
     * @param existing the existing unsubscribed subscriber
     * @param request the resubscription request
     * @return NewsletterSubscriptionResponse for the reactivated subscription
     */
    private NewsletterSubscriptionResponse reactivateSubscription(NewsletterSubscriber existing, NewsletterSubscriptionRequest request) {
        // Update subscriber with new consent data
        existing.setStatus(SubscriptionStatus.PENDING);
        existing.setConsentGivenAt(LocalDateTime.now());
        existing.setConsentIpAddress(request.ipAddress());
        existing.setConsentUserAgent(request.userAgent());
        existing.setPrivacyPolicyVersion(request.privacyPolicyVersion());
        existing.setUnsubscribedAt(null); // Clear unsubscribe timestamp

        // Save updated subscriber
        existing = subscriberRepository.save(existing);
        
        logger.info("Reactivated subscription with ID: {} for email: {}", 
                   existing.getId(), existing.getEmail());

        // Send confirmation email asynchronously
        sendConfirmationEmailAsync(existing);

        // Log LGPD consent
        logConsentGiven(existing, request);

        // Invalidate confirmed subscribers cache
        invalidateConfirmedSubscribersCache();

        return NewsletterSubscriptionResponse.resubscribed(existing);
    }

    /**
     * Send confirmation email asynchronously.
     * 
     * @param subscriber the subscriber to send confirmation to
     */
    @Async
    public void sendConfirmationEmailAsync(NewsletterSubscriber subscriber) {
        try {
            logger.info("Sending confirmation email to: {}", subscriber.getEmail());
            // TODO: Implement email sending logic in US02
            // emailService.sendNewsletterConfirmation(subscriber);
            logger.info("Confirmation email queued for: {}", subscriber.getEmail());
        } catch (Exception e) {
            logger.error("Failed to send confirmation email to: {}", subscriber.getEmail(), e);
            // Don't throw exception to avoid breaking the subscription flow
        }
    }

    /**
     * Log LGPD consent information for audit purposes.
     * 
     * @param subscriber the subscriber
     * @param request the original request
     */
    private void logConsentGiven(NewsletterSubscriber subscriber, NewsletterSubscriptionRequest request) {
        logger.info("LGPD Consent logged - Email: {}, Consent: {}, Policy Version: {}, IP: {}, Timestamp: {}", 
                   subscriber.getEmail(),
                   request.consentToReceiveEmails(),
                   request.privacyPolicyVersion(),
                   request.ipAddress(),
                   subscriber.getConsentGivenAt());
    }

    /**
     * Get all confirmed subscribers for newsletter sending.
     * Cached for performance.
     * 
     * @return list of confirmed subscribers
     */
    @Cacheable(value = "confirmedSubscribers", key = "'all'")
    @Transactional(readOnly = true)
    public List<NewsletterSubscriber> getConfirmedSubscribers() {
        logger.debug("Fetching all confirmed subscribers from database");
        return subscriberRepository.findConfirmedSubscribers();
    }

    /**
     * Get subscriber by email.
     * 
     * @param email the email address
     * @return Optional containing the subscriber if found
     */
    @Transactional(readOnly = true)
    public Optional<NewsletterSubscriber> getSubscriberByEmail(String email) {
        return subscriberRepository.findByEmail(email);
    }

    /**
     * Check if email exists with active subscription.
     * 
     * @param email the email to check
     * @return true if active subscription exists
     */
    @Transactional(readOnly = true)
    public boolean hasActiveSubscription(String email) {
        return subscriberRepository.existsByEmailAndNotDeleted(email);
    }

    /**
     * Get subscription statistics.
     * 
     * @return statistics by status
     */
    @Cacheable(value = "subscriptionStats", key = "'all'")
    @Transactional(readOnly = true)
    public List<Object[]> getSubscriptionStatistics() {
        logger.debug("Fetching subscription statistics");
        return subscriberRepository.getSubscriptionStatistics();
    }

    /**
     * Invalidate confirmed subscribers cache.
     * Called when subscription status changes.
     */
    @CacheEvict(value = "confirmedSubscribers", allEntries = true)
    public void invalidateConfirmedSubscribersCache() {
        logger.debug("Invalidated confirmed subscribers cache");
    }

    /**
     * Invalidate all newsletter caches.
     */
    @CacheEvict(value = {"confirmedSubscribers", "subscriptionStats"}, allEntries = true)
    public void invalidateAllCaches() {
        logger.debug("Invalidated all newsletter caches");
    }
}