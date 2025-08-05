# 05_US01_Inscricao_Usuarios_Criar_NewsletterController_Subscribe.md

### ✅ US01 – Inscrição de Usuários
*Como visitante, quero me inscrever na newsletter informando meu e-mail, para receber novos conteúdos.*

## 📋 Descrição da Tarefa
**Criar NewsletterController Subscribe**

Criar o endpoint REST POST /api/newsletter/subscribe para receber requisições de inscrição na newsletter.
Implementar captura automática de metadados (IP, User-Agent), validação de entrada e documentação Swagger completa.

## 📋 Contexto da Tarefa
- **User Story:** US01 - Inscrição de Usuários
- **Número da Tarefa:** 05/95
- **Complexidade:** Média
- **Estimativa:** 3 horas
- **Dependências:** Tarefas 02, 04 (DTO, NewsletterService)
- **Sprint:** Sprint 1

## 🎯 Objetivo
Criar o endpoint REST POST /api/newsletter/subscribe no NewsletterController para receber requisições de inscrição na newsletter, incluindo captura de metadados (IP, User-Agent) e documentação Swagger.

## 📝 Especificação Técnica

### **Componentes a Implementar:**
- [ ] Classe NewsletterController com @RestController
- [ ] Endpoint POST /api/newsletter/subscribe
- [ ] Captura automática de IP e User-Agent
- [ ] Validação de entrada via @Valid
- [ ] Response com HTTP 202 Accepted
- [ ] Documentação Swagger/OpenAPI
- [ ] Exception handling

### **Integrações Necessárias:**
- **Com Service:** NewsletterService.subscribe()
- **Com DTO:** NewsletterSubscriptionRequest/Response
- **Com Security:** Endpoint público (permitAll)
- **Com Monitoring:** Métricas de endpoint

## ✅ Acceptance Criteria
- [ ] **AC1:** Endpoint POST /api/newsletter/subscribe implementado
- [ ] **AC2:** Aceita NewsletterSubscriptionRequest via @RequestBody
- [ ] **AC3:** Captura IP do cliente (considerando X-Forwarded-For)
- [ ] **AC4:** Captura User-Agent do header
- [ ] **AC5:** Retorna HTTP 202 Accepted para sucesso
- [ ] **AC6:** Retorna HTTP 409 Conflict para email duplicado
- [ ] **AC7:** Retorna HTTP 400 Bad Request para dados inválidos
- [ ] **AC8:** Documentação Swagger completa

## 🧪 Testes Requeridos

### **Testes Unitários:**
- [ ] Teste de endpoint com dados válidos (202)
- [ ] Teste de email duplicado (409)
- [ ] Teste de dados inválidos (400)
- [ ] Teste de captura de IP e User-Agent
- [ ] Teste de integração com service

### **Testes de Integração:**
- [ ] Teste end-to-end do endpoint
- [ ] Teste de binding de dados
- [ ] Teste de responses HTTP
- [ ] Teste de documentação Swagger

## 🔗 Arquivos Afetados
- [ ] **src/main/java/com/blog/api/controller/NewsletterController.java:** Novo controller
- [ ] **src/main/java/com/blog/api/config/SecurityConfig.java:** Adicionar endpoint público
- [ ] **src/test/java/com/blog/api/controller/NewsletterControllerTest.java:** Testes

## 📚 Documentação para IA

### **Contexto do Projeto:**
- **Stack:** Java 21 + Spring Boot 3.2 + PostgreSQL + Redis
- **Arquitetura:** Clean Architecture (Controller → Service → Repository)
- **Padrões:** Builder Pattern, Java Records para DTOs, Cache-First

### **Implementação Esperada:**
Seguir padrões estabelecidos nos controllers existentes. Utilizar:
- @RestController, @RequestMapping annotations
- @Valid para validação automática
- ResponseEntity para controle de HTTP status
- @Tag, @Operation para documentação Swagger
- Exception handling consistente
- Logging estruturado

### **Exemplos de Código Existente:**
- **Referência 1:** `/src/main/java/com/blog/api/controller/AuthController.java` (linhas 28-80 para estrutura base)
- **Referência 2:** `/src/main/java/com/blog/api/controller/UserController.java` (para padrões de response)

## 🔍 Validação e Testes

### **Como Testar:**
1. Executar testes unitários do controller
2. Testar endpoint via Postman/curl
3. Verificar responses HTTP corretos
4. Validar captura de metadados
5. Confirmar documentação Swagger

### **Critérios de Sucesso:**
- [ ] Endpoint respondendo corretamente
- [ ] Validações funcionando
- [ ] Metadados capturados
- [ ] Documentação Swagger visível
- [ ] Testes passando

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
*[Tarefa 06: Configurar validações (email format + unique)]*

---

**Criado em:** Agosto 2025  
**Última Atualização:** Agosto 2025  
**Responsável:** AI-Driven Development  
**Reviewer:** [Nome do reviewer, se aplicável]