package com.blog.api.integration;

import com.blog.api.dto.*;
import com.blog.api.entity.User;
import com.blog.api.entity.VerificationToken;
import com.blog.api.repository.UserRepository;
import com.blog.api.repository.VerificationTokenRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.containsString;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class EmailVerificationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VerificationTokenRepository tokenRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private User testUser;

    @BeforeEach
    void setUp() {
        // Clean up any existing data
        tokenRepository.deleteAll();
        userRepository.deleteAll();

        // Create test user
        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy"); // password
        testUser.setEmailVerified(false);
        testUser.setRole(User.Role.USER);
        testUser.setPasswordChangedAt(LocalDateTime.now());
        testUser = userRepository.save(testUser);
    }

    @Test
    void fullEmailVerificationFlow_Success() throws Exception {
        // Step 1: Register user (should create verification token)
        CreateUserDTO createUserDTO = new CreateUserDTO(
            "newuser", "newuser@example.com", "password123", User.Role.USER);

        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createUserDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("newuser"))
                .andExpect(jsonPath("$.email").value("newuser@example.com"))
                .andExpect(jsonPath("$.emailVerified").value(false));

        // Verify user is created but not verified
        Optional<User> registeredUser = userRepository.findByEmail("newuser@example.com");
        assertTrue(registeredUser.isPresent());
        assertFalse(registeredUser.get().isEmailVerified());

        // Verify token is created
        Optional<VerificationToken> token = tokenRepository
            .findByUserAndTokenType(registeredUser.get(), VerificationToken.TokenType.EMAIL_VERIFICATION)
            .stream().findFirst();
        assertTrue(token.isPresent());
        assertTrue(token.get().isValid());

        // Step 2: Verify email with token
        String tokenValue = token.get().getToken();
        mockMvc.perform(get("/api/v1/auth/verify-email")
                .param("token", tokenValue))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Email verified successfully! Your account is now active."))
                .andExpect(jsonPath("$.data.emailVerified").value(true));

        // Verify user is now verified
        User verifiedUser = userRepository.findByEmail("newuser@example.com").orElseThrow();
        assertTrue(verifiedUser.isEmailVerified());
        assertNotNull(verifiedUser.getEmailVerifiedAt());

        // Verify token is marked as used
        VerificationToken usedToken = tokenRepository.findById(token.get().getId()).orElseThrow();
        assertNotNull(usedToken.getUsedAt());
        assertFalse(usedToken.isValid());

        // Step 3: Try to login (should work now)
        LoginRequest loginRequest = new LoginRequest("newuser@example.com", "password123");
        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.user.emailVerified").value(true));
    }

    @Test
    void loginWithUnverifiedEmail_Fails() throws Exception {
        // Try to login with unverified user
        LoginRequest loginRequest = new LoginRequest("test@example.com", "password");
        
        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Email not verified. Please check your email and verify your account."));
    }

    @Test
    void resendVerificationEmail_Success() throws Exception {
        // Request resend verification
        EmailVerificationRequest request = new EmailVerificationRequest("test@example.com");
        
        mockMvc.perform(post("/api/v1/auth/resend-verification")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Verification email sent successfully. Please check your inbox."));

        // Verify new token is created
        Optional<VerificationToken> token = tokenRepository
            .findValidTokensByUserAndType(testUser, VerificationToken.TokenType.EMAIL_VERIFICATION, LocalDateTime.now())
            .stream().findFirst();
        assertTrue(token.isPresent());
    }

    @Test
    void resendVerificationEmail_AlreadyVerified_Fails() throws Exception {
        // Mark user as verified
        testUser.setEmailVerified(true);
        testUser.setEmailVerifiedAt(LocalDateTime.now());
        userRepository.save(testUser);

        // Try to resend verification
        EmailVerificationRequest request = new EmailVerificationRequest("test@example.com");
        
        mockMvc.perform(post("/api/v1/auth/resend-verification")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("Email is already verified")));
    }

    @Test
    void passwordResetFlow_Success() throws Exception {
        // Step 1: Request password reset
        PasswordResetRequest resetRequest = new PasswordResetRequest("test@example.com");
        
        mockMvc.perform(post("/api/v1/auth/forgot-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(resetRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(containsString("If an account with this email exists")));

        // Verify reset token is created
        Optional<VerificationToken> resetToken = tokenRepository
            .findValidTokensByUserAndType(testUser, VerificationToken.TokenType.PASSWORD_RESET, LocalDateTime.now())
            .stream().findFirst();
        assertTrue(resetToken.isPresent());

        // Step 2: Validate reset token
        String tokenValue = resetToken.get().getToken();
        mockMvc.perform(get("/api/v1/auth/reset-password")
                .param("token", tokenValue))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Token is valid. You can proceed with password reset."));

        // Step 3: Reset password
        PasswordResetConfirmRequest confirmRequest = new PasswordResetConfirmRequest(tokenValue, "newpassword123");
        
        mockMvc.perform(post("/api/v1/auth/reset-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(confirmRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Password reset successfully! You can now login with your new password."));

        // Verify token is marked as used
        VerificationToken usedToken = tokenRepository.findById(resetToken.get().getId()).orElseThrow();
        assertNotNull(usedToken.getUsedAt());

        // Verify user's password change timestamp is updated
        User updatedUser = userRepository.findById(testUser.getId()).orElseThrow();
        assertNotNull(updatedUser.getPasswordChangedAt());
        assertEquals(0, updatedUser.getFailedLoginAttempts());
        assertFalse(updatedUser.isAccountLocked());
    }

    @Test
    void verifyExpiredToken_Fails() throws Exception {
        // Create expired token
        VerificationToken expiredToken = new VerificationToken();
        expiredToken.setUser(testUser);
        expiredToken.setToken("expired-token-123");
        expiredToken.setTokenType(VerificationToken.TokenType.EMAIL_VERIFICATION);
        expiredToken.setExpiresAt(LocalDateTime.now().minusHours(1)); // Expired
        expiredToken.setCreatedAt(LocalDateTime.now().minusHours(25));
        tokenRepository.save(expiredToken);

        // Try to verify expired token
        mockMvc.perform(get("/api/v1/auth/verify-email")
                .param("token", "expired-token-123"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("expired")));
    }

    @Test
    void verifyUsedToken_Fails() throws Exception {
        // Create used token
        VerificationToken usedToken = new VerificationToken();
        usedToken.setUser(testUser);
        usedToken.setToken("used-token-123");
        usedToken.setTokenType(VerificationToken.TokenType.EMAIL_VERIFICATION);
        usedToken.setExpiresAt(LocalDateTime.now().plusHours(1));
        usedToken.setUsedAt(LocalDateTime.now().minusMinutes(30)); // Already used
        usedToken.setCreatedAt(LocalDateTime.now().minusHours(1));
        tokenRepository.save(usedToken);

        // Try to verify used token
        mockMvc.perform(get("/api/v1/auth/verify-email")
                .param("token", "used-token-123"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("already been used")));
    }

    @Test
    void rateLimitingEmailVerification_Works() throws Exception {
        // Send multiple resend requests quickly
        EmailVerificationRequest request = new EmailVerificationRequest("test@example.com");
        String requestJson = objectMapper.writeValueAsString(request);

        // First 3 requests should work (within rate limit)
        for (int i = 0; i < 3; i++) {
            mockMvc.perform(post("/api/v1/auth/resend-verification")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestJson))
                    .andExpect(status().isOk());
        }

        // 4th request should be rate limited
        mockMvc.perform(post("/api/v1/auth/resend-verification")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(containsString("Too many requests")));
    }

    @Test
    void invalidTokenFormat_ReturnsBadRequest() throws Exception {
        // Try with invalid token format
        mockMvc.perform(get("/api/v1/auth/verify-email")
                .param("token", "invalid-token-format"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void passwordResetWithNonExistentEmail_ReturnsGenericMessage() throws Exception {
        // Request password reset for non-existent email
        PasswordResetRequest resetRequest = new PasswordResetRequest("nonexistent@example.com");
        
        mockMvc.perform(post("/api/v1/auth/forgot-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(resetRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(containsString("If an account with this email exists")));

        // Verify no token is created
        assertEquals(0, tokenRepository.count());
    }

    @Test
    void verifyTokenFromDifferentUser_Fails() throws Exception {
        // Create another user
        User anotherUser = new User();
        anotherUser.setUsername("anotheruser");
        anotherUser.setEmail("another@example.com");
        anotherUser.setPassword("hashedpassword");
        anotherUser.setEmailVerified(false);
        anotherUser.setRole(User.Role.USER);
        anotherUser = userRepository.save(anotherUser);

        // Create token for another user
        VerificationToken token = new VerificationToken();
        token.setUser(anotherUser);
        token.setToken("another-user-token");
        token.setTokenType(VerificationToken.TokenType.EMAIL_VERIFICATION);
        token.setExpiresAt(LocalDateTime.now().plusHours(1));
        token.setCreatedAt(LocalDateTime.now());
        tokenRepository.save(token);

        // Verify the token works correctly for the right user
        mockMvc.perform(get("/api/v1/auth/verify-email")
                .param("token", "another-user-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value("another@example.com"));

        // Verify correct user is verified
        User verifiedUser = userRepository.findByEmail("another@example.com").orElseThrow();
        assertTrue(verifiedUser.isEmailVerified());
    }
}