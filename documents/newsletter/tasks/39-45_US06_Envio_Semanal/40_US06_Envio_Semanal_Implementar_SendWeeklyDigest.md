# 40_US06_Envio_Semanal_Implementar_SendWeeklyDigest.md

## 📋 Contexto da Tarefa
- **User Story:** US06 - Envio Semanal
- **Número da Tarefa:** 40/95
- **Complexidade:** Alta
- **Estimativa:** 4 horas
- **Dependências:** Tarefas 01, 39
- **Sprint:** Sprint 2

## 🎯 Objetivo
Implementar método sendWeeklyDigest no NewsletterService.

## 📝 Especificação Técnica

### **Componentes a Implementar:**
- [ ] Método sendWeeklyDigest() no NewsletterService
- [ ] Lógica para consultar subscribers ativos (confirmed=true)
- [ ] Integração com PostService para buscar posts dos últimos 7 dias
- [ ] Processamento de template HTML para digest
- [ ] Envio em lote (batch) com controle de rate limiting
- [ ] Logs de auditoria e métricas de entrega

### **Integrações Necessárias:**
- **Com PostService:** Busca de posts publicados nos últimos 7 dias
- **Com EmailService:** Envio dos emails com template processado
- **Com NewsletterRepository:** Consulta de subscribers confirmados
- **Com Template Engine:** Processamento do template HTML do digest

## ✅ Acceptance Criteria
- [ ] **AC1:** Método sendWeeklyDigest() implementado no NewsletterService
- [ ] **AC2:** Consulta apenas subscribers com status CONFIRMED
- [ ] **AC3:** Integração com PostService para buscar posts dos últimos 7 dias
- [ ] **AC4:** Processamento de template HTML com lista de posts
- [ ] **AC5:** Envio em lote com controle de rate limiting (max 10 emails/segundo)
- [ ] **AC6:** Logs detalhados de sucesso/falha por subscriber
- [ ] **AC7:** Métricas de performance (tempo total, emails enviados/falharam)

## 🧪 Testes Requeridos

### **Testes Unitários:**
- [ ] Teste de sendWeeklyDigest() com lista de posts válida
- [ ] Teste com zero posts nos últimos 7 dias (não deve enviar)
- [ ] Teste com zero subscribers confirmados
- [ ] Teste de tratamento de falhas no EmailService
- [ ] Teste de rate limiting
- [ ] Teste de logs de auditoria

### **Testes de Integração:**
- [ ] Teste end-to-end com dados reais no banco
- [ ] Teste de performance com 1000+ subscribers
- [ ] Teste de integração com MailHog

## 🔗 Arquivos Afetados
- [ ] **src/main/java/com/blog/api/newsletter/service/NewsletterService.java:** Implementar sendWeeklyDigest()
- [ ] **src/main/java/com/blog/api/newsletter/dto/WeeklyDigestData.java:** DTO para dados do digest
- [ ] **src/main/java/com/blog/api/newsletter/service/NewsletterScheduledService.java:** Service para jobs agendados
- [ ] **src/test/java/com/blog/api/newsletter/service/NewsletterServiceTest.java:** Testes unitários
- [ ] **src/test/java/com/blog/api/newsletter/integration/WeeklyDigestIntegrationTest.java:** Testes de integração

## 📚 Documentação para IA

### **Contexto do Projeto:**
- **Stack:** Java 21 + Spring Boot 3.2 + PostgreSQL + Redis
- **Arquitetura:** Clean Architecture (Controller → Service → Repository)
- **Padrões:** Builder Pattern, Java Records para DTOs, Cache-First

### **Implementação Esperada:**
1. Criar método sendWeeklyDigest() que:
   - Consulta posts dos últimos 7 dias via PostService
   - Verifica se existem posts (se não, não envia digest)
   - Busca todos subscribers com status CONFIRMED
   - Processa template HTML com dados dos posts
   - Envia emails em lote com rate limiting
   - Registra logs detalhados e métricas
2. Criar DTO WeeklyDigestData para dados do template
3. Implementar controle de rate limiting (10 emails/segundo)
4. Adicionar tratamento robusto de erros
5. Implementar logs estruturados com MDC

### **Exemplos de Código Existente:**
- **Referência 1:** NewsletterService.sendConfirmationEmail() para padrões de envio
- **Referência 2:** EmailService para integração com sistema de email

## 🔍 Validação e Testes

### **Como Testar:**
1. Criar dados de teste: posts dos últimos 7 dias + subscribers confirmados
2. Executar sendWeeklyDigest() e verificar logs
3. Validar emails recebidos no MailHog
4. Testar cenário sem posts (não deve enviar)
5. Testar cenário sem subscribers (deve logar e finalizar)
6. Verificar rate limiting com sleep entre envios
7. Testar tratamento de falhas no EmailService

### **Critérios de Sucesso:**
- [ ] Método executa sem erros com dados válidos
- [ ] Emails aparecem no MailHog com conteúdo correto
- [ ] Rate limiting respeitado (10 emails/segundo máximo)
- [ ] Logs estruturados com métricas de performance
- [ ] Tratamento adequado de cenários edge cases

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
