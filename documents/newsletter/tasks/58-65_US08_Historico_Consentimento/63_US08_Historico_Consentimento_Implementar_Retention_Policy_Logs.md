# 63_US08_Historico_Consentimento_Implementar_Retention_Policy_Logs.md

## 📋 Contexto da Tarefa
- **User Story:** US08 - Histórico de Consentimento
- **Número da Tarefa:** 63/95
- **Complexidade:** Baixa
- **Estimativa:** 2 horas
- **Dependências:** Tarefas 55, 62
- **Sprint:** Sprint 3

## 🎯 Objetivo
Implementar política de retenção específica para logs.

## 📝 Especificação Técnica

### **Componentes a Implementar:**
- [ ] LogRetentionService para gestão de retenção
- [ ] Scheduled job para limpeza automática de logs antigos
- [ ] Configurações de retenção por tipo de log (3 anos LGPD)
- [ ] Archive service para mover logs antigos para storage de longo prazo
- [ ] Soft delete vs hard delete baseado em compliance requirements
- [ ] Notification system para alertar sobre operações de retenção

### **Integrações Necessárias:**
- **Com Spring Scheduler:** Jobs automatizados de cleanup
- **Com ConsentLogRepository:** Queries para identificação de logs antigos
- **Com AWS S3/Azure Blob:** Archive de logs para storage de longo prazo
- **Com application.properties:** Configurações de períodos de retenção
- **Com Audit Trail:** Logging das próprias operações de retenção
- **Com EmailService:** Notificações para admins sobre retenção

## ✅ Acceptance Criteria
- [ ] **AC1:** Configurar retenção de 3 anos conforme LGPD (dados sensíveis)
- [ ] **AC2:** Configurar retenção de 7 anos para dados de auditoria
- [ ] **AC3:** Implementar soft delete antes do hard delete final
- [ ] **AC4:** Archive automático para cold storage após 1 ano
- [ ] **AC5:** Job diário para identificação de logs elegíveis para cleanup
- [ ] **AC6:** Preservar logs relacionados a disputas legais em andamento
- [ ] **AC7:** Notification 30 dias antes da remoção definitiva
- [ ] **AC8:** Backup de segurança antes de qualquer operação de remoção

## 🧪 Testes Requeridos

### **Testes Unitários:**
- [ ] Teste de identificação de logs elegíveis para retenção
- [ ] Teste de cálculo de períodos de retenção por tipo
- [ ] Teste de soft delete vs hard delete logic
- [ ] Teste de validação de configurações de retenção
- [ ] Teste de exclusões (logs em disputa legal)

### **Testes de Integração:**
- [ ] Teste end-to-end do ciclo completo de retenção
- [ ] Teste de agendamento e execução do job de cleanup
- [ ] Teste de archive para external storage
- [ ] Teste de recovery de logs arquivados
- [ ] Teste de compliance: verificação de períodos corretos

## 🔗 Arquivos Afetados
- [ ] **src/main/java/com/blog/api/domain/newsletter/service/LogRetentionService.java** - Service
- [ ] **src/main/java/com/blog/api/infrastructure/scheduler/LogRetentionScheduler.java** - Scheduler
- [ ] **src/main/java/com/blog/api/infrastructure/storage/LogArchiveService.java** - Archive
- [ ] **src/main/java/com/blog/api/domain/newsletter/repository/ConsentLogRepository.java** - Queries
- [ ] **src/main/resources/application.yml** - Configurações de retenção
- [ ] **src/main/java/com/blog/api/domain/newsletter/entity/ConsentLog.java** - Soft delete fields
- [ ] **src/test/java/com/blog/api/domain/newsletter/service/LogRetentionServiceTest.java** - Testes

## 📚 Documentação para IA

### **Contexto do Projeto:**
- **Stack:** Java 21 + Spring Boot 3.2 + PostgreSQL + Redis
- **Arquitetura:** Clean Architecture (Controller → Service → Repository)
- **Padrões:** Builder Pattern, Java Records para DTOs, Cache-First

### **Implementação Esperada:**
Implementar sistema robusto de gestão de retenção de logs conforme LGPD e best practices de compliance, com ciclo de vida automatizado, archive para storage de longo prazo, e controles de segurança para garantir preservação adequada sem acumular dados desnecessariamente.

### **Exemplos de Código Existente:**
- **Scheduled Services:** Seguir padrões de jobs agendados já implementados
- **Soft Delete:** Aplicar padrões de soft delete existentes no projeto
- **Configuration Management:** Reutilizar approach de configurações externalizadas
- **Storage Integration:** Seguir padrões de integração com external storage

## 🔍 Validação e Testes

### **Como Testar:**
1. Executar implementação completa
2. Validar funcionalidade principal
3. Verificar integrações e dependências
4. Confirmar performance e segurança

### **Critérios de Sucesso:**
- [ ] Funcionalidade implementada e funcional
- [ ] Todos os testes passando
- [ ] Performance dentro dos SLAs
- [ ] Documentação completa e atualizada

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
*[Próxima tarefa na sequência de implementação]*

---

**Criado em:** Agosto 2025  
**Última Atualização:** Agosto 2025  
**Responsável:** AI-Driven Development  
**Reviewer:** [Nome do reviewer, se aplicável]
