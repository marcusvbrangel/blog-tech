package com.blog.api.service;

import com.blog.api.entity.User;
import com.blog.api.entity.VerificationToken;
import com.blog.api.exception.BadRequestException;
import com.blog.api.exception.ResourceNotFoundException;
import com.blog.api.repository.UserRepository;
import com.blog.api.repository.VerificationTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VerificationTokenServiceTest {

    @Mock
    private VerificationTokenRepository tokenRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private VerificationTokenService verificationTokenService;

    private User testUser;
    private VerificationToken testToken;

    @BeforeEach
    void setUp() {
        // Set up test configuration values
        ReflectionTestUtils.setField(verificationTokenService, "emailVerificationExpiration", Duration.ofHours(24));
        ReflectionTestUtils.setField(verificationTokenService, "passwordResetExpiration", Duration.ofMinutes(15));
        ReflectionTestUtils.setField(verificationTokenService, "maxEmailVerificationAttemptsPerHour", 3);
        ReflectionTestUtils.setField(verificationTokenService, "maxPasswordResetAttemptsPerHour", 5);

        // Create test user
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setEmailVerified(false);

        // Create test token
        testToken = new VerificationToken();
        testToken.setId(1L);
        testToken.setUser(testUser);
        testToken.setToken("test-token-123");
        testToken.setTokenType(VerificationToken.TokenType.EMAIL_VERIFICATION);
        testToken.setExpiresAt(LocalDateTime.now().plusHours(24));
        testToken.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void generateAndSendEmailVerification_Success() {
        // Given
        when(tokenRepository.countTokensCreatedSince(eq(testUser), eq(VerificationToken.TokenType.EMAIL_VERIFICATION), any()))
                .thenReturn(0L);
        when(tokenRepository.findValidTokensByUserAndType(eq(testUser), eq(VerificationToken.TokenType.EMAIL_VERIFICATION), any()))
                .thenReturn(java.util.List.of());
        when(tokenRepository.save(any(VerificationToken.class))).thenReturn(testToken);

        // When
        verificationTokenService.generateAndSendEmailVerification(testUser);

        // Then
        verify(tokenRepository).save(any(VerificationToken.class));
        verify(emailService).sendEmailVerification(eq(testUser), anyString());
    }

    @Test
    void generateAndSendEmailVerification_AlreadyVerified_ThrowsException() {
        // Given
        testUser.setEmailVerified(true);

        // When & Then
        assertThrows(BadRequestException.class, () -> 
            verificationTokenService.generateAndSendEmailVerification(testUser));

        verify(tokenRepository, never()).save(any());
        verify(emailService, never()).sendEmailVerification(any(), any());
    }

    @Test
    void generateAndSendEmailVerification_RateLimited_ThrowsException() {
        // Given
        when(tokenRepository.countTokensCreatedSince(eq(testUser), eq(VerificationToken.TokenType.EMAIL_VERIFICATION), any()))
                .thenReturn(5L); // Exceeds limit of 3

        // When & Then
        assertThrows(BadRequestException.class, () -> 
            verificationTokenService.generateAndSendEmailVerification(testUser));

        verify(tokenRepository, never()).save(any());
        verify(emailService, never()).sendEmailVerification(any(), any());
    }

    @Test
    void generateAndSendPasswordReset_Success() {
        // Given
        String email = "test@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(testUser));
        when(tokenRepository.countTokensCreatedSince(eq(testUser), eq(VerificationToken.TokenType.PASSWORD_RESET), any()))
                .thenReturn(0L);
        when(tokenRepository.findValidTokensByUserAndType(eq(testUser), eq(VerificationToken.TokenType.PASSWORD_RESET), any()))
                .thenReturn(java.util.List.of());
        when(tokenRepository.save(any(VerificationToken.class))).thenReturn(testToken);

        // When
        verificationTokenService.generateAndSendPasswordReset(email);

        // Then
        verify(tokenRepository).save(any(VerificationToken.class));
        verify(emailService).sendPasswordReset(eq(testUser), anyString());
    }

    @Test
    void generateAndSendPasswordReset_UserNotFound_ThrowsException() {
        // Given
        String email = "nonexistent@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> 
            verificationTokenService.generateAndSendPasswordReset(email));

        verify(tokenRepository, never()).save(any());
        verify(emailService, never()).sendPasswordReset(any(), any());
    }

    @Test
    void verifyEmailToken_Success() {
        // Given
        String tokenValue = "test-token-123";
        testToken.setExpiresAt(LocalDateTime.now().plusHours(1)); // Not expired
        
        when(tokenRepository.findByTokenAndTokenType(tokenValue, VerificationToken.TokenType.EMAIL_VERIFICATION))
                .thenReturn(Optional.of(testToken));
        when(tokenRepository.save(testToken)).thenReturn(testToken);
        when(userRepository.save(testUser)).thenReturn(testUser);

        // When
        User result = verificationTokenService.verifyEmailToken(tokenValue);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmailVerified());
        assertNotNull(result.getEmailVerifiedAt());
        assertNotNull(testToken.getUsedAt());
        
        verify(tokenRepository).save(testToken);
        verify(userRepository).save(testUser);
        verify(emailService).sendWelcomeEmail(testUser);
    }

    @Test
    void verifyEmailToken_TokenNotFound_ThrowsException() {
        // Given
        String tokenValue = "invalid-token";
        when(tokenRepository.findByTokenAndTokenType(tokenValue, VerificationToken.TokenType.EMAIL_VERIFICATION))
                .thenReturn(Optional.empty());

        // When & Then
        assertThrows(BadRequestException.class, () -> 
            verificationTokenService.verifyEmailToken(tokenValue));

        verify(userRepository, never()).save(any());
        verify(emailService, never()).sendWelcomeEmail(any());
    }

    @Test
    void verifyEmailToken_TokenExpired_ThrowsException() {
        // Given
        String tokenValue = "expired-token";
        testToken.setExpiresAt(LocalDateTime.now().minusHours(1)); // Expired
        
        when(tokenRepository.findByTokenAndTokenType(tokenValue, VerificationToken.TokenType.EMAIL_VERIFICATION))
                .thenReturn(Optional.of(testToken));

        // When & Then
        assertThrows(BadRequestException.class, () -> 
            verificationTokenService.verifyEmailToken(tokenValue));

        verify(userRepository, never()).save(any());
        verify(emailService, never()).sendWelcomeEmail(any());
    }

    @Test
    void verifyEmailToken_TokenAlreadyUsed_ThrowsException() {
        // Given
        String tokenValue = "used-token";
        testToken.setUsedAt(LocalDateTime.now().minusMinutes(30)); // Already used
        
        when(tokenRepository.findByTokenAndTokenType(tokenValue, VerificationToken.TokenType.EMAIL_VERIFICATION))
                .thenReturn(Optional.of(testToken));

        // When & Then
        assertThrows(BadRequestException.class, () -> 
            verificationTokenService.verifyEmailToken(tokenValue));

        verify(userRepository, never()).save(any());
        verify(emailService, never()).sendWelcomeEmail(any());
    }

    @Test
    void verifyPasswordResetToken_Success() {
        // Given
        String tokenValue = "reset-token-123";
        testToken.setTokenType(VerificationToken.TokenType.PASSWORD_RESET);
        testToken.setExpiresAt(LocalDateTime.now().plusMinutes(10)); // Not expired
        
        when(tokenRepository.findByTokenAndTokenType(tokenValue, VerificationToken.TokenType.PASSWORD_RESET))
                .thenReturn(Optional.of(testToken));

        // When
        User result = verificationTokenService.verifyPasswordResetToken(tokenValue);

        // Then
        assertNotNull(result);
        assertEquals(testUser, result);
    }

    @Test
    void markPasswordResetTokenAsUsed_Success() {
        // Given
        String tokenValue = "reset-token-123";
        testToken.setTokenType(VerificationToken.TokenType.PASSWORD_RESET);
        testToken.setExpiresAt(LocalDateTime.now().plusMinutes(10)); // Not expired
        
        when(tokenRepository.findByTokenAndTokenType(tokenValue, VerificationToken.TokenType.PASSWORD_RESET))
                .thenReturn(Optional.of(testToken));
        when(tokenRepository.save(testToken)).thenReturn(testToken);

        // When
        verificationTokenService.markPasswordResetTokenAsUsed(tokenValue);

        // Then
        assertNotNull(testToken.getUsedAt());
        verify(tokenRepository).save(testToken);
    }

    @Test
    void canRequestNewToken_WithinLimit_ReturnsTrue() {
        // Given
        when(tokenRepository.countTokensCreatedSince(eq(testUser), eq(VerificationToken.TokenType.EMAIL_VERIFICATION), any()))
                .thenReturn(2L); // Below limit of 3

        // When
        boolean result = verificationTokenService.canRequestNewToken(testUser, VerificationToken.TokenType.EMAIL_VERIFICATION);

        // Then
        assertTrue(result);
    }

    @Test
    void canRequestNewToken_ExceedsLimit_ReturnsFalse() {
        // Given
        when(tokenRepository.countTokensCreatedSince(eq(testUser), eq(VerificationToken.TokenType.EMAIL_VERIFICATION), any()))
                .thenReturn(5L); // Exceeds limit of 3

        // When
        boolean result = verificationTokenService.canRequestNewToken(testUser, VerificationToken.TokenType.EMAIL_VERIFICATION);

        // Then
        assertFalse(result);
    }

    @Test
    void cleanupExpiredTokens_Success() {
        // Given
        when(tokenRepository.deleteExpiredTokens(any(LocalDateTime.class))).thenReturn(5);

        // When
        verificationTokenService.cleanupExpiredTokens();

        // Then
        verify(tokenRepository).deleteExpiredTokens(any(LocalDateTime.class));
    }

    @Test
    void cleanupOldUsedTokens_Success() {
        // Given
        when(tokenRepository.deleteUsedTokensOlderThan(any(LocalDateTime.class))).thenReturn(10);

        // When
        verificationTokenService.cleanupOldUsedTokens();

        // Then
        verify(tokenRepository).deleteUsedTokensOlderThan(any(LocalDateTime.class));
    }
}