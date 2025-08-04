# 02_US01_Inscricao_Usuarios_Criar_DTO_NewsletterSubscriptionRequest.md

## 📋 Contexto da Tarefa
- **User Story:** US01 - Inscrição de Usuários
- **Número da Tarefa:** 02/95
- **Complexidade:** Baixa
- **Estimativa:** 1 hora
- **Dependências:** Nenhuma (pode ser feito em paralelo com 01)
- **Sprint:** Sprint 1

## 🎯 Objetivo
Criar o DTO (Data Transfer Object) `NewsletterSubscriptionRequest` como Java Record para receber dados de inscrição na newsletter via API REST, incluindo validações e campos necessários para compliance LGPD.

## 📝 Especificação Técnica

### **Componentes a Implementar:**
- [ ] Java Record NewsletterSubscriptionRequest
- [ ] Validações Bean Validation (@NotBlank, @Email, etc.)
- [ ] Campos para consentimento LGPD
- [ ] Documentação OpenAPI (@Schema)
- [ ] Campos para captura de IP e User-Agent


### **Integrações Necessárias:**
- **Com Bean Validation:** Para validações automáticas
- **Com OpenAPI:** Para documentação Swagger
- **Com Controller:** Para receber dados do frontend

## ✅ Acceptance Criteria
- [ ] **AC1:** Java Record criado seguindo padrão do projeto
- [ ] **AC2:** Validações implementadas para todos os campos obrigatórios
- [ ] **AC3:** Documentação OpenAPI implementada
- [ ] **AC4:** Factory methods implementados para facilitar uso
- [ ] **AC5:** Campos LGPD (consentimento, policy version) incluídos
- [ ] **AC6:** Campos de auditoria (IP, User-Agent) incluídos

## 🧪 Testes Requeridos

### **Testes Unitários:**
- [ ] Teste de criação do record
- [ ] Teste de validação de email inválido
- [ ] Teste de validação de campos obrigatórios null/blank
- [ ] Teste do factory method create()
- [ ] Teste do method withClientInfo()
- [ ] Teste de validação de consentimento null

### **Testes de Integração:**
- [ ] Teste de deserialização JSON
- [ ] Teste de validação via Jackson
- [ ] Teste de documentação OpenAPI gerada

## 🔗 Arquivos Afetados
- [ ] **src/main/java/com/blog/api/dto/NewsletterSubscriptionRequest.java:** Novo DTO
- [ ] **src/test/java/com/blog/api/dto/NewsletterSubscriptionRequestTest.java:** Testes unitários

## 📚 Documentação para IA

### **Contexto do Projeto:**
- **Stack:** Java 17 + Spring Boot 3.2 + Bean Validation + OpenAPI
- **Padrões:** Java Records para DTOs (modernização do projeto)
- **Validações:** Bean Validation com mensagens em português

### **Convenções de Código:**
```java
// Padrão seguido no projeto - exemplo CreateUserDTO convertido para Record
public record CreateUserDTO(
    @NotBlank(message = "Username é obrigatório")
    @Size(min = 3, max = 50, message = "Username deve ter entre 3 e 50 caracteres")
    String username,
    
    @NotBlank(message = "Email é obrigatório")
    @Email(message = "Email deve ter formato válido")
    String email
) {}
```

### **Implementação Esperada:**
- Seguir padrão de Java Records já adotado no projeto
- Usar Bean Validation com mensagens em português
- Implementar documentação OpenAPI completa
- Incluir factory methods para facilitar uso nos testes

### **Exemplos de Código Existente:**
- **Referência 1:** src/main/java/com/blog/api/dto/CreateUserDTO.java (padrão de Record)
- **Referência 2:** src/main/java/com/blog/api/dto/LoginRequest.java (validações)

## ⚙️ Configuration & Setup

### **Dependencies:**
```xml
<!-- Dependências já existem no projeto -->
<!-- spring-boot-starter-validation -->
<!-- springdoc-openapi-starter-webmvc-ui -->
```

## 🔍 Validação e Testes

### **Como Testar:**
1. Criar instância do record com dados válidos
2. Testar validações com dados inválidos
3. Verificar messages de erro em português
4. Testar factory methods
5. Verificar serialização/deserialização JSON

### **Critérios de Sucesso:**
- [ ] Record compila sem erros
- [ ] Validações funcionam corretamente
- [ ] Factory methods funcionam
- [ ] Documentação OpenAPI gerada
- [ ] Serialização JSON funciona

### **Comandos de Teste:**
```bash
# Testes unitários específicos
mvn test -Dtest="NewsletterSubscriptionRequestTest"

# Teste de compilação
mvn compile
```

## ✅ Definition of Done

### **Código:**
- [ ] Java Record NewsletterSubscriptionRequest implementado
- [ ] Todas as validações Bean Validation aplicadas
- [ ] Factory methods implementados
- [ ] Documentação OpenAPI completa

### **Testes:**
- [ ] Testes unitários passando
- [ ] Testes de validação passando
- [ ] Cobertura ≥ 85% para o DTO

### **Documentação:**
- [ ] Javadoc para record e methods
- [ ] Documentação OpenAPI (@Schema)

### **Quality Gates:**
- [ ] Compilação sem warnings
- [ ] Validações funcionando
- [ ] Padrões do projeto seguidos

## 📊 Métricas

### **Estimativa vs Real:**
- **Estimativa:** 1 hora
- **Real:** ___ horas *(a ser preenchido após implementação)*

### **Complexidade:**
- **Estimada:** Baixa
- **Real:** _____ *(a ser preenchido após implementação)*

## 📝 Notas de Implementação
*[Este espaço será preenchido durante a implementação]*

## 📊 Status Tracking

### **Status Atual:**
- [x] 📋 **Todo** - Não iniciada
- [ ] 🔄 **In Progress** - Em desenvolvimento  
- [ ] 👀 **Code Review** - Aguardando revisão
- [ ] ✅ **Done** - Concluída e validada

### **Next Steps:**
- Tarefa 05: Criar NewsletterController.subscribe() (usará este DTO)
- Tarefa 06: Configurar validações no controller

---

**Criado em:** Agosto 2025  
**Última Atualização:** Agosto 2025  
**Responsável:** AI-Driven Development