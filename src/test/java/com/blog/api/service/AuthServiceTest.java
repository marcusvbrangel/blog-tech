package com.blog.api.service;

import com.blog.api.dto.CreateUserDTO;
import com.blog.api.dto.JwtResponse;
import com.blog.api.dto.LoginRequest;
import com.blog.api.dto.UserDTO;
import com.blog.api.entity.User;
import com.blog.api.exception.BadRequestException;
import com.blog.api.exception.ResourceNotFoundException;
import com.blog.api.repository.UserRepository;
import com.blog.api.util.JwtUtil;
import io.micrometer.core.instrument.Counter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Auth Service Tests")
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private CustomUserDetailsService userDetailsService;

    @Mock
    private Counter userRegistrationCounter;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private VerificationTokenService verificationTokenService;

    @Mock
    private RefreshTokenService refreshTokenService;

    @Mock
    private AuditLogService auditLogService;

    @Mock
    private UserDetails userDetails;

    @InjectMocks
    private AuthService authService;

    private User testUser;
    private CreateUserDTO createUserDTO;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        testUser = User.ofEncrypted("testuser", "test@example.com", "encodedPassword")
                .role(User.Role.USER)
                .emailVerified(true)
                .failedLoginAttempts(0)
                .accountLocked(false)
                .build();
        testUser.setId(1L);
        testUser.setCreatedAt(LocalDateTime.now());

        createUserDTO = new CreateUserDTO("testuser", "test@example.com", "ValidPassword123!", User.Role.USER);
        loginRequest = new LoginRequest("testuser", "ValidPassword123!");

        // Set default email verification to disabled for most tests
        ReflectionTestUtils.setField(authService, "emailVerificationEnabled", false);
    }

    @Test
    @DisplayName("Deve registrar usuário com sucesso quando dados são válidos")
    void register_ShouldRegisterUser_WhenValidData() {
        // Arrange
        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(passwordEncoder.encode("ValidPassword123!")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        UserDTO result = authService.register(createUserDTO);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.username()).isEqualTo("testuser");
        assertThat(result.email()).isEqualTo("test@example.com");
        verify(userRegistrationCounter).increment();
        verify(userRepository).existsByUsername("testuser");
        verify(userRepository).existsByEmail("test@example.com");
        verify(passwordEncoder).encode("ValidPassword123!");
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Deve lançar BadRequestException quando nome de usuário já existe")
    void register_ShouldThrowBadRequestException_WhenUsernameExists() {
        // Arrange
        when(userRepository.existsByUsername("testuser")).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> authService.register(createUserDTO))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Username already exists");
        
        verify(userRepository).existsByUsername("testuser");
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar BadRequestException quando email já existe")
    void register_ShouldThrowBadRequestException_WhenEmailExists() {
        // Arrange
        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> authService.register(createUserDTO))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Email already exists");
        
        verify(userRepository).existsByUsername("testuser");
        verify(userRepository).existsByEmail("test@example.com");
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve fazer login com sucesso quando credenciais são válidas")
    void login_ShouldLoginSuccessfully_WhenValidCredentials() {
        // Arrange
        com.blog.api.entity.RefreshToken refreshToken = new com.blog.api.entity.RefreshToken();
        refreshToken.setToken("refresh-token-123");
        
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(null);
        when(userDetailsService.loadUserByUsername("testuser")).thenReturn(userDetails);
        when(jwtUtil.generateToken(userDetails)).thenReturn("jwt-token");
        when(refreshTokenService.createRefreshToken(eq(testUser.getId()), any(), any())).thenReturn(refreshToken);

        // Act
        JwtResponse result = authService.login(loginRequest, "device-info", "192.168.1.1", null);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.token()).isEqualTo("jwt-token");
        assertThat(result.user().username()).isEqualTo("testuser");
        assertThat(result.refreshToken()).isEqualTo("refresh-token-123");
        verify(userRepository).findByUsername("testuser");
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtUtil).generateToken(userDetails);
        verify(refreshTokenService).createRefreshToken(eq(testUser.getId()), any(), any());
    }

    @Test
    @DisplayName("Deve lançar BadRequestException quando credenciais são inválidas")
    void login_ShouldThrowBadRequestException_WhenInvalidCredentials() {
        // Arrange
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("nonexistent")).thenReturn(Optional.empty());
        LoginRequest invalidRequest = new LoginRequest("nonexistent", "TestPass123!");

        // Act & Assert
        assertThatThrownBy(() -> authService.login(invalidRequest, null, null, null))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Invalid credentials");
        
        verify(userRepository).findByUsername("nonexistent");
        verify(userRepository).findByEmail("nonexistent");
    }

    @Test
    @DisplayName("Deve validar política de senha durante registro")
    void register_ShouldValidatePasswordPolicy() {
        // Act & Assert - A validação acontece no DTO, não no service
        assertThatThrownBy(() -> new CreateUserDTO("testuser", "test@example.com", "weak", User.Role.USER))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Password policy violation");
    }

    @Test
    @DisplayName("Deve incrementar tentativas de login falhadas")
    void login_ShouldIncrementFailedAttempts_WhenAuthenticationFails() {
        // Arrange
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new RuntimeException("Authentication failed"));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act & Assert
        assertThatThrownBy(() -> authService.login(loginRequest, null, null, null))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Invalid credentials");

        verify(userRepository).save(any(User.class)); // Failed attempts incremented
    }

    @Test
    @DisplayName("Deve bloquear conta após múltiplas tentativas falhadas")
    void login_ShouldLockAccount_AfterMultipleFailedAttempts() {
        // Arrange
        User lockedUser = User.ofEncrypted("testuser", "test@example.com", "encodedPassword")
                .role(User.Role.USER)
                .emailVerified(true)
                .accountLocked(true)
                .lockedUntil(LocalDateTime.now().plusHours(1))
                .build();
        lockedUser.setId(1L);
        
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(lockedUser));

        // Act & Assert
        assertThatThrownBy(() -> authService.login(loginRequest, null, null, null))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Account is temporarily locked");
        
        verify(userRepository).findByUsername("testuser");
    }

    @Test
    @DisplayName("Deve resetar tentativas falhadas após login bem-sucedido")
    void login_ShouldResetFailedAttempts_AfterSuccessfulLogin() {
        // Arrange
        User userWithFailedAttempts = User.ofEncrypted("testuser", "test@example.com", "encodedPassword")
                .role(User.Role.USER)
                .emailVerified(true)
                .failedLoginAttempts(5)
                .accountLocked(false)
                .build();
        userWithFailedAttempts.setId(1L);

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(userWithFailedAttempts));
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(null);
        when(userDetailsService.loadUserByUsername("testuser")).thenReturn(userDetails);
        when(jwtUtil.generateToken(userDetails)).thenReturn("jwt-token");
        com.blog.api.entity.RefreshToken refreshToken = new com.blog.api.entity.RefreshToken();
        refreshToken.setToken("refresh-token-123");
        when(refreshTokenService.createRefreshToken(any(), any(), any())).thenReturn(refreshToken);

        // Act
        JwtResponse result = authService.login(loginRequest, null, null, null);

        // Assert
        assertThat(result).isNotNull();
        verify(userRepository).save(any(User.class)); // Failed attempts reset
    }

    @Test
    @DisplayName("Deve registrar eventos de auditoria durante autenticação")
    void login_ShouldLogAuditEvents_DuringAuthentication() {
        // Arrange
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(null);
        when(userDetailsService.loadUserByUsername("testuser")).thenReturn(userDetails);
        when(jwtUtil.generateToken(userDetails)).thenReturn("jwt-token");
        com.blog.api.entity.RefreshToken refreshToken = new com.blog.api.entity.RefreshToken();
        refreshToken.setToken("refresh-token-123");
        when(refreshTokenService.createRefreshToken(any(), any(), any())).thenReturn(refreshToken);

        // Act
        authService.login(loginRequest, null, null, null);

        // Assert
        // Verifica se os métodos de auditoria foram chamados
        verify(auditLogService).logSuccess(any(), any(), any(), eq(null), any(), any(), any());
    }

    @Test
    void verifyEmail_ShouldReturnUserDTO_WhenValidToken() {
        // Arrange
        ReflectionTestUtils.setField(authService, "emailVerificationEnabled", true);
        String token = "valid-token";
        when(verificationTokenService.verifyEmailToken(token)).thenReturn(testUser);

        // Act
        UserDTO result = authService.verifyEmail(token);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.username()).isEqualTo("testuser");
        verify(verificationTokenService).verifyEmailToken(token);
    }

    @Test
    void verifyEmail_ShouldThrowBadRequestException_WhenEmailVerificationDisabled() {
        // Arrange
        ReflectionTestUtils.setField(authService, "emailVerificationEnabled", false);
        String token = "valid-token";

        // Act & Assert
        assertThatThrownBy(() -> authService.verifyEmail(token))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Email verification is disabled");
        
        verify(verificationTokenService, never()).verifyEmailToken(any());
    }

    @Test
    void resendEmailVerification_ShouldCallVerificationService_WhenValidEmail() {
        // Arrange
        ReflectionTestUtils.setField(authService, "emailVerificationEnabled", true);
        String email = "test@example.com";
        User unverifiedUser = User.of("testuser", "test@example.com", "ValidPassword123!")
                .emailVerified(false)
                .build();
        unverifiedUser.setId(1L);
        
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(unverifiedUser));
        doNothing().when(verificationTokenService).generateAndSendEmailVerification(unverifiedUser);

        // Act
        authService.resendEmailVerification(email);

        // Assert
        verify(userRepository).findByEmail(email);
        verify(verificationTokenService).generateAndSendEmailVerification(unverifiedUser);
    }

    @Test
    void resendEmailVerification_ShouldThrowResourceNotFoundException_WhenUserNotFound() {
        // Arrange
        ReflectionTestUtils.setField(authService, "emailVerificationEnabled", true);
        String email = "nonexistent@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> authService.resendEmailVerification(email))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User not found with email");
        
        verify(userRepository).findByEmail(email);
        verify(verificationTokenService, never()).generateAndSendEmailVerification(any());
    }

    @Test
    void resendEmailVerification_ShouldThrowBadRequestException_WhenEmailAlreadyVerified() {
        // Arrange
        ReflectionTestUtils.setField(authService, "emailVerificationEnabled", true);
        String email = "test@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(testUser)); // testUser is verified

        // Act & Assert
        assertThatThrownBy(() -> authService.resendEmailVerification(email))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Email is already verified");
        
        verify(userRepository).findByEmail(email);
        verify(verificationTokenService, never()).generateAndSendEmailVerification(any());
    }

    @Test
    void requestPasswordReset_ShouldCallVerificationService() {
        // Arrange
        String email = "test@example.com";
        doNothing().when(verificationTokenService).generateAndSendPasswordReset(email);

        // Act
        authService.requestPasswordReset(email);

        // Assert
        verify(verificationTokenService).generateAndSendPasswordReset(email);
    }

    @Test
    void validatePasswordResetToken_ShouldReturnUserDTO_WhenValidToken() {
        // Arrange
        String token = "valid-token";
        when(verificationTokenService.verifyPasswordResetToken(token)).thenReturn(testUser);

        // Act
        UserDTO result = authService.validatePasswordResetToken(token);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.username()).isEqualTo("testuser");
        verify(verificationTokenService).verifyPasswordResetToken(token);
    }

    @Test
    void resetPassword_ShouldResetPasswordAndReturnUserDTO_WhenValidToken() {
        // Arrange
        String token = "valid-token";
        String newPassword = "NewValidPassword123!";
        when(verificationTokenService.verifyPasswordResetToken(token)).thenReturn(testUser);
        when(passwordEncoder.encode(newPassword)).thenReturn("newEncodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        doNothing().when(verificationTokenService).markPasswordResetTokenAsUsed(token);

        // Act
        UserDTO result = authService.resetPassword(token, newPassword);

        // Assert
        assertThat(result).isNotNull();
        verify(verificationTokenService).verifyPasswordResetToken(token);
        verify(passwordEncoder).encode(newPassword);
        verify(userRepository).save(any(User.class));
        verify(verificationTokenService).markPasswordResetTokenAsUsed(token);
    }

    @Test
    void resetPassword_ShouldThrowBadRequestException_WhenPasswordPolicyViolated() {
        // Arrange
        String token = "valid-token";
        String weakPassword = "weak";
        when(verificationTokenService.verifyPasswordResetToken(token)).thenReturn(testUser);

        // Act & Assert
        assertThatThrownBy(() -> authService.resetPassword(token, weakPassword))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Password policy violation");
        
        verify(verificationTokenService).verifyPasswordResetToken(token);
        verify(userRepository, never()).save(any());
        verify(verificationTokenService, never()).markPasswordResetTokenAsUsed(any());
    }
}

