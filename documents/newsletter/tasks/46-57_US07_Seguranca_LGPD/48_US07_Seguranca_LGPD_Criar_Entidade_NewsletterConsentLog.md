# 48_US07_Seguranca_LGPD_Criar_Entidade_NewsletterConsentLog.md

### ✅ US07 – Segurança LGPD
*Como titular de dados, quero que meus dados pessoais sejam protegidos e gerenciados conforme a LGPD, para garantir minha privacidade e direitos.*

## 📋 Descrição da Tarefa
**Criar Entidade NewsletterConsentLog**

Implementa sistema completo de logs de auditoria LGPD com criptografia automática de dados sensíveis.
Estabelece rastreabilidade total de consentimentos com índices otimizados e retenção configurável para compliance legal.

## 📋 Contexto da Tarefa
- **User Story:** US07 - Segurança LGPD
- **Número da Tarefa:** 48/95
- **Complexidade:** Média
- **Estimativa:** 3 horas
- **Dependências:** Tarefa 01
- **Sprint:** Sprint 3

## 🎯 Objetivo
Criar entidade NewsletterConsentLog para auditoria LGPD.

## 📝 Especificação Técnica

### **Componentes a Implementar:**
- [ ] Entidade JPA NewsletterConsentLog
- [ ] Campos para auditoria LGPD completa
- [ ] Índices de performance para consultas
- [ ] Relacionamento com NewsletterSubscriber
- [ ] Configuração de retenção de dados
- [ ] Anotações para criptografia de campos sensíveis

### **Integrações Necessárias:**
- **Com NewsletterSubscriber:** Relacionamento many-to-one para rastreamento
- **Com ConsentimentoRequest:** Persistência de dados de consentimento
- **Com sistema de criptografia:** Proteção de dados pessoais

## ✅ Acceptance Criteria
- [ ] **AC1:** Entidade deve incluir: id, subscriberId, consentType, email, timestamp, ipAddress, userAgent
- [ ] **AC2:** Campos de auditoria: action, previousValue, newValue, reason, legalBasis
- [ ] **AC3:** Criptografia em campos: email, ipAddress, userAgent (dados pessoais)
- [ ] **AC4:** Índices para: email, subscriberId, timestamp, consentType
- [ ] **AC5:** Retenção configurada para 5 anos (requisito LGPD)
- [ ] **AC6:** Soft delete não permitido (log permanente para compliance)
- [ ] **AC7:** Campos não nulos: consentType, timestamp, action, legalBasis

## 🧪 Testes Requeridos

### **Testes Unitários:**
- [ ] Teste de criação de log de consentimento
- [ ] Teste de validação de campos obrigatórios
- [ ] Teste de relacionamento com NewsletterSubscriber
- [ ] Teste de criptografia de campos sensíveis
- [ ] Teste de enumerações (ConsentType, LegalBasis)

### **Testes de Integração:**
- [ ] Teste de persistência no banco de dados
- [ ] Teste de consultas com índices
- [ ] Teste de performance com grandes volumes
- [ ] Teste de retenção de dados

## 🔗 Arquivos Afetados
- [ ] **src/main/java/com/blog/api/newsletter/entity/NewsletterConsentLog.java** - Entidade principal
- [ ] **src/main/java/com/blog/api/newsletter/enums/LegalBasis.java** - Base legal LGPD
- [ ] **src/main/resources/db/migration/V008__create_newsletter_consent_log.sql** - Migração do banco
- [ ] **src/test/java/com/blog/api/newsletter/entity/NewsletterConsentLogTest.java** - Testes unitários
- [ ] **src/test/java/com/blog/api/newsletter/repository/NewsletterConsentLogRepositoryTest.java** - Testes de repositório

## 📚 Documentação para IA

### **Contexto do Projeto:**
- **Stack:** Java 21 + Spring Boot 3.2 + PostgreSQL + Redis
- **Arquitetura:** Clean Architecture (Controller → Service → Repository)
- **Padrões:** Builder Pattern, Java Records para DTOs, Cache-First

### **Implementação Esperada:**

**NewsletterConsentLog.java:**
```java
@Entity
@Table(name = "newsletter_consent_log", indexes = {
    @Index(name = "idx_consent_email", columnList = "email"),
    @Index(name = "idx_consent_subscriber", columnList = "subscriber_id"),
    @Index(name = "idx_consent_timestamp", columnList = "timestamp")
})
public class NewsletterConsentLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subscriber_id")
    private NewsletterSubscriber subscriber;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ConsentType consentType;
    
    @Convert(converter = EncryptedStringConverter.class)
    @Column(nullable = false)
    private String email;
    
    @Column(nullable = false)
    private LocalDateTime timestamp;
    
    @Convert(converter = EncryptedStringConverter.class)
    private String ipAddress;
    
    @Convert(converter = EncryptedStringConverter.class)
    private String userAgent;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ConsentAction action;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LegalBasis legalBasis;
    
    private String previousValue;
    private String newValue;
    private String reason;
    private String dataProcessingPurpose;
    private Integer retentionPeriod;
}
```

**LegalBasis.java:**
```java
public enum LegalBasis {
    CONSENT("Consentimento do titular"),
    LEGITIMATE_INTERESTS("Interesse legítimo"),
    CONTRACT("Execução de contrato"),
    LEGAL_OBLIGATION("Cumprimento de obrigação legal");
}
```

### **Referências de Código:**
- **NewsletterSubscriber:** Padrão de entidade base do projeto
- **BaseEntity:** Campos de auditoria padrão

## 🔍 Validação e Testes

### **Como Testar:**
1. Executar migração do banco: `mvn flyway:migrate`
2. Testar persistência: `mvn test -Dtest=NewsletterConsentLogTest`
3. Validar criptografia de campos sensíveis
4. Testar relacionamento com NewsletterSubscriber
5. Verificar performance de consultas com índices
6. Testar consultas de auditoria por período

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
