package com.blog.api.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Entity representing a newsletter subscriber with LGPD compliance fields.
 * 
 * @author AI-Driven Development
 * @version 1.0
 * @since 2025-08-05
 */
@Entity
@Table(name = "newsletter_subscribers", 
       indexes = {
           @Index(name = "idx_newsletter_email", columnList = "email"),
           @Index(name = "idx_newsletter_status", columnList = "status"),
           @Index(name = "idx_newsletter_created_at", columnList = "created_at")
       })
@EntityListeners(AuditingEntityListener.class)
public class NewsletterSubscriber {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    @Size(max = 255, message = "Email must not exceed 255 characters")
    @Column(unique = true, nullable = false)
    private String email;

    @NotNull(message = "Status is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SubscriptionStatus status = SubscriptionStatus.PENDING;

    // LGPD Compliance Fields
    @Column(name = "consent_given_at")
    private LocalDateTime consentGivenAt;

    @Size(max = 45, message = "Consent IP address must not exceed 45 characters")
    @Column(name = "consent_ip_address", length = 45)
    private String consentIpAddress;

    @Size(max = 500, message = "Consent User-Agent must not exceed 500 characters")
    @Column(name = "consent_user_agent", length = 500)
    private String consentUserAgent;

    @Size(max = 20, message = "Privacy policy version must not exceed 20 characters")
    @Column(name = "privacy_policy_version", length = 20)
    private String privacyPolicyVersion;

    // Status Change Tracking
    @Column(name = "confirmed_at")
    private LocalDateTime confirmedAt;

    @Column(name = "unsubscribed_at")
    private LocalDateTime unsubscribedAt;

    // Audit Fields
    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // JPA required constructor
    public NewsletterSubscriber() {}

    // Private constructor for Builder
    private NewsletterSubscriber(Builder builder) {
        this.email = builder.email;
        this.status = builder.status;
        this.consentGivenAt = builder.consentGivenAt;
        this.consentIpAddress = builder.consentIpAddress;
        this.consentUserAgent = builder.consentUserAgent;
        this.privacyPolicyVersion = builder.privacyPolicyVersion;
        this.confirmedAt = builder.confirmedAt;
        this.unsubscribedAt = builder.unsubscribedAt;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public SubscriptionStatus getStatus() { return status; }
    public void setStatus(SubscriptionStatus status) { this.status = status; }

    public LocalDateTime getConsentGivenAt() { return consentGivenAt; }
    public void setConsentGivenAt(LocalDateTime consentGivenAt) { this.consentGivenAt = consentGivenAt; }

    public String getConsentIpAddress() { return consentIpAddress; }
    public void setConsentIpAddress(String consentIpAddress) { this.consentIpAddress = consentIpAddress; }

    public String getConsentUserAgent() { return consentUserAgent; }
    public void setConsentUserAgent(String consentUserAgent) { this.consentUserAgent = consentUserAgent; }

    public String getPrivacyPolicyVersion() { return privacyPolicyVersion; }
    public void setPrivacyPolicyVersion(String privacyPolicyVersion) { this.privacyPolicyVersion = privacyPolicyVersion; }

    public LocalDateTime getConfirmedAt() { return confirmedAt; }
    public void setConfirmedAt(LocalDateTime confirmedAt) { this.confirmedAt = confirmedAt; }

    public LocalDateTime getUnsubscribedAt() { return unsubscribedAt; }
    public void setUnsubscribedAt(LocalDateTime unsubscribedAt) { this.unsubscribedAt = unsubscribedAt; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    // Business Methods
    public boolean isPending() { return status == SubscriptionStatus.PENDING; }
    public boolean isConfirmed() { return status == SubscriptionStatus.CONFIRMED; }
    public boolean isUnsubscribed() { return status == SubscriptionStatus.UNSUBSCRIBED; }
    public boolean isDeleted() { return status == SubscriptionStatus.DELETED; }

    public void confirm() {
        this.status = SubscriptionStatus.CONFIRMED;
        this.confirmedAt = LocalDateTime.now();
    }

    public void unsubscribe() {
        this.status = SubscriptionStatus.UNSUBSCRIBED;
        this.unsubscribedAt = LocalDateTime.now();
    }

    public void markAsDeleted() {
        this.status = SubscriptionStatus.DELETED;
    }

    // Builder Pattern Implementation
    public static class Builder {
        private String email;
        private SubscriptionStatus status = SubscriptionStatus.PENDING;
        private LocalDateTime consentGivenAt;
        private String consentIpAddress;
        private String consentUserAgent;
        private String privacyPolicyVersion;
        private LocalDateTime confirmedAt;
        private LocalDateTime unsubscribedAt;

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

        public Builder status(SubscriptionStatus status) {
            Objects.requireNonNull(status, "Status cannot be null");
            this.status = status;
            return this;
        }

        public Builder consentGivenAt(LocalDateTime consentGivenAt) {
            this.consentGivenAt = consentGivenAt;
            return this;
        }

        public Builder consentIpAddress(String consentIpAddress) {
            if (consentIpAddress != null && consentIpAddress.length() > 45) {
                throw new IllegalArgumentException("Consent IP address must not exceed 45 characters");
            }
            this.consentIpAddress = consentIpAddress;
            return this;
        }

        public Builder consentUserAgent(String consentUserAgent) {
            if (consentUserAgent != null && consentUserAgent.length() > 500) {
                throw new IllegalArgumentException("Consent User-Agent must not exceed 500 characters");
            }
            this.consentUserAgent = consentUserAgent;
            return this;
        }

        public Builder privacyPolicyVersion(String privacyPolicyVersion) {
            if (privacyPolicyVersion != null && privacyPolicyVersion.length() > 20) {
                throw new IllegalArgumentException("Privacy policy version must not exceed 20 characters");
            }
            this.privacyPolicyVersion = privacyPolicyVersion;
            return this;
        }

        public Builder confirmedAt(LocalDateTime confirmedAt) {
            this.confirmedAt = confirmedAt;
            return this;
        }

        public Builder unsubscribedAt(LocalDateTime unsubscribedAt) {
            this.unsubscribedAt = unsubscribedAt;
            return this;
        }

        public NewsletterSubscriber build() {
            // Final validation of required fields
            Objects.requireNonNull(email, "Email is required");
            Objects.requireNonNull(status, "Status is required");

            // Business rule validations
            if (status == SubscriptionStatus.CONFIRMED && confirmedAt == null) {
                this.confirmedAt = LocalDateTime.now();
            }
            if (status == SubscriptionStatus.UNSUBSCRIBED && unsubscribedAt == null) {
                this.unsubscribedAt = LocalDateTime.now();
            }

            // Set consent timestamp if not provided
            if (consentGivenAt == null) {
                this.consentGivenAt = LocalDateTime.now();
            }

            return new NewsletterSubscriber(this);
        }
    }

    // Factory Methods
    public static Builder newInstance() {
        return new Builder();
    }

    public static Builder from(NewsletterSubscriber other) {
        Objects.requireNonNull(other, "NewsletterSubscriber cannot be null");
        return new Builder()
                .email(other.getEmail())
                .status(other.getStatus())
                .consentGivenAt(other.getConsentGivenAt())
                .consentIpAddress(other.getConsentIpAddress())
                .consentUserAgent(other.getConsentUserAgent())
                .privacyPolicyVersion(other.getPrivacyPolicyVersion())
                .confirmedAt(other.getConfirmedAt())
                .unsubscribedAt(other.getUnsubscribedAt());
    }

    public static Builder of(String email) {
        return new Builder().email(email);
    }

    public static Builder pending(String email) {
        return new Builder()
                .email(email)
                .status(SubscriptionStatus.PENDING);
    }

    public static Builder confirmed(String email) {
        return new Builder()
                .email(email)
                .status(SubscriptionStatus.CONFIRMED)
                .confirmedAt(LocalDateTime.now());
    }

    public static Builder withConsent(String email, String ipAddress, String userAgent, String policyVersion) {
        return new Builder()
                .email(email)
                .consentIpAddress(ipAddress)
                .consentUserAgent(userAgent)
                .privacyPolicyVersion(policyVersion)
                .consentGivenAt(LocalDateTime.now());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NewsletterSubscriber that = (NewsletterSubscriber) o;
        return Objects.equals(email, that.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(email);
    }

    @Override
    public String toString() {
        return "NewsletterSubscriber{" +
                "id=" + id +
                ", email='" + email + '\'' +
                ", status=" + status +
                ", consentGivenAt=" + consentGivenAt +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}