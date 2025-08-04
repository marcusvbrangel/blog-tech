package com.blog.api.util;

import com.blog.api.service.JwtBlacklistService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests for JwtUtil blacklist integration functionality.
 * Focuses on JTI generation, blacklist checks, and token validation with revocation.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("JWT Util Blacklist Tests")
class JwtUtilBlacklistTest {

    @Mock
    private JwtBlacklistService jwtBlacklistService;

    private JwtUtil jwtUtil;
    private UserDetails testUserDetails;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        
        // Set required properties
        ReflectionTestUtils.setField(jwtUtil, "secret", "myTestSecretKey123456789012345678901234567890");
        ReflectionTestUtils.setField(jwtUtil, "expiration", 86400000L); // 24 hours
        ReflectionTestUtils.setField(jwtUtil, "jwtBlacklistService", jwtBlacklistService);

        // Create test user
        testUserDetails = User.withUsername("testuser")
            .password("TestPass123!")
            .authorities("ROLE_USER")
            .build();
    }

    // =====================================================================
    // JTI Generation Tests
    // =====================================================================

    @Test
    @DisplayName("Deve incluir JTI único ao gerar token")
    void generateToken_ShouldIncludeUniqueJTI() {
        // When
        String token1 = jwtUtil.generateToken(testUserDetails);
        String token2 = jwtUtil.generateToken(testUserDetails);

        // Then
        assertNotNull(token1);
        assertNotNull(token2);
        assertNotEquals(token1, token2, "Tokens should be different");

        // Extract JTIs
        String jti1 = jwtUtil.getJtiFromToken(token1);
        String jti2 = jwtUtil.getJtiFromToken(token2);

        assertNotNull(jti1);
        assertNotNull(jti2);
        assertNotEquals(jti1, jti2, "JTIs should be unique");
        
        // JTI should be UUID format (36 characters with 4 hyphens)
        assertEquals(36, jti1.length());
        assertEquals(5, jti1.split("-").length);
    }

    @Test
    @DisplayName("Deve retornar JTI válido quando token é válido")
    void getJtiFromToken_WithValidToken_ShouldReturnJTI() {
        // Given
        String token = jwtUtil.generateToken(testUserDetails);

        // When
        String jti = jwtUtil.getJtiFromToken(token);

        // Then
        assertNotNull(jti);
        assertFalse(jti.trim().isEmpty());
        // UUID format validation
        assertTrue(jti.matches("[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}"));
    }

    @Test
    @DisplayName("Deve lançar exceção quando token é inválido para extração de JTI")
    void getJtiFromToken_WithInvalidToken_ShouldThrowException() {
        // Given
        String invalidToken = "invalid.token.format";

        // When & Then
        assertThrows(Exception.class, () -> jwtUtil.getJtiFromToken(invalidToken));
    }

    // =====================================================================
    // Token Validation with Blacklist Tests
    // =====================================================================

    @Test
    @DisplayName("Deve validar token com sucesso quando não está revogado")
    void validateToken_WithNonRevokedToken_ShouldReturnTrue() {
        // Given
        String token = jwtUtil.generateToken(testUserDetails);
        String jti = jwtUtil.getJtiFromToken(token);
        
        when(jwtBlacklistService.isTokenRevoked(jti)).thenReturn(false);

        // When
        Boolean isValid = jwtUtil.validateToken(token, testUserDetails);

        // Then
        assertTrue(isValid);
        verify(jwtBlacklistService).isTokenRevoked(jti);
    }

    @Test
    @DisplayName("Deve retornar false quando token está revogado")
    void validateToken_WithRevokedToken_ShouldReturnFalse() {
        // Given
        String token = jwtUtil.generateToken(testUserDetails);
        String jti = jwtUtil.getJtiFromToken(token);
        
        when(jwtBlacklistService.isTokenRevoked(jti)).thenReturn(true);

        // When
        Boolean isValid = jwtUtil.validateToken(token, testUserDetails);

        // Then
        assertFalse(isValid);
        verify(jwtBlacklistService).isTokenRevoked(jti);
    }

    @Test
    @DisplayName("Deve pular verificação de blacklist quando serviço é nulo")
    void validateToken_WithBlacklistServiceNull_ShouldSkipBlacklistCheck() {
        // Given
        ReflectionTestUtils.setField(jwtUtil, "jwtBlacklistService", null);
        String token = jwtUtil.generateToken(testUserDetails);

        // When
        Boolean isValid = jwtUtil.validateToken(token, testUserDetails);

        // Then
        assertTrue(isValid); // Should validate normally without blacklist check
        verifyNoInteractions(jwtBlacklistService);
    }

    @Test
    @DisplayName("Deve retornar false quando nome de usuário está incorreto")
    void validateToken_WithWrongUsername_ShouldReturnFalse() {
        // Given
        String token = jwtUtil.generateToken(testUserDetails);
        String jti = jwtUtil.getJtiFromToken(token);
        
        UserDetails differentUser = User.withUsername("differentuser")
            .password("TestPass123!")
            .authorities("ROLE_USER")
            .build();
        
        when(jwtBlacklistService.isTokenRevoked(jti)).thenReturn(false);

        // When
        Boolean isValid = jwtUtil.validateToken(token, differentUser);

        // Then
        assertFalse(isValid);
        verify(jwtBlacklistService).isTokenRevoked(jti);
    }

    @Test
    @DisplayName("Deve retornar false quando serviço de blacklist lança exceção")
    void validateToken_WithBlacklistServiceException_ShouldReturnFalse() {
        // Given
        String token = jwtUtil.generateToken(testUserDetails);
        String jti = jwtUtil.getJtiFromToken(token);
        
        when(jwtBlacklistService.isTokenRevoked(jti)).thenThrow(new RuntimeException("Database error"));

        // When
        Boolean isValid = jwtUtil.validateToken(token, testUserDetails);

        // Then
        assertFalse(isValid); // Should return false on blacklist service error
        verify(jwtBlacklistService).isTokenRevoked(jti);
    }

    // =====================================================================
    // Token Format Validation Tests
    // =====================================================================

    @Test
    @DisplayName("Deve validar formato de token JWT válido")
    void isValidTokenFormat_WithValidJWT_ShouldReturnTrue() {
        // Given
        String validToken = jwtUtil.generateToken(testUserDetails);

        // When
        Boolean isValid = jwtUtil.isValidTokenFormat(validToken);

        // Then
        assertTrue(isValid);
    }

    @Test
    @DisplayName("Deve retornar false para token nulo")
    void isValidTokenFormat_WithNullToken_ShouldReturnFalse() {
        // When
        Boolean isValid = jwtUtil.isValidTokenFormat(null);

        // Then
        assertFalse(isValid);
    }

    @Test
    @DisplayName("Deve retornar false para token vazio")
    void isValidTokenFormat_WithEmptyToken_ShouldReturnFalse() {
        // When
        Boolean isValid = jwtUtil.isValidTokenFormat("");

        // Then
        assertFalse(isValid);
    }

    @Test
    @DisplayName("Deve retornar false para token com espaços em branco")
    void isValidTokenFormat_WithWhitespaceToken_ShouldReturnFalse() {
        // When
        Boolean isValid = jwtUtil.isValidTokenFormat("   ");

        // Then
        assertFalse(isValid);
    }

    @Test
    @DisplayName("Deve retornar false para formato de token inválido")
    void isValidTokenFormat_WithInvalidFormat_ShouldReturnFalse() {
        // Given
        String invalidToken = "not.a.valid.jwt.token.format";

        // When
        Boolean isValid = jwtUtil.isValidTokenFormat(invalidToken);

        // Then
        assertFalse(isValid);
    }

    @Test
    @DisplayName("Deve retornar false para token com duas partes")
    void isValidTokenFormat_WithTwoParts_ShouldReturnFalse() {
        // Given
        String twoPartToken = "header.payload";

        // When
        Boolean isValid = jwtUtil.isValidTokenFormat(twoPartToken);

        // Then
        assertFalse(isValid);
    }

    // =====================================================================
    // Header Extraction Tests
    // =====================================================================

    @Test
    @DisplayName("Deve extrair token do cabeçalho Bearer válido")
    void extractTokenFromHeader_WithValidBearerHeader_ShouldReturnToken() {
        // Given
        String token = "eyJhbGciOiJIUzI1NiJ9.payload.signature";
        String authHeader = "Bearer " + token;

        // When
        String extractedToken = jwtUtil.extractTokenFromHeader(authHeader);

        // Then
        assertEquals(token, extractedToken);
    }

    @Test
    @DisplayName("Deve retornar null quando cabeçalho é nulo")
    void extractTokenFromHeader_WithNullHeader_ShouldReturnNull() {
        // When
        String extractedToken = jwtUtil.extractTokenFromHeader(null);

        // Then
        assertNull(extractedToken);
    }

    @Test
    @DisplayName("Deve retornar null para cabeçalho inválido")
    void extractTokenFromHeader_WithInvalidHeader_ShouldReturnNull() {
        // Given
        String invalidHeader = "Invalid header format";

        // When
        String extractedToken = jwtUtil.extractTokenFromHeader(invalidHeader);

        // Then
        assertNull(extractedToken);
    }

    @Test
    @DisplayName("Deve retornar null para cabeçalho de autenticação básica")
    void extractTokenFromHeader_WithBasicAuth_ShouldReturnNull() {
        // Given
        String basicAuthHeader = "Basic dXNlcjpwYXNzd29yZA==";

        // When
        String extractedToken = jwtUtil.extractTokenFromHeader(basicAuthHeader);

        // Then
        assertNull(extractedToken);
    }

    @Test
    @DisplayName("Deve retornar string vazia para token Bearer vazio")
    void extractTokenFromHeader_WithEmptyBearerToken_ShouldReturnEmptyString() {
        // Given
        String authHeader = "Bearer ";

        // When
        String extractedToken = jwtUtil.extractTokenFromHeader(authHeader);

        // Then
        assertEquals("", extractedToken);
    }

    // =====================================================================
    // Token Refresh Tests
    // =====================================================================

    @Test
    @DisplayName("Deve verificar se token pode ser renovado quando não expirado")
    void canTokenBeRefreshed_WithNonExpiredToken_ShouldReturnTrue() {
        // Given
        String token = jwtUtil.generateToken(testUserDetails);

        // When
        Boolean canRefresh = jwtUtil.canTokenBeRefreshed(token);

        // Then
        assertTrue(canRefresh);
    }

    @Test
    @DisplayName("Deve retornar false para token inválido")
    void canTokenBeRefreshed_WithInvalidToken_ShouldReturnFalse() {
        // Given
        String invalidToken = "invalid.token.format";

        // When
        Boolean canRefresh = jwtUtil.canTokenBeRefreshed(invalidToken);

        // Then
        assertFalse(canRefresh);
    }

    // =====================================================================
    // Additional Claim Extraction Tests
    // =====================================================================

    @Test
    @DisplayName("Deve retornar data de emissão válida do token")
    void getIssuedAtDateFromToken_ShouldReturnValidDate() {
        // Given
        String token = jwtUtil.generateToken(testUserDetails);

        // When
        Date issuedAt = jwtUtil.getIssuedAtDateFromToken(token);

        // Then
        assertNotNull(issuedAt);
        assertTrue(issuedAt.before(new Date())); // Should be issued in the past
        assertTrue(issuedAt.after(new Date(System.currentTimeMillis() - 60000))); // But not more than 1 minute ago
    }

    @Test
    @DisplayName("Deve retornar data de expiração futura do token")
    void getExpirationDateFromToken_ShouldReturnFutureDate() {
        // Given
        String token = jwtUtil.generateToken(testUserDetails);

        // When
        Date expiration = jwtUtil.getExpirationDateFromToken(token);

        // Then
        assertNotNull(expiration);
        assertTrue(expiration.after(new Date())); // Should be in the future
    }

    @Test
    @DisplayName("Deve retornar nome de usuário correto do token")
    void getUsernameFromToken_ShouldReturnCorrectUsername() {
        // Given
        String token = jwtUtil.generateToken(testUserDetails);

        // When
        String username = jwtUtil.getUsernameFromToken(token);

        // Then
        assertEquals(testUserDetails.getUsername(), username);
    }

    // =====================================================================
    // Integration Tests
    // =====================================================================

    @Test
    @DisplayName("Deve funcionar corretamente no ciclo de vida completo do token")
    void tokenLifecycle_CreateValidateRevoke_ShouldWorkCorrectly() {
        // Step 1: Generate token
        String token = jwtUtil.generateToken(testUserDetails);
        String jti = jwtUtil.getJtiFromToken(token);
        assertNotNull(token);
        assertNotNull(jti);

        // Step 2: Validate token (not revoked)
        when(jwtBlacklistService.isTokenRevoked(jti)).thenReturn(false);
        assertTrue(jwtUtil.validateToken(token, testUserDetails));

        // Step 3: Revoke token
        when(jwtBlacklistService.isTokenRevoked(jti)).thenReturn(true);

        // Step 4: Validate token (should fail)
        assertFalse(jwtUtil.validateToken(token, testUserDetails));

        // Verify blacklist service was called
        verify(jwtBlacklistService, times(2)).isTokenRevoked(jti);
    }

    @Test
    @DisplayName("Deve gerar JTIs únicos para múltiplos tokens")
    void multipleTokens_ShouldHaveUniqueJTIs() {
        // Given - generate multiple tokens for same user
        String token1 = jwtUtil.generateToken(testUserDetails);
        String token2 = jwtUtil.generateToken(testUserDetails);
        String token3 = jwtUtil.generateToken(testUserDetails);

        // When - extract JTIs
        String jti1 = jwtUtil.getJtiFromToken(token1);
        String jti2 = jwtUtil.getJtiFromToken(token2);
        String jti3 = jwtUtil.getJtiFromToken(token3);

        // Then - all JTIs should be unique
        assertNotEquals(jti1, jti2);
        assertNotEquals(jti2, jti3);
        assertNotEquals(jti1, jti3);

        // All tokens should have same username but different JTIs
        assertEquals(testUserDetails.getUsername(), jwtUtil.getUsernameFromToken(token1));
        assertEquals(testUserDetails.getUsername(), jwtUtil.getUsernameFromToken(token2));
        assertEquals(testUserDetails.getUsername(), jwtUtil.getUsernameFromToken(token3));
    }
}