# 54_US07_Seguranca_LGPD_Implementar_Soft_Delete_Compliance.md

### ✅ US07 – Segurança LGPD
*Como titular de dados, quero que meus dados pessoais sejam protegidos e gerenciados conforme a LGPD, para garantir minha privacidade e direitos.*

## 📋 Descrição da Tarefa
**Implementar Soft Delete para Compliance LGPD**

Implementa sistema de exclusão lógica com período de grace de 30 dias e hard delete automático posterior.
Fornece funcionalidades de restore administrativo e limpeza schedulada, mantendo trilha de auditoria completa para compliance.

## 📋 Contexto da Tarefa
- **User Story:** US07 - Segurança LGPD
- **Número da Tarefa:** 54/95
- **Complexidade:** Média
- **Estimativa:** 3 horas
- **Dependências:** Tarefas 01, 50
- **Sprint:** Sprint 3

## 🎯 Objetivo
Implementar soft delete para compliance LGPD.

## 📝 Especificação Técnica

### **Componentes a Implementar:**
- [ ] Soft delete em NewsletterSubscriber com campo deletedAt
- [ ] @SQLDelete e @Where annotations para Hibernate
- [ ] Service methods para soft delete e restore
- [ ] Consultas que respeitam soft delete automaticamente
- [ ] Hard delete após período de grace (30 dias)
- [ ] Dashboard admin para gerenciar itens deletados
- [ ] Job schedulado para limpeza automática

### **Integrações Necessárias:**
- **Com Hibernate:** Anotações para soft delete automático
- **Com Spring Scheduler:** Job de limpeza periódica
- **Com AdminController:** Interface para gerenciar itens deletados

## ✅ Acceptance Criteria
- [ ] **AC1:** Campo deletedAt (nullable) em NewsletterSubscriber
- [ ] **AC2:** @SQLDelete annotation para UPDATE em vez de DELETE
- [ ] **AC3:** @Where annotation para filtrar registros não deletados
- [ ] **AC4:** Métodos softDelete() e restore() em NewsletterService
- [ ] **AC5:** Consultas normais ignoram registros soft-deleted automaticamente
- [ ] **AC6:** findDeleted() e findAll(includeDeleted=true) para admins
- [ ] **AC7:** Hard delete automático após 30 dias
- [ ] **AC8:** Log de auditoria para soft delete e restore
- [ ] **AC9:** Dashboard admin para visualizar e restaurar itens

## 🧪 Testes Requeridos

### **Testes Unitários:**
- [ ] Teste de soft delete (deletedAt preenchido)
- [ ] Teste de consultas que ignoram registros deletados
- [ ] Teste de restore (deletedAt = null)
- [ ] Teste de findDeleted() para admins
- [ ] Teste de hard delete após período de grace
- [ ] Teste de job de limpeza automática

### **Testes de Integração:**
- [ ] Teste end-to-end de soft delete via API
- [ ] Teste de dashboard administrativo
- [ ] Teste de job schedulador
- [ ] Teste de auditoria de soft delete

## 🔗 Arquivos Afetados
- [ ] **src/main/java/com/blog/api/newsletter/entity/NewsletterSubscriber.java** - Soft delete annotations
- [ ] **src/main/java/com/blog/api/newsletter/service/NewsletterService.java** - Métodos soft delete
- [ ] **src/main/java/com/blog/api/newsletter/repository/NewsletterRepository.java** - Consultas customizadas
- [ ] **src/main/java/com/blog/api/admin/controller/DeletedItemsController.java** - Dashboard admin
- [ ] **src/main/java/com/blog/api/scheduler/DataCleanupScheduler.java** - Job de limpeza
- [ ] **src/main/resources/db/migration/V011__add_soft_delete_support.sql** - Migração

## 📚 Documentação para IA

### **Contexto do Projeto:**
- **Stack:** Java 21 + Spring Boot 3.2 + PostgreSQL + Redis
- **Arquitetura:** Clean Architecture (Controller → Service → Repository)
- **Padrões:** Builder Pattern, Java Records para DTOs, Cache-First

### **Implementação Esperada:**

**NewsletterSubscriber.java (com Soft Delete):**
```java
@Entity
@Table(name = "newsletter_subscriber")
@SQLDelete(sql = "UPDATE newsletter_subscriber SET deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
@Where(clause = "deleted_at IS NULL")
public class NewsletterSubscriber {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String email;
    
    @Enumerated(EnumType.STRING)
    private SubscriptionStatus status;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "confirmed_at")
    private LocalDateTime confirmedAt;
    
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
    
    // Getters, setters, etc.
}
```

**NewsletterService.java (com Soft Delete):**
```java
@Service
@Transactional
public class NewsletterService {
    
    public void softDelete(String email) {
        NewsletterSubscriber subscriber = findByEmail(email);
        
        // Hibernate irá usar SQLDelete automaticamente
        subscriberRepository.delete(subscriber);
        
        // Log de auditoria
        consentLogService.logSoftDelete(subscriber, getCurrentUserId());
    }
    
    public void restore(Long subscriberId) {
        // Consulta incluindo soft-deleted
        NewsletterSubscriber subscriber = subscriberRepository
            .findByIdIncludingDeleted(subscriberId)
            .orElseThrow(() -> new SubscriberNotFoundException(subscriberId));
        
        if (subscriber.getDeletedAt() == null) {
            throw new IllegalStateException("Subscriber não está deletado");
        }
        
        subscriber.setDeletedAt(null);
        subscriberRepository.save(subscriber);
        
        // Log de auditoria
        consentLogService.logRestore(subscriber, getCurrentUserId());
    }
    
    public void hardDelete(Long subscriberId) {
        NewsletterSubscriber subscriber = subscriberRepository
            .findByIdIncludingDeleted(subscriberId)
            .orElseThrow(() -> new SubscriberNotFoundException(subscriberId));
        
        // Log antes da exclusão definitiva
        consentLogService.logHardDelete(subscriber, getCurrentUserId());
        
        // Exclusão definitiva do banco
        subscriberRepository.hardDelete(subscriberId);
    }
}
```

**NewsletterRepository.java:**
```java
@Repository
public interface NewsletterRepository extends JpaRepository<NewsletterSubscriber, Long> {
    
    // Consulta padrão (ignora soft-deleted automaticamente devido ao @Where)
    Optional<NewsletterSubscriber> findByEmail(String email);
    
    // Consulta incluindo soft-deleted (para admins)
    @Query("SELECT s FROM NewsletterSubscriber s WHERE s.id = :id")
    Optional<NewsletterSubscriber> findByIdIncludingDeleted(@Param("id") Long id);
    
    // Buscar apenas soft-deleted
    @Query("SELECT s FROM NewsletterSubscriber s WHERE s.deletedAt IS NOT NULL")
    Page<NewsletterSubscriber> findDeleted(Pageable pageable);
    
    // Hard delete (exclusão definitiva)
    @Modifying
    @Query("DELETE FROM NewsletterSubscriber s WHERE s.id = :id")
    void hardDelete(@Param("id") Long id);
    
    // Buscar registros para limpeza (soft-deleted há mais de 30 dias)
    @Query("SELECT s FROM NewsletterSubscriber s WHERE s.deletedAt < :cutoffDate")
    List<NewsletterSubscriber> findForHardDeletion(@Param("cutoffDate") LocalDateTime cutoffDate);
}
```

**DataCleanupScheduler.java:**
```java
@Component
public class DataCleanupScheduler {
    
    @Autowired
    private NewsletterService newsletterService;
    
    // Executar diariamente às 2:00 AM
    @Scheduled(cron = "0 0 2 * * *")
    public void cleanupSoftDeletedData() {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(30);
        
        List<NewsletterSubscriber> expiredSubscribers = 
            subscriberRepository.findForHardDeletion(cutoffDate);
        
        for (NewsletterSubscriber subscriber : expiredSubscribers) {
            try {
                newsletterService.hardDelete(subscriber.getId());
                log.info("Hard deleted expired subscriber: {}", subscriber.getEmail());
            } catch (Exception e) {
                log.error("Error hard deleting subscriber {}: {}", 
                    subscriber.getEmail(), e.getMessage());
            }
        }
    }
}
```

### **Referências de Código:**
- **Hibernate Annotations:** Padrões JPA do projeto
- **Spring Scheduler:** Jobs existentes no projeto

## 🔍 Validação e Testes

### **Como Testar:**
1. Testar soft delete: `newsletterService.softDelete("test@test.com")`
2. Verificar no banco: `SELECT * FROM newsletter_subscriber WHERE email = 'test@test.com'` (deve ter deleted_at preenchido)
3. Testar consulta normal: `findByEmail("test@test.com")` (deve retornar vazio)
4. Testar restore: `newsletterService.restore(subscriberId)`
5. Testar job de limpeza: executar manualmente ou aguardar schedule
6. Testar dashboard admin: visualizar itens soft-deleted
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
