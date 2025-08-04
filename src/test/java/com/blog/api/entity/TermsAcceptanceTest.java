package com.blog.api.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

class TermsAcceptanceTest {

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("TestPass123!");
    }

    @Test
    void builderPattern_WithValidData_ShouldCreateTermsAcceptance() {
        // Given
        String termsVersion = "v1.0";
        String ipAddress = "192.168.1.1";
        String userAgent = "Mozilla/5.0 Browser";
        LocalDateTime acceptedAt = LocalDateTime.now();

        // When
        TermsAcceptance acceptance = TermsAcceptance.of(testUser, termsVersion)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .acceptedAt(acceptedAt)
                .build();

        // Then
        assertThat(acceptance).isNotNull();
        assertThat(acceptance.getUser()).isEqualTo(testUser);
        assertThat(acceptance.getTermsVersion()).isEqualTo(termsVersion);
        assertThat(acceptance.getIpAddress()).isEqualTo(ipAddress);
        assertThat(acceptance.getUserAgent()).isEqualTo(userAgent);
        assertThat(acceptance.getAcceptedAt()).isEqualTo(acceptedAt);
    }

    @Test
    void builderPattern_WithCurrentTimestamp_ShouldSetCurrentTime() {
        // Given
        String termsVersion = "v1.0";
        LocalDateTime beforeCreation = LocalDateTime.now().minusSeconds(1);

        // When
        TermsAcceptance acceptance = TermsAcceptance.withCurrentTimestamp(testUser, termsVersion)
                .build();

        // Then
        assertThat(acceptance).isNotNull();
        assertThat(acceptance.getAcceptedAt()).isAfter(beforeCreation);
        assertThat(acceptance.getAcceptedAt()).isBefore(LocalDateTime.now().plusSeconds(1));
    }

    @Test
    void builderPattern_WithNullUser_ShouldThrowException() {
        // When & Then
        assertThatThrownBy(() -> TermsAcceptance.of(null, "v1.0"))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("User cannot be null");
    }

    @Test
    void builderPattern_WithNullTermsVersion_ShouldThrowException() {
        // When & Then
        assertThatThrownBy(() -> TermsAcceptance.of(testUser, null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("Terms version cannot be null");
    }

    @Test
    void builderPattern_WithEmptyTermsVersion_ShouldThrowException() {
        // When & Then
        assertThatThrownBy(() -> TermsAcceptance.of(testUser, "   "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Terms version cannot be empty");
    }

    @Test
    void builderPattern_WithTooLongTermsVersion_ShouldThrowException() {
        // Given
        String longVersion = "v1.0.0.0.0.0"; // More than 10 characters

        // When & Then
        assertThatThrownBy(() -> TermsAcceptance.of(testUser, longVersion))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Terms version cannot exceed 10 characters");
    }

    @Test
    void builderPattern_WithEmptyIpAddress_ShouldSetToNull() {
        // When
        TermsAcceptance acceptance = TermsAcceptance.of(testUser, "v1.0")
                .ipAddress("   ")
                .build();

        // Then
        assertThat(acceptance.getIpAddress()).isNull();
    }

    @Test
    void builderPattern_WithEmptyUserAgent_ShouldSetToNull() {
        // When
        TermsAcceptance acceptance = TermsAcceptance.of(testUser, "v1.0")
                .userAgent("")
                .build();

        // Then
        assertThat(acceptance.getUserAgent()).isNull();
    }

    @Test
    void builderPattern_FromAnotherAcceptance_ShouldCopyAllFields() {
        // Given
        TermsAcceptance original = TermsAcceptance.of(testUser, "v1.0")
                .ipAddress("192.168.1.1")
                .userAgent("Mozilla Browser")
                .acceptedAt(LocalDateTime.now())
                .build();

        // When
        TermsAcceptance copy = TermsAcceptance.from(original)
                .termsVersion("v1.1")
                .build();

        // Then
        assertThat(copy.getUser()).isEqualTo(original.getUser());
        assertThat(copy.getTermsVersion()).isEqualTo("v1.1");
        assertThat(copy.getIpAddress()).isEqualTo(original.getIpAddress());
        assertThat(copy.getUserAgent()).isEqualTo(original.getUserAgent());
        assertThat(copy.getAcceptedAt()).isEqualTo(original.getAcceptedAt());
    }

    @Test
    void businessMethods_IsVersionMatch_ShouldReturnCorrectResult() {
        // Given
        TermsAcceptance acceptance = TermsAcceptance.of(testUser, "v1.0").build();

        // Then
        assertThat(acceptance.isVersionMatch("v1.0")).isTrue();
        assertThat(acceptance.isVersionMatch("v1.1")).isFalse();
        assertThat(acceptance.isVersionMatch(null)).isFalse();
    }

    @Test
    void businessMethods_AcceptanceTimeComparisons_ShouldWorkCorrectly() {
        // Given
        LocalDateTime acceptedAt = LocalDateTime.now();
        TermsAcceptance acceptance = TermsAcceptance.of(testUser, "v1.0")
                .acceptedAt(acceptedAt)
                .build();

        LocalDateTime before = acceptedAt.minusHours(1);
        LocalDateTime after = acceptedAt.plusHours(1);

        // Then
        assertThat(acceptance.isAcceptedAfter(before)).isTrue();
        assertThat(acceptance.isAcceptedAfter(after)).isFalse();
        assertThat(acceptance.isAcceptedBefore(after)).isTrue();
        assertThat(acceptance.isAcceptedBefore(before)).isFalse();
    }

    @Test
    void businessMethods_HasIpAddressAndUserAgent_ShouldReturnCorrectResult() {
        // Given
        TermsAcceptance withDetails = TermsAcceptance.of(testUser, "v1.0")
                .ipAddress("192.168.1.1")
                .userAgent("Mozilla Browser")
                .build();

        TermsAcceptance withoutDetails = TermsAcceptance.of(testUser, "v1.0")
                .build();

        // Then
        assertThat(withDetails.hasIpAddress()).isTrue();
        assertThat(withDetails.hasUserAgent()).isTrue();
        assertThat(withoutDetails.hasIpAddress()).isFalse();
        assertThat(withoutDetails.hasUserAgent()).isFalse();
    }

    @Test
    void equalsAndHashCode_ShouldWorkCorrectly() {
        // Given
        LocalDateTime acceptedAt = LocalDateTime.now();
        TermsAcceptance acceptance1 = TermsAcceptance.of(testUser, "v1.0")
                .acceptedAt(acceptedAt)
                .build();
        acceptance1.setId(1L);

        TermsAcceptance acceptance2 = TermsAcceptance.of(testUser, "v1.0")
                .acceptedAt(acceptedAt)
                .build();
        acceptance2.setId(1L);

        TermsAcceptance acceptance3 = TermsAcceptance.of(testUser, "v1.1")
                .acceptedAt(acceptedAt)
                .build();
        acceptance3.setId(2L);

        // Then
        assertThat(acceptance1).isEqualTo(acceptance2);
        assertThat(acceptance1).isNotEqualTo(acceptance3);
        assertThat(acceptance1.hashCode()).isEqualTo(acceptance2.hashCode());
    }

    @Test
    void toString_ShouldContainEssentialInformation() {
        // Given
        TermsAcceptance acceptance = TermsAcceptance.of(testUser, "v1.0")
                .ipAddress("192.168.1.1")
                .build();
        acceptance.setId(1L);

        // When
        String result = acceptance.toString();

        // Then
        assertThat(result).contains("TermsAcceptance");
        assertThat(result).contains("id=1");
        assertThat(result).contains("user=testuser");
        assertThat(result).contains("termsVersion='v1.0'");
        assertThat(result).contains("ipAddress='192.168.1.1'");
    }

    @Test
    void factoryMethods_ShouldProvideConvenientCreation() {
        // Test newInstance
        TermsAcceptance newInstance = TermsAcceptance.newInstance()
                .user(testUser)
                .termsVersion("v1.0")
                .build();
        assertThat(newInstance).isNotNull();

        // Test forUser
        TermsAcceptance forUser = TermsAcceptance.forUser(testUser)
                .termsVersion("v1.0")
                .build();
        assertThat(forUser.getUser()).isEqualTo(testUser);

        // Test withCurrentTimestamp
        TermsAcceptance withTimestamp = TermsAcceptance.withCurrentTimestamp(testUser, "v1.0")
                .build();
        assertThat(withTimestamp.getAcceptedAt()).isNotNull();
    }
}