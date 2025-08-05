# 18_US03_Descadastro_Criar_UnsubscribeToken_Security.md

## 📋 Contexto da Tarefa
- **User Story:** US03 - Descadastro (Unsubscribe)
- **Número da Tarefa:** 18/95
- **Complexidade:** Baixa
- **Estimativa:** 2 horas
- **Dependências:** Tarefas 09, 11 (NewsletterToken, NewsletterTokenService)
- **Sprint:** Sprint 1

## 🎯 Objetivo
Implementar a lógica de segurança para tokens de unsubscribe, incluindo geração de tokens com TTL longo (1 ano), validação de segurança e integração com links de descadastro nos emails.

## 📝 Especificação Técnica

### **Componentes a Implementar:**
- [ ] Método generateUnsubscribeToken no NewsletterTokenService
- [ ] Validação de token de unsubscribe
- [ ] TTL de 365 dias para tokens unsubscribe
- [ ] Integração com templates de email
- [ ] Logging de eventos de descadastro

### **Integrações Necessárias:**
- **Com TokenService:** Geração e validação de tokens
- **Com EmailService:** Links nos templates de email
- **Com AuditLog:** Registro de eventos de descadastro

## ✅ Acceptance Criteria
- [ ] **AC1:** Tokens de unsubscribe gerados com TTL de 1 ano
- [ ] **AC2:** Token incluído em todos os emails da newsletter
- [ ] **AC3:** Validação de segurança contra ataques de força bruta
- [ ] **AC4:** Tokens únicos por email e sessão
- [ ] **AC5:** Logging de eventos de geração e uso

## 🧪 Testes Requeridos

### **Testes Unitários:**
- [ ] Teste de geração de token unsubscribe
- [ ] Teste de TTL de 365 dias
- [ ] Teste de validação de token
- [ ] Teste de unicidade por email

### **Testes de Integração:**
- [ ] Teste de integração com email templates
- [ ] Teste de logging de eventos

## 🔗 Arquivos Afetados
- [ ] **src/main/java/com/blog/api/service/NewsletterTokenService.java:** Adicionar método unsubscribe
- [ ] **src/main/java/com/blog/api/service/NewsletterService.java:** Integração com tokens
- [ ] **src/test/java/com/blog/api/service/NewsletterTokenServiceTest.java:** Testes

## 📚 Documentação para IA

### **Contexto do Projeto:**
- **Stack:** Java 21 + Spring Boot 3.2 + PostgreSQL + Redis
- **Arquitetura:** Clean Architecture (Controller → Service → Repository)  
- **Padrões:** Builder Pattern, Java Records para DTOs, Cache-First

### **Implementação Esperada:**
Implementar geração segura de tokens unsubscribe com TTL longo. Garantir que cada email tenha token único e válido por 1 ano para facilitar descadastro.

### **Exemplos de Código Existente:**
- **Referência 1:** `NewsletterTokenService.java` (métodos de geração)
- **Referência 2:** `/src/main/java/com/blog/api/service/EmailService.java` (templates)

## 🔍 Validação e Testes

### **Como Testar:**
1. Gerar token unsubscribe
2. Verificar TTL de 1 ano
3. Validar token gerado
4. Testar inclusão em emails
5. Verificar logging

### **Critérios de Sucesso:**
- [ ] Token gerado com TTL correto
- [ ] Validação funcionando
- [ ] Integração com emails
- [ ] Logs registrados

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
*[Tarefa 19: Implementar NewsletterService.unsubscribe()]*

---

**Criado em:** Agosto 2025  
**Última Atualização:** Agosto 2025  
**Responsável:** AI-Driven Development  
**Reviewer:** [Nome do reviewer, se aplicável]