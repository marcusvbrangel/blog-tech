package com.blog.api.service;

import com.blog.api.dto.*;
import com.blog.api.entity.User;
import com.blog.api.exception.BadRequestException;
import com.blog.api.exception.ResourceNotFoundException;
import com.blog.api.repository.UserRepository;
import com.blog.api.util.JwtUtil;
import io.micrometer.core.annotation.Timed;
import io.micrometer.core.instrument.Counter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Autowired
    private Counter userRegistrationCounter;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private VerificationTokenService verificationTokenService;

    @Value("${blog.security.email-verification.enabled:true}")
    private boolean emailVerificationEnabled;

    @Timed(value = "blog_api_user_registration", description = "Time taken to register a user")
    public UserDTO register(CreateUserDTO createUserDTO) {
        userRegistrationCounter.increment();
        if (userRepository.existsByUsername(createUserDTO.username())) {
            throw new BadRequestException("Username already exists");
        }

        if (userRepository.existsByEmail(createUserDTO.email())) {
            throw new BadRequestException("Email already exists");
        }

        User user = new User();
        user.setUsername(createUserDTO.username());
        user.setEmail(createUserDTO.email());
        user.setPassword(passwordEncoder.encode(createUserDTO.password()));
        user.setRole(createUserDTO.role());
        user.setPasswordChangedAt(LocalDateTime.now());
        
        // Set email verification status based on configuration
        if (!emailVerificationEnabled) {
            user.setEmailVerified(true);
            user.setEmailVerifiedAt(LocalDateTime.now());
        }

        User savedUser = userRepository.save(user);

        // Send email verification if enabled
        if (emailVerificationEnabled) {
            try {
                verificationTokenService.generateAndSendEmailVerification(savedUser);
            } catch (Exception e) {
                // Log error but don't fail registration
                throw new RuntimeException("User registered but failed to send verification email", e);
            }
        }

        return UserDTO.fromEntity(savedUser);
    }

    @Timed(value = "blog_api_user_login", description = "Time taken to login a user")
    public JwtResponse login(LoginRequest loginRequest) {
        // Find user first to check email verification
        User user = userRepository.findByUsername(loginRequest.username())
                .or(() -> userRepository.findByEmail(loginRequest.username()))
                .orElseThrow(() -> new BadRequestException("Invalid credentials"));

        // Check if email verification is required and user is not verified
        if (emailVerificationEnabled && !user.isEmailVerified()) {
            throw new BadRequestException("Email not verified. Please check your email and verify your account.");
        }

        // Check if account is locked
        if (user.isAccountLocked()) {
            if (user.getLockedUntil() != null && user.getLockedUntil().isAfter(LocalDateTime.now())) {
                throw new BadRequestException("Account is temporarily locked. Try again later.");
            } else {
                // Unlock account if lock period has passed
                user.setAccountLocked(false);
                user.setLockedUntil(null);
                user.setFailedLoginAttempts(0);
                userRepository.save(user);
            }
        }

        try {
            authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    loginRequest.username(),
                    loginRequest.password()
                )
            );

            // Reset failed login attempts on successful login
            if (user.getFailedLoginAttempts() > 0) {
                user.setFailedLoginAttempts(0);
                user.setLastLogin(LocalDateTime.now());
                userRepository.save(user);
            }

            UserDetails userDetails = userDetailsService.loadUserByUsername(loginRequest.username());
            String token = jwtUtil.generateToken(userDetails);

            return new JwtResponse(token, UserDTO.fromEntity(user));

        } catch (Exception e) {
            // Increment failed login attempts
            incrementFailedLoginAttempts(user);
            throw new BadRequestException("Invalid credentials");
        }
    }

    /**
     * Verify email with token
     */
    public UserDTO verifyEmail(String token) {
        if (!emailVerificationEnabled) {
            throw new BadRequestException("Email verification is disabled");
        }

        User user = verificationTokenService.verifyEmailToken(token);
        return UserDTO.fromEntity(user);
    }

    /**
     * Resend email verification
     */
    public void resendEmailVerification(String email) {
        if (!emailVerificationEnabled) {
            throw new BadRequestException("Email verification is disabled");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        if (user.isEmailVerified()) {
            throw new BadRequestException("Email is already verified");
        }

        verificationTokenService.generateAndSendEmailVerification(user);
    }

    /**
     * Request password reset
     */
    public void requestPasswordReset(String email) {
        // Always call the service method to prevent email enumeration
        // The service will handle the case where user doesn't exist
        verificationTokenService.generateAndSendPasswordReset(email);
    }

    /**
     * Validate password reset token
     */
    public UserDTO validatePasswordResetToken(String token) {
        User user = verificationTokenService.verifyPasswordResetToken(token);
        return UserDTO.fromEntity(user);
    }

    /**
     * Reset password with token
     */
    public UserDTO resetPassword(String token, String newPassword) {
        User user = verificationTokenService.verifyPasswordResetToken(token);
        
        // Update password
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setPasswordChangedAt(LocalDateTime.now());
        user.setFailedLoginAttempts(0);
        user.setAccountLocked(false);
        user.setLockedUntil(null);
        
        userRepository.save(user);
        
        // Mark token as used
        verificationTokenService.markPasswordResetTokenAsUsed(token);
        
        return UserDTO.fromEntity(user);
    }

    /**
     * Increment failed login attempts and lock account if necessary
     */
    private void incrementFailedLoginAttempts(User user) {
        int attempts = user.getFailedLoginAttempts() + 1;
        user.setFailedLoginAttempts(attempts);

        // Lock account after 5 failed attempts for 15 minutes
        if (attempts >= 5) {
            user.setAccountLocked(true);
            user.setLockedUntil(LocalDateTime.now().plusMinutes(15));
        }

        userRepository.save(user);
    }
}