package com.blog.api.service;

import com.blog.api.entity.TermsAcceptance;
import com.blog.api.entity.User;
import com.blog.api.exception.ResourceNotFoundException;
import com.blog.api.repository.TermsAcceptanceRepository;
import com.blog.api.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class TermsService {

    private static final Logger logger = LoggerFactory.getLogger(TermsService.class);

    @Autowired
    private TermsAcceptanceRepository termsAcceptanceRepository;

    @Autowired
    private UserRepository userRepository;

    @Value("${blog.terms.current-version:v1.0}")
    private String currentTermsVersion;

    @Value("${blog.terms.force-acceptance:true}")
    private boolean forceAcceptance;

    @Value("${blog.terms.retention-days:1095}") // 3 years default
    private int retentionDays;

    /**
     * Get current terms version from configuration
     */
    public String getCurrentTermsVersion() {
        return currentTermsVersion;
    }

    /**
     * Check if user needs to accept terms
     */
    @Cacheable(value = "terms_status", key = "'user:' + #userId")
    public boolean userNeedsToAcceptTerms(Long userId) {
        if (!forceAcceptance) {
            return false;
        }

        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            return true; // User not found, require acceptance
        }

        User user = userOpt.get();
        String userAcceptedVersion = user.getTermsAcceptedVersion();

        // User hasn't accepted any version
        if (userAcceptedVersion == null || userAcceptedVersion.trim().isEmpty()) {
            return true;
        }

        // User's accepted version doesn't match current version
        return !currentTermsVersion.equals(userAcceptedVersion);
    }

    /**
     * Check if user needs to accept terms by User entity
     */
    public boolean userNeedsToAcceptTerms(User user) {
        if (!forceAcceptance || user == null) {
            return !forceAcceptance ? false : true;
        }

        String userAcceptedVersion = user.getTermsAcceptedVersion();
        return userAcceptedVersion == null || 
               userAcceptedVersion.trim().isEmpty() || 
               !currentTermsVersion.equals(userAcceptedVersion);
    }

    /**
     * Accept current terms for a user
     */
    @Transactional
    @CacheEvict(value = "terms_status", key = "'user:' + #userId")
    public TermsAcceptance acceptTerms(Long userId, HttpServletRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        return acceptTerms(user, request);
    }

    /**
     * Accept current terms for a user with User entity
     */
    @Transactional
    @CacheEvict(value = "terms_status", key = "'user:' + #user.id")
    public TermsAcceptance acceptTerms(User user, HttpServletRequest request) {
        // Extract request information
        String ipAddress = getClientIpAddress(request);
        String userAgent = request.getHeader("User-Agent");

        return acceptTerms(user, currentTermsVersion, ipAddress, userAgent);
    }

    /**
     * Accept specific terms version for a user
     */
    @Transactional
    @CacheEvict(value = "terms_status", key = "'user:' + #user.id")
    public TermsAcceptance acceptTerms(User user, String termsVersion, String ipAddress, String userAgent) {
        // Check if user already accepted this version
        if (termsAcceptanceRepository.existsByUserAndTermsVersion(user, termsVersion)) {
            logger.info("User {} already accepted terms version {}", user.getUsername(), termsVersion);
            return termsAcceptanceRepository.findByUserAndTermsVersion(user, termsVersion)
                    .orElseThrow(() -> new IllegalStateException("Terms acceptance exists but not found"));
        }

        // Create new terms acceptance record
        TermsAcceptance acceptance = TermsAcceptance.withCurrentTimestamp(user, termsVersion)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .build();

        // Save acceptance record
        acceptance = termsAcceptanceRepository.save(acceptance);

        // Update user's current accepted version
        user.setTermsAcceptedVersion(termsVersion);
        userRepository.save(user);

        logger.info("User {} accepted terms version {} from IP {}", 
                   user.getUsername(), termsVersion, ipAddress);

        return acceptance;
    }

    /**
     * Get user's terms acceptance history
     */
    public List<TermsAcceptance> getUserTermsHistory(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        return termsAcceptanceRepository.findByUserOrderByAcceptedAtDesc(user);
    }

    /**
     * Get user's latest terms acceptance
     */
    public Optional<TermsAcceptance> getUserLatestAcceptance(Long userId) {
        return termsAcceptanceRepository.findLatestByUserId(userId);
    }

    /**
     * Get user's latest terms acceptance by User entity
     */
    public Optional<TermsAcceptance> getUserLatestAcceptance(User user) {
        return termsAcceptanceRepository.findTopByUserOrderByAcceptedAtDesc(user);
    }

    /**
     * Get acceptances for a specific terms version
     */
    public List<TermsAcceptance> getAcceptancesForVersion(String termsVersion) {
        return termsAcceptanceRepository.findByTermsVersionOrderByAcceptedAtDesc(termsVersion);
    }

    /**
     * Get users who haven't accepted latest terms
     */
    public List<User> getUsersWithoutLatestTerms() {
        return termsAcceptanceRepository.findUsersWithoutLatestTerms(currentTermsVersion);
    }

    /**
     * Get users who haven't accepted latest terms with pagination
     */
    public Page<User> getUsersWithoutLatestTerms(Pageable pageable) {
        return termsAcceptanceRepository.findUsersWithoutLatestTerms(currentTermsVersion, pageable);
    }

    /**
     * Get acceptance statistics for current version
     */
    public AcceptanceStatistics getCurrentVersionStatistics() {
        return getVersionStatistics(currentTermsVersion);
    }

    /**
     * Get acceptance statistics for specific version
     */
    public AcceptanceStatistics getVersionStatistics(String termsVersion) {
        Object[] stats = termsAcceptanceRepository.getAcceptanceStatistics(termsVersion);
        
        if (stats == null || stats.length == 0) {
            return new AcceptanceStatistics(termsVersion, 0, 0, null, null);
        }

        Long totalAcceptances = stats[0] != null ? ((Number) stats[0]).longValue() : 0;
        Long uniqueUsers = stats[1] != null ? ((Number) stats[1]).longValue() : 0;
        LocalDateTime firstAcceptance = (LocalDateTime) stats[2];
        LocalDateTime lastAcceptance = (LocalDateTime) stats[3];

        return new AcceptanceStatistics(termsVersion, totalAcceptances, uniqueUsers, 
                                      firstAcceptance, lastAcceptance);
    }

    /**
     * Clean up old acceptance records (LGPD compliance)
     */
    @Transactional
    public int cleanupOldAcceptances() {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(retentionDays);
        int deletedCount = termsAcceptanceRepository.deleteAcceptancesOlderThan(cutoffDate);
        
        if (deletedCount > 0) {
            logger.info("Cleaned up {} old terms acceptance records older than {}", 
                       deletedCount, cutoffDate);
        }
        
        return deletedCount;
    }

    /**
     * Force re-acceptance for all users (when terms are updated)
     */
    @Transactional
    @CacheEvict(value = "terms_status", allEntries = true)
    public void forceReAcceptanceForAllUsers() {
        logger.warn("Forcing re-acceptance of terms for all users due to terms update");
        
        // This would typically be done via a database migration script
        // but providing service method for admin operations
        userRepository.findAll().forEach(user -> {
            if (user.getTermsAcceptedVersion() != null) {
                user.setTermsAcceptedVersion(null);
                userRepository.save(user);
            }
        });
    }

    /**
     * Check if terms acceptance is required for the system
     */
    public boolean isTermsAcceptanceRequired() {
        return forceAcceptance;
    }

    /**
     * Get monthly acceptance statistics
     */
    public List<MonthlyStatistics> getMonthlyStatistics() {
        List<Object[]> rawStats = termsAcceptanceRepository.getMonthlyAcceptanceStatistics();
        
        return rawStats.stream()
                .map(row -> new MonthlyStatistics(
                    ((Number) row[0]).intValue(), // year
                    ((Number) row[1]).intValue(), // month
                    ((Number) row[2]).longValue() // acceptances
                ))
                .toList();
    }

    /**
     * Extract client IP address from request
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }

        return request.getRemoteAddr();
    }

    // Inner classes for statistics
    public static class AcceptanceStatistics {
        private final String termsVersion;
        private final long totalAcceptances;
        private final long uniqueUsers;
        private final LocalDateTime firstAcceptance;
        private final LocalDateTime lastAcceptance;

        public AcceptanceStatistics(String termsVersion, long totalAcceptances, long uniqueUsers,
                                   LocalDateTime firstAcceptance, LocalDateTime lastAcceptance) {
            this.termsVersion = termsVersion;
            this.totalAcceptances = totalAcceptances;
            this.uniqueUsers = uniqueUsers;
            this.firstAcceptance = firstAcceptance;
            this.lastAcceptance = lastAcceptance;
        }

        // Getters
        public String getTermsVersion() { return termsVersion; }
        public long getTotalAcceptances() { return totalAcceptances; }
        public long getUniqueUsers() { return uniqueUsers; }
        public LocalDateTime getFirstAcceptance() { return firstAcceptance; }
        public LocalDateTime getLastAcceptance() { return lastAcceptance; }
    }

    public static class MonthlyStatistics {
        private final int year;
        private final int month;
        private final long acceptances;

        public MonthlyStatistics(int year, int month, long acceptances) {
            this.year = year;
            this.month = month;
            this.acceptances = acceptances;
        }

        // Getters
        public int getYear() { return year; }
        public int getMonth() { return month; }
        public long getAcceptances() { return acceptances; }
    }
}