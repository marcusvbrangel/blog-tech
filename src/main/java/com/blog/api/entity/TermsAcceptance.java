package com.blog.api.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "terms_acceptance")
@EntityListeners(AuditingEntityListener.class)
public class TermsAcceptance {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @NotBlank
    @Size(max = 10)
    @Column(name = "terms_version", nullable = false)
    private String termsVersion;
    
    @CreatedDate
    @Column(name = "accepted_at", nullable = false, updatable = false)
    private LocalDateTime acceptedAt;
    
    @Column(name = "ip_address")
    private String ipAddress;
    
    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;
    
    // Constructors
    public TermsAcceptance() {
        // JPA constructor
    }
    
    private TermsAcceptance(Builder builder) {
        this.user = builder.user;
        this.termsVersion = builder.termsVersion;
        this.acceptedAt = builder.acceptedAt;
        this.ipAddress = builder.ipAddress;
        this.userAgent = builder.userAgent;
    }
    
    // Builder Pattern Implementation
    public static class Builder {
        private User user;
        private String termsVersion;
        private LocalDateTime acceptedAt;
        private String ipAddress;
        private String userAgent;
        
        public Builder user(User user) {
            Objects.requireNonNull(user, "User cannot be null");
            this.user = user;
            return this;
        }
        
        public Builder termsVersion(String termsVersion) {
            Objects.requireNonNull(termsVersion, "Terms version cannot be null");
            if (termsVersion.trim().isEmpty()) {
                throw new IllegalArgumentException("Terms version cannot be empty");
            }
            if (termsVersion.length() > 10) {
                throw new IllegalArgumentException("Terms version cannot exceed 10 characters");
            }
            this.termsVersion = termsVersion.trim();
            return this;
        }
        
        public Builder acceptedAt(LocalDateTime acceptedAt) {
            this.acceptedAt = acceptedAt;
            return this;
        }
        
        public Builder ipAddress(String ipAddress) {
            if (ipAddress != null && ipAddress.trim().isEmpty()) {
                this.ipAddress = null;
            } else {
                this.ipAddress = ipAddress != null ? ipAddress.trim() : null;
            }
            return this;
        }
        
        public Builder userAgent(String userAgent) {
            if (userAgent != null && userAgent.trim().isEmpty()) {
                this.userAgent = null;
            } else {
                this.userAgent = userAgent != null ? userAgent.trim() : null;
            }
            return this;
        }
        
        public TermsAcceptance build() {
            Objects.requireNonNull(user, "User is required");
            Objects.requireNonNull(termsVersion, "Terms version is required");
            
            return new TermsAcceptance(this);
        }
    }
    
    // Factory Methods
    public static Builder of(User user, String termsVersion) {
        return new Builder()
                .user(user)
                .termsVersion(termsVersion);
    }
    
    public static Builder newInstance() {
        return new Builder();
    }
    
    public static Builder from(TermsAcceptance other) {
        Objects.requireNonNull(other, "Source TermsAcceptance cannot be null");
        return new Builder()
                .user(other.user)
                .termsVersion(other.termsVersion)
                .acceptedAt(other.acceptedAt)
                .ipAddress(other.ipAddress)
                .userAgent(other.userAgent);
    }
    
    public static Builder forUser(User user) {
        return new Builder().user(user);
    }
    
    public static Builder withCurrentTimestamp(User user, String termsVersion) {
        return of(user, termsVersion)
                .acceptedAt(LocalDateTime.now());
    }
    
    // Getters
    public Long getId() {
        return id;
    }
    
    public User getUser() {
        return user;
    }
    
    public String getTermsVersion() {
        return termsVersion;
    }
    
    public LocalDateTime getAcceptedAt() {
        return acceptedAt;
    }
    
    public String getIpAddress() {
        return ipAddress;
    }
    
    public String getUserAgent() {
        return userAgent;
    }
    
    // Setters (for JPA)
    public void setId(Long id) {
        this.id = id;
    }
    
    public void setUser(User user) {
        this.user = user;
    }
    
    public void setTermsVersion(String termsVersion) {
        this.termsVersion = termsVersion;
    }
    
    public void setAcceptedAt(LocalDateTime acceptedAt) {
        this.acceptedAt = acceptedAt;
    }
    
    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }
    
    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }
    
    // Business Methods
    public boolean isVersionMatch(String version) {
        return Objects.equals(this.termsVersion, version);
    }
    
    public boolean isAcceptedAfter(LocalDateTime dateTime) {
        return acceptedAt != null && acceptedAt.isAfter(dateTime);
    }
    
    public boolean isAcceptedBefore(LocalDateTime dateTime) {
        return acceptedAt != null && acceptedAt.isBefore(dateTime);
    }
    
    public boolean hasIpAddress() {
        return ipAddress != null && !ipAddress.trim().isEmpty();
    }
    
    public boolean hasUserAgent() {
        return userAgent != null && !userAgent.trim().isEmpty();
    }
    
    // equals, hashCode, toString
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TermsAcceptance that = (TermsAcceptance) o;
        return Objects.equals(id, that.id) && 
               Objects.equals(user, that.user) && 
               Objects.equals(termsVersion, that.termsVersion) && 
               Objects.equals(acceptedAt, that.acceptedAt);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id, user, termsVersion, acceptedAt);
    }
    
    @Override
    public String toString() {
        return "TermsAcceptance{" +
                "id=" + id +
                ", user=" + (user != null ? user.getUsername() : "null") +
                ", termsVersion='" + termsVersion + '\'' +
                ", acceptedAt=" + acceptedAt +
                ", ipAddress='" + ipAddress + '\'' +
                '}';
    }
}