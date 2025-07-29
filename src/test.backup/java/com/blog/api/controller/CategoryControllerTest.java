package com.blog.api.controller;

import com.blog.api.dto.CategoryDTO;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
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

    private CategoryDTO categoryDTO;

    @BeforeEach
    void setUp() {
        categoryDTO = new CategoryDTO(1L, "Technology", "Technology related posts", 5);
    }

    @Test
    void getAllCategories_ShouldReturnPageOfCategories() throws Exception {
        Page<CategoryDTO> categoryPage = new PageImpl<>(Arrays.asList(categoryDTO));
        when(categoryService.getAllCategories(any(PageRequest.class))).thenReturn(categoryPage);

        mockMvc.perform(get("/api/v1/categories")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].name").value("Technology"))
                .andExpect(jsonPath("$.content[0].postCount").value(5));

        verify(categoryService).getAllCategories(any(PageRequest.class));
    }

    @Test
    void getCategoryById_WhenCategoryExists_ShouldReturnCategory() throws Exception {
        when(categoryService.getCategoryById(1L)).thenReturn(categoryDTO);

        mockMvc.perform(get("/api/v1/categories/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Technology"))
                .andExpect(jsonPath("$.description").value("Technology related posts"));

        verify(categoryService).getCategoryById(1L);
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void createCategory_WhenValidData_ShouldReturnCreatedCategory() throws Exception {
        CategoryDTO newCategoryDTO = new CategoryDTO(null, "Science", "Science related posts", 0);

        when(categoryService.createCategory(any(CategoryDTO.class))).thenReturn(categoryDTO);

        mockMvc.perform(post("/api/v1/categories")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newCategoryDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Technology"));

        verify(categoryService).createCategory(any(CategoryDTO.class));
    }

    @Test
    @WithMockUser(username = "user", roles = "USER")
    void createCategory_WhenUserRole_ShouldReturnForbidden() throws Exception {
        CategoryDTO newCategoryDTO = new CategoryDTO(null, "Science", "Science related posts", 0);

        mockMvc.perform(post("/api/v1/categories")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newCategoryDTO)))
                .andExpect(status().isForbidden());
    }

    @Test
    void createCategory_WhenNotAuthenticated_ShouldReturnUnauthorized() throws Exception {
        CategoryDTO newCategoryDTO = new CategoryDTO(null, "Science", "Science related posts", 0);

        mockMvc.perform(post("/api/v1/categories")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newCategoryDTO)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void createCategory_WhenInvalidData_ShouldReturnBadRequest() throws Exception {
        CategoryDTO invalidCategoryDTO = new CategoryDTO(null, "", "Valid description", 0);

        mockMvc.perform(post("/api/v1/categories")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidCategoryDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void updateCategory_WhenValidData_ShouldReturnUpdatedCategory() throws Exception {
        CategoryDTO updateCategoryDTO = new CategoryDTO(null, "Updated Technology", "Updated description", 0);

        when(categoryService.updateCategory(eq(1L), any(CategoryDTO.class))).thenReturn(categoryDTO);

        mockMvc.perform(put("/api/v1/categories/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateCategoryDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Technology"));

        verify(categoryService).updateCategory(eq(1L), any(CategoryDTO.class));
    }

    @Test
    @WithMockUser(username = "user", roles = "USER")
    void updateCategory_WhenUserRole_ShouldReturnForbidden() throws Exception {
        CategoryDTO updateCategoryDTO = new CategoryDTO(null, "Updated Technology", "Updated description", 0);

        mockMvc.perform(put("/api/v1/categories/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateCategoryDTO)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void deleteCategory_WhenValidRequest_ShouldReturnNoContent() throws Exception {
        mockMvc.perform(delete("/api/v1/categories/1")
                .with(csrf()))
                .andExpect(status().isNoContent());

        verify(categoryService).deleteCategory(1L);
    }

    @Test
    @WithMockUser(username = "user", roles = "USER")
    void deleteCategory_WhenUserRole_ShouldReturnForbidden() throws Exception {
        mockMvc.perform(delete("/api/v1/categories/1")
                .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    void deleteCategory_WhenNotAuthenticated_ShouldReturnUnauthorized() throws Exception {
        mockMvc.perform(delete("/api/v1/categories/1")
                .with(csrf()))
                .andExpect(status().isUnauthorized());
    }
}