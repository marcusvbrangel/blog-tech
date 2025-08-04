package com.blog.api.service;

import com.blog.api.entity.TwoFactorAuth;
import com.blog.api.entity.User;
import com.blog.api.repository.TwoFactorAuthRepository;
import com.blog.api.repository.UserRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
public class TwoFactorAuthService {
    
    private static final Logger logger = LoggerFactory.getLogger(TwoFactorAuthService.class);
    
    private static final String ALGORITHM = "HmacSHA1";
    private static final int TOTP_DIGITS = 6;
    private static final int TIME_STEP = 30; // 30 seconds
    private static final int WINDOW = 1; // Allow 1 time step tolerance
    private static final int BACKUP_CODES_COUNT = 10;
    private static final int BACKUP_CODE_LENGTH = 8;
    
    @Autowired
    private TwoFactorAuthRepository twoFactorAuthRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private AuditLogService auditLogService;
    
    private final Counter twoFactorSuccessCounter;
    private final Counter twoFactorFailureCounter;
    private final Counter backupCodeUsageCounter;
    
    public TwoFactorAuthService(MeterRegistry meterRegistry) {
        this.twoFactorSuccessCounter = Counter.builder("blog_api_2fa_success_total")
            .description("Total successful 2FA authentications")
            .register(meterRegistry);
            
        this.twoFactorFailureCounter = Counter.builder("blog_api_2fa_failure_total")
            .description("Total failed 2FA authentications")
            .register(meterRegistry);
            
        this.backupCodeUsageCounter = Counter.builder("blog_api_2fa_backup_code_usage_total")
            .description("Total backup code usage")
            .register(meterRegistry);
    }
    
    /**
     * Setup 2FA for a user - generates secret and backup codes
     */
    @Transactional
    public TwoFactorSetupResponse setupTwoFactorAuth(Long userId) {
        // Check if user exists
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        // Check if 2FA already exists
        Optional<TwoFactorAuth> existing = twoFactorAuthRepository.findByUserId(userId);
        if (existing.isPresent() && existing.get().isEnabled()) {
            throw new IllegalStateException("2FA is already enabled for this user");
        }
        
        // Generate secret key
        String secretKey = generateSecretKey();
        
        // Generate backup codes
        List<String> backupCodes = generateBackupCodes();
        String backupCodesString = String.join(",", backupCodes);
        
        // Create or update 2FA configuration
        TwoFactorAuth twoFactorAuth;
        if (existing.isPresent()) {
            // Update existing configuration
            TwoFactorAuth existingAuth = existing.get();
            twoFactorAuth = TwoFactorAuth.builder()
                .userId(userId)
                .secretKey(secretKey)
                .enabled(false) // Not enabled until verified
                .backupCodes(backupCodesString)
                .backupCodesUsed("")
                .createdAt(existingAuth.getCreatedAt())
                .build();
            twoFactorAuth.setId(existingAuth.getId());
        } else {
            // Create new configuration
            twoFactorAuth = TwoFactorAuth.builder()
                .userId(userId)
                .secretKey(secretKey)
                .enabled(false) // Not enabled until verified
                .backupCodes(backupCodesString)
                .backupCodesUsed("")
                .build();
        }
        
        twoFactorAuthRepository.save(twoFactorAuth);
        
        // Generate QR code URL for authenticator apps
        String qrCodeUrl = generateQRCodeUrl(user.getUsername(), secretKey);
        
        logger.info("2FA setup initiated for user: {}", userId);
        
        return new TwoFactorSetupResponse(secretKey, qrCodeUrl, backupCodes);
    }
    
    /**
     * Enable 2FA after user verifies they can generate correct codes
     */
    @Transactional
    public boolean enableTwoFactorAuth(Long userId, String verificationCode) {
        TwoFactorAuth twoFactorAuth = twoFactorAuthRepository.findByUserId(userId)
            .orElseThrow(() -> new IllegalStateException("2FA not set up for this user"));
        
        if (twoFactorAuth.isEnabled()) {
            throw new IllegalStateException("2FA is already enabled");
        }
        
        // Verify the code
        if (!verifyTOTP(twoFactorAuth.getSecretKey(), verificationCode)) {
            logger.warn("Failed 2FA enable attempt for user: {} - invalid code", userId);
            return false;
        }
        
        // Enable 2FA
        twoFactorAuth.enable();
        twoFactorAuthRepository.save(twoFactorAuth);
        
        logger.info("2FA enabled for user: {}", userId);
        return true;
    }
    
    /**
     * Disable 2FA for a user
     */
    @Transactional
    public boolean disableTwoFactorAuth(Long userId, String verificationCode) {
        TwoFactorAuth twoFactorAuth = twoFactorAuthRepository.findByUserId(userId)
            .orElseThrow(() -> new IllegalStateException("2FA not found for this user"));
        
        if (!twoFactorAuth.isEnabled()) {
            throw new IllegalStateException("2FA is not enabled");
        }
        
        // Verify current code or backup code
        boolean isValid = verifyTOTP(twoFactorAuth.getSecretKey(), verificationCode) ||
                         verifyBackupCode(twoFactorAuth, verificationCode);
        
        if (!isValid) {
            logger.warn("Failed 2FA disable attempt for user: {} - invalid code", userId);
            return false;
        }
        
        // Disable 2FA
        twoFactorAuth.disable();
        twoFactorAuthRepository.save(twoFactorAuth);
        
        logger.info("2FA disabled for user: {}", userId);
        return true;
    }
    
    /**
     * Verify 2FA code during login
     */
    public boolean verifyTwoFactorCode(Long userId, String code) {
        TwoFactorAuth twoFactorAuth = twoFactorAuthRepository.findByUserId(userId)
            .orElse(null);
        
        if (twoFactorAuth == null || !twoFactorAuth.isEnabled()) {
            return true; // 2FA not enabled, allow access
        }
        
        boolean isValid = false;
        boolean isBackupCode = false;
        
        // Try TOTP first
        if (verifyTOTP(twoFactorAuth.getSecretKey(), code)) {
            isValid = true;
            twoFactorAuth.markAsUsed();
        } else if (verifyBackupCode(twoFactorAuth, code)) {
            // Try backup code
            isValid = true;
            isBackupCode = true;
            twoFactorAuth.markBackupCodeAsUsed(code);
            backupCodeUsageCounter.increment();
        }
        
        if (isValid) {
            twoFactorAuthRepository.save(twoFactorAuth);
            twoFactorSuccessCounter.increment();
            
            if (isBackupCode) {
                logger.info("2FA verified with backup code for user: {} (remaining: {})", 
                           userId, twoFactorAuth.getAvailableBackupCodesCount());
            } else {
                logger.debug("2FA verified with TOTP for user: {}", userId);
            }
        } else {
            twoFactorFailureCounter.increment();
            logger.warn("Failed 2FA verification for user: {}", userId);
        }
        
        return isValid;
    }
    
    /**
     * Check if user has 2FA enabled
     */
    public boolean isTwoFactorEnabled(Long userId) {
        return twoFactorAuthRepository.isEnabledByUserId(userId).orElse(false);
    }
    
    /**
     * Get 2FA status for user
     */
    public TwoFactorStatus getTwoFactorStatus(Long userId) {
        Optional<TwoFactorAuth> twoFactorAuth = twoFactorAuthRepository.findByUserId(userId);
        
        if (twoFactorAuth.isEmpty()) {
            return new TwoFactorStatus(false, false, 0, 0, null, null);
        }
        
        TwoFactorAuth auth = twoFactorAuth.get();
        return new TwoFactorStatus(
            true,
            auth.isEnabled(),
            auth.getAvailableBackupCodesCount(),
            auth.getUsedBackupCodesCount(),
            auth.getEnabledAt(),
            auth.getLastUsed()
        );
    }
    
    /**
     * Generate new backup codes
     */
    @Transactional
    public List<String> regenerateBackupCodes(Long userId, String verificationCode) {
        TwoFactorAuth twoFactorAuth = twoFactorAuthRepository.findByUserId(userId)
            .orElseThrow(() -> new IllegalStateException("2FA not found for this user"));
        
        if (!twoFactorAuth.isEnabled()) {
            throw new IllegalStateException("2FA is not enabled");
        }
        
        // Verify current code
        if (!verifyTOTP(twoFactorAuth.getSecretKey(), verificationCode)) {
            throw new SecurityException("Invalid verification code");
        }
        
        // Generate new backup codes
        List<String> newBackupCodes = generateBackupCodes();
        String backupCodesString = String.join(",", newBackupCodes);
        
        // Update configuration
        TwoFactorAuth updatedAuth = TwoFactorAuth.builder()
            .userId(userId)
            .secretKey(twoFactorAuth.getSecretKey())
            .enabled(true)
            .backupCodes(backupCodesString)
            .backupCodesUsed("") // Reset used codes
            .createdAt(twoFactorAuth.getCreatedAt())
            .enabledAt(twoFactorAuth.getEnabledAt())
            .lastUsed(twoFactorAuth.getLastUsed())
            .build();
        updatedAuth.setId(twoFactorAuth.getId());
        
        twoFactorAuthRepository.save(updatedAuth);
        
        logger.info("Backup codes regenerated for user: {}", userId);
        return newBackupCodes;
    }
    
    // Private helper methods
    
    private String generateSecretKey() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[20]; // 160-bit key
        random.nextBytes(bytes);
        return Base64.getEncoder().encodeToString(bytes);
    }
    
    private List<String> generateBackupCodes() {
        SecureRandom random = new SecureRandom();
        List<String> codes = new ArrayList<>();
        
        for (int i = 0; i < BACKUP_CODES_COUNT; i++) {
            StringBuilder code = new StringBuilder();
            for (int j = 0; j < BACKUP_CODE_LENGTH; j++) {
                code.append(random.nextInt(10));
            }
            codes.add(code.toString());
        }
        
        return codes;
    }
    
    private String generateQRCodeUrl(String username, String secretKey) {
        String issuer = "BlogAPI";
        String label = issuer + ":" + username;
        
        return String.format(
            "otpauth://totp/%s?secret=%s&issuer=%s&digits=%d&period=%d",
            label.replace(" ", "%20"),
            secretKey,
            issuer,
            TOTP_DIGITS,
            TIME_STEP
        );
    }
    
    private boolean verifyTOTP(String secretKey, String code) {
        try {
            long currentTime = Instant.now().getEpochSecond() / TIME_STEP;
            
            // Check current time window and adjacent windows for clock skew
            for (int i = -WINDOW; i <= WINDOW; i++) {
                String expectedCode = generateTOTP(secretKey, currentTime + i);
                if (constantTimeEquals(code, expectedCode)) {
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            logger.error("Error verifying TOTP for secret key", e);
            return false;
        }
    }
    
    private String generateTOTP(String secretKey, long timeCounter) 
            throws NoSuchAlgorithmException, InvalidKeyException {
        
        byte[] key = Base64.getDecoder().decode(secretKey);
        byte[] data = ByteBuffer.allocate(8).putLong(timeCounter).array();
        
        Mac mac = Mac.getInstance(ALGORITHM);
        mac.init(new SecretKeySpec(key, ALGORITHM));
        byte[] hash = mac.doFinal(data);
        
        int offset = hash[hash.length - 1] & 0x0F;
        int binary = ((hash[offset] & 0x7F) << 24) |
                    ((hash[offset + 1] & 0xFF) << 16) |
                    ((hash[offset + 2] & 0xFF) << 8) |
                    (hash[offset + 3] & 0xFF);
        
        int otp = binary % (int) Math.pow(10, TOTP_DIGITS);
        return String.format("%0" + TOTP_DIGITS + "d", otp);
    }
    
    private boolean verifyBackupCode(TwoFactorAuth twoFactorAuth, String code) {
        if (twoFactorAuth.isBackupCodeUsed(code)) {
            return false; // Code already used
        }
        
        String[] availableCodes = twoFactorAuth.getAvailableBackupCodes();
        return Arrays.asList(availableCodes).contains(code);
    }
    
    private boolean constantTimeEquals(String a, String b) {
        if (a == null || b == null || a.length() != b.length()) {
            return false;
        }
        
        int result = 0;
        for (int i = 0; i < a.length(); i++) {
            result |= a.charAt(i) ^ b.charAt(i);
        }
        return result == 0;
    }
    
    // Response classes
    public static class TwoFactorSetupResponse {
        private final String secretKey;
        private final String qrCodeUrl;
        private final List<String> backupCodes;
        
        public TwoFactorSetupResponse(String secretKey, String qrCodeUrl, List<String> backupCodes) {
            this.secretKey = secretKey;
            this.qrCodeUrl = qrCodeUrl;
            this.backupCodes = backupCodes;
        }
        
        public String getSecretKey() { return secretKey; }
        public String getQrCodeUrl() { return qrCodeUrl; }
        public List<String> getBackupCodes() { return backupCodes; }
    }
    
    public static class TwoFactorStatus {
        private final boolean configured;
        private final boolean enabled;
        private final int availableBackupCodes;
        private final int usedBackupCodes;
        private final LocalDateTime enabledAt;
        private final LocalDateTime lastUsed;
        
        public TwoFactorStatus(boolean configured, boolean enabled, int availableBackupCodes, 
                              int usedBackupCodes, LocalDateTime enabledAt, LocalDateTime lastUsed) {
            this.configured = configured;
            this.enabled = enabled;
            this.availableBackupCodes = availableBackupCodes;
            this.usedBackupCodes = usedBackupCodes;
            this.enabledAt = enabledAt;
            this.lastUsed = lastUsed;
        }
        
        public boolean isConfigured() { return configured; }
        public boolean isEnabled() { return enabled; }
        public int getAvailableBackupCodes() { return availableBackupCodes; }
        public int getUsedBackupCodes() { return usedBackupCodes; }
        public LocalDateTime getEnabledAt() { return enabledAt; }
        public LocalDateTime getLastUsed() { return lastUsed; }
    }
}