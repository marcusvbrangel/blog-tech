package com.blog.api.dto;

import com.blog.api.entity.Post;

import java.time.LocalDateTime;
import java.io.Serializable;

public record PostDTO(
    Long id,
    String title,
    String content,
    boolean published,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    String authorUsername,
    String categoryName,
    int commentCount
) implements Serializable {
    
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
}