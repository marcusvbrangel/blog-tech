package com.blog.api.dto;

import com.blog.api.entity.Post;

import java.time.LocalDateTime;
import java.io.Serializable;

public class PostDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long id;
    private String title;
    private String content;
    private boolean published;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String authorUsername;
    private String categoryName;
    private int commentCount;

    public PostDTO() {}

    public PostDTO(Long id, String title, String content, boolean published, 
                   LocalDateTime createdAt, LocalDateTime updatedAt, 
                   String authorUsername, String categoryName, int commentCount) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.published = published;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.authorUsername = authorUsername;
        this.categoryName = categoryName;
        this.commentCount = commentCount;
    }

    public static PostDTO fromEntity(Post post) {
        try {
            int commentCount = 0;
            try {
                commentCount = post.getComments() != null ? post.getComments().size() : 0;
            } catch (Exception e) {
                // Handle lazy loading exception by setting comment count to 0
                commentCount = 0;
            }
            
            return new PostDTO(
                post.getId(),
                post.getTitle(),
                post.getContent(),
                post.isPublished(),
                post.getCreatedAt(),
                post.getUpdatedAt(),
                post.getUser().getUsername(),
                post.getCategory() != null ? post.getCategory().getName() : null,
                commentCount
            );
        } catch (Exception e) {
            System.err.println("ERROR in PostDTO.fromEntity: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
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

    public String getAuthorUsername() { return authorUsername; }
    public void setAuthorUsername(String authorUsername) { this.authorUsername = authorUsername; }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

    public int getCommentCount() { return commentCount; }
    public void setCommentCount(int commentCount) { this.commentCount = commentCount; }
}