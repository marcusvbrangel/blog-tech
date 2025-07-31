package com.blog.api.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "comments")
@EntityListeners(AuditingEntityListener.class)
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(min = 1, max = 1000)
    @Column(columnDefinition = "TEXT")
    private String content;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Comment parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Comment> replies = new ArrayList<>();

    // JPA required constructor
    public Comment() {}

    // Legacy constructor - now private to force builder usage
    private Comment(String content, Post post, User user, Comment parent) {
        this.content = content;
        this.post = post;
        this.user = user;
        this.parent = parent;
    }

    // Private constructor for Builder
    private Comment(Builder builder) {
        this.content = builder.content;
        this.post = builder.post;
        this.user = builder.user;
        this.parent = builder.parent;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public Post getPost() { return post; }
    public void setPost(Post post) { this.post = post; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Comment getParent() { return parent; }
    public void setParent(Comment parent) { this.parent = parent; }

    public List<Comment> getReplies() { return replies; }
    public void setReplies(List<Comment> replies) { this.replies = replies; }

    // Builder Pattern Implementation
    public static class Builder {
        private String content;
        private Post post;
        private User user;
        private Comment parent;

        public Builder content(String content) {
            Objects.requireNonNull(content, "Content cannot be null");
            if (content.trim().isEmpty()) {
                throw new IllegalArgumentException("Content cannot be empty");
            }
            if (content.length() > 1000) {
                throw new IllegalArgumentException("Content must not exceed 1000 characters");
            }
            this.content = content.trim();
            return this;
        }

        public Builder post(Post post) {
            Objects.requireNonNull(post, "Post cannot be null");
            this.post = post;
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

        public Builder parent(Comment parent) {
            this.parent = parent;
            return this;
        }

        public Builder replyTo(Comment parentComment) {
            Objects.requireNonNull(parentComment, "Parent comment cannot be null");
            this.parent = parentComment;
            // Inherit post from parent comment
            if (this.post == null) {
                this.post = parentComment.getPost();
            }
            return this;
        }

        public Comment build() {
            // Final validation of required fields
            Objects.requireNonNull(content, "Content is required");
            Objects.requireNonNull(post, "Post is required");
            Objects.requireNonNull(user, "User is required");

            // Business rule validations
            if (parent != null && !parent.getPost().equals(post)) {
                throw new IllegalArgumentException("Reply must belong to the same post as parent comment");
            }

            return new Comment(this);
        }
    }

    // Factory Methods
    public static Builder newInstance() {
        return new Builder();
    }

    public static Builder from(Comment other) {
        Objects.requireNonNull(other, "Comment cannot be null");
        return new Builder()
                .content(other.getContent())
                .post(other.getPost())
                .user(other.getUser())
                .parent(other.getParent());
    }

    public static Builder of(String content) {
        return new Builder()
                .content(content);
    }

    public static Builder of(String content, Post post, User user) {
        return new Builder()
                .content(content)
                .post(post)
                .user(user);
    }

    public static Builder comment(String content, Post post, User user) {
        return of(content, post, user);
    }

    public static Builder reply(String content, Comment parentComment, User user) {
        Objects.requireNonNull(parentComment, "Parent comment cannot be null");
        return new Builder()
                .content(content)
                .replyTo(parentComment)
                .user(user);
    }

    public static Builder asReply(Comment parentComment) {
        Objects.requireNonNull(parentComment, "Parent comment cannot be null");
        return new Builder()
                .replyTo(parentComment);
    }
}