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
import org.junit.jupiter.api.DisplayName;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Comment Service Tests")
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
    @DisplayName("Deve retornar página de comentários por post")
    void getCommentsByPost_ShouldReturnPageOfComments() {
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
    @DisplayName("Deve retornar lista simples de comentários por post")
    void getCommentsByPostSimple_ShouldReturnListOfComments() {
        // Arrange
        Long postId = 1L;
        when(commentRepository.findByPostIdAndParentIsNullOrderByCreatedAtDesc(postId)).thenReturn(Arrays.asList(testComment));

        // Act
        List<CommentDTO> result = commentService.getCommentsByPostSimple(postId);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).content()).isEqualTo("Test comment");
        verify(commentRepository).findByPostIdAndParentIsNullOrderByCreatedAtDesc(postId);
    }

    @Test
    @DisplayName("Deve retornar CommentDTO quando buscar comentário por ID existente")
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
    @DisplayName("Deve lançar ResourceNotFoundException quando buscar comentário por ID inexistente")
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
    @DisplayName("Deve criar e retornar CommentDTO quando dados são válidos")
    void createComment_ShouldCreateAndReturnCommentDTO_WhenValidData() {
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
    @DisplayName("Deve atualizar e retornar CommentDTO quando dados são válidos")
    void updateComment_ShouldUpdateAndReturnCommentDTO_WhenValidData() {
        // Arrange
        String username = "testuser";
        when(commentRepository.findById(1L)).thenReturn(Optional.of(testComment));
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(testUser));
        when(commentRepository.save(any(Comment.class))).thenReturn(testComment);

        // Act
        CommentDTO result = commentService.updateComment(1L, commentDTO, username);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.content()).isEqualTo("Test comment");
        verify(commentRepository).findById(1L);
        verify(userRepository).findByUsername(username);
        verify(commentRepository).save(any(Comment.class));
    }

    @Test
    @DisplayName("Deve deletar comentário quando ele existe")
    void deleteComment_ShouldDeleteComment_WhenCommentExists() {
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
    @DisplayName("Deve permitir apenas autor deletar seu próprio comentário")
    void deleteComment_ShouldOnlyAllowAuthorToDelete() {
        // Arrange
        Long commentId = 1L;
        String username = "anotheruser";
        User anotherUser = User.of("anotheruser", "another@example.com", "ValidPassword123!")
                .role(User.Role.USER)
                .emailVerified(true)
                .build();
        anotherUser.setId(2L);
        anotherUser.setCreatedAt(LocalDateTime.now());

        when(commentRepository.findById(commentId)).thenReturn(Optional.of(testComment));
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(anotherUser));

        // Act & Assert
        assertThatThrownBy(() -> commentService.deleteComment(commentId, username))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Acesso negado");

        verify(commentRepository).findById(commentId);
        verify(userRepository).findByUsername(username);
        verify(commentRepository, never()).delete(any(Comment.class));
    }

    // Método getCommentsWithReplies removido pois não existe no CommentService real
    // O CommentService usa getCommentsByPost() e getCommentsByPostSimple()
}