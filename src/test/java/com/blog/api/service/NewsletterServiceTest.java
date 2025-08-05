package com.blog.api.service;

import com.blog.api.dto.NewsletterSubscriptionRequest;
import com.blog.api.dto.NewsletterSubscriptionResponse;
import com.blog.api.entity.NewsletterSubscriber;
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
}