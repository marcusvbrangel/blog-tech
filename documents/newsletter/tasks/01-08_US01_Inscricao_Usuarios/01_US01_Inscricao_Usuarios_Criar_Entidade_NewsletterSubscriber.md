# 01_US01_Inscricao_Usuarios_Criar_Entidade_NewsletterSubscriber.md

### ✅ US01 – Inscrição de Usuários
*Como visitante, quero me inscrever na newsletter informando meu e-mail, para receber novos conteúdos.*

## 📋 Descrição da Tarefa
**Criar Entidade NewsletterSubscriber**

Criar a entidade principal que representará os assinantes da newsletter no banco de dados, incluindo todos os campos necessários para compliance LGPD.
Implementar a estrutura base com anotações JPA, Builder pattern e campos de auditoria seguindo os padrões arquiteturais do projeto.

## 📋 Contexto da Tarefa
- **User Story:** US01 - Inscrição de Usuários
- **Número da Tarefa:** 01/95
- **Complexidade:** Média
- **Estimativa:** 3 horas
- **Dependências:** Nenhuma (tarefa base)
- **Sprint:** Sprint 1

## 🎯 Objetivo
Criar a entidade principal NewsletterSubscriber que representará os assinantes da newsletter no banco de dados, seguindo os padrões arquiteturais do projeto e incluindo todos os campos necessários para compliance LGPD.

## 📝 Especificação Técnica

### **Componentes a Implementar:**
- [ ] Entidade NewsletterSubscriber com anotações JPA
- [ ] Enum SubscriptionStatus (PENDING, CONFIRMED, UNSUBSCRIBED, DELETED)
- [ ] Builder pattern para construção da entidade
- [ ] Campos de auditoria (CreatedDate, UpdatedDate)
- [ ] Campos de compliance LGPD (consentimento, IP, User-Agent)

### **Integrações Necessárias:**
- **Com JPA:** Mapeamento para tabela newsletter_subscribers
- **Com Enum:** SubscriptionStatus para controle de estado
- **Com Auditoria:** Integração com AuditingEntityListener

## ✅ Acceptance Criteria
- [ ] **AC1:** Entidade NewsletterSubscriber criada com todos os campos obrigatórios
- [ ] **AC2:** Enum SubscriptionStatus com valores PENDING, CONFIRMED, UNSUBSCRIBED, DELETED
- [ ] **AC3:** Builder pattern implementado seguindo padrão do projeto
- [ ] **AC4:** Campos de auditoria automática (createdAt, updatedAt)
- [ ] **AC5:** Campos LGPD implementados (consentGivenAt, consentIpAddress, consentUserAgent, privacyPolicyVersion)
- [ ] **AC6:** Email único e validado com anotações Bean Validation

## 🧪 Testes Requeridos

### **Testes Unitários:**
- [ ] Teste de criação da entidade com Builder
- [ ] Teste de validação de email (formato e unicidade)
- [ ] Teste de enum SubscriptionStatus
- [ ] Teste de campos obrigatórios

### **Testes de Integração:**
- [ ] Teste de persistência no banco de dados
- [ ] Teste de auditoria automática
- [ ] Teste de constraints de banco

## 🔗 Arquivos Afetados
- [ ] **src/main/java/com/blog/api/entity/NewsletterSubscriber.java:** Nova entidade
- [ ] **src/main/java/com/blog/api/entity/SubscriptionStatus.java:** Novo enum
- [ ] **src/test/java/com/blog/api/entity/NewsletterSubscriberTest.java:** Testes unitários

## 📚 Documentação para IA

### **Contexto do Projeto:**
- **Stack:** Java 21 + Spring Boot 3.2 + PostgreSQL + Redis
- **Arquitetura:** Clean Architecture (Controller → Service → Repository)
- **Padrões:** Builder Pattern, Java Records para DTOs, Cache-First

### **Implementação Esperada:**
Seguir o padrão estabelecido na entidade User.java localizada em `/src/main/java/com/blog/api/entity/User.java`. Utilizar:
- Anotações Jakarta Persistence (JPA)
- Builder pattern do Lombok
- Bean Validation para validações
- EntityListeners para auditoria
- Campos de compliance LGPD desde o início

### **Exemplos de Código Existente:**
- **Referência 1:** `/src/main/java/com/blog/api/entity/User.java` (linhas 15-50 para estrutura base)
- **Referência 2:** `/src/main/java/com/blog/api/entity/Post.java` (para padrões de auditoria)

## 🔍 Validação e Testes

### **Como Testar:**
1. Compilar projeto e verificar ausência de erros
2. Executar testes unitários da entidade
3. Verificar geração da tabela no banco (via logs Hibernate)
4. Testar Builder pattern e validações

### **Critérios de Sucesso:**
- [ ] Compilação sem erros
- [ ] Testes unitários passando
- [ ] Entidade persistível no banco
- [ ] Builder pattern funcional

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
*[Tarefa 02: Criar DTO NewsletterSubscriptionRequest]*

---

**Criado em:** Agosto 2025  
**Última Atualização:** Agosto 2025  
**Responsável:** AI-Driven Development  
**Reviewer:** [Nome do reviewer, se aplicável]