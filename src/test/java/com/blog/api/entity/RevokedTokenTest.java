package com.blog.api.entity;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for RevokedToken entity including Builder pattern, factory methods,
 * validation, and business logic methods.
 */
class RevokedTokenTest {

    // =====================================================================
    // Builder Pattern Tests
    // =====================================================================

    @Test
    void builder_WithAllFields_ShouldCreateValidEntity() {
        // Given
        String jti = "test-jti-123";
        Long userId = 1L;
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiration = now.plusHours(24);
        RevokedToken.RevokeReason reason = RevokedToken.RevokeReason.LOGOUT;

        // When
        RevokedToken token = RevokedToken.builder()
            .tokenJti(jti)
            .userId(userId)
            .revokedAt(now)
            .expiresAt(expiration)
            .reason(reason)
            .createdAt(now)
            .build();

        // Then
        assertNotNull(token);
        assertEquals(jti, token.getTokenJti());
        assertEquals(userId, token.getUserId());
        assertEquals(now, token.getRevokedAt());
        assertEquals(expiration, token.getExpiresAt());
        assertEquals(reason, token.getReason());
        assertEquals(now, token.getCreatedAt());
    }

    @Test
    void builder_WithMinimalFields_ShouldCreateValidEntity() {
        // Given
        String jti = "minimal-jti";
        LocalDateTime expiration = LocalDateTime.now().plusHours(1);

        // When
        RevokedToken token = RevokedToken.builder()
            .tokenJti(jti)
            .expiresAt(expiration)
            .build();

        // Then
        assertNotNull(token);
        assertEquals(jti, token.getTokenJti());
        assertEquals(expiration, token.getExpiresAt());
        assertNull(token.getUserId()); // Optional field
        assertNull(token.getReason()); // Optional field
    }

    @Test
    void from_WithExistingEntity_ShouldCreateBuilder() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        RevokedToken original = RevokedToken.builder()
            .tokenJti("original-jti")
            .userId(1L)
            .reason(RevokedToken.RevokeReason.LOGOUT)
            .revokedAt(now)
            .expiresAt(now.plusHours(24))
            .build();

        // When
        RevokedToken modified = RevokedToken.from(original)
            .reason(RevokedToken.RevokeReason.ADMIN_REVOKE)
            .build();

        // Then
        assertEquals(original.getTokenJti(), modified.getTokenJti());
        assertEquals(original.getUserId(), modified.getUserId());
        assertEquals(original.getRevokedAt(), modified.getRevokedAt());
        assertEquals(original.getExpiresAt(), modified.getExpiresAt());
        assertEquals(RevokedToken.RevokeReason.ADMIN_REVOKE, modified.getReason()); // Modified field
    }

    // =====================================================================
    // Factory Method Tests
    // =====================================================================

    @Test
    void forLogout_ShouldCreateTokenWithLogoutReason() {
        // Given
        String jti = "logout-jti";
        Long userId = 1L;
        LocalDateTime expiration = LocalDateTime.now().plusHours(24);

        // When
        RevokedToken token = RevokedToken.forLogout(jti, userId, expiration);

        // Then
        assertNotNull(token);
        assertEquals(jti, token.getTokenJti());
        assertEquals(userId, token.getUserId());
        assertEquals(expiration, token.getExpiresAt());
        assertEquals(RevokedToken.RevokeReason.LOGOUT, token.getReason());
    }

    @Test
    void forPasswordChange_ShouldCreateTokenWithPasswordChangeReason() {
        // Given
        String jti = "password-change-jti";
        Long userId = 2L;
        LocalDateTime expiration = LocalDateTime.now().plusHours(12);

        // When
        RevokedToken token = RevokedToken.forPasswordChange(jti, userId, expiration);

        // Then
        assertNotNull(token);
        assertEquals(jti, token.getTokenJti());
        assertEquals(userId, token.getUserId());
        assertEquals(expiration, token.getExpiresAt());
        assertEquals(RevokedToken.RevokeReason.PASSWORD_CHANGE, token.getReason());
    }

    @Test
    void forAdminRevoke_ShouldCreateTokenWithAdminRevokeReason() {
        // Given
        String jti = "admin-revoke-jti";
        Long userId = 3L;
        LocalDateTime expiration = LocalDateTime.now().plusDays(1);

        // When
        RevokedToken token = RevokedToken.forAdminRevoke(jti, userId, expiration);

        // Then
        assertNotNull(token);
        assertEquals(jti, token.getTokenJti());
        assertEquals(userId, token.getUserId());
        assertEquals(expiration, token.getExpiresAt());
        assertEquals(RevokedToken.RevokeReason.ADMIN_REVOKE, token.getReason());
    }

    // =====================================================================
    // Business Logic Method Tests
    // =====================================================================

    @Test
    void isExpired_WithFutureExpiration_ShouldReturnFalse() {
        // Given
        LocalDateTime futureExpiration = LocalDateTime.now().plusHours(1);
        RevokedToken token = RevokedToken.builder()
            .tokenJti("future-jti")
            .expiresAt(futureExpiration)
            .build();

        // When & Then
        assertFalse(token.isExpired());
    }

    @Test
    void isExpired_WithPastExpiration_ShouldReturnTrue() {
        // Given
        LocalDateTime pastExpiration = LocalDateTime.now().minusHours(1);
        RevokedToken token = RevokedToken.builder()
            .tokenJti("past-jti")
            .expiresAt(pastExpiration)
            .build();

        // When & Then
        assertTrue(token.isExpired());
    }

    @Test
    void isExpired_WithNullExpiration_ShouldReturnTrue() {
        // Given
        RevokedToken token = RevokedToken.builder()
            .tokenJti("null-expiration-jti")
            .expiresAt(null)
            .build();

        // When & Then
        assertTrue(token.isExpired());
    }

    @Test
    void isRecentRevocation_WithRecentRevocation_ShouldReturnTrue() {
        // Given
        LocalDateTime recentRevocation = LocalDateTime.now().minusMinutes(30);
        RevokedToken token = RevokedToken.builder()
            .tokenJti("recent-jti")
            .revokedAt(recentRevocation)
            .expiresAt(LocalDateTime.now().plusHours(1))
            .build();

        // When & Then
        assertTrue(token.isRecentRevocation());
    }

    @Test
    void isRecentRevocation_WithOldRevocation_ShouldReturnFalse() {
        // Given
        LocalDateTime oldRevocation = LocalDateTime.now().minusHours(2);
        RevokedToken token = RevokedToken.builder()
            .tokenJti("old-jti")
            .revokedAt(oldRevocation)
            .expiresAt(LocalDateTime.now().plusHours(1))
            .build();

        // When & Then
        assertFalse(token.isRecentRevocation());
    }

    @Test
    void isRecentRevocation_WithNullRevocation_ShouldReturnFalse() {
        // Given
        RevokedToken token = RevokedToken.builder()
            .tokenJti("null-revocation-jti")
            .revokedAt(null)
            .expiresAt(LocalDateTime.now().plusHours(1))
            .build();

        // When & Then
        assertFalse(token.isRecentRevocation());
    }

    // =====================================================================
    // Equality and HashCode Tests
    // =====================================================================

    @Test
    void equals_WithSameJti_ShouldReturnTrue() {
        // Given
        String jti = "same-jti";
        RevokedToken token1 = RevokedToken.builder()
            .tokenJti(jti)
            .userId(1L)
            .expiresAt(LocalDateTime.now().plusHours(1))
            .build();
        
        RevokedToken token2 = RevokedToken.builder()
            .tokenJti(jti)
            .userId(2L) // Different user ID
            .expiresAt(LocalDateTime.now().plusHours(2)) // Different expiration
            .build();

        // When & Then
        assertEquals(token1, token2);
        assertEquals(token1.hashCode(), token2.hashCode());
    }

    @Test
    void equals_WithDifferentJti_ShouldReturnFalse() {
        // Given
        RevokedToken token1 = RevokedToken.builder()
            .tokenJti("jti-1")
            .expiresAt(LocalDateTime.now().plusHours(1))
            .build();
        
        RevokedToken token2 = RevokedToken.builder()
            .tokenJti("jti-2")
            .expiresAt(LocalDateTime.now().plusHours(1))
            .build();

        // When & Then
        assertNotEquals(token1, token2);
        assertNotEquals(token1.hashCode(), token2.hashCode());
    }

    @Test
    void equals_WithNull_ShouldReturnFalse() {
        // Given
        RevokedToken token = RevokedToken.builder()
            .tokenJti("test-jti")
            .expiresAt(LocalDateTime.now().plusHours(1))
            .build();

        // When & Then
        assertNotEquals(token, null);
    }

    @Test
    void equals_WithDifferentClass_ShouldReturnFalse() {
        // Given
        RevokedToken token = RevokedToken.builder()
            .tokenJti("test-jti")
            .expiresAt(LocalDateTime.now().plusHours(1))
            .build();

        // When & Then
        assertNotEquals(token, "not a token");
    }

    @Test
    void equals_WithSameInstance_ShouldReturnTrue() {
        // Given
        RevokedToken token = RevokedToken.builder()
            .tokenJti("test-jti")
            .expiresAt(LocalDateTime.now().plusHours(1))
            .build();

        // When & Then
        assertEquals(token, token);
    }

    // =====================================================================
    // toString() Tests
    // =====================================================================

    @Test
    void toString_ShouldContainAllFields() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        RevokedToken token = RevokedToken.builder()
            .tokenJti("test-jti-123")
            .userId(42L)
            .reason(RevokedToken.RevokeReason.LOGOUT)
            .revokedAt(now)
            .expiresAt(now.plusHours(24))
            .createdAt(now)
            .build();
        token.setId(1L);

        // When
        String toString = token.toString();

        // Then
        assertNotNull(toString);
        assertTrue(toString.contains("test-jti-123"));
        assertTrue(toString.contains("42"));
        assertTrue(toString.contains("LOGOUT"));
        assertTrue(toString.contains("id=1"));
    }

    // =====================================================================
    // Enum Tests
    // =====================================================================

    @Test
    void revokeReason_ShouldHaveAllExpectedValues() {
        // When & Then
        RevokedToken.RevokeReason[] reasons = RevokedToken.RevokeReason.values();
        
        assertEquals(5, reasons.length);
        assertArrayEquals(new RevokedToken.RevokeReason[] {
            RevokedToken.RevokeReason.LOGOUT,
            RevokedToken.RevokeReason.ADMIN_REVOKE,
            RevokedToken.RevokeReason.PASSWORD_CHANGE,
            RevokedToken.RevokeReason.ACCOUNT_LOCKED,
            RevokedToken.RevokeReason.SECURITY_BREACH
        }, reasons);
    }

    @Test
    void revokeReason_ShouldHaveCorrectStringValues() {
        // When & Then
        assertEquals("LOGOUT", RevokedToken.RevokeReason.LOGOUT.name());
        assertEquals("ADMIN_REVOKE", RevokedToken.RevokeReason.ADMIN_REVOKE.name());
        assertEquals("PASSWORD_CHANGE", RevokedToken.RevokeReason.PASSWORD_CHANGE.name());
        assertEquals("ACCOUNT_LOCKED", RevokedToken.RevokeReason.ACCOUNT_LOCKED.name());
        assertEquals("SECURITY_BREACH", RevokedToken.RevokeReason.SECURITY_BREACH.name());
    }

    // =====================================================================
    // Setter Tests (for JPA compatibility)
    // =====================================================================

    @Test
    void setters_ShouldUpdateFields() {
        // Given
        RevokedToken token = new RevokedToken();
        LocalDateTime now = LocalDateTime.now();

        // When
        token.setId(1L);
        token.setTokenJti("setter-jti");
        token.setUserId(100L);
        token.setRevokedAt(now);
        token.setExpiresAt(now.plusHours(1));
        token.setReason(RevokedToken.RevokeReason.ADMIN_REVOKE);
        token.setCreatedAt(now);

        // Then
        assertEquals(1L, token.getId());
        assertEquals("setter-jti", token.getTokenJti());
        assertEquals(100L, token.getUserId());
        assertEquals(now, token.getRevokedAt());
        assertEquals(now.plusHours(1), token.getExpiresAt());
        assertEquals(RevokedToken.RevokeReason.ADMIN_REVOKE, token.getReason());
        assertEquals(now, token.getCreatedAt());
    }

    // =====================================================================
    // Edge Case Tests
    // =====================================================================

    @Test
    void builder_WithEmptyStringJti_ShouldCreateEntity() {
        // Given & When
        RevokedToken token = RevokedToken.builder()
            .tokenJti("")
            .expiresAt(LocalDateTime.now().plusHours(1))
            .build();

        // Then
        assertNotNull(token);
        assertEquals("", token.getTokenJti());
    }

    @Test
    void isExpired_AtExactExpirationTime_ShouldReturnTrue() {
        // Given
        LocalDateTime exactExpiration = LocalDateTime.now();
        // Add a small delay to ensure time has passed
        try {
            Thread.sleep(1);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        RevokedToken token = RevokedToken.builder()
            .tokenJti("exact-expiration-jti")
            .expiresAt(exactExpiration)
            .build();

        // When & Then
        assertTrue(token.isExpired());
    }

    @Test
    void factoryMethods_WithNullParameters_ShouldCreateEntityWithNulls() {
        // Given & When
        RevokedToken logoutToken = RevokedToken.forLogout(null, null, null);
        RevokedToken passwordToken = RevokedToken.forPasswordChange(null, null, null);
        RevokedToken adminToken = RevokedToken.forAdminRevoke(null, null, null);

        // Then
        assertNotNull(logoutToken);
        assertNotNull(passwordToken);
        assertNotNull(adminToken);
        
        assertNull(logoutToken.getTokenJti());
        assertNull(passwordToken.getUserId());
        assertNull(adminToken.getExpiresAt());
    }
}