package com.blog.api.dto;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TermsAcceptanceRequestTest {

    @Test
    void isValidAcceptance_WhenAllAcceptancesTrue_ShouldReturnTrue() {
        // Given
        TermsAcceptanceRequest request = new TermsAcceptanceRequest(
            "v1.0",
            true,
            true,
            true
        );

        // When
        boolean result = request.isValidAcceptance();

        // Then
        assertThat(result).isTrue();
    }

    @Test
    void isValidAcceptance_WhenPrivacyPolicyFalse_ShouldReturnFalse() {
        // Given
        TermsAcceptanceRequest request = new TermsAcceptanceRequest(
            "v1.0",
            false,
            true,
            true
        );

        // When
        boolean result = request.isValidAcceptance();

        // Then
        assertThat(result).isFalse();
    }

    @Test
    void isValidAcceptance_WhenTermsOfServiceFalse_ShouldReturnFalse() {
        // Given
        TermsAcceptanceRequest request = new TermsAcceptanceRequest(
            "v1.0",
            true,
            false,
            true
        );

        // When
        boolean result = request.isValidAcceptance();

        // Then
        assertThat(result).isFalse();
    }

    @Test
    void isValidAcceptance_WhenCookiePolicyFalse_ShouldReturnFalse() {
        // Given
        TermsAcceptanceRequest request = new TermsAcceptanceRequest(
            "v1.0",
            true,
            true,
            false
        );

        // When
        boolean result = request.isValidAcceptance();

        // Then
        assertThat(result).isFalse();
    }

    @Test
    void isValidAcceptance_WhenAllAcceptancesNull_ShouldReturnFalse() {
        // Given
        TermsAcceptanceRequest request = new TermsAcceptanceRequest(
            "v1.0",
            null,
            null,
            null
        );

        // When
        boolean result = request.isValidAcceptance();

        // Then
        assertThat(result).isFalse();
    }

    @Test
    void isValidAcceptance_WhenMixedNullAndFalse_ShouldReturnFalse() {
        // Given
        TermsAcceptanceRequest request = new TermsAcceptanceRequest(
            "v1.0",
            null,
            false,
            true
        );

        // When
        boolean result = request.isValidAcceptance();

        // Then
        assertThat(result).isFalse();
    }

    @Test
    void acceptAll_ShouldCreateValidAcceptanceRequest() {
        // Given
        String termsVersion = "v2.0";

        // When
        TermsAcceptanceRequest request = TermsAcceptanceRequest.acceptAll(termsVersion);

        // Then
        assertThat(request.termsVersion()).isEqualTo("v2.0");
        assertThat(request.acceptPrivacyPolicy()).isTrue();
        assertThat(request.acceptTermsOfService()).isTrue();
        assertThat(request.acceptCookiePolicy()).isTrue();
        assertThat(request.isValidAcceptance()).isTrue();
    }

    @Test
    void constructor_ShouldSetAllFields() {
        // Given & When
        TermsAcceptanceRequest request = new TermsAcceptanceRequest(
            "v3.0",
            true,
            false,
            true
        );

        // Then
        assertThat(request.termsVersion()).isEqualTo("v3.0");
        assertThat(request.acceptPrivacyPolicy()).isTrue();
        assertThat(request.acceptTermsOfService()).isFalse();
        assertThat(request.acceptCookiePolicy()).isTrue();
    }

    @Test
    void equals_ShouldWorkCorrectly() {
        // Given
        TermsAcceptanceRequest request1 = new TermsAcceptanceRequest("v1.0", true, true, true);
        TermsAcceptanceRequest request2 = new TermsAcceptanceRequest("v1.0", true, true, true);
        TermsAcceptanceRequest request3 = new TermsAcceptanceRequest("v2.0", true, true, true);

        // Then
        assertThat(request1).isEqualTo(request2);
        assertThat(request1).isNotEqualTo(request3);
        assertThat(request1.hashCode()).isEqualTo(request2.hashCode());
    }

    @Test
    void toString_ShouldContainAllFields() {
        // Given
        TermsAcceptanceRequest request = new TermsAcceptanceRequest(
            "v1.0",
            true,
            false,
            true
        );

        // When
        String result = request.toString();

        // Then
        assertThat(result).contains("v1.0");
        assertThat(result).contains("true");
        assertThat(result).contains("false");
    }
}