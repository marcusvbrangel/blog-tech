package com.blog.api.repository;

import com.blog.api.entity.User;
import com.blog.api.entity.VerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Long> {

    /**
     * Find verification token by token string
     */
    Optional<VerificationToken> findByToken(String token);

    /**
     * Find verification token by token string and type
     */
    Optional<VerificationToken> findByTokenAndTokenType(String token, VerificationToken.TokenType tokenType);

    /**
     * Find all tokens for a specific user
     */
    List<VerificationToken> findByUser(User user);

    /**
     * Find all tokens for a specific user and token type
     */
    List<VerificationToken> findByUserAndTokenType(User user, VerificationToken.TokenType tokenType);

    /**
     * Find valid (not expired and not used) tokens for a user and type
     */
    @Query("SELECT vt FROM VerificationToken vt WHERE vt.user = :user AND vt.tokenType = :tokenType " +
           "AND vt.expiresAt > :now AND vt.usedAt IS NULL")
    List<VerificationToken> findValidTokensByUserAndType(@Param("user") User user, 
                                                        @Param("tokenType") VerificationToken.TokenType tokenType,
                                                        @Param("now") LocalDateTime now);

    /**
     * Check if user has valid verification token of specific type
     */
    @Query("SELECT CASE WHEN COUNT(vt) > 0 THEN true ELSE false END FROM VerificationToken vt " +
           "WHERE vt.user = :user AND vt.tokenType = :tokenType AND vt.expiresAt > :now AND vt.usedAt IS NULL")
    boolean hasValidToken(@Param("user") User user, 
                         @Param("tokenType") VerificationToken.TokenType tokenType,
                         @Param("now") LocalDateTime now);

    /**
     * Find all expired tokens (for cleanup)
     */
    @Query("SELECT vt FROM VerificationToken vt WHERE vt.expiresAt < :now AND vt.usedAt IS NULL")
    List<VerificationToken> findExpiredTokens(@Param("now") LocalDateTime now);

    /**
     * Delete expired tokens (cleanup operation)
     */
    @Modifying
    @Query("DELETE FROM VerificationToken vt WHERE vt.expiresAt < :now AND vt.usedAt IS NULL")
    int deleteExpiredTokens(@Param("now") LocalDateTime now);

    /**
     * Delete all tokens for a user (useful when user is deleted)
     */
    @Modifying
    @Query("DELETE FROM VerificationToken vt WHERE vt.user = :user")
    int deleteByUser(@Param("user") User user);

    /**
     * Delete all used tokens older than specified date (cleanup)
     */
    @Modifying
    @Query("DELETE FROM VerificationToken vt WHERE vt.usedAt IS NOT NULL AND vt.usedAt < :cutoffDate")
    int deleteUsedTokensOlderThan(@Param("cutoffDate") LocalDateTime cutoffDate);

    /**
     * Count tokens created for a user in the last period (rate limiting)
     */
    @Query("SELECT COUNT(vt) FROM VerificationToken vt WHERE vt.user = :user " +
           "AND vt.tokenType = :tokenType AND vt.createdAt > :since")
    long countTokensCreatedSince(@Param("user") User user, 
                                @Param("tokenType") VerificationToken.TokenType tokenType,
                                @Param("since") LocalDateTime since);

    /**
     * Find the most recent valid token for a user and type
     */
    @Query("SELECT vt FROM VerificationToken vt WHERE vt.user = :user AND vt.tokenType = :tokenType " +
           "AND vt.expiresAt > :now AND vt.usedAt IS NULL ORDER BY vt.createdAt DESC")
    Optional<VerificationToken> findMostRecentValidToken(@Param("user") User user, 
                                                        @Param("tokenType") VerificationToken.TokenType tokenType,
                                                        @Param("now") LocalDateTime now);
}