package com.blog.api.controller;

import com.blog.api.dto.EmailVerificationRequest;
import com.blog.api.dto.PasswordResetConfirmRequest;
import com.blog.api.dto.PasswordResetRequest;
import com.blog.api.dto.UserDTO;
import com.blog.api.entity.User;
import com.blog.api.exception.BadRequestException;
import com.blog.api.exception.ResourceNotFoundException;
import com.blog.api.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {AuthController.class, com.blog.api.exception.GlobalExceptionHandler.class}, excludeAutoConfiguration = {
    org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class,
    org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration.class,
    org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration.class
}, excludeFilters = @org.springframework.context.annotation.ComponentScan.Filter(
    type = org.springframework.context.annotation.FilterType.ASSIGNABLE_TYPE,
    classes = {
        com.blog.api.config.JwtAuthenticationFilter.class,
        com.blog.api.config.SecurityConfig.class,
        com.blog.api.util.JwtUtil.class,
        com.blog.api.service.CustomUserDetailsService.class
    }
))
@DisplayName("Auth Controller Email Verification Tests")
class AuthControllerEmailVerificationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @Autowired
    private ObjectMapper objectMapper;

    private UserDTO testUserDTO;

    @BeforeEach
    void setUp() {
        testUserDTO = new UserDTO(
            1L,
            "testuser",
            "test@example.com",
            User.Role.USER,
            LocalDateTime.now(),
            true,
            LocalDateTime.now(),
            LocalDateTime.now(),
            "v1.0",
            true
        );
    }

    @Test
    @DisplayName("Deve verificar email com sucesso quando token é válido")
    void verifyEmail_Success() throws Exception {
        // Given
        String token = "verification-token-123";
        when(authService.verifyEmail(token)).thenReturn(testUserDTO);

        // When & Then
        mockMvc.perform(get("/api/v1/auth/verify-email")
                .param("token", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Email verified successfully! Your account is now active."))
                .andExpect(jsonPath("$.data.username").value("testuser"))
                .andExpect(jsonPath("$.data.email").value("test@example.com"))
                .andExpect(jsonPath("$.data.emailVerified").value(true));

        verify(authService).verifyEmail(token);
    }

    @Test
    @DisplayName("Deve retornar BadRequest quando token é inválido")
    void verifyEmail_InvalidToken_ReturnsBadRequest() throws Exception {
        // Given
        String token = "invalid-token";
        when(authService.verifyEmail(token))
            .thenThrow(new BadRequestException("Invalid or expired verification token"));

        // When & Then
        mockMvc.perform(get("/api/v1/auth/verify-email")
                .param("token", token))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Email verification failed: Invalid or expired verification token"));

        verify(authService).verifyEmail(token);
    }

    @Test
    @DisplayName("Deve retornar BadRequest quando token está ausente")
    void verifyEmail_MissingToken_ReturnsBadRequest() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/auth/verify-email"))
                .andExpect(status().isBadRequest());

        verify(authService, never()).verifyEmail(any());
    }

    @Test
    @DisplayName("Deve reenviar email de verificação com sucesso")
    void resendEmailVerification_Success() throws Exception {
        // Given
        EmailVerificationRequest request = new EmailVerificationRequest("test@example.com");
        doNothing().when(authService).resendEmailVerification("test@example.com");

        // When & Then
        mockMvc.perform(post("/api/v1/auth/resend-verification")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Verification email sent successfully. Please check your inbox."));

        verify(authService).resendEmailVerification("test@example.com");
    }

    @Test
    @DisplayName("Deve retornar BadRequest quando usuário não é encontrado para reenvio")
    void resendEmailVerification_UserNotFound_ReturnsBadRequest() throws Exception {
        // Given
        EmailVerificationRequest request = new EmailVerificationRequest("nonexistent@example.com");
        doThrow(new ResourceNotFoundException("User not found"))
            .when(authService).resendEmailVerification("nonexistent@example.com");

        // When & Then
        mockMvc.perform(post("/api/v1/auth/resend-verification")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("Failed to send verification email")));

        verify(authService).resendEmailVerification("nonexistent@example.com");
    }

    @Test
    @DisplayName("Deve retornar BadRequest quando email já está verificado")
    void resendEmailVerification_AlreadyVerified_ReturnsBadRequest() throws Exception {
        // Given
        EmailVerificationRequest request = new EmailVerificationRequest("test@example.com");
        doThrow(new BadRequestException("Email is already verified"))
            .when(authService).resendEmailVerification("test@example.com");

        // When & Then
        mockMvc.perform(post("/api/v1/auth/resend-verification")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("Email is already verified")));

        verify(authService).resendEmailVerification("test@example.com");
    }

    @Test
    @DisplayName("Deve retornar BadRequest quando email é inválido")
    void resendEmailVerification_InvalidEmail_ReturnsBadRequest() throws Exception {
        // Given
        EmailVerificationRequest request = new EmailVerificationRequest("invalid-email");

        // When & Then
        mockMvc.perform(post("/api/v1/auth/resend-verification")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(authService, never()).resendEmailVerification(any());
    }

    @Test
    @DisplayName("Deve processar solicitação de esqueci minha senha com sucesso")
    void forgotPassword_Success() throws Exception {
        // Given
        PasswordResetRequest request = new PasswordResetRequest("test@example.com");
        doNothing().when(authService).requestPasswordReset("test@example.com");

        // When & Then
        mockMvc.perform(post("/api/v1/auth/forgot-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("If an account with this email exists, you will receive a password reset link shortly."));

        verify(authService).requestPasswordReset("test@example.com");
    }

    @Test
    @DisplayName("Deve retornar BadRequest quando email é inválido para reset de senha")
    void forgotPassword_InvalidEmail_ReturnsBadRequest() throws Exception {
        // Given
        PasswordResetRequest request = new PasswordResetRequest("invalid-email");

        // When & Then
        mockMvc.perform(post("/api/v1/auth/forgot-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(authService, never()).requestPasswordReset(any());
    }

    @Test
    @DisplayName("Deve retornar BadRequest quando limite de taxa é excedido")
    void forgotPassword_RateLimited_ReturnsBadRequest() throws Exception {
        // Given
        PasswordResetRequest request = new PasswordResetRequest("test@example.com");
        doThrow(new BadRequestException("Too many requests. Try again later."))
            .when(authService).requestPasswordReset("test@example.com");

        // When & Then
        mockMvc.perform(post("/api/v1/auth/forgot-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("Too many requests")));

        verify(authService).requestPasswordReset("test@example.com");
    }

    @Test
    @DisplayName("Deve resetar senha com sucesso quando token é válido")
    void resetPassword_Success() throws Exception {
        // Given
        PasswordResetConfirmRequest request = new PasswordResetConfirmRequest("reset-token-123", "newpassword");
        when(authService.resetPassword("reset-token-123", "newpassword")).thenReturn(testUserDTO);

        // When & Then
        mockMvc.perform(post("/api/v1/auth/reset-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Password reset successfully! You can now login with your new password."))
                .andExpect(jsonPath("$.data.username").value("testuser"));

        verify(authService).resetPassword("reset-token-123", "newpassword");
    }

    @Test
    @DisplayName("Deve retornar BadRequest quando token de reset é inválido")
    void resetPassword_InvalidToken_ReturnsBadRequest() throws Exception {
        // Given
        PasswordResetConfirmRequest request = new PasswordResetConfirmRequest("invalid-token", "newpassword");
        when(authService.resetPassword("invalid-token", "newpassword"))
            .thenThrow(new BadRequestException("Invalid or expired token"));

        // When & Then
        mockMvc.perform(post("/api/v1/auth/reset-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("Invalid or expired token")));

        verify(authService).resetPassword("invalid-token", "newpassword");
    }

    @Test
    @DisplayName("Deve retornar BadRequest quando nova senha é inválida")
    void resetPassword_InvalidPassword_ReturnsBadRequest() throws Exception {
        // Given
        PasswordResetConfirmRequest request = new PasswordResetConfirmRequest("reset-token-123", "123"); // Too short

        // When & Then
        mockMvc.perform(post("/api/v1/auth/reset-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(authService, never()).resetPassword(any(), any());
    }

    @Test
    @DisplayName("Deve validar token de reset de senha com sucesso")
    void validatePasswordResetToken_Success() throws Exception {
        // Given
        String token = "reset-token-123";
        when(authService.validatePasswordResetToken(token)).thenReturn(testUserDTO);

        // When & Then
        mockMvc.perform(get("/api/v1/auth/reset-password")
                .param("token", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Token is valid. You can proceed with password reset."));

        verify(authService).validatePasswordResetToken(token);
    }

    @Test
    @DisplayName("Deve retornar BadRequest quando token de validação é inválido")
    void validatePasswordResetToken_InvalidToken_ReturnsBadRequest() throws Exception {
        // Given
        String token = "invalid-token";
        when(authService.validatePasswordResetToken(token))
            .thenThrow(new BadRequestException("Invalid or expired token"));

        // When & Then
        mockMvc.perform(get("/api/v1/auth/reset-password")
                .param("token", token))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("Invalid or expired token")));

        verify(authService).validatePasswordResetToken(token);
    }

    @Test
    @DisplayName("Deve retornar BadRequest quando token de validação está ausente")
    void validatePasswordResetToken_MissingToken_ReturnsBadRequest() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/auth/reset-password"))
                .andExpect(status().isBadRequest());

        verify(authService, never()).validatePasswordResetToken(any());
    }

    // Test for request validation
    @Test
    @DisplayName("Deve retornar BadRequest quando request está vazio para reenvio de verificação")
    void resendEmailVerification_EmptyRequest_ReturnsBadRequest() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/v1/auth/resend-verification")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isBadRequest());

        verify(authService, never()).resendEmailVerification(any());
    }

    @Test
    @DisplayName("Deve retornar BadRequest quando request está vazio para esqueci minha senha")
    void forgotPassword_EmptyRequest_ReturnsBadRequest() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/v1/auth/forgot-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isBadRequest());

        verify(authService, never()).requestPasswordReset(any());
    }

    @Test
    @DisplayName("Deve retornar BadRequest quando request está vazio para reset de senha")
    void resetPassword_EmptyRequest_ReturnsBadRequest() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/v1/auth/reset-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isBadRequest());

        verify(authService, never()).resetPassword(any(), any());
    }
}

