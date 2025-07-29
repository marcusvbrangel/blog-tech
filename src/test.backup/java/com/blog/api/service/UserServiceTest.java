package com.blog.api.service;

import com.blog.api.dto.UserDTO;
import com.blog.api.entity.User;
import com.blog.api.exception.ResourceNotFoundException;
import com.blog.api.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setRole(User.Role.USER);
        testUser.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void getUserById_WhenUserExists_ShouldReturnUserDTO() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        UserDTO result = userService.getUserById(1L);

        assertNotNull(result);
        assertEquals(testUser.getId(), result.getId());
        assertEquals(testUser.getUsername(), result.getUsername());
        assertEquals(testUser.getEmail(), result.getEmail());
    }

    @Test
    void getUserById_WhenUserNotExists_ShouldThrowResourceNotFoundException() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.getUserById(1L));
    }

    @Test
    void getUserByUsername_WhenUserExists_ShouldReturnUserDTO() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        UserDTO result = userService.getUserByUsername("testuser");

        assertNotNull(result);
        assertEquals(testUser.getUsername(), result.getUsername());
    }

    @Test
    void getUserByUsername_WhenUserNotExists_ShouldThrowResourceNotFoundException() {
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.getUserByUsername("nonexistent"));
    }
}