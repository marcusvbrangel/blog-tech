package com.blog.api.service;

import com.blog.api.entity.TermsAcceptance;
import com.blog.api.entity.User;
import com.blog.api.exception.ResourceNotFoundException;
import com.blog.api.repository.TermsAcceptanceRepository;
import com.blog.api.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Terms Service Tests")
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
        // Set up configuration values
        ReflectionTestUtils.setField(termsService, "currentTermsVersion", "v1.0");
        ReflectionTestUtils.setField(termsService, "forceAcceptance", true);
        ReflectionTestUtils.setField(termsService, "retentionDays", 1095);

        // Create test user using builder pattern
        testUser = User.of("testuser", "test@example.com", "TestPass123!")
                .role(User.Role.USER)
                .termsAcceptedVersion("v1.0")
                .build();
        testUser.setId(1L);

        // Create test acceptance
        testAcceptance = TermsAcceptance.withCurrentTimestamp(testUser, "v1.0")
                .ipAddress("192.168.1.1")
                .userAgent("Mozilla/5.0")
                .build();
        testAcceptance.setId(1L);
    }

    @Test
    @DisplayName("Deve retornar versão configurada dos termos")
    void getCurrentTermsVersion_ShouldReturnConfiguredVersion() {
        // When
        String result = termsService.getCurrentTermsVersion();

        // Then
        assertThat(result).isEqualTo("v1.0");
    }

    @Test
    @DisplayName("Deve retornar true quando usuário não tem aceitação de termos")
    void userNeedsToAcceptTerms_WithUserIdWhoHasNoAcceptance_ShouldReturnTrue() {
        // Given
        User userWithoutTerms = User.of("testuser", "test@example.com", "TestPass123!")
                .role(User.Role.USER)
                .termsAcceptedVersion(null)
                .build();
        userWithoutTerms.setId(1L);
        
        when(userRepository.findById(1L)).thenReturn(Optional.of(userWithoutTerms));

        // When
        boolean result = termsService.userNeedsToAcceptTerms(1L);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("Deve retornar true quando usuário tem versão antiga dos termos")
    void userNeedsToAcceptTerms_WithUserIdWhoHasOldVersion_ShouldReturnTrue() {
        // Given
        User userWithOldTerms = User.of("testuser", "test@example.com", "TestPass123!")
                .role(User.Role.USER)
                .termsAcceptedVersion("v0.9")
                .build();
        userWithOldTerms.setId(1L);
        
        when(userRepository.findById(1L)).thenReturn(Optional.of(userWithOldTerms));

        // When
        boolean result = termsService.userNeedsToAcceptTerms(1L);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("Deve retornar false quando usuário tem versão atual dos termos")
    void userNeedsToAcceptTerms_WithUserIdWhoHasCurrentVersion_ShouldReturnFalse() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // When
        boolean result = termsService.userNeedsToAcceptTerms(1L);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("Deve funcionar corretamente com entidade de usuário")
    void userNeedsToAcceptTerms_WithUserEntity_ShouldWorkCorrectly() {
        // Test with user who needs to accept
        User userWithoutTerms = User.of("testuser", "test@example.com", "TestPass123!")
                .role(User.Role.USER)
                .termsAcceptedVersion(null)
                .build();

        boolean result = termsService.userNeedsToAcceptTerms(userWithoutTerms);
        assertThat(result).isTrue();

        // Test with user who has current version
        result = termsService.userNeedsToAcceptTerms(testUser);
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("Deve criar registro de aceitação quando aceitar termos com ID do usuário")
    void acceptTerms_WithUserId_ShouldCreateAcceptanceRecord() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(termsAcceptanceRepository.existsByUserAndTermsVersion(testUser, "v1.0")).thenReturn(false);
        when(termsAcceptanceRepository.save(any(TermsAcceptance.class))).thenReturn(testAcceptance);
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(httpServletRequest.getRemoteAddr()).thenReturn("192.168.1.1");
        when(httpServletRequest.getHeader("User-Agent")).thenReturn("Mozilla/5.0");

        // When
        TermsAcceptance result = termsService.acceptTerms(1L, httpServletRequest);

        // Then
        assertThat(result).isNotNull();
        verify(termsAcceptanceRepository).save(any(TermsAcceptance.class));
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Deve criar registro de aceitação quando aceitar termos com entidade do usuário")
    void acceptTerms_WithUserEntity_ShouldCreateAcceptanceRecord() {
        // Given
        when(termsAcceptanceRepository.existsByUserAndTermsVersion(testUser, "v1.0")).thenReturn(false);
        when(termsAcceptanceRepository.save(any(TermsAcceptance.class))).thenReturn(testAcceptance);
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(httpServletRequest.getRemoteAddr()).thenReturn("192.168.1.1");
        when(httpServletRequest.getHeader("User-Agent")).thenReturn("Mozilla/5.0");

        // When
        TermsAcceptance result = termsService.acceptTerms(testUser, httpServletRequest);

        // Then
        assertThat(result).isNotNull();
        verify(termsAcceptanceRepository).save(any(TermsAcceptance.class));
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Deve retornar aceitação existente quando já foi aceito")
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
    }

    @Test
    @DisplayName("Deve retornar histórico de aceitações do usuário")
    void getUserTermsHistory_ShouldReturnUserAcceptances() {
        // Given
        List<TermsAcceptance> expectedHistory = Arrays.asList(testAcceptance);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(termsAcceptanceRepository.findByUserOrderByAcceptedAtDesc(testUser))
                .thenReturn(expectedHistory);

        // When
        List<TermsAcceptance> result = termsService.getUserTermsHistory(1L);

        // Then
        assertThat(result).isEqualTo(expectedHistory);
    }

    @Test
    @DisplayName("Deve retornar aceitação mais recente do usuário")
    void getUserLatestAcceptance_ShouldReturnLatestAcceptance() {
        // Given
        when(termsAcceptanceRepository.findLatestByUserId(1L))
                .thenReturn(Optional.of(testAcceptance));

        // When
        Optional<TermsAcceptance> result = termsService.getUserLatestAcceptance(1L);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(testAcceptance);
    }

    @Test
    @DisplayName("Deve retornar aceitações para versão específica")
    void getAcceptancesForVersion_ShouldReturnAcceptancesForSpecificVersion() {
        // Given
        List<TermsAcceptance> expectedAcceptances = Arrays.asList(testAcceptance);
        when(termsAcceptanceRepository.findByTermsVersionOrderByAcceptedAtDesc("v1.0"))
                .thenReturn(expectedAcceptances);

        // When
        List<TermsAcceptance> result = termsService.getAcceptancesForVersion("v1.0");

        // Then
        assertThat(result).isEqualTo(expectedAcceptances);
    }

    @Test
    @DisplayName("Deve retornar usuários sem aceitação dos termos mais recentes")
    void getUsersWithoutLatestTerms_ShouldReturnUsersWithoutLatestTerms() {
        // Given
        List<User> expectedUsers = Arrays.asList(testUser);
        when(termsAcceptanceRepository.findUsersWithoutLatestTerms("v1.0"))
                .thenReturn(expectedUsers);

        // When
        List<User> result = termsService.getUsersWithoutLatestTerms();

        // Then
        assertThat(result).isEqualTo(expectedUsers);
    }

    @Test
    @DisplayName("Deve retornar usuários paginados sem aceitação dos termos mais recentes")
    void getUsersWithoutLatestTerms_WithPageable_ShouldReturnPagedUsers() {
        // Given
        Page<User> expectedPage = new PageImpl<>(Arrays.asList(testUser));
        PageRequest pageable = PageRequest.of(0, 10);
        when(termsAcceptanceRepository.findUsersWithoutLatestTerms("v1.0", pageable))
                .thenReturn(expectedPage);

        // When
        Page<User> result = termsService.getUsersWithoutLatestTerms(pageable);

        // Then
        assertThat(result).isEqualTo(expectedPage);
    }

    @Test
    @DisplayName("Deve retornar valor de aceitação obrigatória")
    void isTermsAcceptanceRequired_ShouldReturnForceAcceptanceValue() {
        // When
        boolean result = termsService.isTermsAcceptanceRequired();

        // Then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("Deve lançar ResourceNotFoundException quando usuário não é encontrado")
    void acceptTerms_WhenUserNotFound_ShouldThrowResourceNotFoundException() {
        // Given
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> termsService.acceptTerms(999L, httpServletRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User");
    }

    @Test
    @DisplayName("Deve lançar ResourceNotFoundException quando usuário não é encontrado para histórico")
    void getUserTermsHistory_WhenUserNotFound_ShouldThrowResourceNotFoundException() {
        // Given
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> termsService.getUserTermsHistory(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User");
    }
}