package com.blog.api.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "verification_tokens")
@EntityListeners(AuditingEntityListener.class)
public class VerificationToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @NotNull
    private User user;

    @NotBlank
    @Column(unique = true, nullable = false)
    private String token;

    @Enumerated(EnumType.STRING)
    @Column(name = "token_type", nullable = false)
    @NotNull
    private TokenType tokenType;

    @Column(name = "expires_at", nullable = false)
    @NotNull
    private LocalDateTime expiresAt;

    @Column(name = "used_at")
    private LocalDateTime usedAt;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public enum TokenType {
        EMAIL_VERIFICATION,
        PASSWORD_RESET,
        PHONE_VERIFICATION
    }

    // JPA required constructor
    public VerificationToken() {}

    // Legacy constructor - now private to force builder usage
    private VerificationToken(User user, String token, TokenType tokenType, LocalDateTime expiresAt) {
        this.user = user;
        this.token = token;
        this.tokenType = tokenType;
        this.expiresAt = expiresAt;
    }

    // Private constructor for Builder
    private VerificationToken(Builder builder) {
        this.user = builder.user;
        this.token = builder.token;
        this.tokenType = builder.tokenType;
        this.expiresAt = builder.expiresAt;
        this.usedAt = builder.usedAt;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public TokenType getTokenType() { return tokenType; }
    public void setTokenType(TokenType tokenType) { this.tokenType = tokenType; }

    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }

    public LocalDateTime getUsedAt() { return usedAt; }
    public void setUsedAt(LocalDateTime usedAt) { this.usedAt = usedAt; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    // Business methods
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(this.expiresAt);
    }

    public boolean isUsed() {
        return this.usedAt != null;
    }

    public boolean isValid() {
        return !isExpired() && !isUsed();
    }

    public void markAsUsed() {
        this.usedAt = LocalDateTime.now();
    }

    // Builder Pattern Implementation
    public static class Builder {
        private User user;
        private String token;
        private TokenType tokenType;
        private LocalDateTime expiresAt;
        private LocalDateTime usedAt;

        public Builder user(User user) {
            Objects.requireNonNull(user, "User cannot be null");
            this.user = user;
            return this;
        }

        public Builder token(String token) {
            Objects.requireNonNull(token, "Token cannot be null");
            if (token.trim().isEmpty()) {
                throw new IllegalArgumentException("Token cannot be empty");
            }
            this.token = token.trim();
            return this;
        }

        public Builder tokenType(TokenType tokenType) {
            Objects.requireNonNull(tokenType, "Token type cannot be null");
            this.tokenType = tokenType;
            return this;
        }

        public Builder expiresAt(LocalDateTime expiresAt) {
            Objects.requireNonNull(expiresAt, "Expiration date cannot be null");
            if (expiresAt.isBefore(LocalDateTime.now())) {
                throw new IllegalArgumentException("Expiration date cannot be in the past");
            }
            this.expiresAt = expiresAt;
            return this;
        }

        public Builder expiresIn(long hours) {
            return expiresAt(LocalDateTime.now().plusHours(hours));
        }

        public Builder expiresInMinutes(long minutes) {
            return expiresAt(LocalDateTime.now().plusMinutes(minutes));
        }

        public Builder usedAt(LocalDateTime usedAt) {
            this.usedAt = usedAt;
            return this;
        }

        public Builder markAsUsed() {
            this.usedAt = LocalDateTime.now();
            return this;
        }

        public VerificationToken build() {
            // Final validation of required fields
            Objects.requireNonNull(user, "User is required");
            Objects.requireNonNull(token, "Token is required");
            Objects.requireNonNull(tokenType, "Token type is required");
            Objects.requireNonNull(expiresAt, "Expiration date is required");

            // Business rule validations
            if (expiresAt.isBefore(LocalDateTime.now())) {
                throw new IllegalArgumentException("Token cannot be created with past expiration date");
            }

            return new VerificationToken(this);
        }
    }

    // Factory Methods
    public static Builder newInstance() {
        return new Builder();
    }

    public static Builder from(VerificationToken other) {
        Objects.requireNonNull(other, "VerificationToken cannot be null");
        return new Builder()
                .user(other.getUser())
                .token(other.getToken())
                .tokenType(other.getTokenType())
                .expiresAt(other.getExpiresAt())
                .usedAt(other.getUsedAt());
    }

    public static Builder of(User user, TokenType tokenType) {
        return new Builder()
                .user(user)
                .tokenType(tokenType);
    }

    public static Builder of(User user, String token, TokenType tokenType) {
        return new Builder()
                .user(user)
                .token(token)
                .tokenType(tokenType);
    }

    public static Builder forEmailVerification(User user) {
        return new Builder()
                .user(user)
                .tokenType(TokenType.EMAIL_VERIFICATION)
                .expiresIn(24); // 24 hours default
    }

    public static Builder forEmailVerification(User user, String token) {
        return new Builder()
                .user(user)
                .token(token)
                .tokenType(TokenType.EMAIL_VERIFICATION)
                .expiresIn(24); // 24 hours default
    }

    public static Builder forPasswordReset(User user) {
        return new Builder()
                .user(user)
                .tokenType(TokenType.PASSWORD_RESET)
                .expiresInMinutes(15); // 15 minutes default
    }

    public static Builder forPasswordReset(User user, String token) {
        return new Builder()
                .user(user)
                .token(token)
                .tokenType(TokenType.PASSWORD_RESET)
                .expiresInMinutes(15); // 15 minutes default
    }

    public static Builder forPhoneVerification(User user) {
        return new Builder()
                .user(user)
                .tokenType(TokenType.PHONE_VERIFICATION)
                .expiresInMinutes(10); // 10 minutes default
    }

    public static Builder forPhoneVerification(User user, String token) {
        return new Builder()
                .user(user)
                .token(token)
                .tokenType(TokenType.PHONE_VERIFICATION)
                .expiresInMinutes(10); // 10 minutes default
    }
}