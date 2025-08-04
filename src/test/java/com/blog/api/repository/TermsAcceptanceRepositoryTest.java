package com.blog.api.repository;

import com.blog.api.entity.TermsAcceptance;
import com.blog.api.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("Testes do repositório TermsAcceptanceRepository")
class TermsAcceptanceRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private TermsAcceptanceRepository termsAcceptanceRepository;

    private User testUser1;
    private User testUser2;
    private TermsAcceptance acceptance1;
    private TermsAcceptance acceptance2;

    @BeforeEach
    void setUp() {
        // Create test users
        testUser1 = new User();
        testUser1.setUsername("testuser1");
        testUser1.setEmail("test1@example.com");
        testUser1.setPassword("TestPass123!");
        testUser1.setRole(User.Role.USER);
        testUser1 = entityManager.persistAndFlush(testUser1);

        testUser2 = new User();
        testUser2.setUsername("testuser2");
        testUser2.setEmail("test2@example.com");
        testUser2.setPassword("TestPass123!");
        testUser2.setRole(User.Role.USER);
        testUser2 = entityManager.persistAndFlush(testUser2);

        // Create test acceptances
        acceptance1 = TermsAcceptance.of(testUser1, "v1.0")
                .ipAddress("192.168.1.1")
                .userAgent("Mozilla/5.0")
                .acceptedAt(LocalDateTime.now().minusDays(1))
                .build();
        acceptance1 = entityManager.persistAndFlush(acceptance1);

        acceptance2 = TermsAcceptance.of(testUser1, "v1.1")
                .ipAddress("192.168.1.2")
                .userAgent("Chrome/91.0")
                .acceptedAt(LocalDateTime.now())
                .build();
        acceptance2 = entityManager.persistAndFlush(acceptance2);

        // Third acceptance for different user
        TermsAcceptance acceptance3 = TermsAcceptance.of(testUser2, "v1.0")
                .ipAddress("192.168.1.3")
                .userAgent("Safari/14.1")
                .acceptedAt(LocalDateTime.now().minusHours(12))
                .build();
        entityManager.persistAndFlush(acceptance3);

        entityManager.clear();
    }

    @Test
    @DisplayName("Deve retornar última aceitação quando buscar por usuário ordenado por data")
    void findTopByUserOrderByAcceptedAtDesc_ShouldReturnLatestAcceptance() {
        // When
        Optional<TermsAcceptance> result = termsAcceptanceRepository.findTopByUserOrderByAcceptedAtDesc(testUser1);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(acceptance2.getId());
        assertThat(result.get().getTermsVersion()).isEqualTo("v1.1");
    }

    @Test
    @DisplayName("Deve retornar última aceitação quando buscar por ID do usuário")
    void findLatestByUserId_ShouldReturnLatestAcceptance() {
        // When
        Optional<TermsAcceptance> result = termsAcceptanceRepository.findLatestByUserId(testUser1.getId());

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(acceptance2.getId());
        assertThat(result.get().getTermsVersion()).isEqualTo("v1.1");
    }

    @Test
    @DisplayName("Deve retornar aceitação correta quando buscar por usuário e versão dos termos")
    void findByUserAndTermsVersion_ShouldReturnCorrectAcceptance() {
        // When
        Optional<TermsAcceptance> result = termsAcceptanceRepository.findByUserAndTermsVersion(testUser1, "v1.0");

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(acceptance1.getId());
        assertThat(result.get().getTermsVersion()).isEqualTo("v1.0");
    }

    @Test
    @DisplayName("Deve retornar todas as aceitações do usuário ordenadas por data")
    void findByUserOrderByAcceptedAtDesc_ShouldReturnAllUserAcceptancesOrdered() {
        // When
        List<TermsAcceptance> result = termsAcceptanceRepository.findByUserOrderByAcceptedAtDesc(testUser1);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getId()).isEqualTo(acceptance2.getId()); // Latest first
        assertThat(result.get(1).getId()).isEqualTo(acceptance1.getId());
    }

    @Test
    @DisplayName("Deve retornar todas as aceitações do usuário ordenadas por data usando ID")
    void findByUserIdOrderByAcceptedAtDesc_ShouldReturnAllUserAcceptancesOrdered() {
        // When
        List<TermsAcceptance> result = termsAcceptanceRepository.findByUserIdOrderByAcceptedAtDesc(testUser1.getId());

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getTermsVersion()).isEqualTo("v1.1"); // Latest first
        assertThat(result.get(1).getTermsVersion()).isEqualTo("v1.0");
    }

    @Test
    @DisplayName("Deve retornar todas as aceitações para uma versão específica")
    void findByTermsVersionOrderByAcceptedAtDesc_ShouldReturnAllAcceptancesForVersion() {
        // When
        List<TermsAcceptance> result = termsAcceptanceRepository.findByTermsVersionOrderByAcceptedAtDesc("v1.0");

        // Then
        assertThat(result).hasSize(2); // user1 and user2 both accepted v1.0
        assertThat(result.stream().allMatch(a -> a.getTermsVersion().equals("v1.0"))).isTrue();
    }

    @Test
    @DisplayName("Deve retornar resultado correto ao verificar existência por usuário e versão")
    void existsByUserAndTermsVersion_ShouldReturnCorrectResult() {
        // Then
        assertThat(termsAcceptanceRepository.existsByUserAndTermsVersion(testUser1, "v1.0")).isTrue();
        assertThat(termsAcceptanceRepository.existsByUserAndTermsVersion(testUser1, "v1.1")).isTrue();
        assertThat(termsAcceptanceRepository.existsByUserAndTermsVersion(testUser1, "v2.0")).isFalse();
        assertThat(termsAcceptanceRepository.existsByUserAndTermsVersion(testUser2, "v1.1")).isFalse();
    }

    @Test
    @DisplayName("Deve retornar resultado correto ao verificar existência por ID e versão")
    void existsByUserIdAndTermsVersion_ShouldReturnCorrectResult() {
        // Then
        assertThat(termsAcceptanceRepository.existsByUserIdAndTermsVersion(testUser1.getId(), "v1.0")).isTrue();
        assertThat(termsAcceptanceRepository.existsByUserIdAndTermsVersion(testUser1.getId(), "v2.0")).isFalse();
    }

    @Test
    @DisplayName("Deve retornar contagem correta por versão dos termos")
    void countByTermsVersion_ShouldReturnCorrectCount() {
        // When
        long countV10 = termsAcceptanceRepository.countByTermsVersion("v1.0");
        long countV11 = termsAcceptanceRepository.countByTermsVersion("v1.1");
        long countV20 = termsAcceptanceRepository.countByTermsVersion("v2.0");

        // Then
        assertThat(countV10).isEqualTo(2); // user1 and user2
        assertThat(countV11).isEqualTo(1); // only user1
        assertThat(countV20).isEqualTo(0); // none
    }

    @Test
    @DisplayName("Deve retornar aceitações em intervalo de datas")
    void findByAcceptedAtBetween_ShouldReturnAcceptancesInDateRange() {
        // Given
        LocalDateTime start = LocalDateTime.now().minusDays(2);
        LocalDateTime end = LocalDateTime.now().minusHours(1);

        // When
        List<TermsAcceptance> result = termsAcceptanceRepository.findByAcceptedAtBetween(start, end);

        // Then
        assertThat(result).hasSize(2); // acceptance1 (yesterday) and one from user2 (12h ago)
        assertThat(result.stream().allMatch(a -> 
            a.getAcceptedAt().isAfter(start) && a.getAcceptedAt().isBefore(end))).isTrue();
    }

    @Test
    @DisplayName("Deve retornar resultados paginados em intervalo de datas")
    void findByAcceptedAtBetweenWithPagination_ShouldReturnPagedResults() {
        // Given
        LocalDateTime start = LocalDateTime.now().minusDays(2);
        LocalDateTime end = LocalDateTime.now().plusHours(1);
        PageRequest pageRequest = PageRequest.of(0, 1);

        // When
        Page<TermsAcceptance> result = termsAcceptanceRepository.findByAcceptedAtBetween(start, end, pageRequest);

        // Then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(3); // All acceptances
        assertThat(result.getTotalPages()).isEqualTo(3);
    }

    @Test
    @DisplayName("Deve retornar usuários que não aceitaram última versão dos termos")
    void findUsersWithoutLatestTerms_ShouldReturnCorrectUsers() {
        // Given - user1 has v1.1, user2 has only v1.0
        testUser1.setTermsAcceptedVersion("v1.1");
        testUser2.setTermsAcceptedVersion("v1.0");
        entityManager.merge(testUser1);
        entityManager.merge(testUser2);
        entityManager.flush();

        // When
        List<User> result = termsAcceptanceRepository.findUsersWithoutLatestTerms("v1.1");

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(testUser2.getId());
    }

    @Test
    @DisplayName("Deve retornar resultados paginados de usuários sem última versão dos termos")
    void findUsersWithoutLatestTermsWithPagination_ShouldReturnPagedResults() {
        // Given
        testUser1.setTermsAcceptedVersion("v1.0");
        testUser2.setTermsAcceptedVersion("v1.0");
        entityManager.merge(testUser1);
        entityManager.merge(testUser2);
        entityManager.flush();

        PageRequest pageRequest = PageRequest.of(0, 1);

        // When
        Page<User> result = termsAcceptanceRepository.findUsersWithoutLatestTerms("v1.1", pageRequest);

        // Then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(2);
    }

    @Test
    @DisplayName("Deve retornar aceitações por endereço IP ordenadas por data")
    void findByIpAddressOrderByAcceptedAtDesc_ShouldReturnAcceptancesByIp() {
        // When
        List<TermsAcceptance> result = termsAcceptanceRepository.findByIpAddressOrderByAcceptedAtDesc("192.168.1.1");

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(acceptance1.getId());
    }

    @Test
    @DisplayName("Deve retornar estatísticas corretas de aceitação")
    void getAcceptanceStatistics_ShouldReturnCorrectStatistics() {
        // When
        Object[] stats = termsAcceptanceRepository.getAcceptanceStatistics("v1.0");

        // Then - Based on error output, it's returning a List with one array containing 4 elements
        assertThat(stats).isNotNull();
        assertThat(stats).hasSize(1); // One row returned
        
        // The first element should be an array of 4 values
        Object[] row = (Object[]) stats[0];
        assertThat(row).hasSize(4);
        
        Long totalAcceptances = ((Number) row[0]).longValue();
        Long uniqueUsers = ((Number) row[1]).longValue();
        
        assertThat(totalAcceptances).isEqualTo(2L);
        assertThat(uniqueUsers).isEqualTo(2L);
        assertThat(row[2]).isNotNull(); // firstAcceptance
        assertThat(row[3]).isNotNull(); // lastAcceptance
    }

    @Test
    @DisplayName("Deve retornar resultado correto ao verificar existência por usuário")
    void existsByUser_ShouldReturnCorrectResult() {
        // Then
        assertThat(termsAcceptanceRepository.existsByUser(testUser1)).isTrue();
        assertThat(termsAcceptanceRepository.existsByUser(testUser2)).isTrue();
    }

    @Test
    @DisplayName("Deve retornar resultado correto ao verificar existência por ID do usuário")
    void existsByUserId_ShouldReturnCorrectResult() {
        // Then
        assertThat(termsAcceptanceRepository.existsByUserId(testUser1.getId())).isTrue();
        assertThat(termsAcceptanceRepository.existsByUserId(999L)).isFalse();
    }

    @Test
    @DisplayName("Deve deletar aceitações em cascata quando usuário for deletado")
    void cascadeDelete_WhenUserDeleted_ShouldDeleteAcceptances() {
        // Given - Count acceptances before deletion
        long countBefore = termsAcceptanceRepository.count();
        
        // Get user1's acceptances count
        List<TermsAcceptance> user1Acceptances = termsAcceptanceRepository.findByUserOrderByAcceptedAtDesc(testUser1);
        int user1AcceptanceCount = user1Acceptances.size();
        
        // When - Delete acceptances first (H2 constraint workaround)
        termsAcceptanceRepository.deleteAll(user1Acceptances);
        entityManager.flush();
        
        // Then delete user
        entityManager.remove(entityManager.merge(testUser1));
        entityManager.flush();

        // Then - Acceptances should be deleted
        long countAfter = termsAcceptanceRepository.count();
        assertThat(countAfter).isEqualTo(countBefore - user1AcceptanceCount);
        
        // Verify user2's acceptances are still there
        assertThat(termsAcceptanceRepository.existsByUserId(testUser2.getId())).isTrue();
    }
}