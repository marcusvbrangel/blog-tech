package com.blog.api.repository;

import com.blog.api.entity.NewsletterToken;
import com.blog.api.entity.NewsletterTokenType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for NewsletterToken entity operations.
 * Provides methods for token validation, cleanup, and email-based queries.
 * 
 * @author AI-Driven Development
 * @version 1.0
 * @since 2025-08-05
 */
@Repository
public interface NewsletterTokenRepository extends JpaRepository<NewsletterToken, Long> {

    /**
     * Find newsletter token by token string.
     * 
     * @param token the token string to search for
     * @return Optional containing the token if found
     */
    Optional<NewsletterToken> findByToken(String token);

    /**
     * Find newsletter token by token string and type.
     * Most common method for token validation.
     * 
     * @param token the token string to search for
     * @param tokenType the type of token
     * @return Optional containing the token if found
     */
    Optional<NewsletterToken> findByTokenAndTokenType(String token, NewsletterTokenType tokenType);

    /**
     * Find all tokens for a specific email address.
     * 
     * @param email the email address to search for
     * @return List of tokens for the email
     */
    List<NewsletterToken> findByEmail(String email);

    /**
     * Find all tokens for a specific email address and token type.
     * 
     * @param email the email address to search for
     * @param tokenType the type of token
     * @return List of tokens for the email and type
     */
    List<NewsletterToken> findByEmailAndTokenType(String email, NewsletterTokenType tokenType);

    /**
     * Find valid (not expired and not used) tokens for an email and type.
     * Essential for preventing duplicate token generation.
     * 
     * @param email the email address to search for
     * @param tokenType the type of token
     * @param now current timestamp for expiration check
     * @return List of valid tokens
     */
    @Query("SELECT nt FROM NewsletterToken nt WHERE nt.email = :email AND nt.tokenType = :tokenType " +
           "AND nt.expiresAt > :now AND nt.usedAt IS NULL")
    List<NewsletterToken> findValidTokensByEmailAndType(@Param("email") String email, 
                                                       @Param("tokenType") NewsletterTokenType tokenType,
                                                       @Param("now") LocalDateTime now);

    /**
     * Find unused tokens for an email and type (regardless of expiration).
     * Useful for cleanup and preventing token spam.
     * 
     * @param email the email address to search for
     * @param tokenType the type of token
     * @return List of unused tokens
     */
    @Query("SELECT nt FROM NewsletterToken nt WHERE nt.email = :email AND nt.tokenType = :tokenType " +
           "AND nt.usedAt IS NULL")
    List<NewsletterToken> findUnusedTokensByEmailAndType(@Param("email") String email, 
                                                        @Param("tokenType") NewsletterTokenType tokenType);

    /**
     * Check if email has valid token of specific type.
     * Optimized query that returns boolean without loading entities.
     * 
     * @param email the email address to check
     * @param tokenType the type of token
     * @param now current timestamp for expiration check
     * @return true if valid token exists
     */
    @Query("SELECT CASE WHEN COUNT(nt) > 0 THEN true ELSE false END FROM NewsletterToken nt " +
           "WHERE nt.email = :email AND nt.tokenType = :tokenType AND nt.expiresAt > :now AND nt.usedAt IS NULL")
    boolean hasValidToken(@Param("email") String email, 
                         @Param("tokenType") NewsletterTokenType tokenType,
                         @Param("now") LocalDateTime now);

    /**
     * Find all expired tokens (for cleanup operations).
     * 
     * @param now current timestamp for expiration check
     * @return List of expired tokens
     */
    @Query("SELECT nt FROM NewsletterToken nt WHERE nt.expiresAt < :now AND nt.usedAt IS NULL")
    List<NewsletterToken> findExpiredTokens(@Param("now") LocalDateTime now);

    /**
     * Delete expired tokens (cleanup operation).
     * 
     * @param now current timestamp for expiration check
     * @return number of deleted tokens
     */
    @Modifying
    @Query("DELETE FROM NewsletterToken nt WHERE nt.expiresAt < :now AND nt.usedAt IS NULL")
    int deleteExpiredTokens(@Param("now") LocalDateTime now);

    /**
     * Delete all tokens for an email address.
     * Useful for LGPD compliance (data deletion requests).
     * 
     * @param email the email address
     * @return number of deleted tokens
     */
    @Modifying
    @Query("DELETE FROM NewsletterToken nt WHERE nt.email = :email")
    int deleteByEmail(@Param("email") String email);

    /**
     * Delete all used tokens older than specified date (cleanup).
     * 
     * @param cutoffDate tokens used before this date will be deleted
     * @return number of deleted tokens
     */
    @Modifying
    @Query("DELETE FROM NewsletterToken nt WHERE nt.usedAt IS NOT NULL AND nt.usedAt < :cutoffDate")
    int deleteUsedTokensOlderThan(@Param("cutoffDate") LocalDateTime cutoffDate);

    /**
     * Alias for deleteUsedTokensOlderThan to match scheduler naming.
     * 
     * @param cutoffDate tokens used before this date will be deleted
     * @return number of deleted tokens
     */
    @Modifying
    @Query("DELETE FROM NewsletterToken nt WHERE nt.usedAt IS NOT NULL AND nt.usedAt < :cutoffDate")
    int deleteOldUsedTokens(@Param("cutoffDate") LocalDateTime cutoffDate);

    /**
     * Delete orphaned tokens (tokens for emails that don't exist in newsletter_subscribers).
     * 
     * @return number of deleted tokens
     */
    @Modifying
    @Query("DELETE FROM NewsletterToken nt WHERE nt.email NOT IN " +
           "(SELECT ns.email FROM NewsletterSubscriber ns)")
    int deleteOrphanedTokens();

    /**
     * Count expired tokens.
     * 
     * @param now current timestamp
     * @return number of expired tokens
     */
    @Query("SELECT COUNT(nt) FROM NewsletterToken nt WHERE nt.expiresAt < :now AND nt.usedAt IS NULL")
    long countExpiredTokens(@Param("now") LocalDateTime now);

    /**
     * Count used tokens.
     * 
     * @return number of used tokens
     */
    @Query("SELECT COUNT(nt) FROM NewsletterToken nt WHERE nt.usedAt IS NOT NULL")
    long countUsedTokens();

    /**
     * Count tokens created for an email in the last period (rate limiting).
     * 
     * @param email the email address
     * @param tokenType the type of token
     * @param since count tokens created after this timestamp
     * @return number of tokens created since the specified time
     */
    @Query("SELECT COUNT(nt) FROM NewsletterToken nt WHERE nt.email = :email " +
           "AND nt.tokenType = :tokenType AND nt.createdAt > :since")
    long countTokensCreatedSince(@Param("email") String email, 
                                @Param("tokenType") NewsletterTokenType tokenType,
                                @Param("since") LocalDateTime since);

    /**
     * Find the most recent valid token for an email and type.
     * 
     * @param email the email address
     * @param tokenType the type of token
     * @param now current timestamp for expiration check
     * @return Optional containing the most recent valid token
     */
    @Query("SELECT nt FROM NewsletterToken nt WHERE nt.email = :email AND nt.tokenType = :tokenType " +
           "AND nt.expiresAt > :now AND nt.usedAt IS NULL ORDER BY nt.createdAt DESC")
    Optional<NewsletterToken> findMostRecentValidToken(@Param("email") String email, 
                                                      @Param("tokenType") NewsletterTokenType tokenType,
                                                      @Param("now") LocalDateTime now);

    // Additional newsletter-specific methods

    /**
     * Find all confirmation tokens that are about to expire (for reminder emails).
     * 
     * @param reminderTime tokens expiring before this time
     * @param now current timestamp (tokens not yet expired)
     * @return List of tokens about to expire
     */
    @Query("SELECT nt FROM NewsletterToken nt WHERE nt.tokenType = :tokenType " +
           "AND nt.expiresAt < :reminderTime AND nt.expiresAt > :now AND nt.usedAt IS NULL")
    List<NewsletterToken> findTokensAboutToExpire(@Param("tokenType") NewsletterTokenType tokenType,
                                                 @Param("reminderTime") LocalDateTime reminderTime,
                                                 @Param("now") LocalDateTime now);

    /**
     * Count valid tokens by type (for analytics).
     * 
     * @param tokenType the type of token
     * @param now current timestamp for expiration check
     * @return number of valid tokens of the specified type
     */
    @Query("SELECT COUNT(nt) FROM NewsletterToken nt WHERE nt.tokenType = :tokenType " +
           "AND nt.expiresAt > :now AND nt.usedAt IS NULL")
    long countValidTokensByType(@Param("tokenType") NewsletterTokenType tokenType,
                               @Param("now") LocalDateTime now);

    /**
     * Count used tokens by type (for analytics).
     * 
     * @param tokenType the type of token
     * @return number of used tokens of the specified type
     */
    @Query("SELECT COUNT(nt) FROM NewsletterToken nt WHERE nt.tokenType = :tokenType AND nt.usedAt IS NOT NULL")
    long countUsedTokensByType(@Param("tokenType") NewsletterTokenType tokenType);

    /**
     * Find tokens created within a date range (for reporting).
     * 
     * @param startDate start of date range
     * @param endDate end of date range
     * @return List of tokens created within the range
     */
    @Query("SELECT nt FROM NewsletterToken nt WHERE nt.createdAt >= :startDate AND nt.createdAt <= :endDate")
    List<NewsletterToken> findTokensCreatedBetween(@Param("startDate") LocalDateTime startDate,
                                                  @Param("endDate") LocalDateTime endDate);

    /**
     * Delete tokens by type and age (administrative cleanup).
     * 
     * @param tokenType the type of token to delete
     * @param cutoffDate delete tokens created before this date
     * @return number of deleted tokens
     */
    @Modifying
    @Query("DELETE FROM NewsletterToken nt WHERE nt.tokenType = :tokenType AND nt.createdAt < :cutoffDate")
    int deleteTokensByTypeAndAge(@Param("tokenType") NewsletterTokenType tokenType,
                                @Param("cutoffDate") LocalDateTime cutoffDate);
}