package com.blog.api.repository;

import com.blog.api.entity.TermsAcceptance;
import com.blog.api.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TermsAcceptanceRepository extends JpaRepository<TermsAcceptance, Long> {

    /**
     * Find the most recent terms acceptance for a user
     */
    Optional<TermsAcceptance> findTopByUserOrderByAcceptedAtDesc(User user);

    /**
     * Find the most recent terms acceptance for a user by user ID
     */
    @Query("SELECT ta FROM TermsAcceptance ta WHERE ta.user.id = :userId ORDER BY ta.acceptedAt DESC LIMIT 1")
    Optional<TermsAcceptance> findLatestByUserId(@Param("userId") Long userId);

    /**
     * Find specific terms acceptance by user and version
     */
    Optional<TermsAcceptance> findByUserAndTermsVersion(User user, String termsVersion);

    /**
     * Find all terms acceptances for a user
     */
    List<TermsAcceptance> findByUserOrderByAcceptedAtDesc(User user);

    /**
     * Find all terms acceptances for a user by user ID
     */
    @Query("SELECT ta FROM TermsAcceptance ta WHERE ta.user.id = :userId ORDER BY ta.acceptedAt DESC")
    List<TermsAcceptance> findByUserIdOrderByAcceptedAtDesc(@Param("userId") Long userId);

    /**
     * Find all terms acceptances for a specific version
     */
    List<TermsAcceptance> findByTermsVersionOrderByAcceptedAtDesc(String termsVersion);

    /**
     * Check if user has accepted specific terms version
     */
    boolean existsByUserAndTermsVersion(User user, String termsVersion);

    /**
     * Check if user has accepted specific terms version by user ID
     */
    @Query("SELECT COUNT(ta) > 0 FROM TermsAcceptance ta WHERE ta.user.id = :userId AND ta.termsVersion = :termsVersion")
    boolean existsByUserIdAndTermsVersion(@Param("userId") Long userId, @Param("termsVersion") String termsVersion);

    /**
     * Count acceptances for a specific terms version
     */
    long countByTermsVersion(String termsVersion);

    /**
     * Find acceptances within date range
     */
    @Query("SELECT ta FROM TermsAcceptance ta WHERE ta.acceptedAt BETWEEN :startDate AND :endDate ORDER BY ta.acceptedAt DESC")
    List<TermsAcceptance> findByAcceptedAtBetween(@Param("startDate") LocalDateTime startDate, 
                                                 @Param("endDate") LocalDateTime endDate);

    /**
     * Find acceptances within date range with pagination
     */
    @Query("SELECT ta FROM TermsAcceptance ta WHERE ta.acceptedAt BETWEEN :startDate AND :endDate ORDER BY ta.acceptedAt DESC")
    Page<TermsAcceptance> findByAcceptedAtBetween(@Param("startDate") LocalDateTime startDate, 
                                                 @Param("endDate") LocalDateTime endDate, 
                                                 Pageable pageable);

    /**
     * Find users who haven't accepted latest terms version
     */
    @Query("SELECT DISTINCT u FROM User u WHERE u.termsAcceptedVersion != :latestVersion OR u.termsAcceptedVersion IS NULL")
    List<User> findUsersWithoutLatestTerms(@Param("latestVersion") String latestVersion);

    /**
     * Find users who haven't accepted latest terms version with pagination
     */
    @Query("SELECT DISTINCT u FROM User u WHERE u.termsAcceptedVersion != :latestVersion OR u.termsAcceptedVersion IS NULL")
    Page<User> findUsersWithoutLatestTerms(@Param("latestVersion") String latestVersion, Pageable pageable);

    /**
     * Find acceptances by IP address for audit purposes
     */
    List<TermsAcceptance> findByIpAddressOrderByAcceptedAtDesc(String ipAddress);

    /**
     * Get acceptance statistics for a terms version
     */
    @Query("SELECT COUNT(ta) as totalAcceptances, " +
           "COUNT(DISTINCT ta.user.id) as uniqueUsers, " +
           "MIN(ta.acceptedAt) as firstAcceptance, " +
           "MAX(ta.acceptedAt) as lastAcceptance " +
           "FROM TermsAcceptance ta WHERE ta.termsVersion = :termsVersion")
    Object[] getAcceptanceStatistics(@Param("termsVersion") String termsVersion);

    /**
     * Delete old acceptance records (for LGPD compliance)
     * Keep only the most recent acceptance per user
     */
    @Modifying
    @Query("DELETE FROM TermsAcceptance ta WHERE ta.user.id = :userId AND ta.id NOT IN " +
           "(SELECT ta2.id FROM TermsAcceptance ta2 WHERE ta2.user.id = :userId ORDER BY ta2.acceptedAt DESC LIMIT 1)")
    int deleteOldAcceptancesByUserId(@Param("userId") Long userId);

    /**
     * Delete acceptances older than specified date (for data retention)
     */
    @Modifying
    @Query("DELETE FROM TermsAcceptance ta WHERE ta.acceptedAt < :cutoffDate")
    int deleteAcceptancesOlderThan(@Param("cutoffDate") LocalDateTime cutoffDate);

    /**
     * Find acceptances that need user notification (new version available)
     */
    @Query("SELECT ta FROM TermsAcceptance ta WHERE ta.termsVersion != :currentVersion " +
           "AND ta.id IN (SELECT MAX(ta2.id) FROM TermsAcceptance ta2 GROUP BY ta2.user.id)")
    List<TermsAcceptance> findUsersNeedingVersionUpdate(@Param("currentVersion") String currentVersion);

    /**
     * Get monthly acceptance statistics
     */
    @Query("SELECT EXTRACT(YEAR FROM ta.acceptedAt) as year, " +
           "EXTRACT(MONTH FROM ta.acceptedAt) as month, " +
           "COUNT(ta) as acceptances " +
           "FROM TermsAcceptance ta " +
           "GROUP BY EXTRACT(YEAR FROM ta.acceptedAt), EXTRACT(MONTH FROM ta.acceptedAt) " +
           "ORDER BY year DESC, month DESC")
    List<Object[]> getMonthlyAcceptanceStatistics();

    /**
     * Check if user has any terms acceptance
     */
    boolean existsByUser(User user);

    /**
     * Check if user has any terms acceptance by user ID
     */
    @Query("SELECT COUNT(ta) > 0 FROM TermsAcceptance ta WHERE ta.user.id = :userId")
    boolean existsByUserId(@Param("userId") Long userId);
}