package com.blog.api.controller;

import com.blog.api.dto.CreatePostDTO;
import com.blog.api.dto.PostDTO;
import com.blog.api.service.PostService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = PostController.class, 
    excludeFilters = {
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, 
            classes = {
                com.blog.api.config.JwtAuthenticationFilter.class,
                com.blog.api.config.TermsComplianceFilter.class,
                com.blog.api.config.SecurityConfig.class
            })
    },
    excludeAutoConfiguration = {
        org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class,
        org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration.class
    })
class WorkingPostControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PostService postService;

    @Autowired
    private ObjectMapper objectMapper;

    private PostDTO samplePostDTO;
    private CreatePostDTO createPostDTO;

    @BeforeEach
    void setUp() {
        samplePostDTO = new PostDTO(
                1L,
                "Test Post Title",
                "Test post content goes here...",
                true,
                LocalDateTime.of(2025, 8, 2, 10, 0),
                LocalDateTime.of(2025, 8, 2, 10, 0),
                "testuser",
                "Technology",
                0
        );

        createPostDTO = new CreatePostDTO(
                "New Post Title",
                "New post content goes here with more than 10 characters",
                1L,
                true
        );
    }

    @Test
    void getAllPosts_ShouldReturnPageOfPosts() throws Exception {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        List<PostDTO> content = Arrays.asList(samplePostDTO);
        Page<PostDTO> page = new PageImpl<>(content, pageable, 1);
        
        when(postService.getAllPublishedPosts(any(Pageable.class))).thenReturn(page);

        // Act & Assert
        mockMvc.perform(get("/api/v1/posts")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].title").value("Test Post Title"))
                .andExpect(jsonPath("$.content[0].authorUsername").value("testuser"));

        verify(postService).getAllPublishedPosts(any(Pageable.class));
    }

    @Test
    void getPostById_ShouldReturnPost_WhenExists() throws Exception {
        // Arrange
        when(postService.getPostById(1L)).thenReturn(samplePostDTO);

        // Act & Assert
        mockMvc.perform(get("/api/v1/posts/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Test Post Title"))
                .andExpect(jsonPath("$.content").value("Test post content goes here..."));

        verify(postService).getPostById(1L);
    }

    @Test
    void createPost_ShouldCreateAndReturnPost() throws Exception {
        // Arrange
        when(postService.createPost(any(CreatePostDTO.class), eq("testuser"))).thenReturn(samplePostDTO);

        // Act & Assert
        mockMvc.perform(post("/api/v1/posts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createPostDTO))
                .header("X-User-Username", "testuser")) // Simulating user authentication
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.title").value("Test Post Title"));

        verify(postService).createPost(any(CreatePostDTO.class), eq("testuser"));
    }

    @Test
    void updatePost_ShouldUpdateAndReturnPost() throws Exception {
        // Arrange
        when(postService.updatePost(eq(1L), any(CreatePostDTO.class), eq("testuser"))).thenReturn(samplePostDTO);

        // Act & Assert
        mockMvc.perform(put("/api/v1/posts/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createPostDTO))
                .header("X-User-Username", "testuser"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Test Post Title"));

        verify(postService).updatePost(eq(1L), any(CreatePostDTO.class), eq("testuser"));
    }

    @Test
    void deletePost_ShouldReturnNoContent() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/api/v1/posts/1")
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-User-Username", "testuser"))
                .andExpect(status().isNoContent());

        verify(postService).deletePost(1L, "testuser");
    }

    @Test
    void searchPosts_ShouldReturnPageOfPosts() throws Exception {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        List<PostDTO> content = Arrays.asList(samplePostDTO);
        Page<PostDTO> page = new PageImpl<>(content, pageable, 1);
        
        when(postService.searchPosts(eq("test"), any(Pageable.class))).thenReturn(page);

        // Act & Assert
        mockMvc.perform(get("/api/v1/posts/search")
                .param("keyword", "test")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].title").value("Test Post Title"));

        verify(postService).searchPosts(eq("test"), any(Pageable.class));
    }

    @Test
    void getPostsByCategory_ShouldReturnPageOfPosts() throws Exception {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        List<PostDTO> content = Arrays.asList(samplePostDTO);
        Page<PostDTO> page = new PageImpl<>(content, pageable, 1);
        
        when(postService.getPostsByCategory(eq(1L), any(Pageable.class))).thenReturn(page);

        // Act & Assert
        mockMvc.perform(get("/api/v1/posts/category/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].categoryName").value("Technology"));

        verify(postService).getPostsByCategory(eq(1L), any(Pageable.class));
    }

    @Test
    void getPostsByUser_ShouldReturnPageOfPosts() throws Exception {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        List<PostDTO> content = Arrays.asList(samplePostDTO);
        Page<PostDTO> page = new PageImpl<>(content, pageable, 1);
        
        when(postService.getPostsByUser(eq(1L), any(Pageable.class))).thenReturn(page);

        // Act & Assert
        mockMvc.perform(get("/api/v1/posts/user/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].authorUsername").value("testuser"));

        verify(postService).getPostsByUser(eq(1L), any(Pageable.class));
    }
}