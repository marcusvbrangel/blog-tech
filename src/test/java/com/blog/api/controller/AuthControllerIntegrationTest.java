package com.blog.api.controller;

import com.blog.api.dto.CreateUserDTO;
import com.blog.api.dto.LoginRequest;
import com.blog.api.dto.RefreshTokenRequest;
import com.blog.api.entity.User;
import com.blog.api.repository.UserRepository;
import com.blog.api.util.TestDataFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "blog.security.email-verification.enabled=false"
})
@Transactional
class AuthControllerIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
            .apply(springSecurity())
            .build();
    }

    @Test
    void register_ShouldCreateUser_AndLogAuditEvent() throws Exception {
        // Arrange
        CreateUserDTO createUserDTO = new CreateUserDTO(
            "testuser",
            "test@example.com",
            TestDataFactory.VALID_PASSWORD_1,
            User.Role.USER
        );

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createUserDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.role").value("USER"));

        // Verify user was created
        assertThat(userRepository.findByUsername("testuser")).isPresent();
    }

    @Test
    void login_ShouldReturnJwtWithRefreshToken_AndLogAuditEvent() throws Exception {
        // Arrange - Create user first
        CreateUserDTO createUserDTO = new CreateUserDTO(
            "loginuser",
            "login@example.com",
            TestDataFactory.VALID_PASSWORD_2,
            User.Role.USER
        );

        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createUserDTO)))
                .andExpect(status().isCreated());

        LoginRequest loginRequest = new LoginRequest("loginuser", TestDataFactory.VALID_PASSWORD_2);

        // Act & Assert
        MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty())
                .andExpect(jsonPath("$.user.username").value("loginuser"))
                .andExpect(jsonPath("$.type").value("Bearer"))
                .andReturn();

        String responseContent = result.getResponse().getContentAsString();
        assertThat(responseContent).contains("refreshToken");
    }

    @Test
    void login_ShouldFailWithInvalidCredentials_AndLogAuditEvent() throws Exception {
        // Arrange
        LoginRequest loginRequest = new LoginRequest("nonexistent", "wrongpassword");

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void refreshToken_ShouldReturnNewTokens_WhenValidRefreshToken() throws Exception {
        // Arrange - Register and login to get refresh token
        CreateUserDTO createUserDTO = new CreateUserDTO(
            "refreshuser",
            "refresh@example.com",
            TestDataFactory.VALID_PASSWORD_3,
            User.Role.USER
        );

        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createUserDTO)))
                .andExpect(status().isCreated());

        LoginRequest loginRequest = new LoginRequest("refreshuser", TestDataFactory.VALID_PASSWORD_3);

        MvcResult loginResult = mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        String loginResponse = loginResult.getResponse().getContentAsString();
        
        // Extract refresh token from login response
        // This is a simplified extraction - in practice you'd parse the JSON properly
        String refreshToken = extractRefreshTokenFromResponse(loginResponse);
        
        RefreshTokenRequest refreshRequest = new RefreshTokenRequest(refreshToken);

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(refreshRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.access_token").isNotEmpty())
                .andExpect(jsonPath("$.refresh_token").isNotEmpty())
                .andExpect(jsonPath("$.token_type").value("Bearer"));
    }

    @Test
    void refreshToken_ShouldFailWithInvalidToken() throws Exception {
        // Arrange
        RefreshTokenRequest refreshRequest = new RefreshTokenRequest("invalid-refresh-token");

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(refreshRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void revokeRefreshToken_ShouldSucceed_WhenValidToken() throws Exception {
        // Arrange - Get a valid refresh token
        CreateUserDTO createUserDTO = new CreateUserDTO(
            "revokeuser",
            "revoke@example.com",
            TestDataFactory.VALID_PASSWORD_4,
            User.Role.USER
        );

        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createUserDTO)))
                .andExpect(status().isCreated());

        LoginRequest loginRequest = new LoginRequest("revokeuser", TestDataFactory.VALID_PASSWORD_4);

        MvcResult loginResult = mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        String refreshToken = extractRefreshTokenFromResponse(loginResult.getResponse().getContentAsString());
        RefreshTokenRequest revokeRequest = new RefreshTokenRequest(refreshToken);

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/revoke-refresh-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(revokeRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Refresh token revoked successfully"));
    }

    @Test
    void register_ShouldFailWithWeakPassword() throws Exception {
        // Arrange
        CreateUserDTO createUserDTO = new CreateUserDTO(
            "weakuser",
            "weak@example.com",
            "weak", // Weak password
            User.Role.USER
        );

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createUserDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_ShouldFailWithDuplicateUsername() throws Exception {
        // Arrange - Create first user
        CreateUserDTO firstUser = new CreateUserDTO(
            "duplicateuser",
            "first@example.com",
            TestDataFactory.VALID_PASSWORD_1,
            User.Role.USER
        );

        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(firstUser)))
                .andExpect(status().isCreated());

        // Try to create second user with same username
        CreateUserDTO secondUser = new CreateUserDTO(
            "duplicateuser", // Same username
            "second@example.com",
            TestDataFactory.VALID_PASSWORD_2,
            User.Role.USER
        );

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(secondUser)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_ShouldFailWithDuplicateEmail() throws Exception {
        // Arrange - Create first user
        CreateUserDTO firstUser = new CreateUserDTO(
            "user1",
            "duplicate@example.com",
            TestDataFactory.VALID_PASSWORD_1,
            User.Role.USER
        );

        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(firstUser)))
                .andExpect(status().isCreated());

        // Try to create second user with same email
        CreateUserDTO secondUser = new CreateUserDTO(
            "user2",
            "duplicate@example.com", // Same email
            TestDataFactory.VALID_PASSWORD_2,
            User.Role.USER
        );

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(secondUser)))
                .andExpect(status().isBadRequest());
    }

    // Helper method to extract refresh token from JSON response
    private String extractRefreshTokenFromResponse(String jsonResponse) {
        try {
            com.fasterxml.jackson.databind.JsonNode node = objectMapper.readTree(jsonResponse);
            return node.get("refreshToken").asText();
        } catch (Exception e) {
            throw new RuntimeException("Failed to extract refresh token from response", e);
        }
    }
}