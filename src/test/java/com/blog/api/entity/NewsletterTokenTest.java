package com.blog.api.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for NewsletterToken entity.
 * Tests entity creation, validation, and business methods.
 * 
 * @author AI-Driven Development
 * @version 1.0
 * @since 2025-08-05
 */
@DisplayName("NewsletterToken Entity Tests")
class NewsletterTokenTest {

    private String validEmail;
    private String validToken;
    private NewsletterTokenType validTokenType;
    private LocalDateTime futureExpiration;

    @BeforeEach
    void setUp() {
        validEmail = "test@example.com";
        validToken = "550e8400-e29b-41d4-a716-446655440000";
        validTokenType = NewsletterTokenType.CONFIRMATION;
        futureExpiration = LocalDateTime.now().plusHours(24);
    }

    @Test
    @DisplayName("Should create token successfully with Builder")
    void shouldCreateTokenSuccessfullyWithBuilder() {
        // When
        NewsletterToken token = NewsletterToken.newInstance()
                .email(validEmail)
                .token(validToken)
                .tokenType(validTokenType)
                .expiresAt(futureExpiration)
                .build();

        // Then
        assertThat(token).isNotNull();
        assertThat(token.getEmail()).isEqualTo(validEmail);
        assertThat(token.getToken()).isEqualTo(validToken);
        assertThat(token.getTokenType()).isEqualTo(validTokenType);
        assertThat(token.getExpiresAt()).isEqualTo(futureExpiration);
        assertThat(token.getUsedAt()).isNull();
        assertThat(token.isValid()).isTrue();
    }

    @Test
    @DisplayName("Should generate token automatically")
    void shouldGenerateTokenAutomatically() {
        // When
        NewsletterToken token = NewsletterToken.newInstance()
                .email(validEmail)
                .generateToken()
                .tokenType(validTokenType)
                .expiresIn(24)
                .build();

        // Then
        assertThat(token.getToken()).isNotNull();
        assertThat(token.getToken()).hasSize(36); // UUID length
        assertThat(token.getToken()).matches("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$");
    }

    @Test
    @DisplayName("Should normalize email to lowercase")
    void shouldNormalizeEmailToLowercase() {
        // Given
        String upperCaseEmail = "TEST@EXAMPLE.COM";

        // When
        NewsletterToken token = NewsletterToken.newInstance()
                .email(upperCaseEmail)
                .generateToken()
                .tokenType(validTokenType)
                .expiresIn(24)
                .build();

        // Then
        assertThat(token.getEmail()).isEqualTo("test@example.com");
    }

    @Test
    @DisplayName("Should validate email format")
    void shouldValidateEmailFormat() {
        // When & Then
        assertThatThrownBy(() -> NewsletterToken.newInstance()
                .email("invalid-email")
                .generateToken()
                .tokenType(validTokenType)
                .expiresIn(24)
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid email format");
    }

    @Test
    @DisplayName("Should require email")
    void shouldRequireEmail() {
        // When & Then
        assertThatThrownBy(() -> NewsletterToken.newInstance()
                .token(validToken)
                .tokenType(validTokenType)
                .expiresAt(futureExpiration)
                .build())
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("Email is required");
    }

    @Test
    @DisplayName("Should require token")
    void shouldRequireToken() {
        // When & Then
        assertThatThrownBy(() -> NewsletterToken.newInstance()
                .email(validEmail)
                .tokenType(validTokenType)
                .expiresAt(futureExpiration)
                .build())
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("Token is required");
    }

    @Test
    @DisplayName("Should require token type")
    void shouldRequireTokenType() {
        // When & Then
        assertThatThrownBy(() -> NewsletterToken.newInstance()
                .email(validEmail)
                .token(validToken)
                .expiresAt(futureExpiration)
                .build())
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("Token type is required");
    }

    @Test
    @DisplayName("Should require expiration date")
    void shouldRequireExpirationDate() {
        // When & Then
        assertThatThrownBy(() -> NewsletterToken.newInstance()
                .email(validEmail)
                .token(validToken)
                .tokenType(validTokenType)
                .build())
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("Expiration date is required");
    }

    @Test
    @DisplayName("Should reject past expiration date")
    void shouldRejectPastExpirationDate() {
        // Given
        LocalDateTime pastDate = LocalDateTime.now().minusHours(1);

        // When & Then
        assertThatThrownBy(() -> NewsletterToken.newInstance()
                .email(validEmail)
                .token(validToken)
                .tokenType(validTokenType)
                .expiresAt(pastDate)
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Expiration date cannot be in the past");
    }

    @Test
    @DisplayName("Should detect expired token")
    void shouldDetectExpiredToken() {
        // Given - Create token that expires in 1 second
        NewsletterToken token = NewsletterToken.newInstance()
                .email(validEmail)
                .generateToken()
                .tokenType(validTokenType)
                .expiresAt(LocalDateTime.now().plusSeconds(1))
                .build();

        // Initially should not be expired
        assertThat(token.isExpired()).isFalse();
        assertThat(token.isValid()).isTrue();

        // When (wait for expiration)
        try {
            Thread.sleep(1100); // Wait 1.1 seconds
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Then
        assertThat(token.isExpired()).isTrue();
        assertThat(token.isValid()).isFalse();
    }

    @Test
    @DisplayName("Should detect used token")
    void shouldDetectUsedToken() {
        // Given
        NewsletterToken token = NewsletterToken.newInstance()
                .email(validEmail)
                .generateToken()
                .tokenType(validTokenType)
                .expiresIn(24)
                .build();

        // When
        token.markAsUsed();

        // Then
        assertThat(token.isUsed()).isTrue();
        assertThat(token.getUsedAt()).isNotNull();
        assertThat(token.isValid()).isFalse();
    }

    @Test
    @DisplayName("Should create confirmation token with factory method")
    void shouldCreateConfirmationTokenWithFactoryMethod() {
        // When
        NewsletterToken token = NewsletterToken.forConfirmation(validEmail).build();

        // Then
        assertThat(token.getEmail()).isEqualTo(validEmail);
        assertThat(token.getTokenType()).isEqualTo(NewsletterTokenType.CONFIRMATION);
        assertThat(token.getToken()).isNotNull();
        assertThat(token.getExpiresAt()).isAfter(LocalDateTime.now().plusHours(47));
        assertThat(token.getExpiresAt()).isBefore(LocalDateTime.now().plusHours(49));
    }

    @Test
    @DisplayName("Should create unsubscribe token with factory method")
    void shouldCreateUnsubscribeTokenWithFactoryMethod() {
        // When
        NewsletterToken token = NewsletterToken.forUnsubscribe(validEmail).build();

        // Then
        assertThat(token.getEmail()).isEqualTo(validEmail);
        assertThat(token.getTokenType()).isEqualTo(NewsletterTokenType.UNSUBSCRIBE);
        assertThat(token.getToken()).isNotNull();
        assertThat(token.getExpiresAt()).isAfter(LocalDateTime.now().plusDays(364));
        assertThat(token.getExpiresAt()).isBefore(LocalDateTime.now().plusDays(366));
    }

    @Test
    @DisplayName("Should create data request token with factory method")
    void shouldCreateDataRequestTokenWithFactoryMethod() {
        // When
        NewsletterToken token = NewsletterToken.forDataRequest(validEmail).build();

        // Then
        assertThat(token.getEmail()).isEqualTo(validEmail);
        assertThat(token.getTokenType()).isEqualTo(NewsletterTokenType.DATA_REQUEST);
        assertThat(token.getToken()).isNotNull();
        assertThat(token.getExpiresAt()).isAfter(LocalDateTime.now().plusDays(6));
        assertThat(token.getExpiresAt()).isBefore(LocalDateTime.now().plusDays(8));
    }

    @Test
    @DisplayName("Should create token with metadata")
    void shouldCreateTokenWithMetadata() {
        // Given
        String ip = "192.168.1.1";
        String userAgent = "Mozilla/5.0 Test Browser";

        // When
        NewsletterToken token = NewsletterToken.forConfirmation(validEmail, ip, userAgent).build();

        // Then
        assertThat(token.getCreatedIp()).isEqualTo(ip);
        assertThat(token.getCreatedUserAgent()).isEqualTo(userAgent);
    }

    @Test
    @DisplayName("Should truncate long User-Agent")
    void shouldTruncateLongUserAgent() {
        // Given
        String longUserAgent = "A".repeat(600); // Longer than 500 chars

        // When
        NewsletterToken token = NewsletterToken.newInstance()
                .email(validEmail)
                .generateToken()
                .tokenType(validTokenType)
                .expiresIn(24)
                .createdUserAgent(longUserAgent)
                .build();

        // Then
        assertThat(token.getCreatedUserAgent()).hasSize(500);
    }

    @Test
    @DisplayName("Should validate IP address length")
    void shouldValidateIpAddressLength() {
        // Given
        String longIp = "A".repeat(50); // Longer than 45 chars

        // When & Then
        assertThatThrownBy(() -> NewsletterToken.newInstance()
                .email(validEmail)
                .generateToken()
                .tokenType(validTokenType)
                .expiresIn(24)
                .createdIp(longIp)
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("IP address must not exceed 45 characters");
    }

    @Test
    @DisplayName("Should copy token with from method")
    void shouldCopyTokenWithFromMethod() {
        // Given
        NewsletterToken original = NewsletterToken.forConfirmation(validEmail, "192.168.1.1", "Test Agent").build();

        // When
        NewsletterToken copy = NewsletterToken.from(original).build();

        // Then
        assertThat(copy.getEmail()).isEqualTo(original.getEmail());
        assertThat(copy.getToken()).isEqualTo(original.getToken());
        assertThat(copy.getTokenType()).isEqualTo(original.getTokenType());
        assertThat(copy.getExpiresAt()).isEqualTo(original.getExpiresAt());
        assertThat(copy.getCreatedIp()).isEqualTo(original.getCreatedIp());
        assertThat(copy.getCreatedUserAgent()).isEqualTo(original.getCreatedUserAgent());
    }

    @Test
    @DisplayName("Should have proper equals and hashCode based on token")
    void shouldHaveProperEqualsAndHashCodeBasedOnToken() {
        // Given
        NewsletterToken token1 = NewsletterToken.newInstance()
                .email(validEmail)
                .token(validToken)
                .tokenType(validTokenType)
                .expiresIn(24)
                .build();

        NewsletterToken token2 = NewsletterToken.newInstance()
                .email("different@example.com")
                .token(validToken) // Same token
                .tokenType(NewsletterTokenType.UNSUBSCRIBE)
                .expiresIn(1)
                .build();

        // When & Then
        assertThat(token1).isEqualTo(token2); // Same token value
        assertThat(token1.hashCode()).isEqualTo(token2.hashCode());
    }

    @Test
    @DisplayName("Should test all token types from enum")
    void shouldTestAllTokenTypesFromEnum() {
        // Test all enum values can be used
        for (NewsletterTokenType type : NewsletterTokenType.values()) {
            NewsletterToken token = NewsletterToken.newInstance()
                    .email(validEmail)
                    .generateToken()
                    .tokenType(type)
                    .expiresIn(24)
                    .build();

            assertThat(token.getTokenType()).isEqualTo(type);
        }

        // Verify all expected types exist
        assertThat(NewsletterTokenType.values()).hasSize(3);
        assertThat(NewsletterTokenType.values()).containsExactlyInAnyOrder(
                NewsletterTokenType.CONFIRMATION,
                NewsletterTokenType.UNSUBSCRIBE,
                NewsletterTokenType.DATA_REQUEST
        );
    }

    @Test
    @DisplayName("Should have meaningful toString")
    void shouldHaveMeaningfulToString() {
        // Given
        NewsletterToken token = NewsletterToken.forConfirmation(validEmail).build();

        // When
        String toString = token.toString();

        // Then
        assertThat(toString).contains("NewsletterToken");
        assertThat(toString).contains(token.getEmail());
        assertThat(toString).contains(token.getTokenType().toString());
    }
}