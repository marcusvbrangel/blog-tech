package com.blog.api.service;

import com.blog.api.entity.TermsAcceptance;
import com.blog.api.entity.User;
import com.blog.api.exception.ResourceNotFoundException;
import com.blog.api.repository.TermsAcceptanceRepository;
import com.blog.api.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TermsServiceTest {

    @Mock
    private TermsAcceptanceRepository termsAcceptanceRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private HttpServletRequest httpServletRequest;

    @InjectMocks
    private TermsService termsService;

    private User testUser;
    private TermsAcceptance testAcceptance;

    @BeforeEach
    void setUp() {
        // Set configuration values
        ReflectionTestUtils.setField(termsService, "currentTermsVersion", "v1.0");
        ReflectionTestUtils.setField(termsService, "forceAcceptance", true);
        ReflectionTestUtils.setField(termsService, "retentionDays", 1095);

        // Create test user
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("TestPass123!");
        testUser.setRole(User.Role.USER);

        // Create test acceptance
        testAcceptance = TermsAcceptance.of(testUser, "v1.0")
                .ipAddress("192.168.1.1")
                .userAgent("Mozilla/5.0")
                .acceptedAt(LocalDateTime.now())
                .build();
        testAcceptance.setId(1L);
    }

    @Test
    void getCurrentTermsVersion_ShouldReturnConfiguredVersion() {
        // When
        String result = termsService.getCurrentTermsVersion();

        // Then
        assertThat(result).isEqualTo("v1.0");
    }

    @Test
    void userNeedsToAcceptTerms_WithUserIdWhoHasNoAcceptance_ShouldReturnTrue() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        testUser.setTermsAcceptedVersion(null);

        // When
        boolean result = termsService.userNeedsToAcceptTerms(1L);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    void userNeedsToAcceptTerms_WithUserIdWhoHasOldVersion_ShouldReturnTrue() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        testUser.setTermsAcceptedVersion("v0.9");

        // When
        boolean result = termsService.userNeedsToAcceptTerms(1L);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    void userNeedsToAcceptTerms_WithUserIdWhoHasCurrentVersion_ShouldReturnFalse() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        testUser.setTermsAcceptedVersion("v1.0");

        // When
        boolean result = termsService.userNeedsToAcceptTerms(1L);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    void userNeedsToAcceptTerms_WithUserEntityWhoNeedsAcceptance_ShouldReturnTrue() {
        // Given
        testUser.setTermsAcceptedVersion(null);

        // When
        boolean result = termsService.userNeedsToAcceptTerms(testUser);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    void userNeedsToAcceptTerms_WhenForceAcceptanceDisabled_ShouldReturnFalse() {
        // Given
        ReflectionTestUtils.setField(termsService, "forceAcceptance", false);

        // When
        boolean result = termsService.userNeedsToAcceptTerms(1L);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    void acceptTerms_WithValidRequest_ShouldCreateAcceptanceRecord() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(termsAcceptanceRepository.existsByUserAndTermsVersion(testUser, "v1.0")).thenReturn(false);
        when(termsAcceptanceRepository.save(any(TermsAcceptance.class))).thenReturn(testAcceptance);
        when(userRepository.save(testUser)).thenReturn(testUser);
        when(httpServletRequest.getHeader("User-Agent")).thenReturn("Mozilla/5.0");
        when(httpServletRequest.getHeader("X-Forwarded-For")).thenReturn(null);
        when(httpServletRequest.getHeader("X-Real-IP")).thenReturn(null);
        when(httpServletRequest.getRemoteAddr()).thenReturn("192.168.1.1");

        // When
        TermsAcceptance result = termsService.acceptTerms(1L, httpServletRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getTermsVersion()).isEqualTo("v1.0");
        verify(termsAcceptanceRepository).save(any(TermsAcceptance.class));
        verify(userRepository).save(testUser);
        assertThat(testUser.getTermsAcceptedVersion()).isEqualTo("v1.0");
    }

    @Test
    void acceptTerms_WithUserEntity_ShouldCreateAcceptanceRecord() {
        // Given
        when(termsAcceptanceRepository.existsByUserAndTermsVersion(testUser, "v1.0")).thenReturn(false);
        when(termsAcceptanceRepository.save(any(TermsAcceptance.class))).thenReturn(testAcceptance);
        when(userRepository.save(testUser)).thenReturn(testUser);
        when(httpServletRequest.getHeader("User-Agent")).thenReturn("Mozilla/5.0");
        when(httpServletRequest.getHeader("X-Forwarded-For")).thenReturn(null);
        when(httpServletRequest.getHeader("X-Real-IP")).thenReturn(null);
        when(httpServletRequest.getRemoteAddr()).thenReturn("192.168.1.1");

        // When
        TermsAcceptance result = termsService.acceptTerms(testUser, httpServletRequest);

        // Then
        assertThat(result).isNotNull();
        verify(termsAcceptanceRepository).save(any(TermsAcceptance.class));
        verify(userRepository).save(testUser);
    }

    @Test
    void acceptTerms_WhenAlreadyAccepted_ShouldReturnExistingAcceptance() {
        // Given
        when(termsAcceptanceRepository.existsByUserAndTermsVersion(testUser, "v1.0")).thenReturn(true);
        when(termsAcceptanceRepository.findByUserAndTermsVersion(testUser, "v1.0"))
                .thenReturn(Optional.of(testAcceptance));

        // When
        TermsAcceptance result = termsService.acceptTerms(testUser, "v1.0", "192.168.1.1", "Mozilla/5.0");

        // Then
        assertThat(result).isEqualTo(testAcceptance);
        verify(termsAcceptanceRepository, never()).save(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    void acceptTerms_WithNonExistentUser_ShouldThrowException() {
        // Given
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> termsService.acceptTerms(999L, httpServletRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User");
    }

    @Test
    void getUserTermsHistory_WithValidUserId_ShouldReturnHistory() {
        // Given
        List<TermsAcceptance> history = Arrays.asList(testAcceptance);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(termsAcceptanceRepository.findByUserOrderByAcceptedAtDesc(testUser)).thenReturn(history);

        // When
        List<TermsAcceptance> result = termsService.getUserTermsHistory(1L);

        // Then
        assertThat(result).isEqualTo(history);
    }

    @Test
    void getUserLatestAcceptance_WithValidUserId_ShouldReturnLatestAcceptance() {
        // Given
        when(termsAcceptanceRepository.findLatestByUserId(1L)).thenReturn(Optional.of(testAcceptance));

        // When
        Optional<TermsAcceptance> result = termsService.getUserLatestAcceptance(1L);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(testAcceptance);
    }

    @Test
    void getUserLatestAcceptance_WithUserEntity_ShouldReturnLatestAcceptance() {
        // Given
        when(termsAcceptanceRepository.findTopByUserOrderByAcceptedAtDesc(testUser))
                .thenReturn(Optional.of(testAcceptance));

        // When
        Optional<TermsAcceptance> result = termsService.getUserLatestAcceptance(testUser);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(testAcceptance);
    }

    @Test
    void getAcceptancesForVersion_ShouldReturnCorrectAcceptances() {
        // Given
        List<TermsAcceptance> acceptances = Arrays.asList(testAcceptance);
        when(termsAcceptanceRepository.findByTermsVersionOrderByAcceptedAtDesc("v1.0"))
                .thenReturn(acceptances);

        // When
        List<TermsAcceptance> result = termsService.getAcceptancesForVersion("v1.0");

        // Then
        assertThat(result).isEqualTo(acceptances);
    }

    @Test
    void getUsersWithoutLatestTerms_ShouldReturnCorrectUsers() {
        // Given
        List<User> users = Arrays.asList(testUser);
        when(termsAcceptanceRepository.findUsersWithoutLatestTerms("v1.0")).thenReturn(users);

        // When
        List<User> result = termsService.getUsersWithoutLatestTerms();

        // Then
        assertThat(result).isEqualTo(users);
    }

    @Test
    void getUsersWithoutLatestTermsWithPagination_ShouldReturnPagedResults() {
        // Given
        PageRequest pageRequest = PageRequest.of(0, 10);
        Page<User> page = new PageImpl<>(Arrays.asList(testUser));
        when(termsAcceptanceRepository.findUsersWithoutLatestTerms("v1.0", pageRequest)).thenReturn(page);

        // When
        Page<User> result = termsService.getUsersWithoutLatestTerms(pageRequest);

        // Then
        assertThat(result).isEqualTo(page);
    }

    @Test
    void getCurrentVersionStatistics_ShouldReturnStatistics() {
        // Given
        Object[] statsData = {5L, 3L, LocalDateTime.now().minusDays(5), LocalDateTime.now()};
        when(termsAcceptanceRepository.getAcceptanceStatistics("v1.0")).thenReturn(statsData);

        // When
        TermsService.AcceptanceStatistics result = termsService.getCurrentVersionStatistics();

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getTermsVersion()).isEqualTo("v1.0");
        assertThat(result.getTotalAcceptances()).isEqualTo(5L);
        assertThat(result.getUniqueUsers()).isEqualTo(3L);
    }

    @Test
    void getVersionStatistics_WithEmptyStats_ShouldReturnZeroStats() {
        // Given
        when(termsAcceptanceRepository.getAcceptanceStatistics("v2.0")).thenReturn(null);

        // When
        TermsService.AcceptanceStatistics result = termsService.getVersionStatistics("v2.0");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getTermsVersion()).isEqualTo("v2.0");
        assertThat(result.getTotalAcceptances()).isEqualTo(0);
        assertThat(result.getUniqueUsers()).isEqualTo(0);
    }

    @Test
    void cleanupOldAcceptances_ShouldDeleteOldRecords() {
        // Given
        when(termsAcceptanceRepository.deleteAcceptancesOlderThan(any(LocalDateTime.class))).thenReturn(5);

        // When
        int result = termsService.cleanupOldAcceptances();

        // Then
        assertThat(result).isEqualTo(5);
        verify(termsAcceptanceRepository).deleteAcceptancesOlderThan(any(LocalDateTime.class));
    }

    @Test
    void forceReAcceptanceForAllUsers_ShouldResetAllUserTermsVersions() {
        // Given
        testUser.setTermsAcceptedVersion("v1.0");
        User anotherUser = new User();
        anotherUser.setId(2L);
        anotherUser.setTermsAcceptedVersion("v0.9");
        
        when(userRepository.findAll()).thenReturn(Arrays.asList(testUser, anotherUser));

        // When
        termsService.forceReAcceptanceForAllUsers();

        // Then
        verify(userRepository, times(2)).save(any(User.class));
        assertThat(testUser.getTermsAcceptedVersion()).isNull();
        assertThat(anotherUser.getTermsAcceptedVersion()).isNull();
    }

    @Test
    void isTermsAcceptanceRequired_ShouldReturnConfiguredValue() {
        // When
        boolean result = termsService.isTermsAcceptanceRequired();

        // Then
        assertThat(result).isTrue();

        // Given force acceptance disabled
        ReflectionTestUtils.setField(termsService, "forceAcceptance", false);

        // When
        boolean resultDisabled = termsService.isTermsAcceptanceRequired();

        // Then
        assertThat(resultDisabled).isFalse();
    }

    @Test
    void getMonthlyStatistics_ShouldReturnCorrectStatistics() {
        // Given
        Object[][] rawStats = {
            {2024, 1, 10L},
            {2024, 2, 15L}
        };
        when(termsAcceptanceRepository.getMonthlyAcceptanceStatistics())
                .thenReturn(Arrays.asList(rawStats));

        // When
        List<TermsService.MonthlyStatistics> result = termsService.getMonthlyStatistics();

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getYear()).isEqualTo(2024);
        assertThat(result.get(0).getMonth()).isEqualTo(1);
        assertThat(result.get(0).getAcceptances()).isEqualTo(10L);
    }

    @Test
    void acceptTerms_ShouldExtractClientIpFromXForwardedForHeader() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(termsAcceptanceRepository.existsByUserAndTermsVersion(testUser, "v1.0")).thenReturn(false);
        when(termsAcceptanceRepository.save(any(TermsAcceptance.class))).thenReturn(testAcceptance);
        when(userRepository.save(testUser)).thenReturn(testUser);
        when(httpServletRequest.getHeader("X-Forwarded-For")).thenReturn("203.0.113.1, 198.51.100.1");
        when(httpServletRequest.getHeader("User-Agent")).thenReturn("Mozilla/5.0");

        // When
        termsService.acceptTerms(1L, httpServletRequest);

        // Then
        verify(termsAcceptanceRepository).save(argThat(acceptance -> 
            "203.0.113.1".equals(acceptance.getIpAddress())));
    }

    @Test
    void acceptTerms_ShouldExtractClientIpFromXRealIpHeader() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(termsAcceptanceRepository.existsByUserAndTermsVersion(testUser, "v1.0")).thenReturn(false);
        when(termsAcceptanceRepository.save(any(TermsAcceptance.class))).thenReturn(testAcceptance);
        when(userRepository.save(testUser)).thenReturn(testUser);
        when(httpServletRequest.getHeader("X-Forwarded-For")).thenReturn(null);
        when(httpServletRequest.getHeader("X-Real-IP")).thenReturn("203.0.113.2");
        when(httpServletRequest.getHeader("User-Agent")).thenReturn("Mozilla/5.0");

        // When
        termsService.acceptTerms(1L, httpServletRequest);

        // Then
        verify(termsAcceptanceRepository).save(argThat(acceptance -> 
            "203.0.113.2".equals(acceptance.getIpAddress())));
    }

    @Test
    void acceptTerms_ShouldFallbackToRemoteAddr() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(termsAcceptanceRepository.existsByUserAndTermsVersion(testUser, "v1.0")).thenReturn(false);
        when(termsAcceptanceRepository.save(any(TermsAcceptance.class))).thenReturn(testAcceptance);
        when(userRepository.save(testUser)).thenReturn(testUser);
        when(httpServletRequest.getHeader("X-Forwarded-For")).thenReturn(null);
        when(httpServletRequest.getHeader("X-Real-IP")).thenReturn(null);
        when(httpServletRequest.getRemoteAddr()).thenReturn("127.0.0.1");
        when(httpServletRequest.getHeader("User-Agent")).thenReturn("Mozilla/5.0");

        // When
        termsService.acceptTerms(1L, httpServletRequest);

        // Then
        verify(termsAcceptanceRepository).save(argThat(acceptance -> 
            "127.0.0.1".equals(acceptance.getIpAddress())));
    }
}