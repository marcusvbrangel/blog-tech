package com.blog.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CreatePostDTO {
    @NotBlank(message = "Title is required")
    @Size(min = 5, max = 200, message = "Title must be between 5 and 200 characters")
    private String title;

    @NotBlank(message = "Content is required")
    @Size(min = 10, message = "Content must be at least 10 characters")
    private String content;

    private Long categoryId;
    private boolean published = false;

    public CreatePostDTO() {}

    public CreatePostDTO(String title, String content, Long categoryId, boolean published) {
        this.title = title;
        this.content = content;
        this.categoryId = categoryId;
        this.published = published;
    }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }

    public boolean isPublished() { return published; }
    public void setPublished(boolean published) { this.published = published; }
}