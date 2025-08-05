# 35_US05_Envio_Automatico_Consultar_Subscribers_Confirmed.md

## 📋 Contexto da Tarefa
- **User Story:** US05 - Envio Automático
- **Número da Tarefa:** 35/95
- **Complexidade:** Baixa
- **Estimativa:** 2 horas
- **Dependências:** Tarefas 01, 33
- **Sprint:** Sprint 2

## 🎯 Objetivo
Implementar método no NewsletterRepository para consultar apenas subscribers com status CONFIRMED de forma eficiente e paginada, otimizando para uso em envios em massa de newsletter.

## 📝 Especificação Técnica

### **Componentes a Implementar:**
- [ ] Método findConfirmedSubscribersForBulkEmail() no repository
- [ ] Query JPA otimizada com índices apropriados
- [ ] Suporte a paginação (Pageable) para processamento em lotes
- [ ] Projeção apenas dos campos necessários (email, nome)
- [ ] Filtro adicional para subscribers ativos (não unsubscribed)
- [ ] Cache da contagem total para métricas
- [ ] Query nativa otimizada para performance

### **Integrações Necessárias:**
- **Com Spring Data JPA:** Repository com métodos de consulta
- **Com Pageable:** Suporte a paginação para processar em lotes
- **Com PostgreSQL:** Índices otimizados na tabela newsletter_subscribers
- **Com Redis Cache:** Cache de resultados frequentes
- **Com NewsletterService:** Consumo da consulta paginada

## ✅ Acceptance Criteria
- [ ] **AC1:** Consulta apenas subscribers com status = CONFIRMED
- [ ] **AC2:** Exclui subscribers com status UNSUBSCRIBED ou PENDING
- [ ] **AC3:** Suporte a paginação para processar grandes volumes
- [ ] **AC4:** Query otimizada com tempo < 100ms para 10k registros
- [ ] **AC5:** Retorna apenas email e nome (projeção)
- [ ] **AC6:** Índice no campo status para performance
- [ ] **AC7:** Cache inteligente para evitar consultas repetidas

## 🧪 Testes Requeridos

### **Testes Unitários:**
- [ ] Teste de findConfirmedSubscribersForBulkEmail() com dados válidos
- [ ] Teste de filtragem por status CONFIRMED apenas
- [ ] Teste de paginação funcionando corretamente
- [ ] Teste de projeção (só email e nome retornados)
- [ ] Teste com banco vazio (lista vazia)

### **Testes de Integração:**
- [ ] Teste com banco PostgreSQL real e grande volume
- [ ] Teste de performance com 50k+ subscribers
- [ ] Teste de índices (explain plan da query)
- [ ] Teste de cache hit/miss ratio
- [ ] Teste de consistencia com transações concorrentes

## 🔗 Arquivos Afetados
- [ ] **src/main/java/com/blog/api/newsletter/repository/NewsletterRepository.java:** Método de consulta
- [ ] **src/main/java/com/blog/api/newsletter/dto/SubscriberForEmailDto.java:** DTO de projeção
- [ ] **src/main/resources/db/migration/V00X__add_newsletter_indexes.sql:** Índices
- [ ] **src/test/java/com/blog/api/newsletter/repository/NewsletterRepositoryTest.java:** Testes
- [ ] **src/test/java/com/blog/api/newsletter/performance/BulkQueryPerformanceTest.java:** Testes performance

## 📚 Documentação para IA

### **Contexto do Projeto:**
- **Stack:** Java 21 + Spring Boot 3.2 + PostgreSQL + Redis
- **Arquitetura:** Clean Architecture (Controller → Service → Repository)
- **Padrões:** Builder Pattern, Java Records para DTOs, Cache-First

### **Implementação Esperada:**
@Query com JPA ou query nativa otimizada. Usar @Cacheable para resultados freqüentes. DTO projection para evitar carregar entidade completa. Index na coluna status. Pageable para lotes de 500-1000 registros.

### **Exemplos de Código Existente:**
- **Referência 1:** Outros repositories do projeto - padrões JPA
- **Referência 2:** NewsletterRepository métodos existentes
- **Referência 3:** DTOs de projeção já implementados

## 🔍 Validação e Testes

### **Como Testar:**
1. Criar subscribers com diferentes status no banco
2. Executar findConfirmedSubscribersForBulkEmail()
3. Verificar que só CONFIRMED são retornados
4. Testar paginação com diferentes tamanhos
5. Medir performance com EXPLAIN ANALYZE
6. Validar cache funcionando

### **Critérios de Sucesso:**
- [ ] Funcionalidade implementada
- [ ] Testes passando
- [ ] Performance adequada
- [ ] Documentação completa

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
*[Tarefa 36: Implementar rate limiting para envios em massa]*

---

**Criado em:** Agosto 2025  
**Última Atualização:** Agosto 2025  
**Responsável:** AI-Driven Development  
**Reviewer:** [Nome do reviewer, se aplicável]
