# 04_US01_Inscricao_Usuarios_Implementar_NewsletterService_Subscribe.md

### ✅ US01 – Inscrição de Usuários
*Como visitante, quero me inscrever na newsletter informando meu e-mail, para receber novos conteúdos.*

## 📋 Descrição da Tarefa
**Implementar NewsletterService Subscribe**

Implementar a lógica de negócio para processar inscrições na newsletter, incluindo validações de email duplicado e reinscrição.
Integrar com sistema de email para confirmação, logging de consentimento LGPD e gerenciamento de cache Redis.

## 📋 Contexto da Tarefa
- **User Story:** US01 - Inscrição de Usuários
- **Número da Tarefa:** 04/95
- **Complexidade:** Média
- **Estimativa:** 4 horas
- **Dependências:** Tarefas 01, 02, 03 (NewsletterSubscriber, DTO, Repository)
- **Sprint:** Sprint 1

## 🎯 Objetivo
Implementar o método subscribe no NewsletterService para processar inscrições na newsletter, incluindo validações, persistência, logging de consentimento e integração com sistema de email.

## 📝 Especificação Técnica

### **Componentes a Implementar:**
- [ ] Classe NewsletterService com anotação @Service
- [ ] Método subscribe(NewsletterSubscriptionRequest) → NewsletterSubscriptionResponse
- [ ] Validação de email duplicado
- [ ] Lógica de reinscrição para status UNSUBSCRIBED
- [ ] Logging de consentimento LGPD
- [ ] Integração com cache Redis
- [ ] Métricas de monitoramento (@Timed)

### **Integrações Necessárias:**
- **Com Repository:** NewsletterSubscriberRepository para persistência
- **Com EmailService:** Envio de email de confirmação (assíncrono)
- **Com Cache:** Invalidação de cache de subscribers confirmados
- **Com Audit:** Logging de eventos de consentimento

## ✅ Acceptance Criteria
- [ ] **AC1:** Método subscribe aceita NewsletterSubscriptionRequest e retorna NewsletterSubscriptionResponse
- [ ] **AC2:** Email já inscrito (PENDING/CONFIRMED) retorna HTTP 409 Conflict
- [ ] **AC3:** Email UNSUBSCRIBED permite reinscrição
- [ ] **AC4:** Status inicial sempre PENDING
- [ ] **AC5:** Consentimento LGPD registrado com timestamp, IP e User-Agent
- [ ] **AC6:** Email de confirmação enviado assincronamente
- [ ] **AC7:** Cache de subscribers confirmados invalidado

## 🧪 Testes Requeridos

### **Testes Unitários:**
- [ ] Teste de inscrição com email novo (sucesso)
- [ ] Teste de email já inscrito (conflict)
- [ ] Teste de reinscrição após unsubscribe
- [ ] Teste de registro de consentimento LGPD
- [ ] Teste de invalidação de cache
- [ ] Teste de envio de email assíncrono

### **Testes de Integração:**
- [ ] Teste end-to-end do fluxo de inscrição
- [ ] Teste de persistência no banco
- [ ] Teste de integração com EmailService

## 🔗 Arquivos Afetados
- [ ] **src/main/java/com/blog/api/service/NewsletterService.java:** Novo service
- [ ] **src/main/java/com/blog/api/dto/NewsletterSubscriptionResponse.java:** Novo DTO response
- [ ] **src/test/java/com/blog/api/service/NewsletterServiceTest.java:** Testes unitários

## 📚 Documentação para IA

### **Contexto do Projeto:**
- **Stack:** Java 21 + Spring Boot 3.2 + PostgreSQL + Redis
- **Arquitetura:** Clean Architecture (Controller → Service → Repository)
- **Padrões:** Builder Pattern, Java Records para DTOs, Cache-First

### **Implementação Esperada:**
Seguir padrões estabelecidos nos services existentes. Utilizar:
- @Service, @Transactional annotations
- Dependency injection via constructor
- Exception handling com custom exceptions
- Logging estruturado com SLF4J
- Métricas com Micrometer (@Timed)
- Cache invalidation strategies

### **Exemplos de Código Existente:**
- **Referência 1:** `/src/main/java/com/blog/api/service/AuthService.java` (linhas 20-50 para estrutura base)
- **Referência 2:** `/src/main/java/com/blog/api/service/UserService.java` (para padrões de validação)

## 🔍 Validação e Testes

### **Como Testar:**
1. Executar testes unitários do service
2. Testar diferentes cenários de inscrição
3. Verificar persistência no banco
4. Validar envio de emails no MailHog
5. Confirmar invalidação de cache Redis

### **Critérios de Sucesso:**
- [ ] Todos os testes unitários passando
- [ ] Validações funcionando corretamente  
- [ ] Email de confirmação enviado
- [ ] Dados persistidos corretamente
- [ ] Cache invalidado apropriadamente

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
*[Tarefa 05: Criar NewsletterController.subscribe()]*

---

**Criado em:** Agosto 2025  
**Última Atualização:** Agosto 2025  
**Responsável:** AI-Driven Development  
**Reviewer:** [Nome do reviewer, se aplicável]