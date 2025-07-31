package com.blog.api.service;

import com.blog.api.entity.User;
import com.blog.api.entity.VerificationToken;
import com.blog.api.exception.BadRequestException;
import com.blog.api.exception.ResourceNotFoundException;
import com.blog.api.repository.VerificationTokenRepository;
import com.blog.api.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class VerificationTokenService {

    private static final Logger logger = LoggerFactory.getLogger(VerificationTokenService.class);

    @Autowired
    private VerificationTokenRepository tokenRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailService emailService;

    @Value("${blog.security.email-verification.token-expiration:24h}")
    private Duration emailVerificationExpiration;

    @Value("${blog.security.password-reset.token-expiration:15m}")
    private Duration passwordResetExpiration;

    @Value("${blog.security.email-verification.max-attempts-per-hour:3}")
    private int maxEmailVerificationAttemptsPerHour;

    @Value("${blog.security.password-reset.max-attempts-per-hour:5}")
    private int maxPasswordResetAttemptsPerHour;

    /**
     * Generate and send email verification token
     */
    public void generateAndSendEmailVerification(User user) {
        if (user.isEmailVerified()) {
            throw new BadRequestException("Email already verified");
        }

        // Check rate limiting
        checkRateLimit(user, VerificationToken.TokenType.EMAIL_VERIFICATION, maxEmailVerificationAttemptsPerHour);

        // Invalidate existing tokens
        invalidateExistingTokens(user, VerificationToken.TokenType.EMAIL_VERIFICATION);

        // Generate new token
        String tokenValue = generateSecureToken();
        LocalDateTime expirationTime = LocalDateTime.now().plus(emailVerificationExpiration);

        VerificationToken token = VerificationToken.forEmailVerification(user, tokenValue)
                .expiresAt(expirationTime)
                .build();

        tokenRepository.save(token);

        // Send email asynchronously
        sendEmailVerificationAsync(user, tokenValue);

        logger.info("Email verification token generated for user: {}", user.getEmail());
    }

    /**
     * Generate and send password reset token
     */
    public void generateAndSendPasswordReset(String email) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        
        // For security reasons, we don't reveal if email exists or not
        // If user doesn't exist, just return silently
        if (userOpt.isEmpty()) {
            logger.info("Password reset requested for non-existent email: {}", email);
            return;
        }
        
        User user = userOpt.get();

        // Check rate limiting
        checkRateLimit(user, VerificationToken.TokenType.PASSWORD_RESET, maxPasswordResetAttemptsPerHour);

        // Invalidate existing tokens
        invalidateExistingTokens(user, VerificationToken.TokenType.PASSWORD_RESET);

        // Generate new token
        String tokenValue = generateSecureToken();
        LocalDateTime expirationTime = LocalDateTime.now().plus(passwordResetExpiration);

        VerificationToken token = VerificationToken.forPasswordReset(user, tokenValue)
                .expiresAt(expirationTime)
                .build();

        tokenRepository.save(token);

        // Send email asynchronously
        sendPasswordResetAsync(user, tokenValue);

        logger.info("Password reset token generated for user: {}", user.getEmail());
    }

    /**
     * Verify email verification token
     */
    @CacheEvict(value = "users", key = "#token")
    public User verifyEmailToken(String token) {
        VerificationToken verificationToken = findValidToken(token, VerificationToken.TokenType.EMAIL_VERIFICATION);
        
        User user = verificationToken.getUser();
        
        // Mark token as used
        verificationToken.markAsUsed();
        tokenRepository.save(verificationToken);
        
        // Mark user email as verified using builder
        User updatedUser = User.from(user)
                .emailVerified(true)
                .emailVerifiedAt(LocalDateTime.now())
                .build();
        updatedUser.setId(user.getId());
        userRepository.save(updatedUser);
        user = updatedUser;

        // Send welcome email asynchronously
        sendWelcomeEmailAsync(user);

        // Clean up old tokens for this user
        cleanupUserTokens(user, VerificationToken.TokenType.EMAIL_VERIFICATION);

        logger.info("Email verified successfully for user: {}", user.getEmail());
        return user;
    }

    /**
     * Verify password reset token
     */
    @Cacheable(value = "password_reset_tokens", key = "#token")
    public User verifyPasswordResetToken(String token) {
        VerificationToken verificationToken = findValidToken(token, VerificationToken.TokenType.PASSWORD_RESET);
        return verificationToken.getUser();
    }

    /**
     * Mark password reset token as used
     */
    @CacheEvict(value = "password_reset_tokens", key = "#token")
    public void markPasswordResetTokenAsUsed(String token) {
        VerificationToken verificationToken = findValidToken(token, VerificationToken.TokenType.PASSWORD_RESET);
        verificationToken.markAsUsed();
        tokenRepository.save(verificationToken);

        // Clean up old tokens for this user
        cleanupUserTokens(verificationToken.getUser(), VerificationToken.TokenType.PASSWORD_RESET);

        logger.info("Password reset token marked as used for user: {}", verificationToken.getUser().getEmail());
    }

    /**
     * Check if user can request new verification token (rate limiting)
     */
    public boolean canRequestNewToken(User user, VerificationToken.TokenType tokenType) {
        int maxAttempts = (tokenType == VerificationToken.TokenType.EMAIL_VERIFICATION) 
            ? maxEmailVerificationAttemptsPerHour : maxPasswordResetAttemptsPerHour;
        
        LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);
        long recentTokens = tokenRepository.countTokensCreatedSince(user, tokenType, oneHourAgo);
        
        return recentTokens < maxAttempts;
    }

    /**
     * Get time until user can request new token
     */
    public Optional<LocalDateTime> getNextAllowedRequestTime(User user, VerificationToken.TokenType tokenType) {
        if (canRequestNewToken(user, tokenType)) {
            return Optional.empty();
        }

        // Find the oldest token in the last hour
        LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);
        Optional<VerificationToken> oldestRecentToken = tokenRepository
            .findByUserAndTokenType(user, tokenType)
            .stream()
            .filter(token -> token.getCreatedAt().isAfter(oneHourAgo))
            .min((t1, t2) -> t1.getCreatedAt().compareTo(t2.getCreatedAt()));

        return oldestRecentToken.map(token -> token.getCreatedAt().plusHours(1));
    }

    /**
     * Find valid token or throw exception
     */
    private VerificationToken findValidToken(String token, VerificationToken.TokenType expectedType) {
        VerificationToken verificationToken = tokenRepository.findByTokenAndTokenType(token, expectedType)
            .orElseThrow(() -> new BadRequestException("Invalid or expired verification token"));

        if (!verificationToken.isValid()) {
            if (verificationToken.isUsed()) {
                throw new BadRequestException("Verification token has already been used");
            } else if (verificationToken.isExpired()) {
                throw new BadRequestException("Verification token has expired");
            }
        }

        return verificationToken;
    }

    /**
     * Generate secure random token
     */
    private String generateSecureToken() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * Check rate limiting for token generation
     */
    private void checkRateLimit(User user, VerificationToken.TokenType tokenType, int maxAttempts) {
        if (!canRequestNewToken(user, tokenType)) {
            Optional<LocalDateTime> nextAllowed = getNextAllowedRequestTime(user, tokenType);
            String message = nextAllowed.map(time -> 
                String.format("Too many requests. Try again after %s", time))
                .orElse("Too many requests. Please try again later.");
            throw new BadRequestException(message);
        }
    }

    /**
     * Invalidate existing tokens of the same type for user
     */
    private void invalidateExistingTokens(User user, VerificationToken.TokenType tokenType) {
        List<VerificationToken> existingTokens = tokenRepository
            .findValidTokensByUserAndType(user, tokenType, LocalDateTime.now());
        
        existingTokens.forEach(token -> {
            token.markAsUsed();
            tokenRepository.save(token);
        });

        if (!existingTokens.isEmpty()) {
            logger.info("Invalidated {} existing tokens of type {} for user: {}", 
                existingTokens.size(), tokenType, user.getEmail());
        }
    }

    /**
     * Clean up old tokens for user
     */
    @Async
    private void cleanupUserTokens(User user, VerificationToken.TokenType tokenType) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(7);
        List<VerificationToken> oldTokens = tokenRepository.findByUserAndTokenType(user, tokenType)
            .stream()
            .filter(token -> token.getCreatedAt().isBefore(cutoffDate))
            .toList();

        if (!oldTokens.isEmpty()) {
            tokenRepository.deleteAll(oldTokens);
            logger.info("Cleaned up {} old tokens for user: {}", oldTokens.size(), user.getEmail());
        }
    }

    /**
     * Send email verification asynchronously
     */
    @Async
    private void sendEmailVerificationAsync(User user, String token) {
        try {
            emailService.sendEmailVerification(user, token);
        } catch (Exception e) {
            logger.error("Failed to send email verification to: {}", user.getEmail(), e);
        }
    }

    /**
     * Send password reset asynchronously
     */
    @Async
    private void sendPasswordResetAsync(User user, String token) {
        try {
            emailService.sendPasswordReset(user, token);
        } catch (Exception e) {
            logger.error("Failed to send password reset to: {}", user.getEmail(), e);
        }
    }

    /**
     * Send welcome email asynchronously
     */
    @Async
    private void sendWelcomeEmailAsync(User user) {
        try {
            emailService.sendWelcomeEmail(user);
        } catch (Exception e) {
            logger.error("Failed to send welcome email to: {}", user.getEmail(), e);
        }
    }

    /**
     * Scheduled cleanup of expired tokens (runs every hour)
     */
    @Scheduled(fixedRate = 3600000) // 1 hour in milliseconds
    public void cleanupExpiredTokens() {
        try {
            int deletedCount = tokenRepository.deleteExpiredTokens(LocalDateTime.now());
            if (deletedCount > 0) {
                logger.info("Cleaned up {} expired verification tokens", deletedCount);
            }
        } catch (Exception e) {
            logger.error("Error during scheduled token cleanup", e);
        }
    }

    /**
     * Scheduled cleanup of old used tokens (runs daily)
     */
    @Scheduled(fixedRate = 86400000) // 24 hours in milliseconds
    public void cleanupOldUsedTokens() {
        try {
            LocalDateTime cutoffDate = LocalDateTime.now().minusDays(30);
            int deletedCount = tokenRepository.deleteUsedTokensOlderThan(cutoffDate);
            if (deletedCount > 0) {
                logger.info("Cleaned up {} old used verification tokens", deletedCount);
            }
        } catch (Exception e) {
            logger.error("Error during scheduled old token cleanup", e);
        }
    }

    /**
     * Get statistics for monitoring
     */
    public TokenStatistics getTokenStatistics() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime last24Hours = now.minusHours(24);
        LocalDateTime lastHour = now.minusHours(1);

        long totalTokens = tokenRepository.count();
        long activeTokens = tokenRepository.findValidTokensByUserAndType(null, null, now).size();
        
        // This would need custom queries to get accurate counts, simplified for now
        return new TokenStatistics(totalTokens, activeTokens, 0L, 0L);
    }

    /**
     * Statistics record for monitoring
     */
    public record TokenStatistics(
        long totalTokens,
        long activeTokens,
        long tokensLast24Hours,
        long tokensLastHour
    ) {}
}