package com.blog.api.controller;

import com.blog.api.dto.UserDTO;
import com.blog.api.entity.User;
import com.blog.api.service.UserService;
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
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = UserController.class)
@TestPropertySource(properties = {
    "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration",
    "spring.main.allow-bean-definition-overriding=true"
})
@DisplayName("Working User Controller Tests")
class WorkingUserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    // Mock all the problematic dependencies
    @MockBean
    private com.blog.api.util.JwtUtil jwtUtil;
    
    @MockBean  
    private com.blog.api.service.CustomUserDetailsService userDetailsService;
    
    @MockBean
    private com.blog.api.service.TermsService termsService;
    
    @MockBean
    private com.blog.api.config.JwtAuthenticationFilter jwtAuthenticationFilter;
    
    @MockBean
    private com.blog.api.config.TermsComplianceFilter termsComplianceFilter;

    private UserDTO sampleUserDTO;

    @BeforeEach
    void setUp() {
        sampleUserDTO = new UserDTO(
                1L,
                "testuser",
                "test@example.com",
                User.Role.USER,
                LocalDateTime.now(),
                true,
                LocalDateTime.now(),
                null,
                "1.0",
                true
        );
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Deve retornar página de usuários quando buscar todos os usuários")
    void getAllUsers_ShouldReturnPageOfUsers() throws Exception {
        // Arrange
        Page<UserDTO> page = new PageImpl<>(Arrays.asList(sampleUserDTO));
        when(userService.getAllUsers(any())).thenReturn(page);

        // Act & Assert
        mockMvc.perform(get("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].username").value("testuser"))
                .andExpect(jsonPath("$.content[0].email").value("test@example.com"))
                .andExpect(jsonPath("$.content[0].role").value("USER"));

        verify(userService).getAllUsers(any());
    }

    @Test
    @WithMockUser
    @DisplayName("Deve retornar usuário quando buscar por ID existente")
    void getUserById_ShouldReturnUser_WhenExists() throws Exception {
        // Arrange
        when(userService.getUserById(1L)).thenReturn(sampleUserDTO);

        // Act & Assert
        mockMvc.perform(get("/api/v1/users/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.role").value("USER"));

        verify(userService).getUserById(1L);
    }

    @Test
    @WithMockUser
    @DisplayName("Deve retornar usuário quando buscar por nome de usuário existente")
    void getUserByUsername_ShouldReturnUser_WhenExists() throws Exception {
        // Arrange
        when(userService.getUserByUsername("testuser")).thenReturn(sampleUserDTO);

        // Act & Assert
        mockMvc.perform(get("/api/v1/users/username/testuser")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.email").value("test@example.com"));

        verify(userService).getUserByUsername("testuser");
    }

    @Test
    @WithMockUser
    @DisplayName("Deve retornar NotFound quando buscar usuário por ID inexistente")
    void getUserById_ShouldReturnNotFound_WhenNotExists() throws Exception {
        // Arrange
        when(userService.getUserById(999L))
                .thenThrow(new com.blog.api.exception.ResourceNotFoundException("User", "id", 999L));

        // Act & Assert
        mockMvc.perform(get("/api/v1/users/999")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(userService).getUserById(999L);
    }

    @Test
    @WithMockUser
    @DisplayName("Deve retornar NotFound quando buscar usuário por nome inexistente")
    void getUserByUsername_ShouldReturnNotFound_WhenNotExists() throws Exception {
        // Arrange
        when(userService.getUserByUsername("nonexistent"))
                .thenThrow(new com.blog.api.exception.ResourceNotFoundException("User", "username", "nonexistent"));

        // Act & Assert
        mockMvc.perform(get("/api/v1/users/username/nonexistent")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(userService).getUserByUsername("nonexistent");
    }

    @Test
    @WithMockUser(roles = "ADMIN") 
    @DisplayName("Deve retornar página vazia quando não há usuários")
    void getAllUsers_ShouldReturnEmptyPage_WhenNoUsers() throws Exception {
        // Arrange
        Page<UserDTO> emptyPage = new PageImpl<>(Arrays.asList());
        when(userService.getAllUsers(any())).thenReturn(emptyPage);

        // Act & Assert
        mockMvc.perform(get("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content").isEmpty())
                .andExpect(jsonPath("$.totalElements").value(0));

        verify(userService).getAllUsers(any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Deve lidar com paginação corretamente")
    void getAllUsers_ShouldHandlePagination() throws Exception {
        // Arrange
        Page<UserDTO> page = new PageImpl<>(Arrays.asList(sampleUserDTO));
        when(userService.getAllUsers(any())).thenReturn(page);

        // Act & Assert
        mockMvc.perform(get("/api/v1/users")
                .param("page", "0")
                .param("size", "10")
                .param("sort", "username,asc")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].username").value("testuser"))
                .andExpect(jsonPath("$.pageable").exists());

        verify(userService).getAllUsers(any());
    }
}