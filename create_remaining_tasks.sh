#!/bin/bash

# Script para criar TODAS as tarefas restantes (19-95)
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
- [ ] Teste de integrações

### **Testes de Integração:**
- [ ] Teste end-to-end
- [ ] Teste de performance
- [ ] Teste de segurança

## 🔗 Arquivos Afetados
- [ ] **Arquivo principal:** Implementação da funcionalidade
- [ ] **Arquivo de teste:** Testes unitários e integração
- [ ] **Arquivo de configuração:** Configurações necessárias

## 📚 Documentação para IA

### **Contexto do Projeto:**
- **Stack:** Java 21 + Spring Boot 3.2 + PostgreSQL + Redis
- **Arquitetura:** Clean Architecture (Controller → Service → Repository)
- **Padrões:** Builder Pattern, Java Records para DTOs, Cache-First

### **Implementação Esperada:**
$objective - Seguir rigorosamente os padrões estabelecidos no projeto.

### **Exemplos de Código Existente:**
- **Referência 1:** Código similar no projeto
- **Referência 2:** Padrões a seguir

## 🔍 Validação e Testes

### **Como Testar:**
1. Executar implementação
2. Validar funcionalidade
3. Verificar integrações
4. Confirmar performance

### **Critérios de Sucesso:**
- [ ] Funcionalidade implementada
- [ ] Testes passando
- [ ] Performance adequada
- [ ] Documentação completa

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

# US03 - Descadastro (Tarefas 19-23)
create_task "19" "18-23_US03_Descadastro" "US03_Descadastro_Implementar_NewsletterService_Unsubscribe" "US03 - Descadastro" "Média" "3 horas" "Tarefas 01, 18" "Sprint 1" "Implementar método unsubscribe no NewsletterService para processar descadastros via token de segurança."

create_task "20" "18-23_US03_Descadastro" "US03_Descadastro_Criar_Endpoint_Unsubscribe" "US03 - Descadastro" "Baixa" "2 horas" "Tarefa 19" "Sprint 1" "Criar endpoint GET /api/newsletter/unsubscribe para processar descadastros via link."

create_task "21" "18-23_US03_Descadastro" "US03_Descadastro_Atualizar_Status_Unsubscribed" "US03 - Descadastro" "Baixa" "1 hora" "Tarefas 01, 19" "Sprint 1" "Implementar atualização de status para UNSUBSCRIBED na base de dados."

create_task "22" "18-23_US03_Descadastro" "US03_Descadastro_Implementar_Event_Logging" "US03 - Descadastro" "Baixa" "2 horas" "Tarefas 19, 21" "Sprint 1" "Implementar logging de eventos de descadastro para auditoria e compliance."

create_task "23" "18-23_US03_Descadastro" "US03_Descadastro_Testes_End_to_End_Fluxo_Completo" "US03 - Descadastro" "Média" "3 horas" "Tarefas 01-22" "Sprint 1" "Implementar testes end-to-end do fluxo completo de descadastro."

# US04 - Administração (Tarefas 24-29)
create_task "24" "24-29_US04_Administracao" "US04_Administracao_Criar_Endpoint_List_Subscribers" "US04 - Administração" "Média" "3 horas" "Tarefas 01, 03" "Sprint 2" "Criar endpoint GET /api/newsletter/subscribers para listagem administrativa."

create_task "25" "24-29_US04_Administracao" "US04_Administracao_Implementar_Paginacao_Filtros" "US04 - Administração" "Média" "4 horas" "Tarefa 24" "Sprint 2" "Implementar paginação e filtros por status e data no endpoint administrativo."

create_task "26" "24-29_US04_Administracao" "US04_Administracao_Configurar_Spring_Security_Admin" "US04 - Administração" "Baixa" "2 horas" "Tarefa 24" "Sprint 2" "Configurar Spring Security para proteger endpoints administrativos com ROLE_ADMIN."

create_task "27" "24-29_US04_Administracao" "US04_Administracao_Criar_DTO_AdminSubscriberResponse" "US04 - Administração" "Baixa" "1 hora" "Tarefa 01" "Sprint 2" "Criar DTO AdminSubscriberResponse sem expor dados sensíveis."

create_task "28" "24-29_US04_Administracao" "US04_Administracao_Implementar_Filtros_Status_Data" "US04 - Administração" "Média" "3 horas" "Tarefas 25, 27" "Sprint 2" "Implementar filtros avançados por status e range de datas."

create_task "29" "24-29_US04_Administracao" "US04_Administracao_Testes_Autorizacao_Paginacao" "US04 - Administração" "Média" "3 horas" "Tarefas 24-28" "Sprint 2" "Implementar testes de autorização e paginação para endpoints administrativos."

echo "US03 e US04 criadas!"

# US05 - Envio Automático (Tarefas 31-38)
create_task "31" "30-38_US05_Envio_Automatico" "US05_Envio_Automatico_Implementar_NewsletterEventListener" "US05 - Envio Automático" "Alta" "4 horas" "Tarefa 30" "Sprint 2" "Implementar NewsletterEventListener assíncrono para processar eventos de publicação."

create_task "32" "30-38_US05_Envio_Automatico" "US05_Envio_Automatico_Criar_Template_HTML_NewPosts" "US05 - Envio Automático" "Baixa" "3 horas" "Tarefa 13" "Sprint 2" "Criar template HTML para notificação de novos posts."

create_task "33" "30-38_US05_Envio_Automatico" "US05_Envio_Automatico_Implementar_SendNewPostNotification" "US05 - Envio Automático" "Alta" "5 horas" "Tarefas 01, 31, 32" "Sprint 2" "Implementar método sendNewPostNotification no NewsletterService."

create_task "34" "30-38_US05_Envio_Automatico" "US05_Envio_Automatico_Integrar_PostService_Disparar_Evento" "US05 - Envio Automático" "Média" "3 horas" "Tarefas 30, 31" "Sprint 2" "Integrar com PostService para disparar eventos de publicação."

create_task "35" "30-38_US05_Envio_Automatico" "US05_Envio_Automatico_Consultar_Subscribers_Confirmed" "US05 - Envio Automático" "Baixa" "2 horas" "Tarefas 01, 33" "Sprint 2" "Implementar consulta eficiente apenas para subscribers CONFIRMED."

create_task "36" "30-38_US05_Envio_Automatico" "US05_Envio_Automatico_Implementar_Rate_Limiting" "US05 - Envio Automático" "Alta" "4 horas" "Tarefa 33" "Sprint 2" "Implementar rate limiting para envios em massa de emails."

create_task "37" "30-38_US05_Envio_Automatico" "US05_Envio_Automatico_Configurar_Async_Processing" "US05 - Envio Automático" "Média" "3 horas" "Tarefas 31, 33" "Sprint 2" "Configurar processamento assíncrono com @Async para performance."

create_task "38" "30-38_US05_Envio_Automatico" "US05_Envio_Automatico_Testes_Integracao_Eventos" "US05 - Envio Automático" "Alta" "5 horas" "Tarefas 30-37" "Sprint 2" "Implementar testes de integração completos com eventos."

echo "US05 criada!"