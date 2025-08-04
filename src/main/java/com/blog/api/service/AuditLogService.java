package com.blog.api.service;

import com.blog.api.entity.AuditLog;
import com.blog.api.repository.AuditLogRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AuditLogService {
    
    private static final Logger logger = LoggerFactory.getLogger(AuditLogService.class);
    
    @Autowired
    private AuditLogRepository auditLogRepository;
    
    private final Counter auditLogCounter;
    private final Counter auditFailureCounter;
    private final Counter securityViolationCounter;
    
    public AuditLogService(MeterRegistry meterRegistry) {
        this.auditLogCounter = Counter.builder("blog_api_audit_logs_total")
            .description("Total number of audit logs created")
            .register(meterRegistry);
            
        this.auditFailureCounter = Counter.builder("blog_api_audit_failures_total")
            .description("Total number of failed operations logged")
            .register(meterRegistry);
            
        this.securityViolationCounter = Counter.builder("blog_api_security_violations_total")
            .description("Total number of security violations logged")
            .register(meterRegistry);
    }
    
    /**
     * Log an audit event asynchronously
     */
    @Async
    @Transactional
    public void logAsync(AuditLog.AuditAction action, AuditLog.AuditResult result, 
                        Long userId, String username, String ipAddress, String userAgent,
                        String resourceType, Long resourceId, String details, String errorMessage) {
        
        try {
            AuditLog auditLog = AuditLog.builder()
                .action(action)
                .result(result)
                .userId(userId)
                .username(username)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .resourceType(resourceType)
                .resourceId(resourceId)
                .details(details)
                .errorMessage(errorMessage)
                .timestamp(LocalDateTime.now())
                .build();
                
            auditLogRepository.save(auditLog);
            auditLogCounter.increment();
            
            if (result == AuditLog.AuditResult.FAILURE || result == AuditLog.AuditResult.ERROR) {
                auditFailureCounter.increment();
            }
            
            if (action == AuditLog.AuditAction.SECURITY_VIOLATION || 
                action == AuditLog.AuditAction.RATE_LIMIT_EXCEEDED ||
                result == AuditLog.AuditResult.BLOCKED) {
                securityViolationCounter.increment();
            }
            
            logger.debug("Audit log created: action={}, result={}, userId={}, ip={}", 
                        action, result, userId, ipAddress);
                        
        } catch (Exception e) {
            logger.error("Failed to create audit log: action={}, userId={}, error={}", 
                        action, userId, e.getMessage(), e);
        }
    }
    
    /**
     * Log successful operation
     */
    @Async
    public void logSuccess(AuditLog.AuditAction action, Long userId, String username, 
                          HttpServletRequest request, String resourceType, Long resourceId, String details) {
        logAsync(action, AuditLog.AuditResult.SUCCESS, userId, username,
                getIpAddress(request), getUserAgent(request), resourceType, resourceId, details, null);
    }
    
    /**
     * Log failed operation
     */
    @Async
    public void logFailure(AuditLog.AuditAction action, Long userId, String username,
                          HttpServletRequest request, String resourceType, Long resourceId, 
                          String details, String errorMessage) {
        logAsync(action, AuditLog.AuditResult.FAILURE, userId, username,
                getIpAddress(request), getUserAgent(request), resourceType, resourceId, details, errorMessage);
    }
    
    /**
     * Log security violation
     */
    @Async
    public void logSecurityViolation(String violation, Long userId, String username,
                                   HttpServletRequest request, String details) {
        logAsync(AuditLog.AuditAction.SECURITY_VIOLATION, AuditLog.AuditResult.BLOCKED, 
                userId, username, getIpAddress(request), getUserAgent(request),
                "SECURITY", null, violation + ": " + details, null);
    }
    
    /**
     * Log rate limit exceeded
     */
    @Async
    public void logRateLimitExceeded(String operation, Long userId, String username,
                                    HttpServletRequest request, String details) {
        logAsync(AuditLog.AuditAction.RATE_LIMIT_EXCEEDED, AuditLog.AuditResult.BLOCKED,
                userId, username, getIpAddress(request), getUserAgent(request),
                "RATE_LIMIT", null, operation + " rate limit exceeded: " + details, null);
    }
    
    /**
     * Get audit logs by user
     */
    public Page<AuditLog> getAuditLogsByUser(Long userId, Pageable pageable) {
        return auditLogRepository.findByUserIdOrderByTimestampDesc(userId, pageable);
    }
    
    /**
     * Get audit logs by action
     */
    public Page<AuditLog> getAuditLogsByAction(AuditLog.AuditAction action, Pageable pageable) {
        return auditLogRepository.findByActionOrderByTimestampDesc(action, pageable);
    }
    
    /**
     * Get audit logs by IP address
     */
    public Page<AuditLog> getAuditLogsByIpAddress(String ipAddress, Pageable pageable) {
        return auditLogRepository.findByIpAddressOrderByTimestampDesc(ipAddress, pageable);
    }
    
    /**
     * Get audit logs within time range
     */
    public Page<AuditLog> getAuditLogsByTimeRange(LocalDateTime startTime, LocalDateTime endTime, Pageable pageable) {
        return auditLogRepository.findByTimestampBetweenOrderByTimestampDesc(startTime, endTime, pageable);
    }
    
    /**
     * Get failed login attempts for user
     */
    public List<AuditLog> getFailedLoginAttempts(Long userId, LocalDateTime since) {
        return auditLogRepository.findFailedLoginAttempts(userId, since);
    }
    
    /**
     * Count failed login attempts by IP
     */
    public long countFailedLoginAttemptsByIp(String ipAddress, LocalDateTime since) {
        return auditLogRepository.countFailedLoginAttemptsByIp(ipAddress, since);
    }
    
    /**
     * Get security violations
     */
    public Page<AuditLog> getSecurityViolations(Pageable pageable) {
        return auditLogRepository.findSecurityViolations(pageable);
    }
    
    /**
     * Get recent activity for user
     */
    public List<AuditLog> getRecentActivityByUser(Long userId, LocalDateTime since) {
        return auditLogRepository.findRecentActivityByUser(userId, since);
    }
    
    /**
     * Get admin actions
     */
    public Page<AuditLog> getAdminActions(Pageable pageable) {
        return auditLogRepository.findAdminActions(pageable);
    }
    
    /**
     * Get resource access logs
     */
    public Page<AuditLog> getResourceAccessLogs(String resourceType, Long resourceId, Pageable pageable) {
        return auditLogRepository.findByResourceTypeAndResourceIdOrderByTimestampDesc(resourceType, resourceId, pageable);
    }
    
    /**
     * Cleanup old audit logs (runs daily at 2 AM)
     */
    @Scheduled(cron = "0 0 2 * * *")
    @Transactional
    public void cleanupOldAuditLogs() {
        try {
            // Keep audit logs for 90 days by default
            LocalDateTime cutoffDate = LocalDateTime.now().minusDays(90);
            int deleted = auditLogRepository.deleteOldAuditLogs(cutoffDate);
            
            if (deleted > 0) {
                logger.info("Cleaned up {} old audit logs older than {}", deleted, cutoffDate);
            }
        } catch (Exception e) {
            logger.error("Failed to cleanup old audit logs", e);
        }
    }
    
    /**
     * Extract IP address from request
     */
    private String getIpAddress(HttpServletRequest request) {
        if (request == null) return null;
        
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty() && !"unknown".equalsIgnoreCase(xForwardedFor)) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty() && !"unknown".equalsIgnoreCase(xRealIp)) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }
    
    /**
     * Extract User-Agent from request
     */
    private String getUserAgent(HttpServletRequest request) {
        if (request == null) return null;
        
        String userAgent = request.getHeader("User-Agent");
        if (userAgent != null && userAgent.length() > 500) {
            return userAgent.substring(0, 500);
        }
        return userAgent;
    }
}