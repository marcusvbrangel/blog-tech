package com.blog.api.service;

import com.blog.api.entity.RefreshToken;
import com.blog.api.entity.User;
import com.blog.api.repository.RefreshTokenRepository;
import com.blog.api.repository.UserRepository;
import com.blog.api.util.JwtUtil;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Refresh Token Service Tests")
class RefreshTokenServiceTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CustomUserDetailsService userDetailsService;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private RefreshTokenService refreshTokenService;

    private MeterRegistry meterRegistry;
    private User testUser;
    private RefreshToken testRefreshToken;

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        refreshTokenService = new RefreshTokenService(meterRegistry);
        
        // Inject mocks manually
        ReflectionTestUtils.setField(refreshTokenService, "refreshTokenRepository", refreshTokenRepository);
        ReflectionTestUtils.setField(refreshTokenService, "userRepository", userRepository);
        ReflectionTestUtils.setField(refreshTokenService, "userDetailsService", userDetailsService);
        ReflectionTestUtils.setField(refreshTokenService, "jwtUtil", jwtUtil);
        
        // Set configuration values
        ReflectionTestUtils.setField(refreshTokenService, "refreshTokenExpiration", 7 * 24 * 60 * 60); // 7 days in seconds
        ReflectionTestUtils.setField(refreshTokenService, "maxTokensPerUser", 5);
        ReflectionTestUtils.setField(refreshTokenService, "rotationEnabled", true);
        ReflectionTestUtils.setField(refreshTokenService, "maxTokensPerHour", 10);

        // Create test data
        testUser = User.newInstance()
                .username("testuser") 
                .email("test@example.com")
                .password("TestPass123!")
                .role(User.Role.USER)
                .build();
        testUser.setId(1L);

        testRefreshToken = RefreshToken.builder()
                .userId(1L)
                .token("test-refresh-token")
                .expiresAt(LocalDateTime.now().plusDays(7))
                .deviceInfo("Test Device")
                .ipAddress("127.0.0.1")
                .build();
    }

    @Test
    @DisplayName("Deve gerar refresh token com sucesso para usuário válido")
    void generateRefreshToken_ShouldGenerateToken_WhenValidUser() {
        // Arrange
        when(userRepository.existsById(1L)).thenReturn(true);
        when(refreshTokenRepository.hasUserExceededTokenCreationRate(any(), anyInt(), any())).thenReturn(false);
        when(refreshTokenRepository.countActiveByUserId(1L)).thenReturn(0L);
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenReturn(testRefreshToken);

        // Act
        RefreshToken result = refreshTokenService.createRefreshToken(1L, "Test Device", "127.0.0.1");

        // Assert
        assertThat(result).isNotNull();
        verify(refreshTokenRepository).save(any(RefreshToken.class));
        verify(userRepository).existsById(1L);
    }

    @Test
    void createRefreshToken_ShouldThrowException_WhenUserNotFound() {
        // Arrange
        when(userRepository.existsById(1L)).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> refreshTokenService.createRefreshToken(1L, "Device", "IP"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("User not found with ID: 1");
    }

    @Test
    void createRefreshToken_ShouldThrowException_WhenRateLimitExceeded() {
        // Arrange
        when(userRepository.existsById(1L)).thenReturn(true);
        when(refreshTokenRepository.hasUserExceededTokenCreationRate(any(), anyInt(), any())).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> refreshTokenService.createRefreshToken(1L, "Device", "IP"))
                .isInstanceOf(SecurityException.class)
                .hasMessage("Token creation rate limit exceeded");
    }

    @Test
    void refreshAccessToken_ShouldReturnNewToken_WhenValidRefreshToken() {
        // Arrange
        UserDetails userDetails = mock(UserDetails.class);
        String newAccessToken = "new-access-token";
        String newRefreshToken = "new-refresh-token";
        
        RefreshToken newRefreshTokenEntity = RefreshToken.builder()
                .userId(1L)
                .token(newRefreshToken)
                .expiresAt(LocalDateTime.now().plusDays(7))
                .build();

        when(refreshTokenRepository.findActiveByToken("test-refresh-token")).thenReturn(Optional.of(testRefreshToken));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userDetailsService.loadUserByUsername("testuser")).thenReturn(userDetails);
        when(jwtUtil.generateToken(userDetails)).thenReturn(newAccessToken);
        when(userRepository.existsById(1L)).thenReturn(true);
        when(refreshTokenRepository.hasUserExceededTokenCreationRate(any(), anyInt(), any())).thenReturn(false);
        when(refreshTokenRepository.countActiveByUserId(1L)).thenReturn(1L);
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenReturn(newRefreshTokenEntity);

        // Act
        RefreshTokenService.RefreshResponse result = refreshTokenService.refreshAccessToken("test-refresh-token");

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getAccessToken()).isEqualTo(newAccessToken);
        assertThat(result.getUserId()).isEqualTo(1L);
        assertThat(result.getUsername()).isEqualTo("testuser");
    }

    @Test
    void refreshAccessToken_ShouldThrowException_WhenTokenNotFound() {
        // Arrange
        when(refreshTokenRepository.findActiveByToken("invalid-token")).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> refreshTokenService.refreshAccessToken("invalid-token"))
                .isInstanceOf(SecurityException.class)
                .hasMessage("Invalid or expired refresh token");
    }

    @Test
    void refreshAccessToken_ShouldThrowException_WhenUserNotFound() {
        // Arrange
        when(refreshTokenRepository.findActiveByToken("test-refresh-token")).thenReturn(Optional.of(testRefreshToken));
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> refreshTokenService.refreshAccessToken("test-refresh-token"))
                .isInstanceOf(SecurityException.class)
                .hasMessage("User not found");
    }

    @Test
    @DisplayName("Deve revogar refresh token com sucesso")
    void revokeRefreshToken_ShouldRevokeToken_WhenValidToken() {
        // Arrange
        when(refreshTokenRepository.revokeByToken("test-refresh-token")).thenReturn(1);

        // Act
        boolean result = refreshTokenService.revokeRefreshToken("test-refresh-token");

        // Assert
        assertThat(result).isTrue();
        verify(refreshTokenRepository).revokeByToken("test-refresh-token");
    }

    @Test
    void revokeRefreshToken_ShouldReturnFalse_WhenTokenNotFound() {
        // Arrange
        when(refreshTokenRepository.revokeByToken("invalid-token")).thenReturn(0);

        // Act
        boolean result = refreshTokenService.revokeRefreshToken("invalid-token");

        // Assert
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("Deve deletar todos os refresh tokens do usuário")
    void deleteAllUserRefreshTokens_ShouldDeleteTokens_WhenValidUser() {
        // Arrange
        when(userRepository.existsById(1L)).thenReturn(true);
        when(refreshTokenRepository.revokeAllByUserId(1L)).thenReturn(3);

        // Act
        int result = refreshTokenService.revokeAllUserTokens(1L);

        // Assert
        assertThat(result).isEqualTo(3);
        verify(refreshTokenRepository).revokeAllByUserId(1L);
    }

    @Test
    void createRefreshToken_ShouldThrowException_WhenNullUserId() {
        // Act & Assert
        assertThatThrownBy(() -> refreshTokenService.createRefreshToken(null, "Device", "IP"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("User ID cannot be null");
    }

    @Test
    void refreshAccessToken_ShouldThrowException_WhenNullToken() {
        // Act & Assert
        assertThatThrownBy(() -> refreshTokenService.refreshAccessToken(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Refresh token cannot be null or empty");
    }

    @Test
    void refreshAccessToken_ShouldThrowException_WhenEmptyToken() {
        // Act & Assert
        assertThatThrownBy(() -> refreshTokenService.refreshAccessToken(""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Refresh token cannot be null or empty");
    }
}

