package com.blog.api.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs", indexes = {
    @Index(name = "idx_audit_logs_user_timestamp", columnList = "userId, timestamp DESC"),
    @Index(name = "idx_audit_logs_action_timestamp", columnList = "action, timestamp DESC"),
    @Index(name = "idx_audit_logs_timestamp", columnList = "timestamp DESC"),
    @Index(name = "idx_audit_logs_ip_timestamp", columnList = "ipAddress, timestamp DESC")
})
public class AuditLog {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id")
    private Long userId;
    
    @Column(name = "username", length = 100)
    private String username;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "action", nullable = false, length = 50)
    private AuditAction action;
    
    @Column(name = "resource_type", length = 50)
    private String resourceType;
    
    @Column(name = "resource_id")
    private Long resourceId;
    
    @Column(name = "details", columnDefinition = "TEXT")
    private String details;
    
    @Column(name = "ip_address", length = 45)
    private String ipAddress;
    
    @Column(name = "user_agent", length = 500)
    private String userAgent;
    
    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "result", nullable = false, length = 20)
    private AuditResult result;
    
    @Column(name = "error_message", length = 1000)
    private String errorMessage;
    
    public enum AuditAction {
        LOGIN,
        LOGOUT,
        LOGOUT_ALL_DEVICES,
        REGISTER,
        PASSWORD_RESET_REQUEST,
        PASSWORD_RESET_CONFIRM,
        EMAIL_VERIFICATION,
        TOKEN_REFRESH,
        TOKEN_REVOKE,
        USER_CREATE,
        USER_UPDATE,
        USER_DELETE,
        POST_CREATE,
        POST_UPDATE,
        POST_DELETE,
        COMMENT_CREATE,
        COMMENT_UPDATE,
        COMMENT_DELETE,
        CATEGORY_CREATE,
        CATEGORY_UPDATE,
        CATEGORY_DELETE,
        ADMIN_ACCESS,
        PERMISSION_CHANGE,
        SECURITY_VIOLATION,
        RATE_LIMIT_EXCEEDED
    }
    
    public enum AuditResult {
        SUCCESS,
        FAILURE,
        BLOCKED,
        ERROR
    }
    
    private AuditLog() {}
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private final AuditLog auditLog = new AuditLog();
        
        public Builder userId(Long userId) {
            auditLog.userId = userId;
            return this;
        }
        
        public Builder username(String username) {
            auditLog.username = username;
            return this;
        }
        
        public Builder action(AuditAction action) {
            auditLog.action = action;
            return this;
        }
        
        public Builder resourceType(String resourceType) {
            auditLog.resourceType = resourceType;
            return this;
        }
        
        public Builder resourceId(Long resourceId) {
            auditLog.resourceId = resourceId;
            return this;
        }
        
        public Builder details(String details) {
            auditLog.details = details;
            return this;
        }
        
        public Builder ipAddress(String ipAddress) {
            auditLog.ipAddress = ipAddress;
            return this;
        }
        
        public Builder userAgent(String userAgent) {
            auditLog.userAgent = userAgent;
            return this;
        }
        
        public Builder timestamp(LocalDateTime timestamp) {
            auditLog.timestamp = timestamp;
            return this;
        }
        
        public Builder result(AuditResult result) {
            auditLog.result = result;
            return this;
        }
        
        public Builder errorMessage(String errorMessage) {
            auditLog.errorMessage = errorMessage;
            return this;
        }
        
        public AuditLog build() {
            if (auditLog.action == null) {
                throw new IllegalStateException("Action is required");
            }
            if (auditLog.result == null) {
                throw new IllegalStateException("Result is required");
            }
            if (auditLog.timestamp == null) {
                auditLog.timestamp = LocalDateTime.now();
            }
            return auditLog;
        }
    }
    
    // Getters
    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public String getUsername() { return username; }
    public AuditAction getAction() { return action; }
    public String getResourceType() { return resourceType; }
    public Long getResourceId() { return resourceId; }
    public String getDetails() { return details; }
    public String getIpAddress() { return ipAddress; }
    public String getUserAgent() { return userAgent; }  
    public LocalDateTime getTimestamp() { return timestamp; }
    public AuditResult getResult() { return result; }
    public String getErrorMessage() { return errorMessage; }
    
    // For JPA/Hibernate
    void setId(Long id) { this.id = id; }
}