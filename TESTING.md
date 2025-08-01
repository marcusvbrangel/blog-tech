# 🧪 Guia de Testes - Blog API

## 📋 Cobertura de Testes Implementada

### **✅ Testes Unitários (Services)**
- **AuthServiceTest** - Registro, login, validações
- **UserServiceTest** - CRUD usuários, busca  
- **PostServiceTest** - CRUD posts, busca, filtros
- **CategoryServiceTest** - CRUD categorias
- **CommentServiceTest** - CRUD comentários, aninhamento
- **EmailServiceTest** - Envio de emails, templates HTML, health check (13 testes)
- **VerificationTokenServiceTest** - Tokens, validação, rate limiting (15 testes)

### **✅ Testes de Integração (Controllers)**
- **AuthControllerTest** - Endpoints de autenticação + Email Verification (11 testes adicionais)
- **PostControllerTest** - Endpoints de posts + segurança
- **CategoryControllerTest** - Endpoints de categorias + roles

### **✅ Testes de Repository (@DataJpaTest)**
- **PostRepositoryTest** - Queries customizadas, busca
- **UserRepositoryTest** - Queries de usuário
- **VerificationTokenRepositoryTest** - Queries de tokens, cleanup automático

### **✅ Testes de Segurança**
- **JwtUtilTest** - Geração, validação, expiração de tokens
- **SecurityConfigTest** - Endpoints públicos vs protegidos
- **Email Verification Security** - Rate limiting, token security, privacy protection

### **✅ Testes de Email System**
- **EmailServiceTest** - Templates HTML, SMTP, health checks
- **VerificationTokenServiceTest** - Segurança de tokens, expiração, uso único
- **Rate Limiting Tests** - Proteção contra spam de emails

---

## 🚀 Comandos para Executar

### **🔧 Executar Todos os Testes**
```bash
mvn test
```

### **📊 Gerar Relatório de Cobertura**
```bash
# Cobertura removida - JaCoCo desabilitado para melhor performance
mvn clean test
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

# Testes de Email Verification
mvn test -Dtest="EmailServiceTest"
mvn test -Dtest="VerificationTokenServiceTest"

# Todos os testes de Email System
mvn test -Dtest="*Email*,*Verification*"
```

---

## 📊 Relatórios de Cobertura

### **⚠️ JaCoCo Removido**
JaCoCo foi removido permanentemente do projeto por causar:
- Lentidão na compilação e testes
- Problemas de compatibilidade com Java 21
- Complexidade desnecessária no desenvolvimento

### **🔧 JVM Warnings Resolvidos**
Configurações adicionadas para eliminar warnings recorrentes:
- **`.mvn/jvm.config`** - Configurações JVM para Maven
- **`.mvn/maven.config`** - Configurações Maven específicas
- **Surefire Plugin** - Args JVM otimizados para testes

**Resultado**: Execução limpa sem warnings do ByteBuddy/Mockito/OpenJDK

Para análise de cobertura, recomenda-se usar ferramentas IDE ou soluções externas quando necessário.

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
- **Email Verification Security:**
  - Tokens únicos e seguros (UUID v4)
  - Rate limiting por email (3/hora verificação, 5/hora reset)
  - Proteção contra email enumeration
  - Tokens de uso único com expiração
  - Privacy protection em logs

### **🗄️ Casos de Persistência**
- Queries customizadas
- Relacionamentos entre entidades
- Paginação
- Ordenação
- **Email System Persistence:**
  - Tabela verification_tokens com índices otimizados
  - Foreign keys com cascade DELETE
  - Queries de cleanup automático de tokens expirados
  - Atualizações em users (email_verified, email_verified_at)

---

## 🛠️ Ferramentas e Frameworks

### **📋 Testing Stack**
- **JUnit 5** - Framework de testes
- **Mockito** - Mocking framework
- **Spring Boot Test** - Testes de integração
- **Spring Security Test** - Testes de segurança
- **TestContainers** - Testes com banco real (opcional)
- **AssertJ** - Assertions fluentes
- **Spring Mail Test** - Testes de email
- **MockMvc** - Testes HTTP com security context

### **📈 Testing Tools**
- **Maven Surefire** - Test execution
- **JUnit 5** - Test framework

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

---

## 📧 Novos Testes de Email Verification (Janeiro 2025)

### **EmailServiceTest - 13 Testes**
```java
@ExtendWith(MockitoExtension.class)
class EmailServiceTest {
    @Test void sendEmailVerification_Success()
    @Test void sendEmailVerification_EmailDisabled_SkipsEmail()
    @Test void sendEmailVerification_MailSenderThrowsException_ThrowsRuntimeException()
    @Test void sendPasswordReset_Success()
    @Test void sendPasswordReset_EmailDisabled_SkipsEmail()
    @Test void sendPasswordReset_MailSenderThrowsException_ThrowsRuntimeException()
    @Test void sendWelcomeEmail_Success()
    @Test void sendWelcomeEmail_EmailDisabled_SkipsEmail()
    @Test void sendWelcomeEmail_MailSenderThrowsException_DoesNotThrowException()
    @Test void isEmailServiceHealthy_Success_ReturnsTrue()
    @Test void isEmailServiceHealthy_EmailDisabled_ReturnsFalse()
    @Test void isEmailServiceHealthy_MailSenderThrowsException_ReturnsFalse()
    // + testes de validação de conteúdo
}
```

### **VerificationTokenServiceTest - 15 Testes**
```java
@ExtendWith(MockitoExtension.class)
class VerificationTokenServiceTest {
    @Test void generateEmailVerificationToken_Success()
    @Test void generateEmailVerificationToken_UserAlreadyVerified_ThrowsException()  
    @Test void verifyEmailToken_ValidToken_VerifiesUser()
    @Test void verifyEmailToken_InvalidToken_ThrowsException()
    @Test void verifyEmailToken_ExpiredToken_ThrowsException()
    @Test void verifyEmailToken_AlreadyUsedToken_ThrowsException()
    @Test void generatePasswordResetToken_Success()
    @Test void generatePasswordResetToken_NonExistentUser_SilentReturn()
    @Test void resetPassword_ValidToken_UpdatesPassword()
    @Test void resetPassword_InvalidToken_ThrowsException()
    @Test void resetPassword_ExpiredToken_ThrowsException()
    @Test void resetPassword_AlreadyUsedToken_ThrowsException()
    @Test void validatePasswordResetToken_ValidToken_Success()
    @Test void validatePasswordResetToken_InvalidToken_ThrowsException()
    @Test void isWithinRateLimit_Success()
}
```

### **AuthControllerTest - Email Verification (11 Testes Adicionais)**
```java
@WebMvcTest(AuthController.class)
class AuthControllerTest {
    @Test void verifyEmail_ValidToken_ReturnsSuccess()
    @Test void verifyEmail_InvalidToken_ReturnsBadRequest()
    @Test void verifyEmail_ExpiredToken_ReturnsBadRequest()
    @Test void verifyEmail_AlreadyUsedToken_ReturnsBadRequest()
    @Test void resendEmailVerification_ValidEmail_ReturnsSuccess()
    @Test void resendEmailVerification_AlreadyVerifiedEmail_ReturnsBadRequest()
    @Test void resendEmailVerification_NonExistentEmail_ReturnsBadRequest()
    @Test void forgotPassword_ValidEmail_ReturnsSuccess()
    @Test void forgotPassword_NonExistentEmail_ReturnsSuccessForSecurity()
    @Test void resetPassword_ValidToken_ReturnsSuccess()
    @Test void resetPassword_InvalidToken_ReturnsBadRequest()
}
```

### **Configuração de Testes Melhorada**
```yaml
# src/test/resources/application-test.yml
spring:
  cache:
    type: none # Disable caching in tests
  data:
    redis:
      repositories:
        enabled: false # Disable Redis repositories in tests
  mail:
    host: localhost
    port: 3025
```

### **Melhorias na Infraestrutura de Testes**
- ✅ **Redis Desabilitado**: Evita falhas de conexão durante testes
- ✅ **Security Context Isolado**: `@MockBean SecurityConfig` para testes de controller
- ✅ **Email Mocking**: JavaMailSender mockado para testes unitários
- ✅ **Rate Limiting Tests**: Validação de proteção contra spam
- ✅ **Error Handling**: Testes de cenários de erro e exceções

**🎉 Com essa configuração expandida, você tem uma suite de testes ainda mais robusta incluindo o sistema completo de Email Verification!**