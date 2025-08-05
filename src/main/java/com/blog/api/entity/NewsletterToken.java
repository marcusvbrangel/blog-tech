package com.blog.api.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Entity representing newsletter-related tokens for various operations.
 * This entity manages tokens for email confirmation, unsubscribe, and data requests.
 * 
 * Design decisions:
 * - Uses email string instead of FK to NewsletterSubscriber for flexibility
 * - Supports multiple token types with different expiration policies
 * - Includes used/unused tracking for security
 * - UUID-based tokens for security and uniqueness
 * 
 * @author AI-Driven Development
 * @version 1.0
 * @since 2025-08-05
 */
@Entity
@Table(name = "newsletter_tokens",
       indexes = {
           @Index(name = "idx_newsletter_token_token", columnList = "token", unique = true),
           @Index(name = "idx_newsletter_token_email", columnList = "email"),
           @Index(name = "idx_newsletter_token_type", columnList = "token_type"),
           @Index(name = "idx_newsletter_token_expires_at", columnList = "expires_at"),
           @Index(name = "idx_newsletter_token_email_type", columnList = "email, token_type")
       })
@EntityListeners(AuditingEntityListener.class)
public class NewsletterToken {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    @Size(max = 255, message = "Email must not exceed 255 characters")
    @Column(nullable = false, length = 255)
    private String email;

    @NotBlank(message = "Token is required")
    @Size(max = 36, message = "Token must not exceed 36 characters") // UUID length
    @Column(unique = true, nullable = false, length = 36)
    private String token;

    @NotNull(message = "Token type is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "token_type", nullable = false, length = 20)
    private NewsletterTokenType tokenType;

    @NotNull(message = "Expiration date is required")
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "used_at")
    private LocalDateTime usedAt;

    // Additional metadata for audit and context
    @Size(max = 45, message = "IP address must not exceed 45 characters")
    @Column(name = "created_ip", length = 45)
    private String createdIp;

    @Size(max = 500, message = "User-Agent must not exceed 500 characters")
    @Column(name = "created_user_agent", length = 500)
    private String createdUserAgent;

    // Audit Fields
    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    // JPA required constructor
    public NewsletterToken() {}

    // Private constructor for Builder
    private NewsletterToken(Builder builder) {
        this.email = builder.email;
        this.token = builder.token;
        this.tokenType = builder.tokenType;
        this.expiresAt = builder.expiresAt;
        this.usedAt = builder.usedAt;
        this.createdIp = builder.createdIp;
        this.createdUserAgent = builder.createdUserAgent;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public NewsletterTokenType getTokenType() { return tokenType; }
    public void setTokenType(NewsletterTokenType tokenType) { this.tokenType = tokenType; }

    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }

    public LocalDateTime getUsedAt() { return usedAt; }
    public void setUsedAt(LocalDateTime usedAt) { this.usedAt = usedAt; }

    public String getCreatedIp() { return createdIp; }
    public void setCreatedIp(String createdIp) { this.createdIp = createdIp; }

    public String getCreatedUserAgent() { return createdUserAgent; }
    public void setCreatedUserAgent(String createdUserAgent) { this.createdUserAgent = createdUserAgent; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    // Business Methods
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
        private String email;
        private String token;
        private NewsletterTokenType tokenType;
        private LocalDateTime expiresAt;
        private LocalDateTime usedAt;
        private String createdIp;
        private String createdUserAgent;

        public Builder email(String email) {
            Objects.requireNonNull(email, "Email cannot be null");
            if (email.trim().isEmpty()) {
                throw new IllegalArgumentException("Email cannot be empty");
            }
            if (!email.contains("@") || !email.contains(".")) {
                throw new IllegalArgumentException("Invalid email format");
            }
            this.email = email.trim().toLowerCase();
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

        public Builder generateToken() {
            this.token = UUID.randomUUID().toString();
            return this;
        }

        public Builder tokenType(NewsletterTokenType tokenType) {
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

        public Builder expiresInDays(long days) {
            return expiresAt(LocalDateTime.now().plusDays(days));
        }

        public Builder usedAt(LocalDateTime usedAt) {
            this.usedAt = usedAt;
            return this;
        }

        public Builder markAsUsed() {
            this.usedAt = LocalDateTime.now();
            return this;
        }

        public Builder createdIp(String createdIp) {
            if (createdIp != null && createdIp.length() > 45) {
                throw new IllegalArgumentException("IP address must not exceed 45 characters");
            }
            this.createdIp = createdIp;
            return this;
        }

        public Builder createdUserAgent(String createdUserAgent) {
            if (createdUserAgent != null && createdUserAgent.length() > 500) {
                // Truncate if too long
                createdUserAgent = createdUserAgent.substring(0, 500);
            }
            this.createdUserAgent = createdUserAgent;
            return this;
        }

        public NewsletterToken build() {
            // Final validation of required fields
            Objects.requireNonNull(email, "Email is required");
            Objects.requireNonNull(token, "Token is required");
            Objects.requireNonNull(tokenType, "Token type is required");
            Objects.requireNonNull(expiresAt, "Expiration date is required");

            // Business rule validations
            if (expiresAt.isBefore(LocalDateTime.now())) {
                throw new IllegalArgumentException("Token cannot be created with past expiration date");
            }

            return new NewsletterToken(this);
        }
    }

    // Factory Methods
    public static Builder newInstance() {
        return new Builder();
    }

    public static Builder from(NewsletterToken other) {
        Objects.requireNonNull(other, "NewsletterToken cannot be null");
        return new Builder()
                .email(other.getEmail())
                .token(other.getToken())
                .tokenType(other.getTokenType())
                .expiresAt(other.getExpiresAt())
                .usedAt(other.getUsedAt())
                .createdIp(other.getCreatedIp())
                .createdUserAgent(other.getCreatedUserAgent());
    }

    public static Builder of(String email, NewsletterTokenType tokenType) {
        return new Builder()
                .email(email)
                .tokenType(tokenType)
                .generateToken();
    }

    public static Builder of(String email, String token, NewsletterTokenType tokenType) {
        return new Builder()
                .email(email)
                .token(token)
                .tokenType(tokenType);
    }

    // Specialized factory methods for different token types
    public static Builder forConfirmation(String email) {
        return new Builder()
                .email(email)
                .tokenType(NewsletterTokenType.CONFIRMATION)
                .generateToken()
                .expiresIn(48); // 48 hours default for confirmation
    }

    public static Builder forConfirmation(String email, String createdIp, String userAgent) {
        return forConfirmation(email)
                .createdIp(createdIp)
                .createdUserAgent(userAgent);
    }

    public static Builder forUnsubscribe(String email) {
        return new Builder()
                .email(email)
                .tokenType(NewsletterTokenType.UNSUBSCRIBE)
                .generateToken()
                .expiresInDays(365); // 1 year default for unsubscribe (long-lived)
    }

    public static Builder forUnsubscribe(String email, String createdIp, String userAgent) {
        return forUnsubscribe(email)
                .createdIp(createdIp)
                .createdUserAgent(userAgent);
    }

    public static Builder forDataRequest(String email) {
        return new Builder()
                .email(email)
                .tokenType(NewsletterTokenType.DATA_REQUEST)
                .generateToken()
                .expiresInDays(7); // 7 days default for data requests
    }

    public static Builder forDataRequest(String email, String createdIp, String userAgent) {
        return forDataRequest(email)
                .createdIp(createdIp)
                .createdUserAgent(userAgent);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NewsletterToken that = (NewsletterToken) o;
        return Objects.equals(token, that.token);
    }

    @Override
    public int hashCode() {
        return Objects.hash(token);
    }

    @Override
    public String toString() {
        return "NewsletterToken{" +
                "id=" + id +
                ", email='" + email + '\'' +
                ", tokenType=" + tokenType +
                ", expiresAt=" + expiresAt +
                ", usedAt=" + usedAt +
                ", createdAt=" + createdAt +
                '}';
    }
}