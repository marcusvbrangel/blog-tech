#!/bin/bash

# Script para criar as últimas tarefas (US08, US09, Infraestrutura restantes)
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
- [ ] Testes e documentação

### **Integrações Necessárias:**
- **Com sistema principal:** Integração específica
- **Com componentes relacionados:** Dependências técnicas

## ✅ Acceptance Criteria
- [ ] **AC1:** Critério específico e testável
- [ ] **AC2:** Funcionalidade implementada corretamente
- [ ] **AC3:** Integração funcionando adequadamente
- [ ] **AC4:** Testes passando com cobertura adequada
- [ ] **AC5:** Documentação atualizada e completa

## 🧪 Testes Requeridos

### **Testes Unitários:**
- [ ] Teste da funcionalidade principal
- [ ] Teste de cenários de erro e exceções
- [ ] Teste de validações e regras de negócio
- [ ] Teste de integração com componentes

### **Testes de Integração:**
- [ ] Teste end-to-end da funcionalidade
- [ ] Teste de performance e carga
- [ ] Teste de segurança e compliance

## 🔗 Arquivos Afetados
- [ ] **Arquivo principal:** Implementação da funcionalidade core
- [ ] **Arquivo de teste:** Testes unitários e integração
- [ ] **Arquivo de configuração:** Configurações específicas

## 📚 Documentação para IA

### **Contexto do Projeto:**
- **Stack:** Java 21 + Spring Boot 3.2 + PostgreSQL + Redis
- **Arquitetura:** Clean Architecture (Controller → Service → Repository)
- **Padrões:** Builder Pattern, Java Records para DTOs, Cache-First

### **Implementação Esperada:**
$objective - Implementar seguindo rigorosamente os padrões arquiteturais estabelecidos no projeto.

### **Exemplos de Código Existente:**
- **Referência 1:** Código similar existente no projeto
- **Referência 2:** Padrões a seguir e reutilizar

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
*[Próxima tarefa na sequência de implementação]*

---

**Criado em:** Agosto 2025  
**Última Atualização:** Agosto 2025  
**Responsável:** AI-Driven Development  
**Reviewer:** [Nome do reviewer, se aplicável]
EOF

    echo "Criado: $filename"
}

# US08 - Histórico de Consentimento (Tarefas 58-65)
create_task "58" "58-65_US08_Historico_Consentimento" "US08_Historico_Consentimento_Capturar_IP_UserAgent_Consentimento" "US08 - Histórico de Consentimento" "Baixa" "2 horas" "Tarefas 01, 48" "Sprint 3" "Capturar IP e User-Agent no momento do consentimento para auditoria."

create_task "59" "58-65_US08_Historico_Consentimento" "US08_Historico_Consentimento_Implementar_Logging_Automatico_Acoes" "US08 - Histórico de Consentimento" "Média" "3 horas" "Tarefas 48, 49, 58" "Sprint 3" "Implementar logging automático de todas as ações de consentimento."

create_task "60" "58-65_US08_Historico_Consentimento" "US08_Historico_Consentimento_API_Consulta_Logs_Admin_Only" "US08 - Histórico de Consentimento" "Média" "3 horas" "Tarefas 49, 51" "Sprint 3" "Implementar API para consulta de logs restrita a admins."

create_task "61" "58-65_US08_Historico_Consentimento" "US08_Historico_Consentimento_Implementar_Filtros_Data_Logs" "US08 - Histórico de Consentimento" "Baixa" "2 horas" "Tarefa 60" "Sprint 3" "Implementar filtros por data para consulta de logs."

create_task "62" "58-65_US08_Historico_Consentimento" "US08_Historico_Consentimento_Criar_Relatorios_Auditoria" "US08 - Histórico de Consentimento" "Média" "4 horas" "Tarefas 48, 49, 60, 61" "Sprint 3" "Criar relatórios de auditoria para compliance."

create_task "63" "58-65_US08_Historico_Consentimento" "US08_Historico_Consentimento_Implementar_Retention_Policy_Logs" "US08 - Histórico de Consentimento" "Baixa" "2 horas" "Tarefas 55, 62" "Sprint 3" "Implementar política de retenção específica para logs."

create_task "64" "58-65_US08_Historico_Consentimento" "US08_Historico_Consentimento_Testes_Persistencia_Logs" "US08 - Histórico de Consentimento" "Média" "3 horas" "Tarefas 58-63" "Sprint 3" "Implementar testes de persistência e integridade dos logs."

create_task "65" "58-65_US08_Historico_Consentimento" "US08_Historico_Consentimento_Testes_Consulta_Auditoria" "US08 - Histórico de Consentimento" "Média" "3 horas" "Tarefas 60-64" "Sprint 3" "Implementar testes de consulta e geração de relatórios de auditoria."

# US09 - Solicitação de Dados (Tarefas 66-77)
create_task "66" "66-77_US09_Solicitacao_Dados" "US09_Solicitacao_Dados_Criar_Endpoint_GET_My_Data" "US09 - Solicitação de Dados" "Média" "3 horas" "Tarefas 09, 11" "Sprint 3" "Criar endpoint GET /api/newsletter/my-data para solicitação de dados pessoais."

create_task "67" "66-77_US09_Solicitacao_Dados" "US09_Solicitacao_Dados_Implementar_Validacao_Token_Acesso" "US09 - Solicitação de Dados" "Baixa" "2 horas" "Tarefas 09, 11, 66" "Sprint 3" "Implementar validação robusta de token de acesso aos dados."

create_task "68" "66-77_US09_Solicitacao_Dados" "US09_Solicitacao_Dados_Criar_DTO_PersonalDataResponse" "US09 - Solicitação de Dados" "Baixa" "2 horas" "Tarefas 01, 48" "Sprint 3" "Criar DTO PersonalDataResponse para retorno de dados pessoais."

create_task "69" "66-77_US09_Solicitacao_Dados" "US09_Solicitacao_Dados_Implementar_Retorno_Todos_Dados_Subscriber" "US09 - Solicitação de Dados" "Média" "4 horas" "Tarefas 01, 66, 68" "Sprint 3" "Implementar retorno completo de todos os dados do subscriber."

create_task "70" "66-77_US09_Solicitacao_Dados" "US09_Solicitacao_Dados_Incluir_Historico_Consentimento" "US09 - Solicitação de Dados" "Baixa" "2 horas" "Tarefas 48, 49, 69" "Sprint 3" "Incluir histórico completo de consentimento nos dados retornados."

create_task "71" "66-77_US09_Solicitacao_Dados" "US09_Solicitacao_Dados_Incluir_Historico_Emails_Enviados" "US09 - Solicitação de Dados" "Baixa" "2 horas" "Tarefa 69" "Sprint 3" "Incluir histórico de emails enviados nos dados pessoais."

create_task "72" "66-77_US09_Solicitacao_Dados" "US09_Solicitacao_Dados_Implementar_Anonizacao_Dados_Sensiveis" "US09 - Solicitação de Dados" "Alta" "4 horas" "Tarefas 52, 69" "Sprint 3" "Implementar anonização de dados sensíveis antes do retorno."

create_task "73" "66-77_US09_Solicitacao_Dados" "US09_Solicitacao_Dados_Criar_Token_Especifico_Solicitacao" "US09 - Solicitação de Dados" "Baixa" "2 horas" "Tarefas 09, 11" "Sprint 3" "Criar token específico e seguro para solicitação de dados."

create_task "74" "66-77_US09_Solicitacao_Dados" "US09_Solicitacao_Dados_Implementar_Rate_Limiting_Solicitacoes" "US09 - Solicitação de Dados" "Média" "3 horas" "Tarefas 66, 67" "Sprint 3" "Implementar rate limiting para prevenir abuso de solicitações."

create_task "75" "66-77_US09_Solicitacao_Dados" "US09_Solicitacao_Dados_Logs_Solicitacao_Dados" "US09 - Solicitação de Dados" "Baixa" "2 horas" "Tarefas 48, 49, 66" "Sprint 3" "Implementar logs detalhados de solicitações de dados."

create_task "76" "66-77_US09_Solicitacao_Dados" "US09_Solicitacao_Dados_Testes_Portabilidade_LGPD" "US09 - Solicitação de Dados" "Alta" "4 horas" "Tarefas 66-75" "Sprint 3" "Implementar testes de portabilidade de dados conforme LGPD."

create_task "77" "66-77_US09_Solicitacao_Dados" "US09_Solicitacao_Dados_Testes_Compliance_Anonizacao" "US09 - Solicitação de Dados" "Alta" "4 horas" "Tarefas 72, 76" "Sprint 3" "Implementar testes de compliance e anonização de dados."

echo "US08 e US09 criadas!"