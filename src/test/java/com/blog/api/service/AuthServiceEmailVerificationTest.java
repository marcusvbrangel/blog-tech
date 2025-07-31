package com.blog.api.service;

import com.blog.api.dto.CreateUserDTO;
import com.blog.api.dto.LoginRequest;
import com.blog.api.dto.UserDTO;
import com.blog.api.entity.User;
import com.blog.api.exception.BadRequestException;
import com.blog.api.exception.ResourceNotFoundException;
import com.blog.api.repository.UserRepository;
import com.blog.api.util.JwtUtil;
import io.micrometer.core.instrument.Counter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceEmailVerificationTest {

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

    @InjectMocks
    private AuthService authService;

    private User testUser;

    @BeforeEach
    void setUp() {
        // Set up test configuration values
        ReflectionTestUtils.setField(authService, "emailVerificationEnabled", true);

        // Create test user
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("hashedpassword");
        testUser.setEmailVerified(false);
        testUser.setFailedLoginAttempts(0);
        testUser.setAccountLocked(false);
        testUser.setRole(User.Role.USER);
    }

    @Test
    void register_WithEmailVerificationEnabled_SendsVerificationEmail() {
        // Given
        CreateUserDTO createUserDTO = new CreateUserDTO("testuser", "test@example.com", "password", User.Role.USER);
        
        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password")).thenReturn("hashedpassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        UserDTO result = authService.register(createUserDTO);

        // Then
        assertNotNull(result);
        assertEquals("testuser", result.username());
        assertEquals("test@example.com", result.email());
        assertFalse(result.emailVerified());
        
        verify(userRegistrationCounter).increment();
        verify(verificationTokenService).generateAndSendEmailVerification(any(User.class));
        verify(userRepository).save(argThat(user -> 
            !user.isEmailVerified() && user.getPasswordChangedAt() != null));
    }

    @Test
    void register_WithEmailVerificationDisabled_SetsEmailAsVerified() {
        // Given
        ReflectionTestUtils.setField(authService, "emailVerificationEnabled", false);
        CreateUserDTO createUserDTO = new CreateUserDTO("testuser", "test@example.com", "password", User.Role.USER);
        
        testUser.setEmailVerified(true);
        testUser.setEmailVerifiedAt(LocalDateTime.now());
        
        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password")).thenReturn("hashedpassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        UserDTO result = authService.register(createUserDTO);

        // Then
        assertNotNull(result);
        assertTrue(result.emailVerified());
        
        verify(verificationTokenService, never()).generateAndSendEmailVerification(any());
        verify(userRepository).save(argThat(user -> 
            user.isEmailVerified() && user.getEmailVerifiedAt() != null));
    }

    @Test
    void login_WithUnverifiedEmail_ThrowsException() {
        // Given
        LoginRequest loginRequest = new LoginRequest("testuser", "password");
        testUser.setEmailVerified(false);
        
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        // When & Then
        BadRequestException exception = assertThrows(BadRequestException.class, () -> 
            authService.login(loginRequest));
        
        assertEquals("Email not verified. Please check your email and verify your account.", exception.getMessage());
        verify(authenticationManager, never()).authenticate(any());
    }

    @Test
    void login_WithLockedAccount_ThrowsException() {
        // Given
        LoginRequest loginRequest = new LoginRequest("testuser", "password");
        testUser.setEmailVerified(true);
        testUser.setAccountLocked(true);
        testUser.setLockedUntil(LocalDateTime.now().plusMinutes(10));
        
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        // When & Then
        BadRequestException exception = assertThrows(BadRequestException.class, () -> 
            authService.login(loginRequest));
        
        assertEquals("Account is temporarily locked. Try again later.", exception.getMessage());
        verify(authenticationManager, never()).authenticate(any());
    }

    @Test
    void login_WithExpiredLock_UnlocksAccountAndProceedsWithLogin() {
        // Given
        LoginRequest loginRequest = new LoginRequest("testuser", "password");
        testUser.setEmailVerified(true);
        testUser.setAccountLocked(true);
        testUser.setLockedUntil(LocalDateTime.now().minusMinutes(10)); // Expired lock
        
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(userRepository.save(testUser)).thenReturn(testUser);
        when(authenticationManager.authenticate(any())).thenReturn(null);
        when(userDetailsService.loadUserByUsername("testuser")).thenReturn(null);
        when(jwtUtil.generateToken(any())).thenReturn("jwt-token");

        // When
        assertDoesNotThrow(() -> authService.login(loginRequest));

        // Then
        verify(userRepository).save(argThat(user -> 
            !user.isAccountLocked() && 
            user.getLockedUntil() == null && 
            user.getFailedLoginAttempts() == 0));
        verify(authenticationManager).authenticate(any());
    }

    @Test
    void verifyEmail_Success() {
        // Given
        String token = "verification-token";
        testUser.setEmailVerified(true);
        testUser.setEmailVerifiedAt(LocalDateTime.now());
        
        when(verificationTokenService.verifyEmailToken(token)).thenReturn(testUser);

        // When
        UserDTO result = authService.verifyEmail(token);

        // Then
        assertNotNull(result);
        assertTrue(result.emailVerified());
        assertNotNull(result.emailVerifiedAt());
    }

    @Test  
    void verifyEmail_WithEmailVerificationDisabled_ThrowsException() {
        // Given
        ReflectionTestUtils.setField(authService, "emailVerificationEnabled", false);
        String token = "verification-token";

        // When & Then
        BadRequestException exception = assertThrows(BadRequestException.class, () -> 
            authService.verifyEmail(token));
        
        assertEquals("Email verification is disabled", exception.getMessage());
        verify(verificationTokenService, never()).verifyEmailToken(any());
    }

    @Test
    void resendEmailVerification_Success() {
        // Given
        String email = "test@example.com";
        testUser.setEmailVerified(false);
        
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(testUser));

        // When
        assertDoesNotThrow(() -> authService.resendEmailVerification(email));

        // Then
        verify(verificationTokenService).generateAndSendEmailVerification(testUser);
    }

    @Test
    void resendEmailVerification_UserNotFound_ThrowsException() {
        // Given
        String email = "nonexistent@example.com";
        
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> 
            authService.resendEmailVerification(email));

        verify(verificationTokenService, never()).generateAndSendEmailVerification(any());
    }

    @Test
    void resendEmailVerification_AlreadyVerified_ThrowsException() {
        // Given
        String email = "test@example.com";
        testUser.setEmailVerified(true);
        
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(testUser));

        // When & Then
        BadRequestException exception = assertThrows(BadRequestException.class, () -> 
            authService.resendEmailVerification(email));
        
        assertEquals("Email is already verified", exception.getMessage());
        verify(verificationTokenService, never()).generateAndSendEmailVerification(any());
    }

    @Test
    void resendEmailVerification_WithEmailVerificationDisabled_ThrowsException() {
        // Given
        ReflectionTestUtils.setField(authService, "emailVerificationEnabled", false);
        String email = "test@example.com";

        // When & Then
        BadRequestException exception = assertThrows(BadRequestException.class, () -> 
            authService.resendEmailVerification(email));
        
        assertEquals("Email verification is disabled", exception.getMessage());
        verify(userRepository, never()).findByEmail(any());
    }

    @Test
    void requestPasswordReset_CallsVerificationTokenService() {
        // Given
        String email = "test@example.com";

        // When
        authService.requestPasswordReset(email);

        // Then
        verify(verificationTokenService).generateAndSendPasswordReset(email);
    }

    @Test
    void validatePasswordResetToken_Success() {
        // Given
        String token = "reset-token";
        
        when(verificationTokenService.verifyPasswordResetToken(token)).thenReturn(testUser);

        // When
        UserDTO result = authService.validatePasswordResetToken(token);

        // Then
        assertNotNull(result);
        assertEquals(testUser.getEmail(), result.email());
    }

    @Test
    void resetPassword_Success() {
        // Given
        String token = "reset-token";
        String newPassword = "newpassword";
        
        when(verificationTokenService.verifyPasswordResetToken(token)).thenReturn(testUser);
        when(passwordEncoder.encode(newPassword)).thenReturn("hashednewpassword");
        when(userRepository.save(testUser)).thenReturn(testUser);

        // When
        UserDTO result = authService.resetPassword(token, newPassword);

        // Then
        assertNotNull(result);
        verify(verificationTokenService).markPasswordResetTokenAsUsed(token);
        verify(userRepository).save(argThat(user -> 
            user.getPasswordChangedAt() != null &&
            user.getFailedLoginAttempts() == 0 &&
            !user.isAccountLocked() &&
            user.getLockedUntil() == null));
    }

    @Test
    void login_FailedAuthentication_IncrementsFailedAttempts() {
        // Given
        LoginRequest loginRequest = new LoginRequest("testuser", "wrongpassword");
        testUser.setEmailVerified(true);
        
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(authenticationManager.authenticate(any()))
            .thenThrow(new RuntimeException("Authentication failed"));

        // When & Then
        assertThrows(BadRequestException.class, () -> authService.login(loginRequest));

        // Verify failed attempts are incremented
        verify(userRepository).save(argThat(user -> user.getFailedLoginAttempts() == 1));
    }

    @Test
    void login_FiveFailedAttempts_LocksAccount() {
        // Given
        LoginRequest loginRequest = new LoginRequest("testuser", "wrongpassword");
        testUser.setEmailVerified(true);
        testUser.setFailedLoginAttempts(4); // This will be the 5th attempt
        
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(authenticationManager.authenticate(any()))
            .thenThrow(new RuntimeException("Authentication failed"));

        // When & Then
        assertThrows(BadRequestException.class, () -> authService.login(loginRequest));

        // Verify account is locked
        verify(userRepository).save(argThat(user -> 
            user.getFailedLoginAttempts() == 5 &&
            user.isAccountLocked() &&
            user.getLockedUntil() != null));
    }
}