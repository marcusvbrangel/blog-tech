package com.blog.api.integration;

import com.blog.api.dto.CreateUserDTO;
import com.blog.api.dto.PasswordResetConfirmRequest;
import com.blog.api.entity.User;
import com.blog.api.service.AuthService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("Testes de integração da política de senhas")
class PasswordPolicyIntegrationTest {

    @Autowired
    private AuthService authService;

    @Test
    @DisplayName("Deve aplicar política de senhas durante o registro de usuário")
    void shouldEnforcePasswordPolicyDuringRegistration() {
        // Test with weak password - should fail
        assertThrows(Exception.class, () -> {
            CreateUserDTO weakPasswordUser = new CreateUserDTO("testuser", "test@example.com", "123456");
            authService.register(weakPasswordUser);
        });
        
        // Test with strong password - should succeed
        assertDoesNotThrow(() -> {
            CreateUserDTO strongPasswordUser = new CreateUserDTO("testuser2", "test2@example.com", "StrongP@ssw0rd1!");
            authService.register(strongPasswordUser);
        });
    }

    @Test
    @DisplayName("Deve aplicar política de senhas durante a redefinição de senha")
    void shouldEnforcePasswordPolicyDuringPasswordReset() {
        // Test weak password in PasswordResetConfirmRequest - should fail at DTO level
        assertThrows(IllegalArgumentException.class, () -> {
            new PasswordResetConfirmRequest("token123", "weakpass");
        });
        
        // Test strong password - should create DTO successfully
        assertDoesNotThrow(() -> {
            new PasswordResetConfirmRequest("token123", "NewStr0ng!P@ss");
        });
    }

    @Test
    @DisplayName("Deve validar várias regras da política de senhas")
    void shouldValidateVariousPasswordPolicyRules() {
        // Test minimum length
        assertThrows(IllegalArgumentException.class, () -> {
            new CreateUserDTO("user1", "user1@test.com", "Aa1!");
        });
        
        // Test missing uppercase
        assertThrows(IllegalArgumentException.class, () -> {
            new CreateUserDTO("user2", "user2@test.com", "lowercase123!");
        });
        
        // Test missing lowercase
        assertThrows(IllegalArgumentException.class, () -> {
            new CreateUserDTO("user3", "user3@test.com", "UPPERCASE123!");
        });
        
        // Test missing digit
        assertThrows(IllegalArgumentException.class, () -> {
            new CreateUserDTO("user4", "user4@test.com", "NoDigits!");
        });
        
        // Test missing special character
        assertThrows(IllegalArgumentException.class, () -> {
            new CreateUserDTO("user5", "user5@test.com", "NoSpecial123");
        });
        
        // Test common password
        assertThrows(IllegalArgumentException.class, () -> {
            new CreateUserDTO("user6", "user6@test.com", "TestPass123!");
        });
        
        // Test valid strong password
        assertDoesNotThrow(() -> {
            new CreateUserDTO("user7", "user7@test.com", "MySecur3!P@ssw0rd");
        });
    }

    @Test
    @DisplayName("Deve permitir que User Builder funcione com senhas criptografadas")
    void shouldAllowUserBuilderWithEncryptedPasswords() {
        // This should work for encrypted passwords
        assertDoesNotThrow(() -> {
            User user = User.ofEncrypted("testuser", "test@example.com", "$2a$10$encryptedPasswordHash")
                    .role(User.Role.USER)
                    .build();
            
            assertNotNull(user);
            assertEquals("testuser", user.getUsername());
        });
        
        // But raw password validation should still work for plain text
        assertDoesNotThrow(() -> {
            User user = User.of("testuser2", "test2@example.com", "ValidP@ssw0rd1")
                    .role(User.Role.USER)
                    .build();
            
            assertNotNull(user);
            assertEquals("testuser2", user.getUsername());
        });
    }
}