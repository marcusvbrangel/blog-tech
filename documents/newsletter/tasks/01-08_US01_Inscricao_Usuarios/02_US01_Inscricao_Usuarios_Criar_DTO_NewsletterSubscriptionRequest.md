# 02_US01_Inscricao_Usuarios_Criar_DTO_NewsletterSubscriptionRequest.md

### ✅ US01 – Inscrição de Usuários
*Como visitante, quero me inscrever na newsletter informando meu e-mail, para receber novos conteúdos.*

## 📋 Descrição da Tarefa
**Criar DTO NewsletterSubscriptionRequest**

Criar o Data Transfer Object como Java Record para receber dados da requisição de inscrição na newsletter.
Implementar validações Bean Validation e campos de compliance LGPD para captura de consentimento e metadados do usuário.

## 📋 Contexto da Tarefa
- **User Story:** US01 - Inscrição de Usuários
- **Número da Tarefa:** 02/95
- **Complexidade:** Baixa
- **Estimativa:** 1 hora
- **Dependências:** Nenhuma
- **Sprint:** Sprint 1

## 🎯 Objetivo
Criar o DTO (Data Transfer Object) NewsletterSubscriptionRequest como Java Record para receber dados da requisição de inscrição na newsletter, incluindo validações e campos de compliance LGPD.

## 📝 Especificação Técnica

### **Componentes a Implementar:**
- [ ] Java Record NewsletterSubscriptionRequest
- [ ] Validações Bean Validation (@NotBlank, @Email, @NotNull)
- [ ] Campos para captura de consentimento LGPD
- [ ] Campos para captura de metadados (IP, User-Agent)
- [ ] Javadoc completo

### **Integrações Necessárias:**
- **Com Bean Validation:** Anotações de validação
- **Com Controller:** DTO usado no endpoint POST /api/newsletter/subscribe
- **Com Service:** Conversão para entidade NewsletterSubscriber

## ✅ Acceptance Criteria
- [ ] **AC1:** Java Record NewsletterSubscriptionRequest criado seguindo padrão do projeto
- [ ] **AC2:** Campo email com validação @NotBlank e @Email
- [ ] **AC3:** Campo consentToReceiveEmails obrigatório (@NotNull)
- [ ] **AC4:** Campo privacyPolicyVersion obrigatório (@NotBlank)
- [ ] **AC5:** Campos ipAddress e userAgent para metadados
- [ ] **AC6:** Validações funcionais testadas

## 🧪 Testes Requeridos

### **Testes Unitários:**
- [ ] Teste de criação do record com dados válidos
- [ ] Teste de validação de email inválido
- [ ] Teste de validação de campos obrigatórios
- [ ] Teste de serialização/deserialização JSON

### **Testes de Integração:**
- [ ] Teste de binding no controller
- [ ] Teste de mensagens de erro de validação

## 🔗 Arquivos Afetados
- [ ] **src/main/java/com/blog/api/dto/NewsletterSubscriptionRequest.java:** Novo DTO record
- [ ] **src/test/java/com/blog/api/dto/NewsletterSubscriptionRequestTest.java:** Testes unitários

## 📚 Documentação para IA

### **Contexto do Projeto:**
- **Stack:** Java 21 + Spring Boot 3.2 + PostgreSQL + Redis
- **Arquitetura:** Clean Architecture (Controller → Service → Repository)
- **Padrões:** Builder Pattern, Java Records para DTOs, Cache-First

### **Implementação Esperada:**
Criar Java Record seguindo padrão estabelecido nos DTOs existentes. Utilizar:
- Java Records para DTOs (modernização do projeto)
- Bean Validation annotations
- Javadoc para documentação
- Nomenclatura clara e consistente

### **Exemplos de Código Existente:**
- **Referência 1:** `/src/main/java/com/blog/api/dto/CreateUserDTO.java` (para padrão de validações)
- **Referência 2:** `/src/main/java/com/blog/api/dto/LoginRequest.java` (para estrutura de request)

## 🔍 Validação e Testes

### **Como Testar:**
1. Compilar projeto e verificar ausência de erros
2. Executar testes unitários do DTO
3. Testar validações com dados inválidos
4. Verificar serialização JSON

### **Critérios de Sucesso:**
- [ ] Compilação sem erros
- [ ] Testes unitários passando
- [ ] Validações funcionando corretamente
- [ ] JSON binding funcional

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
*[Tarefa 03: Implementar NewsletterRepository]*

---

**Criado em:** Agosto 2025  
**Última Atualização:** Agosto 2025  
**Responsável:** AI-Driven Development  
**Reviewer:** [Nome do reviewer, se aplicável]