package com.blog.api.dto;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class TermsComplianceResponseTest {

    @Test
    void required_ShouldCreateRequiredResponse() {
        // Given
        TermsInfoDTO termsInfo = TermsInfoDTO.requiresAcceptance("v2.0");

        // When
        TermsComplianceResponse response = TermsComplianceResponse.required(termsInfo);

        // Then
        assertThat(response.success()).isFalse();
        assertThat(response.message()).isEqualTo("Terms acceptance is required to continue using the service");
        assertThat(response.termsInfo()).isEqualTo(termsInfo);
        assertThat(response.acceptance()).isNull();
    }

    @Test
    void requiresAcceptance_WithCustomMessage_ShouldCreateCustomResponse() {
        // Given
        String customMessage = "Please accept the updated privacy policy";
        TermsInfoDTO termsInfo = TermsInfoDTO.requiresAcceptance("v3.0");

        // When
        TermsComplianceResponse response = TermsComplianceResponse.requiresAcceptance(customMessage, termsInfo);

        // Then
        assertThat(response.success()).isFalse();
        assertThat(response.message()).isEqualTo(customMessage);
        assertThat(response.termsInfo()).isEqualTo(termsInfo);
        assertThat(response.acceptance()).isNull();
    }

    @Test
    void accepted_ShouldCreateAcceptedResponse() {
        // Given
        TermsAcceptanceResponse acceptance = new TermsAcceptanceResponse(
            1L, 1L, "testuser", "v1.0", LocalDateTime.now(), "***.***.***.**", "*** (privacy protected)"
        );

        // When
        TermsComplianceResponse response = TermsComplianceResponse.accepted(acceptance);

        // Then
        assertThat(response.success()).isTrue();
        assertThat(response.message()).isEqualTo("Terms accepted successfully");
        assertThat(response.termsInfo()).isNull();
        assertThat(response.acceptance()).isEqualTo(acceptance);
    }

    @Test
    void success_WithAllParameters_ShouldCreateSuccessResponse() {
        // Given
        String message = "Operation completed successfully";
        TermsInfoDTO termsInfo = TermsInfoDTO.notRequired("v1.0");
        TermsAcceptanceResponse acceptance = new TermsAcceptanceResponse(
            2L, 2L, "user2", "v1.0", LocalDateTime.now(), null, null
        );

        // When
        TermsComplianceResponse response = TermsComplianceResponse.success(message, termsInfo, acceptance);

        // Then
        assertThat(response.success()).isTrue();
        assertThat(response.message()).isEqualTo(message);
        assertThat(response.termsInfo()).isEqualTo(termsInfo);
        assertThat(response.acceptance()).isEqualTo(acceptance);
    }

    @Test
    void success_WithNullAcceptance_ShouldHandleGracefully() {
        // Given
        String message = "Terms not required";
        TermsInfoDTO termsInfo = TermsInfoDTO.notRequired("v1.0");

        // When
        TermsComplianceResponse response = TermsComplianceResponse.success(message, termsInfo, null);

        // Then
        assertThat(response.success()).isTrue();
        assertThat(response.message()).isEqualTo(message);
        assertThat(response.termsInfo()).isEqualTo(termsInfo);
        assertThat(response.acceptance()).isNull();
    }

    @Test
    void constructor_ShouldSetAllFields() {
        // Given
        boolean success = true;
        String message = "Custom message";
        TermsInfoDTO termsInfo = TermsInfoDTO.requiresAcceptance("v1.5");
        TermsAcceptanceResponse acceptance = new TermsAcceptanceResponse(
            3L, 3L, "user3", "v1.5", LocalDateTime.now(), "192.168.1.1", "Chrome"
        );

        // When
        TermsComplianceResponse response = new TermsComplianceResponse(success, message, termsInfo, acceptance);

        // Then
        assertThat(response.success()).isTrue();
        assertThat(response.message()).isEqualTo(message);
        assertThat(response.termsInfo()).isEqualTo(termsInfo);
        assertThat(response.acceptance()).isEqualTo(acceptance);
    }

    @Test
    void equals_ShouldWorkCorrectly() {
        // Given
        TermsInfoDTO termsInfo = TermsInfoDTO.requiresAcceptance("v1.0");
        TermsComplianceResponse response1 = TermsComplianceResponse.required(termsInfo);
        TermsComplianceResponse response2 = TermsComplianceResponse.required(termsInfo);
        TermsComplianceResponse response3 = TermsComplianceResponse.required(
            TermsInfoDTO.requiresAcceptance("v2.0")
        );

        // Then
        assertThat(response1).isEqualTo(response2);
        assertThat(response1).isNotEqualTo(response3);
        assertThat(response1.hashCode()).isEqualTo(response2.hashCode());
    }

    @Test
    void toString_ShouldContainRelevantFields() {
        // Given
        TermsComplianceResponse response = TermsComplianceResponse.required(
            TermsInfoDTO.requiresAcceptance("v1.0")
        );

        // When
        String result = response.toString();

        // Then
        assertThat(result).contains("false"); // success = false
        assertThat(result).contains("Terms acceptance is required");
    }
}