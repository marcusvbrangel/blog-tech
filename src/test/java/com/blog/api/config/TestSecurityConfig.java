package com.blog.api.config;

import com.blog.api.dto.UserDTO;
import com.blog.api.entity.User;
import com.blog.api.repository.UserRepository;
import com.blog.api.repository.TermsAcceptanceRepository;
import com.blog.api.repository.VerificationTokenRepository;
import com.blog.api.service.CustomUserDetailsService;
import com.blog.api.service.TermsService;
import com.blog.api.service.UserService;
import com.blog.api.service.EmailService;
import com.blog.api.util.JwtUtil;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@TestConfiguration
public class TestSecurityConfig {

    @Bean
    @Primary
    public JwtUtil jwtUtil() {
        return Mockito.mock(JwtUtil.class);
    }

    @Bean
    @Primary
    public UserRepository userRepository() {
        return Mockito.mock(UserRepository.class);
    }

    @Bean
    @Primary
    public TermsAcceptanceRepository termsAcceptanceRepository() {
        return Mockito.mock(TermsAcceptanceRepository.class);
    }

    @Bean
    @Primary
    public VerificationTokenRepository verificationTokenRepository() {
        return Mockito.mock(VerificationTokenRepository.class);
    }

    @Bean
    @Primary
    public EmailService emailService() {
        return Mockito.mock(EmailService.class);
    }

    @Bean
    @Primary
    public PasswordEncoder passwordEncoder() {
        return Mockito.mock(PasswordEncoder.class);
    }

    @Bean
    @Primary
    public CustomUserDetailsService customUserDetailsService() {
        CustomUserDetailsService mockService = Mockito.mock(CustomUserDetailsService.class);
        
        // Create a default UserDetails for tests
        UserDetails mockUserDetails = org.springframework.security.core.userdetails.User.builder()
                .username("testuser")
                .password("TestPass123!")
                .authorities("ROLE_USER")
                .build();
        
        when(mockService.loadUserByUsername(anyString())).thenReturn(mockUserDetails);
        
        return mockService;
    }

    @Bean
    @Primary
    public TermsService termsService() {
        TermsService mockService = Mockito.mock(TermsService.class);
        
        // Mock to return that user does NOT need to accept terms (already accepted)
        when(mockService.userNeedsToAcceptTerms(any(Long.class))).thenReturn(false);
        when(mockService.userNeedsToAcceptTerms(any(User.class))).thenReturn(false);
        when(mockService.isTermsAcceptanceRequired()).thenReturn(false);
        
        return mockService;
    }

    @Bean
    @Primary
    public UserService userServiceForTests() {
        UserService mockService = Mockito.mock(UserService.class);
        
        // Create a default UserDTO for tests
        UserDTO mockUser = new UserDTO(
                1L,
                "testuser",
                "test@example.com",
                User.Role.USER,
                LocalDateTime.now(),
                true,
                LocalDateTime.now(),
                null,
                "1.0",
                true
        );
        
        // Mock common user service calls
        when(mockService.getUserByUsername(anyString())).thenReturn(mockUser);
        when(mockService.getUserById(any(Long.class))).thenReturn(mockUser);
        
        return mockService;
    }
}