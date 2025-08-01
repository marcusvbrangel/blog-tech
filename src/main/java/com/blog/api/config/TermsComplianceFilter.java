package com.blog.api.config;

import com.blog.api.dto.TermsComplianceResponse;
import com.blog.api.dto.TermsInfoDTO;
import com.blog.api.dto.UserDTO;
import com.blog.api.service.TermsService;
import com.blog.api.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Filter to check terms compliance for authenticated users
 * Blocks access to protected endpoints if user hasn't accepted latest terms
 */
@Component
public class TermsComplianceFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(TermsComplianceFilter.class);

    @Autowired
    private TermsService termsService;

    @Autowired
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${blog.terms.enabled:true}")
    private boolean termsEnabled;

    @Value("${blog.terms.enforce-compliance:true}")
    private boolean enforceCompliance;

    // Endpoints that should be excluded from terms compliance check
    private static final List<String> EXCLUDED_PATHS = Arrays.asList(
        "/api/v1/auth/login",
        "/api/v1/auth/register", 
        "/api/v1/auth/refresh",
        "/api/v1/auth/verify-email",
        "/api/v1/auth/resend-verification",
        "/api/v1/auth/forgot-password",
        "/api/v1/auth/reset-password",
        "/api/v1/terms/current",
        "/api/v1/terms/accept",
        "/api/v1/terms/user-status",
        "/actuator/health",
        "/actuator/info",
        "/actuator/metrics",
        "/swagger-ui",
        "/v3/api-docs",
        "/favicon.ico"
    );

    // HTTP methods that should be excluded (typically GET requests for public content)
    private static final List<String> EXCLUDED_METHODS = Arrays.asList("OPTIONS");

    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                  HttpServletResponse response, 
                                  FilterChain filterChain) throws ServletException, IOException {
        
        // Skip if terms compliance is disabled
        if (!termsEnabled || !enforceCompliance) {
            filterChain.doFilter(request, response);
            return;
        }

        // Skip excluded paths and methods
        if (shouldSkipFilter(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        // Check if user is authenticated
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || 
            authentication.getName().equals("anonymousUser")) {
            // Not authenticated, continue with request
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // Get current user
            UserDTO user = userService.getUserByUsername(authentication.getName());
            
            // Check if user needs to accept terms
            if (termsService.userNeedsToAcceptTerms(user.id())) {
                logger.info("Blocking request for user {} - terms acceptance required", user.username());
                
                // Create terms info response
                String currentVersion = termsService.getCurrentTermsVersion();
                TermsInfoDTO termsInfo = TermsInfoDTO.requiresAcceptance(currentVersion);
                TermsComplianceResponse complianceResponse = TermsComplianceResponse.required(termsInfo);
                
                // Return 403 Forbidden with terms compliance information
                response.setStatus(HttpStatus.FORBIDDEN.value());
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                response.setCharacterEncoding("UTF-8");
                
                // Add custom header to indicate terms acceptance is required
                response.setHeader("X-Terms-Compliance-Required", "true");
                response.setHeader("X-Terms-Current-Version", currentVersion);
                response.setHeader("X-Terms-Accept-URL", "/api/v1/terms/accept");
                
                String jsonResponse = objectMapper.writeValueAsString(complianceResponse);
                response.getWriter().write(jsonResponse);
                
                return; // Stop the filter chain
            }
            
            // User has accepted terms, continue with request
            filterChain.doFilter(request, response);
            
        } catch (Exception e) {
            logger.error("Error checking terms compliance for user {}: {}", 
                        authentication.getName(), e.getMessage());
            
            // In case of error, allow the request to continue
            // This prevents the terms compliance check from breaking the application
            filterChain.doFilter(request, response);
        }
    }

    /**
     * Determine if the filter should be skipped for this request
     */
    private boolean shouldSkipFilter(HttpServletRequest request) {
        String requestPath = request.getRequestURI();
        String method = request.getMethod();
        
        // Skip excluded HTTP methods
        if (EXCLUDED_METHODS.contains(method)) {
            return true;
        }
        
        // Skip excluded paths
        for (String excludedPath : EXCLUDED_PATHS) {
            if (requestPath.startsWith(excludedPath)) {
                return true;
            }
        }
        
        // Skip static resources
        if (requestPath.contains("/static/") || 
            requestPath.contains("/css/") || 
            requestPath.contains("/js/") || 
            requestPath.contains("/images/") ||
            requestPath.endsWith(".css") ||
            requestPath.endsWith(".js") ||
            requestPath.endsWith(".png") ||
            requestPath.endsWith(".jpg") ||
            requestPath.endsWith(".ico")) {
            return true;
        }
        
        return false;
    }

    /**
     * Check if request is for a public endpoint that doesn't require terms acceptance
     */
    private boolean isPublicEndpoint(HttpServletRequest request) {
        String requestPath = request.getRequestURI();
        String method = request.getMethod();
        
        // Allow GET requests to public content (categories, published posts)
        if ("GET".equals(method)) {
            return requestPath.startsWith("/api/v1/categories") ||
                   requestPath.equals("/api/v1/posts") ||
                   requestPath.matches("/api/v1/posts/\\d+") ||
                   requestPath.startsWith("/api/v1/posts/search") ||
                   requestPath.startsWith("/api/v1/posts/category");
        }
        
        return false;
    }

    /**
     * Override to ensure this filter only runs for specific requests
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        // This filter should run for all requests to API endpoints
        String requestPath = request.getRequestURI();
        return !requestPath.startsWith("/api/v1/");
    }
}