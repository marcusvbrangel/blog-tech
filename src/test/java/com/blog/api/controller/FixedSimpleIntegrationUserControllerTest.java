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
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.domain.*;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = UserController.class)
@org.springframework.context.annotation.Import(com.blog.api.config.TestSecurityConfig.class)
@DisplayName("Testes de integração simples corrigidos do controlador de usuários")
class FixedSimpleIntegrationUserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    private UserDTO sampleUserDTO;

    @BeforeEach
    void setUp() {
        sampleUserDTO = new UserDTO(
                1L,
                "testuser",
                "test@example.com",
                User.Role.USER,
                LocalDateTime.of(2025, 8, 2, 10, 0),  // Fixed timestamp to avoid serialization issues
                true,
                LocalDateTime.of(2025, 8, 2, 10, 0),  // Fixed timestamp
                null,
                "1.0",
                true
        );
    }

    @Test
    @DisplayName("Deve retornar página de usuários quando solicitar todos os usuários")
    void getAllUsers_ShouldReturnPageOfUsers() throws Exception {
        // Arrange - Create a simpler Page implementation
        Pageable pageable = PageRequest.of(0, 10);
        List<UserDTO> content = new java.util.ArrayList<>(Arrays.asList(sampleUserDTO));
        Page<UserDTO> page = new PageImpl<>(content, pageable, 1);
        
        when(userService.getAllUsers(any(Pageable.class))).thenReturn(page);

        // Act & Assert
        mockMvc.perform(get("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].username").value("testuser"))
                .andExpect(jsonPath("$.content[0].email").value("test@example.com"));

        verify(userService).getAllUsers(any(Pageable.class));
    }

    @Test
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
                .andExpect(jsonPath("$.email").value("test@example.com"));

        verify(userService).getUserById(1L);
    }

    @Test
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
    @DisplayName("Deve retornar não encontrado quando buscar usuário por ID inexistente")
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
    @DisplayName("Deve retornar página vazia quando não houver usuários")
    void getAllUsers_ShouldReturnEmptyPage_WhenNoUsers() throws Exception {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<UserDTO> emptyPage = new PageImpl<>(new java.util.ArrayList<>(Arrays.asList()), pageable, 0);
        when(userService.getAllUsers(any(Pageable.class))).thenReturn(emptyPage);

        // Act & Assert
        mockMvc.perform(get("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content").isEmpty());

        verify(userService).getAllUsers(any(Pageable.class));
    }
}