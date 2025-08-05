# 49_US07_Seguranca_LGPD_Implementar_NewsletterConsentLogRepository.md

### ✅ US07 – Segurança LGPD
*Como titular de dados, quero que meus dados pessoais sejam protegidos e gerenciados conforme a LGPD, para garantir minha privacidade e direitos.*

## 📋 Descrição da Tarefa
**Implementar NewsletterConsentLogRepository**

Desenvolve camada de acesso a dados especializada para logs de consentimento com consultas otimizadas para auditoria.
Fornece métodos customizados para relatórios de compliance e paginação eficiente de grandes volumes de logs LGPD.

## 📋 Contexto da Tarefa
- **User Story:** US07 - Segurança LGPD
- **Número da Tarefa:** 49/95
- **Complexidade:** Baixa
- **Estimativa:** 1 hora
- **Dependências:** Tarefa 48
- **Sprint:** Sprint 3

## 🎯 Objetivo
Implementar repository para logs de consentimento.

## 📝 Especificação Técnica

### **Componentes a Implementar:**
- [ ] Interface NewsletterConsentLogRepository estendendo JpaRepository
- [ ] Métodos de consulta customizados para auditoria LGPD
- [ ] Consultas paginadas para relatórios de compliance
- [ ] Métodos de busca por período e tipo de consentimento
- [ ] Consultas para relatórios de retenção de dados
- [ ] Consultas de auditoria por usuário

### **Integrações Necessárias:**
- **Com NewsletterConsentLog:** CRUD e consultas customizadas
- **Com NewsletterService:** Persistência de logs de consentimento
- **Com AdminController:** Relatórios de compliance para administradores

## ✅ Acceptance Criteria
- [ ] **AC1:** Repository deve estender JpaRepository<NewsletterConsentLog, Long>
- [ ] **AC2:** Método findByEmailAndTimestampBetween para auditoria por usuário
- [ ] **AC3:** Método findByConsentTypeAndTimestampBetween para relatórios por tipo
- [ ] **AC4:** Método findBySubscriberIdOrderByTimestampDesc para histórico
- [ ] **AC5:** Método countByConsentTypeAndTimestampBetween para métricas
- [ ] **AC6:** Paginação implementada em consultas de grande volume
- [ ] **AC7:** @Query customizada para relatórios complexos de compliance

## 🧪 Testes Requeridos

### **Testes Unitários:**
- [ ] Teste de CRUD básico (save, findById, delete)
- [ ] Teste de métodos de consulta customizados
- [ ] Teste de paginação em consultas
- [ ] Teste de consultas por período
- [ ] Teste de consultas por tipo de consentimento

### **Testes de Integração:**
- [ ] Teste de persistência com banco H2
- [ ] Teste de performance com grande volume de dados
- [ ] Teste de consultas complexas com joins
- [ ] Teste de índices de performance

## 🔗 Arquivos Afetados
- [ ] **src/main/java/com/blog/api/newsletter/repository/NewsletterConsentLogRepository.java** - Interface principal
- [ ] **src/test/java/com/blog/api/newsletter/repository/NewsletterConsentLogRepositoryTest.java** - Testes unitários
- [ ] **src/test/java/com/blog/api/newsletter/integration/ConsentLogRepositoryIntegrationTest.java** - Testes de integração

## 📚 Documentação para IA

### **Contexto do Projeto:**
- **Stack:** Java 21 + Spring Boot 3.2 + PostgreSQL + Redis
- **Arquitetura:** Clean Architecture (Controller → Service → Repository)
- **Padrões:** Builder Pattern, Java Records para DTOs, Cache-First

### **Implementação Esperada:**

**NewsletterConsentLogRepository.java:**
```java
@Repository
public interface NewsletterConsentLogRepository extends JpaRepository<NewsletterConsentLog, Long> {
    
    // Busca logs por email e período (para auditoria de usuário)
    List<NewsletterConsentLog> findByEmailAndTimestampBetween(
        String email, LocalDateTime start, LocalDateTime end);
    
    // Busca logs por tipo de consentimento e período
    Page<NewsletterConsentLog> findByConsentTypeAndTimestampBetween(
        ConsentType consentType, LocalDateTime start, LocalDateTime end, Pageable pageable);
    
    // Busca histórico de um subscriber
    List<NewsletterConsentLog> findBySubscriberIdOrderByTimestampDesc(Long subscriberId);
    
    // Conta logs por tipo e período (para métricas)
    Long countByConsentTypeAndTimestampBetween(
        ConsentType consentType, LocalDateTime start, LocalDateTime end);
    
    // Busca logs por ação específica
    List<NewsletterConsentLog> findByActionAndTimestampBetween(
        ConsentAction action, LocalDateTime start, LocalDateTime end);
    
    // Relatório de compliance - logs por base legal
    @Query("SELECT c FROM NewsletterConsentLog c WHERE c.legalBasis = :legalBasis " +
           "AND c.timestamp BETWEEN :start AND :end ORDER BY c.timestamp DESC")
    Page<NewsletterConsentLog> findComplianceReport(
        @Param("legalBasis") LegalBasis legalBasis,
        @Param("start") LocalDateTime start,
        @Param("end") LocalDateTime end,
        Pageable pageable);
    
    // Busca por IP para detecção de padrões suspeitos
    @Query("SELECT c FROM NewsletterConsentLog c WHERE c.ipAddress = :ipAddress " +
           "AND c.timestamp >= :since ORDER BY c.timestamp DESC")
    List<NewsletterConsentLog> findByIpAddressSince(
        @Param("ipAddress") String ipAddress,
        @Param("since") LocalDateTime since);
    
    // Logs para limpeza por retenção
    @Query("SELECT c FROM NewsletterConsentLog c WHERE c.timestamp < :retentionDate")
    List<NewsletterConsentLog> findLogsForRetentionCleanup(
        @Param("retentionDate") LocalDateTime retentionDate);
}
```

### **Referências de Código:**
- **NewsletterRepository:** Padrão de repository do projeto
- **PostRepository:** Referência para consultas customizadas

## 🔍 Validação e Testes

### **Como Testar:**
1. Executar testes unitários: `mvn test -Dtest=NewsletterConsentLogRepositoryTest`
2. Testar persistência: criar logs e verificar consultas
3. Testar consultas customizadas com dados de teste
4. Verificar performance de consultas paginadas
5. Testar consultas por período com diferentes ranges
6. Validar índices executando EXPLAIN PLAN

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
