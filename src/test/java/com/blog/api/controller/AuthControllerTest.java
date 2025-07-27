package com.blog.api.controller;

import com.blog.api.dto.*;
import com.blog.api.entity.User;
import com.blog.api.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser
    void register_WhenValidInput_ShouldReturnCreated() throws Exception {
        CreateUserDTO createUserDTO = new CreateUserDTO("testuser", "test@example.com", "password123", User.Role.USER);
        UserDTO userDTO = new UserDTO(1L, "testuser", "test@example.com", User.Role.USER, LocalDateTime.now());

        when(authService.register(any(CreateUserDTO.class))).thenReturn(userDTO);

        mockMvc.perform(post("/api/v1/auth/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createUserDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.email").value("test@example.com"));
    }

    @Test
    @WithMockUser
    void login_WhenValidCredentials_ShouldReturnJwtResponse() throws Exception {
        LoginRequest loginRequest = new LoginRequest("testuser", "password123");
        UserDTO userDTO = new UserDTO(1L, "testuser", "test@example.com", User.Role.USER, LocalDateTime.now());
        JwtResponse jwtResponse = new JwtResponse("jwt-token", userDTO);

        when(authService.login(any(LoginRequest.class))).thenReturn(jwtResponse);

        mockMvc.perform(post("/api/v1/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token"))
                .andExpect(jsonPath("$.user.username").value("testuser"));
    }
}