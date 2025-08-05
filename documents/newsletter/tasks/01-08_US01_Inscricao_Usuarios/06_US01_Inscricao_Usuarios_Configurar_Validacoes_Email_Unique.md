# 06_US01_Inscricao_Usuarios_Configurar_Validacoes_Email_Unique.md

### ✅ US01 – Inscrição de Usuários
*Como visitante, quero me inscrever na newsletter informando meu e-mail, para receber novos conteúdos.*

## 📋 Descrição da Tarefa
**Configurar Validações Email Unique**

Configurar e implementar validações completas para o campo email, incluindo formato válido e unicidade no banco de dados.
Criar mensagens de erro personalizadas e integração com GlobalExceptionHandler para melhor experiência do usuário.

## 📋 Contexto da Tarefa
- **User Story:** US01 - Inscrição de Usuários
- **Número da Tarefa:** 06/95
- **Complexidade:** Baixa
- **Estimativa:** 2 horas
- **Dependências:** Tarefas 02, 05 (DTO, Controller)
- **Sprint:** Sprint 1

## 🎯 Objetivo
Configurar e implementar validações completas para o campo email, incluindo formato válido, unicidade no banco de dados e mensagens de erro personalizadas para melhor experiência do usuário.

## 📝 Especificação Técnica

### **Componentes a Implementar:**
- [ ] Validação de formato de email com @Email
- [ ] Validação customizada de unicidade de email
- [ ] Mensagens de erro personalizadas
- [ ] Integration com GlobalExceptionHandler
- [ ] Validação de domínios bloqueados (opcional)
- [ ] Testes de todas as validações

### **Integrações Necessárias:**
- **Com Bean Validation:** Anotações de validação
- **Com Repository:** Verificação de unicidade
- **Com Exception Handler:** Tratamento de erros
- **Com i18n:** Mensagens internacionalizadas

## ✅ Acceptance Criteria
- [ ] **AC1:** Email inválido retorna HTTP 400 com mensagem clara
- [ ] **AC2:** Email duplicado retorna HTTP 409 Conflict
- [ ] **AC3:** Email vazio/null retorna HTTP 400
- [ ] **AC4:** Mensagens de erro são user-friendly
- [ ] **AC5:** Validação funciona no controller e service
- [ ] **AC6:** Testes cobrem todos os cenários de validação

## 🧪 Testes Requeridos

### **Testes Unitários:**
- [ ] Teste de email com formato inválido
- [ ] Teste de email vazio/null
- [ ] Teste de email duplicado
- [ ] Teste de email válido e único
- [ ] Teste de mensagens de erro

### **Testes de Integração:**
- [ ] Teste de validação no endpoint
- [ ] Teste de response HTTP correto
- [ ] Teste de mensagens de erro JSON

## 🔗 Arquivos Afetados
- [ ] **src/main/java/com/blog/api/dto/NewsletterSubscriptionRequest.java:** Atualizar validações
- [ ] **src/main/java/com/blog/api/service/NewsletterService.java:** Validação de unicidade
- [ ] **src/main/java/com/blog/api/exception/GlobalExceptionHandler.java:** Tratamento de erros
- [ ] **src/main/resources/messages.properties:** Mensagens de erro

## 📚 Documentação para IA

### **Contexto do Projeto:**
- **Stack:** Java 21 + Spring Boot 3.2 + PostgreSQL + Redis
- **Arquitetura:** Clean Architecture (Controller → Service → Repository)
- **Padrões:** Builder Pattern, Java Records para DTOs, Cache-First

### **Implementação Esperada:**
Seguir padrões de validação estabelecidos. Utilizar:
- Bean Validation annotations (@Email, @NotBlank)
- Custom validation annotations se necessário
- Mensagens de erro consistentes
- Exception handling padronizado
- Testes abrangentes de validação

### **Exemplos de Código Existente:**
- **Referência 1:** `/src/main/java/com/blog/api/dto/CreateUserDTO.java` (para padrões de validação)
- **Referência 2:** `/src/main/java/com/blog/api/exception/GlobalExceptionHandler.java` (para tratamento de erros)

## 🔍 Validação e Testes

### **Como Testar:**
1. Testar diversos formatos de email inválidos
2. Tentar inscrever email duplicado
3. Verificar mensagens de erro retornadas
4. Validar HTTP status codes
5. Testar em diferentes cenários

### **Critérios de Sucesso:**
- [ ] Validações funcionando corretamente
- [ ] Mensagens de erro claras
- [ ] HTTP status codes corretos
- [ ] Testes passando
- [ ] UX consistente

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
*[Tarefa 07: Implementar testes unitários + integração]*

---

**Criado em:** Agosto 2025  
**Última Atualização:** Agosto 2025  
**Responsável:** AI-Driven Development  
**Reviewer:** [Nome do reviewer, se aplicável]