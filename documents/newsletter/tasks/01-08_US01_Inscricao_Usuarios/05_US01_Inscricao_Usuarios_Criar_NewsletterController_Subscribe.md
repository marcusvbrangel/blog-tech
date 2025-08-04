# 05_US01_Inscricao_Usuarios_Criar_NewsletterController_Subscribe.md

## 📋 Contexto da Tarefa
- **User Story:** US01 - Inscrição de Usuários
- **Número da Tarefa:** 05/95
- **Complexidade:** Média
- **Estimativa:** 3 horas
- **Dependências:** Tarefas 02, 04
- **Sprint:** Sprint 1

## 🎯 Objetivo
Criar o endpoint REST `POST /api/newsletter/subscribe` no `NewsletterController` para receber inscrições na newsletter, incluindo captura automática de IP e User-Agent para compliance LGPD.

## 📝 Especificação Técnica

### **Componentes a Implementar:**
- [ ] Controller NewsletterController
- [ ] Endpoint POST /api/newsletter/subscribe
- [ ] Captura de IP e User-Agent
- [ ] Documentação OpenAPI
- [ ] Error handling
- [ ] Response padronizado


## ✅ Acceptance Criteria
- [ ] **AC1:** Endpoint POST /api/newsletter/subscribe criado
- [ ] **AC2:** IP e User-Agent capturados automaticamente
- [ ] **AC3:** Validação automática do DTO funciona
- [ ] **AC4:** Retorna HTTP 202 Accepted para sucesso
- [ ] **AC5:** Retorna HTTP 409 Conflict para email duplicado
- [ ] **AC6:** Documentação OpenAPI completa

## 🧪 Testes Requeridos

### **Testes de Integração:**
- [ ] POST /subscribe - dados válidos → 202
- [ ] POST /subscribe - email duplicado → 409
- [ ] POST /subscribe - email inválido → 400
- [ ] POST /subscribe - campos obrigatórios null → 400
- [ ] Captura de IP e User-Agent funcionando

## 🔗 Arquivos Afetados
- [ ] **src/main/java/com/blog/api/controller/NewsletterController.java:** Novo controller
- [ ] **src/test/java/com/blog/api/controller/NewsletterControllerTest.java:** Testes

## ✅ Definition of Done
- [ ] Controller NewsletterController criado
- [ ] Endpoint POST /subscribe implementado
- [ ] Captura de IP/User-Agent funcionando
- [ ] Testes de integração passando
- [ ] Documentação OpenAPI gerada

---

**Criado em:** Agosto 2025