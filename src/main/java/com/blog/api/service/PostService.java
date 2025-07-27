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
import org.springframework.beans.factory.annotation.Autowired;
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

    public Page<PostDTO> getAllPublishedPosts(Pageable pageable) {
        return postRepository.findByPublishedTrue(pageable)
                .map(PostDTO::fromEntity);
    }

    public Page<PostDTO> getPostsByCategory(Long categoryId, Pageable pageable) {
        return postRepository.findByCategoryId(categoryId, pageable)
                .map(PostDTO::fromEntity);
    }

    public Page<PostDTO> getPostsByUser(Long userId, Pageable pageable) {
        return postRepository.findByUserId(userId, pageable)
                .map(PostDTO::fromEntity);
    }

    public Page<PostDTO> searchPosts(String keyword, Pageable pageable) {
        return postRepository.findPublishedPostsByKeyword(keyword, pageable)
                .map(PostDTO::fromEntity);
    }

    public PostDTO getPostById(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Post", "id", id));
        return PostDTO.fromEntity(post);
    }

    public PostDTO createPost(CreatePostDTO createPostDTO, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));

        Category category = null;
        if (createPostDTO.getCategoryId() != null) {
            category = categoryRepository.findById(createPostDTO.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category", "id", createPostDTO.getCategoryId()));
        }

        Post post = new Post();
        post.setTitle(createPostDTO.getTitle());
        post.setContent(createPostDTO.getContent());
        post.setPublished(createPostDTO.isPublished());
        post.setUser(user);
        post.setCategory(category);

        Post savedPost = postRepository.save(post);
        return PostDTO.fromEntity(savedPost);
    }

    public PostDTO updatePost(Long id, CreatePostDTO createPostDTO, String username) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Post", "id", id));

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));

        if (!post.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("You can only update your own posts");
        }

        Category category = null;
        if (createPostDTO.getCategoryId() != null) {
            category = categoryRepository.findById(createPostDTO.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category", "id", createPostDTO.getCategoryId()));
        }

        post.setTitle(createPostDTO.getTitle());
        post.setContent(createPostDTO.getContent());
        post.setPublished(createPostDTO.isPublished());
        post.setCategory(category);

        Post updatedPost = postRepository.save(post);
        return PostDTO.fromEntity(updatedPost);
    }

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