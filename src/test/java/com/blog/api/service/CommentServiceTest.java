package com.blog.api.service;

import com.blog.api.dto.CommentDTO;
import com.blog.api.entity.Comment;
import com.blog.api.entity.Post;
import com.blog.api.entity.User;
import com.blog.api.exception.ResourceNotFoundException;
import com.blog.api.repository.CommentRepository;
import com.blog.api.repository.PostRepository;
import com.blog.api.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private PostRepository postRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CommentService commentService;

    private User testUser;
    private Post testPost;
    private Comment testComment;
    private Comment parentComment;
    private CommentDTO commentDTO;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setRole(User.Role.USER);

        testPost = new Post();
        testPost.setId(1L);
        testPost.setTitle("Test Post");
        testPost.setContent("Test content");

        parentComment = new Comment();
        parentComment.setId(1L);
        parentComment.setContent("Parent comment");
        parentComment.setUser(testUser);
        parentComment.setPost(testPost);
        parentComment.setCreatedAt(LocalDateTime.now());
        parentComment.setReplies(new ArrayList<>());

        testComment = new Comment();
        testComment.setId(2L);
        testComment.setContent("Test comment");
        testComment.setUser(testUser);
        testComment.setPost(testPost);
        testComment.setParent(null);
        testComment.setCreatedAt(LocalDateTime.now());
        testComment.setReplies(new ArrayList<>());

        commentDTO = new CommentDTO();
        commentDTO.setContent("New comment");
        commentDTO.setPostId(1L);
        commentDTO.setParentId(null);

        pageable = PageRequest.of(0, 10);
    }

    @Test
    void getCommentsByPost_ShouldReturnPageOfCommentDTO() {
        Page<Comment> commentPage = new PageImpl<>(Arrays.asList(testComment));
        when(commentRepository.findByPostIdAndParentIsNull(1L, pageable)).thenReturn(commentPage);

        Page<CommentDTO> result = commentService.getCommentsByPost(1L, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("Test comment", result.getContent().get(0).getContent());
        verify(commentRepository).findByPostIdAndParentIsNull(1L, pageable);
    }

    @Test
    void getCommentsByPostSimple_ShouldReturnListOfCommentDTO() {
        List<Comment> comments = Arrays.asList(testComment);
        when(commentRepository.findByPostIdAndParentIsNullOrderByCreatedAtDesc(1L)).thenReturn(comments);

        List<CommentDTO> result = commentService.getCommentsByPostSimple(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Test comment", result.get(0).getContent());
        verify(commentRepository).findByPostIdAndParentIsNullOrderByCreatedAtDesc(1L);
    }

    @Test
    void getCommentById_WhenCommentExists_ShouldReturnCommentDTO() {
        when(commentRepository.findById(2L)).thenReturn(Optional.of(testComment));

        CommentDTO result = commentService.getCommentById(2L);

        assertNotNull(result);
        assertEquals(testComment.getId(), result.getId());
        assertEquals(testComment.getContent(), result.getContent());
        assertEquals(testUser.getUsername(), result.getAuthorUsername());
    }

    @Test
    void getCommentById_WhenCommentNotExists_ShouldThrowResourceNotFoundException() {
        when(commentRepository.findById(2L)).thenReturn(Optional.empty());

        assertThrows(
            ResourceNotFoundException.class,
            () -> commentService.getCommentById(2L)
        );
    }

    @Test
    void createComment_WhenValidData_ShouldReturnCommentDTO() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(postRepository.findById(1L)).thenReturn(Optional.of(testPost));
        when(commentRepository.save(any(Comment.class))).thenReturn(testComment);

        CommentDTO result = commentService.createComment(commentDTO, "testuser");

        assertNotNull(result);
        assertEquals(testComment.getContent(), result.getContent());
        verify(commentRepository).save(any(Comment.class));
    }

    @Test
    void createComment_WithParent_ShouldCreateReply() {
        commentDTO.setParentId(1L);
        
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(postRepository.findById(1L)).thenReturn(Optional.of(testPost));
        when(commentRepository.findById(1L)).thenReturn(Optional.of(parentComment));
        
        Comment replyComment = new Comment();
        replyComment.setId(3L);
        replyComment.setContent("New comment");
        replyComment.setUser(testUser);
        replyComment.setPost(testPost);
        replyComment.setParent(parentComment);
        replyComment.setReplies(new ArrayList<>());
        
        when(commentRepository.save(any(Comment.class))).thenReturn(replyComment);

        CommentDTO result = commentService.createComment(commentDTO, "testuser");

        assertNotNull(result);
        assertEquals(1L, result.getParentId());
        verify(commentRepository).save(any(Comment.class));
    }

    @Test
    void createComment_WhenUserNotFound_ShouldThrowResourceNotFoundException() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.empty());

        assertThrows(
            ResourceNotFoundException.class,
            () -> commentService.createComment(commentDTO, "testuser")
        );

        verify(commentRepository, never()).save(any(Comment.class));
    }

    @Test
    void createComment_WhenPostNotFound_ShouldThrowResourceNotFoundException() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(postRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(
            ResourceNotFoundException.class,
            () -> commentService.createComment(commentDTO, "testuser")
        );

        verify(commentRepository, never()).save(any(Comment.class));
    }

    @Test
    void createComment_WhenParentNotFound_ShouldThrowResourceNotFoundException() {
        commentDTO.setParentId(999L);
        
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(postRepository.findById(1L)).thenReturn(Optional.of(testPost));
        when(commentRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(
            ResourceNotFoundException.class,
            () -> commentService.createComment(commentDTO, "testuser")
        );

        verify(commentRepository, never()).save(any(Comment.class));
    }

    @Test
    void updateComment_WhenValidData_ShouldReturnUpdatedCommentDTO() {
        CommentDTO updateDTO = new CommentDTO();
        updateDTO.setContent("Updated comment content");

        when(commentRepository.findById(2L)).thenReturn(Optional.of(testComment));
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        
        Comment updatedComment = new Comment();
        updatedComment.setId(2L);
        updatedComment.setContent("Updated comment content");
        updatedComment.setUser(testUser);
        updatedComment.setPost(testPost);
        updatedComment.setReplies(new ArrayList<>());
        
        when(commentRepository.save(any(Comment.class))).thenReturn(updatedComment);

        CommentDTO result = commentService.updateComment(2L, updateDTO, "testuser");

        assertNotNull(result);
        assertEquals("Updated comment content", result.getContent());
        verify(commentRepository).save(any(Comment.class));
    }

    @Test
    void updateComment_WhenNotOwner_ShouldThrowRuntimeException() {
        User differentUser = new User();
        differentUser.setId(2L);
        differentUser.setUsername("differentuser");

        CommentDTO updateDTO = new CommentDTO();
        updateDTO.setContent("Updated content");

        when(commentRepository.findById(2L)).thenReturn(Optional.of(testComment));
        when(userRepository.findByUsername("differentuser")).thenReturn(Optional.of(differentUser));

        assertThrows(
            RuntimeException.class,
            () -> commentService.updateComment(2L, updateDTO, "differentuser")
        );

        verify(commentRepository, never()).save(any(Comment.class));
    }

    @Test
    void deleteComment_WhenOwner_ShouldDeleteComment() {
        when(commentRepository.findById(2L)).thenReturn(Optional.of(testComment));
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        assertDoesNotThrow(() -> commentService.deleteComment(2L, "testuser"));

        verify(commentRepository).delete(testComment);
    }

    @Test
    void deleteComment_WhenAdmin_ShouldDeleteComment() {
        User adminUser = new User();
        adminUser.setId(2L);
        adminUser.setUsername("admin");
        adminUser.setRole(User.Role.ADMIN);

        when(commentRepository.findById(2L)).thenReturn(Optional.of(testComment));
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(adminUser));

        assertDoesNotThrow(() -> commentService.deleteComment(2L, "admin"));

        verify(commentRepository).delete(testComment);
    }

    @Test
    void deleteComment_WhenNotOwnerAndNotAdmin_ShouldThrowRuntimeException() {
        User regularUser = new User();
        regularUser.setId(2L);
        regularUser.setUsername("regularuser");
        regularUser.setRole(User.Role.USER);

        when(commentRepository.findById(2L)).thenReturn(Optional.of(testComment));
        when(userRepository.findByUsername("regularuser")).thenReturn(Optional.of(regularUser));

        assertThrows(
            RuntimeException.class,
            () -> commentService.deleteComment(2L, "regularuser")
        );

        verify(commentRepository, never()).delete(any(Comment.class));
    }
}