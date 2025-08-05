# 44_US06_Envio_Semanal_Configurar_Cron_Sexta_9h.md

## 📋 Contexto da Tarefa
- **User Story:** US06 - Envio Semanal
- **Número da Tarefa:** 44/95
- **Complexidade:** Baixa
- **Estimativa:** 1 hora
- **Dependências:** Tarefa 39
- **Sprint:** Sprint 2

## 🎯 Objetivo
Configurar cron expression para execução às sextas 9h.

## 📝 Especificação Técnica

### **Componentes a Implementar:**
- [ ] Cron expression configurada para sextas-feiras às 9h (0 0 9 * * FRI)
- [ ] Método @Scheduled no NewsletterScheduledService
- [ ] Configuração de timezone (UTC vs local)
- [ ] Tratamento de feriados e exception handling
- [ ] Logs detalhados de execução agendada
- [ ] Configuração externalizada em properties

### **Integrações Necessárias:**
- **Com Spring Scheduler:** Anotação @Scheduled configurada
- **Com NewsletterService:** Chamada para sendWeeklyDigest()
- **Com sistema de logs:** Auditoria de execuções agendadas
- **Com monitoramento:** Métricas de job execution

## ✅ Acceptance Criteria
- [ ] **AC1:** Job executa exatamente às sextas-feiras às 09:00 UTC
- [ ] **AC2:** Cron expression configurada: "0 0 9 * * FRI"
- [ ] **AC3:** Timezone configurado corretamente (UTC)
- [ ] **AC4:** Não executa em feriados configurados
- [ ] **AC5:** Logs registram início, progresso e conclusão
- [ ] **AC6:** Tratamento de erros não impede próximas execuções
- [ ] **AC7:** Configuração externalizada em application.properties

## 🧪 Testes Requeridos

### **Testes Unitários:**
- [ ] Teste de validação da cron expression
- [ ] Teste de timezone configuration
- [ ] Teste de holiday exclusion logic
- [ ] Teste de error handling durante execução
- [ ] Teste de logs de auditoria
- [ ] Mock do NewsletterService.sendWeeklyDigest()

### **Testes de Integração:**
- [ ] Teste de agendamento real (com cron curto para teste)
- [ ] Teste de execução completa do job
- [ ] Validação de métricas coletadas

## 🔗 Arquivos Afetados
- [ ] **src/main/java/com/blog/api/newsletter/service/NewsletterScheduledService.java:** Método @Scheduled
- [ ] **src/main/resources/application.properties:** Configurações de cron e timezone
- [ ] **src/main/java/com/blog/api/newsletter/config/HolidayConfig.java:** Configuração de feriados
- [ ] **src/test/java/com/blog/api/newsletter/service/NewsletterScheduledServiceTest.java:** Testes unitários

## 📚 Documentação para IA

### **Contexto do Projeto:**
- **Stack:** Java 21 + Spring Boot 3.2 + PostgreSQL + Redis
- **Arquitetura:** Clean Architecture (Controller → Service → Repository)
- **Padrões:** Builder Pattern, Java Records para DTOs, Cache-First

### **Implementação Esperada:**
1. Criar NewsletterScheduledService com método:
   ```java
   @Scheduled(cron = "${newsletter.weekly-digest.cron:0 0 9 * * FRI}")
   @SchedulerLock(name = "weeklyDigestJob", lockAtMostFor = "2h")
   public void sendWeeklyDigestJob() {
       // Lógica de execução
   }
   ```
2. Configurar application.properties:
   ```
   newsletter.weekly-digest.cron=0 0 9 * * FRI
   newsletter.weekly-digest.timezone=UTC
   newsletter.weekly-digest.holidays=2025-01-01,2025-12-25
   ```
3. Implementar verificação de feriados
4. Adicionar logs estruturados com MDC
5. Implementar distributed locking para múltiplas instâncias
6. Configurar métricas de execução

### **Exemplos de Código Existente:**
- **Referência 1:** Outros jobs @Scheduled no projeto para padrões
- **Referência 2:** Configurações de timezone em outras partes do sistema

## 🔍 Validação e Testes

### **Como Testar:**
1. Configurar cron para execução em 1 minuto para teste
2. Verificar logs de execução no horário agendado
3. Testar timezone UTC vs local time
4. Simular feriado e verificar que job não executa
5. Testar tratamento de erros (falha no NewsletterService)
6. Verificar que múltiplas instâncias não executam simultaneamente
7. Validar métricas coletadas (duração, sucesso/falha)
8. Testar recovery após falha

### **Critérios de Sucesso:**
- [ ] Job executa precisamente no horário configurado
- [ ] Timezone UTC respeitado independente do servidor
- [ ] Feriados são respeitados (job não executa)
- [ ] Distributed locking previne execução dupla
- [ ] Logs estruturados com todas as informações necessárias
- [ ] Métricas de execução funcionais
- [ ] Recovery automático após falhas

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
*[Próxima tarefa da sequência]*

---

**Criado em:** Agosto 2025  
**Última Atualização:** Agosto 2025  
**Responsável:** AI-Driven Development  
**Reviewer:** [Nome do reviewer, se aplicável]
