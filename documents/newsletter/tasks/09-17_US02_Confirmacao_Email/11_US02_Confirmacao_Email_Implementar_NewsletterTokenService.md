# 11_US02_Confirmacao_Email_Implementar_NewsletterTokenService.md

## 📋 Contexto da Tarefa
- **User Story:** US02 - Confirmação de E-mail
- **Número da Tarefa:** 11/95
- **Complexidade:** Média
- **Estimativa:** 3 horas
- **Dependências:** Tarefas 09, 10 (NewsletterToken, Repository)
- **Sprint:** Sprint 1

## 🎯 Objetivo
Implementar o NewsletterTokenService para geração, validação e limpeza de tokens de confirmação, unsubscribe e solicitação de dados, com diferentes TTLs por tipo.

## 📝 Especificação Técnica

### **Componentes a Implementar:**
- [ ] NewsletterTokenService com @Service
- [ ] Métodos de geração de tokens por tipo
- [ ] Validação de tokens (validade, uso)
- [ ] Limpeza automática de tokens expirados
- [ ] Integração com scheduler

### **Integrações Necessárias:**
- **Com Repository:** NewsletterTokenRepository
- **Com Scheduler:** Limpeza automática
- **Com UUID:** Geração de tokens únicos

## ✅ Acceptance Criteria
- [ ] **AC1:** Métodos para gerar tokens de diferentes tipos
- [ ] **AC2:** Validação de tokens com verificação de expiração
- [ ] **AC3:** Marcação de tokens como usados
- [ ] **AC4:** Job de limpeza de tokens expirados
- [ ] **AC5:** Diferentes TTLs por tipo de token

## 🧪 Testes Requeridos

### **Testes Unitários:**
- [ ] Teste de geração de token
- [ ] Teste de validação de token válido/inválido
- [ ] Teste de token expirado
- [ ] Teste de limpeza automática

### **Testes de Integração:**
- [ ] Teste de persistência de tokens
- [ ] Teste de job de limpeza

## 🔗 Arquivos Afetados
- [ ] **src/main/java/com/blog/api/service/NewsletterTokenService.java:** Novo service
- [ ] **src/test/java/com/blog/api/service/NewsletterTokenServiceTest.java:** Testes

## 📚 Documentação para IA

### **Contexto do Projeto:**
- **Stack:** Java 21 + Spring Boot 3.2 + PostgreSQL + Redis
- **Arquitetura:** Clean Architecture (Controller → Service → Repository)
- **Padrões:** Builder Pattern, Java Records para DTOs, Cache-First

### **Implementação Esperada:**
Criar service dedicado para gerenciamento de tokens. Utilizar diferentes TTLs por tipo: confirmação (48h), unsubscribe (1 ano), dados (24h).

### **Exemplos de Código Existente:**
- **Referência 1:** `/src/main/java/com/blog/api/service/VerificationTokenService.java`

## 🔍 Validação e Testes

### **Como Testar:**
1. Gerar tokens de diferentes tipos
2. Validar expiração
3. Testar limpeza automática
4. Verificar marcação como usado

### **Critérios de Sucesso:**
- [ ] Tokens gerados corretamente
- [ ] Validação funcionando
- [ ] Limpeza automática ativa

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
- **Estimativa:** 3 horas
- **Real:** ___ horas *(a ser preenchido após implementação)*

### **Complexidade:**
- **Estimada:** Média
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
*[Tarefa 12: Integrar com EmailService existente]*

---

**Criado em:** Agosto 2025  
**Última Atualização:** Agosto 2025  
**Responsável:** AI-Driven Development  
**Reviewer:** [Nome do reviewer, se aplicável]