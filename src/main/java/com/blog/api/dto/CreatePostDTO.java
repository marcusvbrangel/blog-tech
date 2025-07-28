package com.blog.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreatePostDTO(
    @NotBlank(message = "Title is required")
    @Size(min = 5, max = 200, message = "Title must be between 5 and 200 characters")
    String title,

    @NotBlank(message = "Content is required")
    @Size(min = 10, message = "Content must be at least 10 characters")
    String content,

    Long categoryId,
    boolean published
) {
    public CreatePostDTO(String title, String content, Long categoryId) {
        this(title, content, categoryId, false);
    }

    public CreatePostDTO(String title, String content) {
        this(title, content, null, false);
    }
}