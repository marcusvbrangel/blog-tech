package com.blog.api.controller;

import com.blog.api.dto.CommentDTO;
import com.blog.api.service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/comments")
@Tag(name = "Comments", description = "Comment management operations")
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @GetMapping("/post/{postId}")
    @Operation(summary = "Get comments by post")
    public ResponseEntity<Page<CommentDTO>> getCommentsByPost(@PathVariable Long postId, Pageable pageable) {
        Page<CommentDTO> comments = commentService.getCommentsByPost(postId, pageable);
        return ResponseEntity.ok(comments);
    }

    @GetMapping("/post/{postId}/simple")
    @Operation(summary = "Get comments by post (simple list)")
    public ResponseEntity<List<CommentDTO>> getCommentsByPostSimple(@PathVariable Long postId) {
        List<CommentDTO> comments = commentService.getCommentsByPostSimple(postId);
        return ResponseEntity.ok(comments);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get comment by ID")
    public ResponseEntity<CommentDTO> getCommentById(@PathVariable Long id) {
        CommentDTO comment = commentService.getCommentById(id);
        return ResponseEntity.ok(comment);
    }

    @PostMapping
    @Operation(summary = "Create new comment")
    public ResponseEntity<CommentDTO> createComment(@Valid @RequestBody CommentDTO commentDTO) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication != null ? authentication.getName() : "anonymous";
        CommentDTO comment = commentService.createComment(commentDTO, username);
        return new ResponseEntity<>(comment, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update comment")
    public ResponseEntity<CommentDTO> updateComment(@PathVariable Long id, 
                                                    @Valid @RequestBody CommentDTO commentDTO) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication != null ? authentication.getName() : "anonymous";
        CommentDTO comment = commentService.updateComment(id, commentDTO, username);
        return ResponseEntity.ok(comment);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete comment")
    public ResponseEntity<Void> deleteComment(@PathVariable Long id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication != null ? authentication.getName() : "anonymous";
        commentService.deleteComment(id, username);
        return ResponseEntity.noContent().build();
    }
}