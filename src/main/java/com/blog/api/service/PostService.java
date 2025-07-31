package com.blog.api.service;

import com.blog.api.dto.CreatePostDTO;
import com.blog.api.dto.PostDTO;
import com.blog.api.entity.Category;
import com.blog.api.entity.Post;
import com.blog.api.entity.User;
import com.blog.api.exception.ResourceNotFoundException;
import com.blog.api.repository.CategoryRepository;
import com.blog.api.repository.PostRepository;
import com.blog.api.repository.UserRepository;
import io.micrometer.core.annotation.Timed;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Timer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class PostService {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private Counter postCreationCounter;

    @Autowired
    private Timer databaseQueryTimer;

    @Cacheable(value = "posts", key = "'all:' + #pageable.pageNumber + ':' + #pageable.pageSize")
    @Timed(value = "blog_api_posts_get_all", description = "Time taken to get all published posts")
    public Page<PostDTO> getAllPublishedPosts(Pageable pageable) {
        return postRepository.findByPublishedTrue(pageable)
                .map(PostDTO::fromEntity);
    }

    @Cacheable(value = "posts", key = "'category:' + #categoryId + ':' + #pageable.pageNumber + ':' + #pageable.pageSize")
    public Page<PostDTO> getPostsByCategory(Long categoryId, Pageable pageable) {
        return postRepository.findByCategoryId(categoryId, pageable)
                .map(PostDTO::fromEntity);
    }

    @Cacheable(value = "posts", key = "'user:' + #userId + ':' + #pageable.pageNumber + ':' + #pageable.pageSize")
    public Page<PostDTO> getPostsByUser(Long userId, Pageable pageable) {
        return postRepository.findByUserId(userId, pageable)
                .map(PostDTO::fromEntity);
    }

    public Page<PostDTO> searchPosts(String keyword, Pageable pageable) {
        return postRepository.findPublishedPostsByKeyword(keyword, pageable)
                .map(PostDTO::fromEntity);
    }

    @Cacheable(value = "posts", key = "'single:' + #id")
    public PostDTO getPostById(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Post", "id", id));
        return PostDTO.fromEntity(post);
    }

    @CacheEvict(value = "posts", allEntries = true)
    @Timed(value = "blog_api_posts_create", description = "Time taken to create a post")
    public PostDTO createPost(CreatePostDTO createPostDTO, String username) {
        postCreationCounter.increment();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));

        Category category = null;
        if (createPostDTO.categoryId() != null) {
            category = categoryRepository.findById(createPostDTO.categoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category", "id", createPostDTO.categoryId()));
        }

        Post post = Post.of(createPostDTO.title(), createPostDTO.content(), user)
                .published(createPostDTO.published())
                .category(category)
                .build();

        Post savedPost = postRepository.save(post);
        return PostDTO.fromEntity(savedPost);
    }

    @Caching(evict = {
            @CacheEvict(value = "posts", key = "'single:' + #id"),
            @CacheEvict(value = "posts", allEntries = true)
    })
    public PostDTO updatePost(Long id, CreatePostDTO createPostDTO, String username) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Post", "id", id));

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));

        if (!post.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("You can only update your own posts");
        }

        Category category = null;
        if (createPostDTO.categoryId() != null) {
            category = categoryRepository.findById(createPostDTO.categoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category", "id", createPostDTO.categoryId()));
        }

        Post updatedPost = Post.from(post)
                .title(createPostDTO.title())
                .content(createPostDTO.content())
                .published(createPostDTO.published())
                .category(category)
                .build();
        updatedPost.setId(post.getId());
        updatedPost.setCreatedAt(post.getCreatedAt());
        post = updatedPost;

        Post savedPost = postRepository.save(post);
        return PostDTO.fromEntity(savedPost);
    }

    @Caching(evict = {
            @CacheEvict(value = "posts", key = "'single:' + #id"),
            @CacheEvict(value = "posts", allEntries = true)
    })
    public void deletePost(Long id, String username) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Post", "id", id));

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));

        if (!post.getUser().getId().equals(user.getId()) && !user.getRole().equals(User.Role.ADMIN)) {
            throw new RuntimeException("You can only delete your own posts");
        }

        postRepository.delete(post);
    }
}