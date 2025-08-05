#!/bin/bash

# Script para gerar todos os 95 arquivos de tarefas da Newsletter
# Baseado no template estabelecido e índice de tarefas

BASE_PATH="/home/base/Documentos/desenvolvimento/claude-estudo/first-project/documents/newsletter/tasks"

# Template base
create_task_file() {
    local task_num=$1
    local folder=$2
    local filename=$3
    local user_story=$4
    local title=$5
    local complexity=$6
    local estimate=$7
    local dependencies=$8
    local sprint=$9
    local objective="${10}"
    
    local full_path="$BASE_PATH/$folder/$filename"
    
    cat > "$full_path" <<EOF
# $filename

## 📋 Contexto da Tarefa
- **User Story:** $user_story
- **Número da Tarefa:** $task_num/95
- **Complexidade:** $complexity
- **Estimativa:** $estimate
- **Dependências:** $dependencies
- **Sprint:** $sprint

## 🎯 Objetivo
$objective

## 📝 Especificação Técnica

### **Componentes a Implementar:**
- [ ] [Componente específico da tarefa]
- [ ] [Integração necessária]
- [ ] [Configuração específica]

### **Integrações Necessárias:**
- **Com Sistema X:** Descrição da integração
- **Com Componente Y:** Descrição da integração

## ✅ Acceptance Criteria
- [ ] **AC1:** Critério específico e testável
- [ ] **AC2:** Critério específico e testável
- [ ] **AC3:** Critério específico e testável

## 🧪 Testes Requeridos

### **Testes Unitários:**
- [ ] Teste específico 1
- [ ] Teste específico 2
- [ ] Teste específico 3

### **Testes de Integração:**
- [ ] Teste de integração 1
- [ ] Teste de integração 2

## 🔗 Arquivos Afetados
- [ ] **Arquivo 1:** Descrição da alteração
- [ ] **Arquivo 2:** Descrição da alteração

## 📚 Documentação para IA

### **Contexto do Projeto:**
- **Stack:** Java 21 + Spring Boot 3.2 + PostgreSQL + Redis
- **Arquitetura:** Clean Architecture (Controller → Service → Repository)
- **Padrões:** Builder Pattern, Java Records para DTOs, Cache-First

### **Implementação Esperada:**
$objective - Seguir padrões estabelecidos no projeto.

### **Exemplos de Código Existente:**
- **Referência 1:** Caminho do arquivo de referência

## 🔍 Validação e Testes

### **Como Testar:**
1. Passo 1 para teste
2. Passo 2 para teste
3. Passo 3 para teste

### **Critérios de Sucesso:**
- [ ] Critério 1
- [ ] Critério 2
- [ ] Critério 3

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
*[Próxima tarefa na sequência]*

---

**Criado em:** Agosto 2025  
**Última Atualização:** Agosto 2025  
**Responsável:** AI-Driven Development  
**Reviewer:** [Nome do reviewer, se aplicável]
EOF

    echo "Criado: $filename"
}

# Criar todas as tarefas restantes da US02
create_task_file "14" "09-17_US02_Confirmacao_Email" "14_US02_Confirmacao_Email_Implementar_NewsletterService_SendConfirmation.md" "US02 - Confirmação de E-mail" "14_US02_Confirmacao_Email_Implementar_NewsletterService_SendConfirmation" "Média" "3 horas" "Tarefas 11, 12, 13" "Sprint 1" "Implementar método sendConfirmation no NewsletterService para integrar geração de token, envio de email e logging de eventos."

create_task_file "15" "09-17_US02_Confirmacao_Email" "15_US02_Confirmacao_Email_Criar_Endpoint_Confirm.md" "US02 - Confirmação de E-mail" "15_US02_Confirmacao_Email_Criar_Endpoint_Confirm" "Média" "3 horas" "Tarefas 09, 11" "Sprint 1" "Criar endpoint GET /api/newsletter/confirm para processar confirmações de email via token."

create_task_file "16" "09-17_US02_Confirmacao_Email" "16_US02_Confirmacao_Email_Implementar_Token_Validation_Expiration.md" "US02 - Confirmação de E-mail" "16_US02_Confirmacao_Email_Implementar_Token_Validation_Expiration" "Alta" "4 horas" "Tarefas 09, 11, 15" "Sprint 1" "Implementar lógica completa de validação de tokens, incluindo verificação de expiração, uso único e segurança."

create_task_file "17" "09-17_US02_Confirmacao_Email" "17_US02_Confirmacao_Email_Testes_Integracao_MailHog.md" "US02 - Confirmação de E-mail" "17_US02_Confirmacao_Email_Testes_Integracao_MailHog" "Alta" "4 horas" "Tarefas 12-16" "Sprint 1" "Implementar testes de integração completos com MailHog para validar todo o fluxo de confirmação de email."

echo "Todas as tarefas da US02 foram criadas!"