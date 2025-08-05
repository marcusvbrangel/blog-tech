# 45_US06_Envio_Semanal_Testes_MockBean_Scheduler.md

## 📋 Contexto da Tarefa
- **User Story:** US06 - Envio Semanal
- **Número da Tarefa:** 45/95
- **Complexidade:** Alta
- **Estimativa:** 4 horas
- **Dependências:** Tarefas 39-44
- **Sprint:** Sprint 2

## 🎯 Objetivo
Implementar testes com @MockBean para scheduler.

## 📝 Especificação Técnica

### **Componentes a Implementar:**
- [ ] Testes unitários com @MockBean para TaskScheduler
- [ ] Testes de configuração do Spring Scheduler
- [ ] Mocks para NewsletterService.sendWeeklyDigest()
- [ ] Testes de cron expression validation
- [ ] Testes de exception handling em scheduled jobs
- [ ] Testes de métricas e logging
- [ ] Testes de distributed locking

### **Integrações Necessárias:**
- **Com Spring Test:** @SpringBootTest e @MockBean
- **Com Mockito:** Mocking de services e repositories
- **Com TestContainers:** Testes de integração com Redis (opcional)
- **Com AssertJ:** Assertions avançadas para validações

## ✅ Acceptance Criteria
- [ ] **AC1:** @MockBean configurado para TaskScheduler sem execução real
- [ ] **AC2:** Testes verificam chamadas para NewsletterService.sendWeeklyDigest()
- [ ] **AC3:** Validação de cron expressions válidas e inválidas
- [ ] **AC4:** Testes de exception handling (service failures)
- [ ] **AC5:** Verificação de logs estruturados em testes
- [ ] **AC6:** Testes de distributed locking behavior
- [ ] **AC7:** Cobertura de código ≥ 95% para classes de scheduling

## 🧪 Testes Requeridos

### **Testes Unitários:**
- [ ] Teste de execução do método @Scheduled com mock
- [ ] Teste de validação de cron expression
- [ ] Teste de holiday checking logic
- [ ] Teste de exception handling (NewsletterService falha)
- [ ] Teste de logging com MDC context
- [ ] Teste de métricas collection
- [ ] Teste de distributed lock acquisition/release

### **Testes de Integração:**
- [ ] Teste de contexto Spring com scheduler habilitado
- [ ] Teste de configuração de beans do scheduler
- [ ] Teste end-to-end com MailHog (opcional)
- [ ] Teste de performance do job completo

## 🔗 Arquivos Afetados
- [ ] **src/test/java/com/blog/api/newsletter/service/NewsletterScheduledServiceTest.java:** Testes unitários
- [ ] **src/test/java/com/blog/api/newsletter/config/NewsletterSchedulerConfigTest.java:** Testes de configuração
- [ ] **src/test/java/com/blog/api/newsletter/integration/WeeklyDigestSchedulingIntegrationTest.java:** Testes integração
- [ ] **src/test/resources/application-test.properties:** Configurações para teste
- [ ] **src/test/java/com/blog/api/newsletter/TestSchedulerConfig.java:** Configuração de teste

## 📚 Documentação para IA

### **Contexto do Projeto:**
- **Stack:** Java 21 + Spring Boot 3.2 + PostgreSQL + Redis
- **Arquitetura:** Clean Architecture (Controller → Service → Repository)
- **Padrões:** Builder Pattern, Java Records para DTOs, Cache-First

### **Implementação Esperada:**
1. Criar NewsletterScheduledServiceTest com:
   ```java
   @SpringBootTest
   @MockBean(TaskScheduler.class)
   class NewsletterScheduledServiceTest {
       @MockBean NewsletterService newsletterService;
       @Autowired NewsletterScheduledService scheduledService;
   }
   ```
2. Implementar testes para:
   - Validação de chamada do sendWeeklyDigest()
   - Verificação de logs com @Slf4j e Logback testing
   - Testes de exception handling com verify()
   - Mock de distributed lock com ShedLock
3. Criar TestSchedulerConfig para desabilitar scheduling real
4. Usar @TestConfiguration para override de beans
5. Implementar testes de métricas com Micrometer test utilities
6. Adicionar testes de integração com @TestContainers (Redis)

### **Exemplos de Código Existente:**
- **Referência 1:** Testes existentes com @MockBean no projeto
- **Referência 2:** Padrões de testes de services com Mockito

## 🔍 Validação e Testes

### **Como Testar:**
1. Executar testes unitários com `mvn test -Dtest=NewsletterScheduledServiceTest`
2. Verificar cobertura de código com JaCoCo report
3. Validar mocks são chamados corretamente (verify)
4. Testar cenários de falha do NewsletterService
5. Verificar logs são gerados nos testes
6. Executar testes de integração com profile 'test'
7. Validar que scheduler real não executa durante testes
8. Verificar métricas são coletadas corretamente

### **Critérios de Sucesso:**
- [ ] Todos os testes passam sem execução real do scheduler
- [ ] Mocks verificam chamadas corretas aos services
- [ ] Exception handling testado adequadamente
- [ ] Logs estruturados validados em testes
- [ ] Cobertura de código ≥ 95% para classes de scheduling
- [ ] Testes executam rapidamente (< 30s total)
- [ ] Distributed locking behavior validado
- [ ] Métricas de teste funcionais

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
- **Estimativa:** 4 horas
- **Real:** ___ horas *(a ser preenchido após implementação)*

### **Complexidade:**
- **Estimada:** Alta
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
