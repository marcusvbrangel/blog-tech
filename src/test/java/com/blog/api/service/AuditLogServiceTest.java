package com.blog.api.service;

import com.blog.api.entity.AuditLog;
import com.blog.api.repository.AuditLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Audit Log Service Tests")
class AuditLogServiceTest {

    @Mock
    private AuditLogRepository auditLogRepository;

    @Mock
    private HttpServletRequest httpServletRequest;

    private AuditLogService auditLogService;

    @BeforeEach
    void setUp() {
        MeterRegistry meterRegistry = new SimpleMeterRegistry();
        auditLogService = new AuditLogService(meterRegistry);

        // Inject mock repository
        ReflectionTestUtils.setField(auditLogService, "auditLogRepository", auditLogRepository);
        
        // Setup mock request with lenient mode
        lenient().when(httpServletRequest.getRemoteAddr()).thenReturn("127.0.0.1");
        lenient().when(httpServletRequest.getHeader("User-Agent")).thenReturn("Test-Agent");
        lenient().when(httpServletRequest.getHeader("X-Forwarded-For")).thenReturn(null);
        lenient().when(httpServletRequest.getHeader("X-Real-IP")).thenReturn(null);
    }

    @Test
    @DisplayName("Deve registrar log de auditoria com sucesso quando dados são válidos")
    void logUserAction_ShouldLogSuccessfully_WhenValidData() {
        // Arrange
        ArgumentCaptor<AuditLog> auditLogCaptor = ArgumentCaptor.forClass(AuditLog.class);
        when(auditLogRepository.save(any(AuditLog.class))).thenReturn(null);

        // Act
        auditLogService.logSuccess(
            AuditLog.AuditAction.LOGIN,
            1L,
            "testuser",
            httpServletRequest,
            "USER",
            1L,
            "Successful login"
        );

        // Give async operation time to complete
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Assert
        verify(auditLogRepository, timeout(1000)).save(auditLogCaptor.capture());
        AuditLog capturedLog = auditLogCaptor.getValue();
        
        assertThat(capturedLog.getAction()).isEqualTo(AuditLog.AuditAction.LOGIN);
        assertThat(capturedLog.getUserId()).isEqualTo(1L);
        assertThat(capturedLog.getUsername()).isEqualTo("testuser");
        assertThat(capturedLog.getResult()).isEqualTo(AuditLog.AuditResult.SUCCESS);
        assertThat(capturedLog.getResourceType()).isEqualTo("USER");
        assertThat(capturedLog.getResourceId()).isEqualTo(1L);
        assertThat(capturedLog.getDetails()).isEqualTo("Successful login");
        assertThat(capturedLog.getIpAddress()).isEqualTo("127.0.0.1");
        assertThat(capturedLog.getUserAgent()).isEqualTo("Test-Agent");
        assertThat(capturedLog.getTimestamp()).isNotNull();
    }

    @Test
    void logFailure_ShouldCreateAuditLog_WithFailureResult() {
        // Arrange
        ArgumentCaptor<AuditLog> auditLogCaptor = ArgumentCaptor.forClass(AuditLog.class);
        when(auditLogRepository.save(any(AuditLog.class))).thenReturn(null);

        // Act
        auditLogService.logFailure(
            AuditLog.AuditAction.LOGIN,
            1L,
            "testuser",
            httpServletRequest,
            "USER",
            1L,
            "Failed login attempt",
            "Invalid credentials"
        );

        // Give async operation time to complete
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Assert
        verify(auditLogRepository, timeout(1000)).save(auditLogCaptor.capture());
        AuditLog capturedLog = auditLogCaptor.getValue();
        
        assertThat(capturedLog.getAction()).isEqualTo(AuditLog.AuditAction.LOGIN);
        assertThat(capturedLog.getResult()).isEqualTo(AuditLog.AuditResult.FAILURE);
        assertThat(capturedLog.getDetails()).isEqualTo("Failed login attempt");
        assertThat(capturedLog.getErrorMessage()).isEqualTo("Invalid credentials");
    }

    @Test
    void logSecurityViolation_ShouldCreateAuditLog_WithBlockedResult() {
        // Arrange
        ArgumentCaptor<AuditLog> auditLogCaptor = ArgumentCaptor.forClass(AuditLog.class);
        when(auditLogRepository.save(any(AuditLog.class))).thenReturn(null);

        // Act
        auditLogService.logSecurityViolation(
            "Suspicious activity",
            1L,
            "testuser",
            httpServletRequest,
            "Multiple failed attempts"
        );

        // Give async operation time to complete
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Assert
        verify(auditLogRepository, timeout(1000)).save(auditLogCaptor.capture());
        AuditLog capturedLog = auditLogCaptor.getValue();
        
        assertThat(capturedLog.getAction()).isEqualTo(AuditLog.AuditAction.SECURITY_VIOLATION);
        assertThat(capturedLog.getResult()).isEqualTo(AuditLog.AuditResult.BLOCKED);
        assertThat(capturedLog.getDetails()).isEqualTo("Suspicious activity: Multiple failed attempts");
        assertThat(capturedLog.getResourceType()).isEqualTo("SECURITY");
    }

    @Test
    void logRateLimitExceeded_ShouldCreateAuditLog_WithRateLimitAction() {
        // Arrange
        ArgumentCaptor<AuditLog> auditLogCaptor = ArgumentCaptor.forClass(AuditLog.class);
        when(auditLogRepository.save(any(AuditLog.class))).thenReturn(null);

        // Act
        auditLogService.logRateLimitExceeded(
            "login",
            1L,
            "testuser",
            httpServletRequest,
            "Too many attempts"
        );

        // Give async operation time to complete
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Assert
        verify(auditLogRepository, timeout(1000)).save(auditLogCaptor.capture());
        AuditLog capturedLog = auditLogCaptor.getValue();
        
        assertThat(capturedLog.getAction()).isEqualTo(AuditLog.AuditAction.RATE_LIMIT_EXCEEDED);
        assertThat(capturedLog.getResult()).isEqualTo(AuditLog.AuditResult.BLOCKED);
        assertThat(capturedLog.getDetails()).isEqualTo("login rate limit exceeded: Too many attempts");
        assertThat(capturedLog.getResourceType()).isEqualTo("RATE_LIMIT");
    }

    @Test
    void getAuditLogsByUser_ShouldReturnPagedResults() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        List<AuditLog> auditLogs = Arrays.asList(
            createTestAuditLog(AuditLog.AuditAction.LOGIN, AuditLog.AuditResult.SUCCESS),
            createTestAuditLog(AuditLog.AuditAction.LOGOUT, AuditLog.AuditResult.SUCCESS)
        );
        Page<AuditLog> page = new PageImpl<>(auditLogs, pageable, 2);
        
        when(auditLogRepository.findByUserIdOrderByTimestampDesc(1L, pageable)).thenReturn(page);

        // Act
        Page<AuditLog> result = auditLogService.getAuditLogsByUser(1L, pageable);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getTotalElements()).isEqualTo(2);
        verify(auditLogRepository).findByUserIdOrderByTimestampDesc(1L, pageable);
    }

    @Test
    void getAuditLogsByAction_ShouldReturnPagedResults() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        List<AuditLog> auditLogs = Arrays.asList(
            createTestAuditLog(AuditLog.AuditAction.LOGIN, AuditLog.AuditResult.SUCCESS)
        );
        Page<AuditLog> page = new PageImpl<>(auditLogs, pageable, 1);
        
        when(auditLogRepository.findByActionOrderByTimestampDesc(AuditLog.AuditAction.LOGIN, pageable))
            .thenReturn(page);

        // Act
        Page<AuditLog> result = auditLogService.getAuditLogsByAction(AuditLog.AuditAction.LOGIN, pageable);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getAction()).isEqualTo(AuditLog.AuditAction.LOGIN);
        verify(auditLogRepository).findByActionOrderByTimestampDesc(AuditLog.AuditAction.LOGIN, pageable);
    }

    @Test
    void getAuditLogsByIpAddress_ShouldReturnPagedResults() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        String ipAddress = "192.168.1.1";
        List<AuditLog> auditLogs = Arrays.asList(
            createTestAuditLog(AuditLog.AuditAction.LOGIN, AuditLog.AuditResult.SUCCESS)
        );
        Page<AuditLog> page = new PageImpl<>(auditLogs, pageable, 1);
        
        when(auditLogRepository.findByIpAddressOrderByTimestampDesc(ipAddress, pageable))
            .thenReturn(page);

        // Act
        Page<AuditLog> result = auditLogService.getAuditLogsByIpAddress(ipAddress, pageable);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        verify(auditLogRepository).findByIpAddressOrderByTimestampDesc(ipAddress, pageable);
    }

    @Test
    void getFailedLoginAttempts_ShouldReturnFailedAttempts() {
        // Arrange
        LocalDateTime since = LocalDateTime.now().minusHours(1);
        List<AuditLog> failedAttempts = Arrays.asList(
            createTestAuditLog(AuditLog.AuditAction.LOGIN, AuditLog.AuditResult.FAILURE)
        );
        
        when(auditLogRepository.findFailedLoginAttempts(1L, since)).thenReturn(failedAttempts);

        // Act
        List<AuditLog> result = auditLogService.getFailedLoginAttempts(1L, since);

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getResult()).isEqualTo(AuditLog.AuditResult.FAILURE);
        verify(auditLogRepository).findFailedLoginAttempts(1L, since);
    }

    @Test
    void countFailedLoginAttemptsByIp_ShouldReturnCount() {
        // Arrange
        String ipAddress = "192.168.1.1";
        LocalDateTime since = LocalDateTime.now().minusHours(1);
        
        when(auditLogRepository.countFailedLoginAttemptsByIp(ipAddress, since)).thenReturn(5L);

        // Act
        long result = auditLogService.countFailedLoginAttemptsByIp(ipAddress, since);

        // Assert
        assertThat(result).isEqualTo(5L);
        verify(auditLogRepository).countFailedLoginAttemptsByIp(ipAddress, since);
    }

    @Test
    void getSecurityViolations_ShouldReturnPagedResults() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        List<AuditLog> violations = Arrays.asList(
            createTestAuditLog(AuditLog.AuditAction.SECURITY_VIOLATION, AuditLog.AuditResult.BLOCKED)
        );
        Page<AuditLog> page = new PageImpl<>(violations, pageable, 1);
        
        when(auditLogRepository.findSecurityViolations(pageable)).thenReturn(page);

        // Act
        Page<AuditLog> result = auditLogService.getSecurityViolations(pageable);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getAction()).isEqualTo(AuditLog.AuditAction.SECURITY_VIOLATION);
        verify(auditLogRepository).findSecurityViolations(pageable);
    }

    @Test
    void getRecentActivityByUser_ShouldReturnRecentActivity() {
        // Arrange
        LocalDateTime since = LocalDateTime.now().minusHours(24);
        List<AuditLog> recentActivity = Arrays.asList(
            createTestAuditLog(AuditLog.AuditAction.LOGIN, AuditLog.AuditResult.SUCCESS),
            createTestAuditLog(AuditLog.AuditAction.POST_CREATE, AuditLog.AuditResult.SUCCESS)
        );
        
        when(auditLogRepository.findRecentActivityByUser(1L, since)).thenReturn(recentActivity);

        // Act
        List<AuditLog> result = auditLogService.getRecentActivityByUser(1L, since);

        // Assert
        assertThat(result).hasSize(2);
        verify(auditLogRepository).findRecentActivityByUser(1L, since);
    }

    @Test
    void getAdminActions_ShouldReturnPagedResults() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        List<AuditLog> adminActions = Arrays.asList(
            createTestAuditLog(AuditLog.AuditAction.ADMIN_ACCESS, AuditLog.AuditResult.SUCCESS)
        );
        Page<AuditLog> page = new PageImpl<>(adminActions, pageable, 1);
        
        when(auditLogRepository.findAdminActions(pageable)).thenReturn(page);

        // Act
        Page<AuditLog> result = auditLogService.getAdminActions(pageable);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getAction()).isEqualTo(AuditLog.AuditAction.ADMIN_ACCESS);
        verify(auditLogRepository).findAdminActions(pageable);
    }

    @Test
    void handleXForwardedForHeader_ShouldExtractFirstIP() {
        // Arrange
        when(httpServletRequest.getHeader("X-Forwarded-For")).thenReturn("192.168.1.1, 10.0.0.1");
        ArgumentCaptor<AuditLog> auditLogCaptor = ArgumentCaptor.forClass(AuditLog.class);
        when(auditLogRepository.save(any(AuditLog.class))).thenReturn(null);

        // Act
        auditLogService.logSuccess(
            AuditLog.AuditAction.LOGIN,
            1L,
            "testuser",
            httpServletRequest,
            "USER",
            1L,
            "Test"
        );

        // Give async operation time to complete
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Assert
        verify(auditLogRepository, timeout(1000)).save(auditLogCaptor.capture());
        AuditLog capturedLog = auditLogCaptor.getValue();
        assertThat(capturedLog.getIpAddress()).isEqualTo("192.168.1.1");
    }

    @Test
    void handleLongUserAgent_ShouldTruncateToMaxLength() {
        // Arrange
        String longUserAgent = "A".repeat(600); // Longer than 500 char limit
        when(httpServletRequest.getHeader("User-Agent")).thenReturn(longUserAgent);
        ArgumentCaptor<AuditLog> auditLogCaptor = ArgumentCaptor.forClass(AuditLog.class);
        when(auditLogRepository.save(any(AuditLog.class))).thenReturn(null);

        // Act
        auditLogService.logSuccess(
            AuditLog.AuditAction.LOGIN,
            1L,
            "testuser",
            httpServletRequest,
            "USER",
            1L,
            "Test"
        );

        // Give async operation time to complete
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Assert
        verify(auditLogRepository, timeout(1000)).save(auditLogCaptor.capture());
        AuditLog capturedLog = auditLogCaptor.getValue();
        assertThat(capturedLog.getUserAgent()).hasSize(500);
    }

    private AuditLog createTestAuditLog(AuditLog.AuditAction action, AuditLog.AuditResult result) {
        return AuditLog.builder()
            .userId(1L)
            .username("testuser")
            .action(action)
            .result(result)
            .ipAddress("127.0.0.1")
            .userAgent("Test-Agent")
            .timestamp(LocalDateTime.now())
            .build();
    }
}

