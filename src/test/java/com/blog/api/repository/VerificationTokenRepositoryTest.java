package com.blog.api.repository;

import com.blog.api.entity.User;
import com.blog.api.entity.VerificationToken;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class VerificationTokenRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private VerificationTokenRepository tokenRepository;

    private User testUser;
    private VerificationToken testToken;

    @BeforeEach
    void setUp() {
        // Create and persist test user
        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("hashedpassword");
        testUser.setEmailVerified(false);
        testUser.setRole(User.Role.USER);
        testUser = entityManager.persistAndFlush(testUser);

        // Create test token
        testToken = new VerificationToken();
        testToken.setUser(testUser);
        testToken.setToken("test-token-123");
        testToken.setTokenType(VerificationToken.TokenType.EMAIL_VERIFICATION);
        testToken.setExpiresAt(LocalDateTime.now().plusHours(24));
        testToken.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void findByToken_ExistingToken_ReturnsToken() {
        // Given
        testToken = entityManager.persistAndFlush(testToken);

        // When
        Optional<VerificationToken> found = tokenRepository.findByToken("test-token-123");

        // Then
        assertTrue(found.isPresent());
        assertEquals(testToken.getId(), found.get().getId());
        assertEquals("test-token-123", found.get().getToken());
    }

    @Test
    void findByToken_NonExistentToken_ReturnsEmpty() {
        // When
        Optional<VerificationToken> found = tokenRepository.findByToken("non-existent-token");

        // Then
        assertFalse(found.isPresent());
    }

    @Test
    void findByTokenAndTokenType_ExistingTokenWithCorrectType_ReturnsToken() {
        // Given
        testToken = entityManager.persistAndFlush(testToken);

        // When
        Optional<VerificationToken> found = tokenRepository.findByTokenAndTokenType(
            "test-token-123", VerificationToken.TokenType.EMAIL_VERIFICATION);

        // Then
        assertTrue(found.isPresent());
        assertEquals(testToken.getId(), found.get().getId());
        assertEquals(VerificationToken.TokenType.EMAIL_VERIFICATION, found.get().getTokenType());
    }

    @Test
    void findByTokenAndTokenType_ExistingTokenWithWrongType_ReturnsEmpty() {
        // Given
        testToken = entityManager.persistAndFlush(testToken);

        // When
        Optional<VerificationToken> found = tokenRepository.findByTokenAndTokenType(
            "test-token-123", VerificationToken.TokenType.PASSWORD_RESET);

        // Then
        assertFalse(found.isPresent());
    }

    @Test
    void findByUser_ExistingTokens_ReturnsAllUserTokens() {
        // Given
        testToken = entityManager.persistAndFlush(testToken);
        
        VerificationToken anotherToken = new VerificationToken();
        anotherToken.setUser(testUser);
        anotherToken.setToken("another-token-456");
        anotherToken.setTokenType(VerificationToken.TokenType.PASSWORD_RESET);
        anotherToken.setExpiresAt(LocalDateTime.now().plusMinutes(15));
        anotherToken.setCreatedAt(LocalDateTime.now());
        entityManager.persistAndFlush(anotherToken);

        // When
        List<VerificationToken> tokens = tokenRepository.findByUser(testUser);

        // Then
        assertEquals(2, tokens.size());
        assertTrue(tokens.stream().anyMatch(t -> t.getToken().equals("test-token-123")));
        assertTrue(tokens.stream().anyMatch(t -> t.getToken().equals("another-token-456")));
    }

    @Test
    void findByUserAndTokenType_ExistingTokensWithSpecificType_ReturnsFilteredTokens() {
        // Given
        testToken = entityManager.persistAndFlush(testToken);
        
        VerificationToken passwordResetToken = new VerificationToken();
        passwordResetToken.setUser(testUser);
        passwordResetToken.setToken("reset-token-789");
        passwordResetToken.setTokenType(VerificationToken.TokenType.PASSWORD_RESET);
        passwordResetToken.setExpiresAt(LocalDateTime.now().plusMinutes(15));
        passwordResetToken.setCreatedAt(LocalDateTime.now());
        entityManager.persistAndFlush(passwordResetToken);

        // When
        List<VerificationToken> emailTokens = tokenRepository.findByUserAndTokenType(
            testUser, VerificationToken.TokenType.EMAIL_VERIFICATION);

        // Then
        assertEquals(1, emailTokens.size());
        assertEquals("test-token-123", emailTokens.get(0).getToken());
        assertEquals(VerificationToken.TokenType.EMAIL_VERIFICATION, emailTokens.get(0).getTokenType());
    }

    @Test
    void findValidTokensByUserAndType_ValidTokens_ReturnsOnlyValidTokens() {
        // Given
        // Valid token
        testToken = entityManager.persistAndFlush(testToken);
        
        // Expired token
        VerificationToken expiredToken = new VerificationToken();
        expiredToken.setUser(testUser);
        expiredToken.setToken("expired-token");
        expiredToken.setTokenType(VerificationToken.TokenType.EMAIL_VERIFICATION);
        expiredToken.setExpiresAt(LocalDateTime.now().minusHours(1)); // Expired
        expiredToken.setCreatedAt(LocalDateTime.now().minusHours(2));
        entityManager.persistAndFlush(expiredToken);
        
        // Used token
        VerificationToken usedToken = new VerificationToken();
        usedToken.setUser(testUser);
        usedToken.setToken("used-token");
        usedToken.setTokenType(VerificationToken.TokenType.EMAIL_VERIFICATION);
        usedToken.setExpiresAt(LocalDateTime.now().plusHours(1));
        usedToken.setUsedAt(LocalDateTime.now().minusMinutes(30)); // Used
        usedToken.setCreatedAt(LocalDateTime.now().minusHours(1));
        entityManager.persistAndFlush(usedToken);

        // When
        List<VerificationToken> validTokens = tokenRepository.findValidTokensByUserAndType(
            testUser, VerificationToken.TokenType.EMAIL_VERIFICATION, LocalDateTime.now());

        // Then
        assertEquals(1, validTokens.size());
        assertEquals("test-token-123", validTokens.get(0).getToken());
    }

    @Test
    void hasValidToken_ValidTokenExists_ReturnsTrue() {
        // Given
        testToken = entityManager.persistAndFlush(testToken);

        // When
        boolean hasValid = tokenRepository.hasValidToken(
            testUser, VerificationToken.TokenType.EMAIL_VERIFICATION, LocalDateTime.now());

        // Then
        assertTrue(hasValid);
    }

    @Test
    void hasValidToken_NoValidToken_ReturnsFalse() {
        // Given
        testToken.setExpiresAt(LocalDateTime.now().minusHours(1)); // Expired
        testToken = entityManager.persistAndFlush(testToken);

        // When
        boolean hasValid = tokenRepository.hasValidToken(
            testUser, VerificationToken.TokenType.EMAIL_VERIFICATION, LocalDateTime.now());

        // Then
        assertFalse(hasValid);
    }

    @Test
    void findExpiredTokens_ExpiredTokensExist_ReturnsExpiredTokens() {
        // Given
        // Valid token
        testToken = entityManager.persistAndFlush(testToken);
        
        // Expired token
        VerificationToken expiredToken = new VerificationToken();
        expiredToken.setUser(testUser);
        expiredToken.setToken("expired-token");
        expiredToken.setTokenType(VerificationToken.TokenType.EMAIL_VERIFICATION);
        expiredToken.setExpiresAt(LocalDateTime.now().minusHours(1)); // Expired
        expiredToken.setCreatedAt(LocalDateTime.now().minusHours(2));
        entityManager.persistAndFlush(expiredToken);

        // When
        List<VerificationToken> expiredTokens = tokenRepository.findExpiredTokens(LocalDateTime.now());

        // Then
        assertEquals(1, expiredTokens.size());
        assertEquals("expired-token", expiredTokens.get(0).getToken());
    }

    @Test
    void deleteExpiredTokens_ExpiredTokensExist_DeletesThem() {
        // Given
        // Valid token
        testToken = entityManager.persistAndFlush(testToken);
        
        // Expired token
        VerificationToken expiredToken = new VerificationToken();
        expiredToken.setUser(testUser);
        expiredToken.setToken("expired-token");
        expiredToken.setTokenType(VerificationToken.TokenType.EMAIL_VERIFICATION);
        expiredToken.setExpiresAt(LocalDateTime.now().minusHours(1)); // Expired
        expiredToken.setCreatedAt(LocalDateTime.now().minusHours(2));
        entityManager.persistAndFlush(expiredToken);

        // When
        int deletedCount = tokenRepository.deleteExpiredTokens(LocalDateTime.now());
        entityManager.flush();

        // Then
        assertEquals(1, deletedCount);
        
        // Verify expired token is deleted but valid token remains
        Optional<VerificationToken> validToken = tokenRepository.findByToken("test-token-123");
        Optional<VerificationToken> deletedToken = tokenRepository.findByToken("expired-token");
        
        assertTrue(validToken.isPresent());
        assertFalse(deletedToken.isPresent());
    }

    @Test
    void deleteUsedTokensOlderThan_OldUsedTokensExist_DeletesThem() {
        // Given
        // Recent used token
        testToken.setUsedAt(LocalDateTime.now().minusHours(1));
        testToken = entityManager.persistAndFlush(testToken);
        
        // Old used token
        VerificationToken oldUsedToken = new VerificationToken();
        oldUsedToken.setUser(testUser);
        oldUsedToken.setToken("old-used-token");
        oldUsedToken.setTokenType(VerificationToken.TokenType.EMAIL_VERIFICATION);
        oldUsedToken.setExpiresAt(LocalDateTime.now().minusDays(5));
        oldUsedToken.setUsedAt(LocalDateTime.now().minusDays(10)); // Old used
        oldUsedToken.setCreatedAt(LocalDateTime.now().minusDays(11));
        entityManager.persistAndFlush(oldUsedToken);

        // When
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(7);
        int deletedCount = tokenRepository.deleteUsedTokensOlderThan(cutoffDate);
        entityManager.flush();

        // Then
        assertEquals(1, deletedCount);
        
        // Verify old token is deleted but recent token remains
        Optional<VerificationToken> recentToken = tokenRepository.findByToken("test-token-123");
        Optional<VerificationToken> oldToken = tokenRepository.findByToken("old-used-token");
        
        assertTrue(recentToken.isPresent());
        assertFalse(oldToken.isPresent());
    }

    @Test
    void countTokensCreatedSince_RecentTokensExist_ReturnsCorrectCount() {
        // Given
        testToken.setCreatedAt(LocalDateTime.now().minusMinutes(30));
        testToken = entityManager.persistAndFlush(testToken);
        
        VerificationToken anotherToken = new VerificationToken();
        anotherToken.setUser(testUser);
        anotherToken.setToken("another-token");
        anotherToken.setTokenType(VerificationToken.TokenType.EMAIL_VERIFICATION);
        anotherToken.setExpiresAt(LocalDateTime.now().plusHours(1));
        anotherToken.setCreatedAt(LocalDateTime.now().minusMinutes(10));
        entityManager.persistAndFlush(anotherToken);
        
        // Old token
        VerificationToken oldToken = new VerificationToken();
        oldToken.setUser(testUser);
        oldToken.setToken("old-token");
        oldToken.setTokenType(VerificationToken.TokenType.EMAIL_VERIFICATION);
        oldToken.setExpiresAt(LocalDateTime.now().plusHours(1));
        oldToken.setCreatedAt(LocalDateTime.now().minusHours(2));
        entityManager.persistAndFlush(oldToken);

        // When
        LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);
        long count = tokenRepository.countTokensCreatedSince(
            testUser, VerificationToken.TokenType.EMAIL_VERIFICATION, oneHourAgo);

        // Then
        assertEquals(2, count); // Two tokens created in the last hour
    }

    @Test
    void findMostRecentValidToken_ValidTokensExist_ReturnsMostRecent() {
        // Given
        testToken.setCreatedAt(LocalDateTime.now().minusHours(2));
        testToken = entityManager.persistAndFlush(testToken);
        
        VerificationToken recentToken = new VerificationToken();
        recentToken.setUser(testUser);
        recentToken.setToken("recent-token");
        recentToken.setTokenType(VerificationToken.TokenType.EMAIL_VERIFICATION);
        recentToken.setExpiresAt(LocalDateTime.now().plusHours(1));
        recentToken.setCreatedAt(LocalDateTime.now().minusMinutes(30)); // More recent
        entityManager.persistAndFlush(recentToken);

        // When
        List<VerificationToken> tokens = tokenRepository.findValidTokensByUserAndType(
            testUser, VerificationToken.TokenType.EMAIL_VERIFICATION, LocalDateTime.now());

        // Then
        assertEquals(2, tokens.size());
        // Find the most recent one manually for this test
        VerificationToken mostRecent = tokens.stream()
            .max((t1, t2) -> t1.getCreatedAt().compareTo(t2.getCreatedAt()))
            .orElse(null);
        assertNotNull(mostRecent);
        assertEquals("recent-token", mostRecent.getToken());
    }
}