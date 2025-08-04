package com.blog.api.service;

import com.blog.api.entity.TwoFactorAuth;
import com.blog.api.entity.User;
import com.blog.api.repository.TwoFactorAuthRepository;
import com.blog.api.repository.UserRepository;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Two Factor Auth Service Tests")
class TwoFactorAuthServiceTest {

    @Mock
    private TwoFactorAuthRepository twoFactorAuthRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuditLogService auditLogService;

    private TwoFactorAuthService twoFactorAuthService;

    private MeterRegistry meterRegistry;
    private User testUser;
    private TwoFactorAuth testTwoFactorAuth;

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        twoFactorAuthService = new TwoFactorAuthService(meterRegistry);
        
        // Inject mocks manually
        ReflectionTestUtils.setField(twoFactorAuthService, "twoFactorAuthRepository", twoFactorAuthRepository);
        ReflectionTestUtils.setField(twoFactorAuthService, "userRepository", userRepository);
        ReflectionTestUtils.setField(twoFactorAuthService, "auditLogService", auditLogService);

        // Create test data
        testUser = User.newInstance()
                .username("testuser")
                .email("test@example.com") 
                .password("TestPass123!")
                .role(User.Role.USER)
                .build();
        testUser.setId(1L);

        testTwoFactorAuth = TwoFactorAuth.builder()
                .userId(1L)
                .secretKey("ABCDEFGHIJKLMNOP")
                .enabled(false)
                .backupCodes("12345678,87654321,11111111,22222222,33333333")
                .build();
    }

    @Test
    void setupTwoFactorAuth_ShouldCreateConfiguration_WhenUserExists() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(twoFactorAuthRepository.findByUserId(1L)).thenReturn(Optional.empty());
        when(twoFactorAuthRepository.save(any(TwoFactorAuth.class))).thenReturn(testTwoFactorAuth);

        // Act
        TwoFactorAuthService.TwoFactorSetupResponse result = twoFactorAuthService.setupTwoFactorAuth(1L);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getSecretKey()).isNotNull();
        assertThat(result.getQrCodeUrl()).contains("otpauth://totp/");
        assertThat(result.getQrCodeUrl()).contains("BlogAPI:testuser");
        assertThat(result.getBackupCodes()).hasSize(10);
        
        verify(twoFactorAuthRepository).save(any(TwoFactorAuth.class));
    }

    @Test
    void setupTwoFactorAuth_ShouldThrowException_WhenUserNotFound() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> twoFactorAuthService.setupTwoFactorAuth(1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("User not found");
    }

    @Test
    void setupTwoFactorAuth_ShouldThrowException_WhenAlreadyEnabled() {
        // Arrange
        TwoFactorAuth enabledAuth = TwoFactorAuth.builder()
                .userId(1L)
                .secretKey("SECRET")
                .enabled(true)
                .build();
        
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(twoFactorAuthRepository.findByUserId(1L)).thenReturn(Optional.of(enabledAuth));

        // Act & Assert
        assertThatThrownBy(() -> twoFactorAuthService.setupTwoFactorAuth(1L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("2FA is already enabled for this user");
    }

    @Test
    void enableTwoFactorAuth_ShouldEnableAuth_WhenValidCode() {
        // Arrange
        when(twoFactorAuthRepository.findByUserId(1L)).thenReturn(Optional.of(testTwoFactorAuth));
        when(twoFactorAuthRepository.save(any(TwoFactorAuth.class))).thenReturn(testTwoFactorAuth);

        // Generate valid TOTP code for current time
        String validCode = generateValidTOTP(testTwoFactorAuth.getSecretKey());

        // Act
        boolean result = twoFactorAuthService.enableTwoFactorAuth(1L, validCode);

        // Assert
        assertThat(result).isTrue();
        verify(twoFactorAuthRepository).save(any(TwoFactorAuth.class));
    }

    @Test
    void enableTwoFactorAuth_ShouldReturnFalse_WhenInvalidCode() {
        // Arrange
        when(twoFactorAuthRepository.findByUserId(1L)).thenReturn(Optional.of(testTwoFactorAuth));

        // Act
        boolean result = twoFactorAuthService.enableTwoFactorAuth(1L, "invalid");

        // Assert
        assertThat(result).isFalse();
        verify(twoFactorAuthRepository, never()).save(any());
    }

    @Test
    void enableTwoFactorAuth_ShouldThrowException_WhenNotSetup() {
        // Arrange
        when(twoFactorAuthRepository.findByUserId(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> twoFactorAuthService.enableTwoFactorAuth(1L, "123456"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("2FA not set up for this user");
    }

    @Test
    void enableTwoFactorAuth_ShouldThrowException_WhenAlreadyEnabled() {
        // Arrange
        TwoFactorAuth enabledAuth = TwoFactorAuth.builder()
                .userId(1L)
                .secretKey("SECRET")
                .enabled(true)
                .build();
        
        when(twoFactorAuthRepository.findByUserId(1L)).thenReturn(Optional.of(enabledAuth));

        // Act & Assert
        assertThatThrownBy(() -> twoFactorAuthService.enableTwoFactorAuth(1L, "123456"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("2FA is already enabled");
    }

    @Test
    void disableTwoFactorAuth_ShouldDisableAuth_WhenValidCode() {
        // Arrange
        TwoFactorAuth enabledAuth = TwoFactorAuth.builder()
                .userId(1L)
                .secretKey("ABCDEFGHIJKLMNOP")
                .enabled(true)
                .backupCodes("12345678,87654321")
                .build();
        
        when(twoFactorAuthRepository.findByUserId(1L)).thenReturn(Optional.of(enabledAuth));
        when(twoFactorAuthRepository.save(any(TwoFactorAuth.class))).thenReturn(enabledAuth);

        String validCode = generateValidTOTP(enabledAuth.getSecretKey());

        // Act
        boolean result = twoFactorAuthService.disableTwoFactorAuth(1L, validCode);

        // Assert
        assertThat(result).isTrue();
        verify(twoFactorAuthRepository).save(any(TwoFactorAuth.class));
    }

    @Test
    void disableTwoFactorAuth_ShouldDisableAuth_WhenValidBackupCode() {
        // Arrange
        TwoFactorAuth enabledAuth = TwoFactorAuth.builder()
                .userId(1L)
                .secretKey("SECRET")
                .enabled(true)
                .backupCodes("12345678,87654321")
                .backupCodesUsed("")
                .build();
        
        when(twoFactorAuthRepository.findByUserId(1L)).thenReturn(Optional.of(enabledAuth));
        when(twoFactorAuthRepository.save(any(TwoFactorAuth.class))).thenReturn(enabledAuth);

        // Act
        boolean result = twoFactorAuthService.disableTwoFactorAuth(1L, "12345678");

        // Assert
        assertThat(result).isTrue();
        verify(twoFactorAuthRepository).save(any(TwoFactorAuth.class));
    }

    @Test
    void verifyTwoFactorCode_ShouldReturnTrue_WhenUserHasNo2FA() {
        // Arrange
        when(twoFactorAuthRepository.findByUserId(1L)).thenReturn(Optional.empty());

        // Act
        boolean result = twoFactorAuthService.verifyTwoFactorCode(1L, "123456");

        // Assert
        assertThat(result).isTrue(); // No 2FA enabled, allow access
    }

    @Test
    void verifyTwoFactorCode_ShouldReturnTrue_WhenUserHas2FADisabled() {
        // Arrange
        when(twoFactorAuthRepository.findByUserId(1L)).thenReturn(Optional.of(testTwoFactorAuth));

        // Act
        boolean result = twoFactorAuthService.verifyTwoFactorCode(1L, "123456");

        // Assert
        assertThat(result).isTrue(); // 2FA disabled, allow access
    }

    @Test
    void verifyTwoFactorCode_ShouldReturnTrue_WhenValidTOTP() {
        // Arrange
        TwoFactorAuth enabledAuth = TwoFactorAuth.builder()
                .userId(1L)
                .secretKey("ABCDEFGHIJKLMNOP")
                .enabled(true)
                .backupCodes("12345678,87654321")
                .build();
        
        when(twoFactorAuthRepository.findByUserId(1L)).thenReturn(Optional.of(enabledAuth));
        when(twoFactorAuthRepository.save(any(TwoFactorAuth.class))).thenReturn(enabledAuth);

        String validCode = generateValidTOTP(enabledAuth.getSecretKey());

        // Act
        boolean result = twoFactorAuthService.verifyTwoFactorCode(1L, validCode);

        // Assert
        assertThat(result).isTrue();
        verify(twoFactorAuthRepository).save(any(TwoFactorAuth.class));
    }

    @Test
    void verifyTwoFactorCode_ShouldReturnTrue_WhenValidBackupCode() {
        // Arrange
        TwoFactorAuth enabledAuth = TwoFactorAuth.builder()
                .userId(1L)
                .secretKey("SECRET")
                .enabled(true)
                .backupCodes("12345678,87654321")
                .backupCodesUsed("")
                .build();
        
        when(twoFactorAuthRepository.findByUserId(1L)).thenReturn(Optional.of(enabledAuth));
        when(twoFactorAuthRepository.save(any(TwoFactorAuth.class))).thenReturn(enabledAuth);

        // Act
        boolean result = twoFactorAuthService.verifyTwoFactorCode(1L, "12345678");

        // Assert
        assertThat(result).isTrue();
        verify(twoFactorAuthRepository).save(any(TwoFactorAuth.class));
    }

    @Test
    void verifyTwoFactorCode_ShouldReturnFalse_WhenInvalidCode() {
        // Arrange
        TwoFactorAuth enabledAuth = TwoFactorAuth.builder()
                .userId(1L)
                .secretKey("SECRET")
                .enabled(true)
                .backupCodes("12345678,87654321")
                .build();
        
        when(twoFactorAuthRepository.findByUserId(1L)).thenReturn(Optional.of(enabledAuth));

        // Act
        boolean result = twoFactorAuthService.verifyTwoFactorCode(1L, "invalid");

        // Assert
        assertThat(result).isFalse();
        verify(twoFactorAuthRepository, never()).save(any());
    }

    @Test
    void verifyTwoFactorCode_ShouldReturnFalse_WhenBackupCodeAlreadyUsed() {
        // Arrange
        TwoFactorAuth enabledAuth = TwoFactorAuth.builder()
                .userId(1L)
                .secretKey("SECRET")
                .enabled(true)
                .backupCodes("12345678,87654321")
                .backupCodesUsed("12345678")
                .build();
        
        when(twoFactorAuthRepository.findByUserId(1L)).thenReturn(Optional.of(enabledAuth));

        // Act
        boolean result = twoFactorAuthService.verifyTwoFactorCode(1L, "12345678");

        // Assert
        assertThat(result).isFalse(); // Code already used
    }

    @Test
    void isTwoFactorEnabled_ShouldReturnTrue_WhenEnabled() {
        // Arrange
        when(twoFactorAuthRepository.isEnabledByUserId(1L)).thenReturn(Optional.of(true));

        // Act
        boolean result = twoFactorAuthService.isTwoFactorEnabled(1L);

        // Assert
        assertThat(result).isTrue();
    }

    @Test
    void isTwoFactorEnabled_ShouldReturnFalse_WhenDisabled() {
        // Arrange
        when(twoFactorAuthRepository.isEnabledByUserId(1L)).thenReturn(Optional.of(false));

        // Act
        boolean result = twoFactorAuthService.isTwoFactorEnabled(1L);

        // Assert
        assertThat(result).isFalse();
    }

    @Test
    void isTwoFactorEnabled_ShouldReturnFalse_WhenNotConfigured() {
        // Arrange
        when(twoFactorAuthRepository.isEnabledByUserId(1L)).thenReturn(Optional.empty());

        // Act
        boolean result = twoFactorAuthService.isTwoFactorEnabled(1L);

        // Assert
        assertThat(result).isFalse();
    }

    @Test
    void getTwoFactorStatus_ShouldReturnStatus_WhenConfigured() {
        // Arrange
        TwoFactorAuth enabledAuth = TwoFactorAuth.builder()
                .userId(1L)
                .secretKey("SECRET")
                .enabled(true)
                .backupCodes("12345678,87654321,11111111")
                .backupCodesUsed("12345678")
                .enabledAt(LocalDateTime.now().minusDays(1))
                .lastUsed(LocalDateTime.now().minusHours(1))
                .build();
        
        when(twoFactorAuthRepository.findByUserId(1L)).thenReturn(Optional.of(enabledAuth));

        // Act
        TwoFactorAuthService.TwoFactorStatus result = twoFactorAuthService.getTwoFactorStatus(1L);

        // Assert
        assertThat(result.isConfigured()).isTrue();
        assertThat(result.isEnabled()).isTrue();
        assertThat(result.getAvailableBackupCodes()).isEqualTo(2); // 3 total - 1 used
        assertThat(result.getUsedBackupCodes()).isEqualTo(1);
        assertThat(result.getEnabledAt()).isNotNull();
        assertThat(result.getLastUsed()).isNotNull();
    }

    @Test
    void getTwoFactorStatus_ShouldReturnUnconfiguredStatus_WhenNotSetup() {
        // Arrange
        when(twoFactorAuthRepository.findByUserId(1L)).thenReturn(Optional.empty());

        // Act
        TwoFactorAuthService.TwoFactorStatus result = twoFactorAuthService.getTwoFactorStatus(1L);

        // Assert
        assertThat(result.isConfigured()).isFalse();
        assertThat(result.isEnabled()).isFalse();
        assertThat(result.getAvailableBackupCodes()).isEqualTo(0);
        assertThat(result.getUsedBackupCodes()).isEqualTo(0);
        assertThat(result.getEnabledAt()).isNull();
        assertThat(result.getLastUsed()).isNull();
    }

    @Test
    void regenerateBackupCodes_ShouldGenerateNewCodes_WhenValidCode() {
        // Arrange
        TwoFactorAuth enabledAuth = TwoFactorAuth.builder()
                .userId(1L)
                .secretKey("ABCDEFGHIJKLMNOP")
                .enabled(true)
                .backupCodes("old1,old2")
                .backupCodesUsed("old1")
                .createdAt(LocalDateTime.now().minusDays(7))
                .enabledAt(LocalDateTime.now().minusDays(1))
                .lastUsed(LocalDateTime.now().minusHours(1))
                .build();
        enabledAuth.setId(1L);
        
        when(twoFactorAuthRepository.findByUserId(1L)).thenReturn(Optional.of(enabledAuth));
        when(twoFactorAuthRepository.save(any(TwoFactorAuth.class))).thenReturn(enabledAuth);

        String validCode = generateValidTOTP(enabledAuth.getSecretKey());

        // Act
        List<String> result = twoFactorAuthService.regenerateBackupCodes(1L, validCode);

        // Assert
        assertThat(result).hasSize(10);
        assertThat(result).doesNotContain("old1", "old2");
        
        ArgumentCaptor<TwoFactorAuth> captor = ArgumentCaptor.forClass(TwoFactorAuth.class);
        verify(twoFactorAuthRepository).save(captor.capture());
        
        TwoFactorAuth saved = captor.getValue();
        assertThat(saved.getBackupCodesUsed()).isEmpty(); // Reset
        assertThat(saved.getBackupCodes()).doesNotContain("old1", "old2");
    }

    @Test
    void regenerateBackupCodes_ShouldThrowException_WhenInvalidCode() {
        // Arrange
        TwoFactorAuth enabledAuth = TwoFactorAuth.builder()
                .userId(1L)
                .secretKey("SECRET")
                .enabled(true)
                .build();
        
        when(twoFactorAuthRepository.findByUserId(1L)).thenReturn(Optional.of(enabledAuth));

        // Act & Assert
        assertThatThrownBy(() -> twoFactorAuthService.regenerateBackupCodes(1L, "invalid"))
                .isInstanceOf(SecurityException.class)
                .hasMessage("Invalid verification code");
    }

    @Test
    void regenerateBackupCodes_ShouldThrowException_WhenNotEnabled() {
        // Arrange
        when(twoFactorAuthRepository.findByUserId(1L)).thenReturn(Optional.of(testTwoFactorAuth));

        // Act & Assert
        assertThatThrownBy(() -> twoFactorAuthService.regenerateBackupCodes(1L, "123456"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("2FA is not enabled");
    }

    // Helper method to generate a valid TOTP code for testing
    private String generateValidTOTP(String secretKey) {
        // For testing purposes, we'll mock the TOTP verification process
        // by using a specific test code that we know will pass
        try {
            // Use reflection to call the actual TOTP generation method with current time
            java.lang.reflect.Method method = TwoFactorAuthService.class.getDeclaredMethod("generateTOTP", String.class, long.class);
            method.setAccessible(true);
            long currentTimeCounter = java.time.Instant.now().getEpochSecond() / 30;
            return (String) method.invoke(twoFactorAuthService, secretKey, currentTimeCounter);
        } catch (Exception e) {
            // If reflection fails, return a test code that should be mocked
            return "123456";
        }
    }
}

