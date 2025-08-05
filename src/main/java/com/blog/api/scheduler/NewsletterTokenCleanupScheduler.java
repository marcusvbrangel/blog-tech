package com.blog.api.scheduler;

import com.blog.api.repository.NewsletterTokenRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Scheduled task for cleaning up expired and used newsletter tokens.
 * Maintains database hygiene and security by removing old tokens.
 * 
 * @author AI-Driven Development
 * @version 1.0
 * @since 2025-08-05
 */
@Component
@ConditionalOnProperty(value = "blog.newsletter.cleanup.enabled", havingValue = "true", matchIfMissing = true)
public class NewsletterTokenCleanupScheduler {

    private static final Logger logger = LoggerFactory.getLogger(NewsletterTokenCleanupScheduler.class);

    @Autowired
    private NewsletterTokenRepository tokenRepository;

    @Value("${blog.newsletter.cleanup.old-tokens-retention-days:30}")
    private int oldTokensRetentionDays;

    /**
     * Clean up expired newsletter tokens.
     * Runs every 6 hours by default.
     */
    @Scheduled(cron = "${blog.newsletter.cleanup.expired-tokens-cron:0 0 */6 * * *}")
    @Transactional
    public void cleanupExpiredTokens() {
        logger.info("Starting cleanup of expired newsletter tokens");
        
        try {
            LocalDateTime now = LocalDateTime.now();
            long deletedCount = tokenRepository.deleteExpiredTokens(now);
            
            if (deletedCount > 0) {
                logger.info("Cleaned up {} expired newsletter tokens", deletedCount);
            } else {
                logger.debug("No expired newsletter tokens found for cleanup");
            }
            
        } catch (Exception e) {
            logger.error("Error during expired newsletter tokens cleanup", e);
        }
    }

    /**
     * Clean up old used newsletter tokens.
     * Runs daily at 2 AM by default.
     * Keeps used tokens for a retention period for audit purposes.
     */
    @Scheduled(cron = "${blog.newsletter.cleanup.used-tokens-cron:0 0 2 * * *}")
    @Transactional
    public void cleanupOldUsedTokens() {
        logger.info("Starting cleanup of old used newsletter tokens (retention: {} days)", oldTokensRetentionDays);
        
        try {
            LocalDateTime cutoffDate = LocalDateTime.now().minusDays(oldTokensRetentionDays);
            long deletedCount = tokenRepository.deleteOldUsedTokens(cutoffDate);
            
            if (deletedCount > 0) {
                logger.info("Cleaned up {} old used newsletter tokens", deletedCount);
            } else {
                logger.debug("No old used newsletter tokens found for cleanup");
            }
            
        } catch (Exception e) {
            logger.error("Error during old used newsletter tokens cleanup", e);
        }
    }

    /**
     * Clean up orphaned tokens (tokens for emails that no longer exist).
     * Runs weekly on Sunday at 3 AM.
     */
    @Scheduled(cron = "0 0 3 * * SUN")
    @Transactional
    public void cleanupOrphanedTokens() {
        logger.info("Starting cleanup of orphaned newsletter tokens");
        
        try {
            long deletedCount = tokenRepository.deleteOrphanedTokens();
            
            if (deletedCount > 0) {
                logger.info("Cleaned up {} orphaned newsletter tokens", deletedCount);
            } else {
                logger.debug("No orphaned newsletter tokens found for cleanup");
            }
            
        } catch (Exception e) {
            logger.error("Error during orphaned newsletter tokens cleanup", e);
        }
    }

    /**
     * Generate cleanup statistics.
     * Runs daily at 1 AM.
     */
    @Scheduled(cron = "0 0 1 * * *")
    @Transactional(readOnly = true)
    public void generateCleanupStatistics() {
        try {
            long totalTokens = tokenRepository.count();
            long expiredTokens = tokenRepository.countExpiredTokens(LocalDateTime.now());
            long usedTokens = tokenRepository.countUsedTokens();
            long activeTokens = totalTokens - expiredTokens - usedTokens;
            
            logger.info("Newsletter token statistics - Total: {}, Active: {}, Expired: {}, Used: {}", 
                       totalTokens, activeTokens, expiredTokens, usedTokens);
                       
        } catch (Exception e) {
            logger.error("Error generating newsletter token statistics", e);
        }
    }
}