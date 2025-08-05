# 28_US04_Administracao_Implementar_Filtros_Status_Data.md

## 📋 Contexto da Tarefa
- **User Story:** US04 - Administração
- **Número da Tarefa:** 28/95
- **Complexidade:** Média
- **Estimativa:** 3 horas
- **Dependências:** Tarefas 25, 27
- **Sprint:** Sprint 2

## 🎯 Objetivo
Implementar filtros avançados por status e range de datas.

## 📝 Especificação Técnica

### **Componentes a Implementar:**
- [ ] NewsletterSpecification com filtros avançados
- [ ] AdminSubscriberAdvancedFilterRequest DTO
- [ ] Métodos de filtro por múltiplos status simultaneamente
- [ ] Filtros de range de data (createdBetween, updatedBetween)
- [ ] Filtro por padrão de email com regex/wildcards
- [ ] Índices otimizados no banco para performance

### **Integrações Necessárias:**
- **Com JPA Criteria API:** Predicates dinâmicos para combinação de filtros
- **Com NewsletterRepository:** Métodos findAll(Specification, Pageable)
- **Com Bean Validation:** Validação de ranges de data e parâmetros
- **Com Cache Redis:** Cache de consultas frequentes
- **Com Database Indexes:** Índices compostos para otimização

## ✅ Acceptance Criteria
- [ ] **AC1:** Filtro por múltiplos status: ?status=PENDING,CONFIRMED
- [ ] **AC2:** Range de datas: ?createdAfter=2025&createdBefore=2025-12-31
- [ ] **AC3:** Filtro email com wildcards: ?email=*@gmail.com
- [ ] **AC4:** Combinação de filtros funciona simultaneamente
- [ ] **AC5:** Performance < 500ms mesmo com filtros complexos
- [ ] **AC6:** Validação impede ranges inválidos (dataInicio > dataFim)
- [ ] **AC7:** Cache Redis acelera consultas repetitivas

## 🧪 Testes Requeridos

### **Testes Unitários:**
- [ ] Teste NewsletterSpecification.byMultipleStatus()
- [ ] Teste NewsletterSpecification.byDateRange()
- [ ] Teste NewsletterSpecification.byEmailPattern()
- [ ] Teste combinação de specifications com Specification.where()
- [ ] Teste validação de parâmetros inválidos
- [ ] Teste casos extremos (data nula, status vazio)

### **Testes de Integração:**
- [ ] Teste end-to-end com massa de dados de 10k+ subscribers
- [ ] Teste de performance com múltiplos filtros combinados
- [ ] Teste de consultas SQL geradas (verificar EXPLAIN PLAN)
- [ ] Teste de cache Redis para consultas repetitivas
- [ ] Teste de índices de banco (performance queries)

## 🔗 Arquivos Afetados
- [ ] **src/main/java/com/blog/api/specification/NewsletterSpecification.java:** Lógica de filtros
- [ ] **src/main/java/com/blog/api/dto/admin/AdminSubscriberAdvancedFilterRequest.java:** DTO filtros
- [ ] **src/main/java/com/blog/api/controller/admin/AdminNewsletterController.java:** Endpoints atualizados
- [ ] **src/main/resources/db/migration/V13__Add_Newsletter_Indexes.sql:** Índices otimizados
- [ ] **src/main/java/com/blog/api/config/CacheConfig.java:** Configuração cache filtros
- [ ] **src/test/java/com/blog/api/specification/NewsletterSpecificationTest.java:** Testes specifications

## 📚 Documentação para IA

### **Contexto do Projeto:**
- **Stack:** Java 21 + Spring Boot 3.2 + PostgreSQL + Redis
- **Arquitetura:** Clean Architecture (Controller → Service → Repository)
- **Padrões:** Builder Pattern, Java Records para DTOs, Cache-First

### **Implementação Esperada:**
Implementar filtros avançados:
- **Multi-status:** List<SubscriptionStatus> com IN clause
- **Date ranges:** LocalDateTime ranges com BETWEEN
- **Email patterns:** LIKE/Regex com wildcards (%*gmail*%)
- **Combined filters:** Specification.where().and().or()
- **Cache strategy:** @Cacheable para consultas freqüentes
- **Index optimization:** Índices compostos (status,created_at,email)
- **Validation:** Bean Validation para ranges e patterns
- **Performance monitoring:** Logs de tempo de execução

### **Exemplos de Código Existente:**
- **Referência 1:** PostSpecification.java - padrão de Specification complexas
- **Referência 2:** UserFilterRequest.java - DTO com validações
- **Referência 3:** PostRepository.findAll(Specification) - uso no repository
- **Referência 4:** CacheConfig.java - configuração Redis existente

## 🔍 Validação e Testes

### **Como Testar:**
1. Criar massa de dados com diferentes status e datas
2. Testar ?status=PENDING,CONFIRMED&createdAfter=2025-01-01
3. Testar ?email=*gmail*&status=CONFIRMED
4. Testar ranges inválidos: ?createdAfter=2025-12-31&createdBefore=2025-01-01
5. Medir performance com EXPLAIN PLAN no PostgreSQL
6. Verificar cache hits no Redis
7. Testar com 10k+ registros para performance

### **Critérios de Sucesso:**
- [ ] Todos os filtros avançados funcionam isolada e combinadamente
- [ ] Performance < 500ms mesmo com filtros complexos e 10k+ registros
- [ ] Validações impedem parâmetros inválidos
- [ ] Cache Redis acelera consultas recorrentes
- [ ] Índices de banco otimizam consultas (verificar EXPLAIN)
- [ ] SQL queries geradas são eficientes
- [ ] Documentação Swagger inclui todos os filtros com exemplos

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
