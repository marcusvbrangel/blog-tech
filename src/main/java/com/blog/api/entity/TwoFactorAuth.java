package com.blog.api.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "two_factor_auth", indexes = {
    @Index(name = "idx_2fa_user_id", columnList = "userId", unique = true),
    @Index(name = "idx_2fa_backup_codes_user", columnList = "userId")
})
public class TwoFactorAuth {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;
    
    @Column(name = "secret_key", nullable = false, length = 100)
    private String secretKey;
    
    @Column(name = "enabled", nullable = false)
    private boolean enabled = false;
    
    @Column(name = "backup_codes", columnDefinition = "TEXT")
    private String backupCodes;
    
    @Column(name = "backup_codes_used", columnDefinition = "TEXT")
    private String backupCodesUsed = "";
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "enabled_at")
    private LocalDateTime enabledAt;
    
    @Column(name = "last_used")
    private LocalDateTime lastUsed;
    
    @Column(name = "last_backup_code_used")
    private LocalDateTime lastBackupCodeUsed;
    
    private TwoFactorAuth() {}
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private final TwoFactorAuth twoFactorAuth = new TwoFactorAuth();
        
        public Builder userId(Long userId) {
            twoFactorAuth.userId = userId;
            return this;
        }
        
        public Builder secretKey(String secretKey) {
            twoFactorAuth.secretKey = secretKey;
            return this;
        }
        
        public Builder enabled(boolean enabled) {
            twoFactorAuth.enabled = enabled;
            return this;
        }
        
        public Builder backupCodes(String backupCodes) {
            twoFactorAuth.backupCodes = backupCodes;
            return this;
        }
        
        public Builder backupCodesUsed(String backupCodesUsed) {
            twoFactorAuth.backupCodesUsed = backupCodesUsed;
            return this;
        }
        
        public Builder createdAt(LocalDateTime createdAt) {
            twoFactorAuth.createdAt = createdAt;
            return this;
        }
        
        public Builder enabledAt(LocalDateTime enabledAt) {
            twoFactorAuth.enabledAt = enabledAt;
            return this;
        }
        
        public Builder lastUsed(LocalDateTime lastUsed) {
            twoFactorAuth.lastUsed = lastUsed;
            return this;
        }
        
        public Builder lastBackupCodeUsed(LocalDateTime lastBackupCodeUsed) {
            twoFactorAuth.lastBackupCodeUsed = lastBackupCodeUsed;
            return this;
        }
        
        public TwoFactorAuth build() {
            if (twoFactorAuth.userId == null) {
                throw new IllegalStateException("User ID is required");
            }
            if (twoFactorAuth.secretKey == null || twoFactorAuth.secretKey.trim().isEmpty()) {
                throw new IllegalStateException("Secret key is required");
            }
            if (twoFactorAuth.createdAt == null) {
                twoFactorAuth.createdAt = LocalDateTime.now();
            }
            if (twoFactorAuth.backupCodesUsed == null) {
                twoFactorAuth.backupCodesUsed = "";
            }
            return twoFactorAuth;
        }
    }
    
    // Business logic methods
    public void enable() {
        this.enabled = true;
        this.enabledAt = LocalDateTime.now();
    }
    
    public void disable() {
        this.enabled = false;
        this.enabledAt = null;
    }
    
    public void markAsUsed() {
        this.lastUsed = LocalDateTime.now();
    }
    
    public void markBackupCodeAsUsed(String usedCode) {
        if (backupCodesUsed == null || backupCodesUsed.isEmpty()) {
            backupCodesUsed = usedCode;
        } else {
            backupCodesUsed += "," + usedCode;
        }
        this.lastBackupCodeUsed = LocalDateTime.now();
    }
    
    public boolean isBackupCodeUsed(String code) {
        if (backupCodesUsed == null || backupCodesUsed.isEmpty()) {
            return false;
        }
        return backupCodesUsed.contains(code);
    }
    
    public String[] getAvailableBackupCodes() {
        if (backupCodes == null || backupCodes.isEmpty()) {
            return new String[0];
        }
        
        String[] allCodes = backupCodes.split(",");
        if (backupCodesUsed == null || backupCodesUsed.isEmpty()) {
            return allCodes;
        }
        
        String[] usedCodesArray = backupCodesUsed.split(",");
        return java.util.Arrays.stream(allCodes)
            .filter(code -> !java.util.Arrays.asList(usedCodesArray).contains(code))
            .toArray(String[]::new);
    }
    
    public int getUsedBackupCodesCount() {
        if (backupCodesUsed == null || backupCodesUsed.isEmpty()) {
            return 0;
        }
        return backupCodesUsed.split(",").length;
    }
    
    public int getAvailableBackupCodesCount() {
        return getAvailableBackupCodes().length;
    }
    
    // Getters
    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public String getSecretKey() { return secretKey; }
    public boolean isEnabled() { return enabled; }
    public String getBackupCodes() { return backupCodes; }
    public String getBackupCodesUsed() { return backupCodesUsed; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getEnabledAt() { return enabledAt; }
    public LocalDateTime getLastUsed() { return lastUsed; }
    public LocalDateTime getLastBackupCodeUsed() { return lastBackupCodeUsed; }
    
    // For JPA/Hibernate
    public void setId(Long id) { this.id = id; }
}