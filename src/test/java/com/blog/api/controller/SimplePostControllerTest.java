package com.blog.api.controller;

import com.blog.api.dto.PostDTO;
import com.blog.api.service.PostService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import java.time.LocalDateTime;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SimplePostControllerTest {

    @Mock
    private PostService postService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private PostController postController;

    private PostDTO samplePostDTO;

    @BeforeEach
    void setUp() {
        samplePostDTO = new PostDTO(
                1L,
                "Test Post",
                "Test content",
                true,
                LocalDateTime.now(),
                LocalDateTime.now(),
                "testuser",
                "Technology",
                5
        );
    }

    @Test
    void getAllPublishedPosts_ShouldReturnPageOfPosts() {
        // Arrange
        Page<PostDTO> page = new PageImpl<>(Arrays.asList(samplePostDTO));
        when(postService.getAllPublishedPosts(any())).thenReturn(page);

        // Act
        ResponseEntity<Page<PostDTO>> response = postController.getAllPublishedPosts(PageRequest.of(0, 10));

        // Assert
        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getContent()).hasSize(1);
        assertThat(response.getBody().getContent().get(0).title()).isEqualTo("Test Post");
        
        verify(postService).getAllPublishedPosts(any());
    }

    @Test
    void getPostById_ShouldReturnPost() {
        // Arrange
        when(postService.getPostById(1L)).thenReturn(samplePostDTO);

        // Act
        ResponseEntity<PostDTO> response = postController.getPostById(1L);

        // Assert
        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().title()).isEqualTo("Test Post");
        assertThat(response.getBody().content()).isEqualTo("Test content");
        
        verify(postService).getPostById(1L);
    }

    @Test
    void searchPosts_ShouldReturnFilteredPosts() {
        // Arrange
        Page<PostDTO> page = new PageImpl<>(Arrays.asList(samplePostDTO));
        when(postService.searchPosts(eq("test"), any())).thenReturn(page);

        // Act
        ResponseEntity<Page<PostDTO>> response = postController.searchPosts("test", PageRequest.of(0, 10));

        // Assert
        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getContent()).hasSize(1);
        
        verify(postService).searchPosts(eq("test"), any());
    }

    @Test
    void getPostsByCategory_ShouldReturnPostsFromCategory() {
        // Arrange
        Page<PostDTO> page = new PageImpl<>(Arrays.asList(samplePostDTO));
        when(postService.getPostsByCategory(eq(1L), any())).thenReturn(page);

        // Act
        ResponseEntity<Page<PostDTO>> response = postController.getPostsByCategory(1L, PageRequest.of(0, 10));

        // Assert
        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getContent()).hasSize(1);
        
        verify(postService).getPostsByCategory(eq(1L), any());
    }

    @Test
    void getPostsByUser_ShouldReturnUserPosts() {
        // Arrange
        Page<PostDTO> page = new PageImpl<>(Arrays.asList(samplePostDTO));
        when(postService.getPostsByUser(eq(1L), any())).thenReturn(page);

        // Act
        ResponseEntity<Page<PostDTO>> response = postController.getPostsByUser(1L, PageRequest.of(0, 10));

        // Assert
        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getContent()).hasSize(1);
        
        verify(postService).getPostsByUser(eq(1L), any());
    }
}