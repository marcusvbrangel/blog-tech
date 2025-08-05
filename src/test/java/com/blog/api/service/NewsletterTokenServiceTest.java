package com.blog.api.service;

import com.blog.api.entity.NewsletterToken;
import com.blog.api.entity.NewsletterTokenType;
import com.blog.api.exception.BadRequestException;
import com.blog.api.repository.NewsletterTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for NewsletterTokenService.
 * Tests token generation, validation, rate limiting, and cleanup.
 * 
 * @author AI-Driven Development
 * @version 1.0
 * @since 2025-08-05
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("NewsletterTokenService Unit Tests")
class NewsletterTokenServiceTest {

    @Mock
    private NewsletterTokenRepository tokenRepository;

    @InjectMocks
    private NewsletterTokenService tokenService;

    private String testEmail;
    private String testIpAddress;
    private String testUserAgent;
    private NewsletterToken mockToken;

    @BeforeEach
    void setUp() {
        testEmail = "test@example.com";
        testIpAddress = "192.168.1.1";
        testUserAgent = "Mozilla/5.0 Test Browser";

        // Set default configuration values
        ReflectionTestUtils.setField(tokenService, "maxConfirmationAttemptsPerHour", 3);
        ReflectionTestUtils.setField(tokenService, "maxUnsubscribeAttemptsPerHour", 2);
        ReflectionTestUtils.setField(tokenService, "maxDataRequestAttemptsPerDay", 1);
        
        // Set Duration values for tests
        ReflectionTestUtils.setField(tokenService, "confirmationTokenExpiration", Duration.ofHours(48));
        ReflectionTestUtils.setField(tokenService, "unsubscribeTokenExpiration", Duration.ofDays(365));
        ReflectionTestUtils.setField(tokenService, "dataRequestTokenExpiration", Duration.ofDays(7));

        mockToken = NewsletterToken.forConfirmation(testEmail, testIpAddress, testUserAgent).build();
    }

    @Test
    @DisplayName("Should generate confirmation token successfully")
    void shouldGenerateConfirmationTokenSuccessfully() {
        // Given
        when(tokenRepository.countTokensCreatedSince(eq(testEmail), eq(NewsletterTokenType.CONFIRMATION), any(LocalDateTime.class)))
                .thenReturn(0L);
        when(tokenRepository.findValidTokensByEmailAndType(eq(testEmail), eq(NewsletterTokenType.CONFIRMATION), any(LocalDateTime.class)))
                .thenReturn(List.of());
        when(tokenRepository.save(any(NewsletterToken.class))).thenReturn(mockToken);

        // When
        NewsletterToken result = tokenService.generateConfirmationToken(testEmail, testIpAddress, testUserAgent);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo(testEmail);
        assertThat(result.getTokenType()).isEqualTo(NewsletterTokenType.CONFIRMATION);
        assertThat(result.getCreatedIp()).isEqualTo(testIpAddress);
        assertThat(result.getCreatedUserAgent()).isEqualTo(testUserAgent);

        verify(tokenRepository).save(any(NewsletterToken.class));
        verify(tokenRepository).findValidTokensByEmailAndType(eq(testEmail), eq(NewsletterTokenType.CONFIRMATION), any(LocalDateTime.class));
    }

    @Test
    @DisplayName("Should generate unsubscribe token successfully")
    void shouldGenerateUnsubscribeTokenSuccessfully() {
        // Given
        when(tokenRepository.countTokensCreatedSince(eq(testEmail), eq(NewsletterTokenType.UNSUBSCRIBE), any(LocalDateTime.class)))
                .thenReturn(0L);
        when(tokenRepository.save(any(NewsletterToken.class))).thenReturn(mockToken);

        // When
        NewsletterToken result = tokenService.generateUnsubscribeToken(testEmail, testIpAddress, testUserAgent);

        // Then
        assertThat(result).isNotNull();
        verify(tokenRepository).save(any(NewsletterToken.class));
        // Should not invalidate existing unsubscribe tokens
        verify(tokenRepository, never()).findValidTokensByEmailAndType(eq(testEmail), eq(NewsletterTokenType.UNSUBSCRIBE), any(LocalDateTime.class));
    }

    @Test
    @DisplayName("Should generate data request token successfully")
    void shouldGenerateDataRequestTokenSuccessfully() {
        // Given
        when(tokenRepository.countTokensCreatedSince(eq(testEmail), eq(NewsletterTokenType.DATA_REQUEST), any(LocalDateTime.class)))
                .thenReturn(0L);
        when(tokenRepository.findValidTokensByEmailAndType(eq(testEmail), eq(NewsletterTokenType.DATA_REQUEST), any(LocalDateTime.class)))
                .thenReturn(List.of());
        when(tokenRepository.save(any(NewsletterToken.class))).thenReturn(mockToken);

        // When
        NewsletterToken result = tokenService.generateDataRequestToken(testEmail, testIpAddress, testUserAgent);

        // Then
        assertThat(result).isNotNull();
        verify(tokenRepository).save(any(NewsletterToken.class));
        verify(tokenRepository).findValidTokensByEmailAndType(eq(testEmail), eq(NewsletterTokenType.DATA_REQUEST), any(LocalDateTime.class));
    }

    @Test
    @DisplayName("Should enforce rate limiting for confirmation tokens")
    void shouldEnforceRateLimitingForConfirmationTokens() {
        // Given - user has already reached the limit
        when(tokenRepository.countTokensCreatedSince(eq(testEmail), eq(NewsletterTokenType.CONFIRMATION), any(LocalDateTime.class)))
                .thenReturn(3L); // Max limit reached

        // When & Then
        assertThatThrownBy(() -> tokenService.generateConfirmationToken(testEmail, testIpAddress, testUserAgent))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Too many requests");

        verify(tokenRepository, never()).save(any(NewsletterToken.class));
    }

    @Test
    @DisplayName("Should enforce rate limiting for data request tokens")
    void shouldEnforceRateLimitingForDataRequestTokens() {
        // Given - user has already made a data request today
        when(tokenRepository.countTokensCreatedSince(eq(testEmail), eq(NewsletterTokenType.DATA_REQUEST), any(LocalDateTime.class)))
                .thenReturn(1L); // Max limit for data requests is 1 per day

        // When & Then
        assertThatThrownBy(() -> tokenService.generateDataRequestToken(testEmail, testIpAddress, testUserAgent))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Too many requests");

        verify(tokenRepository, never()).save(any(NewsletterToken.class));
    }

    @Test
    @DisplayName("Should invalidate existing confirmation tokens")
    void shouldInvalidateExistingConfirmationTokens() {
        // Given
        NewsletterToken existingToken = NewsletterToken.forConfirmation(testEmail).build();
        when(tokenRepository.countTokensCreatedSince(eq(testEmail), eq(NewsletterTokenType.CONFIRMATION), any(LocalDateTime.class)))
                .thenReturn(0L);
        when(tokenRepository.findValidTokensByEmailAndType(eq(testEmail), eq(NewsletterTokenType.CONFIRMATION), any(LocalDateTime.class)))
                .thenReturn(List.of(existingToken));
        when(tokenRepository.save(any(NewsletterToken.class))).thenReturn(mockToken);

        // When
        tokenService.generateConfirmationToken(testEmail, testIpAddress, testUserAgent);

        // Then
        verify(tokenRepository, times(2)).save(any(NewsletterToken.class)); // Once for invalidation, once for new token
    }

    @Test
    @DisplayName("Should validate token successfully")
    void shouldValidateTokenSuccessfully() {
        // Given
        String tokenValue = "valid-token-123";
        NewsletterToken validToken = NewsletterToken.forConfirmation(testEmail).token(tokenValue).build();
        when(tokenRepository.findByTokenAndTokenType(tokenValue, NewsletterTokenType.CONFIRMATION))
                .thenReturn(Optional.of(validToken));

        // When
        NewsletterToken result = tokenService.validateToken(tokenValue, NewsletterTokenType.CONFIRMATION);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getToken()).isEqualTo(tokenValue);
        assertThat(result.isValid()).isTrue();
    }

    @Test
    @DisplayName("Should throw exception for invalid token")
    void shouldThrowExceptionForInvalidToken() {
        // Given
        String invalidToken = "invalid-token";
        when(tokenRepository.findByTokenAndTokenType(invalidToken, NewsletterTokenType.CONFIRMATION))
                .thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> tokenService.validateToken(invalidToken, NewsletterTokenType.CONFIRMATION))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Invalid or expired newsletter token");
    }

    @Test
    @DisplayName("Should throw exception for expired token")
    void shouldThrowExceptionForExpiredToken() {
        // Given
        String tokenValue = "expired-token";
        // Create a token that will be expired by mocking the expiration check
        NewsletterToken expiredToken = NewsletterToken.forConfirmation(testEmail).token(tokenValue).build();
        // Force the token to be expired by setting expiresAt to past
        ReflectionTestUtils.setField(expiredToken, "expiresAt", LocalDateTime.now().minusHours(1));
        
        when(tokenRepository.findByTokenAndTokenType(tokenValue, NewsletterTokenType.CONFIRMATION))
                .thenReturn(Optional.of(expiredToken));

        // When & Then
        assertThatThrownBy(() -> tokenService.validateToken(tokenValue, NewsletterTokenType.CONFIRMATION))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Newsletter token has expired");
    }

    @Test
    @DisplayName("Should throw exception for used token")
    void shouldThrowExceptionForUsedToken() {
        // Given
        String tokenValue = "used-token";
        NewsletterToken usedToken = NewsletterToken.forConfirmation(testEmail)
                .token(tokenValue)
                .markAsUsed()
                .build();
        when(tokenRepository.findByTokenAndTokenType(tokenValue, NewsletterTokenType.CONFIRMATION))
                .thenReturn(Optional.of(usedToken));

        // When & Then
        assertThatThrownBy(() -> tokenService.validateToken(tokenValue, NewsletterTokenType.CONFIRMATION))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Newsletter token has already been used");
    }

    @Test
    @DisplayName("Should mark token as used successfully")
    void shouldMarkTokenAsUsedSuccessfully() {
        // Given
        String tokenValue = "valid-token";
        NewsletterToken validToken = NewsletterToken.forConfirmation(testEmail).token(tokenValue).build();
        when(tokenRepository.findByTokenAndTokenType(tokenValue, NewsletterTokenType.CONFIRMATION))
                .thenReturn(Optional.of(validToken));
        when(tokenRepository.save(any(NewsletterToken.class))).thenReturn(validToken);
        when(tokenRepository.findByEmailAndTokenType(eq(testEmail), eq(NewsletterTokenType.CONFIRMATION)))
                .thenReturn(List.of());

        // When
        NewsletterToken result = tokenService.markTokenAsUsed(tokenValue, NewsletterTokenType.CONFIRMATION);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.isUsed()).isTrue();
        verify(tokenRepository).save(validToken);
    }

    @Test
    @DisplayName("Should check if can request new token correctly")
    void shouldCheckIfCanRequestNewTokenCorrectly() {
        // Given - under the limit
        when(tokenRepository.countTokensCreatedSince(eq(testEmail), eq(NewsletterTokenType.CONFIRMATION), any(LocalDateTime.class)))
                .thenReturn(2L); // Under limit of 3

        // When
        boolean canRequest = tokenService.canRequestNewToken(testEmail, NewsletterTokenType.CONFIRMATION);

        // Then
        assertThat(canRequest).isTrue();
    }

    @Test
    @DisplayName("Should return false when over rate limit")
    void shouldReturnFalseWhenOverRateLimit() {
        // Given - over the limit
        when(tokenRepository.countTokensCreatedSince(eq(testEmail), eq(NewsletterTokenType.CONFIRMATION), any(LocalDateTime.class)))
                .thenReturn(5L); // Over limit of 3

        // When
        boolean canRequest = tokenService.canRequestNewToken(testEmail, NewsletterTokenType.CONFIRMATION);

        // Then
        assertThat(canRequest).isFalse();
    }

    @Test
    @DisplayName("Should check if has valid token")
    void shouldCheckIfHasValidToken() {
        // Given
        when(tokenRepository.hasValidToken(eq(testEmail), eq(NewsletterTokenType.CONFIRMATION), any(LocalDateTime.class)))
                .thenReturn(true);

        // When
        boolean hasValid = tokenService.hasValidToken(testEmail, NewsletterTokenType.CONFIRMATION);

        // Then
        assertThat(hasValid).isTrue();
    }

    @Test
    @DisplayName("Should get most recent valid token")
    void shouldGetMostRecentValidToken() {
        // Given
        NewsletterToken recentToken = NewsletterToken.forConfirmation(testEmail).build();
        when(tokenRepository.findMostRecentValidToken(eq(testEmail), eq(NewsletterTokenType.CONFIRMATION), any(LocalDateTime.class)))
                .thenReturn(Optional.of(recentToken));

        // When
        Optional<NewsletterToken> result = tokenService.getMostRecentValidToken(testEmail, NewsletterTokenType.CONFIRMATION);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getEmail()).isEqualTo(testEmail);
    }

    @Test
    @DisplayName("Should get all tokens for email")
    void shouldGetAllTokensForEmail() {
        // Given
        List<NewsletterToken> tokens = List.of(
                NewsletterToken.forConfirmation(testEmail).build(),
                NewsletterToken.forUnsubscribe(testEmail).build()
        );
        when(tokenRepository.findByEmail(testEmail)).thenReturn(tokens);

        // When
        List<NewsletterToken> result = tokenService.getTokensForEmail(testEmail);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).extracting(NewsletterToken::getEmail).containsOnly(testEmail);
    }

    @Test
    @DisplayName("Should delete all tokens for email")
    void shouldDeleteAllTokensForEmail() {
        // Given
        when(tokenRepository.deleteByEmail(testEmail)).thenReturn(3);

        // When
        int deletedCount = tokenService.deleteAllTokensForEmail(testEmail);

        // Then
        assertThat(deletedCount).isEqualTo(3);
        verify(tokenRepository).deleteByEmail(testEmail);
    }

    @Test
    @DisplayName("Should get next allowed request time when rate limited")
    void shouldGetNextAllowedRequestTimeWhenRateLimited() {
        // Given - user is rate limited
        LocalDateTime tokenCreatedAt = LocalDateTime.now().minusMinutes(30);
        NewsletterToken recentToken = NewsletterToken.newInstance()
                .email(testEmail)
                .generateToken()
                .tokenType(NewsletterTokenType.CONFIRMATION)
                .expiresIn(48)
                .build();
        // Simulate createdAt timestamp
        ReflectionTestUtils.setField(recentToken, "createdAt", tokenCreatedAt);

        when(tokenRepository.countTokensCreatedSince(eq(testEmail), eq(NewsletterTokenType.CONFIRMATION), any(LocalDateTime.class)))
                .thenReturn(3L); // At the limit
        when(tokenRepository.findByEmailAndTokenType(testEmail, NewsletterTokenType.CONFIRMATION))
                .thenReturn(List.of(recentToken));

        // When
        Optional<LocalDateTime> nextAllowed = tokenService.getNextAllowedRequestTime(testEmail, NewsletterTokenType.CONFIRMATION);

        // Then
        assertThat(nextAllowed).isPresent();
        assertThat(nextAllowed.get()).isAfter(LocalDateTime.now());
    }

    @Test
    @DisplayName("Should return empty when not rate limited")
    void shouldReturnEmptyWhenNotRateLimited() {
        // Given - user can request new token
        when(tokenRepository.countTokensCreatedSince(eq(testEmail), eq(NewsletterTokenType.CONFIRMATION), any(LocalDateTime.class)))
                .thenReturn(1L); // Under the limit

        // When
        Optional<LocalDateTime> nextAllowed = tokenService.getNextAllowedRequestTime(testEmail, NewsletterTokenType.CONFIRMATION);

        // Then
        assertThat(nextAllowed).isEmpty();
    }

    @Test
    @DisplayName("Should find tokens about to expire")
    void shouldFindTokensAboutToExpire() {
        // Given
        List<NewsletterToken> expiringTokens = List.of(
                NewsletterToken.forConfirmation("user1@example.com").build(),
                NewsletterToken.forConfirmation("user2@example.com").build()
        );
        when(tokenRepository.findTokensAboutToExpire(eq(NewsletterTokenType.CONFIRMATION), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(expiringTokens);

        // When
        List<NewsletterToken> result = tokenService.findTokensAboutToExpire(NewsletterTokenType.CONFIRMATION, 6);

        // Then
        assertThat(result).hasSize(2);
        verify(tokenRepository).findTokensAboutToExpire(eq(NewsletterTokenType.CONFIRMATION), any(LocalDateTime.class), any(LocalDateTime.class));
    }

    @Test
    @DisplayName("Should get token statistics")
    void shouldGetTokenStatistics() {
        // Given
        when(tokenRepository.countValidTokensByType(eq(NewsletterTokenType.CONFIRMATION), any(LocalDateTime.class))).thenReturn(10L);
        when(tokenRepository.countValidTokensByType(eq(NewsletterTokenType.UNSUBSCRIBE), any(LocalDateTime.class))).thenReturn(5L);
        when(tokenRepository.countValidTokensByType(eq(NewsletterTokenType.DATA_REQUEST), any(LocalDateTime.class))).thenReturn(2L);
        when(tokenRepository.countUsedTokensByType(NewsletterTokenType.CONFIRMATION)).thenReturn(25L);
        when(tokenRepository.countUsedTokensByType(NewsletterTokenType.UNSUBSCRIBE)).thenReturn(8L);
        when(tokenRepository.countUsedTokensByType(NewsletterTokenType.DATA_REQUEST)).thenReturn(3L);

        // When
        NewsletterTokenService.NewsletterTokenStatistics stats = tokenService.getTokenStatistics();

        // Then
        assertThat(stats.validConfirmationTokens()).isEqualTo(10L);
        assertThat(stats.validUnsubscribeTokens()).isEqualTo(5L);
        assertThat(stats.validDataRequestTokens()).isEqualTo(2L);
        assertThat(stats.usedConfirmationTokens()).isEqualTo(25L);
        assertThat(stats.usedUnsubscribeTokens()).isEqualTo(8L);
        assertThat(stats.usedDataRequestTokens()).isEqualTo(3L);
        assertThat(stats.totalValidTokens()).isEqualTo(17L);
        assertThat(stats.totalUsedTokens()).isEqualTo(36L);
    }

    @Test
    @DisplayName("Should handle cleanup expired tokens")
    void shouldHandleCleanupExpiredTokens() {
        // Given
        when(tokenRepository.deleteExpiredTokens(any(LocalDateTime.class))).thenReturn(5);

        // When
        tokenService.cleanupExpiredTokens();

        // Then
        verify(tokenRepository).deleteExpiredTokens(any(LocalDateTime.class));
    }

    @Test
    @DisplayName("Should handle cleanup old used tokens")
    void shouldHandleCleanupOldUsedTokens() {
        // Given
        when(tokenRepository.deleteUsedTokensOlderThan(any(LocalDateTime.class))).thenReturn(10);

        // When
        tokenService.cleanupOldUsedTokens();

        // Then
        verify(tokenRepository).deleteUsedTokensOlderThan(any(LocalDateTime.class));
    }

    @Test
    @DisplayName("Should handle exceptions during cleanup gracefully")
    void shouldHandleExceptionsDuringCleanupGracefully() {
        // Given
        when(tokenRepository.deleteExpiredTokens(any(LocalDateTime.class)))
                .thenThrow(new RuntimeException("Database error"));

        // When & Then - should not throw exception
        assertThatCode(() -> tokenService.cleanupExpiredTokens()).doesNotThrowAnyException();
    }
}