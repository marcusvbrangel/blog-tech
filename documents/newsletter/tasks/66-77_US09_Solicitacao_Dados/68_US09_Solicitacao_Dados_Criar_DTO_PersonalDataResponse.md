# 68_US09_Solicitacao_Dados_Criar_DTO_PersonalDataResponse.md

## 📋 Contexto da Tarefa
- **User Story:** US09 - Solicitação de Dados
- **Número da Tarefa:** 68/95
- **Complexidade:** Baixa
- **Estimativa:** 2 horas
- **Dependências:** Tarefas 01, 48
- **Sprint:** Sprint 3

## 🎯 Objetivo
Criar DTO PersonalDataResponse para retorno de dados pessoais.

## 📝 Especificação Técnica

### **Componentes a Implementar:**
- [ ] PersonalDataResponse DTO principal
- [ ] ConsentHistoryData - Histórico completo de consentimentos
- [ ] EmailHistoryData - Histórico de emails enviados
- [ ] SubscriptionData - Dados de inscrição e status
- [ ] DataRequestMetadata - Metadados da solicitação

### **Integrações Necessárias:**
- **Com NewsletterSubscriber:** Dados principais de inscrição
- **Com ConsentRecord:** Histórico detalhado de consentimentos
- **Com EmailAuditLog:** Registros de emails enviados ao usuário
- **Com DataAnonymizationService:** Anonimização de dados sensíveis quando necessário

## ✅ Acceptance Criteria
- [ ] **AC1:** DTO estruturado em Java Record com validação completa
- [ ] **AC2:** Inclusão de todos os dados pessoais: email, nome, data de inscrição, status
- [ ] **AC3:** Histórico completo de consentimentos com timestamps e versões
- [ ] **AC4:** Histórico de emails enviados com datas, assuntos e status de entrega
- [ ] **AC5:** Metadados da solicitação: timestamp, IP, user agent, motivo
- [ ] **AC6:** Formato JSON estruturado para portabilidade de dados
- [ ] **AC7:** Anotações para documentação Swagger detalhada
- [ ] **AC8:** Conformidade com LGPD para estrutura de dados exportáveis

## 🧪 Testes Requeridos

### **Testes Unitários:**
- [ ] Teste de construção do DTO com todos os campos obrigatórios
- [ ] Teste de serialização JSON com estrutura correta
- [ ] Teste de validação de campos nulos e vazios
- [ ] Teste de formato de datas em ISO 8601
- [ ] Teste de anotações Swagger e documentação

### **Testes de Integração:**
- [ ] Teste de agregação completa de dados de múltiplas entidades
- [ ] Teste de performance na construção de DTOs complexos
- [ ] Teste de portabilidade: export e import de dados
- [ ] Teste de compliance: verificação de completude dos dados LGPD

## 🔗 Arquivos Afetados
- [ ] **src/main/java/com/blog/api/newsletter/dto/PersonalDataResponse.java** - DTO principal
- [ ] **src/main/java/com/blog/api/newsletter/dto/ConsentHistoryData.java** - Sub-DTO de consentimentos
- [ ] **src/main/java/com/blog/api/newsletter/dto/EmailHistoryData.java** - Sub-DTO de emails
- [ ] **src/main/java/com/blog/api/newsletter/dto/SubscriptionData.java** - Sub-DTO de inscrição
- [ ] **src/main/java/com/blog/api/newsletter/dto/DataRequestMetadata.java** - Metadados
- [ ] **src/test/java/com/blog/api/newsletter/dto/PersonalDataResponseTest.java** - Testes unitários

## 📚 Documentação para IA

### **Contexto do Projeto:**
- **Stack:** Java 21 + Spring Boot 3.2 + PostgreSQL + Redis
- **Arquitetura:** Clean Architecture (Controller → Service → Repository)
- **Padrões:** Builder Pattern, Java Records para DTOs, Cache-First

### **Implementação Esperada:**
Desenvolver DTO completo utilizando Java Records para estruturar todos os dados pessoais do usuário em formato portável e compatível com LGPD. Deve incluir dados de inscrição, histórico de consentimentos, emails enviados e metadados da solicitação.

### **Estrutura do DTO:**
```java
public record PersonalDataResponse(
    @Schema(description = "Dados de inscrição do usuário")
    SubscriptionData subscription,
    
    @Schema(description = "Histórico completo de consentimentos")
    List<ConsentHistoryData> consentHistory,
    
    @Schema(description = "Histórico de emails enviados")
    List<EmailHistoryData> emailHistory,
    
    @Schema(description = "Metadados da solicitação de dados")
    DataRequestMetadata requestMetadata
) {
    // Validações e builders conforme necessidade
}
```

### **Exemplos de Código Existente:**
- **NewsletterSubscriptionRequest:** Padrões de DTOs com Java Records
- **AdminSubscriberResponse:** Estrutura de resposta com múltiplos dados
- **ConsentUpdateRequest:** Anotações Swagger e validações

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
