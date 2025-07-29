package com.blog.api.config;

import com.blog.api.service.AuthService;
import com.blog.api.util.JwtUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureWebMvc
class SecurityConfigTest {

    @Autowired
    private WebApplicationContext context;

    @MockBean
    private AuthService authService;

    @MockBean
    private JwtUtil jwtUtil;

    private MockMvc mockMvc;

    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @Test
    void publicEndpoints_ShouldBeAccessibleWithoutAuthentication() throws Exception {
        setUp();
        
        // Test public endpoints
        mockMvc.perform(get("/api/v1/posts"))
                .andExpect(status().isOk());
        
        mockMvc.perform(get("/api/v1/posts/1"))
                .andExpect(status().isNotFound()); // 404 because post doesn't exist, but no auth error
        
        mockMvc.perform(get("/api/v1/categories"))
                .andExpect(status().isOk());
        
        mockMvc.perform(get("/api/v1/comments/post/1"))
                .andExpect(status().isOk());
    }

    @Test
    void authEndpoints_ShouldBeAccessibleWithoutAuthentication() throws Exception {
        setUp();
        
        mockMvc.perform(post("/api/v1/auth/register"))
                .andExpect(status().isBadRequest()); // Bad request due to missing body, not auth error
        
        mockMvc.perform(post("/api/v1/auth/login"))
                .andExpect(status().isBadRequest()); // Bad request due to missing body, not auth error
    }

    @Test
    void swaggerEndpoints_ShouldBeAccessibleWithoutAuthentication() throws Exception {
        setUp();
        
        mockMvc.perform(get("/swagger-ui.html"))
                .andExpect(status().isFound()); // Redirect to swagger-ui/index.html
        
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk());
        
        mockMvc.perform(get("/swagger-ui/index.html"))
                .andExpect(status().isOk());
    }

    @Test
    void protectedEndpoints_ShouldRequireAuthentication() throws Exception {
        setUp();
        
        // Test protected endpoints
        mockMvc.perform(post("/api/v1/posts"))
                .andExpect(status().isUnauthorized());
        
        mockMvc.perform(post("/api/v1/categories"))
                .andExpect(status().isUnauthorized());
        
        mockMvc.perform(post("/api/v1/comments"))
                .andExpect(status().isUnauthorized());
        
        mockMvc.perform(get("/api/v1/users"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void adminEndpoints_ShouldRequireAuthentication() throws Exception {
        setUp();
        
        mockMvc.perform(post("/api/v1/categories"))
                .andExpect(status().isUnauthorized());
        
        mockMvc.perform(get("/api/v1/users"))
                .andExpect(status().isUnauthorized());
    }
}