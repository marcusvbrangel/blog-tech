# 29_US04_Administracao_Testes_Autorizacao_Paginacao.md

## 📋 Contexto da Tarefa
- **User Story:** US04 - Administração
- **Número da Tarefa:** 29/95
- **Complexidade:** Média
- **Estimativa:** 3 horas
- **Dependências:** Tarefas 24-28
- **Sprint:** Sprint 2

## 🎯 Objetivo
Implementar testes de autorização e paginação para endpoints administrativos.

## 📝 Especificação Técnica

### **Componentes a Implementar:**
- [ ] AdminNewsletterControllerTest com casos de autorização
- [ ] AdminNewsletterSecurityTest para testes de segurança
- [ ] Testes de paginação com diferentes roles
- [ ] Testes de filtros com autorização
- [ ] Mock de SecurityContext para diferentes cenários
- [ ] Integration tests com TestRestTemplate e JWT

### **Integrações Necessárias:**
- **Com Spring Security Test:** @WithMockUser, @WithAnonymousUser
- **Com MockMvc:** Simulação de requisições HTTP com autorização
- **Com TestRestTemplate:** Testes end-to-end com JWT real
- **Com @MockBean:** Mock de services para isolar testes
- **Com TestContainers:** Banco PostgreSQL real para integration tests

## ✅ Acceptance Criteria
- [ ] **AC1:** Testes verificam que ROLE_ADMIN acessa endpoints (200 OK)
- [ ] **AC2:** Testes verificam que usuários comuns recebem 403 Forbidden
- [ ] **AC3:** Testes verificam que requests sem auth recebem 401 Unauthorized
- [ ] **AC4:** Testes de paginação funcionam com diferentes page/size
- [ ] **AC5:** Testes de filtros combinados com autorização
- [ ] **AC6:** Cobertura de testes ≥ 90% para controllers administrativos
- [ ] **AC7:** Integration tests com JWT real e banco real

## 🧪 Testes Requeridos

### **Testes Unitários:**
- [ ] @WithMockUser(roles="ADMIN") - acesso autorizado (200)
- [ ] @WithMockUser(roles="USER") - acesso negado (403)
- [ ] @WithAnonymousUser - não autenticado (401)
- [ ] Teste paginação com parâmetros válidos/inválidos
- [ ] Teste filtros com diferentes combinações e autorização
- [ ] Teste response DTO não contém dados sensíveis
- [ ] Mock de NewsletterService para isolar testes

### **Testes de Integração:**
- [ ] TestRestTemplate com JWT token válido ROLE_ADMIN
- [ ] TestRestTemplate com JWT token USER role (deve falhar)
- [ ] TestRestTemplate sem Authorization header (deve falhar)
- [ ] Teste end-to-end com banco PostgreSQL via TestContainers
- [ ] Teste performance com 1000+ registros e autorização
- [ ] Teste de expiração de JWT token
- [ ] Teste de JWT malformado ou inválido

## 🔗 Arquivos Afetados
- [ ] **src/test/java/com/blog/api/controller/admin/AdminNewsletterControllerTest.java:** Unit tests
- [ ] **src/test/java/com/blog/api/security/AdminNewsletterSecurityTest.java:** Security tests
- [ ] **src/test/java/com/blog/api/integration/AdminNewsletterIntegrationTest.java:** Integration tests
- [ ] **src/test/java/com/blog/api/utils/JwtTestUtils.java:** Utilitários para JWT em testes
- [ ] **src/test/resources/application-test.yml:** Configurações de teste
- [ ] **src/test/java/com/blog/api/config/TestSecurityConfig.java:** Security config para testes

## 📚 Documentação para IA

### **Contexto do Projeto:**
- **Stack:** Java 21 + Spring Boot 3.2 + PostgreSQL + Redis
- **Arquitetura:** Clean Architecture (Controller → Service → Repository)
- **Padrões:** Builder Pattern, Java Records para DTOs, Cache-First

### **Implementação Esperada:**
Implementar suite completa de testes:
- **Unit Tests:** MockMvc + @WithMockUser para diferentes roles
- **Security Tests:** Verificar @PreAuthorize funciona corretamente
- **Integration Tests:** TestRestTemplate + JWT real + TestContainers
- **Pagination Tests:** Diferentes page/size com autorização
- **Filter Tests:** Combinações de filtros com roles
- **Performance Tests:** Latency < 500ms mesmo com autorização
- **Coverage:** Atingir 90%+ cobertura nos controllers admin

### **Exemplos de Código Existente:**
- **Referência 1:** UserControllerTest.java - exemplo de testes com @WithMockUser
- **Referência 2:** PostSecurityTest.java - padrão de security tests
- **Referência 3:** AuthIntegrationTest.java - exemplo com TestRestTemplate + JWT
- **Referência 4:** TestContainersConfig.java - setup PostgreSQL para tests

## 🔍 Validação e Testes

### **Como Testar:**
1. Executar ./mvnw test -Dtest=AdminNewsletterControllerTest
2. Verificar todos os cenários de autorização (200, 403, 401)
3. Executar integration tests com TestContainers
4. Verificar cobertura de código via Jacoco report
5. Testar performance com massa de dados grande
6. Verificar logs de segurança nos testes
7. Executar suite completa: ./mvnw verify

### **Critérios de Sucesso:**
- [ ] Todos os testes unitários passam (green)
- [ ] Testes de segurança verificam authorização corretamente
- [ ] Integration tests com JWT real funcionam
- [ ] Cobertura de código ≥ 90% nos controllers admin
- [ ] Performance tests < 500ms mesmo com auth + filtros
- [ ] TestContainers setup funciona em CI/CD
- [ ] Nenhum dado sensível exposto nos responses de teste

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
