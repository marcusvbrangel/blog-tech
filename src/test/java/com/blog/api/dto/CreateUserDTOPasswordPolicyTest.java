package com.blog.api.dto;

import com.blog.api.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Testes de integração da política de senha do CreateUserDTO")
class CreateUserDTOPasswordPolicyTest {

    @Test
    @DisplayName("Deve criar DTO com sucesso quando senha for forte")
    void shouldCreateDTOWithStrongPassword() {
        assertDoesNotThrow(() -> {
            CreateUserDTO dto = new CreateUserDTO("testuser", "test@example.com", "StrongP@ssw0rd1");
            assertEquals("testuser", dto.username());
            assertEquals("test@example.com", dto.email());
            assertEquals("StrongP@ssw0rd1", dto.password());
            assertEquals(User.Role.USER, dto.role());
        });
    }

    @Test
    @DisplayName("Deve criar DTO com sucesso quando incluir parâmetro de role")
    void shouldCreateDTOWithRole() {
        assertDoesNotThrow(() -> {
            CreateUserDTO dto = new CreateUserDTO("testadmin", "testadmin@example.com", "SecureP@ss123", User.Role.ADMIN);
            assertEquals("testadmin", dto.username());
            assertEquals("testadmin@example.com", dto.email());
            assertEquals("SecureP@ss123", dto.password());
            assertEquals(User.Role.ADMIN, dto.role());
        });
    }

    @ParameterizedTest
    @ValueSource(strings = {"weak", "123456", "Password123"})
    @DisplayName("Deve rejeitar senhas fracas durante criação do DTO")
    void shouldRejectWeakPasswords(String weakPassword) {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            new CreateUserDTO("testuser", "test@example.com", weakPassword);
        });
        
        assertTrue(exception.getMessage().contains("Password policy violation"));
    }

    @Test
    @DisplayName("Deve rejeitar senha nula durante criação do DTO")
    void shouldRejectNullPassword() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            new CreateUserDTO("testuser", "test@example.com", null);
        });
        
        assertTrue(exception.getMessage().contains("Password policy violation"));
        assertTrue(exception.getMessage().contains("cannot be null"));
    }

    @Test
    @DisplayName("Deve fornecer mensagem de erro detalhada para múltiplas violações")
    void shouldProvideDetailedErrorMessage() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            new CreateUserDTO("testuser", "test@example.com", "123");
        });
        
        String message = exception.getMessage();
        assertTrue(message.contains("Password policy violation"));
        assertTrue(message.contains("8 characters"));
        assertTrue(message.contains("lowercase"));
        assertTrue(message.contains("uppercase"));
        assertTrue(message.contains("special character"));
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "MyStr0ng!Pass", "Secure@Password1", "C0mplex!Secret", 
        "Rnd0m$Phrase2", "StrongP@ssw0rd", "Val1d!Password"
    })
    @DisplayName("Deve aceitar várias senhas fortes")
    void shouldAcceptVariousStrongPasswords(String strongPassword) {
        assertDoesNotThrow(() -> {
            CreateUserDTO dto = new CreateUserDTO("testuser", "test@example.com", strongPassword);
            assertEquals(strongPassword, dto.password());
        });
    }

    @Test
    @DisplayName("Deve tratar casos extremos de senhas corretamente")
    void shouldHandleEdgeCasePasswords() {
        // Exactly meets all requirements
        assertDoesNotThrow(() -> {
            new CreateUserDTO("testuser", "test@example.com", "Passw0rd!");
        });
        
        // Very long password
        assertDoesNotThrow(() -> {
            new CreateUserDTO("testuser", "test@example.com", "MyVeryLongAndComplexPassword123!@#WithManyCharacters");
        });
        
        // Empty string should fail
        assertThrows(IllegalArgumentException.class, () -> {
            new CreateUserDTO("testuser", "test@example.com", "");
        });
    }
}