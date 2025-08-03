package com.blog.api.config;

import com.blog.api.service.CustomUserDetailsService;
import com.blog.api.service.JwtBlacklistService;
import com.blog.api.util.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT Authentication Filter with blacklist support.
 * This filter processes JWT tokens and validates them against the blacklist
 * before establishing security context.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Autowired
    private JwtBlacklistService jwtBlacklistService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, 
                                    FilterChain filterChain) throws ServletException, IOException {
        
        final String requestTokenHeader = request.getHeader("Authorization");
        final String requestURI = request.getRequestURI();

        String username = null;
        String jwtToken = null;
        String jti = null;

        // Extract and validate token format
        if (requestTokenHeader != null && requestTokenHeader.startsWith("Bearer ")) {
            jwtToken = requestTokenHeader.substring(7);
            
            // Preliminary format validation
            if (!jwtUtil.isValidTokenFormat(jwtToken)) {
                logger.debug("Invalid JWT token format from {}", request.getRemoteAddr());
                handleInvalidToken(response, "Invalid token format");
                return;
            }

            try {
                // Extract basic claims first
                username = jwtUtil.getUsernameFromToken(jwtToken);
                jti = jwtUtil.getJtiFromToken(jwtToken);
                
                // Enhanced blacklist check with detailed logging
                if (jti != null && jwtBlacklistService.isTokenRevoked(jti)) {
                    logger.warn("Attempted access with revoked token - JTI: {}, Username: {}, IP: {}, URI: {}", 
                               jti, username, request.getRemoteAddr(), requestURI);
                    handleRevokedToken(response, jti);
                    return;
                }
                
            } catch (io.jsonwebtoken.ExpiredJwtException e) {
                logger.debug("JWT token expired - JTI: {}, Username: {}", jti, username);
                handleExpiredToken(response);
                return;
            } catch (io.jsonwebtoken.MalformedJwtException e) {
                logger.warn("Malformed JWT token from {}", request.getRemoteAddr());
                handleMalformedToken(response);
                return;
            } catch (io.jsonwebtoken.security.SignatureException e) {
                logger.warn("Invalid JWT signature from {}", request.getRemoteAddr());
                handleInvalidSignature(response);
                return;
            } catch (Exception e) {
                logger.error("Error processing JWT token - JTI: {}, Username: {}", jti, username, e);
                handleTokenProcessingError(response);
                return;
            }
        }

        // Proceed with authentication if token is valid
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

                // The validateToken method in JwtUtil now includes blacklist check
                if (jwtUtil.validateToken(jwtToken, userDetails)) {
                    UsernamePasswordAuthenticationToken authToken = 
                        new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    
                    logger.trace("Successfully authenticated user: {} with JTI: {}", username, jti);
                } else {
                    logger.debug("Token validation failed for user: {} with JTI: {}", username, jti);
                }
            } catch (Exception e) {
                logger.error("Error during user authentication for username: {}", username, e);
            }
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Handle revoked token with specific error response.
     */
    private void handleRevokedToken(HttpServletResponse response, String jti) throws IOException {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("error", "token_revoked");
        errorResponse.put("message", "Token has been revoked");
        errorResponse.put("timestamp", System.currentTimeMillis());
        
        sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, errorResponse);
    }

    /**
     * Handle expired token with specific error response.
     */
    private void handleExpiredToken(HttpServletResponse response) throws IOException {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("error", "token_expired");
        errorResponse.put("message", "Token has expired");
        errorResponse.put("timestamp", System.currentTimeMillis());
        
        sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, errorResponse);
    }

    /**
     * Handle malformed token with specific error response.
     */
    private void handleMalformedToken(HttpServletResponse response) throws IOException {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("error", "token_malformed");
        errorResponse.put("message", "Token is malformed");
        errorResponse.put("timestamp", System.currentTimeMillis());
        
        sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, errorResponse);
    }

    /**
     * Handle invalid token signature with specific error response.
     */
    private void handleInvalidSignature(HttpServletResponse response) throws IOException {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("error", "invalid_signature");
        errorResponse.put("message", "Token signature is invalid");
        errorResponse.put("timestamp", System.currentTimeMillis());
        
        sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, errorResponse);
    }

    /**
     * Handle invalid token format with specific error response.
     */
    private void handleInvalidToken(HttpServletResponse response, String message) throws IOException {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("error", "invalid_token");
        errorResponse.put("message", message);
        errorResponse.put("timestamp", System.currentTimeMillis());
        
        sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, errorResponse);
    }

    /**
     * Handle general token processing errors.
     */
    private void handleTokenProcessingError(HttpServletResponse response) throws IOException {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("error", "token_processing_error");
        errorResponse.put("message", "Unable to process token");
        errorResponse.put("timestamp", System.currentTimeMillis());
        
        sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, errorResponse);
    }

    /**
     * Send standardized error response.
     */
    private void sendErrorResponse(HttpServletResponse response, int status, Map<String, Object> errorResponse) 
            throws IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        
        String jsonResponse = objectMapper.writeValueAsString(errorResponse);
        response.getWriter().write(jsonResponse);
        response.getWriter().flush();
    }
}