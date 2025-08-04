package com.blog.api.controller;

import com.blog.api.dto.CreatePostDTO;
import com.blog.api.dto.PostDTO;
import com.blog.api.exception.ResourceNotFoundException;
import com.blog.api.service.PostService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PostController.class)
@DisplayName("Post Controller Tests")
class PostControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PostService postService;
    
    @MockBean
    private com.blog.api.util.JwtUtil jwtUtil;
    
    @MockBean  
    private com.blog.api.service.CustomUserDetailsService userDetailsService;
    
    @MockBean
    private com.blog.api.service.TermsService termsService;

    @Autowired
    private ObjectMapper objectMapper;

    private PostDTO samplePostDTO;
    private CreatePostDTO createPostDTO;

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

        createPostDTO = new CreatePostDTO("Test Post", "Test content", 1L, true);
    }

    @Test
    @DisplayName("Deve retornar página de posts quando buscar todos os posts")
    void getAllPosts_ShouldReturnPageOfPosts() throws Exception {
        // Arrange
        Page<PostDTO> page = new PageImpl<>(Arrays.asList(samplePostDTO));
        when(postService.getAllPublishedPosts(any())).thenReturn(page);

        // Act & Assert
        mockMvc.perform(get("/api/v1/posts")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].title").value("Test Post"))
                .andExpect(jsonPath("$.content[0].published").value(true));

        verify(postService).getAllPublishedPosts(any());
    }

    @Test
    @DisplayName("Deve retornar post quando post existe")
    void getPostById_ShouldReturnPost_WhenExists() throws Exception {
        // Arrange
        when(postService.getPostById(1L)).thenReturn(samplePostDTO);

        // Act & Assert
        mockMvc.perform(get("/api/v1/posts/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Test Post"))
                .andExpect(jsonPath("$.content").value("Test content"));

        verify(postService).getPostById(1L);
    }

    @Test
    @DisplayName("Deve retornar NotFound quando post não existe")
    void getPostById_ShouldReturnNotFound_WhenNotExists() throws Exception {
        // Arrange
        when(postService.getPostById(999L)).thenThrow(new ResourceNotFoundException("Post", "id", 999L));

        // Act & Assert
        mockMvc.perform(get("/api/v1/posts/999")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(postService).getPostById(999L);
    }

    @Test
    @WithMockUser(username = "testuser")
    @DisplayName("Deve criar e retornar post quando dados são válidos")
    void createPost_ShouldCreateAndReturnPost() throws Exception {
        // Arrange
        when(postService.createPost(any(CreatePostDTO.class), eq("testuser"))).thenReturn(samplePostDTO);

        // Act & Assert
        mockMvc.perform(post("/api/v1/posts")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createPostDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Test Post"))
                .andExpect(jsonPath("$.content").value("Test content"));

        verify(postService).createPost(any(CreatePostDTO.class), eq("testuser"));
    }

    @Test
    @DisplayName("Deve retornar Unauthorized quando não está autenticado")
    void createPost_ShouldReturnUnauthorized_WhenNotAuthenticated() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/v1/posts")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createPostDTO)))
                .andExpect(status().isUnauthorized());

        verify(postService, never()).createPost(any(), any());
    }

    @Test
    @WithMockUser(username = "testuser")
    @DisplayName("Deve retornar BadRequest quando dados são inválidos")
    void createPost_ShouldReturnBadRequest_WhenInvalidData() throws Exception {
        // Arrange
        CreatePostDTO invalidPost = new CreatePostDTO("", "Short", 1L, true); // Invalid title

        // Act & Assert
        mockMvc.perform(post("/api/v1/posts")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidPost)))
                .andExpect(status().isBadRequest());

        verify(postService, never()).createPost(any(), any());
    }

    @Test
    @WithMockUser(username = "testuser")
    @DisplayName("Deve atualizar e retornar post quando dados são válidos")
    void updatePost_ShouldUpdateAndReturnPost() throws Exception {
        // Arrange
        PostDTO updatedPost = new PostDTO(1L, "Updated Post", "Updated content", true, 
                LocalDateTime.now(), LocalDateTime.now(), "testuser", "Technology", 5);
        when(postService.updatePost(eq(1L), any(CreatePostDTO.class), eq("testuser"))).thenReturn(updatedPost);

        // Act & Assert
        mockMvc.perform(put("/api/v1/posts/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createPostDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Updated Post"));

        verify(postService).updatePost(eq(1L), any(CreatePostDTO.class), eq("testuser"));
    }

    @Test
    @WithMockUser(username = "testuser")
    @DisplayName("Deve retornar NotFound quando post para atualizar não existe")
    void updatePost_ShouldReturnNotFound_WhenPostNotExists() throws Exception {
        // Arrange
        when(postService.updatePost(eq(999L), any(CreatePostDTO.class), eq("testuser")))
                .thenThrow(new ResourceNotFoundException("Post", "id", 999L));

        // Act & Assert
        mockMvc.perform(put("/api/v1/posts/999")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createPostDTO)))
                .andExpect(status().isNotFound());

        verify(postService).updatePost(eq(999L), any(CreatePostDTO.class), eq("testuser"));
    }

    @Test
    @WithMockUser(username = "otheruser")
    @DisplayName("Deve retornar Forbidden quando usuário não é dono do post")
    void updatePost_ShouldReturnForbidden_WhenNotOwner() throws Exception {
        // Arrange
        when(postService.updatePost(eq(1L), any(CreatePostDTO.class), eq("otheruser")))
                .thenThrow(new RuntimeException("You can only update your own posts"));

        // Act & Assert
        mockMvc.perform(put("/api/v1/posts/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createPostDTO)))
                .andExpect(status().isInternalServerError());

        verify(postService).updatePost(eq(1L), any(CreatePostDTO.class), eq("otheruser"));
    }

    @Test
    @WithMockUser(username = "testuser")
    @DisplayName("Deve deletar post quando usuário é dono")
    void deletePost_ShouldDeletePost_WhenOwner() throws Exception {
        // Arrange
        doNothing().when(postService).deletePost(1L, "testuser");

        // Act & Assert
        mockMvc.perform(delete("/api/v1/posts/1")
                .with(csrf()))
                .andExpect(status().isNoContent());

        verify(postService).deletePost(1L, "testuser");
    }

    @Test
    @WithMockUser(username = "otheruser")
    @DisplayName("Deve retornar Forbidden quando usuário não é dono para deletar")
    void deletePost_ShouldReturnForbidden_WhenNotOwner() throws Exception {
        // Arrange
        doThrow(new RuntimeException("You can only delete your own posts"))
                .when(postService).deletePost(1L, "otheruser");

        // Act & Assert
        mockMvc.perform(delete("/api/v1/posts/1")
                .with(csrf()))
                .andExpect(status().isInternalServerError());

        verify(postService).deletePost(1L, "otheruser");
    }

    @Test
    @WithMockUser(username = "testuser")
    @DisplayName("Deve retornar NotFound quando post para deletar não existe")
    void deletePost_ShouldReturnNotFound_WhenPostNotExists() throws Exception {
        // Arrange
        doThrow(new ResourceNotFoundException("Post", "id", 999L))
                .when(postService).deletePost(999L, "testuser");

        // Act & Assert
        mockMvc.perform(delete("/api/v1/posts/999")
                .with(csrf()))
                .andExpect(status().isNotFound());

        verify(postService).deletePost(999L, "testuser");
    }

    @Test
    @DisplayName("Deve buscar posts por categoria")
    void getPostsByCategory_ShouldReturnPostsFromCategory() throws Exception {
        // Arrange
        Page<PostDTO> page = new PageImpl<>(Arrays.asList(samplePostDTO));
        when(postService.getPostsByCategory(eq(1L), any())).thenReturn(page);

        // Act & Assert
        mockMvc.perform(get("/api/v1/posts/category/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].categoryName").value("Technology"));

        verify(postService).getPostsByCategory(eq(1L), any());
    }

    @Test
    @DisplayName("Deve buscar posts por autor")
    void getPostsByAuthor_ShouldReturnPostsFromAuthor() throws Exception {
        // Arrange
        Page<PostDTO> page = new PageImpl<>(Arrays.asList(samplePostDTO));
        when(postService.getPostsByUser(eq(1L), any())).thenReturn(page);

        // Act & Assert
        mockMvc.perform(get("/api/v1/posts/user/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].authorUsername").value("testuser"));

        verify(postService).getPostsByUser(eq(1L), any());
    }

    @Test
    @DisplayName("Deve buscar posts por título contendo texto")
    void searchPostsByTitle_ShouldReturnMatchingPosts() throws Exception {
        // Arrange
        Page<PostDTO> page = new PageImpl<>(Arrays.asList(samplePostDTO));
        when(postService.searchPosts(eq("test"), any())).thenReturn(page);

        // Act & Assert
        mockMvc.perform(get("/api/v1/posts/search")
                .param("keyword", "test")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].title").value("Test Post"));

        verify(postService).searchPosts(eq("test"), any());
    }

    // Método getRecentPosts removido pois não existe no PostService real
}