# 10_US02_Confirmacao_Email_Implementar_NewsletterTokenRepository.md

### ✅ US02 – Confirmação de E-mail
*Como usuário inscrito, quero receber um e-mail de confirmação após me inscrever, para validar minha inscrição na newsletter.*

## 📋 Descrição da Tarefa
**10_US02_Confirmacao_Email_Implementar_NewsletterTokenRepository**

Esta tarefa implementa o repositório JPA para operações CRUD com tokens de newsletter.
O repositório incluirá queries otimizadas para busca por token, tipo e limpeza de tokens expirados.

## 📋 Contexto da Tarefa
- **User Story:** US02 - Confirmação de E-mail
- **Número da Tarefa:** 10/95
- **Complexidade:** Baixa
- **Estimativa:** 1 hora
- **Dependências:** Tarefa 09 (NewsletterToken entity)
- **Sprint:** Sprint 1

## 🎯 Objetivo
Implementar o NewsletterTokenRepository usando Spring Data JPA para operações CRUD com tokens de newsletter, incluindo queries para busca por token, tipo e limpeza de tokens expirados.

## 📝 Especificação Técnica

### **Componentes a Implementar:**
- [ ] Interface NewsletterTokenRepository extends JpaRepository
- [ ] Query method findByTokenAndType
- [ ] Query method findByEmailAndTypeAndUsedFalse
- [ ] Método deleteByExpiresAtBefore para limpeza
- [ ] Índices para performance

### **Integrações Necessárias:**
- **Com JPA:** Extensão de JpaRepository
- **Com Entity:** NewsletterToken
- **Com Service:** Usado pelo NewsletterTokenService

## ✅ Acceptance Criteria
- [ ] **AC1:** Interface NewsletterTokenRepository estendendo JpaRepository
- [ ] **AC2:** Método findByTokenAndType para validação
- [ ] **AC3:** Método para buscar tokens não utilizados por email
- [ ] **AC4:** Método de limpeza de tokens expirados
- [ ] **AC5:** Queries otimizadas com índices

## 🧪 Testes Requeridos

### **Testes Unitários:**
- [ ] Teste de busca por token e tipo
- [ ] Teste de busca por email e tipo
- [ ] Teste de limpeza de tokens expirados
- [ ] Teste de queries com dados reais

### **Testes de Integração:**
- [ ] Teste de persistência de tokens
- [ ] Teste de performance das queries

## 🔗 Arquivos Afetados
- [ ] **src/main/java/com/blog/api/repository/NewsletterTokenRepository.java:** Novo repositório
- [ ] **src/test/java/com/blog/api/repository/NewsletterTokenRepositoryTest.java:** Testes

## 📚 Documentação para IA

### **Contexto do Projeto:**
- **Stack:** Java 21 + Spring Boot 3.2 + PostgreSQL + Redis
- **Arquitetura:** Clean Architecture (Controller → Service → Repository)
- **Padrões:** Builder Pattern, Java Records para DTOs, Cache-First

### **Implementação Esperada:**
Seguir padrões estabelecidos nos repositórios existentes. Criar queries eficientes para busca e limpeza de tokens.

### **Exemplos de Código Existente:**
- **Referência 1:** `NewsletterSubscriberRepository.java` (estrutura base)
- **Referência 2:** `/src/main/java/com/blog/api/repository/VerificationTokenRepository.java`

## 🔍 Validação e Testes

### **Como Testar:**
1. Executar testes de repositório
2. Verificar queries SQL geradas
3. Testar operações CRUD
4. Validar performance

### **Critérios de Sucesso:**
- [ ] Testes passando
- [ ] Queries eficientes
- [ ] Operações funcionais

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
- **Estimativa:** 1 hora
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
*[Tarefa 11: Implementar NewsletterTokenService]*

---

**Criado em:** Agosto 2025  
**Última Atualização:** Agosto 2025  
**Responsável:** AI-Driven Development  
**Reviewer:** [Nome do reviewer, se aplicável]