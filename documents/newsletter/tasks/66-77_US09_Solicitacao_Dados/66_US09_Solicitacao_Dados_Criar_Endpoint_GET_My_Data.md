# 66_US09_Solicitacao_Dados_Criar_Endpoint_GET_My_Data.md

## 📋 Contexto da Tarefa
- **User Story:** US09 - Solicitação de Dados
- **Número da Tarefa:** 66/95
- **Complexidade:** Média
- **Estimativa:** 3 horas
- **Dependências:** Tarefas 09, 11
- **Sprint:** Sprint 3

## 🎯 Objetivo
Criar endpoint GET /api/newsletter/my-data para solicitação de dados pessoais.

## 📝 Especificação Técnica

### **Componentes a Implementar:**
- [ ] Controller endpoint GET /api/newsletter/my-data
- [ ] Validação de token de acesso seguro
- [ ] Integração com PersonalDataService
- [ ] Tratamento de erros e exceções LGPD
- [ ] Documentação Swagger para portabilidade

### **Integrações Necessárias:**
- **Com DataRequestTokenService:** Validação de tokens específicos para solicitação de dados
- **Com NewsletterPersonalDataService:** Agregação de todos os dados pessoais do usuário
- **Com AuditLogService:** Registro de todas as solicitações de dados para compliance
- **Com RateLimitingService:** Controle de frequência de solicitações

## ✅ Acceptance Criteria
- [ ] **AC1:** Endpoint GET /api/newsletter/my-data implementado e funcional
- [ ] **AC2:** Validação rigorosa de token específico para solicitação de dados
- [ ] **AC3:** Retorno completo de dados pessoais em formato estruturado (JSON)
- [ ] **AC4:** Rate limiting aplicado (máximo 3 solicitações por hora por usuário)
- [ ] **AC5:** Logs de auditoria registrados para cada solicitação
- [ ] **AC6:** Tratamento adequado de erros (token inválido, usuário não encontrado)
- [ ] **AC7:** Conformidade com LGPD Article 18, VI (direito à portabilidade)
- [ ] **AC8:** Documentação Swagger completa com exemplos de uso

## 🧪 Testes Requeridos

### **Testes Unitários:**
- [ ] Teste de endpoint com token válido retorna dados completos
- [ ] Teste de endpoint com token inválido retorna 401 Unauthorized
- [ ] Teste de endpoint com token expirado retorna 401 Unauthorized
- [ ] Teste de validação de rate limiting (máximo 3 por hora)
- [ ] Teste de tratamento de exceções e logs de auditoria

### **Testes de Integração:**
- [ ] Teste end-to-end: geração de token → solicitação → retorno de dados
- [ ] Teste de performance: resposta em < 200ms para datasets típicos
- [ ] Teste de segurança: tentativas de acesso não autorizado
- [ ] Teste de compliance LGPD: estrutura e completude dos dados

## 🔗 Arquivos Afetados
- [ ] **src/main/java/com/blog/api/newsletter/controller/PersonalDataController.java** - Endpoint principal
- [ ] **src/main/java/com/blog/api/newsletter/service/PersonalDataService.java** - Lógica de agregação de dados
- [ ] **src/main/java/com/blog/api/newsletter/config/RateLimitConfig.java** - Configuração de rate limiting
- [ ] **src/test/java/com/blog/api/newsletter/controller/PersonalDataControllerTest.java** - Testes unitários
- [ ] **src/test/java/com/blog/api/newsletter/integration/PersonalDataIntegrationTest.java** - Testes de integração

## 📚 Documentação para IA

### **Contexto do Projeto:**
- **Stack:** Java 21 + Spring Boot 3.2 + PostgreSQL + Redis
- **Arquitetura:** Clean Architecture (Controller → Service → Repository)
- **Padrões:** Builder Pattern, Java Records para DTOs, Cache-First

### **Implementação Esperada:**
Desenvolver endpoint RESTful seguro para permitir que usuários solicitem acesso a todos os seus dados pessoais em conformidade com LGPD Article 18, VI. O endpoint deve validar tokens específicos, aplicar rate limiting, registrar logs de auditoria e retornar dados em formato estruturado para portabilidade.

### **Estrutura do Endpoint:**
```java
@GetMapping("/my-data")
@Operation(summary = "Solicitar dados pessoais (LGPD Article 18, VI)")
public ResponseEntity<PersonalDataResponse> getMyPersonalData(
    @RequestHeader("X-Data-Request-Token") String token
) {
    // Validar token específico
    // Aplicar rate limiting
    // Buscar dados completos
    // Registrar log de auditoria
    // Retornar dados estruturados
}
```

### **Exemplos de Código Existente:**
- **NewsletterController:** Padrões de validação e tratamento de erros
- **ConsentController:** Estrutura de logs de auditoria e compliance LGPD

## 🔍 Validação e Testes

### **Como Testar:**
1. Gerar token de solicitação de dados via POST /api/newsletter/request-data-token
2. Executar GET /api/newsletter/my-data com header X-Data-Request-Token
3. Verificar estrutura JSON do PersonalDataResponse retornado
4. Testar rate limiting fazendo 4 requests em sequência (4º deve falhar)
5. Validar logs de auditoria registrados no banco de dados
6. Testar cenários de erro (token inválido, expirado, malformado)

### **Critérios de Sucesso:**
- [ ] Endpoint retorna dados completos em estrutura JSON válida
- [ ] Rate limiting funciona (máximo 3 requests/hora por usuário)
- [ ] Tokens inválidos retornam 401 com mensagem apropriada
- [ ] Performance < 200ms para datasets típicos (< 1000 registros)
- [ ] Logs de auditoria registrados com timestamp, IP e user-agent
- [ ] Conformidade LGPD: dados estruturados para portabilidade

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
*[Próxima tarefa na sequência de implementação]*

---

**Criado em:** Agosto 2025  
**Última Atualização:** Agosto 2025  
**Responsável:** AI-Driven Development  
**Reviewer:** [Nome do reviewer, se aplicável]
