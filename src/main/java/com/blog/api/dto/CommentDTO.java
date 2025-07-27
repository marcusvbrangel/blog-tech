package com.blog.api.dto;

import com.blog.api.entity.Comment;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class CommentDTO {
    private Long id;
    
    @NotBlank(message = "Content is required")
    @Size(min = 1, max = 1000, message = "Content must be between 1 and 1000 characters")
    private String content;
    
    private LocalDateTime createdAt;
    private String authorUsername;
    private Long postId;
    private Long parentId;
    private List<CommentDTO> replies;

    public CommentDTO() {}

    public CommentDTO(Long id, String content, LocalDateTime createdAt, 
                      String authorUsername, Long postId, Long parentId, List<CommentDTO> replies) {
        this.id = id;
        this.content = content;
        this.createdAt = createdAt;
        this.authorUsername = authorUsername;
        this.postId = postId;
        this.parentId = parentId;
        this.replies = replies;
    }

    public static CommentDTO fromEntity(Comment comment) {
        return new CommentDTO(
            comment.getId(),
            comment.getContent(),
            comment.getCreatedAt(),
            comment.getUser().getUsername(),
            comment.getPost().getId(),
            comment.getParent() != null ? comment.getParent().getId() : null,
            comment.getReplies().stream()
                .map(CommentDTO::fromEntity)
                .collect(Collectors.toList())
        );
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getAuthorUsername() { return authorUsername; }
    public void setAuthorUsername(String authorUsername) { this.authorUsername = authorUsername; }

    public Long getPostId() { return postId; }
    public void setPostId(Long postId) { this.postId = postId; }

    public Long getParentId() { return parentId; }
    public void setParentId(Long parentId) { this.parentId = parentId; }

    public List<CommentDTO> getReplies() { return replies; }
    public void setReplies(List<CommentDTO> replies) { this.replies = replies; }
}