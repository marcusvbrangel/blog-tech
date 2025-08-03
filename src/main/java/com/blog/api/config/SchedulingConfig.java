package com.blog.api.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Configuration to enable Spring's scheduled task execution capability.
 * This enables the @Scheduled annotations in the application, particularly
 * for JWT token cleanup operations.
 */
@Configuration
@EnableScheduling
public class SchedulingConfig {
    // Configuration class to enable scheduling
    // The actual scheduled methods are in JwtBlacklistService:
    // - cleanupExpiredTokens() - runs daily at 2 AM
    // - updateActiveTokensGauge() - runs every 5 minutes
}