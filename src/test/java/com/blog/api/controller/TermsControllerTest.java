package com.blog.api.controller;

import com.blog.api.dto.*;
import com.blog.api.entity.TermsAcceptance;
import com.blog.api.entity.User;
import com.blog.api.service.TermsService;
import com.blog.api.service.UserService;
import com.blog.api.config.JwtAuthenticationFilter;
import com.blog.api.config.TermsComplianceFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = TermsController.class, 
    excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, 
        classes = {JwtAuthenticationFilter.class, TermsComplianceFilter.class}))
class TermsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TermsService termsService;

    @MockBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    private UserDTO testUser;
    private TermsAcceptance testAcceptance;
    private TermsAcceptanceRequest acceptanceRequest;

    @BeforeEach
    void setUp() {
        testUser = new UserDTO(
            1L,
            "testuser",
            "test@example.com",
            User.Role.USER,
            LocalDateTime.now(),
            false,
            null,
            null,
            null,
            false
        );

        testAcceptance = TermsAcceptance.withCurrentTimestamp(
                User.newInstance()
                    .username("testuser")
                    .email("test@example.com")
                    .password("password123")
                    .role(User.Role.USER)
                    .build(),
                "v1.0"
            )
            .ipAddress("192.168.1.1")
            .userAgent("Test-Agent")
            .build();

        acceptanceRequest = new TermsAcceptanceRequest(
            "v1.0",
            true,
            true,
            true
        );
    }

    // Public endpoints tests

    @Test
    void getCurrentTermsInfo_ForAnonymousUser_ShouldReturnCurrentTerms() throws Exception {
        // Given
        when(termsService.getCurrentTermsVersion()).thenReturn("v1.0");
        when(termsService.isTermsAcceptanceRequired()).thenReturn(true);

        // When & Then
        mockMvc.perform(get("/api/v1/terms/current"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentVersion").value("v1.0"))
                .andExpect(jsonPath("$.acceptanceRequired").value(true));

        verify(termsService).getCurrentTermsVersion();
        verify(termsService).isTermsAcceptanceRequired();
    }

    @Test
    @WithMockUser(username = "testuser")
    void getCurrentTermsInfoForUser_WhenAuthenticated_ShouldReturnPersonalizedInfo() throws Exception {
        // Given
        when(userService.getUserByUsername("testuser")).thenReturn(testUser);
        when(termsService.getCurrentTermsVersion()).thenReturn("v1.0");
        when(termsService.userNeedsToAcceptTerms(1L)).thenReturn(false);
        when(termsService.getUserLatestAcceptance(1L))
                .thenReturn(Optional.of(testAcceptance));

        // When & Then
        mockMvc.perform(get("/api/v1/terms/user-status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentVersion").value("v1.0"))
                .andExpect(jsonPath("$.acceptanceRequired").value(false));

        verify(userService).getUserByUsername("testuser");
        verify(termsService).userNeedsToAcceptTerms(1L);
    }

    @Test
    @WithMockUser(username = "testuser")
    void getCurrentTermsInfoForUser_WhenNeedsAcceptance_ShouldReturnRequiredStatus() throws Exception {
        // Given
        when(userService.getUserByUsername("testuser")).thenReturn(testUser);
        when(termsService.getCurrentTermsVersion()).thenReturn("v1.0");
        when(termsService.userNeedsToAcceptTerms(1L)).thenReturn(true);

        // When & Then
        mockMvc.perform(get("/api/v1/terms/user-status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.acceptanceRequired").value(true));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void getCurrentTermsInfoForUser_AsAdmin_CanCheckOtherUsers() throws Exception {
        // Given
        when(termsService.getCurrentTermsVersion()).thenReturn("v1.0");
        when(termsService.userNeedsToAcceptTerms(2L)).thenReturn(false);
        when(termsService.getUserLatestAcceptance(2L))
                .thenReturn(Optional.of(testAcceptance));

        // When & Then
        mockMvc.perform(get("/api/v1/terms/user-status")
                .param("userId", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.acceptanceRequired").value(false));
    }

    @Test
    @WithMockUser(username = "testuser")
    void getCurrentTermsInfoForUser_AsNonAdmin_CannotCheckOtherUsers() throws Exception {
        // Given
        when(userService.getUserByUsername("testuser")).thenReturn(testUser);

        // When & Then
        mockMvc.perform(get("/api/v1/terms/user-status")
                .param("userId", "2"))
                .andExpect(status().isForbidden());
    }

    @Test
    void getCurrentTermsInfoForUser_WhenNotAuthenticated_ShouldReturn401() throws Exception {
        mockMvc.perform(get("/api/v1/terms/user-status"))
                .andExpect(status().isUnauthorized());
    }

    // Accept terms tests

    @Test
    @WithMockUser(username = "testuser")
    void acceptTerms_WithValidRequest_ShouldReturnSuccess() throws Exception {
        // Given
        when(termsService.getCurrentTermsVersion()).thenReturn("v1.0");
        when(userService.getUserByUsername("testuser")).thenReturn(testUser);
        when(termsService.acceptTerms(eq(1L), any())).thenReturn(testAcceptance);

        // When & Then
        mockMvc.perform(post("/api/v1/terms/accept")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(acceptanceRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Terms accepted successfully"))
                .andExpect(jsonPath("$.acceptance").exists());

        verify(termsService).acceptTerms(eq(1L), any());
    }

    @Test
    @WithMockUser(username = "testuser")
    void acceptTerms_WithInvalidAcceptance_ShouldReturnBadRequest() throws Exception {
        // Given
        TermsAcceptanceRequest invalidRequest = new TermsAcceptanceRequest(
            "v1.0",
            false, // Invalid - not accepting privacy policy
            true,
            true
        );

        // When & Then
        mockMvc.perform(post("/api/v1/terms/accept")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("You must accept all terms, privacy policy, and cookie policy to continue"));
    }

    @Test
    @WithMockUser(username = "testuser")
    void acceptTerms_WithWrongVersion_ShouldReturnBadRequest() throws Exception {
        // Given
        when(termsService.getCurrentTermsVersion()).thenReturn("v2.0");
        TermsAcceptanceRequest wrongVersionRequest = new TermsAcceptanceRequest(
            "v1.0", // Wrong version
            true,
            true,
            true
        );

        // When & Then
        mockMvc.perform(post("/api/v1/terms/accept")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(wrongVersionRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Terms version mismatch. Please accept the current terms version: v2.0"));
    }

    @Test
    @WithMockUser(username = "testuser")
    void acceptTerms_WhenAlreadyAccepted_ShouldReturnConflict() throws Exception {
        // Given
        when(termsService.getCurrentTermsVersion()).thenReturn("v1.0");
        when(userService.getUserByUsername("testuser")).thenReturn(testUser);
        when(termsService.acceptTerms(eq(1L), any()))
                .thenThrow(new IllegalStateException("Already accepted"));

        // When & Then
        mockMvc.perform(post("/api/v1/terms/accept")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(acceptanceRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Terms already accepted for this version"));
    }

    @Test
    void acceptTerms_WhenNotAuthenticated_ShouldReturn401() throws Exception {
        mockMvc.perform(post("/api/v1/terms/accept")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(acceptanceRequest)))
                .andExpect(status().isUnauthorized());
    }

    // History tests

    @Test
    @WithMockUser(username = "testuser")
    void getUserTermsHistory_ShouldReturnHistory() throws Exception {
        // Given
        when(userService.getUserByUsername("testuser")).thenReturn(testUser);
        when(termsService.getUserTermsHistory(1L))
                .thenReturn(Arrays.asList(testAcceptance));

        // When & Then
        mockMvc.perform(get("/api/v1/terms/history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].termsVersion").value("v1.0"));

        verify(termsService).getUserTermsHistory(1L);
    }

    @Test
    void getUserTermsHistory_WhenNotAuthenticated_ShouldReturn401() throws Exception {
        mockMvc.perform(get("/api/v1/terms/history"))
                .andExpect(status().isUnauthorized());
    }

    // Admin endpoints tests

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void getTermsStatistics_AsAdmin_ShouldReturnStatistics() throws Exception {
        // Given
        TermsService.AcceptanceStatistics stats = new TermsService.AcceptanceStatistics(
            "v1.0", 100L, 50L, LocalDateTime.now(), LocalDateTime.now()
        );
        when(termsService.getCurrentTermsVersion()).thenReturn("v1.0");
        when(termsService.getVersionStatistics("v1.0")).thenReturn(stats);
        when(termsService.getMonthlyStatistics()).thenReturn(Arrays.asList());

        // When & Then
        mockMvc.perform(get("/api/v1/terms/admin/statistics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.termsVersion").value("v1.0"))
                .andExpect(jsonPath("$.totalAcceptances").value(100))
                .andExpect(jsonPath("$.uniqueUsers").value(50));
    }

    @Test
    @WithMockUser(username = "user", roles = "USER")
    void getTermsStatistics_AsUser_ShouldReturn403() throws Exception {
        // Should not call service methods due to security
        mockMvc.perform(get("/api/v1/terms/admin/statistics"))
                .andExpect(status().isForbidden());
                
        // Verify service methods were not called due to security
        verify(termsService, never()).getCurrentTermsVersion();
        verify(termsService, never()).getVersionStatistics(any());
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void getNonCompliantUsers_AsAdmin_ShouldReturnUsers() throws Exception {
        // Given
        Page<User> users = new PageImpl<>(Arrays.asList());
        when(termsService.getUsersWithoutLatestTerms(any(PageRequest.class)))
                .thenReturn(users);

        // When & Then
        mockMvc.perform(get("/api/v1/terms/admin/non-compliant-users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void getVersionAcceptances_AsAdmin_ShouldReturnAcceptances() throws Exception {
        // Given
        when(termsService.getAcceptancesForVersion("v1.0"))
                .thenReturn(Arrays.asList(testAcceptance));

        // When & Then
        mockMvc.perform(get("/api/v1/terms/admin/version/v1.0/acceptances"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].termsVersion").value("v1.0"));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void getUserTermsHistoryAdmin_AsAdmin_ShouldReturnHistory() throws Exception {
        // Given
        when(termsService.getUserTermsHistory(1L))
                .thenReturn(Arrays.asList(testAcceptance));

        // When & Then
        mockMvc.perform(get("/api/v1/terms/admin/user/1/history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].termsVersion").value("v1.0"));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void cleanupOldRecords_AsAdmin_ShouldReturnSuccess() throws Exception {
        // Given
        when(termsService.cleanupOldAcceptances()).thenReturn(5);

        // When & Then
        mockMvc.perform(post("/api/v1/terms/admin/cleanup").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string("Cleanup completed. Deleted 5 old terms acceptance records."));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void forceReAcceptance_AsAdmin_ShouldReturnSuccess() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/v1/terms/admin/force-reacceptance").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string("Force re-acceptance initiated. All users will need to accept terms again."));

        verify(termsService).forceReAcceptanceForAllUsers();
    }

    @Test
    @WithMockUser(username = "user", roles = "USER")
    void adminEndpoints_AsUser_ShouldReturn403() throws Exception {
        mockMvc.perform(get("/api/v1/terms/admin/statistics"))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/v1/terms/admin/non-compliant-users"))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/v1/terms/admin/cleanup").with(csrf()))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/v1/terms/admin/force-reacceptance").with(csrf()))
                .andExpect(status().isForbidden());
                
        // Verify that service methods should not be called due to security
        verify(termsService, never()).getCurrentTermsVersion();
        verify(termsService, never()).getVersionStatistics(any());
        verify(termsService, never()).getUsersWithoutLatestTerms(any());
        verify(termsService, never()).cleanupOldAcceptances();
        verify(termsService, never()).forceReAcceptanceForAllUsers();
    }
}