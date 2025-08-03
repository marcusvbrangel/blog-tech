package com.blog.api.controller;

import com.blog.api.dto.*;
import com.blog.api.entity.RevokedToken;
import com.blog.api.entity.User;
import com.blog.api.repository.UserRepository;
import com.blog.api.service.AuthService;
import com.blog.api.service.JwtBlacklistService;
import com.blog.api.util.JwtUtil;
import io.jsonwebtoken.ExpiredJwtException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication", description = "Authentication operations")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final AuthService authService;

    private final JwtBlacklistService jwtBlacklistService;

    private final JwtUtil jwtUtil;

    private final UserRepository userRepository;

    public AuthController(AuthService authService, JwtBlacklistService jwtBlacklistService,
                          JwtUtil jwtUtil, UserRepository userRepository) {
        this.authService = authService;
        this.jwtBlacklistService = jwtBlacklistService;
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
    }

    @PostMapping("/register")
    @Operation(summary = "Register a new user")
    public ResponseEntity<UserDTO> register(@Valid @RequestBody CreateUserDTO createUserDTO) {
        UserDTO user = authService.register(createUserDTO);
        return new ResponseEntity<>(user, HttpStatus.CREATED);
    }

    @PostMapping("/login")
    @Operation(summary = "Login user")
    public ResponseEntity<JwtResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        JwtResponse jwtResponse = authService.login(loginRequest);
        return ResponseEntity.ok(jwtResponse);
    }

    // Email Verification Endpoints

    @GetMapping("/verify-email")
    @Operation(summary = "Verify email address with token")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Email verified successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid or expired token"),
        @ApiResponse(responseCode = "404", description = "Token not found")
    })
    public ResponseEntity<VerificationResponse> verifyEmail(
            @Parameter(description = "Email verification token", required = true)
            @RequestParam String token) {
        
        try {
            UserDTO user = authService.verifyEmail(token);
            logger.info("Email verified successfully for user: {}", user.email());
            
            return ResponseEntity.ok(
                VerificationResponse.success(
                    "Email verified successfully! Your account is now active.", 
                    user
                )
            );
        } catch (Exception e) {
            logger.warn("Email verification failed for token: {}", token, e);
            return ResponseEntity.badRequest().body(
                VerificationResponse.error("Email verification failed: " + e.getMessage())
            );
        }
    }

    @PostMapping("/resend-verification")
    @Operation(summary = "Resend email verification")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Verification email sent"),
        @ApiResponse(responseCode = "400", description = "Email already verified or rate limit exceeded"),
        @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<VerificationResponse> resendEmailVerification(
            @Valid @RequestBody EmailVerificationRequest request) {
        
        try {
            authService.resendEmailVerification(request.email());
            logger.info("Verification email resent to: {}", request.email());
            
            return ResponseEntity.ok(
                VerificationResponse.success(
                    "Verification email sent successfully. Please check your inbox."
                )
            );
        } catch (Exception e) {
            logger.warn("Failed to resend verification email to: {}", request.email(), e);
            return ResponseEntity.badRequest().body(
                VerificationResponse.error("Failed to send verification email: " + e.getMessage())
            );
        }
    }

    // Password Reset Endpoints

    @PostMapping("/forgot-password")
    @Operation(summary = "Request password reset")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Password reset email sent (or would be sent if user exists)"),
        @ApiResponse(responseCode = "400", description = "Rate limit exceeded")
    })
    public ResponseEntity<VerificationResponse> forgotPassword(
            @Valid @RequestBody PasswordResetRequest request) {
        
        try {
            authService.requestPasswordReset(request.email());
            logger.info("Password reset requested for email: {}", request.email());
            
            // Always return success to prevent email enumeration
            return ResponseEntity.ok(
                VerificationResponse.success(
                    "If an account with this email exists, you will receive a password reset link shortly."
                )
            );
        } catch (Exception e) {
            logger.warn("Password reset request failed for email: {}", request.email(), e);
            return ResponseEntity.badRequest().body(
                VerificationResponse.error("Failed to process password reset request: " + e.getMessage())
            );
        }
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Reset password with token")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Password reset successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid or expired token"),
        @ApiResponse(responseCode = "404", description = "Token not found")
    })
    public ResponseEntity<VerificationResponse> resetPassword(
            @Valid @RequestBody PasswordResetConfirmRequest request) {
        
        try {
            UserDTO user = authService.resetPassword(request.token(), request.newPassword());
            logger.info("Password reset successfully for user: {}", user.email());
            
            return ResponseEntity.ok(
                VerificationResponse.success(
                    "Password reset successfully! You can now login with your new password.",
                    user
                )
            );
        } catch (Exception e) {
            logger.warn("Password reset failed for token: {}", request.token(), e);
            return ResponseEntity.badRequest().body(
                VerificationResponse.error("Password reset failed: " + e.getMessage())
            );
        }
    }

    @GetMapping("/reset-password")
    @Operation(summary = "Validate password reset token (for frontend)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Token is valid"),
        @ApiResponse(responseCode = "400", description = "Invalid or expired token")
    })
    public ResponseEntity<VerificationResponse> validatePasswordResetToken(
            @Parameter(description = "Password reset token", required = true)
            @RequestParam String token) {
        
        try {
            // This just validates the token without marking it as used
            authService.validatePasswordResetToken(token);
            
            return ResponseEntity.ok(
                VerificationResponse.success("Token is valid. You can proceed with password reset.")
            );
        } catch (Exception e) {
            logger.warn("Invalid password reset token: {}", token, e);
            return ResponseEntity.badRequest().body(
                VerificationResponse.error("Invalid or expired token: " + e.getMessage())
            );
        }
    }

    // JWT Blacklist/Logout Endpoint

    @PostMapping("/logout")
    @Operation(summary = "Logout user and revoke JWT token")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Logout successful"),
        @ApiResponse(responseCode = "401", description = "Invalid or missing token")
    })
    public ResponseEntity<VerificationResponse> logout(HttpServletRequest request) {
        try {
            // Extract token from Authorization header
            String authHeader = request.getHeader("Authorization");
            
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                logger.debug("Logout attempt without valid Authorization header from IP: {}", 
                           request.getRemoteAddr());
                // Still return success for security (don't reveal token requirements)
                return ResponseEntity.ok(
                    VerificationResponse.success("Logout successful")
                );
            }

            String token = authHeader.substring(7);
            
            // Validate token format before processing
            if (!jwtUtil.isValidTokenFormat(token)) {
                logger.debug("Logout attempt with invalid token format from IP: {}", 
                           request.getRemoteAddr());
                // Still return success for security
                return ResponseEntity.ok(
                    VerificationResponse.success("Logout successful")
                );
            }

            try {
                // Extract token information
                String jti = jwtUtil.getJtiFromToken(token);
                String username = jwtUtil.getUsernameFromToken(token);
                
                // Find user to get user ID
                Optional<User> userOpt = userRepository.findByUsername(username);
                if (userOpt.isPresent()) {
                    User user = userOpt.get();
                    
                    // Check rate limiting for logout requests
                    if (jwtBlacklistService.isRateLimitExceeded(user.getId())) {
                        logger.warn("Logout rate limit exceeded for user: {} from IP: {}", 
                                  username, request.getRemoteAddr());
                        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(
                            VerificationResponse.error("Too many logout requests. Please try again later.")
                        );
                    }
                    
                    // Revoke the token
                    jwtBlacklistService.revokeToken(jti, user.getId(), RevokedToken.RevokeReason.LOGOUT);
                    
                    logger.info("User logged out successfully: {} (JTI: {}) from IP: {}", 
                              username, jti, request.getRemoteAddr());
                } else {
                    logger.warn("Logout attempt with token for non-existent user: {} from IP: {}", 
                              username, request.getRemoteAddr());
                }
                
            } catch (ExpiredJwtException e) {
                logger.debug("Logout attempt with expired token from IP: {}", request.getRemoteAddr());
                // Token already expired, no need to revoke
            } catch (Exception e) {
                logger.error("Error processing logout request from IP: {}", request.getRemoteAddr(), e);
                // Continue to return success for security
            }

            // Always return success to prevent information leakage
            return ResponseEntity.ok(
                VerificationResponse.success("Logout successful")
            );
            
        } catch (Exception e) {
            logger.error("Unexpected error during logout from IP: {}", request.getRemoteAddr(), e);
            // Even in case of unexpected errors, return success for security
            return ResponseEntity.ok(
                VerificationResponse.success("Logout successful")
            );
        }
    }
}