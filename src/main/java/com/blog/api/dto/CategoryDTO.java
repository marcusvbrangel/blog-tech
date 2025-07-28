package com.blog.api.dto;

import com.blog.api.entity.Category;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CategoryDTO(
    Long id,

    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 50, message = "Name must be between 2 and 50 characters")
    String name,

    @Size(max = 255, message = "Description must be at most 255 characters")
    String description,

    int postCount
) {
    public static CategoryDTO fromEntity(Category category) {
        return new CategoryDTO(
            category.getId(),
            category.getName(),
            category.getDescription(),
            category.getPosts().size()
        );
    }
}