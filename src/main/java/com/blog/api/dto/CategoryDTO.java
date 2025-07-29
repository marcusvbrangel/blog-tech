package com.blog.api.dto;

import com.blog.api.entity.Category;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.io.Serializable;

public record CategoryDTO(
    Long id,

    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 50, message = "Name must be between 2 and 50 characters")
    String name,

    @Size(max = 255, message = "Description must be at most 255 characters")
    String description,

    int postCount
) implements Serializable {
    public static CategoryDTO fromEntity(Category category) {
        try {
            int postCount = 0;
            try {
                postCount = category.getPosts() != null ? category.getPosts().size() : 0;
            } catch (Exception e) {
                // Handle lazy loading exception by setting post count to 0
                postCount = 0;
            }
            
            return new CategoryDTO(
                category.getId(),
                category.getName(),
                category.getDescription(),
                postCount
            );
        } catch (Exception e) {
            System.err.println("ERROR in CategoryDTO.fromEntity: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
}