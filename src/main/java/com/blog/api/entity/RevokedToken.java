package com.blog.api.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Entity representing revoked JWT tokens for blacklist functionality.
 * This enables token invalidation before natural expiration for logout, 
 * security breaches, or administrative actions.
 */
@Entity
@Table(name = "revoked_tokens")
public class RevokedToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "token_jti", nullable = false, unique = true, length = 36)
    private String tokenJti;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "revoked_at", nullable = false)
    private LocalDateTime revokedAt;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "reason", length = 20)
    private RevokeReason reason;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Enum defining reasons for token revocation
     */
    public enum RevokeReason {
        LOGOUT,           // Normal user logout
        ADMIN_REVOKE,     // Administrative revocation
        PASSWORD_CHANGE,  // Password change security measure
        ACCOUNT_LOCKED,   // Account locked/suspended
        SECURITY_BREACH   // Security incident response
    }

    // Default constructor for JPA
    public RevokedToken() {}

    // Private constructor for Builder pattern
    private RevokedToken(Builder builder) {
        this.tokenJti = builder.tokenJti;
        this.userId = builder.userId;
        this.revokedAt = builder.revokedAt;
        this.expiresAt = builder.expiresAt;
        this.reason = builder.reason;
        this.createdAt = builder.createdAt;
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (revokedAt == null) {
            revokedAt = LocalDateTime.now();
        }
    }

    // Getters
    public Long getId() { return id; }
    public String getTokenJti() { return tokenJti; }
    public Long getUserId() { return userId; }
    public LocalDateTime getRevokedAt() { return revokedAt; }
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public RevokeReason getReason() { return reason; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    // Setters for JPA
    public void setId(Long id) { this.id = id; }
    public void setTokenJti(String tokenJti) { this.tokenJti = tokenJti; }
    public void setUserId(Long userId) { this.userId = userId; }
    public void setRevokedAt(LocalDateTime revokedAt) { this.revokedAt = revokedAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }
    public void setReason(RevokeReason reason) { this.reason = reason; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    /**
     * Builder pattern implementation
     */
    public static class Builder {
        private String tokenJti;
        private Long userId;
        private LocalDateTime revokedAt;
        private LocalDateTime expiresAt;
        private RevokeReason reason;
        private LocalDateTime createdAt;

        public Builder tokenJti(String tokenJti) {
            this.tokenJti = tokenJti;
            return this;
        }

        public Builder userId(Long userId) {
            this.userId = userId;
            return this;
        }

        public Builder revokedAt(LocalDateTime revokedAt) {
            this.revokedAt = revokedAt;
            return this;
        }

        public Builder expiresAt(LocalDateTime expiresAt) {
            this.expiresAt = expiresAt;
            return this;
        }

        public Builder reason(RevokeReason reason) {
            this.reason = reason;
            return this;
        }

        public Builder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public RevokedToken build() {
            return new RevokedToken(this);
        }
    }

    /**
     * Create a new builder instance
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Create builder from existing entity
     */
    public static Builder from(RevokedToken revokedToken) {
        return new Builder()
                .tokenJti(revokedToken.tokenJti)
                .userId(revokedToken.userId)
                .revokedAt(revokedToken.revokedAt)
                .expiresAt(revokedToken.expiresAt)
                .reason(revokedToken.reason)
                .createdAt(revokedToken.createdAt);
    }

    /**
     * Factory method for logout revocation
     */
    public static RevokedToken forLogout(String tokenJti, Long userId, LocalDateTime expiresAt) {
        return builder()
                .tokenJti(tokenJti)
                .userId(userId)
                .reason(RevokeReason.LOGOUT)
                .expiresAt(expiresAt)
                .build();
    }

    /**
     * Factory method for password change revocation
     */
    public static RevokedToken forPasswordChange(String tokenJti, Long userId, LocalDateTime expiresAt) {
        return builder()
                .tokenJti(tokenJti)
                .userId(userId)
                .reason(RevokeReason.PASSWORD_CHANGE)
                .expiresAt(expiresAt)
                .build();
    }

    /**
     * Factory method for admin revocation
     */
    public static RevokedToken forAdminRevoke(String tokenJti, Long userId, LocalDateTime expiresAt) {
        return builder()
                .tokenJti(tokenJti)
                .userId(userId)
                .reason(RevokeReason.ADMIN_REVOKE)
                .expiresAt(expiresAt)
                .build();
    }

    /**
     * Check if token is expired
     */
    public boolean isExpired() {
        return expiresAt == null || expiresAt.isBefore(LocalDateTime.now());
    }

    /**
     * Check if revocation is recent (within last hour)
     */
    public boolean isRecentRevocation() {
        return revokedAt != null && revokedAt.isAfter(LocalDateTime.now().minusHours(1));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RevokedToken that = (RevokedToken) o;
        return Objects.equals(tokenJti, that.tokenJti);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tokenJti);
    }

    @Override
    public String toString() {
        return "RevokedToken{" +
                "id=" + id +
                ", tokenJti='" + tokenJti + '\'' +
                ", userId=" + userId +
                ", revokedAt=" + revokedAt +
                ", expiresAt=" + expiresAt +
                ", reason=" + reason +
                ", createdAt=" + createdAt +
                '}';
    }
}