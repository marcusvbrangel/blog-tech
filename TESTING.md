# 🧪 Guia de Testes - Blog API

## 📋 Cobertura de Testes Implementada

### **✅ Testes Unitários (Services)**
- **AuthServiceTest** - Registro, login, validações
- **UserServiceTest** - CRUD usuários, busca  
- **PostServiceTest** - CRUD posts, busca, filtros
- **CategoryServiceTest** - CRUD categorias
- **CommentServiceTest** - CRUD comentários, aninhamento

### **✅ Testes de Integração (Controllers)**
- **AuthControllerTest** - Endpoints de autenticação
- **PostControllerTest** - Endpoints de posts + segurança
- **CategoryControllerTest** - Endpoints de categorias + roles

### **✅ Testes de Repository (@DataJpaTest)**
- **PostRepositoryTest** - Queries customizadas, busca
- **UserRepositoryTest** - Queries de usuário

### **✅ Testes de Segurança**
- **JwtUtilTest** - Geração, validação, expiração de tokens
- **SecurityConfigTest** - Endpoints públicos vs protegidos

---

## 🚀 Comandos para Executar

### **🔧 Executar Todos os Testes**
```bash
mvn test
```

### **📊 Gerar Relatório de Cobertura**
```bash
mvn clean test jacoco:report
```

### **📈 Verificar Cobertura Mínima (80%)**
```bash
mvn jacoco:check
```

### **🎯 Executar Testes Específicos**
```bash
# Apenas testes de Service
mvn test -Dtest="*ServiceTest"

# Apenas testes de Controller  
mvn test -Dtest="*ControllerTest"

# Apenas testes de Repository
mvn test -Dtest="*RepositoryTest"

# Teste específico
mvn test -Dtest="AuthServiceTest"
```

---

## 📊 Relatórios de Cobertura

### **📁 Localização dos Relatórios**
Após executar `mvn test jacoco:report`:
- **HTML:** `target/site/jacoco/index.html`
- **XML:** `target/site/jacoco/jacoco.xml`
- **CSV:** `target/site/jacoco/jacoco.csv`

### **🎯 Meta de Cobertura**
- **Mínimo configurado:** 80% de cobertura de linha
- **Exclusões configuradas:**
  - Application class (main)
  - DTOs (apenas transferência de dados)
  - Entities (JPA entities)
  - Configs (configurações)

### **📈 Como Visualizar**
```bash
# Abrir relatório HTML
open target/site/jacoco/index.html
# ou
firefox target/site/jacoco/index.html
```

---

## 🔍 Tipos de Teste Implementados

### **🏗️ 1. Unit Tests (Services)**
```java
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
    @Mock private UserRepository userRepository;
    @InjectMocks private AuthService authService;
    
    @Test
    void register_WhenValidUser_ShouldReturnUserDTO() {
        // Given, When, Then
    }
}
```

### **🌐 2. Integration Tests (Controllers)**
```java
@WebMvcTest(PostController.class)
class PostControllerTest {
    @Autowired private MockMvc mockMvc;
    @MockBean private PostService postService;
    
    @Test
    @WithMockUser(roles = "AUTHOR")
    void createPost_WhenValidData_ShouldReturnCreated() {
        // HTTP request/response testing
    }
}
```

### **🗄️ 3. Repository Tests**
```java
@DataJpaTest
class PostRepositoryTest {
    @Autowired private TestEntityManager entityManager;
    @Autowired private PostRepository postRepository;
    
    @Test
    void findByPublishedTrue_ShouldReturnOnlyPublishedPosts() {
        // Database query testing
    }
}
```

### **🔐 4. Security Tests**
```java
@SpringBootTest
@TestPropertySource(properties = {"jwt.secret=...", "jwt.expiration=..."})
class JwtUtilTest {
    @Test
    void generateToken_ShouldCreateValidToken() {
        // JWT token testing
    }
}
```

---

## 📊 Cenários de Teste Cobertos

### **✅ Casos de Sucesso**
- CRUD operations funcionando
- Autenticação válida
- Busca e filtros
- Validações de entrada

### **❌ Casos de Erro**
- Recursos não encontrados (404)
- Dados inválidos (400)
- Não autorizado (401)
- Acesso negado (403)
- Conflitos (409)

### **🔒 Casos de Segurança**
- Endpoints públicos vs protegidos
- Validação de roles (USER, AUTHOR, ADMIN)
- Tokens JWT válidos/inválidos/expirados
- CSRF protection

### **🗄️ Casos de Persistência**
- Queries customizadas
- Relacionamentos entre entidades
- Paginação
- Ordenação

---

## 🛠️ Ferramentas e Frameworks

### **📋 Testing Stack**
- **JUnit 5** - Framework de testes
- **Mockito** - Mocking framework
- **Spring Boot Test** - Testes de integração
- **Spring Security Test** - Testes de segurança
- **TestContainers** - Testes com banco real (opcional)
- **AssertJ** - Assertions fluentes

### **📈 Coverage Tools**
- **JaCoCo** - Code coverage
- **Maven Surefire** - Test execution

---

## 🎯 Próximos Passos (Opcional)

### **🐳 TestContainers (E2E)**
```xml
<dependency>
    <groupId>org.testcontainers</groupId>
    <artifactId>postgresql</artifactId>
    <scope>test</scope>
</dependency>
```

### **🔄 Mutation Testing**
```xml
<plugin>
    <groupId>org.pitest</groupId>
    <artifactId>pitest-maven</artifactId>
</plugin>
```

### **⚡ Performance Testing**
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-test</artifactId>
    <scope>test</scope>
</dependency>
```

---

## 📝 Comandos Úteis

```bash
# Compilar sem executar testes
mvn compile -DskipTests

# Executar apenas testes rápidos
mvn test -Dtest="*ServiceTest,*RepositoryTest"

# Debug de testes
mvn test -Dmaven.surefire.debug

# Parallelizar testes
mvn test -T 4

# Gerar site completo com relatórios
mvn site
```

**🎉 Com essa configuração, você tem uma suite de testes robusta e profissional para seu portfolio!**