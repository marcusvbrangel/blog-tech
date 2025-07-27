package com.blog.api.controller;

import com.blog.api.dto.CreatePostDTO;
import com.blog.api.dto.PostDTO;
import com.blog.api.service.PostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/posts")
@Tag(name = "Posts", description = "Post management operations")
public class PostController {

    @Autowired
    private PostService postService;

    @GetMapping
    @Operation(summary = "Get all published posts")
    public ResponseEntity<Page<PostDTO>> getAllPublishedPosts(Pageable pageable) {
        Page<PostDTO> posts = postService.getAllPublishedPosts(pageable);
        return ResponseEntity.ok(posts);
    }

    @GetMapping("/search")
    @Operation(summary = "Search posts by keyword")
    public ResponseEntity<Page<PostDTO>> searchPosts(@RequestParam String keyword, Pageable pageable) {
        Page<PostDTO> posts = postService.searchPosts(keyword, pageable);
        return ResponseEntity.ok(posts);
    }

    @GetMapping("/category/{categoryId}")
    @Operation(summary = "Get posts by category")
    public ResponseEntity<Page<PostDTO>> getPostsByCategory(@PathVariable Long categoryId, Pageable pageable) {
        Page<PostDTO> posts = postService.getPostsByCategory(categoryId, pageable);
        return ResponseEntity.ok(posts);
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get posts by user")
    public ResponseEntity<Page<PostDTO>> getPostsByUser(@PathVariable Long userId, Pageable pageable) {
        Page<PostDTO> posts = postService.getPostsByUser(userId, pageable);
        return ResponseEntity.ok(posts);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get post by ID")
    public ResponseEntity<PostDTO> getPostById(@PathVariable Long id) {
        PostDTO post = postService.getPostById(id);
        return ResponseEntity.ok(post);
    }

    @PostMapping
    @Operation(summary = "Create new post")
    @SecurityRequirement(name = "bearer-token")
    @PreAuthorize("hasRole('AUTHOR') or hasRole('ADMIN')")
    public ResponseEntity<PostDTO> createPost(@Valid @RequestBody CreatePostDTO createPostDTO, 
                                              Authentication authentication) {
        PostDTO post = postService.createPost(createPostDTO, authentication.getName());
        return new ResponseEntity<>(post, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update post")
    @SecurityRequirement(name = "bearer-token")
    @PreAuthorize("hasRole('AUTHOR') or hasRole('ADMIN')")
    public ResponseEntity<PostDTO> updatePost(@PathVariable Long id, 
                                              @Valid @RequestBody CreatePostDTO createPostDTO,
                                              Authentication authentication) {
        PostDTO post = postService.updatePost(id, createPostDTO, authentication.getName());
        return ResponseEntity.ok(post);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete post")
    @SecurityRequirement(name = "bearer-token")
    @PreAuthorize("hasRole('AUTHOR') or hasRole('ADMIN')")
    public ResponseEntity<Void> deletePost(@PathVariable Long id, Authentication authentication) {
        postService.deletePost(id, authentication.getName());
        return ResponseEntity.noContent().build();
    }
}