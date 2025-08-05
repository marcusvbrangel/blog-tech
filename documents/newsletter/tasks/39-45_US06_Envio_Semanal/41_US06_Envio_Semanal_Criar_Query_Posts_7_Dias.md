# 41_US06_Envio_Semanal_Criar_Query_Posts_7_Dias.md

### ✅ US06 – Envio Semanal
*Como usuário confirmado, quero receber um digest semanal com os posts publicados na semana, para acompanhar o conteúdo de forma organizada.*

## 📋 Descrição da Tarefa
**Criar Query Posts 7 Dias**

Criar query JPA otimizada no PostRepository para buscar posts publicados nos últimos 7 dias.
Implementar filtragem por status PUBLISHED, ordenação por data e cache Redis para performance.

## 📋 Contexto da Tarefa
- **User Story:** US06 - Envio Semanal
- **Número da Tarefa:** 41/95
- **Complexidade:** Baixa
- **Estimativa:** 2 horas
- **Dependências:** Tarefa 40
- **Sprint:** Sprint 2

## 🎯 Objetivo
Criar query para buscar posts dos últimos 7 dias.

## 📝 Especificação Técnica

### **Componentes a Implementar:**
- [ ] Método findPostsFromLastSevenDays() no PostRepository
- [ ] Query JPA com critério de data (publishedAt >= hoje - 7 dias)
- [ ] Ordenação por data de publicação (mais recentes primeiro)
- [ ] Filtro apenas posts com status PUBLISHED
- [ ] Otimização de performance com índices
- [ ] Cache Redis para consultas frequentes

### **Integrações Necessárias:**
- **Com PostRepository:** Extensão do repositório existente
- **Com sistema de cache:** Cache Redis para otimização de performance
- **Com JPA/Hibernate:** Query nativa ou JPQL otimizada

## ✅ Acceptance Criteria
- [ ] **AC1:** Query retorna apenas posts com publishedAt nos últimos 7 dias
- [ ] **AC2:** Filtra apenas posts com status PUBLISHED
- [ ] **AC3:** Resultados ordenados por publishedAt DESC (mais recentes primeiro)
- [ ] **AC4:** Performance adequada mesmo com milhares de posts (< 100ms)
- [ ] **AC5:** Cache Redis implementado com TTL de 1 hora
- [ ] **AC6:** Query funciona corretamente com timezone UTC

## 🧪 Testes Requeridos

### **Testes Unitários:**
- [ ] Teste com posts dentro do período de 7 dias
- [ ] Teste com posts fora do período (8+ dias atrás)
- [ ] Teste com posts status DRAFT (não deve retornar)
- [ ] Teste de ordenação por data
- [ ] Teste com zero posts no período
- [ ] Teste de timezone (UTC vs local)

### **Testes de Integração:**
- [ ] Teste com banco real e dados de massa
- [ ] Teste de performance com 10k+ posts
- [ ] Teste de cache Redis (hit/miss)
- [ ] Teste de queries SQL geradas pelo JPA

## 🔗 Arquivos Afetados
- [ ] **src/main/java/com/blog/api/post/repository/PostRepository.java:** Adicionar método findPostsFromLastSevenDays()
- [ ] **src/main/java/com/blog/api/post/service/PostService.java:** Método de negócio para busca
- [ ] **src/main/resources/application.properties:** Configurações de cache
- [ ] **src/test/java/com/blog/api/post/repository/PostRepositoryTest.java:** Testes da query
- [ ] **src/test/java/com/blog/api/post/service/PostServiceTest.java:** Testes do service

## 📚 Documentação para IA

### **Contexto do Projeto:**
- **Stack:** Java 21 + Spring Boot 3.2 + PostgreSQL + Redis
- **Arquitetura:** Clean Architecture (Controller → Service → Repository)
- **Padrões:** Builder Pattern, Java Records para DTOs, Cache-First

### **Implementação Esperada:**
1. Implementar query JPA no PostRepository:
   ```java
   @Query("SELECT p FROM Post p WHERE p.publishedAt >= :startDate AND p.status = 'PUBLISHED' ORDER BY p.publishedAt DESC")
   List<Post> findPostsFromLastSevenDays(@Param("startDate") LocalDateTime startDate);
   ```
2. Adicionar método no PostService que:
   - Calcula data de 7 dias atrás (LocalDateTime.now().minusDays(7))
   - Chama o repositório com a data calculada
   - Implementa cache Redis com chave "posts:last7days"
   - Adiciona logs de performance
3. Criar índice composto no banco: (publishedAt, status)
4. Implementar cache com TTL de 1 hora

### **Exemplos de Código Existente:**
- **Referência 1:** PostRepository queries existentes para padrões de JPA
- **Referência 2:** PostService methods para cache e logging patterns

## 🔍 Validação e Testes

### **Como Testar:**
1. Criar posts de teste com datas variadas (hoje, 3 dias, 5 dias, 10 dias atrás)
2. Criar posts DRAFT e PUBLISHED no período
3. Executar findPostsFromLastSevenDays() e validar resultados
4. Verificar ordenação (mais recentes primeiro)
5. Testar cache Redis (primeira chamada: miss, segunda: hit)
6. Medir performance com dados de massa (1000+ posts)
7. Validar índice SQL com EXPLAIN PLAN

### **Critérios de Sucesso:**
- [ ] Query retorna exatamente posts dos últimos 7 dias
- [ ] Apenas posts PUBLISHED são retornados
- [ ] Ordenação correta por data decrescente
- [ ] Performance < 100ms mesmo com 10k+ posts
- [ ] Cache funcionando corretamente (hit rate > 80%)
- [ ] Índice sendo utilizado pelo query planner

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
*[Próxima tarefa da sequência]*

---

**Criado em:** Agosto 2025  
**Última Atualização:** Agosto 2025  
**Responsável:** AI-Driven Development  
**Reviewer:** [Nome do reviewer, se aplicável]
