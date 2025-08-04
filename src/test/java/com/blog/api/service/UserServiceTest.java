package com.blog.api.service;

import com.blog.api.dto.UserDTO;
import com.blog.api.entity.User;
import com.blog.api.exception.ResourceNotFoundException;
import com.blog.api.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("User Service Tests")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private UserDTO expectedUserDTO;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        testUser = User.of("testuser", "test@example.com", "ValidPassword123!")
                .role(User.Role.USER)
                .emailVerified(true)
                .build();
        testUser.setId(1L);
        testUser.setCreatedAt(LocalDateTime.now());

        expectedUserDTO = UserDTO.fromEntity(testUser);
        pageable = PageRequest.of(0, 10);
    }

    @Test
    @DisplayName("Deve retornar uma página de UserDTOs quando buscar todos os usuários")
    void getAllUsers_ShouldReturnPageOfUserDTOs() {
        // Arrange
        Page<User> userPage = new PageImpl<>(Arrays.asList(testUser), pageable, 1);
        when(userRepository.findAll(pageable)).thenReturn(userPage);

        // Act
        Page<UserDTO> result = userService.getAllUsers(pageable);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).username()).isEqualTo("testuser");
        assertThat(result.getContent().get(0).email()).isEqualTo("test@example.com");
        verify(userRepository).findAll(pageable);
    }

    @Test
    @DisplayName("Deve retornar uma página vazia quando não há usuários")
    void getAllUsers_ShouldReturnEmptyPageWhenNoUsers() {
        // Arrange
        Page<User> emptyPage = new PageImpl<>(Arrays.asList(), pageable, 0);
        when(userRepository.findAll(pageable)).thenReturn(emptyPage);

        // Act
        Page<UserDTO> result = userService.getAllUsers(pageable);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isEqualTo(0);
        verify(userRepository).findAll(pageable);
    }

    @Test
    @DisplayName("Deve retornar UserDTO quando buscar usuário por ID existente")
    void getUserById_ShouldReturnUserDTO_WhenUserExists() {
        // Arrange
        Long userId = 1L;
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));

        // Act
        UserDTO result = userService.getUserById(userId);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(userId);
        assertThat(result.username()).isEqualTo("testuser");
        assertThat(result.email()).isEqualTo("test@example.com");
        verify(userRepository).findById(userId);
    }

    @Test
    @DisplayName("Deve lançar ResourceNotFoundException quando buscar usuário por ID inexistente")
    void getUserById_ShouldThrowResourceNotFoundException_WhenUserNotExists() {
        // Arrange
        Long userId = 999L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> userService.getUserById(userId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User")
                .hasMessageContaining("id")
                .hasMessageContaining("999");
        
        verify(userRepository).findById(userId);
    }

    @Test
    @DisplayName("Deve retornar UserDTO quando buscar usuário por nome de usuário existente")
    void getUserByUsername_ShouldReturnUserDTO_WhenUserExists() {
        // Arrange
        String username = "testuser";
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(testUser));

        // Act
        UserDTO result = userService.getUserByUsername(username);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.username()).isEqualTo(username);
        assertThat(result.email()).isEqualTo("test@example.com");
        verify(userRepository).findByUsername(username);
    }

    @Test
    @DisplayName("Deve lançar ResourceNotFoundException quando buscar usuário por nome inexistente")
    void getUserByUsername_ShouldThrowResourceNotFoundException_WhenUserNotExists() {
        // Arrange
        String username = "nonexistentuser";
        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> userService.getUserByUsername(username))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User")
                .hasMessageContaining("username")
                .hasMessageContaining("nonexistentuser");
        
        verify(userRepository).findByUsername(username);
    }

    // Método getUserByEmail removido pois não existe no UserService real

    // Método updateUser removido pois não existe no UserService real

    @Test
    @DisplayName("Deve deletar usuário quando ele existe")
    void deleteUser_ShouldDeleteUser_WhenUserExists() {
        // Arrange
        Long userId = 1L;
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));

        // Act
        userService.deleteUser(userId);

        // Assert
        verify(userRepository).findById(userId);
        verify(userRepository).delete(testUser);
    }

    @Test
    @DisplayName("Deve lançar ResourceNotFoundException quando tentar deletar usuário inexistente")
    void deleteUser_ShouldThrowResourceNotFoundException_WhenUserNotExists() {
        // Arrange
        Long userId = 999L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> userService.deleteUser(userId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User")
                .hasMessageContaining("id")
                .hasMessageContaining("999");
        
        verify(userRepository).findById(userId);
        verify(userRepository, never()).delete(any(User.class));
    }

    // Métodos existsByUsername e existsByEmail removidos pois não existem no UserService real
    // O UserService tem apenas: getAllUsers, getUserById, getUserByUsername, deleteUser
}