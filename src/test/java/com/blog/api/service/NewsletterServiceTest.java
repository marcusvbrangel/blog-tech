package com.blog.api.service;

import com.blog.api.dto.NewsletterSubscriptionRequest;
import com.blog.api.dto.NewsletterSubscriptionResponse;
import com.blog.api.entity.NewsletterSubscriber;
import com.blog.api.entity.NewsletterToken;
import com.blog.api.entity.NewsletterTokenType;
import com.blog.api.entity.SubscriptionStatus;
import com.blog.api.exception.BadRequestException;
import com.blog.api.repository.NewsletterSubscriberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for NewsletterService.
 * Tests subscription logic, LGPD compliance, and edge cases.
 * 
 * @author AI-Driven Development
 * @version 1.0
 * @since 2025-08-05
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("NewsletterService Tests")
class NewsletterServiceTest {

    @Mock
    private NewsletterSubscriberRepository subscriberRepository;

    @Mock
    private EmailService emailService;

    @Mock
    private NewsletterTokenService tokenService;

    @InjectMocks
    private NewsletterService newsletterService;

    private NewsletterSubscriptionRequest validRequest;
    private NewsletterSubscriber existingSubscriber;

    @BeforeEach
    void setUp() {
        validRequest = NewsletterSubscriptionRequest.withMetadata(
            "test@example.com",
            "1.0",
            "192.168.1.1",
            "Mozilla/5.0 Test Browser"
        );

        existingSubscriber = NewsletterSubscriber.withConsent(
            "test@example.com",
            "192.168.1.1",
            "Mozilla/5.0 Test Browser",
            "1.0"
        ).build();
        existingSubscriber.setId(1L);
        existingSubscriber.setCreatedAt(LocalDateTime.now());

        // Set up default mock behavior for tokenService to avoid NullPointerException
        NewsletterToken mockToken = NewsletterToken.forConfirmation("test@example.com").build();
        lenient().when(tokenService.generateConfirmationToken(anyString(), anyString(), anyString()))
            .thenReturn(mockToken);
        lenient().when(tokenService.generateUnsubscribeToken(anyString(), anyString(), anyString()))
            .thenReturn(NewsletterToken.forUnsubscribe("test@example.com").build());
        
        // Set up default mock behavior for emailService  
        lenient().doNothing().when(emailService).sendNewsletterConfirmation(anyString(), any(NewsletterToken.class));
        lenient().doNothing().when(emailService).sendNewsletterWelcome(anyString(), any(NewsletterToken.class));
    }

    @Test
    @DisplayName("Should create new subscription successfully")
    void shouldCreateNewSubscriptionSuccessfully() {
        // Given
        when(subscriberRepository.findByEmail(validRequest.email()))
            .thenReturn(Optional.empty());
        when(subscriberRepository.save(any(NewsletterSubscriber.class)))
            .thenReturn(existingSubscriber);

        // When
        NewsletterSubscriptionResponse response = newsletterService.subscribe(validRequest);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.email()).isEqualTo("test@example.com");
        assertThat(response.status()).isEqualTo(SubscriptionStatus.PENDING);
        assertThat(response.message()).contains("Subscription successful");
        
        verify(subscriberRepository).findByEmail("test@example.com");
        verify(subscriberRepository).save(any(NewsletterSubscriber.class));
    }

    @Test
    @DisplayName("Should reject subscription without LGPD consent")
    void shouldRejectSubscriptionWithoutLgpdConsent() {
        // Given & When & Then - DTO validation happens at construction time
        assertThatThrownBy(() -> new NewsletterSubscriptionRequest(
            "test@example.com", false, "1.0", "192.168.1.1", "Mozilla/5.0"
        ))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("User must explicitly consent to receive emails for LGPD compliance");

        verify(subscriberRepository, never()).findByEmail(anyString());
        verify(subscriberRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should handle already confirmed subscriber")
    void shouldHandleAlreadyConfirmedSubscriber() {
        // Given
        existingSubscriber.setStatus(SubscriptionStatus.CONFIRMED);
        existingSubscriber.setConfirmedAt(LocalDateTime.now());
        
        when(subscriberRepository.findByEmail(validRequest.email()))
            .thenReturn(Optional.of(existingSubscriber));

        // When
        NewsletterSubscriptionResponse response = newsletterService.subscribe(validRequest);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.status()).isEqualTo(SubscriptionStatus.CONFIRMED);
        assertThat(response.message()).contains("already subscribed and confirmed");
        
        verify(subscriberRepository).findByEmail("test@example.com");
        verify(subscriberRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should handle already pending subscriber")
    void shouldHandleAlreadyPendingSubscriber() {
        // Given
        existingSubscriber.setStatus(SubscriptionStatus.PENDING);
        
        when(subscriberRepository.findByEmail(validRequest.email()))
            .thenReturn(Optional.of(existingSubscriber));

        // When
        NewsletterSubscriptionResponse response = newsletterService.subscribe(validRequest);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.status()).isEqualTo(SubscriptionStatus.PENDING);
        assertThat(response.message()).contains("already subscribed");
        
        verify(subscriberRepository).findByEmail("test@example.com");
        verify(subscriberRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should reactivate unsubscribed subscriber")
    void shouldReactivateUnsubscribedSubscriber() {
        // Given
        existingSubscriber.setStatus(SubscriptionStatus.UNSUBSCRIBED);
        existingSubscriber.setUnsubscribedAt(LocalDateTime.now().minusDays(1));
        
        when(subscriberRepository.findByEmail(validRequest.email()))
            .thenReturn(Optional.of(existingSubscriber));
        when(subscriberRepository.save(any(NewsletterSubscriber.class)))
            .thenReturn(existingSubscriber);

        // When
        NewsletterSubscriptionResponse response = newsletterService.subscribe(validRequest);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.message()).contains("Welcome back");
        
        verify(subscriberRepository).findByEmail("test@example.com");
        verify(subscriberRepository).save(any(NewsletterSubscriber.class));
    }

    @Test
    @DisplayName("Should create new subscription for deleted subscriber")
    void shouldCreateNewSubscriptionForDeletedSubscriber() {
        // Given
        existingSubscriber.setStatus(SubscriptionStatus.DELETED);
        
        // Create a new subscriber that will be returned after save (with PENDING status)
        NewsletterSubscriber reactivatedSubscriber = NewsletterSubscriber.newInstance()
            .email("test@example.com")
            .status(SubscriptionStatus.PENDING)
            .consentGivenAt(LocalDateTime.now())
            .consentIpAddress("192.168.1.1")
            .consentUserAgent("Mozilla/5.0")
            .privacyPolicyVersion("1.0")
            .build();
        reactivatedSubscriber.setId(1L); // Set ID after creation
        
        when(subscriberRepository.findByEmail(validRequest.email()))
            .thenReturn(Optional.of(existingSubscriber));
        when(subscriberRepository.save(any(NewsletterSubscriber.class)))
            .thenReturn(reactivatedSubscriber);

        // When
        NewsletterSubscriptionResponse response = newsletterService.subscribe(validRequest);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.status()).isEqualTo(SubscriptionStatus.PENDING);
        assertThat(response.message()).contains("Subscription successful");
        
        verify(subscriberRepository).findByEmail("test@example.com");
        verify(subscriberRepository).save(any(NewsletterSubscriber.class));
    }

    @Test
    @DisplayName("Should handle database constraint violation")
    void shouldHandleDatabaseConstraintViolation() {
        // Given
        when(subscriberRepository.findByEmail(validRequest.email()))
            .thenReturn(Optional.empty());
        when(subscriberRepository.save(any(NewsletterSubscriber.class)))
            .thenThrow(new DataIntegrityViolationException("Duplicate entry"));

        // When & Then
        assertThatThrownBy(() -> newsletterService.subscribe(validRequest))
            .isInstanceOf(BadRequestException.class)
            .hasMessageContaining("Email already exists");

        verify(subscriberRepository).findByEmail("test@example.com");
        verify(subscriberRepository).save(any(NewsletterSubscriber.class));
    }

    @Test
    @DisplayName("Should get confirmed subscribers")
    void shouldGetConfirmedSubscribers() {
        // Given
        var confirmedSubscribers = java.util.List.of(existingSubscriber);
        when(subscriberRepository.findConfirmedSubscribers())
            .thenReturn(confirmedSubscribers);

        // When
        var result = newsletterService.getConfirmedSubscribers();

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getEmail()).isEqualTo("test@example.com");
        
        verify(subscriberRepository).findConfirmedSubscribers();
    }

    @Test
    @DisplayName("Should check if email has active subscription")
    void shouldCheckIfEmailHasActiveSubscription() {
        // Given
        when(subscriberRepository.existsByEmailAndNotDeleted("test@example.com"))
            .thenReturn(true);

        // When
        boolean hasActive = newsletterService.hasActiveSubscription("test@example.com");

        // Then
        assertThat(hasActive).isTrue();
        verify(subscriberRepository).existsByEmailAndNotDeleted("test@example.com");
    }

    @Test
    @DisplayName("Should get subscriber by email")
    void shouldGetSubscriberByEmail() {
        // Given
        when(subscriberRepository.findByEmail("test@example.com"))
            .thenReturn(Optional.of(existingSubscriber));

        // When
        Optional<NewsletterSubscriber> result = newsletterService.getSubscriberByEmail("test@example.com");

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getEmail()).isEqualTo("test@example.com");
        
        verify(subscriberRepository).findByEmail("test@example.com");
    }

    @Test
    @DisplayName("Should handle request without consent")
    void shouldHandleRequestWithoutConsent() {
        // Given - Request with null consent (hasValidConsent() returns false)
        NewsletterSubscriptionRequest requestWithoutConsent = new NewsletterSubscriptionRequest(
            "test@example.com", null, "1.0", "192.168.1.1", "Mozilla/5.0"
        );

        // When & Then - Service should reject due to missing consent
        assertThatThrownBy(() -> newsletterService.subscribe(requestWithoutConsent))
            .isInstanceOf(BadRequestException.class)
            .hasMessageContaining("Explicit consent is required for LGPD compliance");
            
        // No repository calls should be made
        verify(subscriberRepository, never()).findByEmail(anyString());
        verify(subscriberRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should allow request without privacy policy version")
    void shouldAllowRequestWithoutPrivacyPolicyVersion() {
        // Given - Request with null privacy policy (service doesn't validate this)
        NewsletterSubscriptionRequest requestWithoutPolicy = new NewsletterSubscriptionRequest(
            "test@example.com", true, null, "192.168.1.1", "Mozilla/5.0"
        );

        // Mock repository behavior for successful subscription
        when(subscriberRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());
        when(subscriberRepository.save(any(NewsletterSubscriber.class))).thenReturn(existingSubscriber);

        // When - Service should process successfully 
        NewsletterSubscriptionResponse response = newsletterService.subscribe(requestWithoutPolicy);

        // Then - Subscription should be created
        assertThat(response).isNotNull();
        assertThat(response.status()).isEqualTo(SubscriptionStatus.PENDING);
        assertThat(response.message()).contains("Subscription successful");
        
        verify(subscriberRepository).findByEmail("test@example.com");
        verify(subscriberRepository).save(any(NewsletterSubscriber.class));
    }

    // ===== SEND CONFIRMATION TESTS =====

    @Test
    @DisplayName("Should send confirmation email successfully")
    void shouldSendConfirmationEmailSuccessfully() {
        // Given
        String email = "test@example.com";
        String ipAddress = "192.168.1.1";
        String userAgent = "Mozilla/5.0";
        
        NewsletterToken mockToken = NewsletterToken.forConfirmation(email).build();
        when(tokenService.generateConfirmationToken(email, ipAddress, userAgent)).thenReturn(mockToken);
        doNothing().when(emailService).sendNewsletterConfirmation(email, mockToken);

        // When
        NewsletterToken result = newsletterService.sendConfirmation(email, ipAddress, userAgent);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo(email);
        
        verify(tokenService).generateConfirmationToken(email, ipAddress, userAgent);
        verify(emailService).sendNewsletterConfirmation(email, mockToken);
    }

    @Test
    @DisplayName("Should handle token generation failure")
    void shouldHandleTokenGenerationFailure() {
        // Given
        String email = "test@example.com";
        String ipAddress = "192.168.1.1";
        String userAgent = "Mozilla/5.0";
        
        when(tokenService.generateConfirmationToken(email, ipAddress, userAgent))
            .thenThrow(new RuntimeException("Token generation failed"));

        // When & Then
        assertThatThrownBy(() -> newsletterService.sendConfirmation(email, ipAddress, userAgent))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Failed to send confirmation email");

        verify(tokenService).generateConfirmationToken(email, ipAddress, userAgent);
        verify(emailService, never()).sendNewsletterConfirmation(anyString(), any(NewsletterToken.class));
    }

    @Test
    @DisplayName("Should handle email sending failure")
    void shouldHandleEmailSendingFailure() {
        // Given
        String email = "test@example.com";
        String ipAddress = "192.168.1.1";
        String userAgent = "Mozilla/5.0";
        
        NewsletterToken mockToken = NewsletterToken.forConfirmation(email).build();
        when(tokenService.generateConfirmationToken(email, ipAddress, userAgent)).thenReturn(mockToken);
        doThrow(new RuntimeException("Email sending failed"))
            .when(emailService).sendNewsletterConfirmation(email, mockToken);

        // When & Then
        assertThatThrownBy(() -> newsletterService.sendConfirmation(email, ipAddress, userAgent))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Failed to send confirmation email");

        verify(tokenService).generateConfirmationToken(email, ipAddress, userAgent);
        verify(emailService).sendNewsletterConfirmation(email, mockToken);
    }

    @Test
    @DisplayName("Should send confirmation with null IP and user agent")
    void shouldSendConfirmationWithNullIpAndUserAgent() {
        // Given
        String email = "test@example.com";
        
        NewsletterToken mockToken = NewsletterToken.forConfirmation(email).build();
        when(tokenService.generateConfirmationToken(email, null, null)).thenReturn(mockToken);
        doNothing().when(emailService).sendNewsletterConfirmation(email, mockToken);

        // When
        NewsletterToken result = newsletterService.sendConfirmation(email, null, null);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo(email);
        
        verify(tokenService).generateConfirmationToken(email, null, null);
        verify(emailService).sendNewsletterConfirmation(email, mockToken);
    }

    @Test
    @DisplayName("Should send confirmation email async successfully")
    void shouldSendConfirmationEmailAsyncSuccessfully() {
        // Given
        NewsletterSubscriber subscriber = NewsletterSubscriber.withConsent(
            "test@example.com", "192.168.1.1", "Mozilla/5.0", "1.0"
        ).build();
        
        NewsletterToken mockToken = NewsletterToken.forConfirmation(subscriber.getEmail()).build();
        when(tokenService.generateConfirmationToken(
            subscriber.getEmail(), 
            subscriber.getConsentIpAddress(), 
            subscriber.getConsentUserAgent()
        )).thenReturn(mockToken);
        doNothing().when(emailService).sendNewsletterConfirmation(subscriber.getEmail(), mockToken);

        // When
        newsletterService.sendConfirmationEmailAsync(subscriber);

        // Then
        verify(tokenService).generateConfirmationToken(
            subscriber.getEmail(),
            subscriber.getConsentIpAddress(),
            subscriber.getConsentUserAgent()
        );
        verify(emailService).sendNewsletterConfirmation(subscriber.getEmail(), mockToken);
    }

    @Test
    @DisplayName("Should handle async confirmation failure gracefully")
    void shouldHandleAsyncConfirmationFailureGracefully() {
        // Given
        NewsletterSubscriber subscriber = NewsletterSubscriber.withConsent(
            "test@example.com", "192.168.1.1", "Mozilla/5.0", "1.0"
        ).build();
        
        when(tokenService.generateConfirmationToken(anyString(), anyString(), anyString()))
            .thenThrow(new RuntimeException("Token generation failed"));

        // When - Should not throw exception for async method
        newsletterService.sendConfirmationEmailAsync(subscriber);

        // Then
        verify(tokenService).generateConfirmationToken(
            subscriber.getEmail(),
            subscriber.getConsentIpAddress(),
            subscriber.getConsentUserAgent()
        );
        verify(emailService, never()).sendNewsletterConfirmation(anyString(), any(NewsletterToken.class));
    }

    // ===== CONFIRM SUBSCRIPTION TESTS =====

    @Test
    @DisplayName("Should confirm subscription successfully")
    void shouldConfirmSubscriptionSuccessfully() {
        // Given
        String tokenValue = "valid-token-123";
        String ipAddress = "192.168.1.1";
        String userAgent = "Mozilla/5.0";
        
        NewsletterToken validToken = NewsletterToken.forConfirmation("test@example.com").build();
        existingSubscriber.setStatus(SubscriptionStatus.PENDING);
        
        when(tokenService.validateToken(tokenValue, NewsletterTokenType.CONFIRMATION)).thenReturn(validToken);
        when(subscriberRepository.findByEmail("test@example.com")).thenReturn(Optional.of(existingSubscriber));
        when(subscriberRepository.save(any(NewsletterSubscriber.class))).thenReturn(existingSubscriber);
        when(tokenService.markTokenAsUsed(tokenValue, NewsletterTokenType.CONFIRMATION)).thenReturn(validToken);

        // When
        NewsletterSubscriptionResponse response = newsletterService.confirmSubscription(tokenValue, ipAddress, userAgent);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.status()).isEqualTo(SubscriptionStatus.CONFIRMED);
        assertThat(response.message()).contains("confirmed successfully");
        
        verify(tokenService).validateToken(tokenValue, NewsletterTokenType.CONFIRMATION);
        verify(subscriberRepository).findByEmail("test@example.com");
        verify(subscriberRepository).save(any(NewsletterSubscriber.class));
        verify(tokenService).markTokenAsUsed(tokenValue, NewsletterTokenType.CONFIRMATION);
    }

    @Test
    @DisplayName("Should handle already confirmed subscription")
    void shouldHandleAlreadyConfirmedSubscription() {
        // Given
        String tokenValue = "valid-token-123";
        String ipAddress = "192.168.1.1";
        String userAgent = "Mozilla/5.0";
        
        NewsletterToken validToken = NewsletterToken.forConfirmation("test@example.com").build();
        existingSubscriber.setStatus(SubscriptionStatus.CONFIRMED);
        existingSubscriber.setConfirmedAt(LocalDateTime.now());
        
        when(tokenService.validateToken(tokenValue, NewsletterTokenType.CONFIRMATION)).thenReturn(validToken);
        when(subscriberRepository.findByEmail("test@example.com")).thenReturn(Optional.of(existingSubscriber));

        // When
        NewsletterSubscriptionResponse response = newsletterService.confirmSubscription(tokenValue, ipAddress, userAgent);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.status()).isEqualTo(SubscriptionStatus.CONFIRMED);
        assertThat(response.message()).contains("already confirmed");
        
        verify(tokenService).validateToken(tokenValue, NewsletterTokenType.CONFIRMATION);
        verify(subscriberRepository).findByEmail("test@example.com");
        verify(subscriberRepository, never()).save(any(NewsletterSubscriber.class));
        verify(tokenService, never()).markTokenAsUsed(anyString(), any(NewsletterTokenType.class));
    }

    @Test
    @DisplayName("Should handle invalid token")
    void shouldHandleInvalidToken() {
        // Given
        String tokenValue = "invalid-token-123";
        String ipAddress = "192.168.1.1";
        String userAgent = "Mozilla/5.0";
        
        when(tokenService.validateToken(tokenValue, NewsletterTokenType.CONFIRMATION))
            .thenThrow(new BadRequestException("Invalid or expired newsletter token"));

        // When & Then
        assertThatThrownBy(() -> newsletterService.confirmSubscription(tokenValue, ipAddress, userAgent))
            .isInstanceOf(BadRequestException.class)
            .hasMessageContaining("Invalid or expired newsletter token");

        verify(tokenService).validateToken(tokenValue, NewsletterTokenType.CONFIRMATION);
        verify(subscriberRepository, never()).findByEmail(anyString());
        verify(subscriberRepository, never()).save(any(NewsletterSubscriber.class));
    }

    @Test
    @DisplayName("Should handle subscriber not found")
    void shouldHandleSubscriberNotFound() {
        // Given
        String tokenValue = "valid-token-123";
        String ipAddress = "192.168.1.1";
        String userAgent = "Mozilla/5.0";
        
        NewsletterToken validToken = NewsletterToken.forConfirmation("test@example.com").build();
        
        when(tokenService.validateToken(tokenValue, NewsletterTokenType.CONFIRMATION)).thenReturn(validToken);
        when(subscriberRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> newsletterService.confirmSubscription(tokenValue, ipAddress, userAgent))
            .isInstanceOf(BadRequestException.class)
            .hasMessageContaining("Subscription not found for this confirmation token");

        verify(tokenService).validateToken(tokenValue, NewsletterTokenType.CONFIRMATION);
        verify(subscriberRepository).findByEmail("test@example.com");
        verify(subscriberRepository, never()).save(any(NewsletterSubscriber.class));
        verify(tokenService, never()).markTokenAsUsed(anyString(), any(NewsletterTokenType.class));
    }

    @Test
    @DisplayName("Should handle expired token")
    void shouldHandleExpiredToken() {
        // Given
        String tokenValue = "expired-token-123";
        String ipAddress = "192.168.1.1";
        String userAgent = "Mozilla/5.0";
        
        when(tokenService.validateToken(tokenValue, NewsletterTokenType.CONFIRMATION))
            .thenThrow(new BadRequestException("Newsletter token has expired"));

        // When & Then
        assertThatThrownBy(() -> newsletterService.confirmSubscription(tokenValue, ipAddress, userAgent))
            .isInstanceOf(BadRequestException.class)
            .hasMessageContaining("Newsletter token has expired");

        verify(tokenService).validateToken(tokenValue, NewsletterTokenType.CONFIRMATION);
        verify(subscriberRepository, never()).findByEmail(anyString());
        verify(subscriberRepository, never()).save(any(NewsletterSubscriber.class));
    }

    @Test
    @DisplayName("Should handle used token")
    void shouldHandleUsedToken() {
        // Given
        String tokenValue = "used-token-123";
        String ipAddress = "192.168.1.1";
        String userAgent = "Mozilla/5.0";
        
        when(tokenService.validateToken(tokenValue, NewsletterTokenType.CONFIRMATION))
            .thenThrow(new BadRequestException("Newsletter token has already been used"));

        // When & Then
        assertThatThrownBy(() -> newsletterService.confirmSubscription(tokenValue, ipAddress, userAgent))
            .isInstanceOf(BadRequestException.class)
            .hasMessageContaining("Newsletter token has already been used");

        verify(tokenService).validateToken(tokenValue, NewsletterTokenType.CONFIRMATION);
        verify(subscriberRepository, never()).findByEmail(anyString());
        verify(subscriberRepository, never()).save(any(NewsletterSubscriber.class));
    }

    @Test
    @DisplayName("Should confirm subscription with null IP and user agent")
    void shouldConfirmSubscriptionWithNullIpAndUserAgent() {
        // Given
        String tokenValue = "valid-token-123";
        
        NewsletterToken validToken = NewsletterToken.forConfirmation("test@example.com").build();
        existingSubscriber.setStatus(SubscriptionStatus.PENDING);
        
        when(tokenService.validateToken(tokenValue, NewsletterTokenType.CONFIRMATION)).thenReturn(validToken);
        when(subscriberRepository.findByEmail("test@example.com")).thenReturn(Optional.of(existingSubscriber));
        when(subscriberRepository.save(any(NewsletterSubscriber.class))).thenReturn(existingSubscriber);
        when(tokenService.markTokenAsUsed(tokenValue, NewsletterTokenType.CONFIRMATION)).thenReturn(validToken);

        // When
        NewsletterSubscriptionResponse response = newsletterService.confirmSubscription(tokenValue, null, null);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.status()).isEqualTo(SubscriptionStatus.CONFIRMED);
        assertThat(response.message()).contains("confirmed successfully");
        
        verify(tokenService).validateToken(tokenValue, NewsletterTokenType.CONFIRMATION);
        verify(subscriberRepository).findByEmail("test@example.com");
        verify(subscriberRepository).save(any(NewsletterSubscriber.class));
        verify(tokenService).markTokenAsUsed(tokenValue, NewsletterTokenType.CONFIRMATION);
    }

    @Test
    @DisplayName("Should send welcome email async after confirmation")
    void shouldSendWelcomeEmailAsyncAfterConfirmation() {
        // Given
        NewsletterSubscriber confirmedSubscriber = NewsletterSubscriber.withConsent(
            "welcome@example.com", "192.168.1.1", "Mozilla/5.0", "1.0"
        ).build();
        confirmedSubscriber.setStatus(SubscriptionStatus.CONFIRMED);
        
        NewsletterToken unsubToken = NewsletterToken.forUnsubscribe("welcome@example.com").build();
        when(tokenService.generateUnsubscribeToken(
            confirmedSubscriber.getEmail(),
            confirmedSubscriber.getConsentIpAddress(),
            confirmedSubscriber.getConsentUserAgent()
        )).thenReturn(unsubToken);
        doNothing().when(emailService).sendNewsletterWelcome("welcome@example.com", unsubToken);

        // When
        newsletterService.sendWelcomeEmailAsync(confirmedSubscriber);

        // Then
        verify(tokenService).generateUnsubscribeToken(
            confirmedSubscriber.getEmail(),
            confirmedSubscriber.getConsentIpAddress(),
            confirmedSubscriber.getConsentUserAgent()
        );
        verify(emailService).sendNewsletterWelcome("welcome@example.com", unsubToken);
    }

    @Test
    @DisplayName("Should handle welcome email failure gracefully")
    void shouldHandleWelcomeEmailFailureGracefully() {
        // Given
        NewsletterSubscriber confirmedSubscriber = NewsletterSubscriber.withConsent(
            "welcome@example.com", "192.168.1.1", "Mozilla/5.0", "1.0"
        ).build();
        
        when(tokenService.generateUnsubscribeToken(anyString(), anyString(), anyString()))
            .thenThrow(new RuntimeException("Token generation failed"));

        // When - Should not throw exception for async method
        newsletterService.sendWelcomeEmailAsync(confirmedSubscriber);

        // Then
        verify(tokenService).generateUnsubscribeToken(
            confirmedSubscriber.getEmail(),
            confirmedSubscriber.getConsentIpAddress(),
            confirmedSubscriber.getConsentUserAgent()
        );
        verify(emailService, never()).sendNewsletterWelcome(anyString(), any(NewsletterToken.class));
    }
}