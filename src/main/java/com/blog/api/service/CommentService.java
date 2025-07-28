package com.blog.api.service;

import com.blog.api.dto.CommentDTO;
import com.blog.api.entity.Comment;
import com.blog.api.entity.Post;
import com.blog.api.entity.User;
import com.blog.api.exception.ResourceNotFoundException;
import com.blog.api.repository.CommentRepository;
import com.blog.api.repository.PostRepository;
import com.blog.api.repository.UserRepository;
import io.micrometer.core.annotation.Timed;
import io.micrometer.core.instrument.Counter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CommentService {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private Counter commentCreationCounter;

    @Cacheable(value = "comments", key = "'post:' + #postId + ':' + #pageable.pageNumber + ':' + #pageable.pageSize")
    public Page<CommentDTO> getCommentsByPost(Long postId, Pageable pageable) {
        return commentRepository.findByPostIdAndParentIsNull(postId, pageable)
                .map(CommentDTO::fromEntity);
    }

    @Cacheable(value = "comments", key = "'simple:' + #postId")
    public List<CommentDTO> getCommentsByPostSimple(Long postId) {
        return commentRepository.findByPostIdAndParentIsNullOrderByCreatedAtDesc(postId)
                .stream()
                .map(CommentDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Cacheable(value = "comments", key = "'single:' + #id")
    public CommentDTO getCommentById(Long id) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Comment", "id", id));
        return CommentDTO.fromEntity(comment);
    }

    @CacheEvict(value = "comments", allEntries = true)
    @Timed(value = "blog_api_comments_create", description = "Time taken to create a comment")
    public CommentDTO createComment(CommentDTO commentDTO, String username) {
        commentCreationCounter.increment();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));

        Post post = postRepository.findById(commentDTO.postId())
                .orElseThrow(() -> new ResourceNotFoundException("Post", "id", commentDTO.postId()));

        Comment parent = null;
        if (commentDTO.parentId() != null) {
            parent = commentRepository.findById(commentDTO.parentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Comment", "id", commentDTO.parentId()));
        }

        Comment comment = new Comment();
        comment.setContent(commentDTO.content());
        comment.setPost(post);
        comment.setUser(user);
        comment.setParent(parent);

        Comment savedComment = commentRepository.save(comment);
        return CommentDTO.fromEntity(savedComment);
    }

    @Caching(evict = {
            @CacheEvict(value = "comments", key = "'single:' + #id"),
            @CacheEvict(value = "comments", allEntries = true)
    })
    public CommentDTO updateComment(Long id, CommentDTO commentDTO, String username) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Comment", "id", id));

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));

        if (!comment.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("You can only update your own comments");
        }

        comment.setContent(commentDTO.content());

        Comment updatedComment = commentRepository.save(comment);
        return CommentDTO.fromEntity(updatedComment);
    }

    @Caching(evict = {
            @CacheEvict(value = "comments", key = "'single:' + #id"),
            @CacheEvict(value = "comments", allEntries = true)
    })
    public void deleteComment(Long id, String username) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Comment", "id", id));

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));

        if (!comment.getUser().getId().equals(user.getId()) && !user.getRole().equals(User.Role.ADMIN)) {
            throw new RuntimeException("You can only delete your own comments");
        }

        commentRepository.delete(comment);
    }
}