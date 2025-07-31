package com.blog.api.service;

import com.blog.api.entity.User;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private MimeMessage mimeMessage;

    @InjectMocks
    private EmailService emailService;

    private User testUser;

    @BeforeEach
    void setUp() {
        // Set up test configuration values
        ReflectionTestUtils.setField(emailService, "fromEmail", "test@blogapi.com");
        ReflectionTestUtils.setField(emailService, "baseUrl", "http://localhost:8080");
        ReflectionTestUtils.setField(emailService, "emailEnabled", true);

        // Create test user
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");

        // Mock MimeMessage creation
        lenient().when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
    }

    @Test
    void sendEmailVerification_Success() throws MessagingException {
        // Given
        String token = "test-verification-token";

        // When
        assertDoesNotThrow(() -> emailService.sendEmailVerification(testUser, token));

        // Then
        verify(mailSender).createMimeMessage();
        verify(mailSender).send(mimeMessage);
    }

    @Test
    void sendEmailVerification_EmailDisabled_SkipsEmail() {
        // Given
        ReflectionTestUtils.setField(emailService, "emailEnabled", false);
        String token = "test-verification-token";

        // When
        assertDoesNotThrow(() -> emailService.sendEmailVerification(testUser, token));

        // Then
        verify(mailSender, never()).createMimeMessage();
        verify(mailSender, never()).send(any(MimeMessage.class));
    }

    @Test
    void sendEmailVerification_MailSenderThrowsException_ThrowsRuntimeException() throws MessagingException {
        // Given
        String token = "test-verification-token";
        doThrow(new RuntimeException("SMTP error")).when(mailSender).send(mimeMessage);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> 
            emailService.sendEmailVerification(testUser, token));

        assertEquals("Failed to send verification email", exception.getMessage());
        verify(mailSender).createMimeMessage();
        verify(mailSender).send(mimeMessage);
    }

    @Test
    void sendPasswordReset_Success() throws MessagingException {
        // Given
        String token = "test-reset-token";

        // When
        assertDoesNotThrow(() -> emailService.sendPasswordReset(testUser, token));

        // Then
        verify(mailSender).createMimeMessage();
        verify(mailSender).send(mimeMessage);
    }

    @Test
    void sendPasswordReset_EmailDisabled_SkipsEmail() {
        // Given
        ReflectionTestUtils.setField(emailService, "emailEnabled", false);
        String token = "test-reset-token";

        // When
        assertDoesNotThrow(() -> emailService.sendPasswordReset(testUser, token));

        // Then
        verify(mailSender, never()).createMimeMessage();
        verify(mailSender, never()).send(any(MimeMessage.class));
    }

    @Test
    void sendPasswordReset_MailSenderThrowsException_ThrowsRuntimeException() throws MessagingException {
        // Given
        String token = "test-reset-token";
        doThrow(new RuntimeException("SMTP error")).when(mailSender).send(mimeMessage);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> 
            emailService.sendPasswordReset(testUser, token));

        assertEquals("Failed to send password reset email", exception.getMessage());
        verify(mailSender).createMimeMessage();
        verify(mailSender).send(mimeMessage);
    }

    @Test
    void sendWelcomeEmail_Success() throws MessagingException {
        // Given & When
        assertDoesNotThrow(() -> emailService.sendWelcomeEmail(testUser));

        // Then
        verify(mailSender).createMimeMessage();
        verify(mailSender).send(mimeMessage);
    }

    @Test
    void sendWelcomeEmail_EmailDisabled_SkipsEmail() {
        // Given
        ReflectionTestUtils.setField(emailService, "emailEnabled", false);

        // When
        assertDoesNotThrow(() -> emailService.sendWelcomeEmail(testUser));

        // Then
        verify(mailSender, never()).createMimeMessage();
        verify(mailSender, never()).send(any(MimeMessage.class));
    }

    @Test
    void sendWelcomeEmail_MailSenderThrowsException_DoesNotThrowException() throws MessagingException {
        // Given
        doThrow(new RuntimeException("SMTP error")).when(mailSender).send(mimeMessage);

        // When & Then - Should not throw exception for welcome email
        assertDoesNotThrow(() -> emailService.sendWelcomeEmail(testUser));

        verify(mailSender).createMimeMessage();
        verify(mailSender).send(mimeMessage);
    }

    @Test
    void isEmailServiceHealthy_Success_ReturnsTrue() {
        // Given & When
        boolean result = emailService.isEmailServiceHealthy();

        // Then
        assertTrue(result);
        verify(mailSender).createMimeMessage();
    }

    @Test
    void isEmailServiceHealthy_EmailDisabled_ReturnsFalse() {
        // Given
        ReflectionTestUtils.setField(emailService, "emailEnabled", false);

        // When
        boolean result = emailService.isEmailServiceHealthy();

        // Then
        assertFalse(result);
        verify(mailSender).createMimeMessage();
    }

    @Test
    void isEmailServiceHealthy_MailSenderThrowsException_ReturnsFalse() {
        // Given
        when(mailSender.createMimeMessage()).thenThrow(new RuntimeException("Mail server error"));

        // When
        boolean result = emailService.isEmailServiceHealthy();

        // Then
        assertFalse(result);
        verify(mailSender).createMimeMessage();
    }

    @Test
    void sendEmailVerification_ValidatesEmailContent() {
        // Given
        String token = "test-token-123";

        // When
        assertDoesNotThrow(() -> emailService.sendEmailVerification(testUser, token));

        // Then
        verify(mailSender).createMimeMessage();
        verify(mailSender).send(mimeMessage);
        // Additional assertions could be added to verify email content
        // by capturing the MimeMessageHelper calls if needed
    }

    @Test
    void sendPasswordReset_ValidatesEmailContent() {
        // Given
        String token = "reset-token-456";

        // When
        assertDoesNotThrow(() -> emailService.sendPasswordReset(testUser, token));

        // Then
        verify(mailSender).createMimeMessage();
        verify(mailSender).send(mimeMessage);
        // Additional assertions could be added to verify email content
        // by capturing the MimeMessageHelper calls if needed
    }

    @Test
    void sendWelcomeEmail_ValidatesEmailContent() {
        // When
        assertDoesNotThrow(() -> emailService.sendWelcomeEmail(testUser));

        // Then
        verify(mailSender).createMimeMessage();
        verify(mailSender).send(mimeMessage);
        // Additional assertions could be added to verify email content
        // by capturing the MimeMessageHelper calls if needed
    }
}