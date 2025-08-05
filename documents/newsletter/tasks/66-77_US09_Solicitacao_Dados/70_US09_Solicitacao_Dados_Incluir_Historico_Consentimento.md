# 70_US09_Solicitacao_Dados_Incluir_Historico_Consentimento.md

### ✅ US09 – Solicitação de Dados
*Como titular de dados, quero solicitar uma cópia de todos os meus dados pessoais, para exercer meu direito à portabilidade conforme LGPD.*

## 📋 Descrição da Tarefa
**Incluir Histórico Consentimento**

Desenvolver sistema abrangente de coleta e formatação do histórico completo de consentimentos.
Incluir todos os consentimentos, revisões, revogações com contexto detalhado e rastreabilidade completa para LGPD.

## 📋 Contexto da Tarefa
- **User Story:** US09 - Solicitação de Dados
- **Número da Tarefa:** 70/95
- **Complexidade:** Baixa
- **Estimativa:** 2 horas
- **Dependências:** Tarefas 48, 49, 69
- **Sprint:** Sprint 3

## 🎯 Objetivo
Incluir histórico completo de consentimento nos dados retornados.

## 📝 Especificação Técnica

### **Componentes a Implementar:**
- [ ] ConsentHistoryCollectorService - Coletor de histórico de consentimentos
- [ ] ConsentVersionTracker - Rastreamento de versões de termos
- [ ] ConsentTimelineBuilder - Construção de linha do tempo
- [ ] ConsentDataFormatter - Formatador para portabilidade LGPD
- [ ] ConsentComplianceValidator - Validador de completude

### **Integrações Necessárias:**
- **Com ConsentRepository:** Acesso a todos os registros de consentimento
- **Com ConsentVersionRepository:** Histórico de versões de termos e políticas
- **Com AuditLogRepository:** Logs detalhados de mudanças de consentimento
- **Com PersonalDataResponse:** Integração no DTO principal de dados pessoais

## ✅ Acceptance Criteria
- [ ] **AC1:** Inclusão de todos os registros de consentimento: inicial, revisões, revogações
- [ ] **AC2:** Timestamps precisos: data/hora de cada consentimento com timezone
- [ ] **AC3:** Versões completas: texto dos termos aceitos em cada momento
- [ ] **AC4:** Contexto detalhado: IP, user agent, origem da ação (web, email, API)
- [ ] **AC5:** Status de cada consentimento: ativo, revogado, expirado, substituído
- [ ] **AC6:** Categorias de consentimento: newsletter, marketing, analytics, etc.
- [ ] **AC7:** Ordenação cronológica para fácil auditoria
- [ ] **AC8:** Conformidade LGPD: rastreabilidade completa de consentimentos
- [ ] **AC9:** Indicação de consentimentos herdados ou migrados

## 🧪 Testes Requeridos

### **Testes Unitários:**
- [ ] Teste de coleta de histórico completo para subscriber com múltiplos consentimentos
- [ ] Teste de ordenação cronológica de consentimentos
- [ ] Teste de inclusão de versões de termos corretas
- [ ] Teste de tratamento de consentimentos revogados
- [ ] Teste de formatação para portabilidade LGPD

### **Testes de Integração:**
- [ ] Teste de integridade: consentimentos vs registros de auditoria
- [ ] Teste de performance com históricos longos (100+ consentimentos)
- [ ] Teste de completude: nenhum consentimento omitido
- [ ] Teste de compliance LGPD: rastreabilidade total
- [ ] Teste de edge cases: consentimentos corrompidos ou incompletos

## 🔗 Arquivos Afetados
- [ ] **src/main/java/com/blog/api/newsletter/service/ConsentHistoryCollectorService.java** - Coletor principal
- [ ] **src/main/java/com/blog/api/newsletter/tracker/ConsentVersionTracker.java** - Rastreador de versões
- [ ] **src/main/java/com/blog/api/newsletter/builder/ConsentTimelineBuilder.java** - Construtor de timeline
- [ ] **src/main/java/com/blog/api/newsletter/formatter/ConsentDataFormatter.java** - Formatador
- [ ] **src/main/java/com/blog/api/newsletter/dto/ConsentHistoryData.java** - DTO específico
- [ ] **src/test/java/com/blog/api/newsletter/service/ConsentHistoryCollectorServiceTest.java** - Testes unitários

## 📚 Documentação para IA

### **Contexto do Projeto:**
- **Stack:** Java 21 + Spring Boot 3.2 + PostgreSQL + Redis
- **Arquitetura:** Clean Architecture (Controller → Service → Repository)
- **Padrões:** Builder Pattern, Java Records para DTOs, Cache-First

### **Implementação Esperada:**
Desenvolver sistema abrangente de coleta e formatação do histórico completo de consentimentos para conformidade total com LGPD. Deve incluir todos os consentimentos, revisões, revogações com contexto detalhado, versões de termos e rastreabilidade completa.

### **Estrutura do Histórico:**
```java
public record ConsentHistoryData(
    LocalDateTime timestamp,
    ConsentAction action, // GRANTED, REVISED, REVOKED
    ConsentType type,     // NEWSLETTER, MARKETING, ANALYTICS
    String termsVersion,
    String termsContent,
    ConsentContext context, // IP, USER_AGENT, SOURCE
    ConsentStatus status    // ACTIVE, REVOKED, EXPIRED
) {
    // Dados organizados cronologicamente para auditoria
}
```

### **Exemplos de Código Existente:**
- **ConsentService:** Lógica de gerenciamento de consentimentos
- **AuditLogService:** Padrões de rastreamento e logs detalhados
- **ConsentRepository:** Consultas de histórico e versionamento

## 🔍 Validação e Testes

### **Como Testar:**
1. **Validar inclusão de histórico completo:**
   - Testar coleta de todos os registros de consentimento: inicial, revisões, revogações
   - Verificar inclusão de timestamps precisos com timezone correto
   - Validar que versões de termos aceitos são preservadas

2. **Testar ordenação cronológica:**
   - Verificar que consentimentos são ordenados por data/hora
   - Testar facilidade de auditoria com timeline clara
   - Validar que consentimentos mais recentes aparecem primeiro

3. **Verificar contexto detalhado:**
   - Testar captura de IP, user agent e origem da ação
   - Verificar classificação correta: web, email, API
   - Validar inclusão de metadados técnicos completos

4. **Testar status e categorias:**
   - Verificar identificação correta de status: ativo, revogado, expirado
   - Testar categorização: newsletter, marketing, analytics
   - Validar tratamento de consentimentos herdados ou migrados

5. **Validar compliance LGPD:**
   - Confirmar rastreabilidade completa de todos os consentimentos
   - Testar conformidade com requisitos de portabilidade
   - Verificar que nenhum consentimento é omitido do histórico

### **Critérios de Sucesso:**
- [ ] Histórico completo: 100% dos consentimentos incluídos
- [ ] Ordenação cronológica correta para auditoria
- [ ] Contexto detalhado: IP, user agent, origem preservados
- [ ] Versões de termos corretas associadas a cada consentimento
- [ ] Status e categorias classificados adequadamente
- [ ] Performance < 200ms para históricos longos
- [ ] Conformidade LGPD: rastreabilidade total garantida

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
