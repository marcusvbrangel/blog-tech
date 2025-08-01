package com.blog.api.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "users")
@EntityListeners(AuditingEntityListener.class)
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(min = 3, max = 50)
    @Column(unique = true)
    private String username;

    @NotBlank
    @Email
    @Column(unique = true)
    private String email;

    @NotBlank
    @Size(min = 6)
    private String password;

    @Enumerated(EnumType.STRING)
    private Role role = Role.USER;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "email_verified")
    private Boolean emailVerified = false;

    @Column(name = "email_verified_at")
    private LocalDateTime emailVerifiedAt;

    @Column(name = "account_locked")
    private Boolean accountLocked = false;

    @Column(name = "locked_until")
    private LocalDateTime lockedUntil;

    @Column(name = "password_changed_at")
    private LocalDateTime passwordChangedAt;

    @Column(name = "last_login")
    private LocalDateTime lastLogin;

    @Column(name = "failed_login_attempts")
    private Integer failedLoginAttempts = 0;

    @Column(name = "terms_accepted_version")
    private String termsAcceptedVersion;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Post> posts = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Comment> comments = new ArrayList<>();

    public enum Role {
        USER, AUTHOR, ADMIN
    }

    // JPA required constructor
    public User() {}

    // Legacy constructor - now private to force builder usage
    private User(String username, String email, String password, Role role) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.role = role;
    }

    // Private constructor for Builder
    private User(Builder builder) {
        this.username = builder.username;
        this.email = builder.email;
        this.password = builder.password;
        this.role = builder.role;
        this.emailVerified = builder.emailVerified;
        this.accountLocked = builder.accountLocked;
        this.failedLoginAttempts = builder.failedLoginAttempts;
        this.emailVerifiedAt = builder.emailVerifiedAt;
        this.lockedUntil = builder.lockedUntil;
        this.passwordChangedAt = builder.passwordChangedAt;
        this.lastLogin = builder.lastLogin;
        this.termsAcceptedVersion = builder.termsAcceptedVersion;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public List<Post> getPosts() { return posts; }
    public void setPosts(List<Post> posts) { this.posts = posts; }

    public List<Comment> getComments() { return comments; }
    public void setComments(List<Comment> comments) { this.comments = comments; }

    public Boolean getEmailVerified() { return emailVerified; }
    public void setEmailVerified(Boolean emailVerified) { this.emailVerified = emailVerified; }

    public LocalDateTime getEmailVerifiedAt() { return emailVerifiedAt; }
    public void setEmailVerifiedAt(LocalDateTime emailVerifiedAt) { this.emailVerifiedAt = emailVerifiedAt; }

    public Boolean getAccountLocked() { return accountLocked; }
    public void setAccountLocked(Boolean accountLocked) { this.accountLocked = accountLocked; }

    public LocalDateTime getLockedUntil() { return lockedUntil; }
    public void setLockedUntil(LocalDateTime lockedUntil) { this.lockedUntil = lockedUntil; }

    public LocalDateTime getPasswordChangedAt() { return passwordChangedAt; }
    public void setPasswordChangedAt(LocalDateTime passwordChangedAt) { this.passwordChangedAt = passwordChangedAt; }

    public LocalDateTime getLastLogin() { return lastLogin; }
    public void setLastLogin(LocalDateTime lastLogin) { this.lastLogin = lastLogin; }

    public Integer getFailedLoginAttempts() { return failedLoginAttempts; }
    public void setFailedLoginAttempts(Integer failedLoginAttempts) { this.failedLoginAttempts = failedLoginAttempts; }

    public String getTermsAcceptedVersion() { return termsAcceptedVersion; }
    public void setTermsAcceptedVersion(String termsAcceptedVersion) { this.termsAcceptedVersion = termsAcceptedVersion; }

    public boolean isEmailVerified() { return Boolean.TRUE.equals(emailVerified); }
    public boolean isAccountLocked() { return Boolean.TRUE.equals(accountLocked); }
    public boolean hasAcceptedTerms() { return termsAcceptedVersion != null && !termsAcceptedVersion.trim().isEmpty(); }
    public boolean hasAcceptedTermsVersion(String version) { return Objects.equals(termsAcceptedVersion, version); }

    // Builder Pattern Implementation
    public static class Builder {
        private String username;
        private String email;
        private String password;
        private Role role = Role.USER;
        private Boolean emailVerified = false;
        private Boolean accountLocked = false;
        private Integer failedLoginAttempts = 0;
        private LocalDateTime emailVerifiedAt;
        private LocalDateTime lockedUntil;
        private LocalDateTime passwordChangedAt;
        private LocalDateTime lastLogin;
        private String termsAcceptedVersion;

        public Builder username(String username) {
            Objects.requireNonNull(username, "Username cannot be null");
            if (username.trim().isEmpty()) {
                throw new IllegalArgumentException("Username cannot be empty");
            }
            if (username.length() < 3 || username.length() > 50) {
                throw new IllegalArgumentException("Username must be between 3 and 50 characters");
            }
            this.username = username.trim();
            return this;
        }

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

        public Builder password(String password) {
            Objects.requireNonNull(password, "Password cannot be null");
            if (password.length() < 6) {
                throw new IllegalArgumentException("Password must be at least 6 characters");
            }
            this.password = password;
            return this;
        }

        public Builder role(Role role) {
            Objects.requireNonNull(role, "Role cannot be null");
            this.role = role;
            return this;
        }

        public Builder emailVerified(Boolean emailVerified) {
            this.emailVerified = emailVerified != null ? emailVerified : false;
            return this;
        }

        public Builder accountLocked(Boolean accountLocked) {
            this.accountLocked = accountLocked != null ? accountLocked : false;
            return this;
        }

        public Builder failedLoginAttempts(Integer attempts) {
            this.failedLoginAttempts = attempts != null ? attempts : 0;
            return this;
        }

        public Builder emailVerifiedAt(LocalDateTime emailVerifiedAt) {
            this.emailVerifiedAt = emailVerifiedAt;
            return this;
        }

        public Builder lockedUntil(LocalDateTime lockedUntil) {
            this.lockedUntil = lockedUntil;
            return this;
        }

        public Builder passwordChangedAt(LocalDateTime passwordChangedAt) {
            this.passwordChangedAt = passwordChangedAt;
            return this;
        }

        public Builder lastLogin(LocalDateTime lastLogin) {
            this.lastLogin = lastLogin;
            return this;
        }

        public Builder termsAcceptedVersion(String termsAcceptedVersion) {
            this.termsAcceptedVersion = termsAcceptedVersion;
            return this;
        }

        public User build() {
            // Final validation of required fields
            Objects.requireNonNull(username, "Username is required");
            Objects.requireNonNull(email, "Email is required");
            Objects.requireNonNull(password, "Password is required");
            Objects.requireNonNull(role, "Role is required");

            // Business rule validations
            if (emailVerified && emailVerifiedAt == null) {
                this.emailVerifiedAt = LocalDateTime.now();
            }

            return new User(this);
        }
    }

    // Factory Methods
    public static Builder newInstance() {
        return new Builder();
    }

    public static Builder from(User other) {
        Objects.requireNonNull(other, "User cannot be null");
        return new Builder()
                .username(other.getUsername())
                .email(other.getEmail())
                .password(other.getPassword())
                .role(other.getRole())
                .emailVerified(other.getEmailVerified())
                .accountLocked(other.getAccountLocked())
                .failedLoginAttempts(other.getFailedLoginAttempts())
                .emailVerifiedAt(other.getEmailVerifiedAt())
                .lockedUntil(other.getLockedUntil())
                .passwordChangedAt(other.getPasswordChangedAt())
                .lastLogin(other.getLastLogin())
                .termsAcceptedVersion(other.getTermsAcceptedVersion());
    }

    public static Builder of(String username, String email) {
        return new Builder()
                .username(username)
                .email(email);
    }

    public static Builder of(String username, String email, String password) {
        return new Builder()
                .username(username)
                .email(email)
                .password(password);
    }

    public static Builder withDefaults() {
        return new Builder()
                .role(Role.USER)
                .emailVerified(false)
                .accountLocked(false)
                .failedLoginAttempts(0);
    }

    public static Builder asAdmin() {
        return withDefaults()
                .role(Role.ADMIN);
    }

    public static Builder asAuthor() {
        return withDefaults()
                .role(Role.AUTHOR);
    }

    public static Builder verified() {
        return withDefaults()
                .emailVerified(true)
                .emailVerifiedAt(LocalDateTime.now());
    }
}