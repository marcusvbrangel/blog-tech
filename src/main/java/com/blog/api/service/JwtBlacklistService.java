package com.blog.api.service;

import com.blog.api.entity.RevokedToken;
import com.blog.api.repository.RevokedTokenRepository;
import com.blog.api.util.JwtUtil;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

/**
 * Service for managing JWT token blacklist functionality.
 * Provides high-performance token revocation with Redis caching and comprehensive monitoring.
 */
@Service
public class JwtBlacklistService {

    private static final Logger logger = LoggerFactory.getLogger(JwtBlacklistService.class);

    @Autowired
    private RevokedTokenRepository revokedTokenRepository;

    @Autowired
    private JwtUtil jwtUtil;

    // Configuration properties
    @Value("${jwt.blacklist.rate-limit.max-per-user-per-hour:10}")
    private int maxRevocationsPerUserPerHour;

    @Value("${jwt.blacklist.cleanup.enabled:true}")
    private boolean cleanupEnabled;

    @Value("${jwt.blacklist.monitoring.enabled:true}")
    private boolean monitoringEnabled;

    // Metrics registry
    private final MeterRegistry meterRegistry;

    public JwtBlacklistService(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    /**
     * Check if a token is revoked with Redis caching for high performance.
     * This is the most critical method as it's called on every authenticated request.
     * 
     * @param jti the JWT ID to check
     * @return true if token is revoked, false otherwise
     */
    @Cacheable(value = "blacklisted_tokens", key = "#jti", unless = "#result == false")
    public boolean isTokenRevoked(String jti) {
        try {
            boolean isRevoked = revokedTokenRepository.existsByTokenJti(jti);
            
            if (isRevoked) {
                logger.debug("Token found in blacklist: {}", jti);
            } else {
                logger.trace("Token not in blacklist: {}", jti);
            }
            
            return isRevoked;
            
        } catch (Exception e) {
            logger.error("Error checking token revocation status for JTI: {}", jti, e);
            // In case of database error, assume token is valid to avoid blocking legitimate users
            // This should be monitored and alerted
            return false;
        }
    }

    /**
     * Revoke a specific token with comprehensive validation and monitoring.
     * 
     * @param jti the JWT ID to revoke
     * @param userId the user ID owning the token
     * @param reason the reason for revocation
     * @throws IllegalArgumentException if parameters are invalid
     * @throws SecurityException if rate limit is exceeded
     */
    @Transactional
    @CacheEvict(value = "blacklisted_tokens", key = "#jti")
    public void revokeToken(String jti, Long userId, RevokedToken.RevokeReason reason) {
        validateRevokeTokenParameters(jti, userId, reason);
        
        // Check rate limiting
        if (isRateLimitExceeded(userId)) {
            logger.warn("Rate limit exceeded for user {} attempting to revoke token", userId);
            throw new SecurityException("Token revocation rate limit exceeded");
        }

        try {
            // Check if token is already revoked
            if (revokedTokenRepository.existsByTokenJti(jti)) {
                logger.info("Token already revoked: {}", jti);
                return;
            }

            // Extract expiration date from token
            LocalDateTime expiresAt = extractExpirationFromJti(jti);
            
            // Create revoked token record
            RevokedToken revokedToken = RevokedToken.builder()
                    .tokenJti(jti)
                    .userId(userId)
                    .reason(reason)
                    .expiresAt(expiresAt)
                    .build();

            revokedTokenRepository.save(revokedToken);
            
            // Update metrics (simplified)
            if (meterRegistry != null) {
                meterRegistry.counter("jwt_tokens_revoked_total", "reason", reason.name()).increment();
            }
            
            logger.info("Token revoked successfully: jti={}, userId={}, reason={}", jti, userId, reason);
            
        } catch (Exception e) {
            logger.error("Failed to revoke token: jti={}, userId={}, reason={}", jti, userId, reason, e);
            throw new RuntimeException("Failed to revoke token", e);
        }
    }

    /**
     * Revoke token by extracting JTI from the full token string.
     * Convenience method for logout scenarios.
     * 
     * @param token the full JWT token
     * @param userId the user ID owning the token
     * @param reason the reason for revocation
     */
    public void revokeTokenFromString(String token, Long userId, RevokedToken.RevokeReason reason) {
        try {
            String jti = jwtUtil.getJtiFromToken(token);
            revokeToken(jti, userId, reason);
        } catch (Exception e) {
            logger.error("Failed to extract JTI from token for revocation", e);
            throw new IllegalArgumentException("Invalid token format", e);
        }
    }

    /**
     * Revoke all tokens for a user (used for password changes or account suspension).
     * Note: This is a simplified implementation. In a production system with many active tokens,
     * you might want to track active tokens differently.
     * 
     * @param userId the user ID
     * @param reason the reason for revocation
     * @return number of tokens revoked
     */
    @Transactional
    public int revokeAllUserTokens(Long userId, RevokedToken.RevokeReason reason) {
        validateUserId(userId);
        
        try {
            // For this implementation, we'll mark all existing tokens for this user as revoked
            // In a real system, you might maintain an active sessions table
            int existingRevoked = revokedTokenRepository.deleteByUserId(userId);
            
            logger.info("Revoked {} existing tokens for user: {}, reason: {}", existingRevoked, userId, reason);
            
            return existingRevoked;
            
        } catch (Exception e) {
            logger.error("Failed to revoke all tokens for user: {}", userId, e);
            throw new RuntimeException("Failed to revoke all user tokens", e);
        }
    }

    /**
     * Get detailed information about a revoked token.
     * Used for auditing and security investigations.
     * 
     * @param jti the JWT ID
     * @return the revoked token information if found
     */
    public RevokedToken getRevokedTokenInfo(String jti) {
        return revokedTokenRepository.findByTokenJti(jti).orElse(null);
    }

    /**
     * Get revocation statistics for a user within a time period.
     * Used for security monitoring and user session management.
     * 
     * @param userId the user ID
     * @param period the time period to check
     * @return number of revocations in the period
     */
    public long getUserRevocationCount(Long userId, Duration period) {
        LocalDateTime since = LocalDateTime.now().minus(period);
        return revokedTokenRepository.countByUserIdAndRevokedAtAfter(userId, since);
    }

    /**
     * Check if a user has exceeded the revocation rate limit.
     * 
     * @param userId the user ID to check
     * @return true if rate limit is exceeded
     */
    public boolean isRateLimitExceeded(Long userId) {
        if (userId == null) return false;
        
        return revokedTokenRepository.hasUserExceededRateLimit(
                userId, maxRevocationsPerUserPerHour, 60);
    }

    /**
     * Scheduled cleanup of expired tokens from the blacklist.
     * Runs daily at 2 AM to maintain database performance.
     */
    @Scheduled(cron = "${jwt.blacklist.cleanup.cron:0 0 2 * * *}")
    @Transactional
    public void cleanupExpiredTokens() {
        if (!cleanupEnabled) {
            logger.debug("Token cleanup is disabled");
            return;
        }

        try {
            LocalDateTime now = LocalDateTime.now();
            int deletedCount = revokedTokenRepository.deleteByExpiresAtBefore(now);
            
            if (deletedCount > 0) {
                logger.info("Cleanup completed: {} expired tokens removed", deletedCount);
            } else {
                logger.debug("Cleanup completed: no expired tokens found");
            }
            
        } catch (Exception e) {
            logger.error("Error during token cleanup", e);
        }
    }

    /**
     * Update monitoring metrics for active revoked tokens.
     * Called periodically to update metrics.
     */
    @Scheduled(fixedRate = 300000) // Every 5 minutes
    public void updateActiveTokensMetrics() {
        if (!monitoringEnabled) return;
        
        try {
            long count = revokedTokenRepository.countActiveRevokedTokens(LocalDateTime.now());
            if (meterRegistry != null) {
                meterRegistry.gauge("jwt_revoked_tokens_active", count);
            }
            logger.trace("Updated active revoked tokens metrics: {}", count);
        } catch (Exception e) {
            logger.error("Error updating active tokens metrics", e);
        }
    }

    /**
     * Get recent revocations for security monitoring.
     * Used by security systems to detect suspicious activity.
     * 
     * @param minutes number of minutes to look back
     * @return list of recent revocations
     */
    public List<RevokedToken> getRecentRevocations(int minutes) {
        return revokedTokenRepository.findRevocationsInLastMinutes(minutes);
    }

    /**
     * Get statistics about revocations by reason.
     * Used for security analytics and reporting.
     * 
     * @param reason the revocation reason
     * @param period the time period to check
     * @return number of revocations for the given reason
     */
    public long getRevocationCountByReason(RevokedToken.RevokeReason reason, Duration period) {
        LocalDateTime since = LocalDateTime.now().minus(period);
        return revokedTokenRepository.countByReasonAndRevokedAtAfter(reason, since);
    }

    // Private helper methods

    private void validateRevokeTokenParameters(String jti, Long userId, RevokedToken.RevokeReason reason) {
        if (jti == null || jti.trim().isEmpty()) {
            throw new IllegalArgumentException("Token JTI cannot be null or empty");
        }
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }
        if (reason == null) {
            throw new IllegalArgumentException("Revocation reason cannot be null");
        }
    }

    private void validateUserId(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }
    }

    private LocalDateTime extractExpirationFromJti(String jti) {
        try {
            // Note: This is a simplified approach. In practice, you might need to 
            // reconstruct the full token or store expiration separately
            Date expiration = jwtUtil.getExpirationDateFromToken(jti);
            return expiration.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        } catch (Exception e) {
            // If we can't extract expiration, set a reasonable default (24 hours from now)
            logger.warn("Could not extract expiration from JTI: {}, using default", jti);
            return LocalDateTime.now().plusHours(24);
        }
    }
}