package com.blog.api.repository;

import com.blog.api.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    
    /**
     * Find audit logs by user ID with pagination
     */
    @Query("SELECT a FROM AuditLog a WHERE a.userId = :userId ORDER BY a.timestamp DESC")
    Page<AuditLog> findByUserIdOrderByTimestampDesc(@Param("userId") Long userId, Pageable pageable);
    
    /**
     * Find audit logs by action with pagination
     */
    @Query("SELECT a FROM AuditLog a WHERE a.action = :action ORDER BY a.timestamp DESC")
    Page<AuditLog> findByActionOrderByTimestampDesc(@Param("action") AuditLog.AuditAction action, Pageable pageable);
    
    /**
     * Find audit logs by IP address with pagination
     */
    @Query("SELECT a FROM AuditLog a WHERE a.ipAddress = :ipAddress ORDER BY a.timestamp DESC")
    Page<AuditLog> findByIpAddressOrderByTimestampDesc(@Param("ipAddress") String ipAddress, Pageable pageable);
    
    /**
     * Find audit logs within time range
     */
    @Query("SELECT a FROM AuditLog a WHERE a.timestamp BETWEEN :startTime AND :endTime ORDER BY a.timestamp DESC")
    Page<AuditLog> findByTimestampBetweenOrderByTimestampDesc(
        @Param("startTime") LocalDateTime startTime, 
        @Param("endTime") LocalDateTime endTime, 
        Pageable pageable
    );
    
    /**
     * Find failed login attempts for user within time window
     */
    @Query("SELECT a FROM AuditLog a WHERE a.userId = :userId " +
           "AND a.action = 'LOGIN' AND a.result = 'FAILURE' " +
           "AND a.timestamp >= :since ORDER BY a.timestamp DESC")
    List<AuditLog> findFailedLoginAttempts(@Param("userId") Long userId, @Param("since") LocalDateTime since);
    
    /**
     * Count failed login attempts from IP within time window
     */
    @Query("SELECT COUNT(a) FROM AuditLog a WHERE a.ipAddress = :ipAddress " +
           "AND a.action = 'LOGIN' AND a.result = 'FAILURE' " +
           "AND a.timestamp >= :since")
    long countFailedLoginAttemptsByIp(@Param("ipAddress") String ipAddress, @Param("since") LocalDateTime since);
    
    /**
     * Find security violations
     */
    @Query("SELECT a FROM AuditLog a WHERE a.action = 'SECURITY_VIOLATION' " +
           "OR a.action = 'RATE_LIMIT_EXCEEDED' OR a.result = 'BLOCKED' " +
           "ORDER BY a.timestamp DESC")
    Page<AuditLog> findSecurityViolations(Pageable pageable);
    
    /**
     * Find audit logs by user and action
     */
    @Query("SELECT a FROM AuditLog a WHERE a.userId = :userId AND a.action = :action " +
           "ORDER BY a.timestamp DESC")
    Page<AuditLog> findByUserIdAndActionOrderByTimestampDesc(
        @Param("userId") Long userId, 
        @Param("action") AuditLog.AuditAction action, 
        Pageable pageable
    );
    
    /**
     * Find recent activity for user dashboard
     */
    @Query("SELECT a FROM AuditLog a WHERE a.userId = :userId " +
           "AND a.timestamp >= :since ORDER BY a.timestamp DESC")
    List<AuditLog> findRecentActivityByUser(@Param("userId") Long userId, @Param("since") LocalDateTime since);
    
    /**
     * Clean up old audit logs (for scheduled maintenance)
     */
    @Modifying
    @Query("DELETE FROM AuditLog a WHERE a.timestamp < :cutoffDate")
    int deleteOldAuditLogs(@Param("cutoffDate") LocalDateTime cutoffDate);
    
    /**
     * Count total audit logs by result type
     */
    @Query("SELECT COUNT(a) FROM AuditLog a WHERE a.result = :result " +
           "AND a.timestamp >= :since")
    long countByResultSince(@Param("result") AuditLog.AuditResult result, @Param("since") LocalDateTime since);
    
    /**
     * Find admin actions for monitoring
     */
    @Query("SELECT a FROM AuditLog a WHERE a.action = 'ADMIN_ACCESS' " +
           "OR a.action = 'PERMISSION_CHANGE' OR a.action = 'USER_DELETE' " +
           "ORDER BY a.timestamp DESC")
    Page<AuditLog> findAdminActions(Pageable pageable);
    
    /**
     * Find resource access logs
     */
    @Query("SELECT a FROM AuditLog a WHERE a.resourceType = :resourceType " +
           "AND a.resourceId = :resourceId ORDER BY a.timestamp DESC")
    Page<AuditLog> findByResourceTypeAndResourceIdOrderByTimestampDesc(
        @Param("resourceType") String resourceType,
        @Param("resourceId") Long resourceId,
        Pageable pageable
    );
}