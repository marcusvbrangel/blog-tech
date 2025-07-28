package com.blog.api.dto;

import com.blog.api.entity.Comment;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public record CommentDTO(
    Long id,

    @NotBlank(message = "Content is required")
    @Size(min = 1, max = 1000, message = "Content must be between 1 and 1000 characters")
    String content,

    LocalDateTime createdAt,
    String authorUsername,
    Long postId,
    Long parentId,
    List<CommentDTO> replies
) {
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
}