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
import com.blog.api.service.AuditLogService;
import com.blog.api.util.JwtUtil;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

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
                .password("ValidTest123!")
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
    
    @Bean
    @Primary
    public AuditLogService auditLogService() {
        return Mockito.mock(AuditLogService.class);
    }
    
    @Bean
    @Primary
    public com.blog.api.service.JwtBlacklistService jwtBlacklistService() {
        return Mockito.mock(com.blog.api.service.JwtBlacklistService.class);
    }
    
    @Bean
    @Primary
    public com.blog.api.repository.RevokedTokenRepository revokedTokenRepository() {
        return Mockito.mock(com.blog.api.repository.RevokedTokenRepository.class);
    }
    
    @Bean 
    @Primary
    public com.blog.api.service.VerificationTokenService verificationTokenService() {
        return Mockito.mock(com.blog.api.service.VerificationTokenService.class);
    }
    
    @Bean
    @Primary  
    public com.blog.api.service.RefreshTokenService refreshTokenService() {
        return Mockito.mock(com.blog.api.service.RefreshTokenService.class);
    }
    
    @Bean
    @Primary
    public org.springframework.mail.javamail.JavaMailSender javaMailSender() {
        return Mockito.mock(org.springframework.mail.javamail.JavaMailSender.class);
    }
    
    @Bean
    @Primary
    public com.blog.api.service.NewsletterService newsletterService() {
        return Mockito.mock(com.blog.api.service.NewsletterService.class);
    }
    
    @Bean
    @Primary
    public com.blog.api.service.PostService postService() {
        return Mockito.mock(com.blog.api.service.PostService.class);
    }
    
    @Bean
    @Primary
    public com.blog.api.service.CommentService commentService() {
        return Mockito.mock(com.blog.api.service.CommentService.class);
    }
    
    @Bean
    @Primary
    public com.blog.api.service.CategoryService categoryService() {
        return Mockito.mock(com.blog.api.service.CategoryService.class);
    }
    
    // ===== MISSING REPOSITORIES IDENTIFIED BY AGENT =====
    
    @Bean
    @Primary
    public com.blog.api.repository.RefreshTokenRepository refreshTokenRepository() {
        return Mockito.mock(com.blog.api.repository.RefreshTokenRepository.class);
    }
    
    @Bean
    @Primary
    public com.blog.api.repository.NewsletterSubscriberRepository newsletterSubscriberRepository() {
        return Mockito.mock(com.blog.api.repository.NewsletterSubscriberRepository.class);
    }
    
    @Bean
    @Primary
    public com.blog.api.repository.NewsletterTokenRepository newsletterTokenRepository() {
        return Mockito.mock(com.blog.api.repository.NewsletterTokenRepository.class);
    }
    
    @Bean
    @Primary
    public com.blog.api.service.NewsletterTokenService newsletterTokenService() {
        return Mockito.mock(com.blog.api.service.NewsletterTokenService.class);
    }
    
    @Bean
    @Primary
    public com.blog.api.repository.TwoFactorAuthRepository twoFactorAuthRepository() {
        return Mockito.mock(com.blog.api.repository.TwoFactorAuthRepository.class);
    }
    
    @Bean
    @Primary
    public com.blog.api.service.TwoFactorAuthService twoFactorAuthService() {
        return Mockito.mock(com.blog.api.service.TwoFactorAuthService.class);
    }
    
    @Bean
    @Primary
    public com.blog.api.repository.AuditLogRepository auditLogRepository() {
        return Mockito.mock(com.blog.api.repository.AuditLogRepository.class);
    }
    
    @Bean
    @Primary
    public com.blog.api.repository.PostRepository postRepository() {
        return Mockito.mock(com.blog.api.repository.PostRepository.class);
    }
    
    @Bean
    @Primary
    public com.blog.api.repository.CommentRepository commentRepository() {
        return Mockito.mock(com.blog.api.repository.CommentRepository.class);
    }
    
    @Bean
    @Primary
    public com.blog.api.repository.CategoryRepository categoryRepository() {
        return Mockito.mock(com.blog.api.repository.CategoryRepository.class);
    }
    
    // ===== MICROMETER MOCKS =====
    
    @Bean
    @Primary
    public io.micrometer.core.instrument.Counter postCreationCounter() {
        return Mockito.mock(io.micrometer.core.instrument.Counter.class);
    }
    
    @Bean
    @Primary
    public io.micrometer.core.instrument.MeterRegistry meterRegistry() {
        return Mockito.mock(io.micrometer.core.instrument.MeterRegistry.class);
    }
    
    @Bean
    @Primary
    public io.micrometer.core.instrument.Timer databaseQueryTimer() {
        return Mockito.mock(io.micrometer.core.instrument.Timer.class);
    }
    
    @Bean
    @Primary
    public SecurityFilterChain testSecurityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(authz -> authz.anyRequest().permitAll());
        return http.build();
    }
}