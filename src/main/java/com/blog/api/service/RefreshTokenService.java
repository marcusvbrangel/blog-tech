package com.blog.api.service;

import com.blog.api.entity.RefreshToken;
import com.blog.api.entity.User;
import com.blog.api.repository.RefreshTokenRepository;
import com.blog.api.repository.UserRepository;
import com.blog.api.util.JwtUtil;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service for managing refresh tokens in the JWT authentication system.
 * Provides comprehensive refresh token lifecycle management including
 * creation, validation, rotation, and cleanup.
 */
@Service
public class RefreshTokenService {

    private static final Logger logger = LoggerFactory.getLogger(RefreshTokenService.class);

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    // Configuration properties
    @Value("${jwt.refresh-token.expiration:2592000}") // 30 days in seconds
    private long refreshTokenExpiration;

    @Value("${jwt.refresh-token.max-per-user:5}")
    private int maxTokensPerUser;

    @Value("${jwt.refresh-token.cleanup.enabled:true}")
    private boolean cleanupEnabled;

    @Value("${jwt.refresh-token.rotation.enabled:true}")
    private boolean rotationEnabled;

    @Value("${jwt.refresh-token.rate-limit.max-per-hour:10}")
    private int maxTokensPerHour;

    // Metrics registry
    private final MeterRegistry meterRegistry;

    public RefreshTokenService(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    /**
     * Create a new refresh token for a user.
     * Enforces user token limits and rate limiting.
     * 
     * @param userId the user ID
     * @param deviceInfo optional device information
     * @param ipAddress optional IP address
     * @return the created refresh token
     * @throws SecurityException if rate limit exceeded or too many tokens
     */
    @Transactional
    public RefreshToken createRefreshToken(Long userId, String deviceInfo, String ipAddress) {
        validateUserExists(userId);
        enforceRateLimiting(userId);
        enforceTokenLimits(userId);

        try {
            // Generate unique token
            String tokenValue = generateUniqueToken();
            LocalDateTime expiresAt = LocalDateTime.now().plusSeconds(refreshTokenExpiration);

            // Create refresh token
            RefreshToken refreshToken = RefreshToken.createWithDeviceInfo(
                userId, tokenValue, expiresAt, deviceInfo, ipAddress);

            RefreshToken savedToken = refreshTokenRepository.save(refreshToken);

            // Update metrics
            if (meterRegistry != null) {
                meterRegistry.counter("refresh_tokens_created_total").increment();
            }

            logger.info("Created refresh token for user: {}, expires: {}", userId, expiresAt);
            return savedToken;

        } catch (Exception e) {
            logger.error("Failed to create refresh token for user: {}", userId, e);
            throw new RuntimeException("Failed to create refresh token", e);
        }
    }

    /**
     * Refresh an access token using a refresh token.
     * Implements token rotation for enhanced security.
     * 
     * @param refreshTokenValue the refresh token string
     * @return RefreshResponse containing new access token and optionally new refresh token
     * @throws IllegalArgumentException if token is invalid
     * @throws SecurityException if token is revoked or expired
     */
    @Transactional
    public RefreshResponse refreshAccessToken(String refreshTokenValue) {
        validateRefreshTokenParameter(refreshTokenValue);

        try {
            // Find and validate refresh token
            RefreshToken refreshToken = refreshTokenRepository.findActiveByToken(refreshTokenValue)
                .orElseThrow(() -> new SecurityException("Invalid or expired refresh token"));

            // Load user details
            User user = userRepository.findById(refreshToken.getUserId())
                .orElseThrow(() -> new SecurityException("User not found"));

            UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUsername());

            // Generate new access token
            String newAccessToken = jwtUtil.generateToken(userDetails);

            // Mark current refresh token as used
            refreshToken.markAsUsed();
            refreshTokenRepository.save(refreshToken);

            RefreshResponse response;

            if (rotationEnabled) {
                // Create new refresh token and revoke old one
                RefreshToken newRefreshToken = createRefreshToken(
                    user.getId(), refreshToken.getDeviceInfo(), refreshToken.getIpAddress());
                
                refreshToken.revoke();
                refreshTokenRepository.save(refreshToken);

                response = new RefreshResponse(newAccessToken, newRefreshToken.getToken(), user.getId(), user.getUsername());
                logger.info("Rotated refresh token for user: {}", user.getId());
            } else {
                // Keep existing refresh token
                response = new RefreshResponse(newAccessToken, refreshTokenValue, user.getId(), user.getUsername());
            }

            // Update metrics
            if (meterRegistry != null) {
                meterRegistry.counter("refresh_tokens_used_total").increment();
            }

            logger.info("Successfully refreshed access token for user: {}", user.getId());
            return response;

        } catch (SecurityException e) {
            // Update metrics for invalid attempts
            if (meterRegistry != null) {
                meterRegistry.counter("refresh_tokens_invalid_attempts_total").increment();
            }
            logger.warn("Invalid refresh token attempt: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error refreshing access token", e);
            throw new RuntimeException("Failed to refresh access token", e);
        }
    }

    /**
     * Revoke a specific refresh token.
     * 
     * @param refreshTokenValue the refresh token to revoke
     * @return true if token was found and revoked
     */
    @Transactional
    public boolean revokeRefreshToken(String refreshTokenValue) {
        if (refreshTokenValue == null || refreshTokenValue.trim().isEmpty()) {
            return false;
        }

        try {
            int revokedCount = refreshTokenRepository.revokeByToken(refreshTokenValue);
            
            if (revokedCount > 0) {
                if (meterRegistry != null) {
                    meterRegistry.counter("refresh_tokens_revoked_total", "reason", "manual").increment();
                }
                logger.info("Revoked refresh token");
                return true;
            }
            return false;

        } catch (Exception e) {
            logger.error("Error revoking refresh token", e);
            return false;
        }
    }

    /**
     * Revoke all refresh tokens for a user.
     * Used when user changes password or requests logout from all devices.
     * 
     * @param userId the user ID
     * @return number of tokens revoked
     */
    @Transactional
    public int revokeAllUserTokens(Long userId) {
        validateUserExists(userId);

        try {
            int revokedCount = refreshTokenRepository.revokeAllByUserId(userId);
            
            if (revokedCount > 0) {
                if (meterRegistry != null) {
                    meterRegistry.counter("refresh_tokens_revoked_total", "reason", "all_user_tokens")
                        .increment(revokedCount);
                }
                logger.info("Revoked {} refresh tokens for user: {}", revokedCount, userId);
            }
            
            return revokedCount;

        } catch (Exception e) {
            logger.error("Error revoking all tokens for user: {}", userId, e);
            throw new RuntimeException("Failed to revoke user tokens", e);
        }
    }

    /**
     * Get active refresh tokens for a user.
     * Used for displaying active sessions to users.
     * 
     * @param userId the user ID
     * @return List of active refresh tokens
     */
    public List<RefreshToken> getUserActiveTokens(Long userId) {
        validateUserExists(userId);
        return refreshTokenRepository.findActiveByUserId(userId);
    }

    /**
     * Validate if a refresh token is valid and active.
     * 
     * @param refreshTokenValue the refresh token string
     * @return true if token is valid and active
     */
    public boolean isValidRefreshToken(String refreshTokenValue) {
        if (refreshTokenValue == null || refreshTokenValue.trim().isEmpty()) {
            return false;
        }

        try {
            return refreshTokenRepository.existsActiveByToken(refreshTokenValue);
        } catch (Exception e) {
            logger.error("Error validating refresh token", e);
            return false;
        }
    }

    /**
     * Scheduled cleanup of expired and old revoked tokens.
     * Runs daily at 3 AM to maintain database performance.
     */
    @Scheduled(cron = "${jwt.refresh-token.cleanup.cron:0 0 3 * * *}")
    @Transactional
    public void cleanupExpiredTokens() {
        if (!cleanupEnabled) {
            logger.debug("Refresh token cleanup is disabled");
            return;
        }

        try {
            LocalDateTime now = LocalDateTime.now();
            
            // Delete expired tokens
            int expiredDeleted = refreshTokenRepository.deleteExpiredTokens(now);
            
            // Delete old revoked tokens (older than 30 days)
            LocalDateTime cutoffDate = now.minusDays(30);
            int revokedDeleted = refreshTokenRepository.deleteOldRevokedTokens(cutoffDate);
            
            int totalDeleted = expiredDeleted + revokedDeleted;
            
            if (totalDeleted > 0) {
                logger.info("Cleanup completed: {} expired tokens, {} old revoked tokens deleted", 
                           expiredDeleted, revokedDeleted);
            } else {
                logger.debug("Cleanup completed: no tokens to delete");
            }

            // Update metrics
            if (meterRegistry != null) {
                meterRegistry.counter("refresh_tokens_cleaned_total", "type", "expired")
                    .increment(expiredDeleted);
                meterRegistry.counter("refresh_tokens_cleaned_total", "type", "old_revoked")
                    .increment(revokedDeleted);
            }

        } catch (Exception e) {
            logger.error("Error during refresh token cleanup", e);
        }
    }

    /**
     * Update monitoring metrics for active tokens.
     * Called periodically to update metrics.
     */
    @Scheduled(fixedRate = 300000) // Every 5 minutes
    public void updateTokenMetrics() {
        try {
            long activeCount = refreshTokenRepository.countTotalActiveTokens();
            
            if (meterRegistry != null) {
                meterRegistry.gauge("refresh_tokens_active_total", activeCount);
            }
            
            logger.trace("Updated refresh token metrics: {} active tokens", activeCount);
        } catch (Exception e) {
            logger.error("Error updating refresh token metrics", e);
        }
    }

    // Private helper methods

    private String generateUniqueToken() {
        // Generate a secure random UUID-based token
        return UUID.randomUUID().toString().replace("-", "") + 
               UUID.randomUUID().toString().replace("-", "");
    }

    private void validateUserExists(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }
        
        if (!userRepository.existsById(userId)) {
            throw new IllegalArgumentException("User not found with ID: " + userId);
        }
    }

    private void validateRefreshTokenParameter(String refreshTokenValue) {
        if (refreshTokenValue == null || refreshTokenValue.trim().isEmpty()) {
            throw new IllegalArgumentException("Refresh token cannot be null or empty");
        }
    }

    private void enforceRateLimiting(Long userId) {
        LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);
        boolean rateLimitExceeded = refreshTokenRepository.hasUserExceededTokenCreationRate(
            userId, maxTokensPerHour, oneHourAgo);
        
        if (rateLimitExceeded) {
            logger.warn("Refresh token creation rate limit exceeded for user: {}", userId);
            throw new SecurityException("Token creation rate limit exceeded");
        }
    }

    private void enforceTokenLimits(Long userId) {
        long activeTokenCount = refreshTokenRepository.countActiveByUserId(userId);
        
        if (activeTokenCount >= maxTokensPerUser) {
            logger.warn("Maximum active tokens limit reached for user: {} ({})", userId, activeTokenCount);
            
            // Option 1: Throw exception (strict enforcement)
            // throw new SecurityException("Maximum active tokens limit reached");
            
            // Option 2: Revoke oldest token (graceful handling)
            List<RefreshToken> userTokens = refreshTokenRepository.findActiveByUserId(userId);
            if (!userTokens.isEmpty()) {
                RefreshToken oldestToken = userTokens.get(userTokens.size() - 1);
                refreshTokenRepository.revokeByToken(oldestToken.getToken());
                logger.info("Revoked oldest token for user: {} to enforce limit", userId);
            }
        }
    }

    /**
     * Response class for refresh operations.
     */
    public static class RefreshResponse {
        private final String accessToken;
        private final String refreshToken;
        private final Long userId;
        private final String username;

        public RefreshResponse(String accessToken, String refreshToken, Long userId, String username) {
            this.accessToken = accessToken;
            this.refreshToken = refreshToken;  
            this.userId = userId;
            this.username = username;
        }

        public String getAccessToken() {
            return accessToken;
        }

        public String getRefreshToken() {
            return refreshToken;
        }
        
        public Long getUserId() {
            return userId;
        }
        
        public String getUsername() {
            return username;
        }
    }
}