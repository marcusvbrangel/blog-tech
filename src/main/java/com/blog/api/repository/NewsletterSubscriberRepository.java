package com.blog.api.repository;

import com.blog.api.entity.NewsletterSubscriber;
import com.blog.api.entity.SubscriptionStatus;
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

/**
 * Repository interface for NewsletterSubscriber entity operations.
 * Provides custom queries for newsletter subscription management and LGPD compliance.
 * 
 * @author AI-Driven Development
 * @version 1.0
 * @since 2025-08-05
 */
@Repository
public interface NewsletterSubscriberRepository extends JpaRepository<NewsletterSubscriber, Long> {

    /**
     * Find a subscriber by email address.
     * 
     * @param email the email address to search for
     * @return Optional containing the subscriber if found
     */
    Optional<NewsletterSubscriber> findByEmail(String email);

    /**
     * Check if a subscriber exists with the given email.
     * 
     * @param email the email address to check
     * @return true if subscriber exists, false otherwise
     */
    boolean existsByEmail(String email);

    /**
     * Find all subscribers with a specific status.
     * 
     * @param status the subscription status to filter by
     * @return list of subscribers with the specified status
     */
    List<NewsletterSubscriber> findByStatus(SubscriptionStatus status);

    /**
     * Find all subscribers with a specific status with pagination.
     * 
     * @param status the subscription status to filter by
     * @param pageable pagination information
     * @return page of subscribers with the specified status
     */
    Page<NewsletterSubscriber> findByStatus(SubscriptionStatus status, Pageable pageable);

    /**
     * Find all confirmed subscribers (for sending newsletters).
     * 
     * @return list of confirmed subscribers
     */
    List<NewsletterSubscriber> findByStatusOrderByCreatedAtDesc(SubscriptionStatus status);

    /**
     * Find subscribers created within a date range.
     * 
     * @param startDate start of the date range
     * @param endDate end of the date range
     * @return list of subscribers created within the range
     */
    List<NewsletterSubscriber> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Find subscribers by status and created date range with pagination.
     * 
     * @param status the subscription status to filter by
     * @param startDate start of the date range
     * @param endDate end of the date range
     * @param pageable pagination information
     * @return page of filtered subscribers
     */
    Page<NewsletterSubscriber> findByStatusAndCreatedAtBetween(
            SubscriptionStatus status, 
            LocalDateTime startDate, 
            LocalDateTime endDate, 
            Pageable pageable);

    /**
     * Find subscribers by privacy policy version.
     * Useful for LGPD compliance tracking.
     * 
     * @param version the privacy policy version
     * @return list of subscribers who accepted this version
     */
    List<NewsletterSubscriber> findByPrivacyPolicyVersion(String version);

    /**
     * Count subscribers by status.
     * 
     * @param status the subscription status
     * @return count of subscribers with the specified status
     */
    long countByStatus(SubscriptionStatus status);

    /**
     * Count subscribers created in the last N days by status.
     * 
     * @param status the subscription status
     * @param sinceDate the date to count from
     * @return count of subscribers
     */
    long countByStatusAndCreatedAtGreaterThanEqual(SubscriptionStatus status, LocalDateTime sinceDate);

    /**
     * Get statistics for all subscription statuses.
     * 
     * @return list of objects containing status and count
     */
    @Query("SELECT ns.status as status, COUNT(ns) as count FROM NewsletterSubscriber ns GROUP BY ns.status")
    List<Object[]> getSubscriptionStatistics();

    /**
     * Find subscribers with email search (for admin purposes).
     * 
     * @param emailPattern the email pattern to search (supports wildcards)
     * @param pageable pagination information
     * @return page of matching subscribers
     */
    @Query("SELECT ns FROM NewsletterSubscriber ns WHERE LOWER(ns.email) LIKE LOWER(CONCAT('%', :emailPattern, '%'))")
    Page<NewsletterSubscriber> findByEmailContainingIgnoreCase(@Param("emailPattern") String emailPattern, Pageable pageable);

    /**
     * Find active subscribers (CONFIRMED or PENDING) for newsletter sending.
     * 
     * @return list of active subscribers
     */
    @Query("SELECT ns FROM NewsletterSubscriber ns WHERE ns.status IN ('CONFIRMED', 'PENDING') ORDER BY ns.createdAt DESC")
    List<NewsletterSubscriber> findActiveSubscribers();

    /**
     * Find confirmed subscribers for newsletter sending.
     * 
     * @return list of confirmed subscribers
     */
    @Query("SELECT ns FROM NewsletterSubscriber ns WHERE ns.status = 'CONFIRMED' ORDER BY ns.confirmedAt DESC")
    List<NewsletterSubscriber> findConfirmedSubscribers();

    /**
     * Find subscribers for bulk operations with pagination.
     * 
     * @param statuses list of statuses to include
     * @param pageable pagination information
     * @return page of subscribers
     */
    @Query("SELECT ns FROM NewsletterSubscriber ns WHERE ns.status IN :statuses ORDER BY ns.createdAt DESC")
    Page<NewsletterSubscriber> findByStatusIn(@Param("statuses") List<SubscriptionStatus> statuses, Pageable pageable);

    /**
     * Soft delete subscriber by marking as DELETED (LGPD compliance).
     * 
     * @param email the email of the subscriber to soft delete
     * @return number of affected rows
     */
    @Modifying
    @Query("UPDATE NewsletterSubscriber ns SET ns.status = 'DELETED', ns.updatedAt = CURRENT_TIMESTAMP WHERE ns.email = :email")
    int softDeleteByEmail(@Param("email") String email);

    /**
     * Update subscriber status.
     * 
     * @param email the subscriber email
     * @param newStatus the new status
     * @param timestamp the timestamp for status change
     * @return number of affected rows
     */
    @Modifying
    @Query("UPDATE NewsletterSubscriber ns SET ns.status = :status, ns.updatedAt = :timestamp, " +
           "ns.confirmedAt = CASE WHEN :status = 'CONFIRMED' THEN :timestamp ELSE ns.confirmedAt END, " +
           "ns.unsubscribedAt = CASE WHEN :status = 'UNSUBSCRIBED' THEN :timestamp ELSE ns.unsubscribedAt END " +
           "WHERE ns.email = :email")
    int updateStatusByEmail(@Param("email") String email, 
                           @Param("status") SubscriptionStatus newStatus, 
                           @Param("timestamp") LocalDateTime timestamp);

    /**
     * Find subscribers who need to be reminded (PENDING for more than X days).
     * 
     * @param cutoffDate the cutoff date for pending subscriptions
     * @return list of subscribers pending confirmation for too long
     */
    @Query("SELECT ns FROM NewsletterSubscriber ns WHERE ns.status = 'PENDING' AND ns.createdAt < :cutoffDate")
    List<NewsletterSubscriber> findPendingSubscribersOlderThan(@Param("cutoffDate") LocalDateTime cutoffDate);

    /**
     * Get monthly subscription statistics.
     * 
     * @param startDate start of the period
     * @param endDate end of the period
     * @return monthly statistics
     */
    @Query("SELECT FUNCTION('DATE_FORMAT', ns.createdAt, '%Y-%m') as month, " +
           "ns.status as status, COUNT(ns) as count " +
           "FROM NewsletterSubscriber ns " +
           "WHERE ns.createdAt BETWEEN :startDate AND :endDate " +
           "GROUP BY FUNCTION('DATE_FORMAT', ns.createdAt, '%Y-%m'), ns.status " +
           "ORDER BY month DESC")
    List<Object[]> getMonthlyStatistics(@Param("startDate") LocalDateTime startDate, 
                                       @Param("endDate") LocalDateTime endDate);

    /**
     * Find subscribers by consent IP address (for LGPD audit).
     * 
     * @param ipAddress the IP address to search for
     * @return list of subscribers with matching IP
     */
    List<NewsletterSubscriber> findByConsentIpAddress(String ipAddress);

    /**
     * Check if email exists with non-deleted status.
     * 
     * @param email the email to check
     * @return true if active subscription exists
     */
    @Query("SELECT COUNT(ns) > 0 FROM NewsletterSubscriber ns WHERE ns.email = :email AND ns.status != 'DELETED'")
    boolean existsByEmailAndNotDeleted(@Param("email") String email);
}