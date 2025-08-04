package com.blog.api.dto;

import com.blog.api.entity.TermsAcceptance;
import com.blog.api.entity.User;
import com.blog.api.util.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Testes do DTO TermsAcceptanceResponse")
class TermsAcceptanceResponseTest {

    private TermsAcceptance testAcceptance;
    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = TestDataFactory.createValidUserBuilder()
            .build();
        testUser.setId(1L);

        testAcceptance = TermsAcceptance.withCurrentTimestamp(testUser, "v1.0")
            .ipAddress("192.168.1.1")
            .userAgent("Mozilla/5.0")
            .build();
        
        testAcceptance.setId(1L);
        testAcceptance.setAcceptedAt(LocalDateTime.of(2025, 1, 15, 10, 30, 0));
    }

    @Test
    @DisplayName("Deve incluir todos os campos quando criado a partir de entidade")
    void fromEntity_ShouldIncludeAllFields() {
        // When
        TermsAcceptanceResponse response = TermsAcceptanceResponse.fromEntity(testAcceptance);

        // Then
        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.userId()).isEqualTo(1L);
        assertThat(response.username()).isEqualTo("testuser");
        assertThat(response.termsVersion()).isEqualTo("v1.0");
        assertThat(response.acceptedAt()).isEqualTo(LocalDateTime.of(2025, 1, 15, 10, 30, 0));
        assertThat(response.ipAddress()).isEqualTo("192.168.1.1");
        assertThat(response.userAgent()).isEqualTo("Mozilla/5.0");
    }

    @Test
    @DisplayName("Deve ocultar endereço IP e user agent quando usado método safe")
    void fromEntitySafe_ShouldHideIpAddressAndUserAgent() {
        // When
        TermsAcceptanceResponse response = TermsAcceptanceResponse.fromEntitySafe(testAcceptance);

        // Then
        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.userId()).isEqualTo(1L);
        assertThat(response.username()).isEqualTo("testuser");
        assertThat(response.termsVersion()).isEqualTo("v1.0");
        assertThat(response.acceptedAt()).isEqualTo(LocalDateTime.of(2025, 1, 15, 10, 30, 0));
        assertThat(response.ipAddress()).isNull();
        assertThat(response.userAgent()).isNull();
    }

    @Test
    @DisplayName("Deve tratar graciosamente quando IP e user agent forem nulos")
    void fromEntity_WithNullIpAndUserAgent_ShouldHandleGracefully() {
        // Given
        TermsAcceptance acceptanceWithNulls = TermsAcceptance.withCurrentTimestamp(testUser, "v1.0")
            .ipAddress(null)
            .userAgent(null)
            .build();
        acceptanceWithNulls.setId(2L);

        // When
        TermsAcceptanceResponse response = TermsAcceptanceResponse.fromEntity(acceptanceWithNulls);

        // Then
        assertThat(response.id()).isEqualTo(2L);
        assertThat(response.userId()).isEqualTo(1L);
        assertThat(response.username()).isEqualTo("testuser");
        assertThat(response.termsVersion()).isEqualTo("v1.0");
        assertThat(response.ipAddress()).isNull();
        assertThat(response.userAgent()).isNull();
    }

    @Test
    @DisplayName("Deve retornar nulo para IP e user agent quando usado método safe com valores nulos")
    void fromEntitySafe_WithNullIpAndUserAgent_ShouldReturnNull() {
        // Given
        TermsAcceptance acceptanceWithNulls = TermsAcceptance.withCurrentTimestamp(testUser, "v1.0")
            .ipAddress(null)
            .userAgent(null)
            .build();
        acceptanceWithNulls.setId(2L);

        // When
        TermsAcceptanceResponse response = TermsAcceptanceResponse.fromEntitySafe(acceptanceWithNulls);

        // Then
        assertThat(response.ipAddress()).isNull();
        assertThat(response.userAgent()).isNull();
    }

    @Test
    @DisplayName("Deve definir todos os campos quando usado construtor")
    void constructor_ShouldSetAllFields() {
        // Given
        LocalDateTime now = LocalDateTime.now();

        // When
        TermsAcceptanceResponse response = new TermsAcceptanceResponse(
            10L,
            5L,
            "username",
            "v2.0",
            now,
            "10.0.0.1",
            "Chrome"
        );

        // Then
        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.userId()).isEqualTo(5L);
        assertThat(response.username()).isEqualTo("username");
        assertThat(response.termsVersion()).isEqualTo("v2.0");
        assertThat(response.acceptedAt()).isEqualTo(now);
        assertThat(response.ipAddress()).isEqualTo("10.0.0.1");
        assertThat(response.userAgent()).isEqualTo("Chrome");
    }

    @Test
    @DisplayName("Deve implementar equals corretamente")
    void equals_ShouldWorkCorrectly() {
        // Given
        LocalDateTime time = LocalDateTime.now();
        TermsAcceptanceResponse response1 = new TermsAcceptanceResponse(1L, 1L, "user", "v1.0", time, "ip", "agent");
        TermsAcceptanceResponse response2 = new TermsAcceptanceResponse(1L, 1L, "user", "v1.0", time, "ip", "agent");
        TermsAcceptanceResponse response3 = new TermsAcceptanceResponse(2L, 1L, "user", "v1.0", time, "ip", "agent");

        // Then
        assertThat(response1).isEqualTo(response2);
        assertThat(response1).isNotEqualTo(response3);
        assertThat(response1.hashCode()).isEqualTo(response2.hashCode());
    }

    @Test
    @DisplayName("Deve conter todos os campos no toString")
    void toString_ShouldContainAllFields() {
        // Given
        TermsAcceptanceResponse response = new TermsAcceptanceResponse(
            1L, 1L, "user", "v1.0", LocalDateTime.now(), "ip", "agent"
        );

        // When
        String result = response.toString();

        // Then
        assertThat(result).contains("1");
        assertThat(result).contains("user");
        assertThat(result).contains("v1.0");
        assertThat(result).contains("ip");
        assertThat(result).contains("agent");
    }
}