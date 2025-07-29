package com.blog.api.service;

import com.blog.api.dto.*;
import com.blog.api.entity.User;
import com.blog.api.exception.BadRequestException;
import com.blog.api.repository.UserRepository;
import com.blog.api.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private CustomUserDetailsService userDetailsService;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthService authService;

    private CreateUserDTO createUserDTO;
    private User savedUser;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        createUserDTO = new CreateUserDTO();
        createUserDTO.setUsername("testuser");
        createUserDTO.setEmail("test@example.com");
        createUserDTO.setPassword("password123");
        createUserDTO.setRole(User.Role.USER);

        savedUser = new User();
        savedUser.setId(1L);
        savedUser.setUsername("testuser");
        savedUser.setEmail("test@example.com");
        savedUser.setPassword("encodedPassword");
        savedUser.setRole(User.Role.USER);
        savedUser.setCreatedAt(LocalDateTime.now());

        loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("password123");
    }

    @Test
    void register_WhenValidUser_ShouldReturnUserDTO() {
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        UserDTO result = authService.register(createUserDTO);

        assertNotNull(result);
        assertEquals(savedUser.getId(), result.getId());
        assertEquals(savedUser.getUsername(), result.getUsername());
        assertEquals(savedUser.getEmail(), result.getEmail());
        verify(userRepository).save(any(User.class));
        verify(passwordEncoder).encode("password123");
    }

    @Test
    void register_WhenUsernameExists_ShouldThrowBadRequestException() {
        when(userRepository.existsByUsername("testuser")).thenReturn(true);

        BadRequestException exception = assertThrows(
            BadRequestException.class, 
            () -> authService.register(createUserDTO)
        );

        assertEquals("Username already exists", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void register_WhenEmailExists_ShouldThrowBadRequestException() {
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

        BadRequestException exception = assertThrows(
            BadRequestException.class, 
            () -> authService.register(createUserDTO)
        );

        assertEquals("Email already exists", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void login_WhenValidCredentials_ShouldReturnJwtResponse() {
        UserDetails userDetails = mock(UserDetails.class);
        when(userDetails.getUsername()).thenReturn("testuser");
        
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenReturn(null);
        when(userDetailsService.loadUserByUsername("testuser")).thenReturn(userDetails);
        when(jwtUtil.generateToken(userDetails)).thenReturn("jwt-token");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(savedUser));

        JwtResponse result = authService.login(loginRequest);

        assertNotNull(result);
        assertEquals("jwt-token", result.getToken());
        assertEquals("Bearer", result.getType());
        assertNotNull(result.getUser());
        assertEquals("testuser", result.getUser().getUsername());
    }

    @Test
    void login_WhenInvalidCredentials_ShouldThrowBadCredentialsException() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenThrow(new BadCredentialsException("Invalid credentials"));

        assertThrows(
            BadCredentialsException.class,
            () -> authService.login(loginRequest)
        );

        verify(jwtUtil, never()).generateToken(any(UserDetails.class));
    }

    @Test
    void login_WhenUserNotFound_ShouldThrowBadRequestException() {
        UserDetails userDetails = mock(UserDetails.class);
        when(userDetails.getUsername()).thenReturn("testuser");
        
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenReturn(null);
        when(userDetailsService.loadUserByUsername("testuser")).thenReturn(userDetails);
        when(jwtUtil.generateToken(userDetails)).thenReturn("jwt-token");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.empty());

        BadRequestException exception = assertThrows(
            BadRequestException.class,
            () -> authService.login(loginRequest)
        );

        assertEquals("User not found", exception.getMessage());
    }
}