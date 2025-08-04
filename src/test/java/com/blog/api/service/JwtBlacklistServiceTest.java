package com.blog.api.service;

import com.blog.api.entity.RevokedToken;
import com.blog.api.repository.RevokedTokenRepository;
import com.blog.api.util.JwtUtil;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Comprehensive test suite for JwtBlacklistService.
 * Tests all critical functionality including revocation, blacklist checks,
 * rate limiting, cleanup, and error handling.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("JWT Blacklist Service Tests")
class JwtBlacklistServiceTest {

    @Mock
    private RevokedTokenRepository revokedTokenRepository;

    @Mock
    private JwtUtil jwtUtil;

    private JwtBlacklistService jwtBlacklistService;
    private MeterRegistry meterRegistry;

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        jwtBlacklistService = new JwtBlacklistService(meterRegistry);
        
        // Inject mocked dependencies
        ReflectionTestUtils.setField(jwtBlacklistService, "revokedTokenRepository", revokedTokenRepository);
        ReflectionTestUtils.setField(jwtBlacklistService, "jwtUtil", jwtUtil);
        ReflectionTestUtils.setField(jwtBlacklistService, "maxRevocationsPerUserPerHour", 10);
        ReflectionTestUtils.setField(jwtBlacklistService, "cleanupEnabled", true);
        ReflectionTestUtils.setField(jwtBlacklistService, "monitoringEnabled", true);
    }

    // =====================================================================
    // Token Revocation Check Tests
    // =====================================================================

    @Test
    @DisplayName("Deve verificar se token está revogado quando existe na blacklist")
    void isTokenRevoked_WhenTokenExists_ShouldReturnTrue() {
        // Given
        String jti = "test-jti-123";
        when(revokedTokenRepository.existsByTokenJti(jti)).thenReturn(true);

        // When
        boolean result = jwtBlacklistService.isTokenRevoked(jti);

        // Then
        assertTrue(result);
        verify(revokedTokenRepository).existsByTokenJti(jti);
    }

    @Test
    @DisplayName("Deve retornar false quando token não está na blacklist")
    void isTokenRevoked_WhenTokenDoesNotExist_ShouldReturnFalse() {
        // Given
        String jti = "non-existent-jti";
        when(revokedTokenRepository.existsByTokenJti(jti)).thenReturn(false);

        // When
        boolean result = jwtBlacklistService.isTokenRevoked(jti);

        // Then
        assertFalse(result);
        verify(revokedTokenRepository).existsByTokenJti(jti);
    }

    @Test
    @DisplayName("Deve retornar false quando ocorre erro na base de dados")
    void isTokenRevoked_WhenDatabaseError_ShouldReturnFalse() {
        // Given
        String jti = "error-jti";
        when(revokedTokenRepository.existsByTokenJti(jti))
            .thenThrow(new RuntimeException("Database connection error"));

        // When
        boolean result = jwtBlacklistService.isTokenRevoked(jti);

        // Then
        assertFalse(result); // Should return false on error to avoid blocking users
        verify(revokedTokenRepository).existsByTokenJti(jti);
    }

    // =====================================================================
    // Token Revocation Tests
    // =====================================================================

    @Test
    @DisplayName("Deve revogar token quando parâmetros são válidos")
    void revokeToken_WhenValidParameters_ShouldSaveRevokedToken() {
        // Given
        String jti = "valid-jti-123";
        Long userId = 1L;
        RevokedToken.RevokeReason reason = RevokedToken.RevokeReason.LOGOUT;
        Date expiration = new Date(System.currentTimeMillis() + 86400000); // 24 hours

        when(revokedTokenRepository.existsByTokenJti(jti)).thenReturn(false);
        when(revokedTokenRepository.hasUserExceededRateLimit(userId, 10, 60)).thenReturn(false);
        when(jwtUtil.getExpirationDateFromToken(jti)).thenReturn(expiration);

        // When
        jwtBlacklistService.revokeToken(jti, userId, reason);

        // Then
        verify(revokedTokenRepository).save(any(RevokedToken.class));
        verify(revokedTokenRepository).existsByTokenJti(jti);
    }

    @Test
    @DisplayName("Não deve salvar novamente quando token já está revogado")
    void revokeToken_WhenTokenAlreadyRevoked_ShouldNotSaveAgain() {
        // Given
        String jti = "already-revoked-jti";
        Long userId = 1L;
        RevokedToken.RevokeReason reason = RevokedToken.RevokeReason.LOGOUT;

        when(revokedTokenRepository.existsByTokenJti(jti)).thenReturn(true);

        // When
        jwtBlacklistService.revokeToken(jti, userId, reason);

        // Then
        verify(revokedTokenRepository, never()).save(any(RevokedToken.class));
        verify(revokedTokenRepository).existsByTokenJti(jti);
    }

    @Test
    @DisplayName("Deve lançar SecurityException quando limite de taxa é excedido")
    void revokeToken_WhenRateLimitExceeded_ShouldThrowSecurityException() {
        // Given
        String jti = "rate-limited-jti";
        Long userId = 1L;
        RevokedToken.RevokeReason reason = RevokedToken.RevokeReason.LOGOUT;

        when(revokedTokenRepository.hasUserExceededRateLimit(userId, 10, 60)).thenReturn(true);

        // When & Then
        SecurityException exception = assertThrows(SecurityException.class, () ->
            jwtBlacklistService.revokeToken(jti, userId, reason)
        );

        assertEquals("Token revocation rate limit exceeded", exception.getMessage());
        verify(revokedTokenRepository, never()).save(any(RevokedToken.class));
    }

    @Test
    @DisplayName("Deve lançar IllegalArgumentException quando JTI é nulo")
    void revokeToken_WhenJtiIsNull_ShouldThrowIllegalArgumentException() {
        // Given
        String jti = null;
        Long userId = 1L;
        RevokedToken.RevokeReason reason = RevokedToken.RevokeReason.LOGOUT;

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
            jwtBlacklistService.revokeToken(jti, userId, reason)
        );

        assertEquals("Token JTI cannot be null or empty", exception.getMessage());
    }

    @Test
    @DisplayName("Deve lançar IllegalArgumentException quando JTI está vazio")
    void revokeToken_WhenJtiIsEmpty_ShouldThrowIllegalArgumentException() {
        // Given
        String jti = "";
        Long userId = 1L;
        RevokedToken.RevokeReason reason = RevokedToken.RevokeReason.LOGOUT;

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
            jwtBlacklistService.revokeToken(jti, userId, reason)
        );

        assertEquals("Token JTI cannot be null or empty", exception.getMessage());
    }

    @Test
    @DisplayName("Deve lançar IllegalArgumentException quando ID do usuário é nulo")
    void revokeToken_WhenUserIdIsNull_ShouldThrowIllegalArgumentException() {
        // Given
        String jti = "valid-jti";
        Long userId = null;
        RevokedToken.RevokeReason reason = RevokedToken.RevokeReason.LOGOUT;

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
            jwtBlacklistService.revokeToken(jti, userId, reason)
        );

        assertEquals("User ID cannot be null", exception.getMessage());
    }

    @Test
    @DisplayName("Deve lançar IllegalArgumentException quando razão é nula")
    void revokeToken_WhenReasonIsNull_ShouldThrowIllegalArgumentException() {
        // Given
        String jti = "valid-jti";
        Long userId = 1L;
        RevokedToken.RevokeReason reason = null;

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
            jwtBlacklistService.revokeToken(jti, userId, reason)
        );

        assertEquals("Revocation reason cannot be null", exception.getMessage());
    }

    // =====================================================================
    // Token String Revocation Tests
    // =====================================================================

    @Test
    @DisplayName("Deve extrair JTI do token e revogar quando token é válido")
    void revokeTokenFromString_WhenValidToken_ShouldExtractJtiAndRevoke() {
        // Given
        String token = "valid.jwt.token";
        String jti = "extracted-jti";
        Long userId = 1L;
        RevokedToken.RevokeReason reason = RevokedToken.RevokeReason.LOGOUT;
        Date expiration = new Date(System.currentTimeMillis() + 86400000);

        when(jwtUtil.getJtiFromToken(token)).thenReturn(jti);
        when(revokedTokenRepository.existsByTokenJti(jti)).thenReturn(false);
        when(revokedTokenRepository.hasUserExceededRateLimit(userId, 10, 60)).thenReturn(false);
        when(jwtUtil.getExpirationDateFromToken(jti)).thenReturn(expiration);

        // When
        jwtBlacklistService.revokeTokenFromString(token, userId, reason);

        // Then
        verify(jwtUtil).getJtiFromToken(token);
        verify(revokedTokenRepository).save(any(RevokedToken.class));
    }

    @Test
    @DisplayName("Deve lançar IllegalArgumentException quando token é inválido")
    void revokeTokenFromString_WhenInvalidToken_ShouldThrowIllegalArgumentException() {
        // Given
        String token = "invalid.token";
        Long userId = 1L;
        RevokedToken.RevokeReason reason = RevokedToken.RevokeReason.LOGOUT;

        when(jwtUtil.getJtiFromToken(token)).thenThrow(new RuntimeException("Invalid token"));

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
            jwtBlacklistService.revokeTokenFromString(token, userId, reason)
        );

        assertEquals("Invalid token format", exception.getMessage());
    }

    // =====================================================================
    // User Token Revocation Tests
    // =====================================================================

    @Test
    @DisplayName("Deve revogar todos os tokens do usuário quando ID é válido")
    void revokeAllUserTokens_WhenValidUserId_ShouldDeleteAllUserTokens() {
        // Given
        Long userId = 1L;
        RevokedToken.RevokeReason reason = RevokedToken.RevokeReason.PASSWORD_CHANGE;
        int deletedCount = 5;

        when(revokedTokenRepository.deleteByUserId(userId)).thenReturn(deletedCount);

        // When
        int result = jwtBlacklistService.revokeAllUserTokens(userId, reason);

        // Then
        assertEquals(deletedCount, result);
        verify(revokedTokenRepository).deleteByUserId(userId);
    }

    @Test
    @DisplayName("Deve lançar IllegalArgumentException quando ID do usuário é nulo")
    void revokeAllUserTokens_WhenUserIdIsNull_ShouldThrowIllegalArgumentException() {
        // Given
        Long userId = null;
        RevokedToken.RevokeReason reason = RevokedToken.RevokeReason.PASSWORD_CHANGE;

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
            jwtBlacklistService.revokeAllUserTokens(userId, reason)
        );

        assertEquals("User ID cannot be null", exception.getMessage());
    }

    // =====================================================================
    // Token Information Tests
    // =====================================================================

    @Test
    @DisplayName("Deve retornar informações do token quando token existe")
    void getRevokedTokenInfo_WhenTokenExists_ShouldReturnTokenInfo() {
        // Given
        String jti = "existing-jti";
        RevokedToken expectedToken = RevokedToken.builder()
            .tokenJti(jti)
            .userId(1L)
            .reason(RevokedToken.RevokeReason.LOGOUT)
            .build();

        when(revokedTokenRepository.findByTokenJti(jti)).thenReturn(Optional.of(expectedToken));

        // When
        RevokedToken result = jwtBlacklistService.getRevokedTokenInfo(jti);

        // Then
        assertNotNull(result);
        assertEquals(jti, result.getTokenJti());
        assertEquals(RevokedToken.RevokeReason.LOGOUT, result.getReason());
    }

    @Test
    @DisplayName("Deve retornar null quando token não existe")
    void getRevokedTokenInfo_WhenTokenDoesNotExist_ShouldReturnNull() {
        // Given
        String jti = "non-existent-jti";
        when(revokedTokenRepository.findByTokenJti(jti)).thenReturn(Optional.empty());

        // When
        RevokedToken result = jwtBlacklistService.getRevokedTokenInfo(jti);

        // Then
        assertNull(result);
    }

    // =====================================================================
    // Rate Limiting Tests
    // =====================================================================

    @Test
    @DisplayName("Deve retornar false quando ID do usuário é nulo para limite de taxa")
    void isRateLimitExceeded_WhenUserIdIsNull_ShouldReturnFalse() {
        // Given
        Long userId = null;

        // When
        boolean result = jwtBlacklistService.isRateLimitExceeded(userId);

        // Then
        assertFalse(result);
    }

    @Test
    @DisplayName("Deve retornar false quando limite de taxa não foi excedido")
    void isRateLimitExceeded_WhenLimitNotExceeded_ShouldReturnFalse() {
        // Given
        Long userId = 1L;
        when(revokedTokenRepository.hasUserExceededRateLimit(userId, 10, 60)).thenReturn(false);

        // When
        boolean result = jwtBlacklistService.isRateLimitExceeded(userId);

        // Then
        assertFalse(result);
    }

    @Test
    @DisplayName("Deve retornar true quando limite de taxa foi excedido")
    void isRateLimitExceeded_WhenLimitExceeded_ShouldReturnTrue() {
        // Given
        Long userId = 1L;
        when(revokedTokenRepository.hasUserExceededRateLimit(userId, 10, 60)).thenReturn(true);

        // When
        boolean result = jwtBlacklistService.isRateLimitExceeded(userId);

        // Then
        assertTrue(result);
    }

    // =====================================================================
    // Statistics Tests
    // =====================================================================

    @Test
    @DisplayName("Deve retornar contagem correta de revogações do usuário")
    void getUserRevocationCount_ShouldReturnCorrectCount() {
        // Given
        Long userId = 1L;
        Duration period = Duration.ofHours(1);
        long expectedCount = 3L;

        when(revokedTokenRepository.countByUserIdAndRevokedAtAfter(eq(userId), any(LocalDateTime.class)))
            .thenReturn(expectedCount);

        // When
        long result = jwtBlacklistService.getUserRevocationCount(userId, period);

        // Then
        assertEquals(expectedCount, result);
    }

    @Test
    @DisplayName("Deve retornar contagem correta por razão de revogação")
    void getRevocationCountByReason_ShouldReturnCorrectCount() {
        // Given
        RevokedToken.RevokeReason reason = RevokedToken.RevokeReason.LOGOUT;
        Duration period = Duration.ofDays(1);
        long expectedCount = 5L;

        when(revokedTokenRepository.countByReasonAndRevokedAtAfter(eq(reason), any(LocalDateTime.class)))
            .thenReturn(expectedCount);

        // When
        long result = jwtBlacklistService.getRevocationCountByReason(reason, period);

        // Then
        assertEquals(expectedCount, result);
    }

    @Test
    @DisplayName("Deve retornar tokens revogados recentes")
    void getRecentRevocations_ShouldReturnRecentTokens() {
        // Given
        int minutes = 30;
        List<RevokedToken> expectedTokens = List.of(
            RevokedToken.builder().tokenJti("jti1").reason(RevokedToken.RevokeReason.LOGOUT).build(),
            RevokedToken.builder().tokenJti("jti2").reason(RevokedToken.RevokeReason.ADMIN_REVOKE).build()
        );

        when(revokedTokenRepository.findRevocationsInLastMinutes(minutes)).thenReturn(expectedTokens);

        // When
        List<RevokedToken> result = jwtBlacklistService.getRecentRevocations(minutes);

        // Then
        assertEquals(expectedTokens.size(), result.size());
        assertEquals("jti1", result.get(0).getTokenJti());
        assertEquals("jti2", result.get(1).getTokenJti());
    }

    // =====================================================================
    // Cleanup Tests
    // =====================================================================

    @Test
    @DisplayName("Deve deletar tokens expirados quando limpeza está habilitada")
    void cleanupExpiredTokens_WhenCleanupEnabled_ShouldDeleteExpiredTokens() {
        // Given
        int deletedCount = 10;
        when(revokedTokenRepository.deleteByExpiresAtBefore(any(LocalDateTime.class)))
            .thenReturn(deletedCount);

        // When
        jwtBlacklistService.cleanupExpiredTokens();

        // Then
        verify(revokedTokenRepository).deleteByExpiresAtBefore(any(LocalDateTime.class));
    }

    @Test
    @DisplayName("Não deve deletar tokens quando limpeza está desabilitada")
    void cleanupExpiredTokens_WhenCleanupDisabled_ShouldNotDeleteTokens() {
        // Given
        ReflectionTestUtils.setField(jwtBlacklistService, "cleanupEnabled", false);

        // When
        jwtBlacklistService.cleanupExpiredTokens();

        // Then
        verify(revokedTokenRepository, never()).deleteByExpiresAtBefore(any(LocalDateTime.class));
    }

    @Test
    @DisplayName("Deve lidar graciosamente com erro de banco de dados na limpeza")
    void cleanupExpiredTokens_WhenDatabaseError_ShouldHandleGracefully() {
        // Given
        when(revokedTokenRepository.deleteByExpiresAtBefore(any(LocalDateTime.class)))
            .thenThrow(new RuntimeException("Database error"));

        // When & Then (should not throw exception)
        assertDoesNotThrow(() -> jwtBlacklistService.cleanupExpiredTokens());
    }

    // =====================================================================
    // Monitoring Tests
    // =====================================================================

    @Test
    @DisplayName("Deve atualizar métricas quando monitoramento está habilitado")
    void updateActiveTokensMetrics_WhenMonitoringEnabled_ShouldUpdateMetrics() {
        // Given
        long activeCount = 25L;
        when(revokedTokenRepository.countActiveRevokedTokens(any(LocalDateTime.class)))
            .thenReturn(activeCount);

        // When
        jwtBlacklistService.updateActiveTokensMetrics();

        // Then
        verify(revokedTokenRepository).countActiveRevokedTokens(any(LocalDateTime.class));
    }

    @Test
    @DisplayName("Não deve atualizar métricas quando monitoramento está desabilitado")
    void updateActiveTokensMetrics_WhenMonitoringDisabled_ShouldNotUpdateMetrics() {
        // Given
        ReflectionTestUtils.setField(jwtBlacklistService, "monitoringEnabled", false);

        // When
        jwtBlacklistService.updateActiveTokensMetrics();

        // Then
        verify(revokedTokenRepository, never()).countActiveRevokedTokens(any(LocalDateTime.class));
    }

    @Test
    @DisplayName("Deve lidar graciosamente com erro de banco de dados nas métricas")
    void updateActiveTokensMetrics_WhenDatabaseError_ShouldHandleGracefully() {
        // Given
        when(revokedTokenRepository.countActiveRevokedTokens(any(LocalDateTime.class)))
            .thenThrow(new RuntimeException("Database error"));

        // When & Then (should not throw exception)
        assertDoesNotThrow(() -> jwtBlacklistService.updateActiveTokensMetrics());
    }

    // =====================================================================
    // Integration-style Tests
    // =====================================================================

    @Test
    @DisplayName("Deve funcionar corretamente no fluxo de integração de revogar e verificar")
    void revokeAndCheck_IntegrationFlow_ShouldWorkCorrectly() {
        // Given
        String jti = "integration-test-jti";
        Long userId = 1L;
        RevokedToken.RevokeReason reason = RevokedToken.RevokeReason.LOGOUT;
        Date expiration = new Date(System.currentTimeMillis() + 86400000);

        // Setup for revocation
        when(revokedTokenRepository.existsByTokenJti(jti)).thenReturn(false);
        when(revokedTokenRepository.hasUserExceededRateLimit(userId, 10, 60)).thenReturn(false);
        when(jwtUtil.getExpirationDateFromToken(jti)).thenReturn(expiration);

        // When - Revoke token
        jwtBlacklistService.revokeToken(jti, userId, reason);

        // Setup for check
        when(revokedTokenRepository.existsByTokenJti(jti)).thenReturn(true);

        // When - Check if revoked
        boolean isRevoked = jwtBlacklistService.isTokenRevoked(jti);

        // Then
        assertTrue(isRevoked);
        verify(revokedTokenRepository).save(any(RevokedToken.class));
        verify(revokedTokenRepository, times(2)).existsByTokenJti(jti);
    }
}

