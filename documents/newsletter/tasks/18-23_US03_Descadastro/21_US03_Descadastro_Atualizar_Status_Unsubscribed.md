# 21_US03_Descadastro_Atualizar_Status_Unsubscribed.md

### ✅ US03 – Descadastro
*Como usuário inscrito, quero poder me descadastrar da newsletter através de um link seguro, para parar de receber e-mails.*

## 📋 Descrição da Tarefa
**Atualizar Status Unsubscribed**

Implementar atualização atômica do status do subscriber para UNSUBSCRIBED na base de dados.
Garantir integridade transacional, timestamps de auditoria e invalidação automática de cache.

## 📋 Contexto da Tarefa
- **User Story:** US03 - Descadastro
- **Número da Tarefa:** 21/95
- **Complexidade:** Baixa
- **Estimativa:** 1 hora
- **Dependências:** Tarefas 01, 19
- **Sprint:** Sprint 1

## 🎯 Objetivo
Implementar atualização de status para UNSUBSCRIBED na base de dados.

## 📝 Especificação Técnica

### **Componentes a Implementar:**
- [ ] Método updateSubscriberStatus no NewsletterService
- [ ] Enum SubscriptionStatus.UNSUBSCRIBED no entity
- [ ] Atualização no NewsletterRepository para status update
- [ ] Validações de integridade na mudança de status
- [ ] Sistema de timestamps para auditoria

### **Integrações Necessárias:**
- **Com NewsletterRepository:** Atualização direta no banco de dados
- **Com NewsletterService:** Método centralizado de mudança de status
- **Com sistema de auditoria:** Registro de mudanças de estado
- **Com cache Redis:** Invalidação de cache do subscriber

## ✅ Acceptance Criteria
- [ ] **AC1:** Status do subscriber atualizado para UNSUBSCRIBED no banco
- [ ] **AC2:** Timestamp unsubscribedAt registrado corretamente
- [ ] **AC3:** Cache do subscriber invalidado após atualização
- [ ] **AC4:** Transação atômica para mudança de status
- [ ] **AC5:** Log de auditoria registrado para compliance LGPD

## 🧪 Testes Requeridos

### **Testes Unitários:**
- [ ] Teste de atualização de status de CONFIRMED para UNSUBSCRIBED
- [ ] Teste de atualização de status de PENDING para UNSUBSCRIBED
- [ ] Teste de timestamp unsubscribedAt sendo definido
- [ ] Teste de falha na atualização de subscriber inexistente
- [ ] Teste de invalidação de cache após update

### **Testes de Integração:**
- [ ] Teste de persistência no PostgreSQL
- [ ] Teste de invalidação de cache Redis
- [ ] Teste de rollback em caso de erro
- [ ] Teste de performance com múltiplas atualizações simultâneas

## 🔗 Arquivos Afetados
- [ ] **src/main/java/com/blog/api/service/NewsletterService.java:** Método updateSubscriberStatus
- [ ] **src/main/java/com/blog/api/entity/NewsletterSubscriber.java:** Campo unsubscribedAt
- [ ] **src/main/java/com/blog/api/repository/NewsletterRepository.java:** Query personalizada de update
- [ ] **src/test/java/com/blog/api/service/NewsletterServiceTest.java:** Testes unitários

## 📚 Documentação para IA

### **Contexto do Projeto:**
- **Stack:** Java 21 + Spring Boot 3.2 + PostgreSQL + Redis
- **Arquitetura:** Clean Architecture (Controller → Service → Repository)
- **Padrões:** Builder Pattern, Java Records para DTOs, Cache-First

### **Implementação Esperada:**
Implementar atualização atomic do status do subscriber para UNSUBSCRIBED, incluindo timestamp e invalidação de cache. Garantir integridade transacional e compliance LGPD.

### **Exemplos de Código Existente:**
- **Referência 1:** `NewsletterService.confirmSubscription()` (padrão de update)
- **Referência 2:** `src/main/java/com/blog/api/entity/NewsletterSubscriber.java` (enum status)

## 🔍 Validação e Testes

### **Como Testar:**
1. Criar subscriber com status CONFIRMED
2. Executar updateSubscriberStatus para UNSUBSCRIBED
3. Verificar status no banco = UNSUBSCRIBED
4. Confirmar timestamp unsubscribedAt preenchido
5. Validar invalidação do cache Redis

### **Critérios de Sucesso:**
- [ ] Status atualizado corretamente no banco
- [ ] Timestamp unsubscribedAt registrado
- [ ] Cache invalidado automaticamente
- [ ] Performance < 50ms por operação
- [ ] Transação atômica funcionando

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
*[Tarefa 22: Implementar Event Logging para unsubscribe]*

---

**Criado em:** Agosto 2025  
**Última Atualização:** Agosto 2025  
**Responsável:** AI-Driven Development  
**Reviewer:** [Nome do reviewer, se aplicável]
