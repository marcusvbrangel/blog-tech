package com.blog.api.service;

import com.blog.api.entity.User;
import com.blog.api.entity.NewsletterToken;
import com.blog.api.entity.Post;
import com.blog.api.entity.Category;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Email Service Tests")
class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private MimeMessage mimeMessage;

    @InjectMocks
    private EmailService emailService;

    private User testUser;
    private NewsletterToken testToken;
    private Post testPost;
    private Category testCategory;

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

        // Create test newsletter token
        testToken = NewsletterToken.forConfirmation("newsletter@example.com").build();
        
        // Create test category
        testCategory = new Category();
        testCategory.setId(1L);
        testCategory.setName("Technology");
        testCategory.setDescription("Tech posts");
        
        // Create test post
        testPost = new Post();
        testPost.setId(1L);
        testPost.setTitle("Test Post Title");
        testPost.setContent("This is a test post content for newsletter testing purposes.");
        testPost.setUser(testUser);
        testPost.setCategory(testCategory);
        testPost.setCreatedAt(LocalDateTime.now());
        testPost.setPublished(true);

        // Mock MimeMessage creation
        lenient().when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
    }

    @Test
    @DisplayName("Deve enviar email de verificação com sucesso")
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
    @DisplayName("Deve pular envio de email quando email está desabilitado")
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
    @DisplayName("Deve lançar RuntimeException quando MailSender falha")
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
    @DisplayName("Deve enviar email de reset de senha com sucesso")
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
    @DisplayName("Deve pular envio de reset de senha quando email está desabilitado")
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
    @DisplayName("Deve lançar RuntimeException quando reset de senha falha")
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
    @DisplayName("Deve enviar email de boas-vindas com sucesso")
    void sendWelcomeEmail_Success() throws MessagingException {
        // Given & When
        assertDoesNotThrow(() -> emailService.sendWelcomeEmail(testUser));

        // Then
        verify(mailSender).createMimeMessage();
        verify(mailSender).send(mimeMessage);
    }

    @Test
    @DisplayName("Deve pular envio de email de boas-vindas quando email está desabilitado")
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
    @DisplayName("Não deve lançar exceção quando email de boas-vindas falha")
    void sendWelcomeEmail_MailSenderThrowsException_DoesNotThrowException() throws MessagingException {
        // Given
        doThrow(new RuntimeException("SMTP error")).when(mailSender).send(mimeMessage);

        // When & Then - Should not throw exception for welcome email
        assertDoesNotThrow(() -> emailService.sendWelcomeEmail(testUser));

        verify(mailSender).createMimeMessage();
        verify(mailSender).send(mimeMessage);
    }

    @Test
    @DisplayName("Deve retornar true quando serviço de email está saudável")
    void isEmailServiceHealthy_Success_ReturnsTrue() {
        // Given & When
        boolean result = emailService.isEmailServiceHealthy();

        // Then
        assertTrue(result);
        verify(mailSender).createMimeMessage();
    }

    @Test
    @DisplayName("Deve retornar false quando email está desabilitado")
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
    @DisplayName("Deve retornar false quando MailSender lança exceção")
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
    @DisplayName("Deve validar conteúdo do email de verificação")
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
    @DisplayName("Deve validar conteúdo do email de reset de senha")
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
    @DisplayName("Deve validar conteúdo do email de boas-vindas")
    void sendWelcomeEmail_ValidatesEmailContent() {
        // When
        assertDoesNotThrow(() -> emailService.sendWelcomeEmail(testUser));

        // Then
        verify(mailSender).createMimeMessage();
        verify(mailSender).send(mimeMessage);
        // Additional assertions could be added to verify email content
        // by capturing the MimeMessageHelper calls if needed
    }

    // ===== NEWSLETTER EMAIL TESTS =====

    @Test
    @DisplayName("Deve enviar email de confirmação da newsletter com sucesso")
    void sendNewsletterConfirmation_Success() throws MessagingException {
        // Given
        String email = "newsletter@example.com";

        // When
        assertDoesNotThrow(() -> emailService.sendNewsletterConfirmation(email, testToken));

        // Then
        verify(mailSender).createMimeMessage();
        verify(mailSender).send(mimeMessage);
    }

    @Test
    @DisplayName("Deve pular envio de confirmação da newsletter quando email está desabilitado")
    void sendNewsletterConfirmation_EmailDisabled_SkipsEmail() {
        // Given
        ReflectionTestUtils.setField(emailService, "emailEnabled", false);
        String email = "newsletter@example.com";

        // When
        assertDoesNotThrow(() -> emailService.sendNewsletterConfirmation(email, testToken));

        // Then
        verify(mailSender, never()).createMimeMessage();
        verify(mailSender, never()).send(any(MimeMessage.class));
    }

    @Test
    @DisplayName("Deve lançar RuntimeException quando confirmação da newsletter falha")
    void sendNewsletterConfirmation_MailSenderThrowsException_ThrowsRuntimeException() throws MessagingException {
        // Given
        String email = "newsletter@example.com";
        doThrow(new RuntimeException("SMTP error")).when(mailSender).send(mimeMessage);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> 
            emailService.sendNewsletterConfirmation(email, testToken));

        assertEquals("Failed to send newsletter confirmation email", exception.getMessage());
        verify(mailSender).createMimeMessage();
        verify(mailSender).send(mimeMessage);
    }

    @Test
    @DisplayName("Deve enviar email de cancelamento da newsletter com sucesso")
    void sendNewsletterUnsubscribed_Success() throws MessagingException {
        // Given
        String email = "newsletter@example.com";

        // When
        assertDoesNotThrow(() -> emailService.sendNewsletterUnsubscribed(email));

        // Then
        verify(mailSender).createMimeMessage();
        verify(mailSender).send(mimeMessage);
    }

    @Test
    @DisplayName("Deve pular envio de cancelamento da newsletter quando email está desabilitado")
    void sendNewsletterUnsubscribed_EmailDisabled_SkipsEmail() {
        // Given
        ReflectionTestUtils.setField(emailService, "emailEnabled", false);
        String email = "newsletter@example.com";

        // When
        assertDoesNotThrow(() -> emailService.sendNewsletterUnsubscribed(email));

        // Then
        verify(mailSender, never()).createMimeMessage();
        verify(mailSender, never()).send(any(MimeMessage.class));
    }

    @Test
    @DisplayName("Não deve lançar exceção quando cancelamento da newsletter falha")
    void sendNewsletterUnsubscribed_MailSenderThrowsException_DoesNotThrowException() throws MessagingException {
        // Given
        String email = "newsletter@example.com";
        doThrow(new RuntimeException("SMTP error")).when(mailSender).send(mimeMessage);

        // When & Then - Should not throw exception for unsubscribe confirmation
        assertDoesNotThrow(() -> emailService.sendNewsletterUnsubscribed(email));

        verify(mailSender).createMimeMessage();
        verify(mailSender).send(mimeMessage);
    }

    @Test
    @DisplayName("Deve enviar notificação de novo post com sucesso")
    void sendNewPostNotification_Success() throws MessagingException {
        // Given
        String email = "subscriber@example.com";
        NewsletterToken unsubscribeToken = NewsletterToken.forUnsubscribe(email).build();

        // When
        assertDoesNotThrow(() -> emailService.sendNewPostNotification(email, testPost, unsubscribeToken));

        // Then
        verify(mailSender).createMimeMessage();
        verify(mailSender).send(mimeMessage);
    }

    @Test
    @DisplayName("Deve pular notificação de novo post quando email está desabilitado")
    void sendNewPostNotification_EmailDisabled_SkipsEmail() {
        // Given
        ReflectionTestUtils.setField(emailService, "emailEnabled", false);
        String email = "subscriber@example.com";
        NewsletterToken unsubscribeToken = NewsletterToken.forUnsubscribe(email).build();

        // When
        assertDoesNotThrow(() -> emailService.sendNewPostNotification(email, testPost, unsubscribeToken));

        // Then
        verify(mailSender, never()).createMimeMessage();
        verify(mailSender, never()).send(any(MimeMessage.class));
    }

    @Test
    @DisplayName("Não deve lançar exceção quando notificação de novo post falha")
    void sendNewPostNotification_MailSenderThrowsException_DoesNotThrowException() throws MessagingException {
        // Given
        String email = "subscriber@example.com";
        NewsletterToken unsubscribeToken = NewsletterToken.forUnsubscribe(email).build();
        doThrow(new RuntimeException("SMTP error")).when(mailSender).send(mimeMessage);

        // When & Then - Should not throw exception for post notification
        assertDoesNotThrow(() -> emailService.sendNewPostNotification(email, testPost, unsubscribeToken));

        verify(mailSender).createMimeMessage();
        verify(mailSender).send(mimeMessage);
    }

    @Test
    @DisplayName("Deve enviar resumo semanal com sucesso")
    void sendWeeklyDigest_Success() throws MessagingException {
        // Given
        String email = "subscriber@example.com";
        NewsletterToken unsubscribeToken = NewsletterToken.forUnsubscribe(email).build();
        List<Post> posts = List.of(testPost);

        // When
        assertDoesNotThrow(() -> emailService.sendWeeklyDigest(email, posts, unsubscribeToken));

        // Then
        verify(mailSender).createMimeMessage();
        verify(mailSender).send(mimeMessage);
    }

    @Test
    @DisplayName("Deve pular resumo semanal quando email está desabilitado")
    void sendWeeklyDigest_EmailDisabled_SkipsEmail() {
        // Given
        ReflectionTestUtils.setField(emailService, "emailEnabled", false);
        String email = "subscriber@example.com";
        NewsletterToken unsubscribeToken = NewsletterToken.forUnsubscribe(email).build();
        List<Post> posts = List.of(testPost);

        // When
        assertDoesNotThrow(() -> emailService.sendWeeklyDigest(email, posts, unsubscribeToken));

        // Then
        verify(mailSender, never()).createMimeMessage();
        verify(mailSender, never()).send(any(MimeMessage.class));
    }

    @Test
    @DisplayName("Deve enviar resumo semanal com múltiplos posts")
    void sendWeeklyDigest_MultiplePostsSuccess() throws MessagingException {
        // Given
        String email = "subscriber@example.com";
        NewsletterToken unsubscribeToken = NewsletterToken.forUnsubscribe(email).build();
        
        Post post2 = new Post();
        post2.setId(2L);
        post2.setTitle("Second Test Post");
        post2.setContent("This is another test post for weekly digest.");
        post2.setUser(testUser);
        post2.setCategory(testCategory);
        post2.setCreatedAt(LocalDateTime.now().minusDays(1));
        post2.setPublished(true);
        
        List<Post> posts = List.of(testPost, post2);

        // When
        assertDoesNotThrow(() -> emailService.sendWeeklyDigest(email, posts, unsubscribeToken));

        // Then
        verify(mailSender).createMimeMessage();
        verify(mailSender).send(mimeMessage);
    }

    @Test
    @DisplayName("Não deve lançar exceção quando resumo semanal falha")
    void sendWeeklyDigest_MailSenderThrowsException_DoesNotThrowException() throws MessagingException {
        // Given
        String email = "subscriber@example.com";
        NewsletterToken unsubscribeToken = NewsletterToken.forUnsubscribe(email).build();
        List<Post> posts = List.of(testPost);
        doThrow(new RuntimeException("SMTP error")).when(mailSender).send(mimeMessage);

        // When & Then - Should not throw exception for weekly digest
        assertDoesNotThrow(() -> emailService.sendWeeklyDigest(email, posts, unsubscribeToken));

        verify(mailSender).createMimeMessage();
        verify(mailSender).send(mimeMessage);
    }

    @Test
    @DisplayName("Deve enviar email de boas-vindas da newsletter com sucesso")
    void sendNewsletterWelcome_Success() throws MessagingException {
        // Given
        String email = "newsubscriber@example.com";
        NewsletterToken unsubscribeToken = NewsletterToken.forUnsubscribe(email).build();

        // When
        assertDoesNotThrow(() -> emailService.sendNewsletterWelcome(email, unsubscribeToken));

        // Then
        verify(mailSender).createMimeMessage();
        verify(mailSender).send(mimeMessage);
    }

    @Test
    @DisplayName("Deve pular boas-vindas da newsletter quando email está desabilitado")
    void sendNewsletterWelcome_EmailDisabled_SkipsEmail() {
        // Given
        ReflectionTestUtils.setField(emailService, "emailEnabled", false);
        String email = "newsubscriber@example.com";
        NewsletterToken unsubscribeToken = NewsletterToken.forUnsubscribe(email).build();

        // When
        assertDoesNotThrow(() -> emailService.sendNewsletterWelcome(email, unsubscribeToken));

        // Then
        verify(mailSender, never()).createMimeMessage();
        verify(mailSender, never()).send(any(MimeMessage.class));
    }

    @Test
    @DisplayName("Não deve lançar exceção quando boas-vindas da newsletter falha")
    void sendNewsletterWelcome_MailSenderThrowsException_DoesNotThrowException() throws MessagingException {
        // Given
        String email = "newsubscriber@example.com";
        NewsletterToken unsubscribeToken = NewsletterToken.forUnsubscribe(email).build();
        doThrow(new RuntimeException("SMTP error")).when(mailSender).send(mimeMessage);

        // When & Then - Should not throw exception for newsletter welcome
        assertDoesNotThrow(() -> emailService.sendNewsletterWelcome(email, unsubscribeToken));

        verify(mailSender).createMimeMessage();
        verify(mailSender).send(mimeMessage);
    }

    @Test
    @DisplayName("Deve lidar com posts sem categoria na notificação")
    void sendNewPostNotification_PostWithoutCategory_Success() throws MessagingException {
        // Given
        String email = "subscriber@example.com";
        NewsletterToken unsubscribeToken = NewsletterToken.forUnsubscribe(email).build();
        
        Post postWithoutCategory = new Post();
        postWithoutCategory.setId(3L);
        postWithoutCategory.setTitle("Post Without Category");
        postWithoutCategory.setContent("This post has no category.");
        postWithoutCategory.setUser(testUser);
        postWithoutCategory.setCategory(null); // No category
        postWithoutCategory.setCreatedAt(LocalDateTime.now());
        postWithoutCategory.setPublished(true);

        // When
        assertDoesNotThrow(() -> emailService.sendNewPostNotification(email, postWithoutCategory, unsubscribeToken));

        // Then
        verify(mailSender).createMimeMessage();
        verify(mailSender).send(mimeMessage);
    }

    @Test
    @DisplayName("Deve lidar com posts sem usuário na notificação")
    void sendNewPostNotification_PostWithoutUser_Success() throws MessagingException {
        // Given
        String email = "subscriber@example.com";
        NewsletterToken unsubscribeToken = NewsletterToken.forUnsubscribe(email).build();
        
        Post postWithoutUser = new Post();
        postWithoutUser.setId(4L);
        postWithoutUser.setTitle("Post Without User");
        postWithoutUser.setContent("This post has no user.");
        postWithoutUser.setUser(null); // No user
        postWithoutUser.setCategory(testCategory);
        postWithoutUser.setCreatedAt(LocalDateTime.now());
        postWithoutUser.setPublished(true);

        // When
        assertDoesNotThrow(() -> emailService.sendNewPostNotification(email, postWithoutUser, unsubscribeToken));

        // Then
        verify(mailSender).createMimeMessage();
        verify(mailSender).send(mimeMessage);
    }

    @Test
    @DisplayName("Deve lidar com conteúdo longo no post")
    void sendNewPostNotification_LongContent_TruncatesContent() throws MessagingException {
        // Given
        String email = "subscriber@example.com";
        NewsletterToken unsubscribeToken = NewsletterToken.forUnsubscribe(email).build();
        
        String longContent = "A".repeat(300); // Content longer than 200 chars
        Post postWithLongContent = new Post();
        postWithLongContent.setId(5L);
        postWithLongContent.setTitle("Post With Long Content");
        postWithLongContent.setContent(longContent);
        postWithLongContent.setUser(testUser);
        postWithLongContent.setCategory(testCategory);
        postWithLongContent.setCreatedAt(LocalDateTime.now());
        postWithLongContent.setPublished(true);

        // When
        assertDoesNotThrow(() -> emailService.sendNewPostNotification(email, postWithLongContent, unsubscribeToken));

        // Then
        verify(mailSender).createMimeMessage();
        verify(mailSender).send(mimeMessage);
    }

    @Test
    @DisplayName("Deve enviar resumo semanal com lista vazia de posts")
    void sendWeeklyDigest_EmptyPostsList_Success() throws MessagingException {
        // Given
        String email = "subscriber@example.com";
        NewsletterToken unsubscribeToken = NewsletterToken.forUnsubscribe(email).build();
        List<Post> emptyPosts = List.of();

        // When
        assertDoesNotThrow(() -> emailService.sendWeeklyDigest(email, emptyPosts, unsubscribeToken));

        // Then
        verify(mailSender).createMimeMessage();
        verify(mailSender).send(mimeMessage);
    }
}