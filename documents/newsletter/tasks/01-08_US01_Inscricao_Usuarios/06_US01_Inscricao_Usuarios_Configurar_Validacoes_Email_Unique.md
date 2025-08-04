# 06_US01_Inscricao_Usuarios_Configurar_Validacoes_Email_Unique.md

## 📋 Contexto da Tarefa
- **User Story:** US01 - Inscrição de Usuários
- **Número da Tarefa:** 06/95
- **Complexidade:** Baixa
- **Estimativa:** 2 horas
- **Dependências:** Tarefas 02, 05
- **Sprint:** Sprint 1

## 🎯 Objetivo
Configurar e implementar validações robustas para o campo email, incluindo formato, unicidade e custom validators, além de configurar error handling adequado para retornar mensagens apropriadas.

## 📝 Especificação Técnica

### **Componentes a Implementar:**
- [ ] Custom validator para email único
- [ ] Configurar GlobalExceptionHandler
- [ ] Mensagens de erro personalizadas
- [ ] Validação de formato de email
- [ ] Rate limiting básico


## ✅ Acceptance Criteria
- [ ] **AC1:** Validação de formato de email funciona
- [ ] **AC2:** Validação de email único funciona
- [ ] **AC3:** Mensagens de erro em português
- [ ] **AC4:** HTTP 400 Bad Request para validações
- [ ] **AC5:** HTTP 409 Conflict para email duplicado
- [ ] **AC6:** Error handling configurado no GlobalExceptionHandler

## 🧪 Testes Requeridos

### **Testes Unitários:**
- [ ] Teste UniqueEmailValidator - email novo
- [ ] Teste UniqueEmailValidator - email existente
- [ ] Teste UniqueEmailValidator - email unsubscribed
- [ ] Teste formato de email inválido
- [ ] Teste campos obrigatórios null/blank

### **Testes de Integração:**
- [ ] POST /subscribe - email inválido → 400
- [ ] POST /subscribe - email duplicado → 409
- [ ] Mensagens de erro corretas retornadas

## 🔗 Arquivos Afetados
- [ ] **src/main/java/com/blog/api/validation/UniqueEmailValidator.java:** Custom validator
- [ ] **src/main/java/com/blog/api/validation/UniqueEmail.java:** Anotação
- [ ] **src/main/java/com/blog/api/exception/GlobalExceptionHandler.java:** Atualizar
- [ ] **src/main/java/com/blog/api/dto/NewsletterSubscriptionRequest.java:** Adicionar @UniqueEmail

## ✅ Definition of Done
- [ ] Custom validator UniqueEmailValidator implementado
- [ ] Anotação @UniqueEmail criada
- [ ] GlobalExceptionHandler atualizado
- [ ] Testes de validação passando
- [ ] Mensagens de erro apropriadas

---

**Criado em:** Agosto 2025