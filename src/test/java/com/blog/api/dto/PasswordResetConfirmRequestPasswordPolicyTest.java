package com.blog.api.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("PasswordResetConfirmRequest Password Policy Integration Tests")
class PasswordResetConfirmRequestPasswordPolicyTest {

    private static final String VALID_TOKEN = "valid-reset-token-123";

    @Test
    @DisplayName("Should create request successfully with strong password")
    void shouldCreateRequestWithStrongPassword() {
        assertDoesNotThrow(() -> {
            PasswordResetConfirmRequest request = new PasswordResetConfirmRequest(VALID_TOKEN, "NewStr0ng!Pass");
            assertEquals(VALID_TOKEN, request.token());
            assertEquals("NewStr0ng!Pass", request.newPassword());
        });
    }

    @Test
    @DisplayName("Should create request using factory method")
    void shouldCreateRequestUsingFactoryMethod() {
        assertDoesNotThrow(() -> {
            PasswordResetConfirmRequest request = PasswordResetConfirmRequest.of(VALID_TOKEN, "FactoryM3th0d!");
            assertEquals(VALID_TOKEN, request.token());
            assertEquals("FactoryM3th0d!", request.newPassword());
        });
    }

    @ParameterizedTest
    @ValueSource(strings = {"weak", "123456", "password", "Password123", "qwerty"})
    @DisplayName("Should reject weak passwords during request creation")
    void shouldRejectWeakPasswords(String weakPassword) {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            new PasswordResetConfirmRequest(VALID_TOKEN, weakPassword);
        });
        
        assertTrue(exception.getMessage().contains("Password policy violation"));
    }

    @Test
    @DisplayName("Should reject null password during request creation")
    void shouldRejectNullPassword() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            new PasswordResetConfirmRequest(VALID_TOKEN, null);
        });
        
        assertTrue(exception.getMessage().contains("Password policy violation"));
        assertTrue(exception.getMessage().contains("cannot be null"));
    }

    @Test
    @DisplayName("Should provide detailed error message for multiple violations")
    void shouldProvideDetailedErrorMessage() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            new PasswordResetConfirmRequest(VALID_TOKEN, "abc");
        });
        
        String message = exception.getMessage();
        assertTrue(message.contains("Password policy violation"));
        assertTrue(message.contains("8 characters"));
        assertTrue(message.contains("uppercase"));
        assertTrue(message.contains("digit"));
        assertTrue(message.contains("special character"));
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "ResetP@ssw0rd1", "NewSecure!123", "ChangedP@ss1", 
        "Fresh$ecret2", "Updated!Pass3", "Modified@123"
    })
    @DisplayName("Should accept various strong passwords for reset")
    void shouldAcceptVariousStrongPasswordsForReset(String strongPassword) {
        assertDoesNotThrow(() -> {
            PasswordResetConfirmRequest request = new PasswordResetConfirmRequest(VALID_TOKEN, strongPassword);
            assertEquals(strongPassword, request.newPassword());
        });
    }

    @Test
    @DisplayName("Should handle factory method with weak password")
    void shouldHandleFactoryMethodWithWeakPassword() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            PasswordResetConfirmRequest.of(VALID_TOKEN, "weak");
        });
        
        assertTrue(exception.getMessage().contains("Password policy violation"));
    }

    @Test
    @DisplayName("Should allow null token but validate password policy")
    void shouldAllowNullTokenButValidatePassword() {
        // Null token should be allowed (will be validated by @NotBlank)
        // but password policy should still be enforced
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            new PasswordResetConfirmRequest(null, "weak");
        });
        
        assertTrue(exception.getMessage().contains("Password policy violation"));
    }

    @Test
    @DisplayName("Should handle edge case passwords correctly")
    void shouldHandleEdgeCasePasswords() {
        // Exactly meets all requirements
        assertDoesNotThrow(() -> {
            new PasswordResetConfirmRequest(VALID_TOKEN, "NewPass1!");
        });
        
        // Very long password
        assertDoesNotThrow(() -> {
            new PasswordResetConfirmRequest(VALID_TOKEN, "MyVeryLongResetPassword123!@#WithManyCharacters");
        });
        
        // Empty string should fail
        assertThrows(IllegalArgumentException.class, () -> {
            new PasswordResetConfirmRequest(VALID_TOKEN, "");
        });
    }
}