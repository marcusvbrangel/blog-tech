package com.blog.api.repository;

import com.blog.api.entity.Category;
import com.blog.api.entity.Post;
import com.blog.api.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class PostRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private PostRepository postRepository;

    private User testUser;
    private Category testCategory;
    private Post publishedPost;
    private Post draftPost;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("password");
        testUser.setRole(User.Role.AUTHOR);
        testUser = entityManager.persistAndFlush(testUser);

        testCategory = new Category();
        testCategory.setName("Technology");
        testCategory.setDescription("Tech posts");
        testCategory = entityManager.persistAndFlush(testCategory);

        publishedPost = new Post();
        publishedPost.setTitle("Published Post");
        publishedPost.setContent("This is a published post about Java and Spring");
        publishedPost.setPublished(true);
        publishedPost.setUser(testUser);
        publishedPost.setCategory(testCategory);
        publishedPost = entityManager.persistAndFlush(publishedPost);

        draftPost = new Post();
        draftPost.setTitle("Draft Post");
        draftPost.setContent("This is a draft post");
        draftPost.setPublished(false);
        draftPost.setUser(testUser);
        draftPost.setCategory(testCategory);
        draftPost = entityManager.persistAndFlush(draftPost);
    }

    @Test
    void findByPublishedTrue_ShouldReturnOnlyPublishedPosts() {
        Pageable pageable = PageRequest.of(0, 10);
        
        Page<Post> result = postRepository.findByPublishedTrue(pageable);
        
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("Published Post");
        assertThat(result.getContent().get(0).isPublished()).isTrue();
    }

    @Test
    void findByUserId_ShouldReturnPostsByUser() {
        Pageable pageable = PageRequest.of(0, 10);
        
        Page<Post> result = postRepository.findByUserId(testUser.getId(), pageable);
        
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent()).extracting(Post::getUser)
                .allMatch(user -> user.getId().equals(testUser.getId()));
    }

    @Test
    void findByCategoryId_ShouldReturnPostsByCategory() {
        Pageable pageable = PageRequest.of(0, 10);
        
        Page<Post> result = postRepository.findByCategoryId(testCategory.getId(), pageable);
        
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent()).extracting(Post::getCategory)
                .allMatch(category -> category.getId().equals(testCategory.getId()));
    }

    @Test
    void findPublishedPostsByKeyword_ShouldReturnMatchingPosts() {
        Pageable pageable = PageRequest.of(0, 10);
        
        Page<Post> result = postRepository.findPublishedPostsByKeyword("Java", pageable);
        
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("Published Post");
        assertThat(result.getContent().get(0).getContent()).contains("Java");
    }

    @Test
    void findPublishedPostsByKeyword_ShouldBeCaseInsensitive() {
        Pageable pageable = PageRequest.of(0, 10);
        
        Page<Post> result = postRepository.findPublishedPostsByKeyword("SPRING", pageable);
        
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getContent()).containsIgnoringCase("spring");
    }

    @Test
    void findPublishedPostsByKeyword_ShouldSearchInTitle() {
        Pageable pageable = PageRequest.of(0, 10);
        
        Page<Post> result = postRepository.findPublishedPostsByKeyword("Published", pageable);
        
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getTitle()).contains("Published");
    }

    @Test
    void findPublishedPostsByKeyword_ShouldNotReturnDrafts() {
        // Add keyword to draft post content
        draftPost.setContent("This draft contains Java keyword");
        entityManager.persistAndFlush(draftPost);
        
        Pageable pageable = PageRequest.of(0, 10);
        Page<Post> result = postRepository.findPublishedPostsByKeyword("Java", pageable);
        
        // Should only return the published post, not the draft
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).isPublished()).isTrue();
    }

    @Test
    void findUserIdAndPublishedTrue_ShouldReturnPublishedPostsByUser() {
        List<Post> result = postRepository.findByUserIdAndPublishedTrue(testUser.getId());
        
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("Published Post");
        assertThat(result.get(0).isPublished()).isTrue();
        assertThat(result.get(0).getUser().getId()).isEqualTo(testUser.getId());
    }

    @Test
    void findPublishedPostsByKeyword_WhenNoMatch_ShouldReturnEmptyPage() {
        Pageable pageable = PageRequest.of(0, 10);
        
        Page<Post> result = postRepository.findPublishedPostsByKeyword("NonExistentKeyword", pageable);
        
        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isEqualTo(0);
    }

    @Test
    void findByUserId_WhenUserHasNoPosts_ShouldReturnEmptyPage() {
        // Create new user with no posts
        User newUser = new User();
        newUser.setUsername("newuser");
        newUser.setEmail("new@example.com");
        newUser.setPassword("password");
        newUser.setRole(User.Role.USER);
        newUser = entityManager.persistAndFlush(newUser);
        
        Pageable pageable = PageRequest.of(0, 10);
        Page<Post> result = postRepository.findByUserId(newUser.getId(), pageable);
        
        assertThat(result.getContent()).isEmpty();
    }

    @Test
    void findByCategoryId_WhenCategoryHasNoPosts_ShouldReturnEmptyPage() {
        // Create new category with no posts
        Category newCategory = new Category();
        newCategory.setName("Empty Category");
        newCategory.setDescription("No posts here");
        newCategory = entityManager.persistAndFlush(newCategory);
        
        Pageable pageable = PageRequest.of(0, 10);
        Page<Post> result = postRepository.findByCategoryId(newCategory.getId(), pageable);
        
        assertThat(result.getContent()).isEmpty();
    }
}