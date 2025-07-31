package com.blog.api.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "posts")
@EntityListeners(AuditingEntityListener.class)
public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(min = 5, max = 200)
    private String title;

    @NotBlank
    @Size(min = 10)
    @Column(columnDefinition = "TEXT")
    private String content;

    private boolean published = false;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Comment> comments = new ArrayList<>();

    // JPA required constructor
    public Post() {}

    // Legacy constructor - mantido para compatibilidade
    public Post(String title, String content, User user, Category category) {
        this.title = title;
        this.content = content;
        this.user = user;
        this.category = category;
    }

    // Private constructor for Builder
    private Post(Builder builder) {
        this.title = builder.title;
        this.content = builder.content;
        this.published = builder.published;
        this.user = builder.user;
        this.category = builder.category;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public boolean isPublished() { return published; }
    public void setPublished(boolean published) { this.published = published; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }

    public List<Comment> getComments() { return comments; }
    public void setComments(List<Comment> comments) { this.comments = comments; }

    // Builder Pattern Implementation
    public static class Builder {
        private String title;
        private String content;
        private boolean published = false;
        private User user;
        private Category category;

        public Builder title(String title) {
            Objects.requireNonNull(title, "Title cannot be null");
            if (title.trim().isEmpty()) {
                throw new IllegalArgumentException("Title cannot be empty");
            }
            if (title.length() < 5 || title.length() > 200) {
                throw new IllegalArgumentException("Title must be between 5 and 200 characters");
            }
            this.title = title.trim();
            return this;
        }

        public Builder content(String content) {
            Objects.requireNonNull(content, "Content cannot be null");
            if (content.trim().isEmpty()) {
                throw new IllegalArgumentException("Content cannot be empty");
            }
            if (content.length() < 10) {
                throw new IllegalArgumentException("Content must be at least 10 characters");
            }
            this.content = content.trim();
            return this;
        }

        public Builder published(boolean published) {
            this.published = published;
            return this;
        }

        public Builder user(User user) {
            Objects.requireNonNull(user, "User cannot be null");
            this.user = user;
            return this;
        }

        public Builder author(User author) {
            return user(author);
        }

        public Builder category(Category category) {
            this.category = category;
            return this;
        }

        public Post build() {
            // Final validation of required fields
            Objects.requireNonNull(title, "Title is required");
            Objects.requireNonNull(content, "Content is required");
            Objects.requireNonNull(user, "User is required");

            return new Post(this);
        }
    }

    // Factory Methods
    public static Builder newInstance() {
        return new Builder();
    }

    public static Builder from(Post other) {
        Objects.requireNonNull(other, "Post cannot be null");
        return new Builder()
                .title(other.getTitle())
                .content(other.getContent())
                .published(other.isPublished())
                .user(other.getUser())
                .category(other.getCategory());
    }

    public static Builder of(String title, String content) {
        return new Builder()
                .title(title)
                .content(content);
    }

    public static Builder of(String title, String content, User user) {
        return new Builder()
                .title(title)
                .content(content)
                .user(user);
    }

    public static Builder of(String title, String content, User user, Category category) {
        return new Builder()
                .title(title)
                .content(content)
                .user(user)
                .category(category);
    }

    public static Builder withDefaults() {
        return new Builder()
                .published(false);
    }

    public static Builder asDraft() {
        return withDefaults()
                .published(false);
    }

    public static Builder asPublished() {
        return withDefaults()
                .published(true);
    }

    public static Builder draft(String title, String content, User user) {
        return of(title, content, user)
                .published(false);
    }

    public static Builder published(String title, String content, User user) {
        return of(title, content, user)
                .published(true);
    }
}