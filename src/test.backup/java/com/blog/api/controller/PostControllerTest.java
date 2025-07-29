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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PostController.class)
class PostControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PostService postService;

    @Autowired
    private ObjectMapper objectMapper;

    private PostDTO postDTO;
    private CreatePostDTO createPostDTO;

    @BeforeEach
    void setUp() {
        postDTO = new PostDTO(
            1L,
            "Test Post",
            "Test content",
            true,
            LocalDateTime.now(),
            LocalDateTime.now(),
            "testuser",
            "Technology",
            0
        );

        createPostDTO = new CreatePostDTO("New Post", "New post content", 1L, true);
    }

    @Test
    void getAllPublishedPosts_ShouldReturnPageOfPosts() throws Exception {
        Page<PostDTO> postPage = new PageImpl<>(Arrays.asList(postDTO));
        when(postService.getAllPublishedPosts(any(PageRequest.class))).thenReturn(postPage);

        mockMvc.perform(get("/api/v1/posts")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].title").value("Test Post"))
                .andExpect(jsonPath("$.content[0].authorUsername").value("testuser"));

        verify(postService).getAllPublishedPosts(any(PageRequest.class));
    }

    @Test
    void getPostById_WhenPostExists_ShouldReturnPost() throws Exception {
        when(postService.getPostById(1L)).thenReturn(postDTO);

        mockMvc.perform(get("/api/v1/posts/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.title").value("Test Post"))
                .andExpect(jsonPath("$.content").value("Test content"))
                .andExpect(jsonPath("$.authorUsername").value("testuser"));

        verify(postService).getPostById(1L);
    }

    @Test
    void searchPosts_ShouldReturnMatchingPosts() throws Exception {
        Page<PostDTO> postPage = new PageImpl<>(Arrays.asList(postDTO));
        when(postService.searchPosts(eq("test"), any(PageRequest.class))).thenReturn(postPage);

        mockMvc.perform(get("/api/v1/posts/search")
                .param("keyword", "test")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].title").value("Test Post"));

        verify(postService).searchPosts(eq("test"), any(PageRequest.class));
    }

    @Test
    void getPostsByCategory_ShouldReturnPostsFromCategory() throws Exception {
        Page<PostDTO> postPage = new PageImpl<>(Arrays.asList(postDTO));
        when(postService.getPostsByCategory(eq(1L), any(PageRequest.class))).thenReturn(postPage);

        mockMvc.perform(get("/api/v1/posts/category/1")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].categoryName").value("Technology"));

        verify(postService).getPostsByCategory(eq(1L), any(PageRequest.class));
    }

    @Test
    void getPostsByUser_ShouldReturnPostsFromUser() throws Exception {
        Page<PostDTO> postPage = new PageImpl<>(Arrays.asList(postDTO));
        when(postService.getPostsByUser(eq(1L), any(PageRequest.class))).thenReturn(postPage);

        mockMvc.perform(get("/api/v1/posts/user/1")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].authorUsername").value("testuser"));

        verify(postService).getPostsByUser(eq(1L), any(PageRequest.class));
    }

    @Test
    @WithMockUser(username = "testuser", roles = "AUTHOR")
    void createPost_WhenValidData_ShouldReturnCreatedPost() throws Exception {
        when(postService.createPost(any(CreatePostDTO.class), anyString())).thenReturn(postDTO);

        mockMvc.perform(post("/api/v1/posts")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createPostDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Test Post"))
                .andExpect(jsonPath("$.authorUsername").value("testuser"));

        verify(postService).createPost(any(CreatePostDTO.class), eq("testuser"));
    }

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    void createPost_WhenUserRole_ShouldReturnForbidden() throws Exception {
        mockMvc.perform(post("/api/v1/posts")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createPostDTO)))
                .andExpect(status().isForbidden());
    }

    @Test
    void createPost_WhenNotAuthenticated_ShouldReturnUnauthorized() throws Exception {
        mockMvc.perform(post("/api/v1/posts")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createPostDTO)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "testuser", roles = "AUTHOR")
    void createPost_WhenInvalidData_ShouldReturnBadRequest() throws Exception {
        createPostDTO.setTitle(""); // Invalid title
        createPostDTO.setContent(""); // Invalid content

        mockMvc.perform(post("/api/v1/posts")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createPostDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "testuser", roles = "AUTHOR")
    void updatePost_WhenValidData_ShouldReturnUpdatedPost() throws Exception {
        when(postService.updatePost(eq(1L), any(CreatePostDTO.class), anyString())).thenReturn(postDTO);

        mockMvc.perform(put("/api/v1/posts/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createPostDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Test Post"));

        verify(postService).updatePost(eq(1L), any(CreatePostDTO.class), eq("testuser"));
    }

    @Test
    @WithMockUser(username = "testuser", roles = "AUTHOR")
    void deletePost_WhenValidRequest_ShouldReturnNoContent() throws Exception {
        mockMvc.perform(delete("/api/v1/posts/1")
                .with(csrf()))
                .andExpect(status().isNoContent());

        verify(postService).deletePost(1L, "testuser");
    }

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    void deletePost_WhenUserRole_ShouldReturnForbidden() throws Exception {
        mockMvc.perform(delete("/api/v1/posts/1")
                .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    void deletePost_WhenNotAuthenticated_ShouldReturnUnauthorized() throws Exception {
        mockMvc.perform(delete("/api/v1/posts/1")
                .with(csrf()))
                .andExpect(status().isUnauthorized());
    }
}