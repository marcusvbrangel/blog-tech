package com.blog.api.repository;

import com.blog.api.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("password");
        testUser.setRole(User.Role.USER);
        testUser = entityManager.persistAndFlush(testUser);
    }

    @Test
    void findByUsername_WhenUserExists_ShouldReturnUser() {
        Optional<User> result = userRepository.findByUsername("testuser");
        
        assertThat(result).isPresent();
        assertThat(result.get().getUsername()).isEqualTo("testuser");
        assertThat(result.get().getEmail()).isEqualTo("test@example.com");
    }

    @Test
    void findByUsername_WhenUserNotExists_ShouldReturnEmpty() {
        Optional<User> result = userRepository.findByUsername("nonexistent");
        
        assertThat(result).isEmpty();
    }

    @Test
    void findByEmail_WhenUserExists_ShouldReturnUser() {
        Optional<User> result = userRepository.findByEmail("test@example.com");
        
        assertThat(result).isPresent();
        assertThat(result.get().getUsername()).isEqualTo("testuser");
        assertThat(result.get().getEmail()).isEqualTo("test@example.com");
    }

    @Test
    void findByEmail_WhenUserNotExists_ShouldReturnEmpty() {
        Optional<User> result = userRepository.findByEmail("nonexistent@example.com");
        
        assertThat(result).isEmpty();
    }

    @Test
    void existsByUsername_WhenUserExists_ShouldReturnTrue() {
        boolean result = userRepository.existsByUsername("testuser");
        
        assertThat(result).isTrue();
    }

    @Test
    void existsByUsername_WhenUserNotExists_ShouldReturnFalse() {
        boolean result = userRepository.existsByUsername("nonexistent");
        
        assertThat(result).isFalse();
    }

    @Test
    void existsByEmail_WhenEmailExists_ShouldReturnTrue() {
        boolean result = userRepository.existsByEmail("test@example.com");
        
        assertThat(result).isTrue();
    }

    @Test
    void existsByEmail_WhenEmailNotExists_ShouldReturnFalse() {
        boolean result = userRepository.existsByEmail("nonexistent@example.com");
        
        assertThat(result).isFalse();
    }

    @Test
    void findByUsername_ShouldBeCaseExact() {
        Optional<User> result = userRepository.findByUsername("TESTUSER");
        
        assertThat(result).isEmpty();
    }

    @Test
    void findByEmail_ShouldBeCaseExact() {
        Optional<User> result = userRepository.findByEmail("TEST@EXAMPLE.COM");
        
        assertThat(result).isEmpty();
    }

    @Test
    void save_ShouldPersistUser() {
        User newUser = new User();
        newUser.setUsername("newuser");
        newUser.setEmail("new@example.com");
        newUser.setPassword("newpassword");
        newUser.setRole(User.Role.AUTHOR);

        User savedUser = userRepository.save(newUser);

        assertThat(savedUser.getId()).isNotNull();
        assertThat(savedUser.getUsername()).isEqualTo("newuser");
        assertThat(savedUser.getEmail()).isEqualTo("new@example.com");
        assertThat(savedUser.getRole()).isEqualTo(User.Role.AUTHOR);
        assertThat(savedUser.getCreatedAt()).isNotNull();
    }

    @Test
    void findById_WhenUserExists_ShouldReturnUser() {
        Optional<User> result = userRepository.findById(testUser.getId());
        
        assertThat(result).isPresent();
        assertThat(result.get().getUsername()).isEqualTo("testuser");
    }

    @Test
    void findById_WhenUserNotExists_ShouldReturnEmpty() {
        Optional<User> result = userRepository.findById(999L);
        
        assertThat(result).isEmpty();
    }

    @Test
    void delete_ShouldRemoveUser() {
        Long userId = testUser.getId();
        
        userRepository.delete(testUser);
        entityManager.flush();
        
        Optional<User> result = userRepository.findById(userId);
        assertThat(result).isEmpty();
    }
}