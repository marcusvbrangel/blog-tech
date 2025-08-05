# 51_US07_Seguranca_LGPD_Criar_Endpoint_GET_Consent_History.md

## 📋 Contexto da Tarefa
- **User Story:** US07 - Segurança LGPD
- **Número da Tarefa:** 51/95
- **Complexidade:** Média
- **Estimativa:** 3 horas
- **Dependências:** Tarefas 48, 49
- **Sprint:** Sprint 3

## 🎯 Objetivo
Criar endpoint GET /api/newsletter/consent-history para admins.

## 📝 Especificação Técnica

### **Componentes a Implementar:**
- [ ] Endpoint GET /api/admin/newsletter/consent-history (admin only)
- [ ] Sistema de autenticação e autorização para admins
- [ ] Filtros: email, periodo, tipo de consentimento, ação
- [ ] Paginação para grandes volumes de dados
- [ ] Export para CSV/PDF para relatórios
- [ ] Cache com TTL para performance
- [ ] DTO especializado para resposta de auditoria

### **Integrações Necessárias:**
- **Com Spring Security:** Autorização de admin (ROLE_ADMIN)
- **Com NewsletterConsentLogRepository:** Consultas de auditoria
- **Com AdminController:** Endpoints administrativos

## ✅ Acceptance Criteria
- [ ] **AC1:** Endpoint GET /api/admin/newsletter/consent-history protegido por ROLE_ADMIN
- [ ] **AC2:** Filtros: email, startDate, endDate, consentType, action
- [ ] **AC3:** Paginação: page, size, sort (padrão: timestamp desc)
- [ ] **AC4:** Response com dados descriptografados para visualização admin
- [ ] **AC5:** Export para CSV: /api/admin/newsletter/consent-history/export
- [ ] **AC6:** Cache Redis com TTL de 5 minutos
- [ ] **AC7:** Rate limiting: 60 requests/min para admins
- [ ] **AC8:** Logs de acesso administrativo para auditoria
- [ ] **AC9:** Mask de dados sensíveis em logs (IP parcial)

## 🧪 Testes Requeridos

### **Testes Unitários:**
- [ ] Teste de autorização (acesso negado para não-admin)
- [ ] Teste de filtros de consulta
- [ ] Teste de paginação
- [ ] Teste de ordenação por timestamp
- [ ] Teste de cache (hit/miss)
- [ ] Teste de export CSV

### **Testes de Integração:**
- [ ] Teste end-to-end com autenticação admin
- [ ] Teste de performance com grande volume
- [ ] Teste de segurança (tentativas de acesso não autorizado)
- [ ] Teste de export de relatórios

## 🔗 Arquivos Afetados
- [ ] **src/main/java/com/blog/api/admin/controller/AdminNewsletterController.java** - Endpoint admin
- [ ] **src/main/java/com/blog/api/newsletter/dto/ConsentHistoryResponse.java** - DTO de resposta
- [ ] **src/main/java/com/blog/api/newsletter/dto/ConsentHistoryFilter.java** - DTO de filtros
- [ ] **src/main/java/com/blog/api/newsletter/service/ConsentAuditService.java** - Lógica de auditoria
- [ ] **src/main/java/com/blog/api/security/AdminSecurityConfig.java** - Configuração de segurança
- [ ] **src/test/java/com/blog/api/admin/controller/AdminNewsletterControllerTest.java** - Testes

## 📚 Documentação para IA

### **Contexto do Projeto:**
- **Stack:** Java 21 + Spring Boot 3.2 + PostgreSQL + Redis
- **Arquitetura:** Clean Architecture (Controller → Service → Repository)
- **Padrões:** Builder Pattern, Java Records para DTOs, Cache-First

### **Implementação Esperada:**

**AdminNewsletterController.java:**
```java
@RestController
@RequestMapping("/api/admin/newsletter")
@PreAuthorize("hasRole('ADMIN')")
@RateLimited(maxRequests = 60, windowMinutes = 1)
public class AdminNewsletterController {
    
    @GetMapping("/consent-history")
    @Cacheable(value = "consent-history", keyGenerator = "customKeyGenerator")
    public ResponseEntity<Page<ConsentHistoryResponse>> getConsentHistory(
        @Valid ConsentHistoryFilter filter,
        Pageable pageable,
        HttpServletRequest request) {
        
        // Log de acesso administrativo
        auditLogger.logAdminAccess("CONSENT_HISTORY_ACCESS", 
            getCurrentUser(), maskIpAddress(getClientIpAddress(request)));
        
        Page<ConsentHistoryResponse> history = consentAuditService
            .getConsentHistory(filter, pageable);
        
        return ResponseEntity.ok(history);
    }
    
    @GetMapping("/consent-history/export")
    public ResponseEntity<Resource> exportConsentHistory(
        @Valid ConsentHistoryFilter filter,
        @RequestParam(defaultValue = "csv") String format,
        HttpServletRequest request) {
        
        auditLogger.logAdminAccess("CONSENT_HISTORY_EXPORT", 
            getCurrentUser(), maskIpAddress(getClientIpAddress(request)));
        
        ByteArrayResource resource = consentAuditService
            .exportConsentHistory(filter, format);
        
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, 
                    "attachment; filename=consent-history." + format)
            .contentType(MediaType.APPLICATION_OCTET_STREAM)
            .body(resource);
    }
}
```

**ConsentHistoryResponse.java:**
```java
public record ConsentHistoryResponse(
    Long id,
    String email,
    ConsentType consentType,
    ConsentAction action,
    LocalDateTime timestamp,
    String ipAddress,
    String userAgent,
    LegalBasis legalBasis,
    String reason,
    String previousValue,
    String newValue
) {}
```

**ConsentHistoryFilter.java:**
```java
public record ConsentHistoryFilter(
    @Email String email,
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
    ConsentType consentType,
    ConsentAction action,
    LegalBasis legalBasis
) {}
```

**ConsentAuditService.java:**
```java
@Service
@Transactional(readOnly = true)
public class ConsentAuditService {
    
    public Page<ConsentHistoryResponse> getConsentHistory(
        ConsentHistoryFilter filter, Pageable pageable) {
        
        // Construir consulta dinâmica baseada nos filtros
        Page<NewsletterConsentLog> logs = consentLogRepository
            .findWithFilters(filter, pageable);
        
        return logs.map(this::toResponse);
    }
    
    public ByteArrayResource exportConsentHistory(
        ConsentHistoryFilter filter, String format) {
        
        List<NewsletterConsentLog> logs = consentLogRepository
            .findAllWithFilters(filter);
        
        return switch (format.toLowerCase()) {
            case "csv" -> generateCsvReport(logs);
            case "pdf" -> generatePdfReport(logs);
            default -> throw new IllegalArgumentException("Formato não suportado: " + format);
        };
    }
}
```

### **Referências de Código:**
- **AdminController:** Padrões de endpoints administrativos
- **PostController:** Paginação e filtros

## 🔍 Validação e Testes

### **Como Testar:**
1. Autenticar como admin: `POST /api/auth/login` com credenciais admin
2. Testar acesso: `GET /api/admin/newsletter/consent-history`
3. Testar filtros: `GET /api/admin/newsletter/consent-history?email=test@test.com&startDate=2025-01-01T00:00:00`
4. Testar paginação: `GET /api/admin/newsletter/consent-history?page=0&size=10&sort=timestamp,desc`
5. Testar export: `GET /api/admin/newsletter/consent-history/export?format=csv`
6. Testar acesso não autorizado (sem ROLE_ADMIN)
7. Verificar cache Redis: `KEYS consent-history:*`
8. Verificar logs de auditoria admin

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
