# 57_US07_Seguranca_LGPD_Testes_Compliance_LGPD.md

### ✅ US07 – Segurança LGPD
*Como titular de dados, quero que meus dados pessoais sejam protegidos e gerenciados conforme a LGPD, para garantir minha privacidade e direitos.*

## 📋 Descrição da Tarefa
**Implementar Testes Abrangentes de Compliance LGPD**

Desenvolve suite completa de testes end-to-end cobrindo todos os aspectos LGPD: consentimento, criptografia, auditoria e retenção.
Inclui testes de segurança, performance e stress com cobertura ≥95% e validação automática na pipeline CI/CD.

## 📋 Contexto da Tarefa
- **User Story:** US07 - Segurança LGPD
- **Número da Tarefa:** 57/95
- **Complexidade:** Alta
- **Estimativa:** 5 horas
- **Dependências:** Tarefas 46-56
- **Sprint:** Sprint 3

## 🎯 Objetivo
Implementar testes abrangentes de compliance LGPD.

## 📝 Especificação Técnica

### **Componentes a Implementar:**
- [ ] Suite completa de testes de compliance LGPD
- [ ] Testes end-to-end de todos os fluxos LGPD
- [ ] Testes de segurança e privaçidade
- [ ] Testes de performance com dados sensíveis
- [ ] Testes de auditoria e logs
- [ ] Testes de retenção e exclusão de dados
- [ ] Testes de criptografia e segurança
- [ ] Testes de relatórios de compliance

### **Integrações Necessárias:**
- **Com todos os componentes LGPD:** Validação de integração completa
- **Com TestContainers:** Testes com banco real
- **Com MockMvc:** Testes de endpoints de segurança

## ✅ Acceptance Criteria
- [ ] **AC1:** Cobertura de teste ≥ 95% para todos os componentes LGPD
- [ ] **AC2:** Testes end-to-end para consentimento, exclusão e auditoria
- [ ] **AC3:** Testes de segurança: tokens, criptografia, acesso não autorizado
- [ ] **AC4:** Testes de compliance: retenção, auditoria, relatórios
- [ ] **AC5:** Testes de performance: overhead da criptografia <10ms
- [ ] **AC6:** Testes de stress: 1000+ operações simultâneas
- [ ] **AC7:** Validação de todos os cenários de erro LGPD
- [ ] **AC8:** Testes automáticos na pipeline CI/CD
- [ ] **AC9:** Relatório detalhado de compliance com métricas

## 🧪 Testes Requeridos

### **Testes Unitários:**
- [ ] ConsentimentoRequest validation e serialização
- [ ] NewsletterConsentLog persistência e criptografia
- [ ] DeletionToken geração, validação e expiração
- [ ] CryptoService encrypt/decrypt com diferentes algoritmos
- [ ] DataRetentionPolicy cálculos e aplicação
- [ ] DataAccessAudit interceptação e logging

### **Testes de Integração:**
- [ ] Fluxo completo: consentimento → auditoria → exclusão
- [ ] Segurança: tentativas de acesso malévolo
- [ ] Performance: operações com grandes volumes
- [ ] Compliance: geração de relatórios automáticos

## 🔗 Arquivos Afetados
- [ ] **src/test/java/com/blog/api/lgpd/LgpdComplianceTestSuite.java** - Suite principal
- [ ] **src/test/java/com/blog/api/lgpd/ConsentManagementIntegrationTest.java** - Testes de consentimento
- [ ] **src/test/java/com/blog/api/lgpd/DataDeletionIntegrationTest.java** - Testes de exclusão
- [ ] **src/test/java/com/blog/api/lgpd/SecurityComplianceTest.java** - Testes de segurança
- [ ] **src/test/java/com/blog/api/lgpd/PerformanceComplianceTest.java** - Testes de performance
- [ ] **src/test/java/com/blog/api/lgpd/AuditComplianceTest.java** - Testes de auditoria

## 📚 Documentação para IA

### **Contexto do Projeto:**
- **Stack:** Java 21 + Spring Boot 3.2 + PostgreSQL + Redis
- **Arquitetura:** Clean Architecture (Controller → Service → Repository)
- **Padrões:** Builder Pattern, Java Records para DTOs, Cache-First

### **Implementação Esperada:**

**LgpdComplianceTestSuite.java:**
```java
@SpringBootTest
@Testcontainers
@TestMethodOrder(OrderAnnotation.class)
public class LgpdComplianceTestSuite {
    
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");
    
    @Container
    static GenericContainer<?> redis = new GenericContainer<>("redis:7-alpine")
            .withExposedPorts(6379);
    
    @Autowired
    private TestRestTemplate restTemplate;
    
    @Autowired
    private NewsletterService newsletterService;
    
    @Test
    @Order(1)
    @DisplayName("LGPD-001: Teste completo de consentimento")
    void testConsentimentoCompleto() {
        // 1. Criar solicitação de consentimento
        ConsentimentoRequest request = new ConsentimentoRequest(
            "test@lgpd.com",
            ConsentType.SUBSCRIBE,
            LocalDateTime.now(),
            "192.168.1.1",
            "Mozilla/5.0",
            "Newsletter subscription",
            "Marketing communications",
            24
        );
        
        // 2. Enviar requisição
        ResponseEntity<String> response = restTemplate.postForEntity(
            "/api/newsletter/consent", request, String.class);
        
        // 3. Validar resposta
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        
        // 4. Verificar log de auditoria
        List<NewsletterConsentLog> logs = consentLogRepository
            .findByEmailAndTimestampAfter("test@lgpd.com", LocalDateTime.now().minusMinutes(1));
        
        assertThat(logs).hasSize(1);
        assertThat(logs.get(0).getConsentType()).isEqualTo(ConsentType.SUBSCRIBE);
        assertThat(logs.get(0).getEmail()).isEqualTo("test@lgpd.com"); // Deve estar criptografado no banco
    }
    
    @Test
    @Order(2)
    @DisplayName("LGPD-002: Teste de segurança - Tokens de exclusão")
    void testSegurancaTokenExclusao() {
        String email = "security@test.com";
        
        // 1. Criar subscriber
        newsletterService.subscribe(new NewsletterSubscriptionRequest(email));
        
        // 2. Gerar token de exclusão
        ResponseEntity<DeletionRequestResponse> tokenResponse = restTemplate.postForEntity(
            "/api/newsletter/request-deletion", 
            new DeletionRequestDto(email), 
            DeletionRequestResponse.class);
        
        assertThat(tokenResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        
        // 3. Tentar falsificar token
        String fakeToken = "fake.token.here.signature";
        
        ResponseEntity<DataDeletionResponse> deleteResponse = restTemplate.exchange(
            "/api/newsletter/delete/" + fakeToken,
            HttpMethod.DELETE,
            null,
            DataDeletionResponse.class);
        
        assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        
        // 4. Verificar que subscriber ainda existe
        Optional<NewsletterSubscriber> subscriber = subscriberRepository.findByEmail(email);
        assertThat(subscriber).isPresent();
    }
    
    @Test
    @Order(3)
    @DisplayName("LGPD-003: Teste de criptografia de dados sensíveis")
    void testCriptografiaDados() {
        String email = "crypto@test.com";
        String ipAddress = "10.0.0.1";
        
        // 1. Salvar dados sensíveis
        NewsletterConsentLog log = NewsletterConsentLog.builder()
            .email(email)
            .ipAddress(ipAddress)
            .userAgent("Test Agent")
            .consentType(ConsentType.SUBSCRIBE)
            .action(ConsentAction.GRANT)
            .legalBasis(LegalBasis.CONSENT)
            .timestamp(LocalDateTime.now())
            .build();
        
        NewsletterConsentLog saved = consentLogRepository.save(log);
        
        // 2. Verificar que dados estão criptografados no banco
        String rawEmail = jdbcTemplate.queryForObject(
            "SELECT email FROM newsletter_consent_log WHERE id = ?",
            String.class, saved.getId());
        
        // Email no banco deve estar criptografado (base64)
        assertThat(rawEmail).isNotEqualTo(email);
        assertThat(rawEmail.length()).isGreaterThan(email.length());
        
        // 3. Verificar que JPA descriptografa automaticamente
        NewsletterConsentLog retrieved = consentLogRepository.findById(saved.getId()).get();
        assertThat(retrieved.getEmail()).isEqualTo(email);
        assertThat(retrieved.getIpAddress()).isEqualTo(ipAddress);
    }
    
    @Test
    @Order(4)
    @DisplayName("LGPD-004: Teste de auditoria de acesso a dados")
    void testAuditoriaAcesso() {
        String email = "audit@test.com";
        
        // 1. Criar subscriber
        newsletterService.subscribe(new NewsletterSubscriptionRequest(email));
        
        // 2. Acessar dados (deve gerar log)
        NewsletterSubscriber subscriber = newsletterService.findByEmail(email);
        
        // 3. Verificar log de acesso
        List<DataAccessLogEntity> accessLogs = dataAccessLogRepository
            .findByUserIdAndTimestampAfter(
                "system", LocalDateTime.now().minusMinutes(1));
        
        assertThat(accessLogs).hasSizeGreaterThan(0);
        
        DataAccessLogEntity lastLog = accessLogs.get(accessLogs.size() - 1);
        assertThat(lastLog.getOperation()).isEqualTo("READ");
        assertThat(lastLog.getDataType()).isEqualTo("EMAIL");
        assertThat(lastLog.getStatus()).isEqualTo("SUCCESS");
    }
    
    @Test
    @Order(5)
    @DisplayName("LGPD-005: Teste de retenção de dados")
    void testRetencaoDados() {
        // 1. Criar dados antigos (simular)
        LocalDateTime oldDate = LocalDateTime.now().minusYears(3);
        
        NewsletterSubscriber oldSubscriber = NewsletterSubscriber.builder()
            .email("old@test.com")
            .status(SubscriptionStatus.CONFIRMED)
            .createdAt(oldDate)
            .confirmedAt(oldDate)
            .build();
        
        subscriberRepository.save(oldSubscriber);
        
        // 2. Aplicar políticas de retenção
        dataRetentionService.applyRetentionPolicies();
        
        // 3. Verificar que dados antigos foram removidos
        Optional<NewsletterSubscriber> result = subscriberRepository
            .findByEmail("old@test.com");
        
        assertThat(result).isEmpty();
        
        // 4. Verificar log de exclusão por retenção
        List<NewsletterConsentLog> retentionLogs = consentLogRepository
            .findByActionAndTimestampAfter(
                ConsentAction.RETENTION_DELETE, 
                LocalDateTime.now().minusMinutes(5));
        
        assertThat(retentionLogs).hasSizeGreaterThan(0);
    }
    
    @Test
    @Order(6)
    @DisplayName("LGPD-006: Teste de performance com criptografia")
    void testPerformanceCriptografia() {
        int numOperations = 1000;
        List<Long> encryptTimes = new ArrayList<>();
        List<Long> decryptTimes = new ArrayList<>();
        
        for (int i = 0; i < numOperations; i++) {
            String testData = "test.email." + i + "@performance.com";
            
            // Medir tempo de criptografia
            long encryptStart = System.nanoTime();
            String encrypted = cryptoService.encrypt(testData);
            long encryptTime = System.nanoTime() - encryptStart;
            encryptTimes.add(encryptTime);
            
            // Medir tempo de descriptografia
            long decryptStart = System.nanoTime();
            String decrypted = cryptoService.decrypt(encrypted);
            long decryptTime = System.nanoTime() - decryptStart;
            decryptTimes.add(decryptTime);
            
            assertThat(decrypted).isEqualTo(testData);
        }
        
        // Calcular médias
        double avgEncryptMs = encryptTimes.stream()
            .mapToLong(Long::longValue)
            .average().orElse(0) / 1_000_000.0;
        
        double avgDecryptMs = decryptTimes.stream()
            .mapToLong(Long::longValue)
            .average().orElse(0) / 1_000_000.0;
        
        // Verificar SLA de performance
        assertThat(avgEncryptMs).isLessThan(10.0); // <10ms
        assertThat(avgDecryptMs).isLessThan(10.0); // <10ms
        
        System.out.printf("Performance: Encrypt=%.2fms, Decrypt=%.2fms%n", 
            avgEncryptMs, avgDecryptMs);
    }
    
    @Test
    @Order(7)
    @DisplayName("LGPD-007: Teste de compliance - Relatório completo")
    void testComplianceReport() {
        // 1. Gerar dados de teste
        generateTestDataForCompliance();
        
        // 2. Gerar relatório de compliance
        ComplianceReport report = dataRetentionService.generateComplianceReport();
        
        // 3. Validar relatório
        assertThat(report).isNotNull();
        assertThat(report.getReportDate()).isNotNull();
        assertThat(report.getSubscribersTotal()).isGreaterThan(0);
        assertThat(report.getConsentLogsTotal()).isGreaterThan(0);
        assertThat(report.getRetentionPoliciesActive()).isGreaterThan(0);
        
        // 4. Verificar métricas de compliance
        assertThat(report.getDataEncryptionPercentage()).isEqualTo(100.0);
        assertThat(report.getAuditLogsCoverage()).isGreaterThanOrEqualTo(95.0);
        
        System.out.println("Compliance Report: " + report);
    }
    
    private void generateTestDataForCompliance() {
        // Criar subscribers de teste
        for (int i = 0; i < 10; i++) {
            newsletterService.subscribe(
                new NewsletterSubscriptionRequest("compliance" + i + "@test.com"));
        }
        
        // Criar logs de consentimento
        for (int i = 0; i < 20; i++) {
            consentLogService.logConsent(
                "compliance" + (i % 10) + "@test.com",
                ConsentType.SUBSCRIBE,
                "Test IP",
                "Test Agent");
        }
    }
}
```

**SecurityComplianceTest.java:**
```java
@SpringBootTest
@AutoConfigureMockMvc
public class SecurityComplianceTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Test
    @DisplayName("SEC-001: Teste de autenticação admin")
    void testAdminAuthentication() throws Exception {
        // Tentar acessar endpoint admin sem autenticação
        mockMvc.perform(get("/api/admin/newsletter/consent-history"))
            .andExpect(status().isUnauthorized());
        
        // Tentar com usuário comum
        mockMvc.perform(get("/api/admin/newsletter/consent-history")
                .with(user("user").roles("USER")))
            .andExpect(status().isForbidden());
        
        // Sucesso com admin
        mockMvc.perform(get("/api/admin/newsletter/consent-history")
                .with(user("admin").roles("ADMIN")))
            .andExpect(status().isOk());
    }
    
    @Test
    @DisplayName("SEC-002: Teste de rate limiting")
    void testRateLimiting() throws Exception {
        String email = "ratelimit@test.com";
        
        // Fazer múltiplas solicitações rapidamente
        for (int i = 0; i < 5; i++) {
            mockMvc.perform(post("/api/newsletter/request-deletion")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"email\":\"" + email + "\"}"))
                .andExpect(status().isOk());
        }
        
        // A próxima deve ser rejeitada
        mockMvc.perform(post("/api/newsletter/request-deletion")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"" + email + "\"}"))
            .andExpect(status().isTooManyRequests());
    }
}
```

### **Referências de Código:**
- **TestContainers:** Testes de integração existentes
- **MockMvc:** Testes de controller do projeto

## 🔍 Validação e Testes

### **Como Testar:**
1. Executar suite completa: `mvn test -Dtest=LgpdComplianceTestSuite`
2. Executar testes de segurança: `mvn test -Dtest=SecurityComplianceTest`
3. Executar testes de performance: `mvn test -Dtest=PerformanceComplianceTest`
4. Gerar relatório de cobertura: `mvn jacoco:report`
5. Executar testes de stress: `mvn test -Dtest=StressComplianceTest`
6. Validar métricas de compliance nos logs
7. Executar pipeline CI/CD completa para validação

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
- **Estimativa:** 5 horas
- **Real:** ___ horas *(a ser preenchido após implementação)*

### **Complexidade:**
- **Estimada:** Alta
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
