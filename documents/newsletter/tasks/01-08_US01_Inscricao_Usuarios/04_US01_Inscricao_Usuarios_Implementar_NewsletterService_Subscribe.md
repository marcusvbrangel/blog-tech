# 04_US01_Inscricao_Usuarios_Implementar_NewsletterService_Subscribe.md

## 📋 Contexto da Tarefa
- **User Story:** US01 - Inscrição de Usuários
- **Número da Tarefa:** 04/95
- **Complexidade:** Média
- **Estimativa:** 4 horas
- **Dependências:** Tarefas 01, 02, 03
- **Sprint:** Sprint 1

## 🎯 Objetivo
Implementar o método `subscribe()` no `NewsletterService` que processa a inscrição de usuários na newsletter, incluindo validações de negócio, logging de consentimento LGPD, e preparação para envio de email de confirmação.

## 📝 Especificação Técnica

### **Componentes a Implementar:**
- [ ] Service class NewsletterService
- [ ] Método subscribe(NewsletterSubscriptionRequest)
- [ ] Validação de email já existente
- [ ] Logging de consentimento LGPD
- [ ] Invalidação de cache
- [ ] Métricas Prometheus
- [ ] Error handling customizado


## ✅ Acceptance Criteria
- [ ] **AC1:** Método subscribe implementado com todas validações
- [ ] **AC2:** Email já inscrito retorna erro apropriado
- [ ] **AC3:** Reinscrição após unsubscribe é permitida
- [ ] **AC4:** Consentimento LGPD é registrado corretamente
- [ ] **AC5:** Cache é invalidado após nova inscrição
- [ ] **AC6:** Métricas Prometheus são coletadas

## 🧪 Testes Requeridos

### **Testes Unitários:**
- [ ] Teste subscribe - email novo (happy path)
- [ ] Teste subscribe - email já confirmado (erro)
- [ ] Teste subscribe - email pending (erro)
- [ ] Teste subscribe - email unsubscribed (permite)
- [ ] Teste logging de consentimento
- [ ] Teste invalidação de cache

## 🔗 Arquivos Afetados
- [ ] **src/main/java/com/blog/api/service/NewsletterService.java:** Novo service
- [ ] **src/test/java/com/blog/api/service/NewsletterServiceTest.java:** Testes unitários

## ✅ Definition of Done
- [ ] Service NewsletterService criado
- [ ] Método subscribe implementado
- [ ] Todas validações funcionando
- [ ] Testes unitários passando
- [ ] Métricas implementadas
- [ ] Cache invalidation funcionando

---

**Criado em:** Agosto 2025