package com.blog.api.dto;

import com.blog.api.entity.Comment;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
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
) implements Serializable {
    public static CommentDTO fromEntity(Comment comment) {
        try {
            List<CommentDTO> replies = new ArrayList<>();
            try {
                if (comment.getReplies() != null) {
                    replies = comment.getReplies().stream()
                        .map(CommentDTO::fromEntity)
                        .collect(Collectors.toList());
                }
            } catch (Exception e) {
                // Handle lazy loading exception by setting empty replies
                replies = new ArrayList<>();
            }
            
            return new CommentDTO(
                comment.getId(),
                comment.getContent(),
                comment.getCreatedAt(),
                comment.getUser().getUsername(),
                comment.getPost().getId(),
                comment.getParent() != null ? comment.getParent().getId() : null,
                replies
            );
        } catch (Exception e) {
            System.err.println("ERROR in CommentDTO.fromEntity: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
}