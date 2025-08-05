# 55_US07_Seguranca_LGPD_Configurar_Data_Retention_Policies.md

### ✅ US07 – Segurança LGPD
*Como titular de dados, quero que meus dados pessoais sejam protegidos e gerenciados conforme a LGPD, para garantir minha privacidade e direitos.*

## 📋 Descrição da Tarefa
**Configurar Políticas de Retenção de Dados**

Estabelece sistema automatizado de retenção com políticas diferenciadas por tipo de dado conforme requisitos LGPD.
Implementa alertas pré-exclusão, backup seguro e relatórios mensais de compliance com execução schedulada.

## 📋 Contexto da Tarefa
- **User Story:** US07 - Segurança LGPD
- **Número da Tarefa:** 55/95
- **Complexidade:** Baixa
- **Estimativa:** 2 horas
- **Dependências:** Tarefas 48, 54
- **Sprint:** Sprint 3

## 🎯 Objetivo
Configurar políticas de retenção de dados.

## 📝 Especificação Técnica

### **Componentes a Implementar:**
- [ ] DataRetentionPolicyService para gestão de políticas
- [ ] Configuração de políticas por tipo de dado (LGPD)
- [ ] Job schedulado para aplicação de políticas
- [ ] Entidade DataRetentionPolicy para armazenar regras
- [ ] Dashboard admin para gerenciar políticas
- [ ] Alertas antes da exclusão por retenção
- [ ] Relatórios de compliance de retenção

### **Integrações Necessárias:**
- **Com Spring Scheduler:** Execução automática de políticas
- **Com NewsletterConsentLog:** Retenção de logs de auditoria (5 anos)
- **Com NewsletterSubscriber:** Retenção de dados de subscriber

## ✅ Acceptance Criteria
- [ ] **AC1:** Políticas configuráveis por tipo: SUBSCRIBER_DATA (2 anos), CONSENT_LOGS (5 anos)
- [ ] **AC2:** Job diário para verificar e aplicar políticas de retenção
- [ ] **AC3:** Alertas 30 dias antes da exclusão por retenção
- [ ] **AC4:** Dashboard admin para visualizar e gerenciar políticas
- [ ] **AC5:** Configuração flexível via application.yml
- [ ] **AC6:** Logs de auditoria para todas as exclusões por retenção
- [ ] **AC7:** Relatório mensal de compliance de retenção
- [ ] **AC8:** Exempções para dados sob investigação legal
- [ ] **AC9:** Backup antes da exclusão por retenção

## 🧪 Testes Requeridos

### **Testes Unitários:**
- [ ] Teste de cálculo de períodos de retenção
- [ ] Teste de aplicação de políticas
- [ ] Teste de exempções legais
- [ ] Teste de geração de alertas
- [ ] Teste de configuração dinâmica
- [ ] Teste de logs de auditoria

### **Testes de Integração:**
- [ ] Teste de job schedulado
- [ ] Teste de dashboard administrativo
- [ ] Teste de integração com backup
- [ ] Teste de relatórios de compliance

## 🔗 Arquivos Afetados
- [ ] **src/main/java/com/blog/api/data/retention/DataRetentionPolicyService.java** - Serviço principal
- [ ] **src/main/java/com/blog/api/data/retention/entity/DataRetentionPolicy.java** - Entidade
- [ ] **src/main/java/com/blog/api/scheduler/DataRetentionScheduler.java** - Job schedulado
- [ ] **src/main/java/com/blog/api/admin/controller/DataRetentionController.java** - Dashboard
- [ ] **src/main/resources/application.yml** - Configurações de políticas
- [ ] **src/main/resources/db/migration/V012__create_data_retention_policy.sql** - Migração

## 📚 Documentação para IA

### **Contexto do Projeto:**
- **Stack:** Java 21 + Spring Boot 3.2 + PostgreSQL + Redis
- **Arquitetura:** Clean Architecture (Controller → Service → Repository)
- **Padrões:** Builder Pattern, Java Records para DTOs, Cache-First

### **Implementação Esperada:**

**application.yml (Configuração):**
```yaml
data:
  retention:
    policies:
      subscriber-data:
        retention-period: P2Y  # 2 anos (ISO 8601)
        warning-period: P30D   # 30 dias antes
        enabled: true
      consent-logs:
        retention-period: P5Y  # 5 anos (LGPD)
        warning-period: P30D
        enabled: true
      access-logs:
        retention-period: P7Y  # 7 anos (auditoria)
        warning-period: P30D
        enabled: true
    schedule:
      cron: "0 0 3 * * *"  # Todo dia às 3:00 AM
      enabled: true
```

**DataRetentionPolicy.java:**
```java
@Entity
@Table(name = "data_retention_policy")
public class DataRetentionPolicy {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String dataType;
    
    @Column(nullable = false)
    private Period retentionPeriod;
    
    @Column(nullable = false)
    private Period warningPeriod;
    
    @Column(nullable = false)
    private Boolean enabled = true;
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    // Getters, setters...
}
```

**DataRetentionPolicyService.java:**
```java
@Service
@Transactional
public class DataRetentionPolicyService {
    
    @Autowired
    private DataRetentionPolicyRepository policyRepository;
    
    @Autowired
    private NewsletterRepository subscriberRepository;
    
    @Autowired
    private NewsletterConsentLogRepository consentLogRepository;
    
    @Autowired
    private BackupService backupService;
    
    @Autowired
    private AlertService alertService;
    
    public void applyRetentionPolicies() {
        List<DataRetentionPolicy> policies = policyRepository.findByEnabledTrue();
        
        for (DataRetentionPolicy policy : policies) {
            try {
                applyPolicy(policy);
                log.info("Applied retention policy for {}", policy.getDataType());
            } catch (Exception e) {
                log.error("Error applying retention policy for {}: {}", 
                    policy.getDataType(), e.getMessage());
            }
        }
    }
    
    private void applyPolicy(DataRetentionPolicy policy) {
        LocalDateTime cutoffDate = LocalDateTime.now().minus(policy.getRetentionPeriod());
        LocalDateTime warningDate = LocalDateTime.now().minus(
            policy.getRetentionPeriod().minus(policy.getWarningPeriod()));
        
        switch (policy.getDataType()) {
            case "subscriber-data" -> applySubscriberRetention(cutoffDate, warningDate);
            case "consent-logs" -> applyConsentLogRetention(cutoffDate, warningDate);
            case "access-logs" -> applyAccessLogRetention(cutoffDate, warningDate);
            default -> log.warn("Unknown data type for retention: {}", policy.getDataType());
        }
    }
    
    private void applySubscriberRetention(LocalDateTime cutoffDate, LocalDateTime warningDate) {
        // Buscar subscribers para exclusão
        List<NewsletterSubscriber> toDelete = subscriberRepository.findOlderThan(cutoffDate);
        
        // Buscar subscribers para alerta
        List<NewsletterSubscriber> toWarn = subscriberRepository.findBetween(warningDate, cutoffDate);
        
        // Enviar alertas
        for (NewsletterSubscriber subscriber : toWarn) {
            alertService.sendRetentionWarning(subscriber);
        }
        
        // Fazer backup e excluir
        for (NewsletterSubscriber subscriber : toDelete) {
            backupService.backupSubscriber(subscriber);
            
            // Log de auditoria
            consentLogService.logRetentionDeletion(subscriber, "RETENTION_POLICY");
            
            // Exclusão definitiva
            subscriberRepository.hardDelete(subscriber.getId());
        }
    }
    
    private void applyConsentLogRetention(LocalDateTime cutoffDate, LocalDateTime warningDate) {
        // Consent logs têm retenção mais longa (5 anos)
        List<NewsletterConsentLog> toDelete = consentLogRepository.findOlderThan(cutoffDate);
        
        for (NewsletterConsentLog log : toDelete) {
            backupService.backupConsentLog(log);
            consentLogRepository.delete(log);
        }
    }
    
    public ComplianceReport generateComplianceReport() {
        return ComplianceReport.builder()
            .reportDate(LocalDateTime.now())
            .subscribersTotal(subscriberRepository.count())
            .subscribersNearRetention(getSubscribersNearRetention())
            .consentLogsTotal(consentLogRepository.count())
            .consentLogsNearRetention(getConsentLogsNearRetention())
            .retentionPoliciesActive(policyRepository.countByEnabledTrue())
            .build();
    }
}
```

**DataRetentionScheduler.java:**
```java
@Component
@ConditionalOnProperty(name = "data.retention.schedule.enabled", havingValue = "true")
public class DataRetentionScheduler {
    
    @Autowired
    private DataRetentionPolicyService retentionService;
    
    @Scheduled(cron = "${data.retention.schedule.cron}")
    public void executeRetentionPolicies() {
        log.info("Starting data retention policy execution");
        
        try {
            retentionService.applyRetentionPolicies();
            log.info("Data retention policy execution completed successfully");
        } catch (Exception e) {
            log.error("Error executing data retention policies: {}", e.getMessage());
        }
    }
    
    // Gerar relatório mensal
    @Scheduled(cron = "0 0 9 1 * *")  // 1º dia do mês às 9:00
    public void generateMonthlyComplianceReport() {
        ComplianceReport report = retentionService.generateComplianceReport();
        
        // Enviar relatório para administradores
        emailService.sendComplianceReport(report);
    }
}
```

### **Referências de Código:**
- **Spring Scheduler:** Jobs existentes no projeto
- **Configuration Properties:** Padrões de configuração

## 🔍 Validação e Testes

### **Como Testar:**
1. Configurar políticas no application.yml
2. Executar job manualmente: `dataRetentionScheduler.executeRetentionPolicies()`
3. Verificar alertas de retenção gerados
4. Testar dashboard: `GET /api/admin/data-retention/policies`
5. Simular dados antigos para testar exclusão
6. Verificar backup antes da exclusão
7. Gerar relatório de compliance: `GET /api/admin/data-retention/compliance-report`

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
- **Estimativa:** 2 horas
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
