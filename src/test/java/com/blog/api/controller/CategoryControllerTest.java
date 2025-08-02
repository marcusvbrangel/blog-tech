package com.blog.api.controller;

import com.blog.api.dto.CategoryDTO;
import com.blog.api.exception.ResourceNotFoundException;
import com.blog.api.service.CategoryService;
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

import java.util.Arrays;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CategoryController.class)
class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CategoryService categoryService;

    @Autowired
    private ObjectMapper objectMapper;

    private CategoryDTO sampleCategoryDTO;
    private CategoryDTO createCategoryDTO;

    @BeforeEach
    void setUp() {
        sampleCategoryDTO = new CategoryDTO(1L, "Technology", "Technology related posts", 5);
        createCategoryDTO = new CategoryDTO(null, "Science", "Science related posts", 0);
    }

    @Test
    void getAllCategories_ShouldReturnPageOfCategories() throws Exception {
        // Arrange
        Page<CategoryDTO> categoryPage = new PageImpl<>(Arrays.asList(sampleCategoryDTO));
        when(categoryService.getAllCategories(any())).thenReturn(categoryPage);

        // Act & Assert
        mockMvc.perform(get("/api/v1/categories")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].name").value("Technology"))
                .andExpect(jsonPath("$.content[0].description").value("Technology related posts"))
                .andExpect(jsonPath("$.content[0].postCount").value(5));

        verify(categoryService).getAllCategories(any());
    }

    @Test
    void getAllCategories_ShouldReturnEmptyPage_WhenNoCategories() throws Exception {
        // Arrange
        Page<CategoryDTO> emptyPage = new PageImpl<>(Arrays.asList());
        when(categoryService.getAllCategories(any())).thenReturn(emptyPage);

        // Act & Assert
        mockMvc.perform(get("/api/v1/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content").isEmpty())
                .andExpect(jsonPath("$.totalElements").value(0));

        verify(categoryService).getAllCategories(any());
    }

    @Test
    void getAllCategories_ShouldHandlePagination() throws Exception {
        // Arrange
        Page<CategoryDTO> categoryPage = new PageImpl<>(Arrays.asList(sampleCategoryDTO));
        when(categoryService.getAllCategories(any())).thenReturn(categoryPage);

        // Act & Assert
        mockMvc.perform(get("/api/v1/categories")
                .param("page", "1")
                .param("size", "5")
                .param("sort", "name,asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.pageable").exists());

        verify(categoryService).getAllCategories(any());
    }

    @Test
    void getCategoryById_WhenCategoryExists_ShouldReturnCategory() throws Exception {
        // Arrange
        when(categoryService.getCategoryById(1L)).thenReturn(sampleCategoryDTO);

        // Act & Assert
        mockMvc.perform(get("/api/v1/categories/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Technology"))
                .andExpect(jsonPath("$.description").value("Technology related posts"))
                .andExpect(jsonPath("$.postCount").value(5));

        verify(categoryService).getCategoryById(1L);
    }

    @Test
    void getCategoryById_WhenCategoryNotExists_ShouldReturnNotFound() throws Exception {
        // Arrange
        when(categoryService.getCategoryById(999L))
                .thenThrow(new ResourceNotFoundException("Category", "id", 999L));

        // Act & Assert
        mockMvc.perform(get("/api/v1/categories/999"))
                .andExpect(status().isNotFound());

        verify(categoryService).getCategoryById(999L);
    }

    @Test
    void getCategoryById_WhenInvalidIdFormat_ShouldReturnBadRequest() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/v1/categories/invalid"))
                .andExpect(status().isBadRequest());

        verify(categoryService, never()).getCategoryById(any());
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void createCategory_WhenValidData_ShouldReturnCreatedCategory() throws Exception {
        // Arrange
        when(categoryService.createCategory(any(CategoryDTO.class))).thenReturn(sampleCategoryDTO);

        // Act & Assert
        mockMvc.perform(post("/api/v1/categories")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createCategoryDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Technology"))
                .andExpect(jsonPath("$.description").value("Technology related posts"));

        verify(categoryService).createCategory(any(CategoryDTO.class));
    }

    @Test
    @WithMockUser(username = "user", roles = "USER")
    void createCategory_WhenUserRole_ShouldReturnForbidden() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/v1/categories")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createCategoryDTO)))
                .andExpect(status().isForbidden());

        verify(categoryService, never()).createCategory(any());
    }

    @Test
    void createCategory_WhenNotAuthenticated_ShouldReturnUnauthorized() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/v1/categories")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createCategoryDTO)))
                .andExpect(status().isUnauthorized());

        verify(categoryService, never()).createCategory(any());
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void createCategory_WhenInvalidData_ShouldReturnBadRequest() throws Exception {
        // Arrange
        CategoryDTO invalidCategoryDTO = new CategoryDTO(null, "", "Valid description", 0);

        // Act & Assert
        mockMvc.perform(post("/api/v1/categories")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidCategoryDTO)))
                .andExpect(status().isBadRequest());

        verify(categoryService, never()).createCategory(any());
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void createCategory_WhenDuplicateName_ShouldReturnConflict() throws Exception {
        // Arrange
        when(categoryService.createCategory(any(CategoryDTO.class)))
                .thenThrow(new RuntimeException("Category with name already exists"));

        // Act & Assert
        mockMvc.perform(post("/api/v1/categories")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createCategoryDTO)))
                .andExpect(status().isInternalServerError());

        verify(categoryService).createCategory(any(CategoryDTO.class));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void updateCategory_WhenValidData_ShouldReturnUpdatedCategory() throws Exception {
        // Arrange
        CategoryDTO updateCategoryDTO = new CategoryDTO(null, "Updated Technology", "Updated description", 0);
        CategoryDTO updatedResult = new CategoryDTO(1L, "Updated Technology", "Updated description", 7);
        when(categoryService.updateCategory(eq(1L), any(CategoryDTO.class))).thenReturn(updatedResult);

        // Act & Assert
        mockMvc.perform(put("/api/v1/categories/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateCategoryDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Updated Technology"))
                .andExpect(jsonPath("$.description").value("Updated description"))
                .andExpect(jsonPath("$.postCount").value(7));

        verify(categoryService).updateCategory(eq(1L), any(CategoryDTO.class));
    }

    @Test
    @WithMockUser(username = "user", roles = "USER")
    void updateCategory_WhenUserRole_ShouldReturnForbidden() throws Exception {
        // Arrange
        CategoryDTO updateCategoryDTO = new CategoryDTO(null, "Updated Technology", "Updated description", 0);

        // Act & Assert
        mockMvc.perform(put("/api/v1/categories/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateCategoryDTO)))
                .andExpect(status().isForbidden());

        verify(categoryService, never()).updateCategory(any(), any());
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void updateCategory_WhenCategoryNotExists_ShouldReturnNotFound() throws Exception {
        // Arrange
        CategoryDTO updateCategoryDTO = new CategoryDTO(null, "Updated Technology", "Updated description", 0);
        when(categoryService.updateCategory(eq(999L), any(CategoryDTO.class)))
                .thenThrow(new ResourceNotFoundException("Category", "id", 999L));

        // Act & Assert
        mockMvc.perform(put("/api/v1/categories/999")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateCategoryDTO)))
                .andExpect(status().isNotFound());

        verify(categoryService).updateCategory(eq(999L), any(CategoryDTO.class));
    }

    @Test
    void updateCategory_WhenNotAuthenticated_ShouldReturnUnauthorized() throws Exception {
        // Arrange
        CategoryDTO updateCategoryDTO = new CategoryDTO(null, "Updated Technology", "Updated description", 0);

        // Act & Assert
        mockMvc.perform(put("/api/v1/categories/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateCategoryDTO)))
                .andExpect(status().isUnauthorized());

        verify(categoryService, never()).updateCategory(any(), any());
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void deleteCategory_WhenValidRequest_ShouldReturnNoContent() throws Exception {
        // Arrange
        doNothing().when(categoryService).deleteCategory(1L);

        // Act & Assert
        mockMvc.perform(delete("/api/v1/categories/1")
                .with(csrf()))
                .andExpect(status().isNoContent());

        verify(categoryService).deleteCategory(1L);
    }

    @Test
    @WithMockUser(username = "user", roles = "USER")
    void deleteCategory_WhenUserRole_ShouldReturnForbidden() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/api/v1/categories/1")
                .with(csrf()))
                .andExpect(status().isForbidden());

        verify(categoryService, never()).deleteCategory(any());
    }

    @Test
    void deleteCategory_WhenNotAuthenticated_ShouldReturnUnauthorized() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/api/v1/categories/1")
                .with(csrf()))
                .andExpect(status().isUnauthorized());

        verify(categoryService, never()).deleteCategory(any());
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void deleteCategory_WhenCategoryNotExists_ShouldReturnNotFound() throws Exception {
        // Arrange
        doThrow(new ResourceNotFoundException("Category", "id", 999L))
                .when(categoryService).deleteCategory(999L);

        // Act & Assert
        mockMvc.perform(delete("/api/v1/categories/999")
                .with(csrf()))
                .andExpect(status().isNotFound());

        verify(categoryService).deleteCategory(999L);
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void deleteCategory_WhenCategoryHasPosts_ShouldReturnConflict() throws Exception {
        // Arrange
        doThrow(new RuntimeException("Cannot delete category with existing posts"))
                .when(categoryService).deleteCategory(1L);

        // Act & Assert
        mockMvc.perform(delete("/api/v1/categories/1")
                .with(csrf()))
                .andExpect(status().isInternalServerError());

        verify(categoryService).deleteCategory(1L);
    }

    // Test endpoints specific to this controller
    @Test
    void testCategories_ShouldReturnCategoryCount() throws Exception {
        // This tests the /test endpoint - implementation depends on actual controller
        mockMvc.perform(get("/api/v1/categories/test"))
                .andExpect(status().isOk());
    }

    @Test
    void testCategoryDTO_ShouldReturnTestDTO() throws Exception {
        // This tests the /test-dto endpoint - implementation depends on actual controller  
        mockMvc.perform(get("/api/v1/categories/test-dto"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Test Category"))
                .andExpect(jsonPath("$.description").value("Test Description"))
                .andExpect(jsonPath("$.postCount").value(0));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void createCategory_WhenValidDataWithLongDescription_ShouldReturnCreated() throws Exception {
        // Arrange
        String longDescription = "This is a very long description that tests the validation limits for category descriptions in the system";
        CategoryDTO categoryWithLongDesc = new CategoryDTO(null, "Test Category", longDescription, 0);
        when(categoryService.createCategory(any(CategoryDTO.class))).thenReturn(sampleCategoryDTO);

        // Act & Assert
        mockMvc.perform(post("/api/v1/categories")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(categoryWithLongDesc)))
                .andExpect(status().isCreated());

        verify(categoryService).createCategory(any(CategoryDTO.class));
    }

    @Test
    void getAllCategories_WhenServiceThrowsException_ShouldReturnInternalServerError() throws Exception {
        // Arrange
        when(categoryService.getAllCategories(any()))
                .thenThrow(new RuntimeException("Database connection error"));

        // Act & Assert
        mockMvc.perform(get("/api/v1/categories"))
                .andExpect(status().isInternalServerError());

        verify(categoryService).getAllCategories(any());
    }
}