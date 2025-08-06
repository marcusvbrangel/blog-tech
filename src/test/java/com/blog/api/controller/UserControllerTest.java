package com.blog.api.controller;

import com.blog.api.dto.UserDTO;
import com.blog.api.entity.User;
import com.blog.api.exception.ResourceNotFoundException;
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
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UserController.class, 
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
@DisplayName("User Controller Tests")
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;
    
    @MockBean
    private com.blog.api.util.JwtUtil jwtUtil;
    
    @MockBean  
    private com.blog.api.service.CustomUserDetailsService userDetailsService;
    
    @MockBean
    private com.blog.api.service.TermsService termsService;

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
        Page<UserDTO> page = new PageImpl<>(new java.util.ArrayList<>(Arrays.asList(sampleUserDTO)));
        when(userService.getAllUsers(any())).thenReturn(page);

        // Act & Assert
        mockMvc.perform(get("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].username").value("testuser"))
                .andExpect(jsonPath("$.content[0].email").value("test@example.com"))
                .andExpect(jsonPath("$.content[0].role").value("USER"));

        verify(userService).getAllUsers(any());
    }

    @Test
    @DisplayName("Deve retornar unauthorized quando não está autenticado")
    void getAllUsers_ShouldReturnUnauthorized_WhenNotAuthenticated() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());

        verify(userService, never()).getAllUsers(any());
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
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.email").value("test@example.com"));

        verify(userService).getUserById(1L);
    }

    @Test
    @WithMockUser
    @DisplayName("Deve retornar NotFound quando buscar usuário por ID inexistente")
    void getUserById_ShouldReturnNotFound_WhenNotExists() throws Exception {
        // Arrange
        when(userService.getUserById(999L)).thenThrow(new ResourceNotFoundException("User", "id", 999L));

        // Act & Assert
        mockMvc.perform(get("/api/v1/users/999")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(userService).getUserById(999L);
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
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.email").value("test@example.com"));

        verify(userService).getUserByUsername("testuser");
    }

    @Test
    @WithMockUser
    @DisplayName("Deve retornar NotFound quando buscar usuário por nome inexistente")
    void getUserByUsername_ShouldReturnNotFound_WhenNotExists() throws Exception {
        // Arrange
        when(userService.getUserByUsername("nonexistent")).thenThrow(new ResourceNotFoundException("User", "username", "nonexistent"));

        // Act & Assert
        mockMvc.perform(get("/api/v1/users/username/nonexistent")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(userService).getUserByUsername("nonexistent");
    }

    @Test
    @DisplayName("Deve retornar unauthorized quando não está autenticado para busca por username")
    void getUserByUsername_ShouldReturnUnauthorized_WhenNotAuthenticated() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/v1/users/username/testuser")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());

        verify(userService, never()).getUserByUsername(any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Deve deletar usuário quando é admin")
    void deleteUser_ShouldDeleteUser_WhenAdmin() throws Exception {
        // Arrange
        doNothing().when(userService).deleteUser(1L);

        // Act & Assert
        mockMvc.perform(delete("/api/v1/users/1")
                .with(csrf()))
                .andExpect(status().isNoContent());

        verify(userService).deleteUser(1L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Deve retornar NotFound quando usuário a deletar não existe")
    void deleteUser_ShouldReturnNotFound_WhenUserNotExists() throws Exception {
        // Arrange
        doThrow(new ResourceNotFoundException("User", "id", 999L))
                .when(userService).deleteUser(999L);

        // Act & Assert
        mockMvc.perform(delete("/api/v1/users/999")
                .with(csrf()))
                .andExpect(status().isNotFound());

        verify(userService).deleteUser(999L);
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("Deve retornar forbidden quando não é admin")
    void deleteUser_ShouldReturnForbidden_WhenNotAdmin() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/api/v1/users/1")
                .with(csrf()))
                .andExpect(status().isForbidden());

        verify(userService, never()).deleteUser(any());
    }

    @Test
    @DisplayName("Deve retornar unauthorized quando não está autenticado para deletar")
    void deleteUser_ShouldReturnUnauthorized_WhenNotAuthenticated() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/api/v1/users/1")
                .with(csrf()))
                .andExpect(status().isUnauthorized());

        verify(userService, never()).deleteUser(any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Deve retornar página vazia quando não há usuários")
    void getAllUsers_ShouldReturnEmptyPage_WhenNoUsers() throws Exception {
        // Arrange
        Page<UserDTO> emptyPage = new PageImpl<>(new java.util.ArrayList<>());
        when(userService.getAllUsers(any())).thenReturn(emptyPage);

        // Act & Assert
        mockMvc.perform(get("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
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
        Page<UserDTO> page = new PageImpl<>(new java.util.ArrayList<>(Arrays.asList(sampleUserDTO)));
        when(userService.getAllUsers(any())).thenReturn(page);

        // Act & Assert
        mockMvc.perform(get("/api/v1/users")
                .param("page", "0")
                .param("size", "10")
                .param("sort", "username,asc")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].username").value("testuser"))
                .andExpect(jsonPath("$.pageable").exists());

        verify(userService).getAllUsers(any());
    }

    @Test
    @WithMockUser
    @DisplayName("Deve lidar com formatos de ID válidos")
    void getUserById_ShouldHandleValidIdFormats() throws Exception {
        // Arrange
        when(userService.getUserById(123L)).thenReturn(sampleUserDTO);

        // Act & Assert
        mockMvc.perform(get("/api/v1/users/123")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("testuser"));

        verify(userService).getUserById(123L);
    }

    @Test
    @WithMockUser
    @DisplayName("Deve retornar bad request quando formato de ID é inválido")
    void getUserById_ShouldReturnBadRequest_WhenInvalidIdFormat() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/v1/users/invalid")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(userService, never()).getUserById(any());
    }

    @Test
    @WithMockUser
    @DisplayName("Deve lidar com caracteres especiais no nome de usuário")
    void getUserByUsername_ShouldHandleSpecialCharactersInUsername() throws Exception {
        // Arrange
        String specialUsername = "test.user-123";
        UserDTO userWithSpecialName = new UserDTO(2L, specialUsername, "special@example.com", 
                User.Role.USER, LocalDateTime.now(), true, LocalDateTime.now(), null, "1.0", true);
        when(userService.getUserByUsername(specialUsername)).thenReturn(userWithSpecialName);

        // Act & Assert
        mockMvc.perform(get("/api/v1/users/username/" + specialUsername)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(specialUsername));

        verify(userService).getUserByUsername(specialUsername);
    }
}