package com.blog.api.controller;

import com.blog.api.dto.NewsletterSubscriptionRequest;
import com.blog.api.dto.NewsletterSubscriptionResponse;
import com.blog.api.entity.SubscriptionStatus;
import com.blog.api.exception.BadRequestException;
import com.blog.api.service.NewsletterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for newsletter subscription operations.
 * Handles public subscription endpoints with LGPD compliance.
 * 
 * @author AI-Driven Development
 * @version 1.0
 * @since 2025-08-05
 */
@RestController
@RequestMapping("/api/v1/newsletter")
@Tag(name = "Newsletter", description = "Newsletter subscription operations")
public class NewsletterController {

    private static final Logger logger = LoggerFactory.getLogger(NewsletterController.class);

    @Autowired
    private NewsletterService newsletterService;

    /**
     * Subscribe to newsletter with LGPD compliance.
     * Accepts subscription requests and automatically captures audit metadata.
     * 
     * @param request the subscription request with LGPD consent
     * @param httpRequest the HTTP request for metadata extraction
     * @return NewsletterSubscriptionResponse with subscription result
     */
    @PostMapping("/subscribe")
    @Operation(
        summary = "Subscribe to newsletter",
        description = "Subscribe to the newsletter with LGPD compliance. Requires explicit consent and privacy policy acceptance. " +
                     "IP address and User-Agent are automatically captured for audit purposes."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "202", 
            description = "Subscription successful - confirmation email sent",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = NewsletterSubscriptionResponse.class),
                examples = @ExampleObject(
                    name = "Success",
                    value = """
                    {
                        "id": 1,
                        "email": "user@example.com",
                        "status": "PENDING",
                        "createdAt": "2025-08-05T10:30:00",
                        "message": "Subscription successful! Please check your email to confirm your subscription."
                    }
                    """
                )
            )
        ),
        @ApiResponse(
            responseCode = "400", 
            description = "Invalid request data or missing LGPD consent",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Validation Error",
                    value = """
                    {
                        "timestamp": "2025-08-05T10:30:00",
                        "status": 400,
                        "error": "Bad Request",
                        "message": "Explicit consent is required for LGPD compliance",
                        "path": "/api/v1/newsletter/subscribe"
                    }
                    """
                )
            )
        ),
        @ApiResponse(
            responseCode = "409", 
            description = "Email already subscribed with active status",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = NewsletterSubscriptionResponse.class),
                examples = @ExampleObject(
                    name = "Already Subscribed",
                    value = """
                    {
                        "id": 1,
                        "email": "user@example.com",
                        "status": "CONFIRMED",
                        "createdAt": "2025-08-01T10:30:00",
                        "message": "You are already subscribed and confirmed to our newsletter."
                    }
                    """
                )
            )
        )
    })
    public ResponseEntity<NewsletterSubscriptionResponse> subscribe(
            @Parameter(
                description = "Newsletter subscription request with LGPD consent",
                required = true,
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = NewsletterSubscriptionRequest.class),
                    examples = @ExampleObject(
                        name = "Subscription Request",
                        value = """
                        {
                            "email": "user@example.com",
                            "consentToReceiveEmails": true,
                            "privacyPolicyVersion": "1.0"
                        }
                        """
                    )
                )
            )
            @Valid @RequestBody NewsletterSubscriptionRequest request,
            HttpServletRequest httpRequest) {

        logger.info("Newsletter subscription request received for email: {}", request.email());

        try {
            // Capture client metadata for LGPD audit
            String clientIp = extractClientIpAddress(httpRequest);
            String userAgent = extractUserAgent(httpRequest);

            // Create enhanced request with audit metadata
            NewsletterSubscriptionRequest enhancedRequest = NewsletterSubscriptionRequest.withMetadata(
                    request.email(),
                    request.privacyPolicyVersion(),
                    clientIp,
                    userAgent
            );

            // Process subscription
            NewsletterSubscriptionResponse response = newsletterService.subscribe(enhancedRequest);

            // Determine appropriate HTTP status based on response
            HttpStatus status = determineHttpStatus(response);

            logger.info("Newsletter subscription processed for email: {} with status: {}", 
                       request.email(), response.status());

            return ResponseEntity.status(status).body(response);

        } catch (BadRequestException e) {
            logger.warn("Newsletter subscription rejected for email: {} - {}", request.email(), e.getMessage());
            throw e; // Let GlobalExceptionHandler handle it
        } catch (Exception e) {
            logger.error("Unexpected error processing newsletter subscription for email: {}", request.email(), e);
            throw new RuntimeException("Failed to process subscription request", e);
        }
    }

    /**
     * Check if email has active subscription.
     * Public endpoint for frontend validation.
     * 
     * @param email the email to check
     * @return boolean indicating if subscription exists
     */
    @GetMapping("/check")
    @Operation(
        summary = "Check if email is subscribed",
        description = "Check if an email address has an active newsletter subscription (not deleted)."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Check completed successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid email format")
    })
    public ResponseEntity<Boolean> checkSubscription(
            @Parameter(description = "Email address to check", required = true, example = "user@example.com")
            @RequestParam String email) {

        logger.debug("Checking subscription status for email: {}", email);

        if (email == null || email.trim().isEmpty() || !email.contains("@")) {
            throw new BadRequestException("Valid email address is required");
        }

        boolean hasActiveSubscription = newsletterService.hasActiveSubscription(email.trim().toLowerCase());
        
        logger.debug("Email {} has active subscription: {}", email, hasActiveSubscription);
        
        return ResponseEntity.ok(hasActiveSubscription);
    }

    /**
     * Confirm newsletter subscription using token from email.
     * Validates the confirmation token and updates subscriber status.
     * 
     * @param token the confirmation token from email
     * @param httpRequest the HTTP request for metadata extraction
     * @return NewsletterSubscriptionResponse with confirmation result
     */
    @GetMapping("/confirm")
    @Operation(
        summary = "Confirm newsletter subscription",
        description = "Confirm newsletter subscription using the token received in the confirmation email. " +
                     "This endpoint validates the token and updates the subscriber status to confirmed."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Subscription confirmed successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = NewsletterSubscriptionResponse.class),
                examples = @ExampleObject(
                    name = "Confirmation Success",
                    value = """
                    {
                        "id": 1,
                        "email": "user@example.com",
                        "status": "CONFIRMED",
                        "createdAt": "2025-08-05T10:30:00",
                        "message": "Email confirmed successfully! Welcome to our newsletter. You'll receive updates about new posts."
                    }
                    """
                )
            )
        ),
        @ApiResponse(
            responseCode = "400", 
            description = "Invalid, expired, or already used token",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Invalid Token",
                    value = """
                    {
                        "timestamp": "2025-08-05T10:30:00",
                        "status": 400,
                        "error": "Bad Request",
                        "message": "Invalid or expired newsletter token",  
                        "path": "/api/v1/newsletter/confirm"
                    }
                    """
                )
            )
        ),
        @ApiResponse(
            responseCode = "409", 
            description = "Email already confirmed",
            content = @Content(
                mediaType = "application/json", 
                schema = @Schema(implementation = NewsletterSubscriptionResponse.class),
                examples = @ExampleObject(
                    name = "Already Confirmed",
                    value = """
                    {
                        "id": 1,
                        "email": "user@example.com",
                        "status": "CONFIRMED",
                        "createdAt": "2025-08-01T10:30:00",
                        "message": "Your email is already confirmed. You're all set to receive our newsletter updates!"
                    }
                    """
                )
            )
        )
    })
    public ResponseEntity<NewsletterSubscriptionResponse> confirmSubscription(
            @Parameter(
                description = "Confirmation token received in email",
                required = true,
                example = "a1b2c3d4-e5f6-7890-abcd-ef1234567890"
            )
            @RequestParam String token,
            HttpServletRequest httpRequest) {

        logger.info("Newsletter confirmation request received for token: {}", token);

        if (token == null || token.trim().isEmpty()) {
            throw new BadRequestException("Confirmation token is required");
        }

        try {
            // Capture client metadata for audit
            String clientIp = extractClientIpAddress(httpRequest);
            String userAgent = extractUserAgent(httpRequest);

            // Process confirmation
            NewsletterSubscriptionResponse response = newsletterService.confirmSubscription(
                token.trim(), clientIp, userAgent);

            // Determine appropriate HTTP status based on response
            HttpStatus status = response.status() == SubscriptionStatus.CONFIRMED && 
                               response.message().contains("already confirmed") ? 
                               HttpStatus.CONFLICT : HttpStatus.OK;

            logger.info("Newsletter confirmation processed for token: {} with status: {}", 
                       token, response.status());

            return ResponseEntity.status(status).body(response);

        } catch (BadRequestException e) {
            logger.warn("Newsletter confirmation rejected for token: {} - {}", token, e.getMessage());
            throw e; // Let GlobalExceptionHandler handle it
        } catch (Exception e) {
            logger.error("Unexpected error processing newsletter confirmation for token: {}", token, e);
            throw new RuntimeException("Failed to process confirmation", e);
        }
    }

    /**
     * Extract client IP address considering proxy headers.
     * 
     * @param request the HTTP request
     * @return the client IP address
     */
    private String extractClientIpAddress(HttpServletRequest request) {
        // Check for proxy headers in order of preference
        String[] headerNames = {
            "X-Forwarded-For",
            "X-Real-IP", 
            "X-Client-IP",
            "HTTP_X_FORWARDED_FOR",
            "HTTP_X_FORWARDED",
            "HTTP_FORWARDED_FOR",
            "HTTP_FORWARDED",
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP"
        };

        for (String headerName : headerNames) {
            String ip = request.getHeader(headerName);
            if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
                // X-Forwarded-For can contain multiple IPs, take the first one
                if (ip.contains(",")) {
                    ip = ip.split(",")[0].trim();
                }
                logger.debug("Client IP extracted from header {}: {}", headerName, ip);
                return ip;
            }
        }

        // Fallback to remote address
        String ip = request.getRemoteAddr();
        logger.debug("Client IP from remote address: {}", ip);
        return ip;
    }

    /**
     * Extract User-Agent header.
     * 
     * @param request the HTTP request
     * @return the User-Agent string
     */
    private String extractUserAgent(HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        if (userAgent == null || userAgent.trim().isEmpty()) {
            userAgent = "Unknown";
        }
        
        // Truncate if too long (database constraint)
        if (userAgent.length() > 500) {
            userAgent = userAgent.substring(0, 500);
        }
        
        logger.debug("User-Agent extracted: {}", userAgent);
        return userAgent;
    }

    /**
     * Determine appropriate HTTP status code based on response.
     * 
     * @param response the newsletter subscription response
     * @return appropriate HTTP status
     */
    private HttpStatus determineHttpStatus(NewsletterSubscriptionResponse response) {
        switch (response.status()) {
            case PENDING:
                // New subscription or reactivation - needs confirmation
                return HttpStatus.ACCEPTED; // 202
            case CONFIRMED:
            case UNSUBSCRIBED:
            case DELETED:
                // Already exists with some status
                return HttpStatus.CONFLICT; // 409
            default:
                return HttpStatus.ACCEPTED; // 202 - default for new subscriptions
        }
    }
}