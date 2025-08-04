package com.blog.api.repository;

import com.blog.api.entity.TwoFactorAuth;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface TwoFactorAuthRepository extends JpaRepository<TwoFactorAuth, Long> {
    
    /**
     * Find 2FA configuration by user ID
     */
    Optional<TwoFactorAuth> findByUserId(Long userId);
    
    /**
     * Check if user has 2FA enabled
     */
    @Query("SELECT t.enabled FROM TwoFactorAuth t WHERE t.userId = :userId")
    Optional<Boolean> isEnabledByUserId(@Param("userId") Long userId);
    
    /**
     * Check if user has 2FA configured (regardless of enabled status)
     */
    boolean existsByUserId(Long userId);
    
    /**
     * Find all users with 2FA enabled
     */
    @Query("SELECT t FROM TwoFactorAuth t WHERE t.enabled = true ORDER BY t.enabledAt DESC")
    java.util.List<TwoFactorAuth> findAllEnabled();
    
    /**
     * Count users with 2FA enabled
     */
    @Query("SELECT COUNT(t) FROM TwoFactorAuth t WHERE t.enabled = true")
    long countEnabled();
    
    /**
     * Count users with 2FA configured but not enabled
     */
    @Query("SELECT COUNT(t) FROM TwoFactorAuth t WHERE t.enabled = false")
    long countConfiguredButNotEnabled();
    
    /**
     * Find users who have used 2FA recently
     */
    @Query("SELECT t FROM TwoFactorAuth t WHERE t.enabled = true AND t.lastUsed >= :since ORDER BY t.lastUsed DESC")
    java.util.List<TwoFactorAuth> findRecentlyUsed(@Param("since") LocalDateTime since);
    
    /**
     * Find users who have used backup codes recently
     */
    @Query("SELECT t FROM TwoFactorAuth t WHERE t.enabled = true AND t.lastBackupCodeUsed >= :since ORDER BY t.lastBackupCodeUsed DESC")
    java.util.List<TwoFactorAuth> findRecentBackupCodeUsage(@Param("since") LocalDateTime since);
    
    /**
     * Update last used timestamp
     */
    @Modifying
    @Query("UPDATE TwoFactorAuth t SET t.lastUsed = :timestamp WHERE t.userId = :userId")
    int updateLastUsed(@Param("userId") Long userId, @Param("timestamp") LocalDateTime timestamp);
    
    /**
     * Update backup code usage
     */
    @Modifying
    @Query("UPDATE TwoFactorAuth t SET t.backupCodesUsed = :usedCodes, t.lastBackupCodeUsed = :timestamp WHERE t.userId = :userId")
    int updateBackupCodeUsage(@Param("userId") Long userId, @Param("usedCodes") String usedCodes, @Param("timestamp") LocalDateTime timestamp);
    
    /**
     * Enable 2FA for user
     */
    @Modifying
    @Query("UPDATE TwoFactorAuth t SET t.enabled = true, t.enabledAt = :timestamp WHERE t.userId = :userId")
    int enableForUser(@Param("userId") Long userId, @Param("timestamp") LocalDateTime timestamp);
    
    /**
     * Disable 2FA for user
     */
    @Modifying
    @Query("UPDATE TwoFactorAuth t SET t.enabled = false, t.enabledAt = null WHERE t.userId = :userId")
    int disableForUser(@Param("userId") Long userId);
    
    /**
     * Delete 2FA configuration for user
     */
    @Modifying
    @Query("DELETE FROM TwoFactorAuth t WHERE t.userId = :userId")
    int deleteByUserId(@Param("userId") Long userId);
    
    /**
     * Find users with many backup codes used (potential security concern)
     */
    @Query("SELECT t FROM TwoFactorAuth t WHERE t.enabled = true AND LENGTH(t.backupCodesUsed) > :threshold")
    java.util.List<TwoFactorAuth> findUsersWithManyBackupCodesUsed(@Param("threshold") int threshold);
}