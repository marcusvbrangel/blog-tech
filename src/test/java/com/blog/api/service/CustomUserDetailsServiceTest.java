package com.blog.api.service;

import com.blog.api.entity.User;
import com.blog.api.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.ofEncrypted("testuser", "test@example.com", "encodedPassword")
                .role(User.Role.USER)
                .emailVerified(true)
                .build();
        testUser.setId(1L);
        testUser.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void loadUserByUsername_ShouldReturnUserDetails_WhenUserFoundByUsername() {
        // Arrange
        String username = "testuser";
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(testUser));
        when(userRepository.findByEmail(username)).thenReturn(Optional.empty());

        // Act
        UserDetails result = customUserDetailsService.loadUserByUsername(username);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo("testuser");
        assertThat(result.getPassword()).isEqualTo("encodedPassword");
        assertThat(result.getAuthorities()).hasSize(1);
        assertThat(result.getAuthorities().iterator().next().getAuthority()).isEqualTo("ROLE_USER");
        assertThat(result.isEnabled()).isTrue();
        assertThat(result.isAccountNonExpired()).isTrue();
        assertThat(result.isAccountNonLocked()).isTrue();
        assertThat(result.isCredentialsNonExpired()).isTrue();
        
        verify(userRepository).findByUsername(username);
    }

    @Test
    void loadUserByUsername_ShouldReturnUserDetails_WhenUserFoundByEmail() {
        // Arrange
        String email = "test@example.com";
        when(userRepository.findByUsername(email)).thenReturn(Optional.empty());
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(testUser));

        // Act
        UserDetails result = customUserDetailsService.loadUserByUsername(email);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo("testuser");
        assertThat(result.getPassword()).isEqualTo("encodedPassword");
        assertThat(result.getAuthorities()).hasSize(1);
        assertThat(result.getAuthorities().iterator().next().getAuthority()).isEqualTo("ROLE_USER");
        
        verify(userRepository).findByUsername(email);
        verify(userRepository).findByEmail(email);
    }

    @Test
    void loadUserByUsername_ShouldReturnUserDetailsWithAdminRole_WhenUserIsAdmin() {
        // Arrange
        String username = "admin";
        User adminUser = User.ofEncrypted("admin", "admin@example.com", "encodedPassword")
                .role(User.Role.ADMIN)
                .emailVerified(true)
                .build();
        adminUser.setId(2L);
        adminUser.setCreatedAt(LocalDateTime.now());
        
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(adminUser));
        when(userRepository.findByEmail(username)).thenReturn(Optional.empty());

        // Act
        UserDetails result = customUserDetailsService.loadUserByUsername(username);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo("admin");
        assertThat(result.getAuthorities()).hasSize(1);
        assertThat(result.getAuthorities().iterator().next().getAuthority()).isEqualTo("ROLE_ADMIN");
        
        verify(userRepository).findByUsername(username);
    }

    @Test
    void loadUserByUsername_ShouldReturnUserDetailsWithModeratorRole_WhenUserIsModerator() {
        // Arrange
        String username = "moderator";
        User moderatorUser = User.ofEncrypted("moderator", "moderator@example.com", "encodedPassword")
                .role(User.Role.AUTHOR)
                .emailVerified(true)
                .build();
        moderatorUser.setId(3L);
        moderatorUser.setCreatedAt(LocalDateTime.now());
        
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(moderatorUser));
        when(userRepository.findByEmail(username)).thenReturn(Optional.empty());

        // Act
        UserDetails result = customUserDetailsService.loadUserByUsername(username);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo("moderator");
        assertThat(result.getAuthorities()).hasSize(1);
        assertThat(result.getAuthorities().iterator().next().getAuthority()).isEqualTo("ROLE_AUTHOR");
        
        verify(userRepository).findByUsername(username);
    }

    @Test
    void loadUserByUsername_ShouldThrowUsernameNotFoundException_WhenUserNotFound() {
        // Arrange
        String username = "nonexistentuser";
        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());
        when(userRepository.findByEmail(username)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> customUserDetailsService.loadUserByUsername(username))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("User not found: nonexistentuser");
        
        verify(userRepository).findByUsername(username);
        verify(userRepository).findByEmail(username);
    }

    @Test
    void loadUserByUsername_ShouldTryEmailSearch_WhenUsernameSearchFails() {
        // Arrange
        String identifier = "user@example.com";
        when(userRepository.findByUsername(identifier)).thenReturn(Optional.empty());
        when(userRepository.findByEmail(identifier)).thenReturn(Optional.of(testUser));

        // Act
        UserDetails result = customUserDetailsService.loadUserByUsername(identifier);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo("testuser");
        
        verify(userRepository).findByUsername(identifier);
        verify(userRepository).findByEmail(identifier);
    }

    @Test
    void loadUserByUsername_ShouldHandleNullInput() {
        // Arrange
        String nullUsername = null;
        when(userRepository.findByUsername(nullUsername)).thenReturn(Optional.empty());
        when(userRepository.findByEmail(nullUsername)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> customUserDetailsService.loadUserByUsername(nullUsername))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("User not found: null");
        
        verify(userRepository).findByUsername(nullUsername);
        verify(userRepository).findByEmail(nullUsername);
    }

    @Test
    void loadUserByUsername_ShouldHandleEmptyInput() {
        // Arrange
        String emptyUsername = "";
        when(userRepository.findByUsername(emptyUsername)).thenReturn(Optional.empty());
        when(userRepository.findByEmail(emptyUsername)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> customUserDetailsService.loadUserByUsername(emptyUsername))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("User not found: ");
        
        verify(userRepository).findByUsername(emptyUsername);
        verify(userRepository).findByEmail(emptyUsername);
    }
}