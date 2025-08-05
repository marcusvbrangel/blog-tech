# 56_US07_Seguranca_LGPD_Implementar_Token_Exclusao_Dados.md

### ✅ US07 – Segurança LGPD
*Como titular de dados, quero que meus dados pessoais sejam protegidos e gerenciados conforme a LGPD, para garantir minha privacidade e direitos.*

## 📋 Descrição da Tarefa
**Implementar Token Específico para Exclusão de Dados**

Desenvolve sistema seguro de tokens JWT+HMAC com uso único e expiração de 24 horas para solicitações de exclusão.
Implementa rate limiting, validação de IP e processo completo de email seguro para garantir autenticidade das solicitações.

## 📋 Contexto da Tarefa
- **User Story:** US07 - Segurança LGPD
- **Número da Tarefa:** 56/95
- **Complexidade:** Média
- **Estimativa:** 3 horas
- **Dependências:** Tarefas 09, 11, 50
- **Sprint:** Sprint 3

## 🎯 Objetivo
Implementar token específico para exclusão de dados.

## 📝 Especificação Técnica

### **Componentes a Implementar:**
- [ ] DeletionTokenService para geração e validação de tokens seguros
- [ ] Entidade DeletionToken com expiração de 24 horas
- [ ] Endpoint POST /api/newsletter/request-deletion para solicitar exclusão
- [ ] Criptografia forte para tokens (JWT + UUID + HMAC)
- [ ] Rate limiting para prevent abuso de solicitações
- [ ] Email com link seguro para exclusão
- [ ] Auditoria completa de tokens gerados e usados

### **Integrações Necessárias:**
- **Com JWT Service:** Tokens assinados e com expiração
- **Com EmailService:** Envio de link de exclusão por email
- **Com NewsletterService:** Validação e execução da exclusão

## ✅ Acceptance Criteria
- [ ] **AC1:** Token seguro: JWT + UUID + HMAC-SHA256 para prevenir falsificação
- [ ] **AC2:** Expiração de 24 horas após geração
- [ ] **AC3:** Rate limiting: máximo 3 solicitações por email por dia
- [ ] **AC4:** Token usado apenas uma vez (one-time use)
- [ ] **AC5:** Email deve existir no sistema para gerar token
- [ ] **AC6:** Link de exclusão enviado por email com template seguro
- [ ] **AC7:** Validação de IP na geração e uso do token
- [ ] **AC8:** Logs de auditoria para geração, uso e expiração de tokens
- [ ] **AC9:** Limpeza automática de tokens expirados

## 🧪 Testes Requeridos

### **Testes Unitários:**
- [ ] Teste de geração de token seguro
- [ ] Teste de validação de token válido/inválido
- [ ] Teste de expiração de token
- [ ] Teste de rate limiting
- [ ] Teste de one-time use
- [ ] Teste de validação de IP

### **Testes de Integração:**
- [ ] Teste end-to-end: solicitação → email → exclusão
- [ ] Teste de segurança: tentativas de falsificação
- [ ] Teste de limpeza automática de tokens
- [ ] Teste de performance sob alta carga

## 🔗 Arquivos Afetados
- [ ] **src/main/java/com/blog/api/security/token/DeletionTokenService.java** - Serviço principal
- [ ] **src/main/java/com/blog/api/security/token/entity/DeletionToken.java** - Entidade
- [ ] **src/main/java/com/blog/api/newsletter/controller/NewsletterController.java** - Endpoint solicitação
- [ ] **src/main/java/com/blog/api/email/templates/deletion-request.html** - Template email
- [ ] **src/main/java/com/blog/api/scheduler/TokenCleanupScheduler.java** - Limpeza tokens
- [ ] **src/main/resources/db/migration/V013__create_deletion_token.sql** - Migração

## 📚 Documentação para IA

### **Contexto do Projeto:**
- **Stack:** Java 21 + Spring Boot 3.2 + PostgreSQL + Redis
- **Arquitetura:** Clean Architecture (Controller → Service → Repository)
- **Padrões:** Builder Pattern, Java Records para DTOs, Cache-First

### **Implementação Esperada:**

**DeletionToken.java:**
```java
@Entity
@Table(name = "deletion_token")
public class DeletionToken {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String token;
    
    @Column(nullable = false)
    private String email;
    
    @Column(nullable = false)
    private String ipAddress;
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    @Column(nullable = false)
    private LocalDateTime expiresAt;
    
    @Column(nullable = false)
    private Boolean used = false;
    
    private LocalDateTime usedAt;
    
    private String usedFromIp;
    
    // Getters, setters...
}
```

**DeletionTokenService.java:**
```java
@Service
@Transactional
public class DeletionTokenService {
    
    private static final int TOKEN_EXPIRY_HOURS = 24;
    private static final int MAX_REQUESTS_PER_EMAIL_PER_DAY = 3;
    
    @Autowired
    private DeletionTokenRepository tokenRepository;
    
    @Autowired
    private JwtService jwtService;
    
    @Autowired
    private NewsletterRepository subscriberRepository;
    
    @Value("${app.security.deletion-token.secret}")
    private String deletionTokenSecret;
    
    public String generateDeletionToken(String email, String ipAddress) {
        // Verificar rate limiting
        validateRateLimit(email);
        
        // Verificar se email existe no sistema
        NewsletterSubscriber subscriber = subscriberRepository.findByEmail(email)
            .orElseThrow(() -> new SubscriberNotFoundException(email));
        
        // Invalidar tokens anteriores do mesmo email
        tokenRepository.invalidateByEmail(email);
        
        // Gerar token seguro
        String uuid = UUID.randomUUID().toString();
        String payload = email + "|" + ipAddress + "|" + System.currentTimeMillis();
        
        // Criar JWT com payload
        String jwtToken = jwtService.createToken(
            Map.of(
                "email", email,
                "uuid", uuid,
                "ip", ipAddress,
                "purpose", "DATA_DELETION"
            ),
            Duration.ofHours(TOKEN_EXPIRY_HOURS)
        );
        
        // Assinar com HMAC para segurança adicional
        String signature = createHmacSignature(jwtToken);
        String finalToken = jwtToken + "." + signature;
        
        // Salvar no banco
        DeletionToken deletionToken = DeletionToken.builder()
            .token(finalToken)
            .email(email)
            .ipAddress(ipAddress)
            .createdAt(LocalDateTime.now())
            .expiresAt(LocalDateTime.now().plusHours(TOKEN_EXPIRY_HOURS))
            .build();
        
        tokenRepository.save(deletionToken);
        
        // Log de auditoria
        auditService.logTokenGeneration(email, ipAddress, finalToken);
        
        return finalToken;
    }
    
    public DeletionToken validateDeletionToken(String token) {
        // Buscar token no banco
        DeletionToken deletionToken = tokenRepository.findByToken(token)
            .orElseThrow(() -> new InvalidTokenException("Token não encontrado"));
        
        // Verificar se já foi usado
        if (deletionToken.getUsed()) {
            throw new InvalidTokenException("Token já foi utilizado");
        }
        
        // Verificar expiração
        if (deletionToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new InvalidTokenException("Token expirado");
        }
        
        // Validar estrutura do token
        validateTokenStructure(token);
        
        return deletionToken;
    }
    
    public void markTokenAsUsed(String token, String ipAddress) {
        DeletionToken deletionToken = tokenRepository.findByToken(token)
            .orElseThrow(() -> new InvalidTokenException("Token não encontrado"));
        
        deletionToken.setUsed(true);
        deletionToken.setUsedAt(LocalDateTime.now());
        deletionToken.setUsedFromIp(ipAddress);
        
        tokenRepository.save(deletionToken);
        
        // Log de auditoria
        auditService.logTokenUsage(deletionToken.getEmail(), ipAddress, token);
    }
    
    private void validateRateLimit(String email) {
        LocalDateTime yesterday = LocalDateTime.now().minusDays(1);
        long requestsToday = tokenRepository.countByEmailAndCreatedAtAfter(email, yesterday);
        
        if (requestsToday >= MAX_REQUESTS_PER_EMAIL_PER_DAY) {
            throw new RateLimitExceededException(
                "Máximo de " + MAX_REQUESTS_PER_EMAIL_PER_DAY + " solicitações por dia excedido");
        }
    }
    
    private void validateTokenStructure(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 4) { // JWT tem 3 partes + nossa assinatura
                throw new InvalidTokenException("Estrutura de token inválida");
            }
            
            String jwtPart = String.join(".", parts[0], parts[1], parts[2]);
            String signature = parts[3];
            
            // Validar JWT
            if (!jwtService.isTokenValid(jwtPart)) {
                throw new InvalidTokenException("JWT inválido");
            }
            
            // Validar assinatura HMAC
            String expectedSignature = createHmacSignature(jwtPart);
            if (!MessageDigest.isEqual(signature.getBytes(), expectedSignature.getBytes())) {
                throw new InvalidTokenException("Assinatura inválida");
            }
            
        } catch (Exception e) {
            throw new InvalidTokenException("Erro na validação do token: " + e.getMessage());
        }
    }
    
    private String createHmacSignature(String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(deletionTokenSecret.getBytes(), "HmacSHA256");
            mac.init(secretKey);
            
            byte[] hash = mac.doFinal(data.getBytes());
            return Base64.getEncoder().encodeToString(hash);
            
        } catch (Exception e) {
            throw new RuntimeException("Erro ao criar assinatura HMAC", e);
        }
    }
}
```

**NewsletterController.java (endpoint de solicitação):**
```java
@PostMapping("/request-deletion")
@RateLimited(maxAttempts = 5, windowHours = 1)
public ResponseEntity<DeletionRequestResponse> requestDeletion(
    @Valid @RequestBody DeletionRequestDto request,
    HttpServletRequest httpRequest) {
    
    String ipAddress = getClientIpAddress(httpRequest);
    
    try {
        String token = deletionTokenService.generateDeletionToken(request.email(), ipAddress);
        
        // Enviar email com link de exclusão
        String deletionLink = buildDeletionLink(token);
        emailService.sendDeletionRequestEmail(request.email(), deletionLink);
        
        return ResponseEntity.ok(new DeletionRequestResponse(
            true, 
            "Link de exclusão enviado para o email",
            LocalDateTime.now().plusHours(24) // expiração
        ));
        
    } catch (RateLimitExceededException e) {
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
            .body(new DeletionRequestResponse(false, e.getMessage(), null));
    } catch (SubscriberNotFoundException e) {
        // Não revelar se email existe ou não (segurança)
        return ResponseEntity.ok(new DeletionRequestResponse(
            true,
            "Se o email existir em nossa base, um link será enviado",
            null
        ));
    }
}
```

### **Referências de Código:**
- **JwtService:** Padrões de token do projeto
- **EmailService:** Envio de emails existente

## 🔍 Validação e Testes

### **Como Testar:**
1. Solicitar exclusão: `POST /api/newsletter/request-deletion {"email": "test@test.com"}`
2. Verificar email enviado com link de exclusão
3. Testar token no endpoint DELETE: usar token do email
4. Tentar usar token novamente (deve falhar - one-time use)
5. Testar rate limiting: fazer 4 solicitações no mesmo dia
6. Testar expiração: aguardar 24h e tentar usar token
7. Verificar logs de auditoria para todas as operações

### **Critérios de Sucesso:**
- [ ] Funcionalidade implementada
- [ ] Testes passando
- [ ] Performance adequada

## ✅ Definition of Done

### **Código:**
- [ ] Implementação completa seguindo padrões do projeto
- [ ] Code review interno (self-review)
- [ ] Sem warnings ou erros de compilação
- [ ] Logging apropriado implementado

### **Testes:**
- [ ] Testes unitários implementados e passando
- [ ] Testes de integração implementados (se aplicável)
- [ ] Cobertura de código ≥ 85% para componentes novos
- [ ] Todos os ACs validados via testes

### **Documentação:**
- [ ] Javadoc atualizado para métodos públicos
- [ ] Swagger/OpenAPI atualizado (se endpoint)
- [ ] README atualizado (se necessário)
- [ ] Este arquivo de tarefa atualizado com notas de implementação

### **Quality Gates:**
- [ ] Performance dentro dos SLAs (< 200ms para endpoints)
- [ ] Security validation (input validation, authorization)
- [ ] OWASP compliance (se aplicável)
- [ ] Cache strategy implementada (se aplicável)

## 📊 Métricas

### **Estimativa vs Real:**
- **Estimativa:** 3 horas
- **Real:** ___ horas *(a ser preenchido após implementação)*

### **Complexidade:**
- **Estimada:** Média
- **Real:** _____ *(a ser preenchido após implementação)*

## 📝 Notas de Implementação
*[Este espaço será preenchido durante a implementação com descobertas, decisões técnicas, e observações importantes]*

### **Decisões Técnicas:**
- [Decisão 1: justificativa]
- [Decisão 2: justificativa]

### **Descobertas:**
- [Descoberta 1: impacto]
- [Descoberta 2: impacto]

### **Refactorings Necessários:**
- [Refactoring 1: razão]
- [Refactoring 2: razão]

## 📊 Status Tracking

### **Status Atual:**
- [x] 📋 **Todo** - Não iniciada
- [ ] 🔄 **In Progress** - Em desenvolvimento  
- [ ] 👀 **Code Review** - Aguardando revisão
- [ ] ✅ **Done** - Concluída e validada

### **Bloqueadores:**
*[Lista de impedimentos, se houver]*

### **Next Steps:**
*[Próxima tarefa da sequência]*

---

**Criado em:** Agosto 2025  
**Última Atualização:** Agosto 2025  
**Responsável:** AI-Driven Development  
**Reviewer:** [Nome do reviewer, se aplicável]
