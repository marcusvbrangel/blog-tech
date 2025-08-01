package com.blog.api.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Password Policy Validator Tests")
class PasswordPolicyValidatorTest {

    @Test
    @DisplayName("Should validate strong password successfully")
    void shouldValidateStrongPasswordSuccessfully() {
        String strongPassword = "MyStr0ng!Pass";
        
        PasswordPolicyValidator.ValidationResult result = PasswordPolicyValidator.validate(strongPassword);
        
        assertTrue(result.isValid());
        assertTrue(result.getErrors().isEmpty());
    }

    @Test
    @DisplayName("Should reject null password")
    void shouldRejectNullPassword() {
        PasswordPolicyValidator.ValidationResult result = PasswordPolicyValidator.validate(null);
        
        assertFalse(result.isValid());
        assertEquals(1, result.getErrors().size());
        assertTrue(result.getErrors().get(0).contains("cannot be null"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"1234567", "short", "a", "Ab1@"})
    @DisplayName("Should reject passwords shorter than 8 characters")
    void shouldRejectShortPasswords(String shortPassword) {
        PasswordPolicyValidator.ValidationResult result = PasswordPolicyValidator.validate(shortPassword);
        
        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream()
            .anyMatch(error -> error.contains("at least 8 characters")));
    }

    @ParameterizedTest
    @ValueSource(strings = {"PASSWORD123!", "MYSTRONG!PASS", "UPPERCASE1@"})
    @DisplayName("Should reject passwords without lowercase letters")
    void shouldRejectPasswordsWithoutLowercase(String password) {
        PasswordPolicyValidator.ValidationResult result = PasswordPolicyValidator.validate(password);
        
        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream()
            .anyMatch(error -> error.contains("lowercase letter")));
    }

    @ParameterizedTest
    @ValueSource(strings = {"password123!", "mystrong!pass", "lowercase1@"})
    @DisplayName("Should reject passwords without uppercase letters")
    void shouldRejectPasswordsWithoutUppercase(String password) {
        PasswordPolicyValidator.ValidationResult result = PasswordPolicyValidator.validate(password);
        
        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream()
            .anyMatch(error -> error.contains("uppercase letter")));
    }

    @ParameterizedTest
    @ValueSource(strings = {"Password!", "MyStrongPass!", "NoNumbers@"})
    @DisplayName("Should reject passwords without digits")
    void shouldRejectPasswordsWithoutDigits(String password) {
        PasswordPolicyValidator.ValidationResult result = PasswordPolicyValidator.validate(password);
        
        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream()
            .anyMatch(error -> error.contains("digit")));
    }

    @ParameterizedTest
    @ValueSource(strings = {"Password123", "MyStrongPass1", "NoSpecialChars1"})
    @DisplayName("Should reject passwords without special characters")
    void shouldRejectPasswordsWithoutSpecialChars(String password) {
        PasswordPolicyValidator.ValidationResult result = PasswordPolicyValidator.validate(password);
        
        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream()
            .anyMatch(error -> error.contains("special character")));
    }

    @ParameterizedTest
    @ValueSource(strings = {"123456", "password", "123456789", "qwerty", "admin", "letmein"})
    @DisplayName("Should reject common passwords")
    void shouldRejectCommonPasswords(String commonPassword) {
        PasswordPolicyValidator.ValidationResult result = PasswordPolicyValidator.validate(commonPassword);
        
        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream()
            .anyMatch(error -> error.contains("too common")));
    }

    @ParameterizedTest
    @ValueSource(strings = {"123456Aa!", "QwertyAa1!", "AbcdefGh1!"})
    @DisplayName("Should reject passwords with sequential patterns")
    void shouldRejectSequentialPatterns(String password) {
        PasswordPolicyValidator.ValidationResult result = PasswordPolicyValidator.validate(password);
        
        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream()
            .anyMatch(error -> error.contains("sequential")));
    }

    @ParameterizedTest
    @ValueSource(strings = {"AAAAbbbb1!", "MyPaaaaaass1!", "1234aaaa!"})
    @DisplayName("Should reject passwords with too many repeated characters")
    void shouldRejectRepeatedCharacters(String password) {
        PasswordPolicyValidator.ValidationResult result = PasswordPolicyValidator.validate(password);
        
        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream()
            .anyMatch(error -> error.contains("repeated characters")));
    }

    @ParameterizedTest
    @ValueSource(strings = {"UserPass1!", "AdminSystem1!", "BlogApi123!"})
    @DisplayName("Should reject passwords with personal information")
    void shouldRejectPersonalInformation(String password) {
        PasswordPolicyValidator.ValidationResult result = PasswordPolicyValidator.validate(password);
        
        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream()
            .anyMatch(error -> error.contains("personal information")));
    }

    @Test
    @DisplayName("Should return multiple validation errors for weak password")
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
    @DisplayName("Should validate various strong passwords")
    void shouldValidateVariousStrongPasswords(String strongPassword) {
        PasswordPolicyValidator.ValidationResult result = PasswordPolicyValidator.validate(strongPassword);
        
        assertTrue(result.isValid(), 
            "Password '" + strongPassword + "' should be valid but got errors: " + result.getErrorMessage());
        assertTrue(result.getErrors().isEmpty());
    }

    @Test
    @DisplayName("Should provide helpful error message")
    void shouldProvideHelpfulErrorMessage() {
        String weakPassword = "weak";
        
        PasswordPolicyValidator.ValidationResult result = PasswordPolicyValidator.validate(weakPassword);
        
        assertFalse(result.isValid());
        assertFalse(result.getErrorMessage().isEmpty());
        assertTrue(result.getErrorMessage().contains(";"));
    }

    @Test
    @DisplayName("Should check if password is strong using convenience method")
    void shouldCheckPasswordStrengthUsingConvenienceMethod() {
        assertTrue(PasswordPolicyValidator.isStrongPassword("MyStr0ng!Pass"));
        assertFalse(PasswordPolicyValidator.isStrongPassword("weak"));
        assertFalse(PasswordPolicyValidator.isStrongPassword(null));
    }

    @Test
    @DisplayName("Should provide password requirements")
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
    @DisplayName("Should handle edge cases correctly")
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