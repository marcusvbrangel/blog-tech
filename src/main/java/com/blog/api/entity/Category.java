package com.blog.api.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "categories")
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(min = 2, max = 50)
    @Column(unique = true)
    private String name;

    @Size(max = 255)
    private String description;

    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Post> posts = new ArrayList<>();

    // JPA required constructor
    public Category() {}

    // Legacy constructor - mantido para compatibilidade
    public Category(String name, String description) {
        this.name = name;
        this.description = description;
    }

    // Private constructor for Builder
    private Category(Builder builder) {
        this.name = builder.name;
        this.description = builder.description;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public List<Post> getPosts() { return posts; }
    public void setPosts(List<Post> posts) { this.posts = posts; }

    // Builder Pattern Implementation
    public static class Builder {
        private String name;
        private String description;

        public Builder name(String name) {
            Objects.requireNonNull(name, "Name cannot be null");
            if (name.trim().isEmpty()) {
                throw new IllegalArgumentException("Name cannot be empty");
            }
            if (name.length() < 2 || name.length() > 50) {
                throw new IllegalArgumentException("Name must be between 2 and 50 characters");
            }
            this.name = name.trim();
            return this;
        }

        public Builder description(String description) {
            if (description != null) {
                if (description.length() > 255) {
                    throw new IllegalArgumentException("Description must not exceed 255 characters");
                }
                this.description = description.trim().isEmpty() ? null : description.trim();
            }
            return this;
        }

        public Category build() {
            // Final validation of required fields
            Objects.requireNonNull(name, "Name is required");

            return new Category(this);
        }
    }

    // Factory Methods
    public static Builder newInstance() {
        return new Builder();
    }

    public static Builder from(Category other) {
        Objects.requireNonNull(other, "Category cannot be null");
        return new Builder()
                .name(other.getName())
                .description(other.getDescription());
    }

    public static Builder of(String name) {
        return new Builder()
                .name(name);
    }

    public static Builder of(String name, String description) {
        return new Builder()
                .name(name)
                .description(description);
    }

    public static Builder withName(String name) {
        return of(name);
    }

    public static Builder withDescription(String name, String description) {
        return of(name, description);
    }
}