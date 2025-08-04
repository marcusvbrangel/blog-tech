package com.blog.api.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Testes do validador de política de senhas")
class PasswordPolicyValidatorTest {

    @Test
    @DisplayName("Deve validar senha forte com sucesso")
    void shouldValidateStrongPasswordSuccessfully() {
        String strongPassword = "MyStr0ng!Pass";
        
        PasswordPolicyValidator.ValidationResult result = PasswordPolicyValidator.validate(strongPassword);
        
        assertTrue(result.isValid());
        assertTrue(result.getErrors().isEmpty());
    }

    @Test
    @DisplayName("Deve rejeitar senha nula")
    void shouldRejectNullPassword() {
        PasswordPolicyValidator.ValidationResult result = PasswordPolicyValidator.validate(null);
        
        assertFalse(result.isValid());
        assertEquals(1, result.getErrors().size());
        assertTrue(result.getErrors().get(0).contains("cannot be null"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"1234567", "short", "a", "Ab1@"})
    @DisplayName("Deve rejeitar senhas com menos de 8 caracteres")
    void shouldRejectShortPasswords(String shortPassword) {
        PasswordPolicyValidator.ValidationResult result = PasswordPolicyValidator.validate(shortPassword);
        
        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream()
            .anyMatch(error -> error.contains("at least 8 characters")));
    }

    @ParameterizedTest
    @ValueSource(strings = {"PASSWORD123!", "MYSTRONG!PASS", "UPPERCASE1@"})
    @DisplayName("Deve rejeitar senhas sem letras minúsculas")
    void shouldRejectPasswordsWithoutLowercase(String password) {
        PasswordPolicyValidator.ValidationResult result = PasswordPolicyValidator.validate(password);
        
        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream()
            .anyMatch(error -> error.contains("lowercase letter")));
    }

    @ParameterizedTest
    @ValueSource(strings = {"password123!", "mystrong!pass", "lowercase1@"})
    @DisplayName("Deve rejeitar senhas sem letras maiúsculas")
    void shouldRejectPasswordsWithoutUppercase(String password) {
        PasswordPolicyValidator.ValidationResult result = PasswordPolicyValidator.validate(password);
        
        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream()
            .anyMatch(error -> error.contains("uppercase letter")));
    }

    @ParameterizedTest
    @ValueSource(strings = {"Password!", "MyStrongPass!", "NoNumbers@"})
    @DisplayName("Deve rejeitar senhas sem dígitos")
    void shouldRejectPasswordsWithoutDigits(String password) {
        PasswordPolicyValidator.ValidationResult result = PasswordPolicyValidator.validate(password);
        
        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream()
            .anyMatch(error -> error.contains("digit")));
    }

    @ParameterizedTest
    @ValueSource(strings = {"Password123", "MyStrongPass1", "NoSpecialChars1"})
    @DisplayName("Deve rejeitar senhas sem caracteres especiais")
    void shouldRejectPasswordsWithoutSpecialChars(String password) {
        PasswordPolicyValidator.ValidationResult result = PasswordPolicyValidator.validate(password);
        
        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream()
            .anyMatch(error -> error.contains("special character")));
    }

    @ParameterizedTest
    @ValueSource(strings = {"123456", "password", "123456789", "qwerty", "admin", "letmein"})
    @DisplayName("Deve rejeitar senhas comuns")
    void shouldRejectCommonPasswords(String commonPassword) {
        PasswordPolicyValidator.ValidationResult result = PasswordPolicyValidator.validate(commonPassword);
        
        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream()
            .anyMatch(error -> error.contains("too common")));
    }

    @ParameterizedTest
    @ValueSource(strings = {"123456Aa!", "QwertyAa1!", "AbcdefGh1!"})
    @DisplayName("Deve rejeitar senhas com padrões sequenciais")
    void shouldRejectSequentialPatterns(String password) {
        PasswordPolicyValidator.ValidationResult result = PasswordPolicyValidator.validate(password);
        
        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream()
            .anyMatch(error -> error.contains("sequential")));
    }

    @ParameterizedTest
    @ValueSource(strings = {"AAAAbbbb1!", "MyPaaaaaass1!", "1234aaaa!"})
    @DisplayName("Deve rejeitar senhas com muitos caracteres repetidos")
    void shouldRejectRepeatedCharacters(String password) {
        PasswordPolicyValidator.ValidationResult result = PasswordPolicyValidator.validate(password);
        
        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream()
            .anyMatch(error -> error.contains("repeated characters")));
    }

    @ParameterizedTest
    @ValueSource(strings = {"UserPass1!", "AdminSystem1!", "BlogApi123!"})
    @DisplayName("Deve rejeitar senhas com informações pessoais")
    void shouldRejectPersonalInformation(String password) {
        PasswordPolicyValidator.ValidationResult result = PasswordPolicyValidator.validate(password);
        
        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream()
            .anyMatch(error -> error.contains("personal information")));
    }

    @Test
    @DisplayName("Deve retornar múltiplos erros de validação para senha fraca")
    void shouldReturnMultipleErrorsForWeakPassword() {
        String weakPassword = "123";
        
        PasswordPolicyValidator.ValidationResult result = PasswordPolicyValidator.validate(weakPassword);
        
        assertFalse(result.isValid());
        assertTrue(result.getErrors().size() > 1);
        
        // Should contain multiple specific errors
        String errorMessage = result.getErrorMessage();
        assertTrue(errorMessage.contains("8 characters"));
        assertTrue(errorMessage.contains("lowercase"));
        assertTrue(errorMessage.contains("uppercase"));
        assertTrue(errorMessage.contains("special character"));
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "MyStr0ng!Pass", "Secure@Password1", "C0mplex!Secret", 
        "Rnd0m$Phrase2", "StrongP@ssw0rd", "Val1d!Password"
    })
    @DisplayName("Deve validar várias senhas fortes")
    void shouldValidateVariousStrongPasswords(String strongPassword) {
        PasswordPolicyValidator.ValidationResult result = PasswordPolicyValidator.validate(strongPassword);
        
        assertTrue(result.isValid(), 
            "Password '" + strongPassword + "' should be valid but got errors: " + result.getErrorMessage());
        assertTrue(result.getErrors().isEmpty());
    }

    @Test
    @DisplayName("Deve fornecer mensagem de erro útil")
    void shouldProvideHelpfulErrorMessage() {
        String weakPassword = "weak";
        
        PasswordPolicyValidator.ValidationResult result = PasswordPolicyValidator.validate(weakPassword);
        
        assertFalse(result.isValid());
        assertFalse(result.getErrorMessage().isEmpty());
        assertTrue(result.getErrorMessage().contains(";"));
    }

    @Test
    @DisplayName("Deve verificar se senha é forte usando método de conveniência")
    void shouldCheckPasswordStrengthUsingConvenienceMethod() {
        assertTrue(PasswordPolicyValidator.isStrongPassword("MyStr0ng!Pass"));
        assertFalse(PasswordPolicyValidator.isStrongPassword("weak"));
        assertFalse(PasswordPolicyValidator.isStrongPassword(null));
    }

    @Test
    @DisplayName("Deve fornecer requisitos de senha")
    void shouldProvidePasswordRequirements() {
        String requirements = PasswordPolicyValidator.getPasswordRequirements();
        
        assertNotNull(requirements);
        assertFalse(requirements.isEmpty());
        assertTrue(requirements.contains("8 characters"));
        assertTrue(requirements.contains("lowercase"));
        assertTrue(requirements.contains("uppercase"));
        assertTrue(requirements.contains("digit"));
        assertTrue(requirements.contains("special character"));
    }

    @Test
    @DisplayName("Deve lidar com casos extremos corretamente")
    void shouldHandleEdgeCases() {
        // Empty string
        PasswordPolicyValidator.ValidationResult emptyResult = PasswordPolicyValidator.validate("");
        assertFalse(emptyResult.isValid());
        
        // Exactly 8 characters with all requirements
        PasswordPolicyValidator.ValidationResult exactResult = PasswordPolicyValidator.validate("Passw0rd!");
        assertTrue(exactResult.isValid());
        
        // Very long password
        PasswordPolicyValidator.ValidationResult longResult = PasswordPolicyValidator.validate("MyVeryLongAndComplexPassword123!@#WithManyCharacters");
        assertTrue(longResult.isValid());
    }
}