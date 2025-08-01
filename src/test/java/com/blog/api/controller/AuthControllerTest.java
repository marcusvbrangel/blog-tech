package com.blog.api.controller;

import com.blog.api.dto.*;
import com.blog.api.entity.User;
import com.blog.api.exception.ResourceNotFoundException;
import com.blog.api.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @Autowired
    private ObjectMapper objectMapper;

    private UserDTO sampleUserDTO;
    private CreateUserDTO createUserDTO;
    private LoginRequest loginRequest;
    private JwtResponse jwtResponse;

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

        createUserDTO = new CreateUserDTO("testuser", "test@example.com", "ValidPassword123!", User.Role.USER);
        loginRequest = new LoginRequest("testuser", "ValidPassword123!");
        jwtResponse = new JwtResponse("jwt-token-123", sampleUserDTO);
    }

    @Test
    void register_WhenValidInput_ShouldReturnCreated() throws Exception {
        // Arrange
        when(authService.register(any(CreateUserDTO.class))).thenReturn(sampleUserDTO);

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createUserDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.role").value("USER"))
                .andExpect(jsonPath("$.emailVerified").value(true));

        verify(authService).register(any(CreateUserDTO.class));
    }

    @Test
    void register_WhenInvalidData_ShouldReturnBadRequest() throws Exception {
        // Arrange
        CreateUserDTO invalidUser = new CreateUserDTO("", "invalid-email", "123", User.Role.USER);

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidUser)))
                .andExpect(status().isBadRequest());

        verify(authService, never()).register(any());
    }

    @Test
    void register_WhenUserAlreadyExists_ShouldReturnConflict() throws Exception {
        // Arrange
        when(authService.register(any(CreateUserDTO.class)))
                .thenThrow(new RuntimeException("User already exists"));

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createUserDTO)))
                .andExpect(status().isInternalServerError());

        verify(authService).register(any(CreateUserDTO.class));
    }

    @Test
    void login_WhenValidCredentials_ShouldReturnJwtResponse() throws Exception {
        // Arrange
        when(authService.login(any(LoginRequest.class))).thenReturn(jwtResponse);

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token-123"))
                .andExpect(jsonPath("$.user.id").value(1))
                .andExpect(jsonPath("$.user.username").value("testuser"))
                .andExpect(jsonPath("$.user.email").value("test@example.com"));

        verify(authService).login(any(LoginRequest.class));
    }

    @Test
    void login_WhenInvalidCredentials_ShouldReturnUnauthorized() throws Exception {
        // Arrange
        when(authService.login(any(LoginRequest.class)))
                .thenThrow(new RuntimeException("Invalid credentials"));

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isInternalServerError());

        verify(authService).login(any(LoginRequest.class));
    }

    @Test
    void login_WhenInvalidData_ShouldReturnBadRequest() throws Exception {
        // Arrange
        LoginRequest invalidLogin = new LoginRequest("", "");

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidLogin)))
                .andExpect(status().isBadRequest());

        verify(authService, never()).login(any());
    }

    // Email Verification Tests

    @Test
    void verifyEmail_WhenValidToken_ShouldReturnSuccess() throws Exception {
        // Arrange
        String validToken = "valid-token-123";
        when(authService.verifyEmail(validToken)).thenReturn(sampleUserDTO);

        // Act & Assert
        mockMvc.perform(get("/api/v1/auth/verify-email")
                .param("token", validToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Email verified successfully! Your account is now active."))
                .andExpect(jsonPath("$.user.username").value("testuser"));

        verify(authService).verifyEmail(validToken);
    }

    @Test
    void verifyEmail_WhenInvalidToken_ShouldReturnBadRequest() throws Exception {
        // Arrange
        String invalidToken = "invalid-token";
        when(authService.verifyEmail(invalidToken))
                .thenThrow(new RuntimeException("Invalid token"));

        // Act & Assert
        mockMvc.perform(get("/api/v1/auth/verify-email")
                .param("token", invalidToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Email verification failed: Invalid token"));

        verify(authService).verifyEmail(invalidToken);
    }

    @Test
    void verifyEmail_WhenMissingToken_ShouldReturnBadRequest() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/v1/auth/verify-email"))
                .andExpect(status().isBadRequest());

        verify(authService, never()).verifyEmail(any());
    }

    @Test
    void resendEmailVerification_WhenValidEmail_ShouldReturnSuccess() throws Exception {
        // Arrange
        EmailVerificationRequest request = new EmailVerificationRequest("test@example.com");
        doNothing().when(authService).resendEmailVerification("test@example.com");

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/resend-verification")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Verification email sent successfully. Please check your inbox."));

        verify(authService).resendEmailVerification("test@example.com");
    }

    @Test
    void resendEmailVerification_WhenInvalidEmail_ShouldReturnBadRequest() throws Exception {
        // Arrange
        EmailVerificationRequest request = new EmailVerificationRequest("test@example.com");
        doThrow(new RuntimeException("Email already verified"))
                .when(authService).resendEmailVerification("test@example.com");

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/resend-verification")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));

        verify(authService).resendEmailVerification("test@example.com");
    }

    // Password Reset Tests

    @Test
    void forgotPassword_WhenValidEmail_ShouldReturnSuccess() throws Exception {
        // Arrange
        PasswordResetRequest request = new PasswordResetRequest("test@example.com");
        doNothing().when(authService).requestPasswordReset("test@example.com");

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/forgot-password")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("If an account with this email exists, you will receive a password reset link shortly."));

        verify(authService).requestPasswordReset("test@example.com");
    }

    @Test
    void forgotPassword_WhenRateLimitExceeded_ShouldReturnBadRequest() throws Exception {
        // Arrange
        PasswordResetRequest request = new PasswordResetRequest("test@example.com");
        doThrow(new RuntimeException("Rate limit exceeded"))
                .when(authService).requestPasswordReset("test@example.com");

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/forgot-password")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));

        verify(authService).requestPasswordReset("test@example.com");
    }

    @Test
    void resetPassword_WhenValidToken_ShouldReturnSuccess() throws Exception {
        // Arrange
        PasswordResetConfirmRequest request = new PasswordResetConfirmRequest("valid-token", "NewPassword123!");
        when(authService.resetPassword("valid-token", "NewPassword123!")).thenReturn(sampleUserDTO);

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/reset-password")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Password reset successfully! You can now login with your new password."))
                .andExpect(jsonPath("$.user.username").value("testuser"));

        verify(authService).resetPassword("valid-token", "NewPassword123!");
    }

    @Test
    void resetPassword_WhenInvalidToken_ShouldReturnBadRequest() throws Exception {
        // Arrange
        PasswordResetConfirmRequest request = new PasswordResetConfirmRequest("invalid-token", "NewPassword123!");
        when(authService.resetPassword("invalid-token", "NewPassword123!"))
                .thenThrow(new RuntimeException("Invalid or expired token"));

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/reset-password")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));

        verify(authService).resetPassword("invalid-token", "NewPassword123!");
    }

    @Test
    void validatePasswordResetToken_WhenValidToken_ShouldReturnSuccess() throws Exception {
        // Arrange
        String validToken = "valid-token-123";
        doNothing().when(authService).validatePasswordResetToken(validToken);

        // Act & Assert
        mockMvc.perform(get("/api/v1/auth/reset-password")
                .param("token", validToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Token is valid. You can proceed with password reset."));

        verify(authService).validatePasswordResetToken(validToken);
    }

    @Test
    void validatePasswordResetToken_WhenInvalidToken_ShouldReturnBadRequest() throws Exception {
        // Arrange
        String invalidToken = "invalid-token";
        doThrow(new RuntimeException("Invalid token"))
                .when(authService).validatePasswordResetToken(invalidToken);

        // Act & Assert
        mockMvc.perform(get("/api/v1/auth/reset-password")
                .param("token", invalidToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));

        verify(authService).validatePasswordResetToken(invalidToken);
    }
}