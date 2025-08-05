# 47_US07_Seguranca_LGPD_Criar_DTO_ConsentimentoRequest.md

### ✅ US07 – Segurança LGPD
*Como titular de dados, quero que meus dados pessoais sejam protegidos e gerenciados conforme a LGPD, para garantir minha privacidade e direitos.*

## 📋 Descrição da Tarefa
**Criar DTO ConsentimentoRequest**

Cria estrutura de dados padronizada para capturar solicitações de consentimento LGPD com validações robustas.
Implementa enum de tipos de consentimento e conversão automática para entidades de auditoria, garantindo integridade dos dados.

## 📋 Contexto da Tarefa
- **User Story:** US07 - Segurança LGPD
- **Número da Tarefa:** 47/95
- **Complexidade:** Baixa
- **Estimativa:** 1 hora
- **Dependências:** Tarefa 46
- **Sprint:** Sprint 3

## 🎯 Objetivo
Criar DTO ConsentimentoRequest para solicitações de consentimento.

## 📝 Especificação Técnica

### **Componentes a Implementar:**
- [ ] DTO ConsentimentoRequest record class
- [ ] Validações de entrada (Bean Validation)
- [ ] Enum para tipos de consentimento LGPD
- [ ] Conversão para entidades (mappers)
- [ ] Integração com NewsletterConsentLog

### **Integrações Necessárias:**
- **Com NewsletterConsentLog:** Para auditoria de consentimento
- **Com NewsletterController:** Recebimento de solicitações de consentimento
- **Com Bean Validation:** Validação de campos obrigatórios

## ✅ Acceptance Criteria
- [ ] **AC1:** DTO ConsentimentoRequest deve incluir campos: email, consentType, timestamp, ipAddress, userAgent
- [ ] **AC2:** Enum ConsentType deve ter valores: SUBSCRIBE, UNSUBSCRIBE, MARKETING, DATA_PROCESSING
- [ ] **AC3:** Validações obrigatórias: email válido, consentType não nulo, timestamp não nulo
- [ ] **AC4:** Campos opcionais: purpose, dataProcessingDetails, retentionPeriod
- [ ] **AC5:** Implementado como Java Record com anotações de validação
- [ ] **AC6:** Serialização JSON funcional para API REST

## 🧪 Testes Requeridos

### **Testes Unitários:**
- [ ] Teste de criação do DTO com todos os campos
- [ ] Teste de validação de email inválido
- [ ] Teste de validação de campos obrigatórios nulos
- [ ] Teste de serialização/deserialização JSON
- [ ] Teste de enum ConsentType

### **Testes de Integração:**
- [ ] Teste de integração com endpoint REST
- [ ] Teste de conversão para entidade NewsletterConsentLog

## 🔗 Arquivos Afetados
- [ ] **src/main/java/com/blog/api/newsletter/dto/ConsentimentoRequest.java** - DTO principal
- [ ] **src/main/java/com/blog/api/newsletter/enums/ConsentType.java** - Enum de tipos de consentimento
- [ ] **src/test/java/com/blog/api/newsletter/dto/ConsentimentoRequestTest.java** - Testes unitários
- [ ] **src/test/java/com/blog/api/newsletter/integration/ConsentimentoIntegrationTest.java** - Testes de integração

## 📚 Documentação para IA

### **Contexto do Projeto:**
- **Stack:** Java 21 + Spring Boot 3.2 + PostgreSQL + Redis
- **Arquitetura:** Clean Architecture (Controller → Service → Repository)
- **Padrões:** Builder Pattern, Java Records para DTOs, Cache-First

### **Implementação Esperada:**

**ConsentimentoRequest.java:**
```java
public record ConsentimentoRequest(
    @NotBlank @Email String email,
    @NotNull ConsentType consentType,
    @NotNull LocalDateTime timestamp,
    @NotBlank String ipAddress,
    String userAgent,
    String purpose,
    String dataProcessingDetails,
    Integer retentionPeriod
) {}
```

**ConsentType.java:**
```java
public enum ConsentType {
    SUBSCRIBE("Consentimento para receber newsletter"),
    UNSUBSCRIBE("Revogação de consentimento"),
    MARKETING("Consentimento para marketing"),
    DATA_PROCESSING("Consentimento para processamento de dados");
    
    private final String description;
}
```

### **Referências de Código:**
- **NewsletterSubscriptionRequest:** Padrão de DTO existente
- **BaseEntity:** Padrão de validações no projeto

## 🔍 Validação e Testes

### **Como Testar:**
1. Executar testes unitários: `mvn test -Dtest=ConsentimentoRequestTest`
2. Testar validações com dados inválidos (email malformado, campos nulos)
3. Testar serialização JSON via endpoint REST
4. Verificar integração com sistema de logs de consentimento
5. Testar todos os valores do enum ConsentType

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
- **Estimativa:** 1 hora
- **Real:** ___ horas *(a ser preenchido após implementação)*

### **Complexidade:**
- **Estimada:** Baixa
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
