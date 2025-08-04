package com.blog.api.service;

import com.blog.api.entity.User;
import com.blog.api.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Custom User Details Service Tests")
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
    @DisplayName("Deve retornar UserDetails quando usuário é encontrado pelo nome de usuário")
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
    @DisplayName("Deve retornar UserDetails quando usuário é encontrado pelo email")
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
    @DisplayName("Deve lançar UsernameNotFoundException quando usuário não é encontrado")
    void loadUserByUsername_ShouldThrowException_WhenUserNotFound() {
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
    @DisplayName("Deve incluir autoridades corretas baseadas no papel do usuário")
    void loadUserByUsername_ShouldIncludeCorrectAuthorities_BasedOnUserRole() {
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
    @DisplayName("Deve lidar com usuário ADMIN corretamente")
    void loadUserByUsername_ShouldHandleAdminUser_Correctly() {
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
    @DisplayName("Deve lidar com busca por email quando contém caracteres especiais")
    void loadUserByUsername_ShouldHandleEmailSearch_WithSpecialCharacters() {
        // Arrange
        String emailWithSpecialChars = "test+alias@example.com";
        User userWithSpecialChars = User.ofEncrypted("testuser", "test+alias@example.com", "encodedPassword")
                .role(User.Role.USER)
                .emailVerified(true)
                .build();
        userWithSpecialChars.setId(4L);
        userWithSpecialChars.setCreatedAt(LocalDateTime.now());

        when(userRepository.findByUsername(emailWithSpecialChars)).thenReturn(Optional.empty());
        when(userRepository.findByEmail(emailWithSpecialChars)).thenReturn(Optional.of(userWithSpecialChars));

        // Act
        UserDetails result = customUserDetailsService.loadUserByUsername(emailWithSpecialChars);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo("testuser");
        assertThat(result.getPassword()).isEqualTo("encodedPassword");
        assertThat(result.getAuthorities()).hasSize(1);
        assertThat(result.getAuthorities().iterator().next().getAuthority()).isEqualTo("ROLE_USER");

        verify(userRepository).findByUsername(emailWithSpecialChars);
        verify(userRepository).findByEmail(emailWithSpecialChars);
    }

    @Test
    @DisplayName("Deve priorizar busca por nome de usuário sobre email")
    void loadUserByUsername_ShouldPrioritizeUsernameOverEmail() {
        // Arrange
        String identifier = "testuser";
        when(userRepository.findByUsername(identifier)).thenReturn(Optional.of(testUser));
        when(userRepository.findByEmail(identifier)).thenReturn(Optional.empty());

        // Act
        UserDetails result = customUserDetailsService.loadUserByUsername(identifier);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo("testuser");

        verify(userRepository).findByUsername(identifier);
        verify(userRepository).findByEmail(identifier);
    }

    @Test
    @DisplayName("Deve retornar usuário com senha criptografada")
    void loadUserByUsername_ShouldReturnUserWithEncodedPassword() {
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

        verify(userRepository).findByUsername(username);
    }

    @Test
    @DisplayName("Deve verificar se conta está habilitada baseada na verificação de email")
    void loadUserByUsername_ShouldCheckAccountEnabled_BasedOnEmailVerification() {
        // Arrange
        String username = "testuser";
        testUser.setEmailVerified(false);
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(testUser));
        when(userRepository.findByEmail(username)).thenReturn(Optional.empty());

        // Act
        UserDetails result = customUserDetailsService.loadUserByUsername(username);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo("testuser");
        assertThat(result.isEnabled()).isFalse();

        verify(userRepository).findByUsername(username);
    }

    @Test
    @DisplayName("Deve verificar que conta não expira")
    void loadUserByUsername_ShouldVerifyAccountNonExpired() {
        // Arrange
        String username = "testuser";
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(testUser));
        when(userRepository.findByEmail(username)).thenReturn(Optional.empty());

        // Act
        UserDetails result = customUserDetailsService.loadUserByUsername(username);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo("testuser");
        assertThat(result.isAccountNonExpired()).isTrue();

        verify(userRepository).findByUsername(username);
    }

    @Test
    @DisplayName("Deve verificar que conta não está bloqueada")
    void loadUserByUsername_ShouldVerifyAccountNonLocked() {
        // Arrange
        String username = "testuser";
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(testUser));
        when(userRepository.findByEmail(username)).thenReturn(Optional.empty());

        // Act
        UserDetails result = customUserDetailsService.loadUserByUsername(username);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo("testuser");
        assertThat(result.isAccountNonLocked()).isTrue();

        verify(userRepository).findByUsername(username);
    }

    @Test
    @DisplayName("Deve verificar que credenciais não expiraram")
    void loadUserByUsername_ShouldVerifyCredentialsNonExpired() {
        // Arrange
        String username = "testuser";
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(testUser));
        when(userRepository.findByEmail(username)).thenReturn(Optional.empty());

        // Act
        UserDetails result = customUserDetailsService.loadUserByUsername(username);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo("testuser");
        assertThat(result.isCredentialsNonExpired()).isTrue();

        verify(userRepository).findByUsername(username);
    }

    @Test
    @DisplayName("Deve lançar UsernameNotFoundException quando usuário não é encontrado")
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
    @DisplayName("Deve tentar busca por email quando busca por nome de usuário falha")
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
    @DisplayName("Deve lidar com entrada nula")
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
    @DisplayName("Deve lidar com entrada vazia")
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