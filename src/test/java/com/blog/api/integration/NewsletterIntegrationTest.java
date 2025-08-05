package com.blog.api.integration;

import com.blog.api.dto.NewsletterSubscriptionRequest;
import com.blog.api.dto.NewsletterSubscriptionResponse;
import com.blog.api.entity.NewsletterSubscriber;
import com.blog.api.entity.NewsletterToken;
import com.blog.api.entity.SubscriptionStatus;
import com.blog.api.repository.NewsletterSubscriberRepository;
import com.blog.api.repository.NewsletterTokenRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for newsletter subscription flow.
 * Tests the complete flow from subscription to confirmation.
 * 
 * @author AI-Driven Development
 * @version 1.0
 * @since 2025-08-05
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@ActiveProfiles("test")
@Transactional
@DisplayName("Newsletter Integration Tests")
class NewsletterIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private NewsletterSubscriberRepository subscriberRepository;

    @Autowired
    private NewsletterTokenRepository tokenRepository;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
            .build();
    }

    @Test
    @DisplayName("Should complete full newsletter subscription and confirmation flow")
    void shouldCompleteFullNewsletterFlow() throws Exception {
        // Step 1: Subscribe to newsletter
        NewsletterSubscriptionRequest subscriptionRequest = new NewsletterSubscriptionRequest(
            "integration@test.com", true, "1.0", "192.168.1.1", "Test Browser"
        );

        String subscriptionResponse = mockMvc.perform(post("/api/v1/newsletter/subscribe")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(subscriptionRequest)))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.email").value("integration@test.com"))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.message").value("Subscription successful! Please check your email to confirm your subscription."))
                .andReturn()
                .getResponse()
                .getContentAsString();

        NewsletterSubscriptionResponse response = objectMapper.readValue(
            subscriptionResponse, NewsletterSubscriptionResponse.class);

        // Verify subscriber was created
        Optional<NewsletterSubscriber> subscriberOpt = subscriberRepository.findByEmail("integration@test.com");
        assertThat(subscriberOpt).isPresent();
        NewsletterSubscriber subscriber = subscriberOpt.get();
        assertThat(subscriber.getStatus()).isEqualTo(SubscriptionStatus.PENDING);
        assertThat(subscriber.getConsentIpAddress()).isEqualTo("127.0.0.1"); // MockMvc uses localhost IP

        // Step 2: Find confirmation token
        Optional<NewsletterToken> tokenOpt = tokenRepository.findValidTokenByEmail(
            "integration@test.com", 
            com.blog.api.entity.NewsletterTokenType.CONFIRMATION,
            java.time.LocalDateTime.now()
        );
        assertThat(tokenOpt).isPresent();
        String confirmationToken = tokenOpt.get().getToken();

        // Step 3: Confirm subscription using token
        String confirmationResponse = mockMvc.perform(get("/api/v1/newsletter/confirm")
                .param("token", confirmationToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("integration@test.com"))
                .andExpect(jsonPath("$.status").value("CONFIRMED"))
                .andExpect(jsonPath("$.message").value("Email confirmed successfully! Welcome to our newsletter. You'll receive updates about new posts."))
                .andReturn()
                .getResponse()
                .getContentAsString();

        // Verify subscriber is now confirmed
        subscriber = subscriberRepository.findByEmail("integration@test.com").orElseThrow();
        assertThat(subscriber.getStatus()).isEqualTo(SubscriptionStatus.CONFIRMED);
        assertThat(subscriber.getConfirmedAt()).isNotNull();

        // Verify token was marked as used
        NewsletterToken token = tokenRepository.findByToken(confirmationToken).orElseThrow();
        assertThat(token.isUsed()).isTrue();
        assertThat(token.getUsedAt()).isNotNull();
    }

    @Test
    @DisplayName("Should handle duplicate subscription attempts")
    void shouldHandleDuplicateSubscriptionAttempts() throws Exception {
        // First subscription
        NewsletterSubscriptionRequest request = new NewsletterSubscriptionRequest(
            "duplicate@test.com", true, "1.0", "192.168.1.1", "Test Browser"
        );

        mockMvc.perform(post("/api/v1/newsletter/subscribe")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.status").value("PENDING"));

        // Second subscription attempt
        mockMvc.perform(post("/api/v1/newsletter/subscribe")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.message").value("You are already subscribed. Please check your email to confirm your subscription."));
    }

    @Test
    @DisplayName("Should reject subscription without consent")
    void shouldRejectSubscriptionWithoutConsent() throws Exception {
        // Build request without consent manually to test the validation
        String jsonRequest = "{\"email\":\"noconsent@test.com\",\"consent\":false,\"appVersion\":\"1.0\",\"ipAddress\":\"192.168.1.1\",\"userAgent\":\"Test Browser\"}";

        mockMvc.perform(post("/api/v1/newsletter/subscribe")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"));

        // Verify no subscriber was created
        Optional<NewsletterSubscriber> subscriber = subscriberRepository.findByEmail("noconsent@test.com");
        assertThat(subscriber).isEmpty();
    }

    @Test
    @DisplayName("Should handle invalid confirmation token")
    void shouldHandleInvalidConfirmationToken() throws Exception {
        mockMvc.perform(get("/api/v1/newsletter/confirm")
                .param("token", "invalid-token-123"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid or expired newsletter token"));
    }

    @Test
    @DisplayName("Should handle expired confirmation token")
    void shouldHandleExpiredConfirmationToken() throws Exception {
        // Create subscriber
        NewsletterSubscriber subscriber = NewsletterSubscriber.withConsent(
            "expired@test.com", "192.168.1.1", "Test Browser", "1.0"
        ).build();
        subscriberRepository.save(subscriber);

        // Create expired token
        NewsletterToken expiredToken = NewsletterToken.forConfirmation("expired@test.com")
                .build();
        // Set expiration in the past using reflection
        ReflectionTestUtils.setField(expiredToken, "expiresAt", LocalDateTime.now().minusHours(1));
        tokenRepository.save(expiredToken);

        mockMvc.perform(get("/api/v1/newsletter/confirm")
                .param("token", expiredToken.getToken()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Newsletter token has expired"));
    }

    @Test
    @DisplayName("Should handle used confirmation token")
    void shouldHandleUsedConfirmationToken() throws Exception {
        // Create subscriber
        NewsletterSubscriber subscriber = NewsletterSubscriber.withConsent(
            "used@test.com", "192.168.1.1", "Test Browser", "1.0"
        ).build();
        subscriberRepository.save(subscriber);

        // Create used token
        NewsletterToken usedToken = NewsletterToken.forConfirmation("used@test.com")
                .build();
        // Mark as used using reflection
        ReflectionTestUtils.setField(usedToken, "usedAt", LocalDateTime.now().minusMinutes(10));
        tokenRepository.save(usedToken);

        mockMvc.perform(get("/api/v1/newsletter/confirm")
                .param("token", usedToken.getToken()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Newsletter token has already been used"));
    }

    @Test
    @DisplayName("Should check subscription status")
    void shouldCheckSubscriptionStatus() throws Exception {
        // Create confirmed subscriber
        NewsletterSubscriber subscriber = NewsletterSubscriber.withConsent(
            "check@test.com", "192.168.1.1", "Test Browser", "1.0"
        ).status(SubscriptionStatus.CONFIRMED).build();
        subscriberRepository.save(subscriber);

        // Check existing subscription
        mockMvc.perform(get("/api/v1/newsletter/check")
                .param("email", "check@test.com"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));

        // Check non-existing subscription
        mockMvc.perform(get("/api/v1/newsletter/check")
                .param("email", "nonexistent@test.com"))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));
    }

    @Test
    @DisplayName("Should handle double confirmation attempt")
    void shouldHandleDoubleConfirmationAttempt() throws Exception {
        // Subscribe
        NewsletterSubscriptionRequest request = new NewsletterSubscriptionRequest(
            "double@test.com", true, "1.0", "192.168.1.1", "Test Browser"
        );

        mockMvc.perform(post("/api/v1/newsletter/subscribe")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isAccepted());

        // Get token
        NewsletterToken token = tokenRepository.findValidTokenByEmail(
            "double@test.com",
            com.blog.api.entity.NewsletterTokenType.CONFIRMATION,
            java.time.LocalDateTime.now()
        ).orElseThrow();

        // First confirmation
        mockMvc.perform(get("/api/v1/newsletter/confirm")
                .param("token", token.getToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Email confirmed successfully! Welcome to our newsletter. You'll receive updates about new posts."));

        // Second confirmation attempt (should return conflict)
        mockMvc.perform(get("/api/v1/newsletter/confirm")
                .param("token", token.getToken()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Newsletter token has already been used"));
    }

    @Test
    @DisplayName("Should validate email format in subscription")
    void shouldValidateEmailFormatInSubscription() throws Exception {
        NewsletterSubscriptionRequest invalidEmailRequest = new NewsletterSubscriptionRequest(
            "invalid-email", true, "1.0", "192.168.1.1", "Test Browser"
        );

        mockMvc.perform(post("/api/v1/newsletter/subscribe")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidEmailRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should validate email format in check endpoint")
    void shouldValidateEmailFormatInCheckEndpoint() throws Exception {
        mockMvc.perform(get("/api/v1/newsletter/check")
                .param("email", "invalid-email"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Valid email address is required"));
    }
}