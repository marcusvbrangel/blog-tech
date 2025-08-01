package com.blog.api.controller;

import com.blog.api.dto.*;
import com.blog.api.entity.TermsAcceptance;
import com.blog.api.entity.User;
import com.blog.api.exception.ResourceNotFoundException;
import com.blog.api.service.TermsService;
import com.blog.api.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/terms")
@Tag(name = "Terms & Compliance", description = "Terms of service and privacy policy compliance operations")
public class TermsController {

    private static final Logger logger = LoggerFactory.getLogger(TermsController.class);

    @Autowired
    private TermsService termsService;

    @Autowired
    private UserService userService;

    @GetMapping("/current")
    @Operation(summary = "Get current terms information")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Terms information retrieved successfully")
    })
    public ResponseEntity<TermsInfoDTO> getCurrentTermsInfo() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        // If user is authenticated, provide personalized info
        if (auth != null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser")) {
            try {
                UserDTO userDTO = userService.getUserByUsername(auth.getName());
                return getCurrentTermsInfoForUser(userDTO.id());
            } catch (ResourceNotFoundException e) {
                logger.warn("Authenticated user {} not found in database", auth.getName());
            }
        }
        
        // For anonymous users, just provide current terms version
        String currentVersion = termsService.getCurrentTermsVersion();
        boolean acceptanceRequired = termsService.isTermsAcceptanceRequired();
        
        TermsInfoDTO termsInfo = acceptanceRequired 
            ? TermsInfoDTO.requiresAcceptance(currentVersion)
            : TermsInfoDTO.notRequired(currentVersion);
            
        return ResponseEntity.ok(termsInfo);
    }

    @GetMapping("/user-status")
    @Operation(summary = "Get terms acceptance status for current user")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User terms status retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "User not authenticated")
    })
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<TermsInfoDTO> getCurrentTermsInfoForUser(@RequestParam(required = false) Long userId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        // If no userId provided, use current authenticated user
        if (userId == null) {
            UserDTO userDTO = userService.getUserByUsername(auth.getName());
            userId = userDTO.id();
        } else {
            // Only admins can check other users' status
            if (!auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
                UserDTO currentUser = userService.getUserByUsername(auth.getName());
                if (!currentUser.id().equals(userId)) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
                }
            }
        }

        String currentVersion = termsService.getCurrentTermsVersion();
        boolean needsAcceptance = termsService.userNeedsToAcceptTerms(userId);
        
        if (needsAcceptance) {
            return ResponseEntity.ok(TermsInfoDTO.requiresAcceptance(currentVersion));
        }
        
        // Get user's latest acceptance
        Optional<TermsAcceptance> latestAcceptance = termsService.getUserLatestAcceptance(userId);
        if (latestAcceptance.isPresent()) {
            TermsAcceptance acceptance = latestAcceptance.get();
            return ResponseEntity.ok(TermsInfoDTO.alreadyAccepted(
                currentVersion,
                acceptance.getTermsVersion(),
                acceptance.getAcceptedAt()
            ));
        }
        
        return ResponseEntity.ok(TermsInfoDTO.notRequired(currentVersion));
    }

    @PostMapping("/accept")
    @Operation(summary = "Accept current terms of service and privacy policy")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Terms accepted successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid acceptance data"),
        @ApiResponse(responseCode = "401", description = "User not authenticated"),
        @ApiResponse(responseCode = "409", description = "Terms already accepted")
    })
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<TermsComplianceResponse> acceptTerms(
            @Valid @RequestBody TermsAcceptanceRequest request,
            HttpServletRequest httpRequest) {
        
        // Validate that all required acceptances are true
        if (!request.isValidAcceptance()) {
            return ResponseEntity.badRequest().body(
                TermsComplianceResponse.requiresAcceptance(
                    "You must accept all terms, privacy policy, and cookie policy to continue",
                    TermsInfoDTO.requiresAcceptance(termsService.getCurrentTermsVersion())
                )
            );
        }
        
        // Validate terms version matches current version
        String currentVersion = termsService.getCurrentTermsVersion();
        if (!currentVersion.equals(request.termsVersion())) {
            return ResponseEntity.badRequest().body(
                TermsComplianceResponse.requiresAcceptance(
                    "Terms version mismatch. Please accept the current terms version: " + currentVersion,
                    TermsInfoDTO.requiresAcceptance(currentVersion)
                )
            );
        }
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserDTO userDTO = userService.getUserByUsername(auth.getName());
        
        try {
            TermsAcceptance acceptance = termsService.acceptTerms(userDTO.id(), httpRequest);
            TermsAcceptanceResponse response = TermsAcceptanceResponse.fromEntitySafe(acceptance);
            
            logger.info("User {} accepted terms version {}", userDTO.username(), acceptance.getTermsVersion());
            
            return ResponseEntity.ok(TermsComplianceResponse.accepted(response));
            
        } catch (IllegalStateException e) {
            // User already accepted this version
            return ResponseEntity.status(HttpStatus.CONFLICT).body(
                TermsComplianceResponse.success(
                    "Terms already accepted for this version",
                    TermsInfoDTO.alreadyAccepted(currentVersion, currentVersion, null),
                    null
                )
            );
        }
    }

    @GetMapping("/history")
    @Operation(summary = "Get terms acceptance history for current user")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Terms history retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "User not authenticated")
    })
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<TermsAcceptanceResponse>> getUserTermsHistory() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserDTO userDTO = userService.getUserByUsername(auth.getName());
        
        List<TermsAcceptance> history = termsService.getUserTermsHistory(userDTO.id());
        List<TermsAcceptanceResponse> response = history.stream()
                .map(TermsAcceptanceResponse::fromEntitySafe)
                .toList();
        
        return ResponseEntity.ok(response);
    }

    // Admin endpoints

    @GetMapping("/admin/statistics")
    @Operation(summary = "Get terms acceptance statistics (Admin only)")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Statistics retrieved successfully"),
        @ApiResponse(responseCode = "403", description = "Admin access required")
    })
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TermsStatisticsDTO> getTermsStatistics(
            @RequestParam(required = false) String version) {
        
        String targetVersion = version != null ? version : termsService.getCurrentTermsVersion();
        
        TermsService.AcceptanceStatistics stats = termsService.getVersionStatistics(targetVersion);
        List<TermsService.MonthlyStatistics> monthlyStats = termsService.getMonthlyStatistics();
        
        TermsStatisticsDTO response = TermsStatisticsDTO.fromService(stats, monthlyStats);
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/admin/non-compliant-users")  
    @Operation(summary = "Get users who haven't accepted latest terms (Admin only)")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Non-compliant users retrieved successfully"),
        @ApiResponse(responseCode = "403", description = "Admin access required")
    })
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<UserDTO>> getNonCompliantUsers(Pageable pageable) {
        Page<User> users = termsService.getUsersWithoutLatestTerms(pageable);
        Page<UserDTO> response = users.map(UserDTO::fromEntity);
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/admin/version/{version}/acceptances")
    @Operation(summary = "Get all acceptances for a specific terms version (Admin only)")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Version acceptances retrieved successfully"),
        @ApiResponse(responseCode = "403", description = "Admin access required")
    })
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<TermsAcceptanceResponse>> getVersionAcceptances(
            @Parameter(description = "Terms version") @PathVariable String version) {
        
        List<TermsAcceptance> acceptances = termsService.getAcceptancesForVersion(version);
        List<TermsAcceptanceResponse> response = acceptances.stream()
                .map(TermsAcceptanceResponse::fromEntity) // Admin can see full details
                .toList();
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/admin/user/{userId}/history")
    @Operation(summary = "Get terms acceptance history for specific user (Admin only)")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User terms history retrieved successfully"),
        @ApiResponse(responseCode = "403", description = "Admin access required"),
        @ApiResponse(responseCode = "404", description = "User not found")
    })
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<TermsAcceptanceResponse>> getUserTermsHistoryAdmin(
            @Parameter(description = "User ID") @PathVariable Long userId) {
        
        List<TermsAcceptance> history = termsService.getUserTermsHistory(userId);
        List<TermsAcceptanceResponse> response = history.stream()
                .map(TermsAcceptanceResponse::fromEntity) // Admin can see full details
                .toList();
        
        return ResponseEntity.ok(response);
    }

    @PostMapping("/admin/cleanup")
    @Operation(summary = "Clean up old terms acceptance records (Admin only)")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Cleanup completed successfully"),
        @ApiResponse(responseCode = "403", description = "Admin access required")
    })
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> cleanupOldRecords() {
        int deletedCount = termsService.cleanupOldAcceptances();
        
        String message = String.format("Cleanup completed. Deleted %d old terms acceptance records.", deletedCount);
        logger.info("Admin cleanup: {}", message);
        
        return ResponseEntity.ok(message);
    }

    @PostMapping("/admin/force-reacceptance")
    @Operation(summary = "Force all users to re-accept terms (Admin only)")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Force re-acceptance initiated successfully"),
        @ApiResponse(responseCode = "403", description = "Admin access required")
    })
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> forceReAcceptance() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        logger.warn("Admin {} initiated force re-acceptance of terms for all users", auth.getName());
        
        termsService.forceReAcceptanceForAllUsers();
        
        return ResponseEntity.ok("Force re-acceptance initiated. All users will need to accept terms again.");
    }
}