package com.blog.api.dto;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class TermsInfoDTOTest {

    @Test
    void requiresAcceptance_ShouldCreateRequiredStatus() {
        // Given
        String currentVersion = "v2.0";

        // When
        TermsInfoDTO dto = TermsInfoDTO.requiresAcceptance(currentVersion);

        // Then
        assertThat(dto.currentVersion()).isEqualTo("v2.0");
        assertThat(dto.acceptanceRequired()).isTrue();
        assertThat(dto.userHasAccepted()).isFalse();
        assertThat(dto.userAcceptedVersion()).isNull();
        assertThat(dto.lastAcceptedAt()).isNull();
        assertThat(dto.termsUrl()).isEqualTo("/terms-of-service");
        assertThat(dto.privacyPolicyUrl()).isEqualTo("/privacy-policy");
        assertThat(dto.cookiePolicyUrl()).isEqualTo("/cookie-policy");
    }

    @Test
    void notRequired_ShouldCreateNotRequiredStatus() {
        // Given
        String currentVersion = "v1.5";

        // When
        TermsInfoDTO dto = TermsInfoDTO.notRequired(currentVersion);

        // Then
        assertThat(dto.currentVersion()).isEqualTo("v1.5");
        assertThat(dto.acceptanceRequired()).isFalse();
        assertThat(dto.userHasAccepted()).isTrue();
        assertThat(dto.userAcceptedVersion()).isEqualTo("v1.5");
        assertThat(dto.lastAcceptedAt()).isNull();
        assertThat(dto.termsUrl()).isEqualTo("/terms-of-service");
    }

    @Test
    void alreadyAccepted_WithAcceptedAt_ShouldCreateAcceptedStatus() {
        // Given
        String currentVersion = "v1.0";
        String userVersion = "v1.0";
        LocalDateTime acceptedAt = LocalDateTime.of(2025, 1, 15, 14, 30, 0);

        // When
        TermsInfoDTO dto = TermsInfoDTO.alreadyAccepted(currentVersion, userVersion, acceptedAt);

        // Then
        assertThat(dto.currentVersion()).isEqualTo("v1.0");
        assertThat(dto.acceptanceRequired()).isFalse();
        assertThat(dto.userHasAccepted()).isTrue();
        assertThat(dto.userAcceptedVersion()).isEqualTo("v1.0");
        assertThat(dto.lastAcceptedAt()).isEqualTo(acceptedAt);
    }

    @Test
    void alreadyAccepted_WithNullAcceptedAt_ShouldCreateAcceptedStatus() {
        // Given
        String currentVersion = "v1.0";
        String userVersion = "v1.0";

        // When
        TermsInfoDTO dto = TermsInfoDTO.alreadyAccepted(currentVersion, userVersion, null);

        // Then
        assertThat(dto.currentVersion()).isEqualTo("v1.0");
        assertThat(dto.acceptanceRequired()).isFalse();
        assertThat(dto.userHasAccepted()).isTrue();
        assertThat(dto.userAcceptedVersion()).isEqualTo("v1.0");
        assertThat(dto.lastAcceptedAt()).isNull();
    }

    @Test
    void constructor_ShouldSetAllFields() {
        // Given
        String currentVersion = "v3.0";
        boolean acceptanceRequired = true;
        boolean userHasAccepted = false;
        String userAcceptedVersion = "v2.5";
        LocalDateTime lastAcceptedAt = LocalDateTime.now();
        String termsUrl = "/terms";
        String privacyUrl = "/privacy";
        String cookieUrl = "/cookies";

        // When
        TermsInfoDTO dto = new TermsInfoDTO(
            currentVersion,
            acceptanceRequired,
            userHasAccepted,
            userAcceptedVersion,
            lastAcceptedAt,
            termsUrl,
            privacyUrl,
            cookieUrl
        );

        // Then
        assertThat(dto.currentVersion()).isEqualTo("v3.0");
        assertThat(dto.acceptanceRequired()).isTrue();
        assertThat(dto.userHasAccepted()).isFalse();
        assertThat(dto.userAcceptedVersion()).isEqualTo("v2.5");
        assertThat(dto.lastAcceptedAt()).isEqualTo(lastAcceptedAt);
        assertThat(dto.termsUrl()).isEqualTo("/terms");
        assertThat(dto.privacyPolicyUrl()).isEqualTo("/privacy");
        assertThat(dto.cookiePolicyUrl()).isEqualTo("/cookies");
    }

    @Test
    void equals_ShouldWorkCorrectly() {
        // Given
        LocalDateTime time = LocalDateTime.now();
        TermsInfoDTO dto1 = new TermsInfoDTO("v1.0", true, false, "v1.0", time, "/terms", "/privacy", "/cookies");
        TermsInfoDTO dto2 = new TermsInfoDTO("v1.0", true, false, "v1.0", time, "/terms", "/privacy", "/cookies");
        TermsInfoDTO dto3 = new TermsInfoDTO("v2.0", true, false, "v1.0", time, "/terms", "/privacy", "/cookies");

        // Then
        assertThat(dto1).isEqualTo(dto2);
        assertThat(dto1).isNotEqualTo(dto3);
        assertThat(dto1.hashCode()).isEqualTo(dto2.hashCode());
    }

    @Test
    void toString_ShouldContainRelevantFields() {
        // Given
        TermsInfoDTO dto = new TermsInfoDTO("v1.0", true, false, "v0.9", LocalDateTime.now(), "/terms", "/privacy", "/cookies");

        // When
        String result = dto.toString();

        // Then
        assertThat(result).contains("v1.0");
        assertThat(result).contains("true");
        assertThat(result).contains("v0.9");
        assertThat(result).contains("/terms");
    }
}