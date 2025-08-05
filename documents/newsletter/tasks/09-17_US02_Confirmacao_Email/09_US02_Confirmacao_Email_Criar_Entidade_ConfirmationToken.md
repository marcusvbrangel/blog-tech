# 09_US02_Confirmacao_Email_Criar_Entidade_ConfirmationToken.md

## 📋 Contexto da Tarefa
- **User Story:** US02 - Confirmação de E-mail
- **Número da Tarefa:** 09/95
- **Complexidade:** Média
- **Estimativa:** 3 horas
- **Dependências:** Tarefa 01 (NewsletterSubscriber)
- **Sprint:** Sprint 1

## 🎯 Objetivo
Criar a entidade NewsletterToken para gerenciar tokens de confirmação de email, unsubscribe e solicitação de dados, com diferentes tipos e funcionalidades de expiração.

## 📝 Especificação Técnica

### **Componentes a Implementar:**
- [ ] Entidade NewsletterToken com anotações JPA
- [ ] Enum TokenType (CONFIRMATION, UNSUBSCRIBE, DATA_REQUEST)
- [ ] Campos de controle (token UUID, expiração, usado)
- [ ] Builder pattern para construção
- [ ] Relacionamento com email (não FK para flexibilidade)
- [ ] Índices para performance

### **Integrações Necessárias:**
- **Com JPA:** Mapeamento para tabela newsletter_tokens
- **Com UUID:** Geração de tokens únicos
- **Com LocalDateTime:** Controle de expiração

## ✅ Acceptance Criteria
- [ ] **AC1:** Entidade NewsletterToken criada com todos os campos
- [ ] **AC2:** Enum TokenType com valores CONFIRMATION, UNSUBSCRIBE, DATA_REQUEST
- [ ] **AC3:** Token UUID único gerado automaticamente
- [ ] **AC4:** Controle de expiração com LocalDateTime
- [ ] **AC5:** Flag 'used' para controle de uso único
- [ ] **AC6:** Builder pattern implementado

## 🧪 Testes Requeridos

### **Testes Unitários:**
- [ ] Teste de criação da entidade com Builder
- [ ] Teste de enum TokenType
- [ ] Teste de campos obrigatórios
- [ ] Teste de validação de expiração

### **Testes de Integração:**
- [ ] Teste de persistência no banco
- [ ] Teste de constraints e índices

## 🔗 Arquivos Afetados
- [ ] **src/main/java/com/blog/api/entity/NewsletterToken.java:** Nova entidade
- [ ] **src/main/java/com/blog/api/entity/TokenType.java:** Novo enum
- [ ] **src/test/java/com/blog/api/entity/NewsletterTokenTest.java:** Testes

## 📚 Documentação para IA

### **Contexto do Projeto:**
- **Stack:** Java 21 + Spring Boot 3.2 + PostgreSQL + Redis
- **Arquitetura:** Clean Architecture (Controller → Service → Repository)
- **Padrões:** Builder Pattern, Java Records para DTOs, Cache-First

### **Implementação Esperada:**
Seguir padrão estabelecido na NewsletterSubscriber. Utilizar:
- JPA annotations para mapeamento
- Builder pattern do Lombok
- UUID para tokens únicos
- LocalDateTime para controle temporal
- Índices para busca por token

### **Exemplos de Código Existente:**
- **Referência 1:** `NewsletterSubscriber.java` (estrutura base)
- **Referência 2:** `/src/main/java/com/blog/api/entity/VerificationToken.java` (padrão de token)

## 🔍 Validação e Testes

### **Como Testar:**
1. Compilar projeto sem erros
2. Executar testes unitários
3. Verificar criação da tabela
4. Testar geração de UUID

### **Critérios de Sucesso:**
- [ ] Compilação sem erros
- [ ] Testes passando
- [ ] Tabela criada corretamente
- [ ] UUID gerado automaticamente

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
*[Tarefa 10: Implementar NewsletterTokenService]*

---

**Criado em:** Agosto 2025  
**Última Atualização:** Agosto 2025  
**Responsável:** AI-Driven Development  
**Reviewer:** [Nome do reviewer, se aplicável]