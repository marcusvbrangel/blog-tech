package com.blog.api.dto;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Testes do DTO TermsAcceptanceRequest")
class TermsAcceptanceRequestTest {

    @Test
    @DisplayName("Deve retornar verdadeiro quando todas as aceitações forem verdadeiras")
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
    @DisplayName("Deve retornar falso quando política de privacidade for falsa")
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
    @DisplayName("Deve retornar falso quando termos de serviço forem falsos")
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
    @DisplayName("Deve retornar falso quando política de cookies for falsa")
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
    @DisplayName("Deve retornar falso quando todas as aceitações forem nulas")
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
    @DisplayName("Deve retornar falso quando houver mix de valores nulos e falsos")
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
    @DisplayName("Deve criar solicitação válida quando usar método acceptAll")
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
    @DisplayName("Deve definir todos os campos quando usado construtor")
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
    @DisplayName("Deve implementar equals corretamente")
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
    @DisplayName("Deve conter todos os campos no toString")
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