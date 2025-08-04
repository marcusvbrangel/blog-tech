package com.blog.api.service;

// Importações de DTOs - Data Transfer Objects utilizados para transferir dados entre camadas
import com.blog.api.dto.CreatePostDTO;    // DTO para criação de posts - contém title, content, categoryId, published
import com.blog.api.dto.PostDTO;          // DTO de resposta para posts - versão read-only com todos os dados

// Importações de entidades JPA - representam as tabelas do banco de dados
import com.blog.api.entity.Category;     // Entidade categoria - agrupa posts por temas
import com.blog.api.entity.Post;         // Entidade principal - representa um post do blog
import com.blog.api.entity.User;         // Entidade usuário - autor dos posts

// Importações de exceções customizadas
import com.blog.api.exception.ResourceNotFoundException;  // Exceção lançada quando recurso não é encontrado

// Importações de repositórios - interfaces que fazem a ponte com o banco de dados
import com.blog.api.repository.CategoryRepository;  // Acesso aos dados de categorias
import com.blog.api.repository.PostRepository;      // Acesso aos dados de posts  
import com.blog.api.repository.UserRepository;      // Acesso aos dados de usuários

// Importações do Micrometer - biblioteca para métricas e observabilidade
import io.micrometer.core.instrument.Counter;  // Contador de métricas - conta eventos como posts criados
import io.micrometer.core.instrument.Timer;    // Timer de métricas - mede tempo de operações

// Importações do JUnit 5 - framework de testes unitários
import org.junit.jupiter.api.BeforeEach;       // Anotação para executar setup antes de cada teste
import org.junit.jupiter.api.DisplayName;      // Anotação para dar nome descritivo aos testes
import org.junit.jupiter.api.Test;             // Anotação que marca um método como teste
import org.junit.jupiter.api.extension.ExtendWith;  // Extensão para integrar com outras bibliotecas

// Importações do Mockito - biblioteca para criar mocks (objetos simulados) em testes
import org.mockito.InjectMocks;               // Injeta mocks automaticamente no objeto testado
import org.mockito.Mock;                      // Cria um mock de uma dependência
import org.mockito.junit.jupiter.MockitoExtension;  // Extensão do Mockito para JUnit 5

// Importações do Spring Data - para paginação e manipulação de dados
import org.springframework.data.domain.Page;        // Interface para resultados paginados
import org.springframework.data.domain.PageImpl;    // Implementação concreta de Page para testes
import org.springframework.data.domain.PageRequest; // Criador de objetos Pageable
import org.springframework.data.domain.Pageable;    // Interface para parâmetros de paginação

// Importações padrão do Java
import java.time.LocalDateTime;  // Classe para trabalhar com data e hora
import java.util.Arrays;         // Utilitários para arrays - usado para criar listas de teste
import java.util.Optional;       // Container que pode ou não conter um valor - usado pelo JPA

// Importações estáticas do AssertJ - biblioteca para assertions mais legíveis
import static org.assertj.core.api.Assertions.assertThat;           // Assertions básicas
import static org.assertj.core.api.Assertions.assertThatThrownBy;   // Assertions para exceções

// Importações estáticas do Mockito - métodos para configurar comportamento dos mocks
import static org.mockito.ArgumentMatchers.any;  // Matcher para qualquer argumento
import static org.mockito.Mockito.verify;        // Verifica se um método foi chamado no mock
import static org.mockito.Mockito.when;          // Define comportamento do mock quando método é chamado

/**
 * Classe de teste para PostService
 * 
 * Esta classe testa todas as funcionalidades do serviço de posts do blog,
 * incluindo operações CRUD (Create, Read, Update, Delete) e consultas específicas.
 * 
 * Utiliza a arquitetura de testes unitários com mocks para isolar o serviço
 * das suas dependências externas (repositórios, métricas).
 */
@ExtendWith(MockitoExtension.class)     // Habilita o uso do Mockito no JUnit 5
@DisplayName("Post Service Tests")       // Nome descritivo exibido nos relatórios de teste
class PostServiceTest {

    // ===== MOCKS - Simulação das dependências do PostService =====
    
    @Mock
    private PostRepository postRepository;      // Mock do repositório de posts - simula acesso ao banco
    
    @Mock
    private UserRepository userRepository;      // Mock do repositório de usuários - simula busca de autores
    
    @Mock
    private CategoryRepository categoryRepository;  // Mock do repositório de categorias - simula busca de categorias
    
    @Mock
    private Counter postCreationCounter;        // Mock do contador de métricas - simula contagem de posts criados
    
    @Mock
    private Timer databaseQueryTimer;           // Mock do timer de métricas - simula medição de tempo de queries
    
    // ===== OBJETO TESTADO =====
    
    @InjectMocks
    private PostService postService;            // Objeto real sendo testado - recebe os mocks injetados automaticamente
    
    // ===== OBJETOS DE TESTE - Dados reutilizados em múltiplos testes =====
    
    private User testUser;          // Usuário de teste - representa o autor dos posts
    private Category testCategory;  // Categoria de teste - representa a categoria dos posts  
    private Post testPost;          // Post de teste - representa um post completo no sistema
    private CreatePostDTO createPostDTO;  // DTO de teste - representa dados para criar/atualizar posts
    private Pageable pageable;      // Objeto de paginação - define página e tamanho para consultas paginadas

    /**
     * Método executado antes de cada teste (@BeforeEach)
     * 
     * Inicializa todos os objetos de teste que serão reutilizados pelos métodos de teste.
     * Esse setup garante que cada teste tenha dados consistentes e isolados.
     */
    @BeforeEach
    void setUp() {
        // ===== CRIAÇÃO DO USUÁRIO DE TESTE =====
        
        // Cria um usuário usando o padrão Builder implementado na entidade User
        testUser = User.of("testuser", "test@example.com", "ValidPassword123!")  // Dados básicos: username, email, senha
                .role(User.Role.USER)           // Define role como USER (pode criar posts)
                .emailVerified(true)            // Marca email como verificado (necessário para criar posts)
                .build();                       // Constrói o objeto User
        
        testUser.setId(1L);                     // Define ID do usuário (simulando que já foi salvo no banco)
        testUser.setCreatedAt(LocalDateTime.now());  // Define data de criação do usuário

        // ===== CRIAÇÃO DA CATEGORIA DE TESTE =====
        
        // Cria uma categoria usando o padrão Builder
        testCategory = Category.of("Technology", "Tech posts")  // Nome e descrição da categoria
                .build();                       // Constrói o objeto Category
        testCategory.setId(1L);                 // Define ID da categoria (simulando que já foi salva)

        // ===== CRIAÇÃO DO POST DE TESTE =====
        
        // Cria um post usando o padrão Builder, associando ao usuário criado acima
        testPost = Post.of("Test Post", "Test content", testUser)  // Título, conteúdo e autor
                .published(true)                // Marca como publicado (visível para todos)
                .category(testCategory)         // Associa à categoria criada acima
                .build();                       // Constrói o objeto Post
        
        testPost.setId(1L);                     // Define ID do post (simulando que já foi salvo)
        testPost.setCreatedAt(LocalDateTime.now());   // Define data de criação
        testPost.setUpdatedAt(LocalDateTime.now());   // Define data de última atualização

        // ===== CRIAÇÃO DO DTO DE TESTE =====
        
        // Cria um DTO para operações de criação/atualização de posts
        // Parâmetros: título, conteúdo, ID da categoria, se está publicado
        createPostDTO = new CreatePostDTO("Test Post", "Test content", 1L, true);
        
        // ===== CONFIGURAÇÃO DE PAGINAÇÃO =====
        
        // Cria objeto de paginação: página 0 (primeira), 10 itens por página
        pageable = PageRequest.of(0, 10);
    }

    /**
     * TESTE: Buscar todos os posts publicados com paginação
     * 
     * Cenário: Usuário solicita lista de posts publicados
     * Expectativa: Retorna página com posts convertidos para DTO
     */
    @Test
    @DisplayName("Deve retornar uma página de PostDTOs quando buscar todos os posts")
    void getAllPosts_ShouldReturnPageOfPostDTOs() {
        
        // ===== ARRANGE (Preparação) =====
        // Cria uma página simulada contendo um post para retorno do repositório
        Page<Post> postPage = new PageImpl<>(Arrays.asList(testPost), pageable, 1);
        
        // Configura o mock: quando o repositório for chamado, retorna a página criada
        when(postRepository.findByPublishedTrue(pageable)).thenReturn(postPage);

        // ===== ACT (Ação) =====
        // Executa o método sendo testado - busca posts publicados
        Page<PostDTO> result = postService.getAllPublishedPosts(pageable);

        // ===== ASSERT (Verificação) =====
        assertThat(result).isNotNull();                                    // Verifica que o resultado não é nulo
        assertThat(result.getContent()).hasSize(1);                        // Verifica que retornou 1 post
        assertThat(result.getContent().get(0).title()).isEqualTo("Test Post");      // Verifica título do post
        assertThat(result.getContent().get(0).content()).isEqualTo("Test content"); // Verifica conteúdo do post
        
        // Verifica que o repositório foi chamado exatamente com os parâmetros esperados
        verify(postRepository).findByPublishedTrue(pageable);
    }

    /**
     * TESTE: Buscar post por ID existente
     * 
     * Cenário: Usuário solicita um post específico pelo ID
     * Expectativa: Retorna PostDTO com dados do post encontrado
     */
    @Test
    @DisplayName("Deve retornar PostDTO quando buscar post por ID existente")
    void getPostById_ShouldReturnPostDTO_WhenPostExists() {
        
        // ===== ARRANGE (Preparação) =====
        Long postId = 1L;  // ID do post que será buscado
        
        // Configura mock: quando buscar por ID, retorna o post de teste
        when(postRepository.findById(postId)).thenReturn(Optional.of(testPost));

        // ===== ACT (Ação) =====
        // Executa busca por ID no serviço
        PostDTO result = postService.getPostById(postId);

        // ===== ASSERT (Verificação) =====
        assertThat(result).isNotNull();                    // Verifica que resultado não é nulo
        assertThat(result.id()).isEqualTo(postId);         // Verifica que ID confere
        assertThat(result.title()).isEqualTo("Test Post"); // Verifica que título confere
        
        // Verifica que repositório foi chamado com ID correto
        verify(postRepository).findById(postId);
    }

    /**
     * TESTE: Buscar post por ID inexistente
     * 
     * Cenário: Usuário solicita post que não existe no banco
     * Expectativa: Lança ResourceNotFoundException com mensagem específica
     */
    @Test
    @DisplayName("Deve lançar ResourceNotFoundException quando buscar post por ID inexistente")
    void getPostById_ShouldThrowResourceNotFoundException_WhenPostNotExists() {
        
        // ===== ARRANGE (Preparação) =====
        Long postId = 999L;  // ID que não existe no banco
        
        // Configura mock: quando buscar por ID inexistente, retorna Optional vazio
        when(postRepository.findById(postId)).thenReturn(Optional.empty());

        // ===== ACT & ASSERT (Ação e Verificação juntas) =====
        // Verifica que o método lança a exceção esperada com mensagem correta
        assertThatThrownBy(() -> postService.getPostById(postId))
                .isInstanceOf(ResourceNotFoundException.class)     // Tipo correto da exceção
                .hasMessageContaining("Post")                      // Mensagem contém "Post"
                .hasMessageContaining("id")                       // Mensagem contém "id"
                .hasMessageContaining("999");                     // Mensagem contém o ID buscado
        
        // Verifica que repositório foi chamado (mesmo com ID inexistente)
        verify(postRepository).findById(postId);
    }

    /**
     * TESTE: Criar novo post com dados válidos
     * 
     * Cenário: Autor autenticado cria novo post com título, conteúdo e categoria
     * Expectativa: Post é salvo no banco e retorna DTO com dados completos
     */
    @Test
    @DisplayName("Deve criar e retornar PostDTO quando dados são válidos")
    void createPost_ShouldCreateAndReturnPostDTO_WhenValidData() {
        
        // ===== ARRANGE (Preparação) =====
        String username = "testuser";  // Username do autor autenticado
        
        // Configura mocks para simular busca bem-sucedida de dependências
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(testUser));      // Usuário existe
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));          // Categoria existe
        when(postRepository.save(any(Post.class))).thenReturn(testPost);                     // Salvamento retorna post criado

        // ===== ACT (Ação) =====
        // Executa criação do post através do serviço
        PostDTO result = postService.createPost(createPostDTO, username);

        // ===== ASSERT (Verificação) =====
        assertThat(result).isNotNull();                             // Resultado não é nulo
        assertThat(result.title()).isEqualTo("Test Post");          // Título foi preservado
        assertThat(result.content()).isEqualTo("Test content");     // Conteúdo foi preservado
        
        // Verifica que métricas foram atualizadas
        verify(postCreationCounter).increment();                   // Contador de posts criados foi incrementado
        
        // Verifica que todas as dependências foram consultadas
        verify(userRepository).findByUsername(username);           // Buscou usuário pelo username
        verify(categoryRepository).findById(1L);                   // Buscou categoria pelo ID
        verify(postRepository).save(any(Post.class));             // Salvou o post no banco
    }

    /**
     * TESTE: Atualizar post existente com dados válidos
     * 
     * Cenário: Autor atualiza seu próprio post com novos dados
     * Expectativa: Post é atualizado no banco e retorna DTO com novos dados
     */
    @Test
    @DisplayName("Deve atualizar e retornar PostDTO quando dados são válidos")
    void updatePost_ShouldUpdateAndReturnPostDTO_WhenValidData() {
        
        // ===== ARRANGE (Preparação) =====
        Long postId = 1L;              // ID do post a ser atualizado
        String username = "testuser";   // Username do autor (deve ser o mesmo do post)
        
        // Configura mocks para simular busca bem-sucedida
        when(postRepository.findById(postId)).thenReturn(Optional.of(testPost));         // Post existe
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(testUser)); // Usuário existe
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));     // Categoria existe (necessário para atualização)
        when(postRepository.save(any(Post.class))).thenReturn(testPost);                // Salvamento bem-sucedido

        // ===== ACT (Ação) =====
        // Executa atualização do post
        PostDTO result = postService.updatePost(postId, createPostDTO, username);

        // ===== ASSERT (Verificação) =====
        assertThat(result).isNotNull();                             // Resultado não é nulo
        assertThat(result.title()).isEqualTo("Test Post");          // Título foi atualizado
        assertThat(result.content()).isEqualTo("Test content");     // Conteúdo foi atualizado
        
        // Verifica que todas as operações necessárias foram executadas
        verify(postRepository).findById(postId);                   // Buscou post pelo ID
        verify(userRepository).findByUsername(username);           // Verificou se usuário existe
        verify(categoryRepository).findById(1L);                   // Verificou se categoria existe
        verify(postRepository).save(any(Post.class));             // Salvou post atualizado
    }

    /**
     * TESTE: Deletar post existente
     * 
     * Cenário: Autor deleta seu próprio post
     * Expectativa: Post é removido do banco de dados
     */
    @Test
    @DisplayName("Deve deletar post quando ele existe")
    void deletePost_ShouldDeletePost_WhenPostExists() {
        
        // ===== ARRANGE (Preparação) =====
        Long postId = 1L;              // ID do post a ser deletado
        String username = "testuser";   // Username do autor (deve ser o mesmo do post)
        
        // Configura mocks para simular busca bem-sucedida
        when(postRepository.findById(postId)).thenReturn(Optional.of(testPost));         // Post existe
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(testUser)); // Usuário existe

        // ===== ACT (Ação) =====
        // Executa deleção do post (método void - não retorna nada)
        postService.deletePost(postId, username);

        // ===== ASSERT (Verificação) =====
        // Como é método void, verificamos que as operações necessárias foram chamadas
        verify(postRepository).findById(postId);                   // Buscou post pelo ID
        verify(userRepository).findByUsername(username);           // Verificou se usuário existe
        verify(postRepository).delete(testPost);                   // Deletou o post do banco
    }

    /**
     * TESTE: Buscar posts por categoria específica
     * 
     * Cenário: Usuário filtra posts por uma categoria específica
     * Expectativa: Retorna apenas posts da categoria solicitada, paginados
     */
    @Test
    @DisplayName("Deve buscar posts por categoria")
    void getPostsByCategory_ShouldReturnPostsFromCategory() {
        
        // ===== ARRANGE (Preparação) =====
        Long categoryId = 1L;  // ID da categoria para filtrar
        
        // Cria página simulada com posts da categoria
        Page<Post> postPage = new PageImpl<>(Arrays.asList(testPost), pageable, 1);
        
        // Configura mock: busca por categoria retorna página criada
        when(postRepository.findByCategoryId(categoryId, pageable)).thenReturn(postPage);

        // ===== ACT (Ação) =====
        // Executa busca por categoria
        Page<PostDTO> result = postService.getPostsByCategory(categoryId, pageable);

        // ===== ASSERT (Verificação) =====
        assertThat(result).isNotNull();                                    // Resultado não é nulo
        assertThat(result.getContent()).hasSize(1);                        // Retornou 1 post
        assertThat(result.getContent().get(0).title()).isEqualTo("Test Post"); // Título confere
        
        // Verifica que repositório foi chamado com parâmetros corretos
        verify(postRepository).findByCategoryId(categoryId, pageable);
    }

}