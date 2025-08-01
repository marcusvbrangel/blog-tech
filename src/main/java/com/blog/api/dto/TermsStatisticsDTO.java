package com.blog.api.dto;

import com.blog.api.service.TermsService;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for terms acceptance statistics
 */
public record TermsStatisticsDTO(
    String termsVersion,
    long totalAcceptances,
    long uniqueUsers,
    LocalDateTime firstAcceptance,
    LocalDateTime lastAcceptance,
    List<MonthlyStatsDTO> monthlyStatistics
) implements Serializable {
    
    /**
     * Create from service statistics
     */
    public static TermsStatisticsDTO fromService(TermsService.AcceptanceStatistics stats,
                                                List<TermsService.MonthlyStatistics> monthlyStats) {
        List<MonthlyStatsDTO> monthlyDTOs = monthlyStats.stream()
                .map(monthly -> new MonthlyStatsDTO(
                    monthly.getYear(),
                    monthly.getMonth(),
                    monthly.getAcceptances()
                ))
                .toList();
                
        return new TermsStatisticsDTO(
            stats.getTermsVersion(),
            stats.getTotalAcceptances(),
            stats.getUniqueUsers(),
            stats.getFirstAcceptance(),
            stats.getLastAcceptance(),
            monthlyDTOs
        );
    }
    
    /**
     * DTO for monthly statistics
     */
    public record MonthlyStatsDTO(
        int year,
        int month,
        long acceptances
    ) implements Serializable {}
}