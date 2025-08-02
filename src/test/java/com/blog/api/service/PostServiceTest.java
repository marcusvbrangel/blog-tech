package com.blog.api.service;

import com.blog.api.dto.CreatePostDTO;
import com.blog.api.dto.PostDTO;
import com.blog.api.entity.Category;
import com.blog.api.entity.Post;
import com.blog.api.entity.User;
import com.blog.api.exception.ResourceNotFoundException;
import com.blog.api.repository.CategoryRepository;
import com.blog.api.repository.PostRepository;
import com.blog.api.repository.UserRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Timer;
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
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @Mock
    private PostRepository postRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private Counter postCreationCounter;

    @Mock
    private Timer databaseQueryTimer;

    @InjectMocks
    private PostService postService;

    private User testUser;
    private Category testCategory;
    private Post testPost;
    private CreatePostDTO createPostDTO;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        testUser = User.of("testuser", "test@example.com", "ValidPassword123!")
                .role(User.Role.USER)
                .emailVerified(true)
                .build();
        testUser.setId(1L);
        testUser.setCreatedAt(LocalDateTime.now());

        testCategory = Category.of("Technology", "Tech posts")
                .build();
        testCategory.setId(1L);

        testPost = Post.of("Test Post", "Test content", testUser)
                .published(true)
                .category(testCategory)
                .build();
        testPost.setId(1L);
        testPost.setCreatedAt(LocalDateTime.now());
        testPost.setUpdatedAt(LocalDateTime.now());

        createPostDTO = new CreatePostDTO("Test Post", "Test content", 1L, true);
        pageable = PageRequest.of(0, 10);
    }

    @Test
    void getAllPublishedPosts_ShouldReturnPageOfPostDTOs() {
        // Arrange
        Page<Post> postPage = new PageImpl<>(Arrays.asList(testPost), pageable, 1);
        when(postRepository.findByPublishedTrue(pageable)).thenReturn(postPage);

        // Act
        Page<PostDTO> result = postService.getAllPublishedPosts(pageable);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).title()).isEqualTo("Test Post");
        assertThat(result.getContent().get(0).content()).isEqualTo("Test content");
        verify(postRepository).findByPublishedTrue(pageable);
    }

    @Test
    void getPostsByCategory_ShouldReturnPageOfPostDTOs() {
        // Arrange
        Long categoryId = 1L;
        Page<Post> postPage = new PageImpl<>(Arrays.asList(testPost), pageable, 1);
        when(postRepository.findByCategoryId(categoryId, pageable)).thenReturn(postPage);

        // Act
        Page<PostDTO> result = postService.getPostsByCategory(categoryId, pageable);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).title()).isEqualTo("Test Post");
        verify(postRepository).findByCategoryId(categoryId, pageable);
    }

    @Test
    void getPostById_ShouldReturnPostDTO_WhenPostExists() {
        // Arrange
        Long postId = 1L;
        when(postRepository.findById(postId)).thenReturn(Optional.of(testPost));

        // Act
        PostDTO result = postService.getPostById(postId);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(postId);
        assertThat(result.title()).isEqualTo("Test Post");
        verify(postRepository).findById(postId);
    }

    @Test
    void getPostById_ShouldThrowResourceNotFoundException_WhenPostNotExists() {
        // Arrange
        Long postId = 999L;
        when(postRepository.findById(postId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> postService.getPostById(postId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Post")
                .hasMessageContaining("id")
                .hasMessageContaining("999");
        
        verify(postRepository).findById(postId);
    }

    @Test
    void createPost_ShouldCreateAndReturnPostDTO_WhenValidData() {
        // Arrange
        String username = "testuser";
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(testUser));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        when(postRepository.save(any(Post.class))).thenReturn(testPost);

        // Act
        PostDTO result = postService.createPost(createPostDTO, username);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.title()).isEqualTo("Test Post");
        assertThat(result.content()).isEqualTo("Test content");
        verify(postCreationCounter).increment();
        verify(userRepository).findByUsername(username);
        verify(categoryRepository).findById(1L);
        verify(postRepository).save(any(Post.class));
    }

    @Test
    void createPost_ShouldThrowResourceNotFoundException_WhenUserNotExists() {
        // Arrange
        String username = "nonexistentuser";
        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> postService.createPost(createPostDTO, username))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User")
                .hasMessageContaining("username")
                .hasMessageContaining("nonexistentuser");
        
        verify(userRepository).findByUsername(username);
        verify(postRepository, never()).save(any());
    }

    @Test
    void deletePost_ShouldDeletePost_WhenUserIsOwner() {
        // Arrange
        Long postId = 1L;
        String username = "testuser";
        when(postRepository.findById(postId)).thenReturn(Optional.of(testPost));
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(testUser));

        // Act
        postService.deletePost(postId, username);

        // Assert
        verify(postRepository).findById(postId);
        verify(userRepository).findByUsername(username);
        verify(postRepository).delete(testPost);
    }

    @Test
    void deletePost_ShouldDeletePost_WhenUserIsAdmin() {
        // Arrange
        Long postId = 1L;
        String username = "admin";
        User adminUser = User.of("admin", "admin@example.com", "ValidPassword123!")
                .role(User.Role.ADMIN)
                .build();
        adminUser.setId(2L);
        
        when(postRepository.findById(postId)).thenReturn(Optional.of(testPost));
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(adminUser));

        // Act
        postService.deletePost(postId, username);

        // Assert
        verify(postRepository).findById(postId);
        verify(userRepository).findByUsername(username);
        verify(postRepository).delete(testPost);
    }

    @Test
    void deletePost_ShouldThrowException_WhenUserNotOwnerAndNotAdmin() {
        // Arrange
        Long postId = 1L;
        String username = "differentuser";
        User differentUser = User.of("differentuser", "different@example.com", "ValidPassword123!")
                .role(User.Role.USER)
                .build();
        differentUser.setId(2L);
        
        when(postRepository.findById(postId)).thenReturn(Optional.of(testPost));
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(differentUser));

        // Act & Assert
        assertThatThrownBy(() -> postService.deletePost(postId, username))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("You can only delete your own posts");
        
        verify(postRepository).findById(postId);
        verify(userRepository).findByUsername(username);
        verify(postRepository, never()).delete(any());
    }
}