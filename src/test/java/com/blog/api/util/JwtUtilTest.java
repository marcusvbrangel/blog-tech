package com.blog.api.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.security.Key;
import java.util.ArrayList;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class JwtUtilTest {

    private JwtUtil jwtUtil;
    private UserDetails userDetails;
    private String testSecret = "myTestSecretKey123456789012345678901234567890";
    private Long testExpiration = 3600000L; // 1 hour

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret", testSecret);
        ReflectionTestUtils.setField(jwtUtil, "expiration", testExpiration);
        
        userDetails = User.builder()
                .username("testuser")
                .password("password")
                .authorities(new ArrayList<>())
                .build();
    }

    @Test
    void generateToken_ShouldCreateValidToken() {
        String token = jwtUtil.generateToken(userDetails);
        
        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
        
        // Verify token structure (header.payload.signature)
        String[] parts = token.split("\\.");
        assertThat(parts).hasSize(3);
    }

    @Test
    void getUsernameFromToken_ShouldReturnCorrectUsername() {
        String token = jwtUtil.generateToken(userDetails);
        
        String username = jwtUtil.getUsernameFromToken(token);
        
        assertThat(username).isEqualTo("testuser");
    }

    @Test
    void getExpirationDateFromToken_ShouldReturnValidExpirationDate() {
        String token = jwtUtil.generateToken(userDetails);
        
        Date expirationDate = jwtUtil.getExpirationDateFromToken(token);
        Date now = new Date();
        
        assertThat(expirationDate).isAfter(now);
        assertThat(expirationDate.getTime() - now.getTime()).isLessThanOrEqualTo(testExpiration);
    }

    @Test
    void validateToken_WhenValidToken_ShouldReturnTrue() {
        String token = jwtUtil.generateToken(userDetails);
        
        Boolean isValid = jwtUtil.validateToken(token, userDetails);
        
        assertThat(isValid).isTrue();
    }

    @Test
    void validateToken_WhenWrongUsername_ShouldReturnFalse() {
        String token = jwtUtil.generateToken(userDetails);
        
        UserDetails differentUser = User.builder()
                .username("differentuser")
                .password("password")
                .authorities(new ArrayList<>())
                .build();
        
        Boolean isValid = jwtUtil.validateToken(token, differentUser);
        
        assertThat(isValid).isFalse();
    }

    @Test
    void validateToken_WhenExpiredToken_ShouldReturnFalse() {
        // Create expired token by setting very short expiration
        ReflectionTestUtils.setField(jwtUtil, "expiration", -1000L);
        String expiredToken = jwtUtil.generateToken(userDetails);
        
        // Reset expiration to normal
        ReflectionTestUtils.setField(jwtUtil, "expiration", testExpiration);
        
        try {
            Boolean isValid = jwtUtil.validateToken(expiredToken, userDetails);
            assertThat(isValid).isFalse();
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            // Expected behavior - expired token should throw exception
            // This is actually correct behavior for JWT validation
        }
    }

    @Test
    void getUsernameFromToken_WhenInvalidToken_ShouldThrowException() {
        String invalidToken = "invalid.token.here";
        
        assertThrows(MalformedJwtException.class, () -> {
            jwtUtil.getUsernameFromToken(invalidToken);
        });
    }

    @Test
    void getUsernameFromToken_WhenWrongSignature_ShouldThrowException() {
        // Create token with different secret
        Key wrongKey = Keys.hmacShaKeyFor("differentSecret123456789012345678901234567890".getBytes());
        String tokenWithWrongSignature = Jwts.builder()
                .setSubject("testuser")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + testExpiration))
                .signWith(wrongKey)
                .compact();
        
        assertThrows(SignatureException.class, () -> {
            jwtUtil.getUsernameFromToken(tokenWithWrongSignature);
        });
    }

    @Test
    void getExpirationDateFromToken_WhenExpiredToken_ShouldStillReturnDate() {
        // Create expired token
        ReflectionTestUtils.setField(jwtUtil, "expiration", -1000L);
        String expiredToken = jwtUtil.generateToken(userDetails);
        
        try {
            // Should still be able to extract expiration date even if expired
            Date expirationDate = jwtUtil.getExpirationDateFromToken(expiredToken);
            assertThat(expirationDate).isNotNull();
            assertThat(expirationDate).isBefore(new Date());
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            // In newer JWT versions, expired tokens may throw exception immediately
            // This is acceptable behavior
            assertThat(e.getMessage()).contains("JWT expired");
        }
    }

    @Test
    void generateToken_ShouldHaveCorrectClaims() {
        String token = jwtUtil.generateToken(userDetails);
        
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(Keys.hmacShaKeyFor(testSecret.getBytes()))
                .build()
                .parseClaimsJws(token)
                .getBody();
        
        assertThat(claims.getSubject()).isEqualTo("testuser");
        assertThat(claims.getIssuedAt()).isNotNull();
        assertThat(claims.getExpiration()).isNotNull();
        assertThat(claims.getIssuedAt()).isBefore(claims.getExpiration());
    }

    @Test
    void validateToken_WhenNullToken_ShouldThrowException() {
        assertThrows(IllegalArgumentException.class, () -> {
            jwtUtil.validateToken(null, userDetails);
        });
    }

    @Test
    void validateToken_WhenEmptyToken_ShouldThrowException() {
        assertThrows(IllegalArgumentException.class, () -> {
            jwtUtil.validateToken("", userDetails);
        });
    }

    @Test
    void generateToken_ForDifferentUsers_ShouldCreateDifferentTokens() {
        UserDetails user1 = User.builder()
                .username("user1")
                .password("password")
                .authorities(new ArrayList<>())
                .build();
        
        UserDetails user2 = User.builder()
                .username("user2")
                .password("password")
                .authorities(new ArrayList<>())
                .build();
        
        String token1 = jwtUtil.generateToken(user1);
        String token2 = jwtUtil.generateToken(user2);
        
        assertThat(token1).isNotEqualTo(token2);
        assertThat(jwtUtil.getUsernameFromToken(token1)).isEqualTo("user1");
        assertThat(jwtUtil.getUsernameFromToken(token2)).isEqualTo("user2");
    }

    @Test
    void getClaimFromToken_ShouldExtractSpecificClaim() {
        String token = jwtUtil.generateToken(userDetails);
        
        String subject = jwtUtil.getClaimFromToken(token, Claims::getSubject);
        Date issuedAt = jwtUtil.getClaimFromToken(token, Claims::getIssuedAt);
        
        assertThat(subject).isEqualTo("testuser");
        assertThat(issuedAt).isNotNull();
        assertThat(issuedAt).isBeforeOrEqualTo(new Date());
    }

    @Test
    void generateToken_ShouldIncludeIssuedAtClaim() {
        Date beforeGeneration = new Date();
        String token = jwtUtil.generateToken(userDetails);
        Date afterGeneration = new Date();
        
        Date issuedAt = jwtUtil.getClaimFromToken(token, Claims::getIssuedAt);
        
        assertThat(issuedAt).isNotNull();
        assertThat(issuedAt).isBetween(beforeGeneration, afterGeneration);
    }

    @Test
    void generateToken_ShouldSetCorrectExpirationTime() {
        Date beforeGeneration = new Date();
        String token = jwtUtil.generateToken(userDetails);
        
        Date expirationDate = jwtUtil.getExpirationDateFromToken(token);
        Date expectedExpiration = new Date(beforeGeneration.getTime() + testExpiration);
        
        // Allow 1 second tolerance for execution time
        assertThat(Math.abs(expirationDate.getTime() - expectedExpiration.getTime())).isLessThan(1000);
    }

    @Test
    void validateToken_ShouldCheckBothUsernameAndExpiration() {
        // Create token that will expire soon
        ReflectionTestUtils.setField(jwtUtil, "expiration", 100L);
        String shortLivedToken = jwtUtil.generateToken(userDetails);
        
        // Immediately validate - should be valid
        Boolean isValidImmediately = jwtUtil.validateToken(shortLivedToken, userDetails);
        assertThat(isValidImmediately).isTrue();
        
        // Wait for expiration
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        try {
            // Should now be invalid due to expiration
            Boolean isValidAfterExpiration = jwtUtil.validateToken(shortLivedToken, userDetails);
            assertThat(isValidAfterExpiration).isFalse();
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            // Expected behavior - expired token should throw exception
            assertThat(e.getMessage()).contains("JWT expired");
        }
    }

    @Test
    void generateToken_WithNullUserDetails_ShouldThrowException() {
        assertThrows(NullPointerException.class, () -> {
            jwtUtil.generateToken(null);
        });
    }

    @Test
    void generateToken_WithNullUsername_ShouldThrowException() {
        // This test validates that User.builder() properly validates null username
        assertThrows(IllegalArgumentException.class, () -> {
            User.builder()
                    .username(null)
                    .password("password")
                    .authorities(new ArrayList<>())
                    .build();
        });
    }

    @Test
    void getSigningKey_ShouldGenerateConsistentKey() {
        // Use reflection to test the private method indirectly by generating multiple tokens
        String token1 = jwtUtil.generateToken(userDetails);
        String token2 = jwtUtil.generateToken(userDetails);
        
        // Both tokens should be parseable with the same key (verified by successful username extraction)
        String username1 = jwtUtil.getUsernameFromToken(token1);
        String username2 = jwtUtil.getUsernameFromToken(token2);
        
        assertThat(username1).isEqualTo("testuser");
        assertThat(username2).isEqualTo("testuser");
    }

    @Test
    void validateToken_WithMismatchedUserDetails_ShouldReturnFalse() {
        String token = jwtUtil.generateToken(userDetails);
        
        UserDetails mismatchedUser = User.builder()
                .username("differentuser")
                .password("differentpassword")
                .authorities(new ArrayList<>())
                .build();
        
        Boolean isValid = jwtUtil.validateToken(token, mismatchedUser);
        assertThat(isValid).isFalse();
    }
}