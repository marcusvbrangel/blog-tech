package com.blog.api.controller;

import com.blog.api.dto.NewsletterSubscriptionRequest;
import com.blog.api.dto.NewsletterSubscriptionResponse;
import com.blog.api.entity.SubscriptionStatus;
import com.blog.api.exception.BadRequestException;
import com.blog.api.service.NewsletterService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for NewsletterController.
 * Tests REST endpoints, validation, and error handling.
 * 
 * @author AI-Driven Development
 * @version 1.0
 * @since 2025-08-05
 */
@WebMvcTest(controllers = NewsletterController.class, 
    excludeFilters = {
        @org.springframework.context.annotation.ComponentScan.Filter(
            type = org.springframework.context.annotation.FilterType.ASSIGNABLE_TYPE, 
            classes = {
                com.blog.api.config.JwtAuthenticationFilter.class,
                com.blog.api.config.TermsComplianceFilter.class,
                com.blog.api.config.SecurityConfig.class
            })
    },
    excludeAutoConfiguration = {
        org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class,
        org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration.class
    })
@DisplayName("NewsletterController Tests")
class NewsletterControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private NewsletterService newsletterService;

    private NewsletterSubscriptionRequest validRequest;
    private NewsletterSubscriptionResponse successResponse;

    @BeforeEach
    void setUp() {
        validRequest = NewsletterSubscriptionRequest.withConsent("test@example.com", "1.0");
        
        successResponse = new NewsletterSubscriptionResponse(
            1L,
            "test@example.com",
            SubscriptionStatus.PENDING,
            LocalDateTime.now(),
            "Subscription successful! Please check your email to confirm your subscription."
        );
    }

    @Test
    @DisplayName("Should subscribe successfully with valid request")
    void shouldSubscribeSuccessfullyWithValidRequest() throws Exception {
        // Given
        when(newsletterService.subscribe(any(NewsletterSubscriptionRequest.class)))
            .thenReturn(successResponse);

        // When & Then
        mockMvc.perform(post("/api/v1/newsletter/subscribe")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest))
                .header("X-Forwarded-For", "192.168.1.1")
                .header("User-Agent", "Mozilla/5.0 Test Browser"))
                .andExpect(status().isAccepted())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.message").value(containsString("Subscription successful")));

        verify(newsletterService).subscribe(any(NewsletterSubscriptionRequest.class));
    }

    @Test
    @DisplayName("Should return 400 for invalid email")
    void shouldReturn400ForInvalidEmail() throws Exception {
        // Given
        NewsletterSubscriptionRequest invalidRequest = new NewsletterSubscriptionRequest(
            "invalid-email", true, "1.0", null, null
        );

        // When & Then
        mockMvc.perform(post("/api/v1/newsletter/subscribe")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(newsletterService, never()).subscribe(any());
    }

    @Test
    @DisplayName("Should return 400 for missing email")
    void shouldReturn400ForMissingEmail() throws Exception {
        // Given
        NewsletterSubscriptionRequest invalidRequest = new NewsletterSubscriptionRequest(
            null, true, "1.0", null, null
        );

        // When & Then
        mockMvc.perform(post("/api/v1/newsletter/subscribe")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(newsletterService, never()).subscribe(any());
    }

    @Test
    @DisplayName("Should return 400 for missing consent")
    void shouldReturn400ForMissingConsent() throws Exception {
        // Given
        NewsletterSubscriptionRequest invalidRequest = new NewsletterSubscriptionRequest(
            "test@example.com", null, "1.0", null, null
        );

        // When & Then
        mockMvc.perform(post("/api/v1/newsletter/subscribe")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(newsletterService, never()).subscribe(any());
    }

    @Test
    @DisplayName("Should return 400 for missing privacy policy version")
    void shouldReturn400ForMissingPrivacyPolicyVersion() throws Exception {
        // Given
        NewsletterSubscriptionRequest invalidRequest = new NewsletterSubscriptionRequest(
            "test@example.com", true, null, null, null
        );

        // When & Then
        mockMvc.perform(post("/api/v1/newsletter/subscribe")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(newsletterService, never()).subscribe(any());
    }

    @Test
    @DisplayName("Should return 400 when service throws BadRequestException")
    void shouldReturn400WhenServiceThrowsBadRequestException() throws Exception {
        // Given
        when(newsletterService.subscribe(any(NewsletterSubscriptionRequest.class)))
            .thenThrow(new BadRequestException("Explicit consent is required for LGPD compliance"));

        // When & Then
        mockMvc.perform(post("/api/v1/newsletter/subscribe")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isBadRequest());

        verify(newsletterService).subscribe(any(NewsletterSubscriptionRequest.class));
    }

    @Test
    @DisplayName("Should return 409 for already subscribed email")
    void shouldReturn409ForAlreadySubscribedEmail() throws Exception {
        // Given
        NewsletterSubscriptionResponse conflictResponse = new NewsletterSubscriptionResponse(
            1L,
            "test@example.com",
            SubscriptionStatus.CONFIRMED,
            LocalDateTime.now().minusDays(1),
            "You are already subscribed and confirmed to our newsletter."
        );
        
        when(newsletterService.subscribe(any(NewsletterSubscriptionRequest.class)))
            .thenReturn(conflictResponse);

        // When & Then
        mockMvc.perform(post("/api/v1/newsletter/subscribe")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value("CONFIRMED"))
                .andExpect(jsonPath("$.message").value(containsString("already subscribed")));

        verify(newsletterService).subscribe(any(NewsletterSubscriptionRequest.class));
    }

    @Test
    @DisplayName("Should capture IP from X-Forwarded-For header")
    void shouldCaptureIpFromXForwardedForHeader() throws Exception {
        // Given
        when(newsletterService.subscribe(any(NewsletterSubscriptionRequest.class)))
            .thenReturn(successResponse);

        // When & Then
        mockMvc.perform(post("/api/v1/newsletter/subscribe")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest))
                .header("X-Forwarded-For", "203.0.113.1, 198.51.100.1")
                .header("User-Agent", "Mozilla/5.0 Test Browser"))
                .andExpect(status().isAccepted());

        // Verify that service was called with enhanced request containing IP
        verify(newsletterService).subscribe(argThat(request -> 
            request.ipAddress() != null && request.ipAddress().equals("203.0.113.1")
        ));
    }

    @Test
    @DisplayName("Should capture User-Agent header")
    void shouldCaptureUserAgentHeader() throws Exception {
        // Given
        when(newsletterService.subscribe(any(NewsletterSubscriptionRequest.class)))
            .thenReturn(successResponse);

        String testUserAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) Test Browser";

        // When & Then
        mockMvc.perform(post("/api/v1/newsletter/subscribe")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest))
                .header("User-Agent", testUserAgent))
                .andExpect(status().isAccepted());

        // Verify that service was called with enhanced request containing User-Agent
        verify(newsletterService).subscribe(argThat(request -> 
            request.userAgent() != null && request.userAgent().equals(testUserAgent)
        ));
    }

    @Test
    @DisplayName("Should check subscription status successfully")
    void shouldCheckSubscriptionStatusSuccessfully() throws Exception {
        // Given
        when(newsletterService.hasActiveSubscription("test@example.com"))
            .thenReturn(true);

        // When & Then
        mockMvc.perform(get("/api/v1/newsletter/check")
                .param("email", "test@example.com"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().string("true"));

        verify(newsletterService).hasActiveSubscription("test@example.com");
    }

    @Test
    @DisplayName("Should return false for non-existent subscription")
    void shouldReturnFalseForNonExistentSubscription() throws Exception {
        // Given
        when(newsletterService.hasActiveSubscription("nonexistent@example.com"))
            .thenReturn(false);

        // When & Then
        mockMvc.perform(get("/api/v1/newsletter/check")
                .param("email", "nonexistent@example.com"))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));

        verify(newsletterService).hasActiveSubscription("nonexistent@example.com");
    }

    @Test
    @DisplayName("Should return 400 for invalid email in check endpoint")
    void shouldReturn400ForInvalidEmailInCheckEndpoint() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/newsletter/check")
                .param("email", "invalid-email"))
                .andExpect(status().isBadRequest());

        verify(newsletterService, never()).hasActiveSubscription(anyString());
    }

    @Test
    @DisplayName("Should return 400 for missing email in check endpoint")
    void shouldReturn400ForMissingEmailInCheckEndpoint() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/newsletter/check"))
                .andExpect(status().isBadRequest());

        verify(newsletterService, never()).hasActiveSubscription(anyString());
    }

    @Test
    @DisplayName("Should handle empty request body")
    void shouldHandleEmptyRequestBody() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/v1/newsletter/subscribe")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isBadRequest());

        verify(newsletterService, never()).subscribe(any());
    }

    @Test
    @DisplayName("Should handle malformed JSON")
    void shouldHandleMalformedJson() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/v1/newsletter/subscribe")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{invalid json"))
                .andExpect(status().isBadRequest());

        verify(newsletterService, never()).subscribe(any());
    }
}