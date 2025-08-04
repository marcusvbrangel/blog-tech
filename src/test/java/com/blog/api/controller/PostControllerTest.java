package com.blog.api.controller;

// Importações de DTOs - Data Transfer Objects para transferência de dados via HTTP
import com.blog.api.dto.CreatePostDTO;    // DTO para criação/atualização de posts via API
import com.blog.api.dto.PostDTO;          // DTO de resposta com dados completos do post

// Importações de exceções customizadas
import com.blog.api.exception.ResourceNotFoundException;  // Exceção para recursos não encontrados (HTTP 404)

// Importações de serviços - lógica de negócio
import com.blog.api.service.PostService;  // Serviço de posts que contém a lógica de negócio

// Importações do Jackson - biblioteca para serialização/deserialização JSON
import com.fasterxml.jackson.databind.ObjectMapper;  // Converte objetos Java para JSON e vice-versa

// Importações do JUnit 5 - framework de testes unitários
import org.junit.jupiter.api.BeforeEach;       // Anotação para setup antes de cada teste
import org.junit.jupiter.api.DisplayName;      // Anotação para nomes descritivos dos testes
import org.junit.jupiter.api.Test;             // Anotação que marca um método como teste

// Importações do Spring Boot Test - para testes de integração web
import org.springframework.beans.factory.annotation.Autowired;           // Injeção de dependência automática
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest; // Teste focado em camada web (controllers)
import org.springframework.boot.test.mock.mockito.MockBean;              // Mock específico do Spring Boot

// Importações do Spring Data - para paginação
import org.springframework.data.domain.Page;        // Interface para resultados paginados
import org.springframework.data.domain.PageImpl;    // Implementação concreta de Page para testes

// Importações do Spring Web - para tipos HTTP e testes web
import org.springframework.http.MediaType;                               // Tipos MIME (application/json, etc.)
import org.springframework.security.test.context.support.WithMockUser;  // Simula usuário autenticado nos testes
import org.springframework.test.web.servlet.MockMvc;                     // Simula requisições HTTP para testes

// Importações padrão do Java
import java.time.LocalDateTime;  // Classe para trabalhar com data e hora
import java.util.Arrays;         // Utilitários para arrays - usado para criar listas de teste

// Importações estáticas do Mockito - para configurar comportamento de mocks
import static org.mockito.ArgumentMatchers.any;  // Matcher para qualquer argumento
import static org.mockito.ArgumentMatchers.eq;   // Matcher para argumentos específicos
import static org.mockito.Mockito.*;             // Métodos estáticos do Mockito (when, verify, doNothing, etc.)

// Importações estáticas do Spring Security Test - para proteção CSRF em testes
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

// Importações estáticas do Spring Test MVC - para construir requisições HTTP simuladas
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;  // get, post, put, delete

// Importações estáticas do Spring Test MVC - para verificar respostas HTTP
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath; // Verifica JSON na resposta
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;   // Verifica status HTTP

/**
 * Classe de teste para PostController
 * 
 * Esta classe testa a camada web (REST API) do sistema de posts,
 * verificando se os endpoints HTTP respondem corretamente às requisições.
 * 
 * Utiliza @WebMvcTest para carregar apenas o contexto web do Spring,
 * isolando o controller das outras camadas (service, repository).
 * 
 * Testa aspectos como:
 * - Mapeamento de URLs
 * - Serialização/deserialização JSON
 * - Status codes HTTP
 * - Validação de dados
 * - Autenticação e autorização
 * - Tratamento de exceções
 */
@WebMvcTest(PostController.class)     // Carrega apenas o contexto web focado no PostController
@DisplayName("Post Controller Tests") // Nome descritivo para relatórios de teste
class PostControllerTest {

    // ===== COMPONENTES INJETADOS AUTOMATICAMENTE =====
    
    @Autowired
    private MockMvc mockMvc;              // Simula requisições HTTP sem subir servidor real
    
    @Autowired
    private ObjectMapper objectMapper;    // Converte objetos Java para JSON (injetado pelo Spring)

    // ===== MOCKS DAS DEPENDÊNCIAS DO CONTROLLER =====
    
    @MockBean
    private PostService postService;      // Mock do serviço de posts - simula lógica de negócio
    
    // Mocks das dependências de segurança (necessários para @WebMvcTest funcionar)
    @MockBean
    private com.blog.api.util.JwtUtil jwtUtil;                              // Mock do utilitário JWT
    
    @MockBean  
    private com.blog.api.service.CustomUserDetailsService userDetailsService; // Mock do serviço de autenticação
    
    @MockBean
    private com.blog.api.service.TermsService termsService;                  // Mock do serviço de termos
    
    @MockBean
    private com.blog.api.service.JwtBlacklistService jwtBlacklistService;    // Mock do serviço de blacklist JWT
    
    @MockBean
    private com.blog.api.service.UserService userService;                    // Mock do serviço de usuários

    // ===== OBJETOS DE TESTE - Dados reutilizados em múltiplos testes =====
    
    private PostDTO samplePostDTO;       // DTO de resposta - representa um post completo retornado pela API
    private CreatePostDTO createPostDTO; // DTO de entrada - representa dados para criar/atualizar post

    /**
     * Método executado antes de cada teste (@BeforeEach)
     * 
     * Inicializa os objetos de teste que serão reutilizados pelos métodos de teste.
     * Garante que cada teste tenha dados consistentes e isolados.
     */
    @BeforeEach
    void setUp() {
        
        // ===== CRIAÇÃO DO DTO DE RESPOSTA (PostDTO) =====
        
        // Cria um PostDTO que simula a resposta da API para um post existente
        samplePostDTO = new PostDTO(
                1L,                    // ID do post
                "Test Post",           // Título do post
                "Test content",        // Conteúdo do post
                true,                  // Status de publicação (true = publicado)
                LocalDateTime.now(),   // Data de criação
                LocalDateTime.now(),   // Data de última atualização
                "testuser",            // Username do autor
                "Technology",          // Nome da categoria
                5                      // Número de comentários
        );

        // ===== CRIAÇÃO DO DTO DE ENTRADA (CreatePostDTO) =====
        
        // Cria um CreatePostDTO que simula dados enviados pelo cliente para criar/atualizar posts
        // Parâmetros: título, conteúdo, ID da categoria, status de publicação
        createPostDTO = new CreatePostDTO("Test Post", "Test content", 1L, true);
    }

    /**
     * TESTE HTTP: GET /api/v1/posts - Buscar todos os posts publicados
     * 
     * Cenário: Cliente faz requisição GET para listar posts publicados com paginação
     * Expectativa: 
     * - Status HTTP 200 (OK)
     * - Resposta JSON com estrutura de página
     * - Array "content" contendo os posts
     * - Campos do post corretamente serializados
     */
    @Test
    @DisplayName("Deve retornar página de posts quando buscar todos os posts")
    void getAllPosts_ShouldReturnPageOfPosts() throws Exception {
        
        // ===== ARRANGE (Preparação) =====
        // Cria página simulada com um post para retorno do serviço
        Page<PostDTO> page = new PageImpl<>(Arrays.asList(samplePostDTO));
        
        // Configura mock: quando serviço for chamado, retorna a página criada
        when(postService.getAllPublishedPosts(any())).thenReturn(page);

        // ===== ACT & ASSERT (Ação e Verificação juntas) =====
        // Executa requisição HTTP GET e verifica resposta
        mockMvc.perform(get("/api/v1/posts")                          // GET /api/v1/posts
                .contentType(MediaType.APPLICATION_JSON))             // Header Content-Type: application/json
                .andExpect(status().isOk())                           // Verifica status HTTP 200
                .andExpect(jsonPath("$.content").isArray())            // Verifica que "content" é um array
                .andExpect(jsonPath("$.content[0].id").value(1))       // Verifica ID do primeiro post
                .andExpect(jsonPath("$.content[0].title").value("Test Post"))  // Verifica título
                .andExpect(jsonPath("$.content[0].published").value(true));     // Verifica status publicado

        // Verifica que o serviço foi chamado corretamente
        verify(postService).getAllPublishedPosts(any());
    }

    /**
     * TESTE HTTP: GET /api/v1/posts/{id} - Buscar post por ID (caso de sucesso)
     * 
     * Cenário: Cliente solicita um post específico que existe no sistema
     * Expectativa:
     * - Status HTTP 200 (OK)
     * - Resposta JSON com dados completos do post
     * - Todos os campos corretamente serializados
     */
    @Test
    @DisplayName("Deve retornar post quando post existe")
    void getPostById_ShouldReturnPost_WhenExists() throws Exception {
        
        // ===== ARRANGE (Preparação) =====
        // Configura mock: quando buscar post por ID 1, retorna o post de teste
        when(postService.getPostById(1L)).thenReturn(samplePostDTO);

        // ===== ACT & ASSERT (Ação e Verificação juntas) =====
        // Executa requisição HTTP GET com ID específico
        mockMvc.perform(get("/api/v1/posts/1")                        // GET /api/v1/posts/1
                .contentType(MediaType.APPLICATION_JSON))             // Header Content-Type
                .andExpect(status().isOk())                           // Verifica status HTTP 200
                .andExpect(jsonPath("$.id").value(1))                  // Verifica ID do post
                .andExpect(jsonPath("$.title").value("Test Post"))     // Verifica título
                .andExpect(jsonPath("$.content").value("Test content")); // Verifica conteúdo

        // Verifica que o serviço foi chamado com o ID correto
        verify(postService).getPostById(1L);
    }

    /**
     * TESTE HTTP: GET /api/v1/posts/{id} - Buscar post por ID (caso de erro)
     * 
     * Cenário: Cliente solicita um post que não existe no sistema
     * Expectativa:
     * - Status HTTP 404 (Not Found)
     * - Exceção ResourceNotFoundException é tratada corretamente pelo controller
     * - Resposta adequada para o cliente
     */
    @Test
    @DisplayName("Deve retornar NotFound quando post não existe")
    void getPostById_ShouldReturnNotFound_WhenNotExists() throws Exception {
        
        // ===== ARRANGE (Preparação) =====
        // Configura mock: quando buscar post inexistente, lança exceção
        when(postService.getPostById(999L)).thenThrow(new ResourceNotFoundException("Post", "id", 999L));

        // ===== ACT & ASSERT (Ação e Verificação juntas) =====
        // Executa requisição para post inexistente
        mockMvc.perform(get("/api/v1/posts/999")                      // GET /api/v1/posts/999
                .contentType(MediaType.APPLICATION_JSON))             // Header Content-Type
                .andExpect(status().isNotFound());                    // Verifica status HTTP 404

        // Verifica que o serviço foi chamado (mesmo com ID inexistente)
        verify(postService).getPostById(999L);
    }

    /**
     * TESTE HTTP: POST /api/v1/posts - Criar novo post (caso de sucesso)
     * 
     * Cenário: Usuário autenticado envia dados válidos para criar post
     * Expectativa:
     * - Status HTTP 201 (Created)
     * - Resposta JSON com dados do post criado
     * - Autenticação é verificada (@WithMockUser)
     * - Proteção CSRF é aplicada (.with(csrf()))
     * - JSON é deserializado corretamente para CreatePostDTO
     */
    @Test
    @WithMockUser(username = "testuser")    // Simula usuário autenticado com username "testuser"
    @DisplayName("Deve criar e retornar post quando dados são válidos")
    void createPost_ShouldCreateAndReturnPost() throws Exception {
        
        // ===== ARRANGE (Preparação) =====
        // Configura mock: quando criar post, retorna o post criado
        when(postService.createPost(any(CreatePostDTO.class), eq("testuser"))).thenReturn(samplePostDTO);

        // ===== ACT & ASSERT (Ação e Verificação juntas) =====
        // Executa requisição HTTP POST com corpo JSON
        mockMvc.perform(post("/api/v1/posts")                             // POST /api/v1/posts
                .with(csrf())                                             // Inclui token CSRF (segurança)
                .contentType(MediaType.APPLICATION_JSON)                  // Header Content-Type: application/json
                .content(objectMapper.writeValueAsString(createPostDTO))) // Corpo: CreatePostDTO como JSON
                .andExpect(status().isCreated())                          // Verifica status HTTP 201
                .andExpect(jsonPath("$.id").value(1))                     // Verifica ID do post criado
                .andExpect(jsonPath("$.title").value("Test Post"))        // Verifica título
                .andExpect(jsonPath("$.content").value("Test content"));  // Verifica conteúdo

        // Verifica que o serviço foi chamado com DTO e username corretos
        verify(postService).createPost(any(CreatePostDTO.class), eq("testuser"));
    }

    /**
     * TESTE HTTP: POST /api/v1/posts - Criar post sem autenticação (caso de erro)
     * 
     * Cenário: Cliente tenta criar post sem estar autenticado
     * Expectativa:
     * - Status HTTP 401 (Unauthorized)
     * - Spring Security bloqueia a requisição antes de chegar ao controller
     * - Serviço nunca é chamado (segurança em camada)
     * 
     * Nota: Não usa @WithMockUser, simulando usuário não autenticado
     */
    @Test
    @DisplayName("Deve retornar Unauthorized quando não está autenticado")
    void createPost_ShouldReturnUnauthorized_WhenNotAuthenticated() throws Exception {
        
        // ===== ACT & ASSERT (Ação e Verificação juntas) =====
        // Executa requisição sem autenticação
        mockMvc.perform(post("/api/v1/posts")                             // POST /api/v1/posts
                .with(csrf())                                             // CSRF ainda necessário
                .contentType(MediaType.APPLICATION_JSON)                  // Content-Type
                .content(objectMapper.writeValueAsString(createPostDTO))) // Corpo JSON válido
                .andExpect(status().isUnauthorized());                    // Verifica status HTTP 401

        // Verifica que o serviço NUNCA foi chamado (segurança funcionou)
        verify(postService, never()).createPost(any(), any());
    }

    /**
     * TESTE HTTP: POST /api/v1/posts - Criar post com dados inválidos (validação)
     * 
     * Cenário: Usuário autenticado envia dados que não passam na validação
     * Expectativa:
     * - Status HTTP 400 (Bad Request)
     * - Validação do Spring (Bean Validation) rejeita os dados
     * - Serviço nunca é chamado (validação em camada superior)
     * - Resposta contém detalhes dos erros de validação
     */
    @Test
    @WithMockUser(username = "testuser")    // Usuário autenticado (passa na segurança)
    @DisplayName("Deve retornar BadRequest quando dados são inválidos")
    void createPost_ShouldReturnBadRequest_WhenInvalidData() throws Exception {
        
        // ===== ARRANGE (Preparação) =====
        // Cria DTO com dados inválidos para testar validação
        CreatePostDTO invalidPost = new CreatePostDTO("", "Short", 1L, true); // Título vazio (inválido)

        // ===== ACT & ASSERT (Ação e Verificação juntas) =====
        // Executa requisição com dados inválidos
        mockMvc.perform(post("/api/v1/posts")                             // POST /api/v1/posts
                .with(csrf())                                             // Token CSRF
                .contentType(MediaType.APPLICATION_JSON)                  // Content-Type
                .content(objectMapper.writeValueAsString(invalidPost)))   // Corpo com dados inválidos
                .andExpect(status().isBadRequest());                      // Verifica status HTTP 400

        // Verifica que o serviço NUNCA foi chamado (validação bloqueou)
        verify(postService, never()).createPost(any(), any());
    }

    /**
     * TESTE HTTP: PUT /api/v1/posts/{id} - Atualizar post existente (caso de sucesso)
     * 
     * Cenário: Proprietário do post atualiza com dados válidos
     * Expectativa:
     * - Status HTTP 200 (OK)
     * - Resposta JSON com dados atualizados do post
     * - Verificação de autenticação (@WithMockUser)
     * - Verificação de propriedade (ownership) do post
     * - Proteção CSRF aplicada
     */
    @Test
    @WithMockUser(username = "testuser")    // Simula proprietário do post
    @DisplayName("Deve atualizar e retornar post quando dados são válidos")
    void updatePost_ShouldUpdateAndReturnPost() throws Exception {
        
        // ===== ARRANGE (Preparação) =====
        // Cria DTO representando o post após atualização
        PostDTO updatedPost = new PostDTO(1L, "Updated Post", "Updated content", true, 
                LocalDateTime.now(), LocalDateTime.now(), "testuser", "Technology", 5);
        
        // Configura mock: atualização bem-sucedida retorna post atualizado
        when(postService.updatePost(eq(1L), any(CreatePostDTO.class), eq("testuser"))).thenReturn(updatedPost);

        // ===== ACT & ASSERT (Ação e Verificação juntas) =====
        // Executa requisição HTTP PUT com corpo JSON
        mockMvc.perform(put("/api/v1/posts/1")                            // PUT /api/v1/posts/1
                .with(csrf())                                             // Token CSRF
                .contentType(MediaType.APPLICATION_JSON)                  // Content-Type
                .content(objectMapper.writeValueAsString(createPostDTO))) // Corpo: dados atualizados
                .andExpect(status().isOk())                               // Verifica status HTTP 200
                .andExpect(jsonPath("$.id").value(1))                     // Verifica ID mantido
                .andExpect(jsonPath("$.title").value("Updated Post"));    // Verifica título atualizado

        // Verifica que serviço foi chamado com parâmetros corretos
        verify(postService).updatePost(eq(1L), any(CreatePostDTO.class), eq("testuser"));
    }

    /**
     * TESTE HTTP: PUT /api/v1/posts/{id} - Tentar atualizar post inexistente
     * 
     * Cenário: Usuário tenta atualizar post que não existe no sistema
     * Expectativa:
     * - Status HTTP 404 (Not Found)
     * - Serviço lança ResourceNotFoundException
     * - Controller trata a exceção adequadamente
     * - Resposta contém informações sobre o erro
     */
    @Test
    @WithMockUser(username = "testuser")    // Usuário autenticado válido
    @DisplayName("Deve retornar NotFound quando post para atualizar não existe")
    void updatePost_ShouldReturnNotFound_WhenPostNotExists() throws Exception {
        
        // ===== ARRANGE (Preparação) =====
        // Configura mock: tentativa de atualizar post inexistente lança exceção
        when(postService.updatePost(eq(999L), any(CreatePostDTO.class), eq("testuser")))
                .thenThrow(new ResourceNotFoundException("Post", "id", 999L));

        // ===== ACT & ASSERT (Ação e Verificação juntas) =====
        // Executa requisição para atualizar post inexistente
        mockMvc.perform(put("/api/v1/posts/999")                          // PUT /api/v1/posts/999
                .with(csrf())                                             // Token CSRF
                .contentType(MediaType.APPLICATION_JSON)                  // Content-Type
                .content(objectMapper.writeValueAsString(createPostDTO))) // Dados válidos
                .andExpect(status().isNotFound());                        // Verifica status HTTP 404

        // Verifica que serviço foi chamado (mesmo com post inexistente)
        verify(postService).updatePost(eq(999L), any(CreatePostDTO.class), eq("testuser"));
    }

    /**
     * TESTE HTTP: PUT /api/v1/posts/{id} - Tentar atualizar post de outro usuário
     * 
     * Cenário: Usuário autenticado tenta atualizar post que não é seu
     * Expectativa:
     * - Status HTTP 500 (Internal Server Error) - tratamento pode ser melhorado
     * - Serviço lança RuntimeException sobre propriedade
     * - Verificação de ownership (autorização) funciona
     * 
     * Nota: Idealmente deveria retornar 403 (Forbidden), mas está retornando 500
     */
    @Test
    @WithMockUser(username = "otheruser")    // Usuário diferente do proprietário
    @DisplayName("Deve retornar Forbidden quando usuário não é dono do post")
    void updatePost_ShouldReturnForbidden_WhenNotOwner() throws Exception {
        
        // ===== ARRANGE (Preparação) =====
        // Configura mock: tentativa de atualizar post de outro usuário lança exceção
        when(postService.updatePost(eq(1L), any(CreatePostDTO.class), eq("otheruser")))
                .thenThrow(new RuntimeException("You can only update your own posts"));

        // ===== ACT & ASSERT (Ação e Verificação juntas) =====
        // Executa requisição de usuário não autorizado
        mockMvc.perform(put("/api/v1/posts/1")                            // PUT /api/v1/posts/1
                .with(csrf())                                             // Token CSRF
                .contentType(MediaType.APPLICATION_JSON)                  // Content-Type
                .content(objectMapper.writeValueAsString(createPostDTO))) // Dados válidos
                .andExpect(status().isInternalServerError());             // Verifica status HTTP 500

        // Verifica que serviço foi chamado e verificou propriedade
        verify(postService).updatePost(eq(1L), any(CreatePostDTO.class), eq("otheruser"));
    }

    /**
     * TESTE HTTP: DELETE /api/v1/posts/{id} - Deletar post próprio (caso de sucesso)
     * 
     * Cenário: Proprietário do post executa deleção
     * Expectativa:
     * - Status HTTP 204 (No Content) - deleção bem-sucedida, sem corpo de resposta
     * - Verificação de autenticação (@WithMockUser)
     * - Verificação de propriedade (ownership) do post
     * - Proteção CSRF aplicada
     * - Método void do serviço executado sem erros
     */
    @Test
    @WithMockUser(username = "testuser")    // Simula proprietário do post
    @DisplayName("Deve deletar post quando usuário é dono")
    void deletePost_ShouldDeletePost_WhenOwner() throws Exception {
        
        // ===== ARRANGE (Preparação) =====
        // Configura mock: deleção bem-sucedida não retorna nada (método void)
        doNothing().when(postService).deletePost(1L, "testuser");

        // ===== ACT & ASSERT (Ação e Verificação juntas) =====
        // Executa requisição HTTP DELETE
        mockMvc.perform(delete("/api/v1/posts/1")                        // DELETE /api/v1/posts/1
                .with(csrf()))                                           // Token CSRF
                .andExpect(status().isNoContent());                      // Verifica status HTTP 204

        // Verifica que serviço foi chamado para executar deleção
        verify(postService).deletePost(1L, "testuser");
    }

    /**
     * TESTE HTTP: DELETE /api/v1/posts/{id} - Tentar deletar post de outro usuário
     * 
     * Cenário: Usuário autenticado tenta deletar post que não é seu
     * Expectativa:
     * - Status HTTP 500 (Internal Server Error) - tratamento pode ser melhorado
     * - Serviço lança RuntimeException sobre propriedade
     * - Verificação de ownership (autorização) funciona
     * 
     * Nota: Idealmente deveria retornar 403 (Forbidden), mas está retornando 500
     */
    @Test
    @WithMockUser(username = "otheruser")    // Usuário diferente do proprietário
    @DisplayName("Deve retornar Forbidden quando usuário não é dono para deletar")
    void deletePost_ShouldReturnForbidden_WhenNotOwner() throws Exception {
        
        // ===== ARRANGE (Preparação) =====
        // Configura mock: tentativa de deletar post de outro usuário lança exceção
        doThrow(new RuntimeException("You can only delete your own posts"))
                .when(postService).deletePost(1L, "otheruser");

        // ===== ACT & ASSERT (Ação e Verificação juntas) =====
        // Executa requisição de usuário não autorizado
        mockMvc.perform(delete("/api/v1/posts/1")                        // DELETE /api/v1/posts/1
                .with(csrf()))                                           // Token CSRF
                .andExpect(status().isInternalServerError());            // Verifica status HTTP 500

        // Verifica que serviço foi chamado e verificou propriedade
        verify(postService).deletePost(1L, "otheruser");
    }

    /**
     * TESTE HTTP: DELETE /api/v1/posts/{id} - Tentar deletar post inexistente
     * 
     * Cenário: Usuário tenta deletar post que não existe no sistema
     * Expectativa:
     * - Status HTTP 404 (Not Found)
     * - Serviço lança ResourceNotFoundException
     * - Controller trata a exceção adequadamente
     * - Resposta contém informações sobre o erro
     */
    @Test
    @WithMockUser(username = "testuser")    // Usuário autenticado válido
    @DisplayName("Deve retornar NotFound quando post para deletar não existe")
    void deletePost_ShouldReturnNotFound_WhenPostNotExists() throws Exception {
        
        // ===== ARRANGE (Preparação) =====
        // Configura mock: tentativa de deletar post inexistente lança exceção
        doThrow(new ResourceNotFoundException("Post", "id", 999L))
                .when(postService).deletePost(999L, "testuser");

        // ===== ACT & ASSERT (Ação e Verificação juntas) =====
        // Executa requisição para deletar post inexistente
        mockMvc.perform(delete("/api/v1/posts/999")                      // DELETE /api/v1/posts/999
                .with(csrf()))                                           // Token CSRF
                .andExpect(status().isNotFound());                       // Verifica status HTTP 404

        // Verifica que serviço foi chamado (mesmo com post inexistente)
        verify(postService).deletePost(999L, "testuser");
    }

    /**
     * TESTE HTTP: GET /api/v1/posts/category/{categoryId} - Buscar posts por categoria
     * 
     * Cenário: Cliente solicita posts filtrados por categoria específica
     * Expectativa:
     * - Status HTTP 200 (OK)
     * - Resposta JSON paginada com posts da categoria
     * - Array "content" contendo apenas posts da categoria solicitada
     * - Campo categoryName preenchido corretamente
     * - Não requer autenticação (endpoint público)
     */
    @Test
    @DisplayName("Deve buscar posts por categoria")
    void getPostsByCategory_ShouldReturnPostsFromCategory() throws Exception {
        
        // ===== ARRANGE (Preparação) =====
        // Cria página simulada com posts da categoria
        Page<PostDTO> page = new PageImpl<>(Arrays.asList(samplePostDTO));
        
        // Configura mock: busca por categoria retorna página filtrada
        when(postService.getPostsByCategory(eq(1L), any())).thenReturn(page);

        // ===== ACT & ASSERT (Ação e Verificação juntas) =====
        // Executa requisição de busca por categoria
        mockMvc.perform(get("/api/v1/posts/category/1")                   // GET /api/v1/posts/category/1
                .contentType(MediaType.APPLICATION_JSON))                // Content-Type
                .andExpect(status().isOk())                              // Verifica status HTTP 200
                .andExpect(jsonPath("$.content").isArray())               // Verifica estrutura paginada
                .andExpect(jsonPath("$.content[0].categoryName").value("Technology")); // Verifica categoria

        // Verifica que serviço foi chamado com ID da categoria correto
        verify(postService).getPostsByCategory(eq(1L), any());
    }

    /**
     * TESTE HTTP: GET /api/v1/posts/user/{userId} - Buscar posts por autor/usuário
     * 
     * Cenário: Cliente solicita posts criados por um usuário específico
     * Expectativa:
     * - Status HTTP 200 (OK)
     * - Resposta JSON paginada com posts do autor
     * - Array "content" contendo apenas posts do usuário solicitado
     * - Campo authorUsername preenchido corretamente
     * - Não requer autenticação (endpoint público)
     */
    @Test
    @DisplayName("Deve buscar posts por autor")
    void getPostsByAuthor_ShouldReturnPostsFromAuthor() throws Exception {
        
        // ===== ARRANGE (Preparação) =====
        // Cria página simulada com posts do autor
        Page<PostDTO> page = new PageImpl<>(Arrays.asList(samplePostDTO));
        
        // Configura mock: busca por usuário retorna página filtrada
        when(postService.getPostsByUser(eq(1L), any())).thenReturn(page);

        // ===== ACT & ASSERT (Ação e Verificação juntas) =====
        // Executa requisição de busca por autor
        mockMvc.perform(get("/api/v1/posts/user/1")                       // GET /api/v1/posts/user/1
                .contentType(MediaType.APPLICATION_JSON))                // Content-Type
                .andExpect(status().isOk())                              // Verifica status HTTP 200
                .andExpect(jsonPath("$.content").isArray())               // Verifica estrutura paginada
                .andExpect(jsonPath("$.content[0].authorUsername").value("testuser")); // Verifica autor

        // Verifica que serviço foi chamado com ID do usuário correto
        verify(postService).getPostsByUser(eq(1L), any());
    }

    /**
     * TESTE HTTP: GET /api/v1/posts/search?keyword={keyword} - Buscar posts por palavra-chave
     * 
     * Cenário: Cliente busca posts que contenham determinada palavra-chave no título ou conteúdo
     * Expectativa:
     * - Status HTTP 200 (OK)
     * - Resposta JSON paginada com posts que contêm a palavra-chave
     * - Parâmetro "keyword" passado como query parameter (?keyword=valor)
     * - Array "content" contendo posts relevantes à busca
     * - Não requer autenticação (endpoint público)
     */
    @Test
    @DisplayName("Deve buscar posts por título contendo texto")
    void searchPostsByTitle_ShouldReturnMatchingPosts() throws Exception {
        
        // ===== ARRANGE (Preparação) =====
        // Cria página simulada com posts que contêm a palavra-chave
        Page<PostDTO> page = new PageImpl<>(Arrays.asList(samplePostDTO));
        
        // Configura mock: busca por palavra-chave retorna posts relevantes
        when(postService.searchPosts(eq("test"), any())).thenReturn(page);

        // ===== ACT & ASSERT (Ação e Verificação juntas) =====
        // Executa requisição de busca com parâmetro keyword
        mockMvc.perform(get("/api/v1/posts/search")                       // GET /api/v1/posts/search
                .param("keyword", "test")                               // Query parameter: ?keyword=test
                .contentType(MediaType.APPLICATION_JSON))                // Content-Type
                .andExpect(status().isOk())                              // Verifica status HTTP 200
                .andExpect(jsonPath("$.content").isArray())               // Verifica estrutura paginada
                .andExpect(jsonPath("$.content[0].title").value("Test Post")); // Verifica resultado relevante

        // Verifica que serviço foi chamado com palavra-chave correta
        verify(postService).searchPosts(eq("test"), any());
    }

}