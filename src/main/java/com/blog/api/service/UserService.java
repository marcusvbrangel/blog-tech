package com.blog.api.service;

import com.blog.api.dto.UserDTO;
import com.blog.api.entity.User;
import com.blog.api.exception.ResourceNotFoundException;
import com.blog.api.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Cacheable(value = "users", key = "'all:' + #pageable.pageNumber + ':' + #pageable.pageSize")
    public Page<UserDTO> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable)
                .map(UserDTO::fromEntity);
    }

    @Cacheable(value = "users", key = "'single:' + #id")
    public UserDTO getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        return UserDTO.fromEntity(user);
    }

    @Cacheable(value = "users", key = "'username:' + #username")
    public UserDTO getUserByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));
        return UserDTO.fromEntity(user);
    }

    @Caching(evict = {
            @CacheEvict(value = "users", key = "'single:' + #id"),
            @CacheEvict(value = "users", allEntries = true)
    })
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        userRepository.delete(user);
    }
}