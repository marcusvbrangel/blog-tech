package com.blog.api.util;

import com.blog.api.entity.User;

/**
 * Factory class for creating valid test data objects.
 * Provides data that complies with all security policies including strong password requirements.
 */
public class TestDataFactory {

    // Valid passwords that meet all password policy requirements:
    // - Minimum 8 characters
    // - At least 1 lowercase letter
    // - At least 1 uppercase letter  
    // - At least 1 digit
    // - At least 1 special character
    // - Not in common passwords list
    // - Not a sequential pattern
    public static final String VALID_PASSWORD_1 = "TestPass123!";
    public static final String VALID_PASSWORD_2 = "SecureKey456@";
    public static final String VALID_PASSWORD_3 = "StrongAuth789#";
    public static final String VALID_PASSWORD_4 = "SafeLogin321$";
    public static final String VALID_PASSWORD_5 = "TrustAuth654%";

    /**
     * Creates a valid User builder with strong password for testing.
     * The password meets all security policy requirements.
     * 
     * @return User.Builder with valid test data
     */
    public static User.Builder createValidUserBuilder() {
        return User.newInstance()
                .username("testuser")
                .email("test@example.com")
                .password(VALID_PASSWORD_1)
                .role(User.Role.USER);
    }

    /**
     * Creates a valid User builder with custom username and email.
     * 
     * @param username the username
     * @param email the email
     * @return User.Builder with valid test data
     */
    public static User.Builder createValidUserBuilder(String username, String email) {
        return User.newInstance()
                .username(username)
                .email(email)
                .password(VALID_PASSWORD_1)
                .role(User.Role.USER);
    }

    /**
     * Creates a valid User builder with custom password.
     * Use this when you need a specific password for testing.
     * 
     * @param password the password (should be valid)
     * @return User.Builder with valid test data
     */
    public static User.Builder createValidUserBuilderWithPassword(String password) {
        return User.newInstance()
                .username("testuser")
                .email("test@example.com")
                .password(password)
                .role(User.Role.USER);
    }

    /**
     * Creates a valid admin User builder.
     * 
     * @return User.Builder with admin role and valid test data
     */
    public static User.Builder createValidAdminBuilder() {
        return User.newInstance()
                .username("adminuser")
                .email("admin@example.com")
                .password(VALID_PASSWORD_2)
                .role(User.Role.ADMIN);
    }

    /**
     * Creates a valid author User builder.
     * 
     * @return User.Builder with author role and valid test data
     */
    public static User.Builder createValidAuthorBuilder() {
        return User.newInstance()
                .username("authoruser")
                .email("author@example.com")
                .password(VALID_PASSWORD_3)
                .role(User.Role.AUTHOR);
    }

    /**
     * Creates a complete valid User entity for testing.
     * 
     * @return User entity with valid test data
     */
    public static User createValidUser() {
        return createValidUserBuilder().build();
    }

    /**
     * Creates a complete valid User entity with ID set.
     * 
     * @param id the user ID
     * @return User entity with valid test data and ID
     */
    public static User createValidUserWithId(Long id) {
        User user = createValidUser();
        user.setId(id);
        return user;
    }

    /**
     * Validates that a password meets the security policy.
     * Useful for ensuring test passwords are valid.
     * 
     * @param password the password to validate
     * @return true if password is valid, false otherwise
     */
    public static boolean isValidPassword(String password) {
        PasswordPolicyValidator.ValidationResult result = 
            PasswordPolicyValidator.validate(password);
        return result.isValid();
    }

    /**
     * Gets a random valid password from the predefined list.
     * 
     * @return a valid password for testing
     */
    public static String getRandomValidPassword() {
        String[] passwords = {
            VALID_PASSWORD_1, VALID_PASSWORD_2, VALID_PASSWORD_3, 
            VALID_PASSWORD_4, VALID_PASSWORD_5
        };
        int index = (int) (Math.random() * passwords.length);
        return passwords[index];
    }
}