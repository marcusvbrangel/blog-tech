package com.blog.api.controller;

import com.blog.api.config.JwtAuthenticationFilter;
import com.blog.api.config.TermsComplianceFilter;
import com.blog.api.dto.TermsAcceptanceRequest;
import com.blog.api.dto.UserDTO;
import com.blog.api.entity.TermsAcceptance;
import com.blog.api.entity.User;
import com.blog.api.service.TermsService;
import com.blog.api.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = TermsController.class, 
    excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, 
        classes = {JwtAuthenticationFilter.class, TermsComplianceFilter.class}))
@DisplayName("Terms Controller Tests")
class TermsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TermsService termsService;

    @MockBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    private UserDTO sampleUserDTO;
    private TermsAcceptance sampleAcceptance;
    private TermsAcceptanceRequest acceptanceRequest;

    @BeforeEach
    void setUp() {
        sampleUserDTO = new UserDTO(
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

        User testUser = User.of("testuser", "test@example.com", "TestPass123!")
                .role(User.Role.USER)
                .termsAcceptedVersion("1.0")
                .build();
        testUser.setId(1L);

        sampleAcceptance = TermsAcceptance.withCurrentTimestamp(testUser, "1.0")
                .ipAddress("192.168.1.1")
                .userAgent("Mozilla/5.0")
                .build();
        sampleAcceptance.setId(1L);

        acceptanceRequest = new TermsAcceptanceRequest(
                "1.0",
                true,
                true,
                true
        );
    }

    @Test
    @DisplayName("Should return current terms info for anonymous users")
    void getCurrentTermsInfo_ShouldReturnTermsInfo_WhenAnonymousUser() throws Exception {
        // Given
        when(termsService.getCurrentTermsVersion()).thenReturn("1.0");
        when(termsService.isTermsAcceptanceRequired()).thenReturn(true);

        // When & Then
        mockMvc.perform(get("/api/v1/terms/current"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.version").value("1.0"))
                .andExpect(jsonPath("$.acceptanceRequired").value(true));

        verify(termsService).getCurrentTermsVersion();
        verify(termsService).isTermsAcceptanceRequired();
    }

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    @DisplayName("Should return user terms status for authenticated users")
    void getUserTermsStatus_ShouldReturnUserStatus_WhenAuthenticated() throws Exception {
        // Given
        when(userService.getUserByUsername("testuser")).thenReturn(sampleUserDTO);
        when(termsService.getCurrentTermsVersion()).thenReturn("1.0");
        when(termsService.userNeedsToAcceptTerms(1L)).thenReturn(false);
        when(termsService.getUserLatestAcceptance(1L)).thenReturn(Optional.of(sampleAcceptance));

        // When & Then
        mockMvc.perform(get("/api/v1/terms/user-status"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.version").value("1.0"))
                .andExpect(jsonPath("$.acceptanceRequired").value(false));

        verify(userService).getUserByUsername("testuser");
        verify(termsService).getCurrentTermsVersion();
        verify(termsService).userNeedsToAcceptTerms(1L);
    }

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    @DisplayName("Should accept terms successfully when valid request")
    void acceptTerms_ShouldAcceptTerms_WhenValidRequest() throws Exception {
        // Given
        when(termsService.getCurrentTermsVersion()).thenReturn("1.0");
        when(userService.getUserByUsername("testuser")).thenReturn(sampleUserDTO);
        when(termsService.acceptTerms(eq(1L), any(jakarta.servlet.http.HttpServletRequest.class))).thenReturn(sampleAcceptance);

        // When & Then
        mockMvc.perform(post("/api/v1/terms/accept")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(acceptanceRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true));

        verify(termsService).getCurrentTermsVersion();
        verify(userService).getUserByUsername("testuser");
        verify(termsService).acceptTerms(eq(1L), any(jakarta.servlet.http.HttpServletRequest.class));
    }

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    @DisplayName("Should return bad request when invalid acceptance request")
    void acceptTerms_ShouldReturnBadRequest_WhenInvalidRequest() throws Exception {
        // Given
        TermsAcceptanceRequest invalidRequest = new TermsAcceptanceRequest(
                "1.0",
                false, // Invalid - must be true
                true,
                true
        );
        when(termsService.getCurrentTermsVersion()).thenReturn("1.0");

        // When & Then
        mockMvc.perform(post("/api/v1/terms/accept")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());

        verify(termsService).getCurrentTermsVersion();
        verify(termsService, never()).acceptTerms(any(Long.class), any(jakarta.servlet.http.HttpServletRequest.class));
    }

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    @DisplayName("Should return user terms history")
    void getUserTermsHistory_ShouldReturnHistory_WhenAuthenticated() throws Exception {
        // Given
        List<TermsAcceptance> history = Arrays.asList(sampleAcceptance);
        when(userService.getUserByUsername("testuser")).thenReturn(sampleUserDTO);
        when(termsService.getUserTermsHistory(1L)).thenReturn(history);

        // When & Then
        mockMvc.perform(get("/api/v1/terms/history"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].termsVersion").value("1.0"));

        verify(userService).getUserByUsername("testuser");
        verify(termsService).getUserTermsHistory(1L);
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    @DisplayName("Should return terms statistics for admin")
    void getTermsStatistics_ShouldReturnStatistics_WhenAdmin() throws Exception {
        // Given
        TermsService.AcceptanceStatistics stats = new TermsService.AcceptanceStatistics(
                "1.0", 100, 95, LocalDateTime.now().minusDays(30), LocalDateTime.now()
        );
        List<TermsService.MonthlyStatistics> monthlyStats = Arrays.asList(
                new TermsService.MonthlyStatistics(2024, 1, 50)
        );
        
        when(termsService.getCurrentTermsVersion()).thenReturn("1.0");
        when(termsService.getVersionStatistics("1.0")).thenReturn(stats);
        when(termsService.getMonthlyStatistics()).thenReturn(monthlyStats);

        // When & Then
        mockMvc.perform(get("/api/v1/terms/admin/statistics"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.version").value("1.0"))
                .andExpect(jsonPath("$.totalAcceptances").value(100));

        verify(termsService).getCurrentTermsVersion();
        verify(termsService).getVersionStatistics("1.0");
        verify(termsService).getMonthlyStatistics();
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    @DisplayName("Should return non-compliant users for admin")
    void getNonCompliantUsers_ShouldReturnUsers_WhenAdmin() throws Exception {
        // Given
        User testUser = User.of("user2", "user2@example.com", "TestPass123!")
                .role(User.Role.USER)
                .build();
        testUser.setId(2L);
        
        Page<User> usersPage = new PageImpl<>(Arrays.asList(testUser));
        when(termsService.getUsersWithoutLatestTerms(any())).thenReturn(usersPage);

        // When & Then
        mockMvc.perform(get("/api/v1/terms/admin/non-compliant-users"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].username").value("user2"));

        verify(termsService).getUsersWithoutLatestTerms(any());
    }

    @Test
    @WithMockUser(username = "user", roles = "USER")
    @DisplayName("Should return forbidden when non-admin tries to access admin endpoints")
    void getTermsStatistics_ShouldReturnForbidden_WhenNotAdmin() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/terms/admin/statistics"))
                .andExpect(status().isForbidden());

        verify(termsService, never()).getVersionStatistics(any());
    }

    @Test
    @DisplayName("Should return unauthorized when not authenticated for protected endpoints")
    void acceptTerms_ShouldReturnUnauthorized_WhenNotAuthenticated() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/v1/terms/accept")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(acceptanceRequest)))
                .andExpect(status().isUnauthorized());

        verify(termsService, never()).acceptTerms(any(Long.class), any(jakarta.servlet.http.HttpServletRequest.class));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    @DisplayName("Should initiate cleanup for admin")
    void cleanupOldRecords_ShouldInitiateCleanup_WhenAdmin() throws Exception {
        // Given
        when(termsService.cleanupOldAcceptances()).thenReturn(25);

        // When & Then
        mockMvc.perform(post("/api/v1/terms/admin/cleanup")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Deleted 25 old terms acceptance records")));

        verify(termsService).cleanupOldAcceptances();
    }
}