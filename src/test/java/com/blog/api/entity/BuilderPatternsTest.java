package com.blog.api.entity;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@DisplayName("Builder Patterns Tests")
class BuilderPatternsTest {

    @Test
    @DisplayName("User Builder - Deve criar usuário com validações")
    void userBuilder_ShouldCreateUserWithValidations() {
        // Given
        String username = "johndoe";
        String email = "john@example.com";
        String password = "StrongP@ssw0rd1";

        // When
        User user = User.of(username, email, password)
                .role(User.Role.AUTHOR)
                .emailVerified(true)
                .build();

        // Then
        assertNotNull(user);
        assertEquals(username, user.getUsername());
        assertEquals(email.toLowerCase(), user.getEmail());
        assertEquals(password, user.getPassword());
        assertEquals(User.Role.AUTHOR, user.getRole());
        assertTrue(user.isEmailVerified());
        assertNotNull(user.getEmailVerifiedAt());
    }

    @Test
    @DisplayName("User Builder - Deve falhar com dados inválidos")
    void userBuilder_ShouldFailWithInvalidData() {
        // Given & When & Then
        assertThrows(NullPointerException.class, () -> 
            User.newInstance().username(null));
        
        assertThrows(IllegalArgumentException.class, () -> 
            User.newInstance().username("ab")); // muito curto
        
        assertThrows(IllegalArgumentException.class, () -> 
            User.newInstance().email("invalid-email"));
    }

    @Test
    @DisplayName("Post Builder - Deve criar post com validações")
    void postBuilder_ShouldCreatePostWithValidations() {
        // Given
        User author = User.of("author", "author@test.com", "password123").build();
        Category category = Category.of("Technology").build();

        // When
        Post post = Post.of("Test Title", "This is a test content with more than 10 characters")
                .user(author)
                .category(category)
                .published(true)
                .build();

        // Then
        assertNotNull(post);
        assertEquals("Test Title", post.getTitle());
        assertTrue(post.isPublished());
        assertEquals(author, post.getUser());
        assertEquals(category, post.getCategory());
    }

    @Test
    @DisplayName("Post Builder - Factory methods semânticos")
    void postBuilder_SemanticFactoryMethods() {
        // Given
        User author = User.asAuthor().username("author").email("author@test.com").password("pass123").build();
        
        // When - Draft
        Post draft = Post.draft("Draft Title", "Draft content for testing", author).build();
        
        // When - Published
        Post published = Post.published("Published Title", "Published content for testing", author).build();

        // Then
        assertFalse(draft.isPublished());
        assertTrue(published.isPublished());
    }

    @Test
    @DisplayName("VerificationToken Builder - Deve criar tokens com tipos específicos")
    void verificationTokenBuilder_ShouldCreateSpecificTokenTypes() {
        // Given
        User user = User.withDefaults().username("user").email("user@test.com").password("pass123").build();
        String tokenValue = "abc123";

        // When - Email Verification
        VerificationToken emailToken = VerificationToken.forEmailVerification(user, tokenValue).build();
        
        // When - Password Reset
        VerificationToken resetToken = VerificationToken.forPasswordReset(user, tokenValue + "reset").build();

        // Then
        assertEquals(VerificationToken.TokenType.EMAIL_VERIFICATION, emailToken.getTokenType());
        assertEquals(VerificationToken.TokenType.PASSWORD_RESET, resetToken.getTokenType());
        assertEquals(tokenValue, emailToken.getToken());
        assertTrue(emailToken.getExpiresAt().isAfter(LocalDateTime.now()));
        assertTrue(resetToken.getExpiresAt().isBefore(emailToken.getExpiresAt())); // Reset expira antes
    }

    @Test
    @DisplayName("Comment Builder - Deve criar comentários e replies")
    void commentBuilder_ShouldCreateCommentsAndReplies() {
        // Given
        User author = User.asAuthor().username("author").email("author@test.com").password("pass123").build();
        User commenter = User.withDefaults().username("commenter").email("commenter@test.com").password("pass123").build();
        
        Category category = Category.of("Tech").build();
        Post post = Post.of("Test Post", "Content for testing comments", author).category(category).build();

        // When - Comment
        Comment comment = Comment.comment("Great post!", post, commenter).build();
        
        // When - Reply
        Comment reply = Comment.reply("Thanks for the feedback!", comment, author).build();

        // Then
        assertEquals("Great post!", comment.getContent());
        assertEquals(post, comment.getPost());
        assertEquals(commenter, comment.getUser());
        assertNull(comment.getParent());

        assertEquals("Thanks for the feedback!", reply.getContent());
        assertEquals(post, reply.getPost()); // Inherited from parent
        assertEquals(author, reply.getUser());
        assertEquals(comment, reply.getParent());
    }

    @Test
    @DisplayName("Category Builder - Deve criar categorias com validações")
    void categoryBuilder_ShouldCreateCategoriesWithValidations() {
        // When
        Category category = Category.of("Technology", "All about tech")
                .build();

        // Then
        assertNotNull(category);
        assertEquals("Technology", category.getName());
        assertEquals("All about tech", category.getDescription());
    }

    @Test
    @DisplayName("Factory Methods - Deve copiar entidades existentes")
    void factoryMethods_ShouldCopyExistingEntities() {
        // Given
        User originalUser = User.asAdmin()
                .username("admin")
                .email("admin@test.com")
                .password("admin123")
                .build();

        // When
        User copiedUser = User.from(originalUser)
                .username("admin2") // Modificar apenas username
                .build();

        // Then
        assertEquals("admin2", copiedUser.getUsername());
        assertEquals(originalUser.getEmail(), copiedUser.getEmail());
        assertEquals(originalUser.getPassword(), copiedUser.getPassword());
        assertEquals(originalUser.getRole(), copiedUser.getRole());
    }

    @Test
    @DisplayName("Business Rules - Deve aplicar regras de negócio")
    void businessRules_ShouldApplyCorrectly() {
        // Given
        User user = User.withDefaults().username("user").email("user@test.com").password("pass123").build();
        Post post = Post.of("Title", "Content for testing", user).build();
        Comment parentComment = Comment.of("Parent comment", post, user).build();

        // When & Then - Reply deve herdar post do parent
        Comment reply = Comment.asReply(parentComment)
                .content("Reply content")
                .user(user)
                .build();

        assertEquals(post, reply.getPost());
        assertEquals(parentComment, reply.getParent());
    }
}