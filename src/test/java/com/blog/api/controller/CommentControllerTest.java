package com.blog.api.controller;

import com.blog.api.dto.CommentDTO;
import com.blog.api.exception.ResourceNotFoundException;
import com.blog.api.service.CommentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
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
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CommentController.class)
class CommentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CommentService commentService;

    @Autowired
    private ObjectMapper objectMapper;

    private CommentDTO sampleCommentDTO;
    private CommentDTO createCommentDTO;

    @BeforeEach
    void setUp() {
        sampleCommentDTO = new CommentDTO(
                1L,
                "Test comment content",
                LocalDateTime.now(),
                "testuser",
                1L,
                null,
                Arrays.asList()
        );

        createCommentDTO = new CommentDTO(
                null,
                "New comment content",
                null,
                null,
                1L,
                null,
                null
        );
    }

    @Test
    void getCommentsByPost_ShouldReturnPageOfComments() throws Exception {
        // Arrange
        Page<CommentDTO> page = new PageImpl<>(Arrays.asList(sampleCommentDTO));
        when(commentService.getCommentsByPost(eq(1L), any())).thenReturn(page);

        // Act & Assert
        mockMvc.perform(get("/api/v1/comments/post/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].content").value("Test comment content"))
                .andExpect(jsonPath("$.content[0].authorUsername").value("testuser"))
                .andExpect(jsonPath("$.content[0].postId").value(1));

        verify(commentService).getCommentsByPost(eq(1L), any());
    }

    @Test
    void getCommentsByPost_ShouldHandlePagination() throws Exception {
        // Arrange
        Page<CommentDTO> page = new PageImpl<>(Arrays.asList(sampleCommentDTO));
        when(commentService.getCommentsByPost(eq(1L), any())).thenReturn(page);

        // Act & Assert
        mockMvc.perform(get("/api/v1/comments/post/1")
                .param("page", "0")
                .param("size", "10")
                .param("sort", "createdAt,desc")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.pageable").exists());

        verify(commentService).getCommentsByPost(eq(1L), any());
    }

    @Test
    void getCommentsByPostSimple_ShouldReturnListOfComments() throws Exception {
        // Arrange
        List<CommentDTO> comments = Arrays.asList(sampleCommentDTO);
        when(commentService.getCommentsByPostSimple(1L)).thenReturn(comments);

        // Act & Assert
        mockMvc.perform(get("/api/v1/comments/post/1/simple")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].content").value("Test comment content"));

        verify(commentService).getCommentsByPostSimple(1L);
    }

    @Test
    void getCommentById_ShouldReturnComment_WhenExists() throws Exception {
        // Arrange
        when(commentService.getCommentById(1L)).thenReturn(sampleCommentDTO);

        // Act & Assert
        mockMvc.perform(get("/api/v1/comments/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.content").value("Test comment content"))
                .andExpect(jsonPath("$.authorUsername").value("testuser"));

        verify(commentService).getCommentById(1L);
    }

    @Test
    void getCommentById_ShouldReturnNotFound_WhenNotExists() throws Exception {
        // Arrange
        when(commentService.getCommentById(999L)).thenThrow(new ResourceNotFoundException("Comment", "id", 999L));

        // Act & Assert
        mockMvc.perform(get("/api/v1/comments/999")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(commentService).getCommentById(999L);
    }

    @Test
    @WithMockUser(username = "testuser")
    void createComment_ShouldCreateAndReturnComment() throws Exception {
        // Arrange
        when(commentService.createComment(any(CommentDTO.class), eq("testuser"))).thenReturn(sampleCommentDTO);

        // Act & Assert
        mockMvc.perform(post("/api/v1/comments")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createCommentDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.content").value("Test comment content"))
                .andExpect(jsonPath("$.authorUsername").value("testuser"));

        verify(commentService).createComment(any(CommentDTO.class), eq("testuser"));
    }

    @Test
    void createComment_ShouldReturnUnauthorized_WhenNotAuthenticated() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/v1/comments")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createCommentDTO)))
                .andExpect(status().isUnauthorized());

        verify(commentService, never()).createComment(any(), any());
    }

    @Test
    @WithMockUser(username = "testuser")
    void createComment_ShouldReturnBadRequest_WhenInvalidData() throws Exception {
        // Arrange
        CommentDTO invalidComment = new CommentDTO(null, "", null, null, 1L, null, null); // Empty content

        // Act & Assert
        mockMvc.perform(post("/api/v1/comments")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidComment)))
                .andExpect(status().isBadRequest());

        verify(commentService, never()).createComment(any(), any());
    }

    @Test
    @WithMockUser(username = "testuser")
    void createComment_ShouldReturnBadRequest_WhenContentTooLong() throws Exception {
        // Arrange
        String longContent = "a".repeat(1001); // Exceeds 1000 character limit
        CommentDTO invalidComment = new CommentDTO(null, longContent, null, null, 1L, null, null);

        // Act & Assert
        mockMvc.perform(post("/api/v1/comments")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidComment)))
                .andExpect(status().isBadRequest());

        verify(commentService, never()).createComment(any(), any());
    }

    @Test
    @WithMockUser(username = "testuser")
    void updateComment_ShouldUpdateAndReturnComment() throws Exception {
        // Arrange
        CommentDTO updatedComment = new CommentDTO(1L, "Updated comment content", LocalDateTime.now(), 
                "testuser", 1L, null, Arrays.asList());
        when(commentService.updateComment(eq(1L), any(CommentDTO.class), eq("testuser"))).thenReturn(updatedComment);

        // Act & Assert
        mockMvc.perform(put("/api/v1/comments/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createCommentDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.content").value("Updated comment content"));

        verify(commentService).updateComment(eq(1L), any(CommentDTO.class), eq("testuser"));
    }

    @Test
    @WithMockUser(username = "testuser")
    void updateComment_ShouldReturnNotFound_WhenCommentNotExists() throws Exception {
        // Arrange
        when(commentService.updateComment(eq(999L), any(CommentDTO.class), eq("testuser")))
                .thenThrow(new ResourceNotFoundException("Comment", "id", 999L));

        // Act & Assert
        mockMvc.perform(put("/api/v1/comments/999")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createCommentDTO)))
                .andExpect(status().isNotFound());

        verify(commentService).updateComment(eq(999L), any(CommentDTO.class), eq("testuser"));
    }

    @Test
    @WithMockUser(username = "otheruser")
    void updateComment_ShouldReturnForbidden_WhenNotOwner() throws Exception {
        // Arrange
        when(commentService.updateComment(eq(1L), any(CommentDTO.class), eq("otheruser")))
                .thenThrow(new RuntimeException("You can only update your own comments"));

        // Act & Assert
        mockMvc.perform(put("/api/v1/comments/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createCommentDTO)))
                .andExpect(status().isInternalServerError());

        verify(commentService).updateComment(eq(1L), any(CommentDTO.class), eq("otheruser"));
    }

    @Test
    void updateComment_ShouldReturnUnauthorized_WhenNotAuthenticated() throws Exception {
        // Act & Assert
        mockMvc.perform(put("/api/v1/comments/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createCommentDTO)))
                .andExpect(status().isUnauthorized());

        verify(commentService, never()).updateComment(any(), any(), any());
    }

    @Test
    @WithMockUser(username = "testuser")
    void deleteComment_ShouldDeleteComment_WhenOwner() throws Exception {
        // Arrange
        doNothing().when(commentService).deleteComment(1L, "testuser");

        // Act & Assert
        mockMvc.perform(delete("/api/v1/comments/1")
                .with(csrf()))
                .andExpect(status().isNoContent());

        verify(commentService).deleteComment(1L, "testuser");
    }

    @Test
    @WithMockUser(username = "otheruser")
    void deleteComment_ShouldReturnForbidden_WhenNotOwner() throws Exception {
        // Arrange
        doThrow(new RuntimeException("You can only delete your own comments"))
                .when(commentService).deleteComment(1L, "otheruser");

        // Act & Assert
        mockMvc.perform(delete("/api/v1/comments/1")
                .with(csrf()))
                .andExpect(status().isInternalServerError());

        verify(commentService).deleteComment(1L, "otheruser");
    }

    @Test
    @WithMockUser(username = "testuser")
    void deleteComment_ShouldReturnNotFound_WhenCommentNotExists() throws Exception {
        // Arrange
        doThrow(new ResourceNotFoundException("Comment", "id", 999L))
                .when(commentService).deleteComment(999L, "testuser");

        // Act & Assert
        mockMvc.perform(delete("/api/v1/comments/999")
                .with(csrf()))
                .andExpect(status().isNotFound());

        verify(commentService).deleteComment(999L, "testuser");
    }

    @Test
    void deleteComment_ShouldReturnUnauthorized_WhenNotAuthenticated() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/api/v1/comments/1")
                .with(csrf()))
                .andExpect(status().isUnauthorized());

        verify(commentService, never()).deleteComment(any(), any());
    }

    @Test
    void getCommentsByPost_ShouldReturnEmptyPage_WhenNoComments() throws Exception {
        // Arrange
        Page<CommentDTO> emptyPage = new PageImpl<>(Arrays.asList());
        when(commentService.getCommentsByPost(eq(1L), any())).thenReturn(emptyPage);

        // Act & Assert
        mockMvc.perform(get("/api/v1/comments/post/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content").isEmpty())
                .andExpect(jsonPath("$.totalElements").value(0));

        verify(commentService).getCommentsByPost(eq(1L), any());
    }

    @Test
    void getCommentsByPostSimple_ShouldReturnEmptyList_WhenNoComments() throws Exception {
        // Arrange
        when(commentService.getCommentsByPostSimple(1L)).thenReturn(Arrays.asList());

        // Act & Assert
        mockMvc.perform(get("/api/v1/comments/post/1/simple")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());

        verify(commentService).getCommentsByPostSimple(1L);
    }

    @Test
    @WithMockUser(username = "testuser")
    void createComment_ShouldCreateReply_WhenParentIdProvided() throws Exception {
        // Arrange
        CommentDTO replyComment = new CommentDTO(null, "This is a reply", null, null, 1L, 1L, null);
        CommentDTO createdReply = new CommentDTO(2L, "This is a reply", LocalDateTime.now(), 
                "testuser", 1L, 1L, Arrays.asList());
        when(commentService.createComment(any(CommentDTO.class), eq("testuser"))).thenReturn(createdReply);

        // Act & Assert
        mockMvc.perform(post("/api/v1/comments")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(replyComment)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.content").value("This is a reply"))
                .andExpect(jsonPath("$.parentId").value(1));

        verify(commentService).createComment(any(CommentDTO.class), eq("testuser"));
    }

    @Test
    void getCommentById_ShouldReturnBadRequest_WhenInvalidIdFormat() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/v1/comments/invalid")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(commentService, never()).getCommentById(any());
    }

    @Test
    void getCommentsByPost_ShouldReturnBadRequest_WhenInvalidPostIdFormat() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/v1/comments/post/invalid")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(commentService, never()).getCommentsByPost(any(), any());
    }
}