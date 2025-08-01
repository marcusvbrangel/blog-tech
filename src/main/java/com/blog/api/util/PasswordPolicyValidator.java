package com.blog.api.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

public class PasswordPolicyValidator {
    
    private static final int MIN_LENGTH = 8;
    private static final Pattern LOWERCASE_PATTERN = Pattern.compile("[a-z]");
    private static final Pattern UPPERCASE_PATTERN = Pattern.compile("[A-Z]");
    private static final Pattern DIGIT_PATTERN = Pattern.compile("[0-9]");
    private static final Pattern SPECIAL_CHAR_PATTERN = Pattern.compile("[!@#$%^&*()_+\\-=\\[\\]{}|;:,.<>?]");
    
    private static final Set<String> COMMON_PASSWORDS = Set.of(
        "123456", "password", "123456789", "12345678", "12345", "1234567", "1234567890",
        "qwerty", "abc123", "111111", "123123", "admin", "letmein", "welcome", "monkey",
        "1234", "dragon", "master", "hello", "login", "pass", "admin123", "qwerty123",
        "password123", "welcome123", "user", "root", "toor", "test", "guest", "info",
        "adm", "mysql", "user123", "administrator", "root123", "pass123", "admin1",
        "test123", "oracle", "postgres", "ubuntu", "jenkins", "minecraft"
    );
    
    private static final Set<String> SEQUENTIAL_PATTERNS = Set.of(
        "123456", "654321", "1234567", "7654321", "12345678", "87654321",
        "abcdef", "fedcba", "abcdefg", "gfedcba", "abcdefgh", "hgfedcba",
        "qwerty", "ytrewq", "qwertyui", "iuytrewq", "asdfgh", "hgfdsa",
        "zxcvbn", "nbvcxz", "qazwsx", "xswzaq", "147258", "852741"
    );

    public static class ValidationResult {
        private final boolean isValid;
        private final List<String> errors;

        public ValidationResult(boolean isValid, List<String> errors) {
            this.isValid = isValid;
            this.errors = errors;
        }

        public boolean isValid() {
            return isValid;
        }

        public List<String> getErrors() {
            return errors;
        }

        public String getErrorMessage() {
            return String.join("; ", errors);
        }
    }

    public static ValidationResult validate(String password) {
        if (password == null) {
            return new ValidationResult(false, Arrays.asList("Password cannot be null"));
        }

        List<String> errors = new ArrayList<>();

        if (password.length() < MIN_LENGTH) {
            errors.add("Password must be at least " + MIN_LENGTH + " characters long");
        }

        if (!LOWERCASE_PATTERN.matcher(password).find()) {
            errors.add("Password must contain at least one lowercase letter");
        }

        if (!UPPERCASE_PATTERN.matcher(password).find()) {
            errors.add("Password must contain at least one uppercase letter");
        }

        if (!DIGIT_PATTERN.matcher(password).find()) {
            errors.add("Password must contain at least one digit");
        }

        if (!SPECIAL_CHAR_PATTERN.matcher(password).find()) {
            errors.add("Password must contain at least one special character (!@#$%^&*()_+-=[]{}|;:,.<>?)");
        }

        if (COMMON_PASSWORDS.contains(password.toLowerCase())) {
            errors.add("Password is too common and easily guessable");
        }

        if (containsSequentialPatterns(password)) {
            errors.add("Password contains sequential or repetitive patterns");
        }

        if (containsRepeatedCharacters(password)) {
            errors.add("Password contains too many repeated characters");
        }

        if (containsUserInformation(password)) {
            errors.add("Password should not contain obvious personal information");
        }

        return new ValidationResult(errors.isEmpty(), errors);
    }

    private static boolean containsSequentialPatterns(String password) {
        String lowerPassword = password.toLowerCase();
        
        for (String pattern : SEQUENTIAL_PATTERNS) {
            if (lowerPassword.contains(pattern)) {
                return true;
            }
        }
        return false;
    }

    private static boolean containsRepeatedCharacters(String password) {
        if (password.length() < 4) return false;
        
        for (int i = 0; i <= password.length() - 4; i++) {
            char c = password.charAt(i);
            if (password.charAt(i + 1) == c && 
                password.charAt(i + 2) == c && 
                password.charAt(i + 3) == c) {
                return true;
            }
        }
        return false;
    }

    private static boolean containsUserInformation(String password) {
        String lowerPassword = password.toLowerCase();
        
        String[] commonPersonalPatterns = {
            "user", "admin", "name", "email", "blog", "api", "system", "service"
        };
        
        for (String pattern : commonPersonalPatterns) {
            if (lowerPassword.contains(pattern)) {
                return true;
            }
        }
        
        return false;
    }

    public static boolean isStrongPassword(String password) {
        return validate(password).isValid();
    }

    public static String getPasswordRequirements() {
        return "Password must:\n" +
               "- Be at least " + MIN_LENGTH + " characters long\n" +
               "- Contain at least one lowercase letter (a-z)\n" +
               "- Contain at least one uppercase letter (A-Z)\n" +
               "- Contain at least one digit (0-9)\n" +
               "- Contain at least one special character (!@#$%^&*()_+-=[]{}|;:,.<>?)\n" +
               "- Not be a common or easily guessable password\n" +
               "- Not contain sequential or repetitive patterns\n" +
               "- Not contain obvious personal information";
    }
}