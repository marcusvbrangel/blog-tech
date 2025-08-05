#!/bin/bash

# Script para criar as tarefas finais (US06, US07 restantes, US08, US09, Infraestrutura restantes)
BASE_PATH="/home/base/Documentos/desenvolvimento/claude-estudo/first-project/documents/newsletter/tasks"

# Função para criar arquivo de tarefa
create_task() {
    local num=$1
    local folder=$2
    local title=$3
    local us=$4
    local complexity=$5
    local estimate=$6
    local deps=$7
    local sprint=$8
    local objective=$9
    
    local filename="${num}_${title}.md"
    local full_path="$BASE_PATH/$folder/$filename"
    
    cat > "$full_path" <<EOF
# $filename

## 📋 Contexto da Tarefa
- **User Story:** $us
- **Número da Tarefa:** $num/95
- **Complexidade:** $complexity
- **Estimativa:** $estimate
- **Dependências:** $deps
- **Sprint:** $sprint

## 🎯 Objetivo
$objective

## 📝 Especificação Técnica

### **Componentes a Implementar:**
- [ ] Componente principal da tarefa
- [ ] Integrações necessárias
- [ ] Configurações específicas
- [ ] Validações e tratamento de erros

### **Integrações Necessárias:**
- **Com sistema principal:** Integração específica
- **Com componentes relacionados:** Dependências

## ✅ Acceptance Criteria
- [ ] **AC1:** Critério específico e testável
- [ ] **AC2:** Funcionalidade implementada corretamente
- [ ] **AC3:** Integração funcionando
- [ ] **AC4:** Testes passando
- [ ] **AC5:** Documentação atualizada

## 🧪 Testes Requeridos

### **Testes Unitários:**
- [ ] Teste da funcionalidade principal
- [ ] Teste de cenários de erro
- [ ] Teste de validações

### **Testes de Integração:**
- [ ] Teste end-to-end
- [ ] Teste de performance

## 🔗 Arquivos Afetados
- [ ] **Arquivo principal:** Implementação da funcionalidade
- [ ] **Arquivo de teste:** Testes unitários e integração

## 📚 Documentação para IA

### **Contexto do Projeto:**
- **Stack:** Java 21 + Spring Boot 3.2 + PostgreSQL + Redis
- **Arquitetura:** Clean Architecture (Controller → Service → Repository)
- **Padrões:** Builder Pattern, Java Records para DTOs, Cache-First

### **Implementação Esperada:**
$objective - Seguir rigorosamente os padrões estabelecidos no projeto.

### **Exemplos de Código Existente:**
- **Referência 1:** Código similar no projeto

## 🔍 Validação e Testes

### **Como Testar:**
1. Executar implementação
2. Validar funcionalidade
3. Verificar integrações

### **Critérios de Sucesso:**
- [ ] Funcionalidade implementada
- [ ] Testes passando
- [ ] Performance adequada

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
- **Estimativa:** $estimate
- **Real:** ___ horas *(a ser preenchido após implementação)*

### **Complexidade:**
- **Estimada:** $complexity
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
EOF

    echo "Criado: $filename"
}

# US06 - Envio Semanal (Tarefas 39-45)
create_task "39" "39-45_US06_Envio_Semanal" "US06_Envio_Semanal_Configurar_Spring_Scheduler" "US06 - Envio Semanal" "Média" "3 horas" "Nenhuma" "Sprint 2" "Configurar Spring Scheduler com @Scheduled para jobs automáticos."

create_task "40" "39-45_US06_Envio_Semanal" "US06_Envio_Semanal_Implementar_SendWeeklyDigest" "US06 - Envio Semanal" "Alta" "4 horas" "Tarefas 01, 39" "Sprint 2" "Implementar método sendWeeklyDigest no NewsletterService."

create_task "41" "39-45_US06_Envio_Semanal" "US06_Envio_Semanal_Criar_Query_Posts_7_Dias" "US06 - Envio Semanal" "Baixa" "2 horas" "Tarefa 40" "Sprint 2" "Criar query para buscar posts dos últimos 7 dias."

create_task "42" "39-45_US06_Envio_Semanal" "US06_Envio_Semanal_Criar_Template_HTML_Digest" "US06 - Envio Semanal" "Baixa" "3 horas" "Tarefa 32" "Sprint 2" "Criar template HTML para digest semanal de posts."

create_task "43" "39-45_US06_Envio_Semanal" "US06_Envio_Semanal_Implementar_Logic_Multiplos_Posts" "US06 - Envio Semanal" "Média" "3 horas" "Tarefas 40, 41, 42" "Sprint 2" "Implementar lógica para processar múltiplos posts no digest."

create_task "44" "39-45_US06_Envio_Semanal" "US06_Envio_Semanal_Configurar_Cron_Sexta_9h" "US06 - Envio Semanal" "Baixa" "1 hora" "Tarefa 39" "Sprint 2" "Configurar cron expression para execução às sextas 9h."

create_task "45" "39-45_US06_Envio_Semanal" "US06_Envio_Semanal_Testes_MockBean_Scheduler" "US06 - Envio Semanal" "Alta" "4 horas" "Tarefas 39-44" "Sprint 2" "Implementar testes com @MockBean para scheduler."

# US07 - Segurança LGPD (Tarefas restantes 47-57)
create_task "47" "46-57_US07_Seguranca_LGPD" "US07_Seguranca_LGPD_Criar_DTO_ConsentimentoRequest" "US07 - Segurança LGPD" "Baixa" "1 hora" "Tarefa 46" "Sprint 3" "Criar DTO ConsentimentoRequest para solicitações de consentimento."

create_task "48" "46-57_US07_Seguranca_LGPD" "US07_Seguranca_LGPD_Criar_Entidade_NewsletterConsentLog" "US07 - Segurança LGPD" "Média" "3 horas" "Tarefa 01" "Sprint 3" "Criar entidade NewsletterConsentLog para auditoria LGPD."

create_task "49" "46-57_US07_Seguranca_LGPD" "US07_Seguranca_LGPD_Implementar_NewsletterConsentLogRepository" "US07 - Segurança LGPD" "Baixa" "1 hora" "Tarefa 48" "Sprint 3" "Implementar repository para logs de consentimento."

create_task "50" "46-57_US07_Seguranca_LGPD" "US07_Seguranca_LGPD_Criar_Endpoint_DELETE_Newsletter_Delete" "US07 - Segurança LGPD" "Média" "4 horas" "Tarefas 09, 11" "Sprint 3" "Criar endpoint DELETE /api/newsletter/delete para exclusão total."

create_task "51" "46-57_US07_Seguranca_LGPD" "US07_Seguranca_LGPD_Criar_Endpoint_GET_Consent_History" "US07 - Segurança LGPD" "Média" "3 horas" "Tarefas 48, 49" "Sprint 3" "Criar endpoint GET /api/newsletter/consent-history para admins."

create_task "52" "46-57_US07_Seguranca_LGPD" "US07_Seguranca_LGPD_Implementar_Criptografia_Dados_Sensiveis" "US07 - Segurança LGPD" "Alta" "5 horas" "Tarefas 01, 48" "Sprint 3" "Implementar criptografia de dados sensíveis no banco."

create_task "53" "46-57_US07_Seguranca_LGPD" "US07_Seguranca_LGPD_Implementar_Logs_Acesso_Dados_Pessoais" "US07 - Segurança LGPD" "Média" "3 horas" "Tarefas 48, 49" "Sprint 3" "Implementar logs de acesso a dados pessoais."

create_task "54" "46-57_US07_Seguranca_LGPD" "US07_Seguranca_LGPD_Implementar_Soft_Delete_Compliance" "US07 - Segurança LGPD" "Média" "3 horas" "Tarefas 01, 50" "Sprint 3" "Implementar soft delete para compliance LGPD."

create_task "55" "46-57_US07_Seguranca_LGPD" "US07_Seguranca_LGPD_Configurar_Data_Retention_Policies" "US07 - Segurança LGPD" "Baixa" "2 horas" "Tarefas 48, 54" "Sprint 3" "Configurar políticas de retenção de dados."

create_task "56" "46-57_US07_Seguranca_LGPD" "US07_Seguranca_LGPD_Implementar_Token_Exclusao_Dados" "US07 - Segurança LGPD" "Média" "3 horas" "Tarefas 09, 11, 50" "Sprint 3" "Implementar token específico para exclusão de dados."

create_task "57" "46-57_US07_Seguranca_LGPD" "US07_Seguranca_LGPD_Testes_Compliance_LGPD" "US07 - Segurança LGPD" "Alta" "5 horas" "Tarefas 46-56" "Sprint 3" "Implementar testes abrangentes de compliance LGPD."

echo "US06 e US07 restantes criadas!"