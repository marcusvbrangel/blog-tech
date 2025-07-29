package com.blog.api.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.util.ReflectionTestUtils;

import java.security.Key;
import java.util.ArrayList;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(properties = {
    "jwt.secret=myTestSecretKey123456789012345678901234567890",
    "jwt.expiration=3600000"
})
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
        
        Boolean isValid = jwtUtil.validateToken(expiredToken, userDetails);
        
        assertThat(isValid).isFalse();
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
        
        // Should still be able to extract expiration date even if expired
        Date expirationDate = jwtUtil.getExpirationDateFromToken(expiredToken);
        
        assertThat(expirationDate).isNotNull();
        assertThat(expirationDate).isBefore(new Date());
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
}