package com.blog.api.config;

import com.blog.api.dto.TermsComplianceResponse;
import com.blog.api.dto.UserDTO;
import com.blog.api.entity.User;
import com.blog.api.service.TermsService;
import com.blog.api.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TermsComplianceFilterTest {

    @Mock
    private TermsService termsService;

    @Mock
    private UserService userService;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @Mock
    private Authentication authentication;

    @Mock
    private SecurityContext securityContext;

    @InjectMocks
    private TermsComplianceFilter termsComplianceFilter;

    private UserDTO testUser;
    private StringWriter responseWriter;

    @BeforeEach
    void setUp() {
        testUser = new UserDTO(
            1L,
            "testuser",
            "test@example.com",
            User.Role.USER,
            LocalDateTime.now(),
            true,
            LocalDateTime.now(),
            null,
            "v1.0",
            true
        );

        responseWriter = new StringWriter();
        
        // Set filter configuration via reflection
        ReflectionTestUtils.setField(termsComplianceFilter, "termsEnabled", true);
        ReflectionTestUtils.setField(termsComplianceFilter, "enforceCompliance", true);
        
        // Setup SecurityContext
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void doFilterInternal_WhenTermsDisabled_ShouldContinueFilterChain() throws Exception {
        // Given
        ReflectionTestUtils.setField(termsComplianceFilter, "termsEnabled", false);

        // When
        termsComplianceFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(request, response);
        verify(termsService, never()).userNeedsToAcceptTerms(any(Long.class));
    }

    @Test
    void doFilterInternal_WhenComplianceNotEnforced_ShouldContinueFilterChain() throws Exception {
        // Given
        ReflectionTestUtils.setField(termsComplianceFilter, "enforceCompliance", false);

        // When
        termsComplianceFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(request, response);
        verify(termsService, never()).userNeedsToAcceptTerms(any(Long.class));
    }

    @Test
    void doFilterInternal_WhenExcludedPath_ShouldContinueFilterChain() throws Exception {
        // Given
        when(request.getRequestURI()).thenReturn("/api/v1/terms/current");
        when(request.getMethod()).thenReturn("GET");

        // When
        termsComplianceFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(request, response);
        verify(termsService, never()).userNeedsToAcceptTerms(any(Long.class));
    }

    @Test
    void doFilterInternal_WhenExcludedMethod_ShouldContinueFilterChain() throws Exception {
        // Given
        when(request.getRequestURI()).thenReturn("/api/v1/posts");
        when(request.getMethod()).thenReturn("OPTIONS");

        // When
        termsComplianceFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(request, response);
        verify(termsService, never()).userNeedsToAcceptTerms(any(Long.class));
    }

    @Test
    void doFilterInternal_WhenStaticResource_ShouldContinueFilterChain() throws Exception {
        // Given
        when(request.getRequestURI()).thenReturn("/static/css/styles.css");
        when(request.getMethod()).thenReturn("GET");

        // When
        termsComplianceFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(request, response);
        verify(termsService, never()).userNeedsToAcceptTerms(any(Long.class));
    }

    @Test
    void doFilterInternal_WhenNotAuthenticated_ShouldContinueFilterChain() throws Exception {
        // Given
        when(request.getRequestURI()).thenReturn("/api/v1/posts");
        when(request.getMethod()).thenReturn("GET");
        when(securityContext.getAuthentication()).thenReturn(null);

        // When
        termsComplianceFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(request, response);
        verify(userService, never()).getUserByUsername(anyString());
    }

    @Test
    void doFilterInternal_WhenAnonymousUser_ShouldContinueFilterChain() throws Exception {
        // Given
        when(request.getRequestURI()).thenReturn("/api/v1/posts");
        when(request.getMethod()).thenReturn("GET");
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("anonymousUser");

        // When
        termsComplianceFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(request, response);
        verify(userService, never()).getUserByUsername(anyString());
    }

    @Test
    void doFilterInternal_WhenUserAcceptedTerms_ShouldContinueFilterChain() throws Exception {
        // Given
        when(request.getRequestURI()).thenReturn("/api/v1/posts");
        when(request.getMethod()).thenReturn("GET");
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("testuser");
        when(userService.getUserByUsername("testuser")).thenReturn(testUser);
        when(termsService.userNeedsToAcceptTerms(1L)).thenReturn(false);

        // When
        termsComplianceFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(request, response);
        verify(termsService).userNeedsToAcceptTerms(1L);
    }

    @Test
    void doFilterInternal_WhenUserNeedsToAcceptTerms_ShouldBlockRequest() throws Exception {
        // Given
        when(request.getRequestURI()).thenReturn("/api/v1/posts");
        when(request.getMethod()).thenReturn("GET");
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("testuser");
        when(userService.getUserByUsername("testuser")).thenReturn(testUser);
        when(termsService.userNeedsToAcceptTerms(1L)).thenReturn(true);
        when(termsService.getCurrentTermsVersion()).thenReturn("v2.0");
        
        PrintWriter writer = new PrintWriter(responseWriter);
        when(response.getWriter()).thenReturn(writer);
        when(objectMapper.writeValueAsString(any(TermsComplianceResponse.class)))
                .thenReturn("{\"status\":\"REQUIRED\"}");

        // When
        termsComplianceFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(response).setStatus(HttpStatus.FORBIDDEN.value());
        verify(response).setContentType(MediaType.APPLICATION_JSON_VALUE);
        verify(response).setCharacterEncoding("UTF-8");
        verify(response).setHeader("X-Terms-Compliance-Required", "true");
        verify(response).setHeader("X-Terms-Current-Version", "v2.0");
        verify(response).setHeader("X-Terms-Accept-URL", "/api/v1/terms/accept");
        verify(filterChain, never()).doFilter(request, response);
        
        writer.flush();
        assertThat(responseWriter.toString()).contains("{\"status\":\"REQUIRED\"}");
    }

    @Test
    void doFilterInternal_WhenServiceThrowsException_ShouldContinueFilterChain() throws Exception {
        // Given
        when(request.getRequestURI()).thenReturn("/api/v1/posts");
        when(request.getMethod()).thenReturn("GET");
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("testuser");
        when(userService.getUserByUsername("testuser"))
                .thenThrow(new RuntimeException("Service error"));

        // When
        termsComplianceFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(request, response);
        verify(response, never()).setStatus(HttpStatus.FORBIDDEN.value());
    }

    @Test
    void shouldSkipFilter_WithExcludedPaths_ShouldReturnTrue() throws Exception {
        // Test various excluded paths
        String[] excludedPaths = {
            "/api/v1/auth/login",
            "/api/v1/auth/register",
            "/api/v1/terms/current",
            "/api/v1/terms/accept",
            "/actuator/health",
            "/swagger-ui/index.html",
            "/favicon.ico"
        };

        for (String path : excludedPaths) {
            when(request.getRequestURI()).thenReturn(path);
            when(request.getMethod()).thenReturn("GET");
            
            // Use reflection to test private method
            Boolean result = (Boolean) ReflectionTestUtils.invokeMethod(
                termsComplianceFilter, "shouldSkipFilter", request);
            
            assertThat(result).isTrue();
        }
    }

    @Test
    void shouldSkipFilter_WithStaticResources_ShouldReturnTrue() throws Exception {
        // Test static resources
        String[] staticPaths = {
            "/static/css/style.css",
            "/css/main.css",
            "/js/app.js",
            "/images/logo.png",
            "/app.css",
            "/script.js",
            "/favicon.ico"
        };

        for (String path : staticPaths) {
            when(request.getRequestURI()).thenReturn(path);
            when(request.getMethod()).thenReturn("GET");
            
            Boolean result = (Boolean) ReflectionTestUtils.invokeMethod(
                termsComplianceFilter, "shouldSkipFilter", request);
            
            assertThat(result).isTrue();
        }
    }

    @Test
    void shouldSkipFilter_WithExcludedMethods_ShouldReturnTrue() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/v1/posts");
        when(request.getMethod()).thenReturn("OPTIONS");
        
        Boolean result = (Boolean) ReflectionTestUtils.invokeMethod(
            termsComplianceFilter, "shouldSkipFilter", request);
        
        assertThat(result).isTrue();
    }

    @Test
    void shouldSkipFilter_WithRegularApiPath_ShouldReturnFalse() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/v1/posts");
        when(request.getMethod()).thenReturn("GET");
        
        Boolean result = (Boolean) ReflectionTestUtils.invokeMethod(
            termsComplianceFilter, "shouldSkipFilter", request);
        
        assertThat(result).isFalse();
    }

    @Test
    void isPublicEndpoint_WithPublicGetRequests_ShouldReturnTrue() throws Exception {
        String[] publicPaths = {
            "/api/v1/categories",
            "/api/v1/posts",
            "/api/v1/posts/123",
            "/api/v1/posts/search",
            "/api/v1/posts/category/tech"
        };

        for (String path : publicPaths) {
            when(request.getRequestURI()).thenReturn(path);
            when(request.getMethod()).thenReturn("GET");
            
            Boolean result = (Boolean) ReflectionTestUtils.invokeMethod(
                termsComplianceFilter, "isPublicEndpoint", request);
            
            assertThat(result).isTrue();
        }
    }

    @Test
    void isPublicEndpoint_WithNonGetRequests_ShouldReturnFalse() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/v1/posts");
        when(request.getMethod()).thenReturn("POST");
        
        Boolean result = (Boolean) ReflectionTestUtils.invokeMethod(
            termsComplianceFilter, "isPublicEndpoint", request);
        
        assertThat(result).isFalse();
    }

    @Test
    void shouldNotFilter_WithNonApiPaths_ShouldReturnTrue() throws Exception {
        when(request.getRequestURI()).thenReturn("/health");
        
        Boolean result = (Boolean) ReflectionTestUtils.invokeMethod(
            termsComplianceFilter, "shouldNotFilter", request);
        
        assertThat(result).isTrue();
    }

    @Test
    void shouldNotFilter_WithApiPaths_ShouldReturnFalse() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/v1/posts");
        
        Boolean result = (Boolean) ReflectionTestUtils.invokeMethod(
            termsComplianceFilter, "shouldNotFilter", request);
        
        assertThat(result).isFalse();
    }
}