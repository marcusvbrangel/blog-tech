package com.blog.api.repository;

import com.blog.api.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for RefreshToken entity operations.
 * Provides optimized queries for refresh token management including
 * token validation, cleanup, and security monitoring.
 */
@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    // =====================================================================
    // Token Lookup Operations
    // =====================================================================

    /**
     * Find an active refresh token by token string.
     * Only returns non-revoked, non-expired tokens.
     * 
     * @param token the refresh token string
     * @return Optional containing the refresh token if found and active
     */
    @Query("SELECT rt FROM RefreshToken rt " +
           "WHERE rt.token = :token " +
           "AND rt.revoked = false " +
           "AND rt.expiresAt > CURRENT_TIMESTAMP")
    Optional<RefreshToken> findActiveByToken(@Param("token") String token);

    /**
     * Find refresh token by token string (regardless of status).
     * 
     * @param token the refresh token string
     * @return Optional containing the refresh token if found
     */
    Optional<RefreshToken> findByToken(String token);

    /**
     * Check if a refresh token exists and is active.
     * Optimized for high-frequency validation checks.
     * 
     * @param token the refresh token string
     * @return true if token exists and is active
     */
    @Query("SELECT CASE WHEN COUNT(rt) > 0 THEN true ELSE false END " +
           "FROM RefreshToken rt " +
           "WHERE rt.token = :token " +
           "AND rt.revoked = false " +
           "AND rt.expiresAt > CURRENT_TIMESTAMP")
    boolean existsActiveByToken(@Param("token") String token);

    // =====================================================================
    // User Token Management
    // =====================================================================

    /**
     * Find all active refresh tokens for a user.
     * 
     * @param userId the user ID
     * @return List of active refresh tokens
     */
    @Query("SELECT rt FROM RefreshToken rt " +
           "WHERE rt.userId = :userId " +
           "AND rt.revoked = false " +
           "AND rt.expiresAt > CURRENT_TIMESTAMP " +
           "ORDER BY rt.createdAt DESC")
    List<RefreshToken> findActiveByUserId(@Param("userId") Long userId);

    /**
     * Find all refresh tokens for a user (active and inactive).
     * 
     * @param userId the user ID
     * @return List of all refresh tokens for the user
     */
    @Query("SELECT rt FROM RefreshToken rt " +
           "WHERE rt.userId = :userId " +
           "ORDER BY rt.createdAt DESC")
    List<RefreshToken> findAllByUserId(@Param("userId") Long userId);

    /**
     * Count active refresh tokens for a user.
     * Used for enforcing token limits per user.
     * 
     * @param userId the user ID
     * @return number of active tokens for the user
     */
    @Query("SELECT COUNT(rt) FROM RefreshToken rt " +
           "WHERE rt.userId = :userId " +
           "AND rt.revoked = false " +
           "AND rt.expiresAt > CURRENT_TIMESTAMP")
    long countActiveByUserId(@Param("userId") Long userId);

    /**
     * Revoke all active refresh tokens for a user.
     * Used when user changes password or logs out from all devices.
     * 
     * @param userId the user ID
     * @return number of tokens revoked
     */
    @Modifying
    @Query("UPDATE RefreshToken rt " +
           "SET rt.revoked = true, rt.revokedAt = CURRENT_TIMESTAMP " +
           "WHERE rt.userId = :userId " +
           "AND rt.revoked = false")
    int revokeAllByUserId(@Param("userId") Long userId);

    // =====================================================================
    // Cleanup Operations
    // =====================================================================

    /**
     * Delete expired refresh tokens to maintain database performance.
     * Should be called periodically by scheduled cleanup jobs.
     * 
     * @param now current timestamp
     * @return number of tokens deleted
     */
    @Modifying
    @Query("DELETE FROM RefreshToken rt " +
           "WHERE rt.expiresAt < :now")
    int deleteExpiredTokens(@Param("now") LocalDateTime now);

    /**
     * Delete revoked tokens older than specified date.
     * Used for cleanup while maintaining audit trail for recent revocations.
     * 
     * @param cutoffDate tokens revoked before this date will be deleted
     * @return number of tokens deleted
     */
    @Modifying
    @Query("DELETE FROM RefreshToken rt " +
           "WHERE rt.revoked = true " +
           "AND rt.revokedAt < :cutoffDate")
    int deleteOldRevokedTokens(@Param("cutoffDate") LocalDateTime cutoffDate);

    /**
     * Find expired tokens for cleanup (query before delete for logging).
     * 
     * @param now current timestamp
     * @return List of expired tokens
     */
    @Query("SELECT rt FROM RefreshToken rt " +
           "WHERE rt.expiresAt < :now")
    List<RefreshToken> findExpiredTokens(@Param("now") LocalDateTime now);

    // =====================================================================
    // Security and Monitoring
    // =====================================================================

    /**
     * Find tokens that haven't been used recently (potential security issue).
     * 
     * @param cutoffDate tokens not used since this date
     * @return List of stale tokens
     */
    @Query("SELECT rt FROM RefreshToken rt " +
           "WHERE rt.revoked = false " +
           "AND rt.expiresAt > CURRENT_TIMESTAMP " +
           "AND (rt.lastUsed IS NULL OR rt.lastUsed < :cutoffDate) " +
           "ORDER BY rt.createdAt DESC")
    List<RefreshToken> findStaleTokens(@Param("cutoffDate") LocalDateTime cutoffDate);

    /**
     * Find recently created tokens for a user (potential security monitoring).
     * 
     * @param userId the user ID
     * @param since tokens created since this timestamp
     * @return List of recent tokens
     */
    @Query("SELECT rt FROM RefreshToken rt " +
           "WHERE rt.userId = :userId " +
           "AND rt.createdAt > :since " +
           "ORDER BY rt.createdAt DESC")
    List<RefreshToken> findRecentTokensByUser(@Param("userId") Long userId, 
                                             @Param("since") LocalDateTime since);

    /**
     * Check if user has exceeded token creation rate limit.
     * 
     * @param userId the user ID
     * @param maxTokens maximum allowed tokens
     * @param timeWindowMinutes time window in minutes
     * @return true if rate limit exceeded
     */
    @Query("SELECT CASE WHEN COUNT(rt) >= :maxTokens THEN true ELSE false END " +
           "FROM RefreshToken rt " +
           "WHERE rt.userId = :userId " +
           "AND rt.createdAt > :since")
    boolean hasUserExceededTokenCreationRate(@Param("userId") Long userId,
                                           @Param("maxTokens") int maxTokens,
                                           @Param("since") LocalDateTime since);

    /**
     * Find tokens by IP address for security analysis.
     * 
     * @param ipAddress the IP address
     * @param since tokens created since this timestamp
     * @return List of tokens from the IP
     */
    @Query("SELECT rt FROM RefreshToken rt " +
           "WHERE rt.ipAddress = :ipAddress " +
           "AND rt.createdAt > :since " +
           "ORDER BY rt.createdAt DESC")
    List<RefreshToken> findTokensByIpAddress(@Param("ipAddress") String ipAddress,
                                           @Param("since") LocalDateTime since);

    // =====================================================================
    // Statistics and Analytics
    // =====================================================================

    /**
     * Count total active refresh tokens in the system.
     * Used for monitoring and metrics.
     * 
     * @return total number of active tokens
     */
    @Query("SELECT COUNT(rt) FROM RefreshToken rt " +
           "WHERE rt.revoked = false " +
           "AND rt.expiresAt > CURRENT_TIMESTAMP")
    long countTotalActiveTokens();

    /**
     * Count tokens created in the last period.
     * 
     * @param since tokens created since this timestamp
     * @return number of tokens created in period
     */
    @Query("SELECT COUNT(rt) FROM RefreshToken rt " +
           "WHERE rt.createdAt > :since")
    long countTokensCreatedSince(@Param("since") LocalDateTime since);

    /**
     * Count tokens revoked in the last period.
     * 
     * @param since tokens revoked since this timestamp
     * @return number of tokens revoked in period
     */
    @Query("SELECT COUNT(rt) FROM RefreshToken rt " +
           "WHERE rt.revoked = true " +
           "AND rt.revokedAt > :since")
    long countTokensRevokedSince(@Param("since") LocalDateTime since);

    /**
     * Find most active users by token creation.
     * 
     * @param since tokens created since this timestamp
     * @param limit maximum number of results
     * @return List of user IDs ordered by token creation count
     */
    @Query(value = "SELECT rt.user_id, COUNT(*) as token_count " +
                   "FROM refresh_tokens rt " +
                   "WHERE rt.created_at > :since " +
                   "GROUP BY rt.user_id " +
                   "ORDER BY token_count DESC " +
                   "LIMIT :limit", 
           nativeQuery = true)
    List<Object[]> findMostActiveUsers(@Param("since") LocalDateTime since, 
                                      @Param("limit") int limit);

    // =====================================================================
    // Token Rotation Support
    // =====================================================================

    /**
     * Mark token as used and update last used timestamp.
     * 
     * @param token the refresh token string
     * @return number of rows updated (should be 1 if successful)
     */
    @Modifying
    @Query("UPDATE RefreshToken rt " +
           "SET rt.lastUsed = CURRENT_TIMESTAMP " +
           "WHERE rt.token = :token " +
           "AND rt.revoked = false")
    int markTokenAsUsed(@Param("token") String token);

    /**
     * Revoke specific token by token string.
     * 
     * @param token the refresh token string
     * @return number of rows updated (should be 1 if successful)
     */
    @Modifying
    @Query("UPDATE RefreshToken rt " +
           "SET rt.revoked = true, rt.revokedAt = CURRENT_TIMESTAMP " +
           "WHERE rt.token = :token")
    int revokeByToken(@Param("token") String token);
}