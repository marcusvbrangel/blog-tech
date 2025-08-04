package com.blog.api.integration;

import com.blog.api.dto.*;
import com.blog.api.entity.User;
import com.blog.api.entity.VerificationToken;
import com.blog.api.repository.UserRepository;
import com.blog.api.repository.VerificationTokenRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class DebugSpecificFailuresTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VerificationTokenRepository tokenRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        tokenRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void debugLoginAfterEmailVerification() throws Exception {
        System.out.println("=== DEBUG LOGIN AFTER EMAIL VERIFICATION ===");
        
        // Step 1: Register user
        CreateUserDTO createUserDTO = new CreateUserDTO(
            "testuser", "testuser@example.com", "TestPass123!", User.Role.USER);

        MvcResult registerResult = mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createUserDTO)))
                .andExpect(status().isCreated())
                .andReturn();
        
        System.out.println("Register response: " + registerResult.getResponse().getContentAsString());

        // Get the user
        Optional<User> registeredUser = userRepository.findByEmail("testuser@example.com");
        assertTrue(registeredUser.isPresent());
        
        System.out.println("User password hash: " + registeredUser.get().getPassword());
        System.out.println("User verified before: " + registeredUser.get().isEmailVerified());

        // Get token
        Optional<VerificationToken> token = tokenRepository
            .findByUserAndTokenType(registeredUser.get(), VerificationToken.TokenType.EMAIL_VERIFICATION)
            .stream().findFirst();
        assertTrue(token.isPresent());

        // Step 2: Verify email
        String tokenValue = token.get().getToken();
        MvcResult verifyResult = mockMvc.perform(get("/api/v1/auth/verify-email")
                .param("token", tokenValue))
                .andExpect(status().isOk())
                .andReturn();
        
        System.out.println("Verify response: " + verifyResult.getResponse().getContentAsString());

        // Check user after verification
        User verifiedUser = userRepository.findByEmail("testuser@example.com").orElseThrow();
        System.out.println("User verified after: " + verifiedUser.isEmailVerified());
        System.out.println("User password hash after: " + verifiedUser.getPassword());

        // Step 3: Try to login
        LoginRequest loginRequest = new LoginRequest("testuser@example.com", "TestPass123!");
        System.out.println("Login request: " + objectMapper.writeValueAsString(loginRequest));
        
        MvcResult loginResult = mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andReturn();
        
        System.out.println("Login status: " + loginResult.getResponse().getStatus());
        System.out.println("Login response: " + loginResult.getResponse().getContentAsString());
        
        if (loginResult.getResponse().getStatus() != 200) {
            System.out.println("LOGIN FAILED!");
        }
    }

    @Test
    void debugPasswordResetWithNonExistentEmail() throws Exception {
        System.out.println("=== DEBUG PASSWORD RESET WITH NON-EXISTENT EMAIL ===");
        
        PasswordResetRequest resetRequest = new PasswordResetRequest("nonexistent@example.com");
        System.out.println("Reset request: " + objectMapper.writeValueAsString(resetRequest));
        
        MvcResult result = mockMvc.perform(post("/api/v1/auth/forgot-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(resetRequest)))
                .andReturn();
        
        System.out.println("Reset status: " + result.getResponse().getStatus());
        System.out.println("Reset response: " + result.getResponse().getContentAsString());
        
        if (result.getResponse().getStatus() != 200) {
            System.out.println("PASSWORD RESET FAILED!");
        }
    }
}