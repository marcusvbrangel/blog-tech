package com.blog.api.repository;

import com.blog.api.entity.NewsletterToken;
import com.blog.api.entity.NewsletterTokenType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

/**
 * Integration tests for NewsletterTokenRepository.
 * Tests database operations and custom queries.
 * 
 * @author AI-Driven Development
 * @version 1.0
 * @since 2025-08-05
 */
@DataJpaTest
@ActiveProfiles("test")
@DisplayName("NewsletterTokenRepository Integration Tests")
class NewsletterTokenRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private NewsletterTokenRepository tokenRepository;

    private String testEmail;
    private String testToken;
    private NewsletterToken confirmationToken;
    private NewsletterToken unsubscribeToken;

    @BeforeEach
    void setUp() {
        testEmail = "test@example.com";
        testToken = "550e8400-e29b-41d4-a716-446655440000";

        // Create and persist test tokens
        confirmationToken = NewsletterToken.forConfirmation(testEmail)
                .token(testToken)
                .build();
        entityManager.persistAndFlush(confirmationToken);

        unsubscribeToken = NewsletterToken.forUnsubscribe(testEmail).build();
        entityManager.persistAndFlush(unsubscribeToken);

        entityManager.clear();
    }

    @Test
    @DisplayName("Should find token by token string")
    void shouldFindTokenByTokenString() {
        // When
        Optional<NewsletterToken> found = tokenRepository.findByToken(testToken);

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo(testEmail);
        assertThat(found.get().getTokenType()).isEqualTo(NewsletterTokenType.CONFIRMATION);
    }

    @Test
    @DisplayName("Should find token by token and type")
    void shouldFindTokenByTokenAndType() {
        // When
        Optional<NewsletterToken> found = tokenRepository.findByTokenAndTokenType(testToken, NewsletterTokenType.CONFIRMATION);

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo(testEmail);
    }

    @Test
    @DisplayName("Should not find token with wrong type")
    void shouldNotFindTokenWithWrongType() {
        // When
        Optional<NewsletterToken> found = tokenRepository.findByTokenAndTokenType(testToken, NewsletterTokenType.UNSUBSCRIBE);

        // Then
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("Should find all tokens by email")
    void shouldFindAllTokensByEmail() {
        // When
        List<NewsletterToken> tokens = tokenRepository.findByEmail(testEmail);

        // Then
        assertThat(tokens).hasSize(2);
        assertThat(tokens).extracting(NewsletterToken::getTokenType)
                .containsExactlyInAnyOrder(NewsletterTokenType.CONFIRMATION, NewsletterTokenType.UNSUBSCRIBE);
    }

    @Test
    @DisplayName("Should find tokens by email and type")
    void shouldFindTokensByEmailAndType() {
        // When
        List<NewsletterToken> confirmationTokens = tokenRepository.findByEmailAndTokenType(testEmail, NewsletterTokenType.CONFIRMATION);

        // Then
        assertThat(confirmationTokens).hasSize(1);
        assertThat(confirmationTokens.get(0).getToken()).isEqualTo(testToken);
    }

    @Test
    @DisplayName("Should find valid tokens by email and type")
    void shouldFindValidTokensByEmailAndType() {
        // When
        List<NewsletterToken> validTokens = tokenRepository.findValidTokensByEmailAndType(
                testEmail, NewsletterTokenType.CONFIRMATION, LocalDateTime.now());

        // Then
        assertThat(validTokens).hasSize(1);
        assertThat(validTokens.get(0).isValid()).isTrue();
    }

    @Test
    @DisplayName("Should not find expired tokens as valid")
    void shouldNotFindExpiredTokensAsValid() {
        // Given - Create token that will expire soon, then make it expire
        NewsletterToken expiredToken = NewsletterToken.newInstance()
                .email("expired@example.com")
                .generateToken()
                .tokenType(NewsletterTokenType.CONFIRMATION)
                .expiresAt(LocalDateTime.now().plusSeconds(1))  // Will expire in 1 second
                .build();
        entityManager.persistAndFlush(expiredToken);

        // Wait for expiration
        try {
            Thread.sleep(1100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // When
        List<NewsletterToken> validTokens = tokenRepository.findValidTokensByEmailAndType(
                "expired@example.com", NewsletterTokenType.CONFIRMATION, LocalDateTime.now());

        // Then
        assertThat(validTokens).isEmpty();
    }

    @Test
    @DisplayName("Should not find used tokens as valid")
    void shouldNotFindUsedTokensAsValid() {
        // Given - Mark token as used
        confirmationToken.markAsUsed();
        entityManager.merge(confirmationToken);
        entityManager.flush();

        // When
        List<NewsletterToken> validTokens = tokenRepository.findValidTokensByEmailAndType(
                testEmail, NewsletterTokenType.CONFIRMATION, LocalDateTime.now());

        // Then
        assertThat(validTokens).isEmpty();
    }

    @Test
    @DisplayName("Should find unused tokens by email and type")
    void shouldFindUnusedTokensByEmailAndType() {
        // When
        List<NewsletterToken> unusedTokens = tokenRepository.findUnusedTokensByEmailAndType(
                testEmail, NewsletterTokenType.CONFIRMATION);

        // Then
        assertThat(unusedTokens).hasSize(1);
        assertThat(unusedTokens.get(0).isUsed()).isFalse();
    }

    @Test
    @DisplayName("Should check if email has valid token")
    void shouldCheckIfEmailHasValidToken() {
        // When
        boolean hasValidToken = tokenRepository.hasValidToken(
                testEmail, NewsletterTokenType.CONFIRMATION, LocalDateTime.now());

        // Then
        assertThat(hasValidToken).isTrue();
    }

    @Test
    @DisplayName("Should return false for email without valid token")
    void shouldReturnFalseForEmailWithoutValidToken() {
        // When
        boolean hasValidToken = tokenRepository.hasValidToken(
                "nonexistent@example.com", NewsletterTokenType.CONFIRMATION, LocalDateTime.now());

        // Then
        assertThat(hasValidToken).isFalse();
    }

    @Test
    @DisplayName("Should find expired tokens")
    void shouldFindExpiredTokens() {
        // Given - Create token that will expire soon
        NewsletterToken expiredToken = NewsletterToken.newInstance()
                .email("expired@example.com")
                .generateToken()
                .tokenType(NewsletterTokenType.CONFIRMATION)
                .expiresAt(LocalDateTime.now().plusSeconds(1))
                .build();
        entityManager.persistAndFlush(expiredToken);

        // Wait for expiration
        try {
            Thread.sleep(1100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // When
        List<NewsletterToken> expiredTokens = tokenRepository.findExpiredTokens(LocalDateTime.now());

        // Then
        assertThat(expiredTokens).hasSize(1);
        assertThat(expiredTokens.get(0).isExpired()).isTrue();
    }

    @Test
    @DisplayName("Should delete expired tokens")
    void shouldDeleteExpiredTokens() {
        // Given - Create token that will expire soon
        NewsletterToken expiredToken = NewsletterToken.newInstance()
                .email("expired@example.com")
                .generateToken()
                .tokenType(NewsletterTokenType.CONFIRMATION)
                .expiresAt(LocalDateTime.now().plusSeconds(1))
                .build();
        entityManager.persistAndFlush(expiredToken);
        String tokenToFind = expiredToken.getToken();

        // Wait for expiration
        try {
            Thread.sleep(1100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // When
        int deletedCount = tokenRepository.deleteExpiredTokens(LocalDateTime.now());

        // Then
        assertThat(deletedCount).isEqualTo(1);
        
        // Verify token is deleted
        Optional<NewsletterToken> found = tokenRepository.findByToken(tokenToFind);
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("Should delete tokens by email")
    void shouldDeleteTokensByEmail() {
        // When
        int deletedCount = tokenRepository.deleteByEmail(testEmail);

        // Then
        assertThat(deletedCount).isEqualTo(2); // confirmation + unsubscribe
        
        // Verify tokens are deleted
        List<NewsletterToken> remainingTokens = tokenRepository.findByEmail(testEmail);
        assertThat(remainingTokens).isEmpty();
    }

    @Test
    @DisplayName("Should delete used tokens older than cutoff date")
    void shouldDeleteUsedTokensOlderThanCutoffDate() {
        // Given - Create and use token
        NewsletterToken usedToken = NewsletterToken.newInstance()
                .email("old@example.com")
                .generateToken()
                .tokenType(NewsletterTokenType.CONFIRMATION)
                .expiresIn(24)
                .markAsUsed()
                .build();
        entityManager.persistAndFlush(usedToken);

        // When - Delete tokens used before future date (should include our token)
        int deletedCount = tokenRepository.deleteUsedTokensOlderThan(LocalDateTime.now().plusDays(1));

        // Then
        assertThat(deletedCount).isEqualTo(1);
    }


    @Test
    @DisplayName("Should find most recent valid token")
    void shouldFindMostRecentValidToken() {
        // Given - Use different email to avoid collision with setUp tokens
        String differentEmail = "mostrecent@example.com";
        NewsletterToken singleToken = NewsletterToken.forConfirmation(differentEmail).build();
        entityManager.persistAndFlush(singleToken);

        // When
        Optional<NewsletterToken> mostRecent = tokenRepository.findMostRecentValidToken(
                differentEmail, NewsletterTokenType.CONFIRMATION, LocalDateTime.now());

        // Then
        assertThat(mostRecent).isPresent();
        assertThat(mostRecent.get().getToken()).isEqualTo(singleToken.getToken());
    }

    @Test
    @DisplayName("Should find tokens about to expire")
    void shouldFindTokensAboutToExpire() {
        // Given - Create token expiring soon
        NewsletterToken soonToExpireToken = NewsletterToken.newInstance()
                .email("expiring@example.com")
                .generateToken()
                .tokenType(NewsletterTokenType.CONFIRMATION)
                .expiresAt(LocalDateTime.now().plusHours(1)) // Expires in 1 hour
                .build();
        entityManager.persistAndFlush(soonToExpireToken);

        // When - Look for tokens expiring in next 2 hours
        List<NewsletterToken> tokensAboutToExpire = tokenRepository.findTokensAboutToExpire(
                NewsletterTokenType.CONFIRMATION,
                LocalDateTime.now().plusHours(2), // reminder time
                LocalDateTime.now()
        );

        // Then
        assertThat(tokensAboutToExpire).hasSize(1);
        assertThat(tokensAboutToExpire.get(0).getEmail()).isEqualTo("expiring@example.com");
    }

    @Test
    @DisplayName("Should count valid tokens by type")
    void shouldCountValidTokensByType() {
        // When
        long confirmationCount = tokenRepository.countValidTokensByType(
                NewsletterTokenType.CONFIRMATION, LocalDateTime.now());
        long unsubscribeCount = tokenRepository.countValidTokensByType(
                NewsletterTokenType.UNSUBSCRIBE, LocalDateTime.now());

        // Then
        assertThat(confirmationCount).isEqualTo(1);
        assertThat(unsubscribeCount).isEqualTo(1);
    }

    @Test
    @DisplayName("Should count used tokens by type")
    void shouldCountUsedTokensByType() {
        // Given - Mark confirmation token as used
        confirmationToken.markAsUsed();
        entityManager.merge(confirmationToken);
        entityManager.flush();

        // When
        long usedConfirmationCount = tokenRepository.countUsedTokensByType(NewsletterTokenType.CONFIRMATION);

        // Then
        assertThat(usedConfirmationCount).isEqualTo(1);
    }


    @Test
    @DisplayName("Should handle empty results gracefully")
    void shouldHandleEmptyResultsGracefully() {
        // When
        Optional<NewsletterToken> notFound = tokenRepository.findByToken("nonexistent-token");
        List<NewsletterToken> emptyList = tokenRepository.findByEmail("nonexistent@example.com");
        boolean hasToken = tokenRepository.hasValidToken("nonexistent@example.com", NewsletterTokenType.CONFIRMATION, LocalDateTime.now());

        // Then
        assertThat(notFound).isEmpty();
        assertThat(emptyList).isEmpty();
        assertThat(hasToken).isFalse();
    }
}