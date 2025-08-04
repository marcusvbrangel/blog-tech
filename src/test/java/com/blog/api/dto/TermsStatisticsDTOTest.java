package com.blog.api.dto;

import com.blog.api.service.TermsService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Testes do DTO TermsStatisticsDTO")
class TermsStatisticsDTOTest {

    @Test
    @DisplayName("Deve criar DTO a partir de objetos do serviço")
    void fromService_ShouldCreateDTOFromServiceObjects() {
        // Given
        LocalDateTime firstAcceptance = LocalDateTime.of(2025, 1, 1, 10, 0, 0);
        LocalDateTime lastAcceptance = LocalDateTime.of(2025, 1, 31, 15, 30, 0);
        
        TermsService.AcceptanceStatistics acceptanceStats = new TermsService.AcceptanceStatistics(
            "v2.0",
            150L,
            75L,
            firstAcceptance,
            lastAcceptance
        );

        List<TermsService.MonthlyStatistics> monthlyStats = Arrays.asList(
            new TermsService.MonthlyStatistics(2025, 1, 100L),
            new TermsService.MonthlyStatistics(2024, 12, 50L)
        );

        // When
        TermsStatisticsDTO dto = TermsStatisticsDTO.fromService(acceptanceStats, monthlyStats);

        // Then
        assertThat(dto.termsVersion()).isEqualTo("v2.0");
        assertThat(dto.totalAcceptances()).isEqualTo(150L);
        assertThat(dto.uniqueUsers()).isEqualTo(75L);
        assertThat(dto.firstAcceptance()).isEqualTo(firstAcceptance);
        assertThat(dto.lastAcceptance()).isEqualTo(lastAcceptance);
        assertThat(dto.monthlyStatistics()).hasSize(2);
        assertThat(dto.monthlyStatistics().get(0).acceptances()).isEqualTo(100L);
        assertThat(dto.monthlyStatistics().get(1).acceptances()).isEqualTo(50L);
    }

    @Test
    @DisplayName("Deve tratar graciosamente quando estatísticas mensais forem nulas")
    void fromService_WithNullMonthlyStats_ShouldHandleGracefully() {
        // Given
        TermsService.AcceptanceStatistics acceptanceStats = new TermsService.AcceptanceStatistics(
            "v1.0", 50L, 25L, LocalDateTime.now(), LocalDateTime.now()
        );

        // When & Then - Should throw NPE for null monthlyStats
        assertThatThrownBy(() -> TermsStatisticsDTO.fromService(acceptanceStats, null))
                .isInstanceOf(NullPointerException.class);

    }

    @Test
    @DisplayName("Deve retornar lista vazia quando estatísticas mensais estiverem vazias")
    void fromService_WithEmptyMonthlyStats_ShouldReturnEmptyList() {
        // Given
        TermsService.AcceptanceStatistics acceptanceStats = new TermsService.AcceptanceStatistics(
            "v1.5", 0L, 0L, null, null
        );
        List<TermsService.MonthlyStatistics> monthlyStats = Arrays.asList();

        // When
        TermsStatisticsDTO dto = TermsStatisticsDTO.fromService(acceptanceStats, monthlyStats);

        // Then
        assertThat(dto.monthlyStatistics()).isEmpty();
        assertThat(dto.totalAcceptances()).isEqualTo(0L);
        assertThat(dto.uniqueUsers()).isEqualTo(0L);
    }

    @Test
    @DisplayName("Deve definir todos os campos quando usado construtor")
    void constructor_ShouldSetAllFields() {
        // Given
        String termsVersion = "v3.0";
        long totalAcceptances = 200L;
        long uniqueUsers = 100L;
        LocalDateTime firstAcceptance = LocalDateTime.of(2024, 6, 1, 9, 0, 0);
        LocalDateTime lastAcceptance = LocalDateTime.of(2025, 1, 15, 17, 45, 0);
        
        List<TermsStatisticsDTO.MonthlyStatsDTO> monthlyStats = Arrays.asList(
            new TermsStatisticsDTO.MonthlyStatsDTO(2025, 1, 80L),
            new TermsStatisticsDTO.MonthlyStatsDTO(2024, 12, 120L)
        );

        // When
        TermsStatisticsDTO dto = new TermsStatisticsDTO(
            termsVersion,
            totalAcceptances,
            uniqueUsers,
            firstAcceptance,
            lastAcceptance,
            monthlyStats
        );

        // Then
        assertThat(dto.termsVersion()).isEqualTo("v3.0");
        assertThat(dto.totalAcceptances()).isEqualTo(200L);
        assertThat(dto.uniqueUsers()).isEqualTo(100L);
        assertThat(dto.firstAcceptance()).isEqualTo(firstAcceptance);
        assertThat(dto.lastAcceptance()).isEqualTo(lastAcceptance);
        assertThat(dto.monthlyStatistics()).isEqualTo(monthlyStats);
    }

    @Test
    @DisplayName("Deve definir todos os campos do MonthlyStatsDTO quando usado construtor")
    void monthlyStatsDTO_Constructor_ShouldSetAllFields() {
        // When
        TermsStatisticsDTO.MonthlyStatsDTO stat = new TermsStatisticsDTO.MonthlyStatsDTO(2025, 3, 45L);

        // Then
        assertThat(stat.year()).isEqualTo(2025);
        assertThat(stat.month()).isEqualTo(3);
        assertThat(stat.acceptances()).isEqualTo(45L);
    }

    @Test
    @DisplayName("Deve implementar equals corretamente")
    void equals_ShouldWorkCorrectly() {
        // Given
        List<TermsStatisticsDTO.MonthlyStatsDTO> monthlyStats = Arrays.asList(
            new TermsStatisticsDTO.MonthlyStatsDTO(2025, 1, 10L)
        );
        
        TermsStatisticsDTO dto1 = new TermsStatisticsDTO("v1.0", 10L, 5L, null, null, monthlyStats);
        TermsStatisticsDTO dto2 = new TermsStatisticsDTO("v1.0", 10L, 5L, null, null, monthlyStats);
        TermsStatisticsDTO dto3 = new TermsStatisticsDTO("v2.0", 10L, 5L, null, null, monthlyStats);

        // Then
        assertThat(dto1).isEqualTo(dto2);
        assertThat(dto1).isNotEqualTo(dto3);
        assertThat(dto1.hashCode()).isEqualTo(dto2.hashCode());
    }

    @Test
    @DisplayName("Deve implementar equals corretamente no MonthlyStatsDTO")
    void monthlyStatsDTO_Equals_ShouldWorkCorrectly() {
        // Given
        TermsStatisticsDTO.MonthlyStatsDTO stat1 = new TermsStatisticsDTO.MonthlyStatsDTO(2025, 1, 10L);
        TermsStatisticsDTO.MonthlyStatsDTO stat2 = new TermsStatisticsDTO.MonthlyStatsDTO(2025, 1, 10L);
        TermsStatisticsDTO.MonthlyStatsDTO stat3 = new TermsStatisticsDTO.MonthlyStatsDTO(2025, 2, 10L);

        // Then
        assertThat(stat1).isEqualTo(stat2);
        assertThat(stat1).isNotEqualTo(stat3);
        assertThat(stat1.hashCode()).isEqualTo(stat2.hashCode());
    }

    @Test
    @DisplayName("Deve conter campos relevantes no toString")
    void toString_ShouldContainRelevantFields() {
        // Given
        TermsStatisticsDTO dto = new TermsStatisticsDTO(
            "v1.0", 100L, 50L, LocalDateTime.now(), LocalDateTime.now(), Arrays.asList()
        );

        // When
        String result = dto.toString();

        // Then
        assertThat(result).contains("v1.0");
        assertThat(result).contains("100");
        assertThat(result).contains("50");
    }

    @Test
    @DisplayName("Deve conter todos os campos no toString do MonthlyStatsDTO")
    void monthlyStatsDTO_ToString_ShouldContainAllFields() {
        // Given
        TermsStatisticsDTO.MonthlyStatsDTO stat = new TermsStatisticsDTO.MonthlyStatsDTO(2025, 3, 25L);

        // When
        String result = stat.toString();

        // Then
        assertThat(result).contains("2025");
        assertThat(result).contains("3");
        assertThat(result).contains("25");
    }
}