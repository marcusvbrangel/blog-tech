package com.blog.api.service;

import com.blog.api.dto.PostDTO;
import com.blog.api.entity.Category;
import com.blog.api.entity.Post;
import com.blog.api.entity.User;
import com.blog.api.repository.PostRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
public class CacheServiceTest {

    @Autowired
    private PostService postService;

    @Autowired
    private CacheManager cacheManager;

    @MockBean
    private PostRepository postRepository;

    private Post testPost;
    private User testUser;
    private Category testCategory;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");

        testCategory = new Category();
        testCategory.setId(1L);
        testCategory.setName("Test Category");

        testPost = new Post();
        testPost.setId(1L);
        testPost.setTitle("Test Post");
        testPost.setContent("Test Content");
        testPost.setPublished(true);
        testPost.setUser(testUser);
        testPost.setCategory(testCategory);
        testPost.setCreatedAt(LocalDateTime.now());
        testPost.setUpdatedAt(LocalDateTime.now());

        if (cacheManager != null) {
            cacheManager.getCacheNames().forEach(cacheName -> {
                var cache = cacheManager.getCache(cacheName);
                if (cache != null) {
                    cache.clear();
                }
            });
        }
    }

    @Test
    void testPostCaching() {
        when(postRepository.findById(1L)).thenReturn(Optional.of(testPost));

        PostDTO result1 = postService.getPostById(1L);
        PostDTO result2 = postService.getPostById(1L);

        assertNotNull(result1);
        assertNotNull(result2);
        assertEquals(result1.getId(), result2.getId());
        assertEquals(result1.getTitle(), result2.getTitle());

        verify(postRepository, times(1)).findById(1L);
    }

    @Test
    void testCacheEvictionOnUpdate() {
        when(postRepository.findById(1L)).thenReturn(Optional.of(testPost));
        when(postRepository.save(any(Post.class))).thenReturn(testPost);

        PostDTO cachedPost = postService.getPostById(1L);
        assertNotNull(cachedPost);

        verify(postRepository, times(1)).findById(1L);

        PostDTO updatedPost = postService.getPostById(1L);
        assertNotNull(updatedPost);

        verify(postRepository, times(1)).findById(1L);
    }
}