# 📋 Development Log - Blog API REST

**Projeto:** Blog API REST com Java Spring Boot  
**Data:** 27/07/2025  
**Metodologia:** Desenvolvimento com IA  

## 🎯 Metodologia Aplicada

Seguimos rigorosamente um processo estruturado de desenvolvimento com IA:

### **Fase 1: PRD (Product Requirements Document)**
- **Objetivo:** API para gerenciar posts, usuários, comentários e categorias
- **Tecnologia:** Java 17+ com Spring Boot 3.x
- **Funcionalidades Core:**
  - Gestão de Usuários (cadastro, autenticação JWT, perfis)
  - Gestão de Posts (CRUD, publicação, categorização, busca)
  - Sistema de Comentários (aninhados, moderação)
  - Categorias e Tags (CRUD, filtros)
- **Requisitos Não-Funcionais:**
  - Performance < 200ms
  - Segurança JWT
  - Documentação Swagger
  - APIs: `/api/v1/{auth,users,posts,comments,categories}/*`

### **Fase 2: Tech Specs**
- **Arquitetura:** Controller → Service → Repository → Database
- **Stack Completo:**
  - Java 17, Spring Boot 3.2+, Spring Security, Spring Data JPA
  - PostgreSQL, Maven, Swagger, JUnit 5
- **Estrutura do Projeto:**
  ```
  src/main/java/com/blog/api/
  ├── config/     # Security, Swagger, JPA
  ├── controller/ # REST endpoints
  ├── service/    # Business logic
  ├── repository/ # Data access
  ├── entity/     # JPA entities
  ├── dto/        # Data transfer
  ├── exception/  # Error handling
  └── util/       # JWT utilities
  ```
- **Modelo de Dados:**
  - User (id, username, email, password, role, created_at)
  - Post (id, title, content, user_id, category_id, published, created_at)
  - Category (id, name, description)
  - Comment (id, content, post_id, user_id, parent_id, created_at)

### **Fase 3: Lista de Tarefas Técnicas (20 tarefas)**

#### **Setup Inicial:**
1. ✅ Criar projeto Spring Boot com Maven
2. ✅ Configurar dependencies no pom.xml
3. ✅ Estrutura de pastas

#### **Configuração:**
4. ✅ application.yml (DB, logging)
5. ✅ SecurityConfig (JWT)
6. ✅ SwaggerConfig

#### **Entities & DTOs:**
7. ✅ User.java, Post.java, Category.java, Comment.java
8. ✅ UserDTO, CreateUserDTO, PostDTO, CreatePostDTO, CategoryDTO, CommentDTO

#### **Repositories:**
9. ✅ UserRepository, PostRepository, CategoryRepository, CommentRepository

#### **Services:**
10. ✅ AuthService (JWT, login/register)
11. ✅ UserService (CRUD usuários)
12. ✅ PostService (CRUD posts, busca)
13. ✅ CategoryService (CRUD categorias)
14. ✅ CommentService (CRUD comentários)

#### **Controllers:**
15. ✅ AuthController (/api/v1/auth/*)
16. ✅ UserController (/api/v1/users/*)
17. ✅ PostController (/api/v1/posts/*)
18. ✅ CategoryController (/api/v1/categories/*)
19. ✅ CommentController (/api/v1/comments/*)

#### **Exception Handling:**
20. ✅ GlobalExceptionHandler
21. ✅ Custom Exceptions (ResourceNotFoundException, BadRequestException)

#### **Validação & Testes:**
22. ✅ Validation annotations
23. ✅ Unit tests (UserServiceTest)
24. ✅ Integration tests (AuthControllerTest)

## 🛠️ Decisões Técnicas Importantes

### **1. Arquitetura**
- **Pattern:** Layered Architecture (Controller-Service-Repository)
- **Justificativa:** Separação clara de responsabilidades, testabilidade, manutenibilidade

### **2. Segurança**
- **Autenticação:** JWT (stateless)
- **Autorização:** Role-based (USER, AUTHOR, ADMIN)
- **Endpoints públicos:** Posts e categorias (leitura)
- **Endpoints protegidos:** Criação/edição (roles específicas)

### **3. Banco de Dados**
- **ORM:** Spring Data JPA + Hibernate
- **Auditoria:** @CreatedDate, @LastModifiedDate
- **Relacionamentos:** OneToMany, ManyToOne com Lazy Loading

### **4. APIs e Documentação**
- **Padrão REST:** Verbos HTTP corretos, status codes apropriados
- **Paginação:** Pageable em todas as listagens
- **Documentação:** Swagger/OpenAPI automática
- **Validação:** Bean Validation (@Valid, @NotBlank, etc.)

## 📂 Estrutura Final do Projeto

```
/first-project/
├── pom.xml                    # Dependencies Maven
├── README.md                  # Documentação do projeto
├── DEVELOPMENT_LOG.md         # Este arquivo
├── src/main/java/com/blog/api/
│   ├── BlogApiApplication.java     # Main class
│   ├── config/
│   │   ├── JpaConfig.java          # JPA Auditing
│   │   ├── SecurityConfig.java     # Spring Security + JWT
│   │   ├── SwaggerConfig.java      # OpenAPI config
│   │   └── JwtAuthenticationFilter.java
│   ├── controller/
│   │   ├── AuthController.java     # /auth/* endpoints
│   │   ├── UserController.java     # /users/* endpoints
│   │   ├── PostController.java     # /posts/* endpoints
│   │   ├── CategoryController.java # /categories/* endpoints
│   │   └── CommentController.java  # /comments/* endpoints
│   ├── service/
│   │   ├── AuthService.java        # Login/Register logic
│   │   ├── UserService.java        # User business logic
│   │   ├── PostService.java        # Post business logic
│   │   ├── CategoryService.java    # Category logic
│   │   ├── CommentService.java     # Comment logic
│   │   └── CustomUserDetailsService.java
│   ├── repository/
│   │   ├── UserRepository.java     # User data access
│   │   ├── PostRepository.java     # Post data access
│   │   ├── CategoryRepository.java # Category data access
│   │   └── CommentRepository.java  # Comment data access
│   ├── entity/
│   │   ├── User.java              # User JPA entity
│   │   ├── Post.java              # Post JPA entity
│   │   ├── Category.java          # Category JPA entity
│   │   └── Comment.java           # Comment JPA entity
│   ├── dto/
│   │   ├── UserDTO.java           # User data transfer
│   │   ├── CreateUserDTO.java     # User creation
│   │   ├── PostDTO.java           # Post data transfer
│   │   ├── CreatePostDTO.java     # Post creation
│   │   ├── CategoryDTO.java       # Category data transfer
│   │   ├── CommentDTO.java        # Comment data transfer
│   │   ├── LoginRequest.java      # Login payload
│   │   └── JwtResponse.java       # JWT response
│   ├── exception/
│   │   ├── ResourceNotFoundException.java
│   │   ├── BadRequestException.java
│   │   └── GlobalExceptionHandler.java
│   └── util/
│       └── JwtUtil.java           # JWT token utilities
├── src/main/resources/
│   └── application.yml            # App configuration
└── src/test/java/com/blog/api/
    ├── service/
    │   └── UserServiceTest.java   # Service unit tests
    └── controller/
        └── AuthControllerTest.java # Controller tests
```

## 🔧 Configuração e Dependências

### **pom.xml Dependencies:**
- spring-boot-starter-web
- spring-boot-starter-data-jpa  
- spring-boot-starter-security
- spring-boot-starter-validation
- postgresql
- jjwt-api, jjwt-impl, jjwt-jackson
- springdoc-openapi-starter-webmvc-ui
- spring-boot-starter-test

### **application.yml Key Configs:**
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/blogdb
    username: bloguser
    password: blogpass
  jpa:
    hibernate.ddl-auto: update
    show-sql: true

jwt:
  secret: mySecretKey123456789012345678901234567890
  expiration: 86400000

server:
  port: 8080
```

## 🧪 Testes Implementados

### **Unit Tests:**
- **UserServiceTest:** Testa getUserById, getUserByUsername com cenários de sucesso e erro

### **Integration Tests:**
- **AuthControllerTest:** Testa endpoints de register e login com MockMvc

### **Coverage Areas:**
- Service layer validation
- Controller endpoint testing
- Exception handling scenarios

## 🔄 Funcionalidades Implementadas

### **✅ Autenticação Completa:**
- Registro de usuários com validação
- Login com JWT token
- Roles: USER, AUTHOR, ADMIN
- Endpoints: POST /auth/register, POST /auth/login

### **✅ Gestão de Usuários:**
- CRUD completo com paginação
- Busca por ID e username
- Controle de acesso por role
- Endpoints: GET/DELETE /users/*

### **✅ Sistema de Posts:**
- CRUD completo para autores
- Publicação/despublicação
- Busca por palavra-chave
- Filtros por categoria e autor
- Paginação em todas as listagens
- Endpoints: GET/POST/PUT/DELETE /posts/*

### **✅ Sistema de Comentários:**
- Comentários aninhados (parent/child)
- CRUD para usuários autenticados
- Listagem por post
- Endpoints: GET/POST/PUT/DELETE /comments/*

### **✅ Gestão de Categorias:**
- CRUD completo (admin only)
- Associação com posts
- Endpoints: GET/POST/PUT/DELETE /categories/*

## 📊 Métricas do Projeto

- **Total de arquivos criados:** ~35 arquivos
- **Linhas de código:** ~2000+ linhas
- **Endpoints API:** 20+ endpoints REST
- **Tempo de desenvolvimento:** ~2 horas (metodologia estruturada)
- **Cobertura de testes:** Básica (expandível)

## 🚀 Como Executar

1. **Prerequisites:**
   ```bash
   # PostgreSQL rodando na porta 5432
   # Database 'blogdb' criado
   # User 'bloguser' com senha 'blogpass'
   ```

2. **Execute:**
   ```bash
   mvn spring-boot:run
   ```

3. **Acesse:**
   - API Base: http://localhost:8080/api/v1/
   - Swagger UI: http://localhost:8080/swagger-ui.html
   - API Docs: http://localhost:8080/v3/api-docs

## 🎯 Próximos Passos Sugeridos

### **Imediatos:**
1. Setup PostgreSQL local e testes
2. Deploy com Docker/Docker Compose
3. CI/CD com GitHub Actions

### **Melhorias Técnicas:**
1. Aumentar cobertura de testes (>80%)
2. Implementar cache (Redis)
3. Rate limiting e monitoring
4. Logs estruturados

### **Features Avançadas:**
1. Upload de imagens para posts
2. Sistema de likes/favoritos
3. Notificações em tempo real
4. Frontend React/Angular

## 💡 Lições Aprendidas

### **✅ Sucessos da Metodologia:**
- **Planning First:** PRD + Tech Specs evitaram retrabalho
- **Task Breakdown:** 20 tarefas claras facilitaram execução
- **Iterative Validation:** Validação em cada fase garantiu qualidade
- **AI Assistance:** Acelerou desenvolvimento sem comprometer qualidade

### **🔧 Pontos de Melhoria:**
- Testes poderiam ser criados em paralelo com implementação
- Database migrations explícitas (Flyway/Liquibase)
- Environment-specific configs

---

**Este log preserva toda a jornada de desenvolvimento para referência futura e continuidade do projeto.**