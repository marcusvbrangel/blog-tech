package com.blog.api.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Entity representing a refresh token for JWT authentication.
 * Refresh tokens are long-lived tokens used to obtain new access tokens
 * without requiring the user to re-authenticate.
 */
@Entity
@Table(name = "refresh_tokens")
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "token", unique = true, nullable = false, length = 255)
    private String token;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "last_used")
    private LocalDateTime lastUsed;

    @Column(name = "device_info", length = 500)
    private String deviceInfo;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "revoked")
    private boolean revoked = false;

    @Column(name = "revoked_at")
    private LocalDateTime revokedAt;

    // Constructors
    public RefreshToken() {
        this.createdAt = LocalDateTime.now();
    }

    private RefreshToken(Builder builder) {
        this.userId = builder.userId;
        this.token = builder.token;
        this.expiresAt = builder.expiresAt;
        this.createdAt = builder.createdAt != null ? builder.createdAt : LocalDateTime.now();
        this.lastUsed = builder.lastUsed;
        this.deviceInfo = builder.deviceInfo;
        this.ipAddress = builder.ipAddress;
        this.revoked = builder.revoked;
        this.revokedAt = builder.revokedAt;
    }

    // Builder Pattern
    public static Builder builder() {
        return new Builder();
    }

    public static Builder from(RefreshToken refreshToken) {
        return new Builder(refreshToken);
    }

    public static class Builder {
        private Long userId;
        private String token;
        private LocalDateTime expiresAt;
        private LocalDateTime createdAt;
        private LocalDateTime lastUsed;
        private String deviceInfo;
        private String ipAddress;
        private boolean revoked = false;
        private LocalDateTime revokedAt;

        public Builder() {}

        public Builder(RefreshToken refreshToken) {
            this.userId = refreshToken.userId;
            this.token = refreshToken.token;
            this.expiresAt = refreshToken.expiresAt;
            this.createdAt = refreshToken.createdAt;
            this.lastUsed = refreshToken.lastUsed;
            this.deviceInfo = refreshToken.deviceInfo;
            this.ipAddress = refreshToken.ipAddress;
            this.revoked = refreshToken.revoked;
            this.revokedAt = refreshToken.revokedAt;
        }

        public Builder userId(Long userId) {
            this.userId = userId;
            return this;
        }

        public Builder token(String token) {
            this.token = token;
            return this;
        }

        public Builder expiresAt(LocalDateTime expiresAt) {
            this.expiresAt = expiresAt;
            return this;
        }

        public Builder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder lastUsed(LocalDateTime lastUsed) {
            this.lastUsed = lastUsed;
            return this;
        }

        public Builder deviceInfo(String deviceInfo) {
            this.deviceInfo = deviceInfo;
            return this;
        }

        public Builder ipAddress(String ipAddress) {
            this.ipAddress = ipAddress;
            return this;
        }

        public Builder revoked(boolean revoked) {
            this.revoked = revoked;
            return this;
        }

        public Builder revokedAt(LocalDateTime revokedAt) {
            this.revokedAt = revokedAt;
            return this;
        }

        public RefreshToken build() {
            validateRequiredFields();
            return new RefreshToken(this);
        }

        private void validateRequiredFields() {
            if (userId == null) {
                throw new IllegalArgumentException("User ID is required");
            }
            if (token == null || token.trim().isEmpty()) {
                throw new IllegalArgumentException("Token is required");
            }
            if (expiresAt == null) {
                throw new IllegalArgumentException("Expiration date is required");
            }
        }
    }

    // Factory Methods
    public static RefreshToken createForUser(Long userId, String token, LocalDateTime expiresAt) {
        return builder()
                .userId(userId)
                .token(token)
                .expiresAt(expiresAt)
                .build();
    }

    public static RefreshToken createWithDeviceInfo(Long userId, String token, 
                                                   LocalDateTime expiresAt, String deviceInfo, String ipAddress) {
        return builder()
                .userId(userId)
                .token(token)
                .expiresAt(expiresAt)
                .deviceInfo(deviceInfo)
                .ipAddress(ipAddress)
                .build();
    }

    // Business Logic Methods
    public boolean isExpired() {
        return expiresAt.isBefore(LocalDateTime.now());
    }

    public boolean isActive() {
        return !revoked && !isExpired();
    }

    public boolean isRecentlyUsed() {
        if (lastUsed == null) return false;
        return lastUsed.isAfter(LocalDateTime.now().minusHours(1));
    }

    public void markAsUsed() {
        this.lastUsed = LocalDateTime.now();
    }

    public void revoke() {
        this.revoked = true;
        this.revokedAt = LocalDateTime.now();
    }

    // Getters
    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public String getToken() {
        return token;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getLastUsed() {
        return lastUsed;
    }

    public String getDeviceInfo() {
        return deviceInfo;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public boolean isRevoked() {
        return revoked;
    }

    public LocalDateTime getRevokedAt() {
        return revokedAt;
    }

    // Setters (for JPA compatibility)
    public void setId(Long id) {
        this.id = id;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setLastUsed(LocalDateTime lastUsed) {
        this.lastUsed = lastUsed;
    }

    public void setDeviceInfo(String deviceInfo) {
        this.deviceInfo = deviceInfo;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public void setRevoked(boolean revoked) {
        this.revoked = revoked;
    }

    public void setRevokedAt(LocalDateTime revokedAt) {
        this.revokedAt = revokedAt;
    }

    // equals and hashCode
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RefreshToken)) return false;
        RefreshToken that = (RefreshToken) o;
        return Objects.equals(token, that.token);
    }

    @Override
    public int hashCode() {
        return Objects.hash(token);
    }

    // toString
    @Override
    public String toString() {
        return "RefreshToken{" +
                "id=" + id +
                ", userId=" + userId +
                ", token='" + token.substring(0, Math.min(10, token.length())) + "...'" +
                ", expiresAt=" + expiresAt +
                ", revoked=" + revoked +
                ", createdAt=" + createdAt +
                '}';
    }
}