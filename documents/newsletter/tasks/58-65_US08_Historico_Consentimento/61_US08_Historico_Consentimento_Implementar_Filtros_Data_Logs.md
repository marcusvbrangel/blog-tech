# 61_US08_Historico_Consentimento_Implementar_Filtros_Data_Logs.md

### ✅ US08 – Histórico de Consentimento
*Como titular de dados, quero ter acesso ao histórico completo dos meus consentimentos, para acompanhar como meus dados são utilizados.*

## 📋 Descrição da Tarefa
**Implementar filtros por data para consulta de logs**

Cria sistema robusto de filtragem temporal para logs de consentimento com suporte a ranges de data flexíveis e pre-sets comuns.
Otimiza performance através de índices de banco e facilita auditoria por períodos específicos para compliance.

## 📋 Contexto da Tarefa
- **User Story:** US08 - Histórico de Consentimento
- **Número da Tarefa:** 61/95
- **Complexidade:** Baixa
- **Estimativa:** 2 horas
- **Dependências:** Tarefa 60
- **Sprint:** Sprint 3

## 🎯 Objetivo
Implementar filtros por data para consulta de logs.

## 📝 Especificação Técnica

### **Componentes a Implementar:**
- [ ] DateRangeFilterDto para parâmetros de filtragem temporal
- [ ] Custom queries no ConsentLogRepository com filtros por data
- [ ] Validação de ranges de data (data inicial ≤ data final)
- [ ] Índices de banco otimizados para consultas por timestamp
- [ ] Suporte a diferentes formatos de data (ISO 8601, LocalDate)
- [ ] Pre-sets de períodos: hoje, última semana, último mês, último ano

### **Integrações Necessárias:**
- **Com ConsentLogController:** Receber parâmetros de data via query params
- **Com JPA Criteria API:** Queries dinâmicas com filtros opcionais
- **Com PostgreSQL:** Índices BTREE otimizados para timestamp ranges
- **Com Bean Validation:** Validação de formato e lógica de datas
- **Com Redis Cache:** Cache de consultas por períodos comuns

## ✅ Acceptance Criteria
- [ ] **AC1:** Filtrar logs por data inicial (?startDate=2025-08-01)
- [ ] **AC2:** Filtrar logs por data final (?endDate=2025-08-31)
- [ ] **AC3:** Combinar ambos filtros para range específico
- [ ] **AC4:** Validar que startDate ≤ endDate
- [ ] **AC5:** Suportar formato ISO 8601 com timezone (2025-08-01T00:00:00Z)
- [ ] **AC6:** Pre-sets via query param (?period=last_week, last_month, last_year)
- [ ] **AC7:** Performance ≤ 200ms mesmo com ranges grandes
- [ ] **AC8:** Retornar erro 400 para datas inválidas ou ranges inconsistentes

## 🧪 Testes Requeridos

### **Testes Unitários:**
- [ ] Teste de filtragem apenas por startDate
- [ ] Teste de filtragem apenas por endDate
- [ ] Teste de range completo (startDate + endDate)
- [ ] Teste de validação de datas inválidas
- [ ] Teste de pre-sets de períodos (last_week, etc.)
- [ ] Teste de edge cases (datas futuras, ranges muito grandes)

### **Testes de Integração:**
- [ ] Teste de consulta com diferentes ranges de data
- [ ] Teste de performance com large datasets dentro de ranges
- [ ] Teste de índices: explain query plans
- [ ] Teste de timezone handling para diferentes fusos
- [ ] Teste de combinação com outros filtros (email + data)

## 🔗 Arquivos Afetados
- [ ] **src/main/java/com/blog/api/application/admin/dto/DateRangeFilterDto.java** - DTO filtros
- [ ] **src/main/java/com/blog/api/domain/newsletter/repository/ConsentLogRepository.java** - Custom queries
- [ ] **src/main/java/com/blog/api/presentation/admin/ConsentLogController.java** - Query params
- [ ] **src/main/java/com/blog/api/infrastructure/validation/DateRangeValidator.java** - Validação
- [ ] **src/main/resources/db/migration/V009__add_consent_log_date_indexes.sql** - Índices
- [ ] **src/test/java/com/blog/api/domain/newsletter/repository/ConsentLogRepositoryTest.java** - Testes

## 📚 Documentação para IA

### **Contexto do Projeto:**
- **Stack:** Java 21 + Spring Boot 3.2 + PostgreSQL + Redis
- **Arquitetura:** Clean Architecture (Controller → Service → Repository)
- **Padrões:** Builder Pattern, Java Records para DTOs, Cache-First

### **Implementação Esperada:**
Implementar sistema robusto de filtragem temporal para logs de consentimento, com suporte a ranges de data flexíveis, pre-sets comuns, validação rigorosa e performance otimizada via índices de banco, facilitando auditoria e compliance por períodos específicos.

### **Exemplos de Código Existente:**
- **Repository Queries:** Seguir padrões de custom queries já implementadas
- **Date Validation:** Reutilizar validators de data existentes no projeto
- **Query Parameters:** Aplicar mesmo padrão de parâmetros de endpoints
- **Database Indexes:** Seguir estratégia de indexação já estabelecida

## 🔍 Validação e Testes

### **Como Testar:**
1. Executar implementação completa
2. Validar funcionalidade principal
3. Verificar integrações e dependências
4. Confirmar performance e segurança

### **Critérios de Sucesso:**
- [ ] Funcionalidade implementada e funcional
- [ ] Todos os testes passando
- [ ] Performance dentro dos SLAs
- [ ] Documentação completa e atualizada

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
*[Próxima tarefa na sequência de implementação]*

---

**Criado em:** Agosto 2025  
**Última Atualização:** Agosto 2025  
**Responsável:** AI-Driven Development  
**Reviewer:** [Nome do reviewer, se aplicável]
