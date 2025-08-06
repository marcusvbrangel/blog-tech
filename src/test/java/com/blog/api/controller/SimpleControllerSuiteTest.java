package com.blog.api.controller;

import com.blog.api.dto.*;
import com.blog.api.entity.User;
import com.blog.api.service.*;
import com.blog.api.service.AuditLogService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import jakarta.servlet.http.HttpServletRequest;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Suíte de testes simples dos controladores")
class SimpleControllerSuiteTest {

    // UserController Tests
    @Mock
    private UserService userService;
    
    @InjectMocks
    private UserController userController;

    // CommentController Tests  
    @Mock
    private CommentService commentService;
    
    @InjectMocks
    private CommentController commentController;

    // CategoryController Tests
    @Mock
    private CategoryService categoryService;
    
    @InjectMocks
    private CategoryController categoryController;

    // AuthController Tests
    @Mock
    private AuthService authService;

    @Mock
    private AuditLogService auditLogService;
    
    @InjectMocks
    private AuthController authController;

    @Mock
    private Authentication authentication;

    @Mock
    private HttpServletRequest mockRequest;

    private UserDTO sampleUserDTO;
    private CommentDTO sampleCommentDTO;
    private CategoryDTO sampleCategoryDTO;
    private CreateUserDTO createUserDTO;
    private LoginRequest loginRequest;
    private JwtResponse jwtResponse;

    @BeforeEach
    void setUp() {
        sampleUserDTO = new UserDTO(
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

        sampleCommentDTO = new CommentDTO(
                1L,
                "Test comment",
                LocalDateTime.now(),
                "testuser",
                1L,
                null,
                new java.util.ArrayList<>(Arrays.asList())
        );

        sampleCategoryDTO = new CategoryDTO(1L, "Technology", "Tech posts", 5);
        
        createUserDTO = new CreateUserDTO("testuser", "test@example.com", "ValidPassword123!", User.Role.USER);
        loginRequest = new LoginRequest("testuser", "ValidPassword123!");
        jwtResponse = new JwtResponse("jwt-token-123", sampleUserDTO);
    }

    // UserController Tests
    @Test
    @DisplayName("Deve retornar página de usuários quando controlador de usuários solicitar todos os usuários")
    void userController_getAllUsers_ShouldReturnPageOfUsers() {
        // Arrange
        Page<UserDTO> page = new PageImpl<>(new java.util.ArrayList<>(Arrays.asList(sampleUserDTO)));
        when(userService.getAllUsers(any())).thenReturn(page);

        // Act
        ResponseEntity<Page<UserDTO>> response = userController.getAllUsers(PageRequest.of(0, 10));

        // Assert
        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getContent()).hasSize(1);
        assertThat(response.getBody().getContent().get(0).username()).isEqualTo("testuser");
        
        verify(userService).getAllUsers(any());
    }

    @Test
    @DisplayName("Deve retornar usuário quando controlador de usuários buscar por ID")
    void userController_getUserById_ShouldReturnUser() {
        // Arrange
        when(userService.getUserById(1L)).thenReturn(sampleUserDTO);

        // Act
        ResponseEntity<UserDTO> response = userController.getUserById(1L);

        // Assert
        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().username()).isEqualTo("testuser");
        
        verify(userService).getUserById(1L);
    }

    @Test
    @DisplayName("Deve retornar usuário quando controlador de usuários buscar por nome de usuário")
    void userController_getUserByUsername_ShouldReturnUser() {
        // Arrange
        when(userService.getUserByUsername("testuser")).thenReturn(sampleUserDTO);

        // Act
        ResponseEntity<UserDTO> response = userController.getUserByUsername("testuser");

        // Assert
        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().username()).isEqualTo("testuser");
        
        verify(userService).getUserByUsername("testuser");
    }

    @Test
    @DisplayName("Deve retornar sem conteúdo quando controlador de usuários deletar usuário")
    void userController_deleteUser_ShouldReturnNoContent() {
        // Arrange
        doNothing().when(userService).deleteUser(1L);

        // Act
        ResponseEntity<Void> response = userController.deleteUser(1L);

        // Assert
        assertThat(response.getStatusCodeValue()).isEqualTo(204);
        
        verify(userService).deleteUser(1L);
    }

    // CommentController Tests
    @Test
    @DisplayName("Deve retornar página de comentários quando controlador de comentários buscar por post")
    void commentController_getCommentsByPost_ShouldReturnPageOfComments() {
        // Arrange
        Page<CommentDTO> page = new PageImpl<>(new java.util.ArrayList<>(Arrays.asList(sampleCommentDTO)));
        when(commentService.getCommentsByPost(eq(1L), any())).thenReturn(page);

        // Act
        ResponseEntity<Page<CommentDTO>> response = commentController.getCommentsByPost(1L, PageRequest.of(0, 10));

        // Assert
        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getContent()).hasSize(1);
        assertThat(response.getBody().getContent().get(0).content()).isEqualTo("Test comment");
        
        verify(commentService).getCommentsByPost(eq(1L), any());
    }

    @Test
    @DisplayName("Deve retornar lista de comentários quando controlador de comentários buscar por post simples")
    void commentController_getCommentsByPostSimple_ShouldReturnListOfComments() {
        // Arrange
        List<CommentDTO> comments = new java.util.ArrayList<>(Arrays.asList(sampleCommentDTO));
        when(commentService.getCommentsByPostSimple(1L)).thenReturn(comments);

        // Act
        ResponseEntity<List<CommentDTO>> response = commentController.getCommentsByPostSimple(1L);

        // Assert
        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody().get(0).content()).isEqualTo("Test comment");
        
        verify(commentService).getCommentsByPostSimple(1L);
    }

    @Test
    @DisplayName("Deve retornar comentário quando controlador de comentários buscar por ID")
    void commentController_getCommentById_ShouldReturnComment() {
        // Arrange
        when(commentService.getCommentById(1L)).thenReturn(sampleCommentDTO);

        // Act
        ResponseEntity<CommentDTO> response = commentController.getCommentById(1L);

        // Assert
        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().content()).isEqualTo("Test comment");
        
        verify(commentService).getCommentById(1L);
    }

    @Test
    @DisplayName("Deve retornar comentário criado quando controlador de comentários criar comentário")
    void commentController_createComment_ShouldReturnCreatedComment() {
        // Arrange
        when(authentication.getName()).thenReturn("testuser");
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(commentService.createComment(any(CommentDTO.class), eq("testuser"))).thenReturn(sampleCommentDTO);

        // Act
        ResponseEntity<CommentDTO> response = commentController.createComment(sampleCommentDTO);

        // Assert
        assertThat(response.getStatusCodeValue()).isEqualTo(201);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().content()).isEqualTo("Test comment");
        
        verify(commentService).createComment(any(CommentDTO.class), eq("testuser"));
    }

    @Test
    @DisplayName("Deve retornar sem conteúdo quando controlador de comentários deletar comentário")
    void commentController_deleteComment_ShouldReturnNoContent() {
        // Arrange
        when(authentication.getName()).thenReturn("testuser");
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        doNothing().when(commentService).deleteComment(1L, "testuser");

        // Act
        ResponseEntity<Void> response = commentController.deleteComment(1L);

        // Assert
        assertThat(response.getStatusCodeValue()).isEqualTo(204);
        
        verify(commentService).deleteComment(1L, "testuser");
    }

    // CategoryController Tests
    @Test
    @DisplayName("Deve retornar página de categorias quando controlador de categorias solicitar todas as categorias")
    void categoryController_getAllCategories_ShouldReturnPageOfCategories() {
        // Arrange
        Page<CategoryDTO> page = new PageImpl<>(new java.util.ArrayList<>(Arrays.asList(sampleCategoryDTO)));
        when(categoryService.getAllCategories(any())).thenReturn(page);

        // Act
        ResponseEntity<Page<CategoryDTO>> response = categoryController.getAllCategories(PageRequest.of(0, 10));

        // Assert
        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getContent()).hasSize(1);
        assertThat(response.getBody().getContent().get(0).name()).isEqualTo("Technology");
        
        verify(categoryService).getAllCategories(any());
    }

    @Test
    @DisplayName("Deve retornar categoria quando controlador de categorias buscar por ID")
    void categoryController_getCategoryById_ShouldReturnCategory() {
        // Arrange
        when(categoryService.getCategoryById(1L)).thenReturn(sampleCategoryDTO);

        // Act
        ResponseEntity<CategoryDTO> response = categoryController.getCategoryById(1L);

        // Assert
        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().name()).isEqualTo("Technology");
        
        verify(categoryService).getCategoryById(1L);
    }

    @Test
    @DisplayName("Deve retornar categoria criada quando controlador de categorias criar categoria")
    void categoryController_createCategory_ShouldReturnCreatedCategory() {
        // Arrange
        when(categoryService.createCategory(any(CategoryDTO.class))).thenReturn(sampleCategoryDTO);

        // Act
        ResponseEntity<CategoryDTO> response = categoryController.createCategory(sampleCategoryDTO);

        // Assert
        assertThat(response.getStatusCodeValue()).isEqualTo(201);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().name()).isEqualTo("Technology");
        
        verify(categoryService).createCategory(any(CategoryDTO.class));
    }

    @Test
    @DisplayName("Deve retornar sem conteúdo quando controlador de categorias deletar categoria")
    void categoryController_deleteCategory_ShouldReturnNoContent() {
        // Arrange
        doNothing().when(categoryService).deleteCategory(1L);

        // Act
        ResponseEntity<Void> response = categoryController.deleteCategory(1L);

        // Assert
        assertThat(response.getStatusCodeValue()).isEqualTo(204);
        
        verify(categoryService).deleteCategory(1L);
    }

    // AuthController Tests
    @Test
    @DisplayName("Deve retornar usuário criado quando controlador de autenticação registrar usuário")
    void authController_register_ShouldReturnCreatedUser() {
        // Arrange
        when(authService.register(any(CreateUserDTO.class))).thenReturn(sampleUserDTO);

        // Act
        ResponseEntity<UserDTO> response = authController.register(createUserDTO, mockRequest);

        // Assert
        assertThat(response.getStatusCodeValue()).isEqualTo(201);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().username()).isEqualTo("testuser");
        
        verify(authService).register(any(CreateUserDTO.class));
    }

    @Test
    @DisplayName("Deve retornar resposta JWT quando controlador de autenticação fazer login")
    void authController_login_ShouldReturnJwtResponse() {
        // Arrange
        when(authService.login(any(LoginRequest.class), any(), any(), any())).thenReturn(jwtResponse);

        // Act
        ResponseEntity<JwtResponse> response = authController.login(loginRequest, mockRequest);

        // Assert
        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().token()).isEqualTo("jwt-token-123");
        assertThat(response.getBody().user().username()).isEqualTo("testuser");
        
        verify(authService).login(any(LoginRequest.class), any(), any(), any());
    }

    @Test
    @DisplayName("Deve retornar resposta de sucesso quando controlador de autenticação verificar email")
    void authController_verifyEmail_ShouldReturnSuccessResponse() {
        // Arrange
        String token = "valid-token";
        when(authService.verifyEmail(token)).thenReturn(sampleUserDTO);

        // Act
        ResponseEntity<VerificationResponse> response = authController.verifyEmail(token);

        // Assert
        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().success()).isTrue();
        
        verify(authService).verifyEmail(token);
    }

    @Test
    @DisplayName("Deve retornar resposta de sucesso quando controlador de autenticação reenviar verificação de email")
    void authController_resendEmailVerification_ShouldReturnSuccessResponse() {
        // Arrange
        EmailVerificationRequest request = new EmailVerificationRequest("test@example.com");
        doNothing().when(authService).resendEmailVerification("test@example.com");

        // Act
        ResponseEntity<VerificationResponse> response = authController.resendEmailVerification(request);

        // Assert
        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().success()).isTrue();
        
        verify(authService).resendEmailVerification("test@example.com");
    }

    @Test
    @DisplayName("Deve retornar resposta de sucesso quando controlador de autenticação esquecer senha")
    void authController_forgotPassword_ShouldReturnSuccessResponse() {
        // Arrange
        PasswordResetRequest request = new PasswordResetRequest("test@example.com");
        doNothing().when(authService).requestPasswordReset("test@example.com");

        // Act
        ResponseEntity<VerificationResponse> response = authController.forgotPassword(request);

        // Assert
        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().success()).isTrue();
        
        verify(authService).requestPasswordReset("test@example.com");
    }

    @Test
    @DisplayName("Deve retornar resposta de sucesso quando controlador de autenticação redefinir senha")
    void authController_resetPassword_ShouldReturnSuccessResponse() {
        // Arrange
        PasswordResetConfirmRequest request = new PasswordResetConfirmRequest("valid-token", "NewPassword123!");
        when(authService.resetPassword("valid-token", "NewPassword123!")).thenReturn(sampleUserDTO);

        // Act
        ResponseEntity<VerificationResponse> response = authController.resetPassword(request);

        // Assert
        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().success()).isTrue();
        
        verify(authService).resetPassword("valid-token", "NewPassword123!");
    }

    @Test
    @DisplayName("Deve retornar resposta de sucesso quando controlador de autenticação validar token de redefinição de senha")
    void authController_validatePasswordResetToken_ShouldReturnSuccessResponse() {
        // Arrange
        String token = "valid-token";
        // validatePasswordResetToken doesn't return anything, just validates or throws exception

        // Act
        ResponseEntity<VerificationResponse> response = authController.validatePasswordResetToken(token);

        // Assert
        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().success()).isTrue();
        
        verify(authService).validatePasswordResetToken(token);
    }
}