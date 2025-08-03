package com.blog.api.repository;

import com.blog.api.entity.RevokedToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for managing revoked JWT tokens.
 * Optimized for high-performance blacklist lookups with minimal database impact.
 */
@Repository
public interface RevokedTokenRepository extends JpaRepository<RevokedToken, Long> {

    /**
     * Fast lookup to check if a token is revoked.
     * This is the most critical query for performance as it's called on every authenticated request.
     * 
     * @param tokenJti the JWT ID to check
     * @return true if token is revoked, false otherwise
     */
    boolean existsByTokenJti(String tokenJti);

    /**
     * Find revoked token by JTI for detailed information.
     * Used when we need full token details for logging or auditing.
     * 
     * @param tokenJti the JWT ID to find
     * @return Optional containing the revoked token if found
     */
    Optional<RevokedToken> findByTokenJti(String tokenJti);

    /**
     * Cleanup expired tokens from the blacklist.
     * This is critical for database maintenance and performance.
     * 
     * @param now current timestamp
     * @return number of tokens deleted
     */
    @Modifying
    @Query("DELETE FROM RevokedToken r WHERE r.expiresAt < :now")
    int deleteByExpiresAtBefore(@Param("now") LocalDateTime now);

    /**
     * Count revoked tokens by user within a time period.
     * Used for rate limiting and security monitoring.
     * 
     * @param userId the user ID
     * @param since start time for counting
     * @return number of tokens revoked since the given time
     */
    @Query("SELECT COUNT(r) FROM RevokedToken r WHERE r.userId = :userId AND r.revokedAt > :since")
    long countByUserIdAndRevokedAtAfter(@Param("userId") Long userId, @Param("since") LocalDateTime since);

    /**
     * Find all revoked tokens for a specific user, ordered by most recent first.
     * Used for user session management and security auditing.
     * 
     * @param userId the user ID
     * @return list of revoked tokens for the user
     */
    List<RevokedToken> findByUserIdOrderByRevokedAtDesc(Long userId);

    /**
     * Remove all revoked tokens for a specific user.
     * Used during account deletion or bulk cleanup operations.
     * 
     * @param userId the user ID
     * @return number of tokens deleted
     */
    @Modifying
    @Query("DELETE FROM RevokedToken r WHERE r.userId = :userId")
    int deleteByUserId(@Param("userId") Long userId);

    /**
     * Count currently active (non-expired) revoked tokens.
     * Used for monitoring and alerting on blacklist size.
     * 
     * @param now current timestamp
     * @return number of active revoked tokens
     */
    @Query("SELECT COUNT(r) FROM RevokedToken r WHERE r.expiresAt > :now")
    long countActiveRevokedTokens(@Param("now") LocalDateTime now);

    /**
     * Find revoked tokens by reason within a time period.
     * Used for security analytics and threat detection.
     * 
     * @param reason the revocation reason
     * @param since start time for search
     * @return list of matching revoked tokens
     */
    @Query("SELECT r FROM RevokedToken r WHERE r.reason = :reason AND r.revokedAt > :since ORDER BY r.revokedAt DESC")
    List<RevokedToken> findByReasonAndRevokedAtAfter(@Param("reason") RevokedToken.RevokeReason reason, 
                                                     @Param("since") LocalDateTime since);

    /**
     * Find tokens approaching expiration for cleanup warning.
     * Used by monitoring systems to predict cleanup load.
     * 
     * @param from start of expiration window
     * @param to end of expiration window
     * @return number of tokens expiring in the given window
     */
    @Query("SELECT COUNT(r) FROM RevokedToken r WHERE r.expiresAt BETWEEN :from AND :to")
    long countTokensExpiringBetween(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    /**
     * Find the oldest active revoked token.
     * Used for determining optimal cleanup frequency.
     * 
     * @param now current timestamp
     * @return the oldest non-expired revoked token
     */
    @Query("SELECT r FROM RevokedToken r WHERE r.expiresAt > :now ORDER BY r.createdAt ASC")
    Optional<RevokedToken> findOldestActiveToken(@Param("now") LocalDateTime now);

    /**
     * Bulk find existence of multiple token JTIs.
     * Used for batch validation scenarios.
     * 
     * @param tokenJtis list of JWT IDs to check
     * @return list of JWT IDs that are revoked
     */
    @Query("SELECT r.tokenJti FROM RevokedToken r WHERE r.tokenJti IN :tokenJtis")
    List<String> findRevokedTokenJtis(@Param("tokenJtis") List<String> tokenJtis);

    /**
     * Count revocations by reason within a time period.
     * Used for security analytics and reporting.
     * 
     * @param reason the revocation reason
     * @param since start time for counting
     * @return number of revocations for the given reason
     */
    @Query("SELECT COUNT(r) FROM RevokedToken r WHERE r.reason = :reason AND r.revokedAt > :since")
    long countByReasonAndRevokedAtAfter(@Param("reason") RevokedToken.RevokeReason reason, 
                                        @Param("since") LocalDateTime since);

    /**
     * Find recent revocations for security monitoring.
     * Used by alerting systems to detect suspicious activity.
     * 
     * @param minutes number of minutes to look back
     * @return list of recently revoked tokens
     */
    @Query("SELECT r FROM RevokedToken r WHERE r.revokedAt > :since ORDER BY r.revokedAt DESC")
    List<RevokedToken> findRecentRevocations(@Param("since") LocalDateTime since);

    /**
     * Custom method to check if user has exceeded revocation rate limit.
     * 
     * @param userId the user ID
     * @param maxRevocations maximum allowed revocations
     * @param timeWindowMinutes time window in minutes
     * @return true if user has exceeded the rate limit
     */
    default boolean hasUserExceededRateLimit(Long userId, int maxRevocations, int timeWindowMinutes) {
        LocalDateTime since = LocalDateTime.now().minusMinutes(timeWindowMinutes);
        long count = countByUserIdAndRevokedAtAfter(userId, since);
        return count >= maxRevocations;
    }

    /**
     * Custom method to find recent revocations within specified minutes.
     * 
     * @param minutes number of minutes to look back
     * @return list of recently revoked tokens
     */
    default List<RevokedToken> findRevocationsInLastMinutes(int minutes) {
        LocalDateTime since = LocalDateTime.now().minusMinutes(minutes);
        return findRecentRevocations(since);
    }
}