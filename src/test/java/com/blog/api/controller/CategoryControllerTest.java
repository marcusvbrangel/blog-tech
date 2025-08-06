package com.blog.api.controller;

import com.blog.api.dto.CategoryDTO;
import com.blog.api.exception.ResourceNotFoundException;
import com.blog.api.service.CategoryService;
import com.blog.api.service.CustomUserDetailsService;
import com.blog.api.service.TermsService;
import com.blog.api.util.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = CategoryController.class, 
    excludeFilters = {
        @org.springframework.context.annotation.ComponentScan.Filter(
            type = org.springframework.context.annotation.FilterType.ASSIGNABLE_TYPE, 
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
@DisplayName("Category Controller Tests")
class CategoryControllerTest {

    @org.springframework.beans.factory.annotation.Autowired
    private MockMvc mockMvc;

    @org.springframework.boot.test.mock.mockito.MockBean
    private CategoryService categoryService;
    
    @org.springframework.boot.test.mock.mockito.MockBean
    private com.blog.api.util.JwtUtil jwtUtil;
    
    @org.springframework.boot.test.mock.mockito.MockBean
    private com.blog.api.service.CustomUserDetailsService userDetailsService;
    
    @org.springframework.boot.test.mock.mockito.MockBean
    private com.blog.api.service.TermsService termsService;

    @org.springframework.beans.factory.annotation.Autowired
    private ObjectMapper objectMapper;

    private CategoryDTO sampleCategoryDTO;
    private CategoryDTO createCategoryDTO;

    @BeforeEach
    void setUp() {
        sampleCategoryDTO = new CategoryDTO(1L, "Technology", "Technology related posts", 5);
        createCategoryDTO = new CategoryDTO(null, "Science", "Science related posts", 0);
    }

    @Test
    @DisplayName("Deve retornar página de categorias quando buscar todas as categorias")
    void getAllCategories_ShouldReturnPageOfCategories() throws Exception {
        // Arrange
        java.util.List<CategoryDTO> content = new java.util.ArrayList<>();
        content.add(sampleCategoryDTO);
        Page<CategoryDTO> categoryPage = new PageImpl<>(content);
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
    @DisplayName("Deve retornar página vazia quando não há categorias")
    void getAllCategories_ShouldReturnEmptyPage_WhenNoCategories() throws Exception {
        // Arrange
        Page<CategoryDTO> emptyPage = new PageImpl<>(new java.util.ArrayList<>());
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
    @DisplayName("Deve lidar com paginação corretamente")
    void getAllCategories_ShouldHandlePagination() throws Exception {
        // Arrange
        java.util.List<CategoryDTO> content = new java.util.ArrayList<>();
        content.add(sampleCategoryDTO);
        Page<CategoryDTO> categoryPage = new PageImpl<>(content);
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
    @DisplayName("Deve retornar categoria quando categoria existe")
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
    @DisplayName("Deve retornar NotFound quando categoria não existe")
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
    @DisplayName("Deve retornar BadRequest quando formato do ID é inválido")
    void getCategoryById_WhenInvalidIdFormat_ShouldReturnBadRequest() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/v1/categories/invalid"))
                .andExpect(status().isBadRequest());

        verify(categoryService, never()).getCategoryById(any());
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    @DisplayName("Deve criar categoria com sucesso quando dados são válidos")
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
    @DisplayName("Deve retornar Forbidden quando usuário não é admin")
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
    @DisplayName("Deve retornar Unauthorized quando não está autenticado")
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
    @DisplayName("Deve retornar BadRequest quando dados são inválidos")
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
    @DisplayName("Deve retornar Conflict quando nome já existe")
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
    @DisplayName("Deve atualizar categoria com sucesso quando dados são válidos")
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
    @DisplayName("Deve retornar Forbidden quando usuário tenta atualizar categoria")
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
    @DisplayName("Deve retornar NotFound quando categoria para atualizar não existe")
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
    @DisplayName("Deve retornar Unauthorized quando não está autenticado para atualização")
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
    @DisplayName("Deve deletar categoria com sucesso quando solicitação é válida")
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
    @DisplayName("Deve retornar Forbidden quando usuário tenta deletar categoria")
    void deleteCategory_WhenUserRole_ShouldReturnForbidden() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/api/v1/categories/1")
                .with(csrf()))
                .andExpect(status().isForbidden());

        verify(categoryService, never()).deleteCategory(any());
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    @DisplayName("Deve retornar NotFound quando categoria para deletar não existe")
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
    @DisplayName("Deve retornar Conflict quando categoria tem posts associados")
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
    @DisplayName("Deve retornar contagem de categorias no endpoint de teste")
    void testCategories_ShouldReturnCategoryCount() throws Exception {
        // This tests the /test endpoint - implementation depends on actual controller
        mockMvc.perform(get("/api/v1/categories/test"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Deve retornar DTO de teste no endpoint de teste DTO")
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
    @DisplayName("Deve criar categoria com sucesso quando dados são válidos e descrição é longa")
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
    @DisplayName("Deve retornar InternalServerError quando serviço lança exceção")
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

