package com.blog.api.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Testes de integração da política de senha do PasswordResetConfirmRequest")
class PasswordResetConfirmRequestPasswordPolicyTest {

    private static final String VALID_TOKEN = "valid-reset-token-123";

    @Test
    @DisplayName("Deve criar solicitação com sucesso quando senha for forte")
    void shouldCreateRequestWithStrongPassword() {
        assertDoesNotThrow(() -> {
            PasswordResetConfirmRequest request = new PasswordResetConfirmRequest(VALID_TOKEN, "NewStr0ng!Pass");
            assertEquals(VALID_TOKEN, request.token());
            assertEquals("NewStr0ng!Pass", request.newPassword());
        });
    }

    @Test
    @DisplayName("Deve criar solicitação usando factory method")
    void shouldCreateRequestUsingFactoryMethod() {
        assertDoesNotThrow(() -> {
            PasswordResetConfirmRequest request = PasswordResetConfirmRequest.of(VALID_TOKEN, "FactoryM3th0d!");
            assertEquals(VALID_TOKEN, request.token());
            assertEquals("FactoryM3th0d!", request.newPassword());
        });
    }

    @ParameterizedTest
    @ValueSource(strings = {"weak", "123456", "TestPass123!", "Password123", "qwerty"})
    @DisplayName("Deve rejeitar senhas fracas durante criação da solicitação")
    void shouldRejectWeakPasswords(String weakPassword) {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            new PasswordResetConfirmRequest(VALID_TOKEN, weakPassword);
        });
        
        assertTrue(exception.getMessage().contains("Password policy violation"));
    }

    @Test
    @DisplayName("Deve rejeitar senha nula durante criação da solicitação")
    void shouldRejectNullPassword() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            new PasswordResetConfirmRequest(VALID_TOKEN, null);
        });
        
        assertTrue(exception.getMessage().contains("Password policy violation"));
        assertTrue(exception.getMessage().contains("cannot be null"));
    }

    @Test
    @DisplayName("Deve fornecer mensagem de erro detalhada para múltiplas violações")
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
    @DisplayName("Deve aceitar várias senhas fortes para redefinir")
    void shouldAcceptVariousStrongPasswordsForReset(String strongPassword) {
        assertDoesNotThrow(() -> {
            PasswordResetConfirmRequest request = new PasswordResetConfirmRequest(VALID_TOKEN, strongPassword);
            assertEquals(strongPassword, request.newPassword());
        });
    }

    @Test
    @DisplayName("Deve tratar factory method com senha fraca")
    void shouldHandleFactoryMethodWithWeakPassword() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            PasswordResetConfirmRequest.of(VALID_TOKEN, "weak");
        });
        
        assertTrue(exception.getMessage().contains("Password policy violation"));
    }

    @Test
    @DisplayName("Deve permitir token nulo mas validar política de senha")
    void shouldAllowNullTokenButValidatePassword() {
        // Null token should be allowed (will be validated by @NotBlank)
        // but password policy should still be enforced
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            new PasswordResetConfirmRequest(null, "weak");
        });
        
        assertTrue(exception.getMessage().contains("Password policy violation"));
    }

    @Test
    @DisplayName("Deve tratar casos extremos de senhas corretamente")
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