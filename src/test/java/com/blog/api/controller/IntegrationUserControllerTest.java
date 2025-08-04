package com.blog.api.controller;

import com.blog.api.config.TestSecurityConfig;
import com.blog.api.dto.UserDTO;
import com.blog.api.entity.User;
import com.blog.api.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
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

@WebMvcTest(UserController.class)
@Import(TestSecurityConfig.class)
@TestPropertySource(properties = {
    "spring.security.user.name=testuser",
    "spring.security.user.password=password",
    "spring.security.user.roles=ADMIN"
})
@DisplayName("Testes de integração do controlador de usuários")
class IntegrationUserControllerTest {

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
    @DisplayName("Deve retornar página de usuários quando usuário admin solicitar todos os usuários")
    void getAllUsers_ShouldReturnPageOfUsers() throws Exception {
        // Arrange
        Page<UserDTO> page = new PageImpl<>(Arrays.asList(sampleUserDTO));
        when(userService.getAllUsers(any())).thenReturn(page);

        // Act & Assert
        mockMvc.perform(get("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].username").value("testuser"))
                .andExpect(jsonPath("$.content[0].email").value("test@example.com"));

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
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.email").value("test@example.com"));

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
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.email").value("test@example.com"));

        verify(userService).getUserByUsername("testuser");
    }

    @Test
    @DisplayName("Deve retornar não autorizado quando buscar usuário por ID sem autenticação")
    void getUserById_ShouldReturnUnauthorized_WhenNotAuthenticated() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/v1/users/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Deve retornar não autorizado quando buscar todos os usuários sem autenticação")
    void getAllUsers_ShouldReturnUnauthorized_WhenNotAuthenticated() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }
}