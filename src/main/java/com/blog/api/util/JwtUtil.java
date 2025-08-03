package com.blog.api.util;

import com.blog.api.service.JwtBlacklistService;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

@Component
public class JwtUtil {

    private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long expiration;

    // Lazy injection to avoid circular dependency
    @Autowired
    @Lazy
    private JwtBlacklistService jwtBlacklistService;

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    /**
     * Generate JWT token with unique JTI for blacklist support.
     * 
     * @param userDetails the user details
     * @return the generated JWT token
     */
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        return createToken(claims, userDetails.getUsername());
    }

    /**
     * Generate JWT token with custom claims and unique JTI.
     * 
     * @param claims custom claims to include
     * @param userDetails the user details
     * @return the generated JWT token
     */
    public String generateToken(Map<String, Object> claims, UserDetails userDetails) {
        return createToken(claims, userDetails.getUsername());
    }

    /**
     * Create JWT token with unique JTI for revocation support.
     * 
     * @param claims the claims to include
     * @param subject the subject (username)
     * @return the JWT token string
     */
    private String createToken(Map<String, Object> claims, String subject) {
        String jti = UUID.randomUUID().toString();
        
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setId(jti) // Set unique JWT ID for blacklist support
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Validate JWT token including blacklist check.
     * This method now checks if the token has been revoked.
     * 
     * @param token the JWT token
     * @param userDetails the user details
     * @return true if token is valid and not revoked
     */
    public Boolean validateToken(String token, UserDetails userDetails) {
        try {
            final String username = getUsernameFromToken(token);
            final String jti = getJtiFromToken(token);
            
            // First check if token is revoked (fast cache lookup)
            if (jwtBlacklistService != null && jwtBlacklistService.isTokenRevoked(jti)) {
                logger.debug("Token validation failed: token is revoked (JTI: {})", jti);
                return false;
            }
            
            // Then check standard validations
            boolean isValid = username.equals(userDetails.getUsername()) && !isTokenExpired(token);
            
            if (!isValid) {
                logger.debug("Token validation failed: username mismatch or token expired");
            }
            
            return isValid;
            
        } catch (Exception e) {
            logger.error("Error validating token", e);
            return false;
        }
    }

    /**
     * Extract username from JWT token.
     * 
     * @param token the JWT token
     * @return the username
     */
    public String getUsernameFromToken(String token) {
        return getClaimFromToken(token, Claims::getSubject);
    }

    /**
     * Extract JWT ID (JTI) from token for blacklist operations.
     * 
     * @param token the JWT token
     * @return the JWT ID
     */
    public String getJtiFromToken(String token) {
        return getClaimFromToken(token, Claims::getId);
    }

    /**
     * Extract expiration date from JWT token.
     * 
     * @param token the JWT token
     * @return the expiration date
     */
    public Date getExpirationDateFromToken(String token) {
        return getClaimFromToken(token, Claims::getExpiration);
    }

    /**
     * Extract issued at date from JWT token.
     * 
     * @param token the JWT token
     * @return the issued at date
     */
    public Date getIssuedAtDateFromToken(String token) {
        return getClaimFromToken(token, Claims::getIssuedAt);
    }

    /**
     * Extract specific claim from JWT token.
     * 
     * @param token the JWT token
     * @param claimsResolver function to extract the claim
     * @return the extracted claim value
     */
    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Extract all claims from JWT token.
     * 
     * @param token the JWT token
     * @return all claims
     */
    private Claims getAllClaimsFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * Check if JWT token is expired.
     * 
     * @param token the JWT token
     * @return true if token is expired
     */
    private Boolean isTokenExpired(String token) {
        final Date expiration = getExpirationDateFromToken(token);
        return expiration.before(new Date());
    }

    /**
     * Check if token can be refreshed (not expired by more than refresh grace period).
     * 
     * @param token the JWT token
     * @return true if token can be refreshed
     */
    public Boolean canTokenBeRefreshed(String token) {
        try {
            final Date expiration = getExpirationDateFromToken(token);
            // Allow refresh if token expired less than 1 hour ago
            final Date refreshCutoff = new Date(System.currentTimeMillis() - 3600000); // 1 hour
            return expiration.after(refreshCutoff);
        } catch (Exception e) {
            logger.error("Error checking if token can be refreshed", e);
            return false;
        }
    }

    /**
     * Extract token from Authorization header.
     * 
     * @param authHeader the Authorization header value
     * @return the token string without "Bearer " prefix, or null if invalid
     */
    public String extractTokenFromHeader(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }

    /**
     * Validate token format without full validation.
     * Used for preliminary checks before expensive operations.
     * 
     * @param token the JWT token
     * @return true if token has valid format
     */
    public Boolean isValidTokenFormat(String token) {
        if (token == null || token.trim().isEmpty()) {
            return false;
        }
        
        // JWT should have 3 parts separated by dots
        String[] parts = token.split("\\.");
        return parts.length == 3;
    }
}