# 03_US01_Inscricao_Usuarios_Implementar_NewsletterRepository.md

### ✅ US01 – Inscrição de Usuários
*Como visitante, quero me inscrever na newsletter informando meu e-mail, para receber novos conteúdos.*

## 📋 Descrição da Tarefa
**Implementar NewsletterRepository**

Implementar o repositório Spring Data JPA para operações de persistência com a entidade NewsletterSubscriber.
Criar queries customizadas para busca por email, status e operações específicas de administração e compliance LGPD.

## 📋 Contexto da Tarefa
- **User Story:** US01 - Inscrição de Usuários
- **Número da Tarefa:** 03/95
- **Complexidade:** Baixa
- **Estimativa:** 2 horas
- **Dependências:** Tarefa 01 (NewsletterSubscriber entity)
- **Sprint:** Sprint 1

## 🎯 Objetivo
Implementar o repositório NewsletterSubscriberRepository usando Spring Data JPA, incluindo queries customizadas para busca por email, status e operações específicas da newsletter.

## 📝 Especificação Técnica

### **Componentes a Implementar:**
- [ ] Interface NewsletterSubscriberRepository extends JpaRepository
- [ ] Query method findByEmail para busca por email
- [ ] Query method findByStatus para busca por status
- [ ] Query customizada com filtros para administração
- [ ] Query para soft delete (LGPD compliance)
- [ ] Métodos de contagem para estatísticas

### **Integrações Necessárias:**
- **Com JPA:** Extensão de JpaRepository
- **Com Entity:** NewsletterSubscriber
- **Com Service:** Usado pelo NewsletterService

## ✅ Acceptance Criteria
- [ ] **AC1:** Interface NewsletterSubscriberRepository estendendo JpaRepository
- [ ] **AC2:** Método findByEmail retornando Optional<NewsletterSubscriber>
- [ ] **AC3:** Método findByStatus retornando List<NewsletterSubscriber>
- [ ] **AC4:** Query customizada para filtros de administração (status, data)
- [ ] **AC5:** Método para soft delete (marcar como DELETED)
- [ ] **AC6:** Métodos de contagem para estatísticas

## 🧪 Testes Requeridos

### **Testes Unitários:**
- [ ] Teste de busca por email existente
- [ ] Teste de busca por email inexistente
- [ ] Teste de busca por status
- [ ] Teste de query customizada com filtros

### **Testes de Integração:**
- [ ] Teste de persistência de NewsletterSubscriber
- [ ] Teste de queries com dados reais
- [ ] Teste de soft delete
- [ ] Teste de contagem

## 🔗 Arquivos Afetados
- [ ] **src/main/java/com/blog/api/repository/NewsletterSubscriberRepository.java:** Novo repositório
- [ ] **src/test/java/com/blog/api/repository/NewsletterSubscriberRepositoryTest.java:** Testes

## 📚 Documentação para IA

### **Contexto do Projeto:**
- **Stack:** Java 21 + Spring Boot 3.2 + PostgreSQL + Redis
- **Arquitetura:** Clean Architecture (Controller → Service → Repository)
- **Padrões:** Builder Pattern, Java Records para DTOs, Cache-First

### **Implementação Esperada:**
Seguir padrões estabelecidos nos repositórios existentes. Utilizar:
- Spring Data JPA com query methods
- @Query annotations para queries customizadas
- Nomenclatura consistente com outros repositórios
- Paginação para listagens administrativas

### **Exemplos de Código Existente:**
- **Referência 1:** `/src/main/java/com/blog/api/repository/UserRepository.java` (para estrutura base)
- **Referência 2:** `/src/main/java/com/blog/api/repository/PostRepository.java` (para queries customizadas)

## 🔍 Validação e Testes

### **Como Testar:**
1. Compilar projeto e verificar ausência de erros
2. Executar testes de repositório
3. Verificar queries no log SQL
4. Testar operações CRUD básicas

### **Critérios de Sucesso:**
- [ ] Compilação sem erros
- [ ] Testes de repositório passando
- [ ] Queries SQL corretas no log
- [ ] Operações CRUD funcionais

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
*[Tarefa 04: Implementar NewsletterService.subscribe()]*

---

**Criado em:** Agosto 2025  
**Última Atualização:** Agosto 2025  
**Responsável:** AI-Driven Development  
**Reviewer:** [Nome do reviewer, se aplicável]