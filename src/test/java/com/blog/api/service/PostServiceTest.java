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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @Mock
    private PostRepository postRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private PostService postService;

    private User testUser;
    private Category testCategory;
    private Post testPost;
    private CreatePostDTO createPostDTO;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setRole(User.Role.AUTHOR);

        testCategory = new Category();
        testCategory.setId(1L);
        testCategory.setName("Technology");
        testCategory.setDescription("Tech posts");

        testPost = new Post();
        testPost.setId(1L);
        testPost.setTitle("Test Post");
        testPost.setContent("This is a test post content");
        testPost.setPublished(true);
        testPost.setUser(testUser);
        testPost.setCategory(testCategory);
        testPost.setCreatedAt(LocalDateTime.now());
        testPost.setUpdatedAt(LocalDateTime.now());
        testPost.setComments(new ArrayList<>());

        createPostDTO = new CreatePostDTO();
        createPostDTO.setTitle("New Post");
        createPostDTO.setContent("New post content");
        createPostDTO.setCategoryId(1L);
        createPostDTO.setPublished(true);

        pageable = PageRequest.of(0, 10);
    }

    @Test
    void getAllPublishedPosts_ShouldReturnPageOfPostDTO() {
        Page<Post> postPage = new PageImpl<>(Arrays.asList(testPost));
        when(postRepository.findByPublishedTrue(pageable)).thenReturn(postPage);

        Page<PostDTO> result = postService.getAllPublishedPosts(pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("Test Post", result.getContent().get(0).getTitle());
        verify(postRepository).findByPublishedTrue(pageable);
    }

    @Test
    void getPostById_WhenPostExists_ShouldReturnPostDTO() {
        when(postRepository.findById(1L)).thenReturn(Optional.of(testPost));

        PostDTO result = postService.getPostById(1L);

        assertNotNull(result);
        assertEquals(testPost.getId(), result.getId());
        assertEquals(testPost.getTitle(), result.getTitle());
        assertEquals(testPost.getContent(), result.getContent());
        assertEquals(testUser.getUsername(), result.getAuthorUsername());
    }

    @Test
    void getPostById_WhenPostNotExists_ShouldThrowResourceNotFoundException() {
        when(postRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> postService.getPostById(1L));
    }

    @Test
    void createPost_WhenValidData_ShouldReturnPostDTO() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        when(postRepository.save(any(Post.class))).thenReturn(testPost);

        PostDTO result = postService.createPost(createPostDTO, "testuser");

        assertNotNull(result);
        assertEquals(testPost.getTitle(), result.getTitle());
        assertEquals(testPost.getContent(), result.getContent());
        verify(postRepository).save(any(Post.class));
    }

    @Test
    void createPost_WhenUserNotFound_ShouldThrowResourceNotFoundException() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.empty());

        assertThrows(
            ResourceNotFoundException.class,
            () -> postService.createPost(createPostDTO, "testuser")
        );

        verify(postRepository, never()).save(any(Post.class));
    }

    @Test
    void createPost_WhenCategoryNotFound_ShouldThrowResourceNotFoundException() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(categoryRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(
            ResourceNotFoundException.class,
            () -> postService.createPost(createPostDTO, "testuser")
        );

        verify(postRepository, never()).save(any(Post.class));
    }

    @Test
    void createPost_WithoutCategory_ShouldCreatePost() {
        createPostDTO.setCategoryId(null);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        
        Post postWithoutCategory = new Post();
        postWithoutCategory.setId(1L);
        postWithoutCategory.setTitle("New Post");
        postWithoutCategory.setContent("New post content");
        postWithoutCategory.setUser(testUser);
        postWithoutCategory.setCategory(null);
        postWithoutCategory.setComments(new ArrayList<>());
        
        when(postRepository.save(any(Post.class))).thenReturn(postWithoutCategory);

        PostDTO result = postService.createPost(createPostDTO, "testuser");

        assertNotNull(result);
        assertNull(result.getCategoryName());
        verify(postRepository).save(any(Post.class));
        verify(categoryRepository, never()).findById(anyLong());
    }

    @Test
    void updatePost_WhenValidData_ShouldReturnUpdatedPostDTO() {
        when(postRepository.findById(1L)).thenReturn(Optional.of(testPost));
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        when(postRepository.save(any(Post.class))).thenReturn(testPost);

        PostDTO result = postService.updatePost(1L, createPostDTO, "testuser");

        assertNotNull(result);
        verify(postRepository).save(any(Post.class));
    }

    @Test
    void updatePost_WhenNotOwner_ShouldThrowRuntimeException() {
        User differentUser = new User();
        differentUser.setId(2L);
        differentUser.setUsername("differentuser");

        when(postRepository.findById(1L)).thenReturn(Optional.of(testPost));
        when(userRepository.findByUsername("differentuser")).thenReturn(Optional.of(differentUser));

        assertThrows(
            RuntimeException.class,
            () -> postService.updatePost(1L, createPostDTO, "differentuser")
        );

        verify(postRepository, never()).save(any(Post.class));
    }

    @Test
    void deletePost_WhenOwner_ShouldDeletePost() {
        when(postRepository.findById(1L)).thenReturn(Optional.of(testPost));
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        assertDoesNotThrow(() -> postService.deletePost(1L, "testuser"));

        verify(postRepository).delete(testPost);
    }

    @Test
    void deletePost_WhenAdmin_ShouldDeletePost() {
        User adminUser = new User();
        adminUser.setId(2L);
        adminUser.setUsername("admin");
        adminUser.setRole(User.Role.ADMIN);

        when(postRepository.findById(1L)).thenReturn(Optional.of(testPost));
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(adminUser));

        assertDoesNotThrow(() -> postService.deletePost(1L, "admin"));

        verify(postRepository).delete(testPost);
    }

    @Test
    void deletePost_WhenNotOwnerAndNotAdmin_ShouldThrowRuntimeException() {
        User regularUser = new User();
        regularUser.setId(2L);
        regularUser.setUsername("regularuser");
        regularUser.setRole(User.Role.USER);

        when(postRepository.findById(1L)).thenReturn(Optional.of(testPost));
        when(userRepository.findByUsername("regularuser")).thenReturn(Optional.of(regularUser));

        assertThrows(
            RuntimeException.class,
            () -> postService.deletePost(1L, "regularuser")
        );

        verify(postRepository, never()).delete(any(Post.class));
    }

    @Test
    void searchPosts_ShouldReturnMatchingPosts() {
        Page<Post> postPage = new PageImpl<>(Arrays.asList(testPost));
        when(postRepository.findPublishedPostsByKeyword("test", pageable)).thenReturn(postPage);

        Page<PostDTO> result = postService.searchPosts("test", pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(postRepository).findPublishedPostsByKeyword("test", pageable);
    }

    @Test
    void getPostsByCategory_ShouldReturnPostsFromCategory() {
        Page<Post> postPage = new PageImpl<>(Arrays.asList(testPost));
        when(postRepository.findByCategoryId(1L, pageable)).thenReturn(postPage);

        Page<PostDTO> result = postService.getPostsByCategory(1L, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(postRepository).findByCategoryId(1L, pageable);
    }

    @Test
    void getPostsByUser_ShouldReturnPostsFromUser() {
        Page<Post> postPage = new PageImpl<>(Arrays.asList(testPost));
        when(postRepository.findByUserId(1L, pageable)).thenReturn(postPage);

        Page<PostDTO> result = postService.getPostsByUser(1L, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(postRepository).findByUserId(1L, pageable);
    }
}