package com.blog.api.dto;

import com.blog.api.entity.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class JsonSerializationTest {

    @Test
    void testUserDTOSerialization() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule()); // For LocalDateTime support
        
        UserDTO userDTO = new UserDTO(
                1L,
                "testuser",
                "test@example.com",
                User.Role.USER,
                LocalDateTime.now(),
                true,
                LocalDateTime.now(),
                null,
                "1.0",
                true
        );
        
        String json = objectMapper.writeValueAsString(userDTO);
        assertNotNull(json);
        System.out.println("JSON: " + json);
        
        UserDTO deserializedUser = objectMapper.readValue(json, UserDTO.class);
        assertNotNull(deserializedUser);
    }

    @Test
    void testUserDTOSerializationWithSpringMapper() {
        try {
            // Use Spring's default ObjectMapper configuration
            ObjectMapper springMapper = new ObjectMapper();
            springMapper.findAndRegisterModules();
            
            UserDTO userDTO = new UserDTO(
                    1L,
                    "testuser",
                    "test@example.com",
                    User.Role.USER,
                    LocalDateTime.now(),
                    true,
                    LocalDateTime.now(),
                    null,
                    "1.0",
                    true
            );
            
            String json = springMapper.writeValueAsString(userDTO);
            System.out.println("Spring JSON: " + json);
            assertNotNull(json);
            
        } catch (Exception e) {
            System.err.println("Error serializing UserDTO: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}