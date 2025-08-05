package com.blog.api.service;

import com.blog.api.entity.NewsletterToken;
import com.blog.api.entity.NewsletterTokenType;
import com.blog.api.exception.BadRequestException;
import com.blog.api.repository.NewsletterTokenRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service for managing newsletter tokens (confirmation, unsubscribe, data request).
 * Handles token generation, validation, cleanup and rate limiting.
 * 
 * @author AI-Driven Development
 * @version 1.0
 * @since 2025-08-05
 */
@Service
@Transactional
public class NewsletterTokenService {

    private static final Logger logger = LoggerFactory.getLogger(NewsletterTokenService.class);

    @Autowired
    private NewsletterTokenRepository tokenRepository;

    // Token expiration settings - different TTLs per token type
    @Value("${blog.newsletter.confirmation.token-expiration:48h}")
    private Duration confirmationTokenExpiration;

    @Value("${blog.newsletter.unsubscribe.token-expiration:365d}")
    private Duration unsubscribeTokenExpiration;

    @Value("${blog.newsletter.data-request.token-expiration:7d}")
    private Duration dataRequestTokenExpiration;

    // Rate limiting settings
    @Value("${blog.newsletter.confirmation.max-attempts-per-hour:3}")
    private int maxConfirmationAttemptsPerHour;

    @Value("${blog.newsletter.unsubscribe.max-attempts-per-hour:2}")
    private int maxUnsubscribeAttemptsPerHour;

    @Value("${blog.newsletter.data-request.max-attempts-per-day:1}")
    private int maxDataRequestAttemptsPerDay;

    /**
     * Generate confirmation token for newsletter subscription.
     * Used after user subscribes to newsletter.
     * 
     * @param email the email address
     * @param ipAddress client IP address (optional)
     * @param userAgent client user agent (optional)
     * @return the generated token
     */
    public NewsletterToken generateConfirmationToken(String email, String ipAddress, String userAgent) {
        // Check rate limiting
        checkRateLimit(email, NewsletterTokenType.CONFIRMATION, maxConfirmationAttemptsPerHour, 1);

        // Invalidate existing confirmation tokens for this email
        invalidateExistingTokens(email, NewsletterTokenType.CONFIRMATION);

        // Generate new token with 48h expiration
        NewsletterToken token = NewsletterToken.forConfirmation(email, ipAddress, userAgent)
                .expiresIn(confirmationTokenExpiration.toHours())
                .build();

        tokenRepository.save(token);

        logger.info("Confirmation token generated for email: {}", email);
        return token;
    }

    /**
     * Generate unsubscribe token for newsletter.
     * Used when user wants to unsubscribe from newsletter.
     * 
     * @param email the email address
     * @param ipAddress client IP address (optional)
     * @param userAgent client user agent (optional)
     * @return the generated token
     */
    public NewsletterToken generateUnsubscribeToken(String email, String ipAddress, String userAgent) {
        // Check rate limiting (less restrictive for unsubscribe)
        checkRateLimit(email, NewsletterTokenType.UNSUBSCRIBE, maxUnsubscribeAttemptsPerHour, 1);

        // Don't invalidate existing unsubscribe tokens - they should remain valid

        // Generate new token with 1 year expiration
        NewsletterToken token = NewsletterToken.forUnsubscribe(email, ipAddress, userAgent)
                .expiresIn(unsubscribeTokenExpiration.toHours())
                .build();

        tokenRepository.save(token);

        logger.info("Unsubscribe token generated for email: {}", email);
        return token;
    }

    /**
     * Generate data request token for LGPD compliance.
     * Used when user requests their data or data deletion.
     * 
     * @param email the email address
     * @param ipAddress client IP address (optional)
     * @param userAgent client user agent (optional)
     * @return the generated token
     */
    public NewsletterToken generateDataRequestToken(String email, String ipAddress, String userAgent) {
        // Check rate limiting (once per day for data requests)
        checkRateLimit(email, NewsletterTokenType.DATA_REQUEST, maxDataRequestAttemptsPerDay, 24);

        // Invalidate existing data request tokens
        invalidateExistingTokens(email, NewsletterTokenType.DATA_REQUEST);

        // Generate new token with 7 days expiration
        NewsletterToken token = NewsletterToken.forDataRequest(email, ipAddress, userAgent)
                .expiresIn(dataRequestTokenExpiration.toHours())
                .build();

        tokenRepository.save(token);

        logger.info("Data request token generated for email: {}", email);
        return token;
    }

    /**
     * Validate and retrieve token.
     * Used to verify tokens before processing actions.
     * 
     * @param tokenValue the token string
     * @param expectedType the expected token type
     * @return the valid token
     * @throws BadRequestException if token is invalid, expired, or used
     */
    @Cacheable(value = "newsletter_tokens", key = "#tokenValue + '_' + #expectedType")
    public NewsletterToken validateToken(String tokenValue, NewsletterTokenType expectedType) {
        NewsletterToken token = tokenRepository.findByTokenAndTokenType(tokenValue, expectedType)
                .orElseThrow(() -> new BadRequestException("Invalid or expired newsletter token"));

        if (!token.isValid()) {
            if (token.isUsed()) {
                throw new BadRequestException("Newsletter token has already been used");
            } else if (token.isExpired()) {
                throw new BadRequestException("Newsletter token has expired");
            }
        }

        return token;
    }

    /**
     * Mark token as used.
     * Called after successfully processing token action.
     * 
     * @param tokenValue the token string
     * @param tokenType the token type
     * @return the used token
     */
    @CacheEvict(value = "newsletter_tokens", key = "#tokenValue + '_' + #tokenType")
    public NewsletterToken markTokenAsUsed(String tokenValue, NewsletterTokenType tokenType) {
        NewsletterToken token = validateToken(tokenValue, tokenType);
        token.markAsUsed();
        tokenRepository.save(token);

        // Clean up old tokens for this email
        cleanupEmailTokens(token.getEmail(), tokenType);

        logger.info("Newsletter token marked as used for email: {} (type: {})", token.getEmail(), tokenType);
        return token;
    }

    /**
     * Check if email can request new token (rate limiting).
     * 
     * @param email the email address
     * @param tokenType the token type
     * @return true if can request new token
     */
    public boolean canRequestNewToken(String email, NewsletterTokenType tokenType) {
        int maxAttempts = getMaxAttemptsForType(tokenType);
        int hours = getHoursForRateLimit(tokenType);
        
        LocalDateTime cutoffTime = LocalDateTime.now().minusHours(hours);
        long recentTokens = tokenRepository.countTokensCreatedSince(email, tokenType, cutoffTime);
        
        return recentTokens < maxAttempts;
    }

    /**
     * Get time until email can request new token.
     * 
     * @param email the email address
     * @param tokenType the token type
     * @return next allowed request time if rate limited
     */
    public Optional<LocalDateTime> getNextAllowedRequestTime(String email, NewsletterTokenType tokenType) {
        if (canRequestNewToken(email, tokenType)) {
            return Optional.empty();
        }

        int hours = getHoursForRateLimit(tokenType);
        LocalDateTime cutoffTime = LocalDateTime.now().minusHours(hours);
        
        // Find tokens in the rate limit window
        List<NewsletterToken> recentTokens = tokenRepository.findByEmailAndTokenType(email, tokenType)
                .stream()
                .filter(token -> token.getCreatedAt().isAfter(cutoffTime))
                .sorted((t1, t2) -> t1.getCreatedAt().compareTo(t2.getCreatedAt()))
                .toList();

        if (!recentTokens.isEmpty()) {
            return Optional.of(recentTokens.get(0).getCreatedAt().plusHours(hours));
        }

        return Optional.empty();
    }

    /**
     * Check if email has valid token of specific type.
     * 
     * @param email the email address
     * @param tokenType the token type
     * @return true if has valid token
     */
    public boolean hasValidToken(String email, NewsletterTokenType tokenType) {
        return tokenRepository.hasValidToken(email, tokenType, LocalDateTime.now());
    }

    /**
     * Get most recent valid token for email and type.
     * 
     * @param email the email address
     * @param tokenType the token type
     * @return the most recent valid token if exists
     */
    public Optional<NewsletterToken> getMostRecentValidToken(String email, NewsletterTokenType tokenType) {
        return tokenRepository.findMostRecentValidToken(email, tokenType, LocalDateTime.now());
    }

    /**
     * Get all tokens for email (for admin/debugging purposes).
     * 
     * @param email the email address
     * @return list of all tokens for email
     */
    public List<NewsletterToken> getTokensForEmail(String email) {
        return tokenRepository.findByEmail(email);
    }

    /**
     * Delete all tokens for email (LGPD compliance).
     * 
     * @param email the email address
     * @return number of deleted tokens
     */
    @CacheEvict(value = "newsletter_tokens", allEntries = true)
    public int deleteAllTokensForEmail(String email) {
        int deletedCount = tokenRepository.deleteByEmail(email);
        logger.info("Deleted {} newsletter tokens for email: {}", deletedCount, email);
        return deletedCount;
    }

    /**
     * Generate secure random token.
     * 
     * @return UUID token without hyphens
     */
    private String generateSecureToken() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * Check rate limiting for token generation.
     * 
     * @param email the email address
     * @param tokenType the token type
     * @param maxAttempts maximum attempts allowed
     * @param hours hours for rate limit window
     */
    private void checkRateLimit(String email, NewsletterTokenType tokenType, int maxAttempts, int hours) {
        if (!canRequestNewToken(email, tokenType)) {
            Optional<LocalDateTime> nextAllowed = getNextAllowedRequestTime(email, tokenType);
            String message = nextAllowed.map(time -> 
                String.format("Too many requests. Try again after %s", time))
                .orElse("Too many requests. Please try again later.");
            throw new BadRequestException(message);
        }
    }

    /**
     * Invalidate existing tokens of the same type for email.
     * 
     * @param email the email address
     * @param tokenType the token type
     */
    private void invalidateExistingTokens(String email, NewsletterTokenType tokenType) {
        List<NewsletterToken> existingTokens = tokenRepository
                .findValidTokensByEmailAndType(email, tokenType, LocalDateTime.now());
        
        existingTokens.forEach(token -> {
            token.markAsUsed();
            tokenRepository.save(token);
        });

        if (!existingTokens.isEmpty()) {
            logger.info("Invalidated {} existing tokens of type {} for email: {}", 
                existingTokens.size(), tokenType, email);
        }
    }

    /**
     * Clean up old tokens for email.
     * 
     * @param email the email address
     * @param tokenType the token type
     */
    @Async
    private void cleanupEmailTokens(String email, NewsletterTokenType tokenType) {
        try {
            LocalDateTime cutoffDate = LocalDateTime.now().minusDays(30);
            List<NewsletterToken> oldTokens = tokenRepository.findByEmailAndTokenType(email, tokenType)
                    .stream()
                    .filter(token -> token.getCreatedAt() != null && token.getCreatedAt().isBefore(cutoffDate))
                    .toList();

            if (!oldTokens.isEmpty()) {
                tokenRepository.deleteAll(oldTokens);
                logger.info("Cleaned up {} old tokens for email: {} (type: {})", 
                    oldTokens.size(), email, tokenType);
            }
        } catch (Exception e) {
            logger.error("Error cleaning up tokens for email: {} (type: {})", email, tokenType, e);
        }
    }

    /**
     * Get maximum attempts for token type.
     * 
     * @param tokenType the token type
     * @return maximum attempts allowed
     */
    private int getMaxAttemptsForType(NewsletterTokenType tokenType) {
        return switch (tokenType) {
            case CONFIRMATION -> maxConfirmationAttemptsPerHour;
            case UNSUBSCRIBE -> maxUnsubscribeAttemptsPerHour;
            case DATA_REQUEST -> maxDataRequestAttemptsPerDay;
        };
    }

    /**
     * Get hours for rate limit window.
     * 
     * @param tokenType the token type
     * @return hours for rate limit window
     */
    private int getHoursForRateLimit(NewsletterTokenType tokenType) {
        return switch (tokenType) {
            case CONFIRMATION, UNSUBSCRIBE -> 1; // 1 hour window
            case DATA_REQUEST -> 24; // 24 hour window
        };
    }

    /**
     * Scheduled cleanup of expired tokens (runs every hour).
     */
    @Scheduled(fixedRate = 3600000) // 1 hour in milliseconds
    public void cleanupExpiredTokens() {
        try {
            int deletedCount = tokenRepository.deleteExpiredTokens(LocalDateTime.now());
            if (deletedCount > 0) {
                logger.info("Cleaned up {} expired newsletter tokens", deletedCount);
            }
        } catch (Exception e) {
            logger.error("Error during scheduled newsletter token cleanup", e);
        }
    }

    /**
     * Scheduled cleanup of old used tokens (runs daily).
     */
    @Scheduled(fixedRate = 86400000) // 24 hours in milliseconds
    public void cleanupOldUsedTokens() {
        try {
            LocalDateTime cutoffDate = LocalDateTime.now().minusDays(30);
            int deletedCount = tokenRepository.deleteUsedTokensOlderThan(cutoffDate);
            if (deletedCount > 0) {
                logger.info("Cleaned up {} old used newsletter tokens", deletedCount);
            }
        } catch (Exception e) {
            logger.error("Error during scheduled old newsletter token cleanup", e);
        }
    }

    /**
     * Find tokens about to expire for reminder notifications.
     * 
     * @param tokenType the token type
     * @param hoursBeforeExpiration hours before expiration to send reminder
     * @return list of tokens about to expire
     */
    public List<NewsletterToken> findTokensAboutToExpire(NewsletterTokenType tokenType, int hoursBeforeExpiration) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime reminderTime = now.plusHours(hoursBeforeExpiration);
        
        return tokenRepository.findTokensAboutToExpire(tokenType, reminderTime, now);
    }

    /**
     * Get statistics for monitoring.
     * 
     * @return token statistics
     */
    public NewsletterTokenStatistics getTokenStatistics() {
        LocalDateTime now = LocalDateTime.now();
        
        long validConfirmationTokens = tokenRepository.countValidTokensByType(NewsletterTokenType.CONFIRMATION, now);
        long validUnsubscribeTokens = tokenRepository.countValidTokensByType(NewsletterTokenType.UNSUBSCRIBE, now);
        long validDataRequestTokens = tokenRepository.countValidTokensByType(NewsletterTokenType.DATA_REQUEST, now);
        
        long usedConfirmationTokens = tokenRepository.countUsedTokensByType(NewsletterTokenType.CONFIRMATION);
        long usedUnsubscribeTokens = tokenRepository.countUsedTokensByType(NewsletterTokenType.UNSUBSCRIBE);
        long usedDataRequestTokens = tokenRepository.countUsedTokensByType(NewsletterTokenType.DATA_REQUEST);
        
        return new NewsletterTokenStatistics(
            validConfirmationTokens,
            validUnsubscribeTokens,
            validDataRequestTokens,
            usedConfirmationTokens,
            usedUnsubscribeTokens,
            usedDataRequestTokens
        );
    }

    /**
     * Statistics record for monitoring newsletter tokens.
     */
    public record NewsletterTokenStatistics(
        long validConfirmationTokens,
        long validUnsubscribeTokens,
        long validDataRequestTokens,
        long usedConfirmationTokens,
        long usedUnsubscribeTokens,
        long usedDataRequestTokens
    ) {
        public long totalValidTokens() {
            return validConfirmationTokens + validUnsubscribeTokens + validDataRequestTokens;
        }
        
        public long totalUsedTokens() {
            return usedConfirmationTokens + usedUnsubscribeTokens + usedDataRequestTokens;
        }
    }
}