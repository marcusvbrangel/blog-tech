#!/usr/bin/env python3
"""
Script para criar todos os arquivos de documentação de tarefas da Newsletter
baseado no template e índice de tarefas.
"""

import os
from datetime import datetime

# Template base para os arquivos
TEMPLATE = """# {filename}

## 📋 Contexto da Tarefa
- **User Story:** {user_story}
- **Número da Tarefa:** {task_number}/95
- **Complexidade:** {complexity}
- **Estimativa:** {estimate}
- **Dependências:** {dependencies}
- **Sprint:** {sprint}

## 🎯 Objetivo
{objective}

## 📝 Especificação Técnica

### **Componentes a Implementar:**
{components}

### **Integrações Necessárias:**
{integrations}

## ✅ Acceptance Criteria
{acceptance_criteria}

## 🧪 Testes Requeridos

### **Testes Unitários:**
{unit_tests}

### **Testes de Integração:**
{integration_tests}

## 🔗 Arquivos Afetados
{affected_files}

## 📚 Documentação para IA

### **Contexto do Projeto:**
- **Stack:** Java 21 + Spring Boot 3.2 + PostgreSQL + Redis
- **Arquitetura:** Clean Architecture (Controller → Service → Repository)
- **Padrões:** Builder Pattern, Java Records para DTOs, Cache-First

### **Implementação Esperada:**
{implementation_expected}

### **Exemplos de Código Existente:**
{code_examples}

## 🔍 Validação e Testes

### **Como Testar:**
{how_to_test}

### **Critérios de Sucesso:**
{success_criteria}

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
- **Estimativa:** {estimate}
- **Real:** ___ horas *(a ser preenchido após implementação)*

### **Complexidade:**
- **Estimada:** {complexity}
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
*{next_steps}*

---

**Criado em:** Agosto 2025  
**Última Atualização:** Agosto 2025  
**Responsável:** AI-Driven Development  
**Reviewer:** [Nome do reviewer, se aplicável]"""

# Definição das tarefas baseada no índice
TASKS = {
    # US02 - Confirmação de Email (09-17)
    10: {
        "folder": "09-17_US02_Confirmacao_Email",
        "filename": "10_US02_Confirmacao_Email_Implementar_TokenService.md",
        "user_story": "US02 - Confirmação de E-mail",
        "task_number": "10",
        "complexity": "Média",
        "estimate": "1 hora",
        "dependencies": "Tarefa 09 (NewsletterToken)",
        "sprint": "Sprint 1",
        "objective": "Implementar o NewsletterTokenService para geração, validação e limpeza de tokens de confirmação, unsubscribe e solicitação de dados.",
        "components": "- [ ] NewsletterTokenService com @Service\n- [ ] Métodos de geração de tokens por tipo\n- [ ] Validação de tokens (validade, uso)\n- [ ] Limpeza automática de tokens expirados\n- [ ] Integração com scheduler",
        "integrations": "- **Com Repository:** NewsletterTokenRepository\n- **Com Scheduler:** Limpeza automática\n- **Com UUID:** Geração de tokens únicos",
        "acceptance_criteria": "- [ ] **AC1:** Métodos para gerar tokens de diferentes tipos\n- [ ] **AC2:** Validação de tokens com verificação de expiração\n- [ ] **AC3:** Marcação de tokens como usados\n- [ ] **AC4:** Job de limpeza de tokens expirados\n- [ ] **AC5:** Diferentes TTLs por tipo de token",
        "unit_tests": "- [ ] Teste de geração de token\n- [ ] Teste de validação de token válido/inválido\n- [ ] Teste de token expirado\n- [ ] Teste de limpeza automática",
        "integration_tests": "- [ ] Teste de persistência de tokens\n- [ ] Teste de job de limpeza",
        "affected_files": "- [ ] **src/main/java/com/blog/api/service/NewsletterTokenService.java:** Novo service\n- [ ] **src/test/java/com/blog/api/service/NewsletterTokenServiceTest.java:** Testes",
        "implementation_expected": "Criar service dedicado para gerenciamento de tokens. Utilizar diferentes TTLs por tipo: confirmação (48h), unsubscribe (1 ano), dados (24h).",
        "code_examples": "- **Referência 1:** `/src/main/java/com/blog/api/service/VerificationTokenService.java`",
        "how_to_test": "1. Gerar tokens de diferentes tipos\n2. Validar expiração\n3. Testar limpeza automática\n4. Verificar marcação como usado",
        "success_criteria": "- [ ] Tokens gerados corretamente\n- [ ] Validação funcionando\n- [ ] Limpeza automática ativa",
        "next_steps": "Tarefa 11: Criar template HTML de confirmação"
    },
    # Adicionar mais tarefas conforme necessário...
}

def create_task_file(task_info):
    """Cria um arquivo de documentação de tarefa"""
    folder_path = f"/home/base/Documentos/desenvolvimento/claude-estudo/first-project/documents/newsletter/tasks/{task_info['folder']}"
    file_path = os.path.join(folder_path, task_info['filename'])
    
    # Criar pasta se não existir
    os.makedirs(folder_path, exist_ok=True)
    
    # Preencher template
    content = TEMPLATE.format(**task_info)
    
    # Escrever arquivo
    with open(file_path, 'w', encoding='utf-8') as f:
        f.write(content)
    
    print(f"Criado: {task_info['filename']}")

def main():
    """Função principal"""
    print("Criando arquivos de documentação de tarefas...")
    
    for task_id, task_info in TASKS.items():
        create_task_file(task_info)
    
    print(f"Total de {len(TASKS)} arquivos criados!")

if __name__ == "__main__":
    main()