package com.blog.api.service;

import com.blog.api.dto.CommentDTO;
import com.blog.api.entity.Comment;
import com.blog.api.entity.Post;
import com.blog.api.entity.User;
import com.blog.api.exception.ResourceNotFoundException;
import com.blog.api.repository.CommentRepository;
import com.blog.api.repository.PostRepository;
import com.blog.api.repository.UserRepository;
import io.micrometer.core.instrument.Counter;
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
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private PostRepository postRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private Counter commentCreationCounter;

    @InjectMocks
    private CommentService commentService;

    private User testUser;
    private Post testPost;
    private Comment testComment;
    private Comment testReply;
    private CommentDTO commentDTO;
    private CommentDTO replyDTO;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        testUser = User.of("testuser", "test@example.com", "ValidPassword123!")
                .role(User.Role.USER)
                .emailVerified(true)
                .build();
        testUser.setId(1L);
        testUser.setCreatedAt(LocalDateTime.now());

        testPost = Post.of("Test Post", "Test content", testUser)
                .published(true)
                .build();
        testPost.setId(1L);
        testPost.setCreatedAt(LocalDateTime.now());

        testComment = Comment.comment("Test comment", testPost, testUser)
                .build();
        testComment.setId(1L);
        testComment.setCreatedAt(LocalDateTime.now());

        testReply = Comment.reply("Test reply", testComment, testUser)
                .build();
        testReply.setId(2L);
        testReply.setCreatedAt(LocalDateTime.now());

        commentDTO = new CommentDTO(null, "Test comment", null, null, 1L, null, null);
        replyDTO = new CommentDTO(null, "Test reply", null, null, 1L, 1L, null);
        pageable = PageRequest.of(0, 10);
    }

    @Test
    void getCommentsByPost_ShouldReturnPageOfCommentDTOs() {
        // Arrange
        Long postId = 1L;
        Page<Comment> commentPage = new PageImpl<>(Arrays.asList(testComment), pageable, 1);
        when(commentRepository.findByPostIdAndParentIsNull(postId, pageable)).thenReturn(commentPage);

        // Act
        Page<CommentDTO> result = commentService.getCommentsByPost(postId, pageable);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).content()).isEqualTo("Test comment");
        verify(commentRepository).findByPostIdAndParentIsNull(postId, pageable);
    }

    @Test
    void getCommentById_ShouldReturnCommentDTO_WhenCommentExists() {
        // Arrange
        Long commentId = 1L;
        when(commentRepository.findById(commentId)).thenReturn(Optional.of(testComment));

        // Act
        CommentDTO result = commentService.getCommentById(commentId);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(commentId);
        assertThat(result.content()).isEqualTo("Test comment");
        verify(commentRepository).findById(commentId);
    }

    @Test
    void getCommentById_ShouldThrowResourceNotFoundException_WhenCommentNotExists() {
        // Arrange
        Long commentId = 999L;
        when(commentRepository.findById(commentId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> commentService.getCommentById(commentId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Comment")
                .hasMessageContaining("id")
                .hasMessageContaining("999");
        
        verify(commentRepository).findById(commentId);
    }

    @Test
    void createComment_ShouldCreateTopLevelComment_WhenNoParent() {
        // Arrange
        String username = "testuser";
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(testUser));
        when(postRepository.findById(1L)).thenReturn(Optional.of(testPost));
        when(commentRepository.save(any(Comment.class))).thenReturn(testComment);

        // Act
        CommentDTO result = commentService.createComment(commentDTO, username);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.content()).isEqualTo("Test comment");
        verify(commentCreationCounter).increment();
        verify(userRepository).findByUsername(username);
        verify(postRepository).findById(1L);
        verify(commentRepository).save(any(Comment.class));
    }

    @Test
    void createComment_ShouldCreateReply_WhenParentExists() {
        // Arrange
        String username = "testuser";
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(testUser));
        when(postRepository.findById(1L)).thenReturn(Optional.of(testPost));
        when(commentRepository.findById(1L)).thenReturn(Optional.of(testComment));
        when(commentRepository.save(any(Comment.class))).thenReturn(testReply);

        // Act
        CommentDTO result = commentService.createComment(replyDTO, username);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.content()).isEqualTo("Test reply");
        verify(commentCreationCounter).increment();
        verify(userRepository).findByUsername(username);
        verify(postRepository).findById(1L);
        verify(commentRepository).findById(1L);
        verify(commentRepository).save(any(Comment.class));
    }

    @Test
    void deleteComment_ShouldDeleteComment_WhenUserIsOwner() {
        // Arrange
        Long commentId = 1L;
        String username = "testuser";
        when(commentRepository.findById(commentId)).thenReturn(Optional.of(testComment));
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(testUser));

        // Act
        commentService.deleteComment(commentId, username);

        // Assert
        verify(commentRepository).findById(commentId);
        verify(userRepository).findByUsername(username);
        verify(commentRepository).delete(testComment);
    }

    @Test
    void deleteComment_ShouldDeleteComment_WhenUserIsAdmin() {
        // Arrange
        Long commentId = 1L;
        String username = "admin";
        User adminUser = User.of("admin", "admin@example.com", "ValidPassword123!")
                .role(User.Role.ADMIN)
                .build();
        adminUser.setId(2L);
        
        when(commentRepository.findById(commentId)).thenReturn(Optional.of(testComment));
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(adminUser));

        // Act
        commentService.deleteComment(commentId, username);

        // Assert
        verify(commentRepository).findById(commentId);
        verify(userRepository).findByUsername(username);
        verify(commentRepository).delete(testComment);
    }
}