# 24_US04_Administracao_Criar_Endpoint_List_Subscribers.md

### ✅ US04 – Administração
*Como administrador, quero visualizar e gerenciar todos os inscritos da newsletter, para ter controle administrativo do sistema.*

## 📋 Descrição da Tarefa
**Criar Endpoint List Subscribers**

Implementar endpoint administrativo GET /api/admin/newsletter/subscribers para listagem paginada de todos os inscritos da newsletter.
O endpoint deve ser protegido por ROLE_ADMIN e retornar dados seguros através do DTO AdminSubscriberResponse.

## 📋 Contexto da Tarefa
- **User Story:** US04 - Administração
- **Número da Tarefa:** 24/95
- **Complexidade:** Média
- **Estimativa:** 3 horas
- **Dependências:** Tarefas 01, 03
- **Sprint:** Sprint 2

## 🎯 Objetivo
Criar endpoint GET /api/newsletter/subscribers para listagem administrativa.

## 📝 Especificação Técnica

### **Componentes a Implementar:**
- [ ] AdminNewsletterController com endpoint GET /api/admin/newsletter/subscribers
- [ ] Método listSubscribers com autorização ROLE_ADMIN
- [ ] Integração com NewsletterService para buscar subscribers
- [ ] Validação de parâmetros de entrada (page, size)
- [ ] Tratamento de erros de autorização (403 Forbidden)

### **Integrações Necessárias:**
- **Com NewsletterService:** Chamada para buscar lista paginada de subscribers
- **Com Spring Security:** Validação de autorização ROLE_ADMIN
- **Com AdminSubscriberResponse:** DTO para resposta sem dados sensíveis
- **Com Spring Data:** Integração com Pageable para paginação

## ✅ Acceptance Criteria
- [ ] **AC1:** Endpoint GET /api/admin/newsletter/subscribers retorna lista paginada
- [ ] **AC2:** Somente usuários com ROLE_ADMIN podem acessar o endpoint
- [ ] **AC3:** Resposta inclui dados do subscriber sem informações sensíveis
- [ ] **AC4:** Paginação funciona corretamente com parâmetros page e size
- [ ] **AC5:** Retorna 403 Forbidden para usuários sem permissão ADMIN
- [ ] **AC6:** Documentação Swagger atualizada com esquemas de autorização

## 🧪 Testes Requeridos

### **Testes Unitários:**
- [ ] Teste de listSubscribers com usuário ADMIN retorna 200 OK
- [ ] Teste de listSubscribers com usuário comum retorna 403 Forbidden
- [ ] Teste de paginação com parâmetros válidos
- [ ] Teste de validação de parâmetros inválidos (page/size negativos)
- [ ] Teste de integração com NewsletterService mockado

### **Testes de Integração:**
- [ ] Teste end-to-end com banco de dados real
- [ ] Teste de autorização com Spring Security TestContext
- [ ] Teste de performance com grandes volumes (1000+ subscribers)
- [ ] Teste de segurança: tentativa de acesso sem token JWT

## 🔗 Arquivos Afetados
- [ ] **src/main/java/com/blog/api/controller/admin/AdminNewsletterController.java:** Implementação do endpoint
- [ ] **src/test/java/com/blog/api/controller/admin/AdminNewsletterControllerTest.java:** Testes unitários
- [ ] **src/test/java/com/blog/api/integration/admin/AdminNewsletterIntegrationTest.java:** Testes de integração
- [ ] **src/main/java/com/blog/api/dto/admin/AdminSubscriberResponse.java:** DTO de resposta (dependência tarefa 27)

## 📚 Documentação para IA

### **Contexto do Projeto:**
- **Stack:** Java 21 + Spring Boot 3.2 + PostgreSQL + Redis
- **Arquitetura:** Clean Architecture (Controller → Service → Repository)
- **Padrões:** Builder Pattern, Java Records para DTOs, Cache-First

### **Implementação Esperada:**
Criar AdminNewsletterController com endpoint GET /api/admin/newsletter/subscribers protegido por ROLE_ADMIN. O endpoint deve:
- Aceitar parâmetros de paginação (page, size) via @RequestParam
- Usar @PreAuthorize("hasRole('ADMIN')")
- Retornar Page<AdminSubscriberResponse> com dados não sensíveis
- Implementar logging de acesso para auditoria
- Seguir padrões REST e Clean Architecture
- Usar padrões de tratamento de erro do projeto

### **Exemplos de Código Existente:**
- **Referência 1:** PostController.java - padrão de endpoint paginado
- **Referência 2:** UserController.java - padrão de autorização com @PreAuthorize
- **Referência 3:** NewsletterController.java - estrutura base para newsletter endpoints

## 🔍 Validação e Testes

### **Como Testar:**
1. Subir aplicação e autenticar como ADMIN
2. Fazer GET /api/admin/newsletter/subscribers?page=0&size=10
3. Verificar resposta 200 OK com estrutura Page<AdminSubscriberResponse>
4. Testar sem autenticação (deve retornar 401)
5. Testar com usuário comum (deve retornar 403)
6. Validar paginação com diferentes page/size
7. Verificar performance com 1000+ registros

### **Critérios de Sucesso:**
- [ ] Endpoint responde corretamente para admin autorizado
- [ ] Autorização funciona (403 para não-admin, 401 para não-autenticado)
- [ ] Paginação retorna dados corretos
- [ ] Response time < 200ms para até 1000 subscribers
- [ ] Logs de auditoria registram acessos
- [ ] Swagger documentação atualizada

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
