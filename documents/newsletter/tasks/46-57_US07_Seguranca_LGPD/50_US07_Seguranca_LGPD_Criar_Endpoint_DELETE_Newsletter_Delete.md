# 50_US07_Seguranca_LGPD_Criar_Endpoint_DELETE_Newsletter_Delete.md

## 📋 Contexto da Tarefa
- **User Story:** US07 - Segurança LGPD
- **Número da Tarefa:** 50/95
- **Complexidade:** Média
- **Estimativa:** 4 horas
- **Dependências:** Tarefas 09, 11
- **Sprint:** Sprint 3

## 🎯 Objetivo
Criar endpoint DELETE /api/newsletter/delete para exclusão total.

## 📝 Especificação Técnica

### **Componentes a Implementar:**
- [ ] Endpoint DELETE /api/newsletter/delete para exclusão definitiva
- [ ] Token de segurança para validação da exclusão
- [ ] Validação de identidade antes da exclusão
- [ ] Log completo da operação (before/after)
- [ ] Exclusão em cascata de dados relacionados
- [ ] Rate limiting para prevenir abuso
- [ ] Email de confirmação de exclusão

### **Integrações Necessárias:**
- **Com TokenService:** Geração e validação de token de exclusão
- **Com NewsletterConsentLog:** Log da exclusão para compliance
- **Com EmailService:** Confirmação de exclusão por email

## ✅ Acceptance Criteria
- [ ] **AC1:** Endpoint DELETE /api/newsletter/delete/{token} funcional
- [ ] **AC2:** Token de exclusão deve ter expiração de 24 horas
- [ ] **AC3:** Validação de email/token antes da exclusão
- [ ] **AC4:** Exclusão definitiva (hard delete) de NewsletterSubscriber
- [ ] **AC5:** Exclusão de dados relacionados (tokens, cache)
- [ ] **AC6:** Log de auditoria em NewsletterConsentLog
- [ ] **AC7:** Rate limiting: máximo 3 tentativas por IP/hora
- [ ] **AC8:** Resposta 200 OK com confirmação de exclusão
- [ ] **AC9:** Email de confirmação após exclusão bem-sucedida

## 🧪 Testes Requeridos

### **Testes Unitários:**
- [ ] Teste de exclusão com token válido
- [ ] Teste de rejeição com token inválido/expirado
- [ ] Teste de validação de email
- [ ] Teste de rate limiting
- [ ] Teste de geração de log de auditoria
- [ ] Teste de exclusão de dados relacionados

### **Testes de Integração:**
- [ ] Teste end-to-end do fluxo completo de exclusão
- [ ] Teste de envio de email de confirmação
- [ ] Teste de exclusão de cache Redis
- [ ] Teste de segurança (tentativas malévolas)

## 🔗 Arquivos Afetados
- [ ] **src/main/java/com/blog/api/newsletter/controller/NewsletterController.java** - Endpoint DELETE
- [ ] **src/main/java/com/blog/api/newsletter/service/NewsletterService.java** - Lógica de exclusão
- [ ] **src/main/java/com/blog/api/newsletter/dto/DataDeletionResponse.java** - DTO de resposta
- [ ] **src/main/java/com/blog/api/security/RateLimitingService.java** - Rate limiting
- [ ] **src/test/java/com/blog/api/newsletter/controller/NewsletterControllerTest.java** - Testes unitários
- [ ] **src/test/java/com/blog/api/newsletter/integration/DataDeletionIntegrationTest.java** - Testes de integração

## 📚 Documentação para IA

### **Contexto do Projeto:**
- **Stack:** Java 21 + Spring Boot 3.2 + PostgreSQL + Redis
- **Arquitetura:** Clean Architecture (Controller → Service → Repository)
- **Padrões:** Builder Pattern, Java Records para DTOs, Cache-First

### **Implementação Esperada:**

**NewsletterController.java:**
```java
@DeleteMapping("/delete/{token}")
@RateLimited(maxAttempts = 3, windowHours = 1)
public ResponseEntity<DataDeletionResponse> deleteData(
    @PathVariable String token,
    HttpServletRequest request) {
    
    String ipAddress = getClientIpAddress(request);
    
    try {
        DataDeletionResponse response = newsletterService.deleteUserData(token, ipAddress);
        return ResponseEntity.ok(response);
    } catch (InvalidTokenException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(new DataDeletionResponse(false, "Token inválido ou expirado"));
    } catch (RateLimitExceededException e) {
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
            .body(new DataDeletionResponse(false, "Muitas tentativas. Tente novamente em 1 hora"));
    }
}
```

**NewsletterService.java:**
```java
@Transactional
public DataDeletionResponse deleteUserData(String token, String ipAddress) {
    // 1. Validar token
    DeletionToken deletionToken = tokenService.validateDeletionToken(token);
    
    // 2. Buscar subscriber
    NewsletterSubscriber subscriber = findByEmail(deletionToken.getEmail());
    
    // 3. Log antes da exclusão
    consentLogService.logDataDeletion(subscriber, ipAddress, "LGPD_REQUEST");
    
    // 4. Deletar dados relacionados
    tokenRepository.deleteByEmail(subscriber.getEmail());
    cacheService.evictSubscriber(subscriber.getEmail());
    
    // 5. Deletar subscriber (hard delete)
    subscriberRepository.delete(subscriber);
    
    // 6. Enviar email de confirmação
    emailService.sendDeletionConfirmation(subscriber.getEmail());
    
    return new DataDeletionResponse(true, "Dados excluídos com sucesso");
}
```

**DataDeletionResponse.java:**
```java
public record DataDeletionResponse(
    boolean success,
    String message,
    LocalDateTime timestamp
) {
    public DataDeletionResponse(boolean success, String message) {
        this(success, message, LocalDateTime.now());
    }
}
```

### **Referências de Código:**
- **NewsletterController:** Padrões de endpoint existentes
- **TokenService:** Lógica de validação de tokens

## 🔍 Validação e Testes

### **Como Testar:**
1. Gerar token de exclusão: `POST /api/newsletter/request-deletion`
2. Testar exclusão: `DELETE /api/newsletter/delete/{token}`
3. Verificar exclusão no banco: `SELECT * FROM newsletter_subscriber WHERE email = '...'`
4. Testar token expirado (após 24h)
5. Testar rate limiting com múltiplas tentativas
6. Verificar log de auditoria em newsletter_consent_log
7. Verificar envio de email de confirmação

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
- **Estimativa:** 4 horas
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
