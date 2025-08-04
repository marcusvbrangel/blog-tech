# 01_US01_Inscricao_Usuarios_Criar_Entidade_NewsletterSubscriber.md

## 📋 Contexto da Tarefa
- **User Story:** US01 - Inscrição de Usuários
- **Número da Tarefa:** 01/95
- **Complexidade:** Média
- **Estimativa:** 3 horas
- **Dependências:** Nenhuma (tarefa base)
- **Sprint:** Sprint 1

## 🎯 Objetivo
Criar a entidade JPA `NewsletterSubscriber` que será a base do sistema de newsletter, incluindo todos os campos necessários para LGPD compliance, auditoria e gestão de status de inscrição.

## 📝 Especificação Técnica

### **Componentes a Implementar:**
- [ ] Entidade JPA NewsletterSubscriber
- [ ] Enum SubscriptionStatus
- [ ] Campos de auditoria (@CreationTimestamp, @UpdateTimestamp)
- [ ] Campos de consentimento LGPD
- [ ] Validações de negócio
- [ ] Builder pattern seguindo padrão do projeto


### **Integrações Necessárias:**
- **Com Spring Data JPA:** Anotações JPA para persistência
- **Com validation:** @Email, @NotNull para validações
- **Com Lombok:** Builder pattern + getters/setters

## ✅ Acceptance Criteria
- [ ] **AC1:** Entidade criada com todos os campos especificados
- [ ] **AC2:** Enum SubscriptionStatus com todos os valores necessários
- [ ] **AC3:** Constraints de banco aplicadas (unique email, not null)
- [ ] **AC4:** Builder pattern implementado seguindo padrão do projeto
- [ ] **AC5:** Campos de auditoria automáticos funcionando
- [ ] **AC6:** Campos LGPD implementados conforme compliance

## 🧪 Testes Requeridos

### **Testes Unitários:**
- [ ] Teste de criação da entidade com Builder
- [ ] Teste de validação de email
- [ ] Teste de constraints de campos obrigatórios
- [ ] Teste de enum SubscriptionStatus
- [ ] Teste de campos de auditoria automáticos

### **Testes de Integração:**
- [ ] Teste de persistência no banco H2 (test)
- [ ] Teste de constraint unique no email
- [ ] Teste de relacionamentos JPA (se houver)

## 🔗 Arquivos Afetados
- [ ] **src/main/java/com/blog/api/entity/NewsletterSubscriber.java:** Nova entidade
- [ ] **src/main/java/com/blog/api/entity/SubscriptionStatus.java:** Novo enum
- [ ] **src/test/java/com/blog/api/entity/NewsletterSubscriberTest.java:** Testes unitários

## 📚 Documentação para IA

### **Contexto do Projeto:**
- **Stack:** Java 17 + Spring Boot 3.2 + PostgreSQL + Redis
- **Arquitetura:** Clean Architecture (Controller → Service → Repository)
- **Padrões:** Builder Pattern, Java Records para DTOs, Cache-First

### **Convenções de Código:**
```java
// Padrões seguidos no projeto - exemplo da entidade User
@Entity
@Table(name = "users")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
```

### **Implementação Esperada:**
- Seguir exatamente o padrão das outras entidades do projeto
- Usar Lombok annotations para reduzir boilerplate
- Implementar todos os campos necessários para LGPD
- Garantir que constraint unique funcione corretamente

### **Exemplos de Código Existente:**
- **Referência 1:** src/main/java/com/blog/api/entity/User.java (padrão de entidade)
- **Referência 2:** src/main/java/com/blog/api/entity/Post.java (builder pattern)

## ⚙️ Configuration & Setup

### **Database Changes:**
```sql
-- Migration será criada em tarefa posterior (78)
-- Por enquanto, apenas definir a estrutura JPA
```

### **Dependencies:**
```xml
<!-- Dependências já existem no projeto -->
<!-- spring-boot-starter-data-jpa -->
<!-- lombok -->
<!-- spring-boot-starter-validation -->
```

## 🔍 Validação e Testes

### **Como Testar:**
1. Criar instância com Builder pattern
2. Verificar se campos obrigatórios funcionam
3. Testar validação de email
4. Verificar timestamps automáticos
5. Testar todos os enum values

### **Critérios de Sucesso:**
- [ ] Entidade compila sem erros
- [ ] Builder pattern funciona corretamente
- [ ] Validações de campo funcionam
- [ ] Enum values estão corretos
- [ ] Timestamps são preenchidos automaticamente

### **Comandos de Teste:**
```bash
# Testes unitários específicos
mvn test -Dtest="NewsletterSubscriberTest"

# Compilação
mvn compile
```

## ✅ Definition of Done

### **Código:**
- [ ] Entidade NewsletterSubscriber implementada
- [ ] Enum SubscriptionStatus implementado
- [ ] Builder pattern funcionando
- [ ] Todas as anotações JPA aplicadas
- [ ] Campos LGPD implementados

### **Testes:**
- [ ] Testes unitários da entidade passando
- [ ] Teste de Builder pattern passando
- [ ] Cobertura ≥ 85% para a entidade

### **Documentação:**
- [ ] Javadoc para classe e campos principais
- [ ] Comentários sobre campos LGPD

### **Quality Gates:**
- [ ] Compilação sem warnings
- [ ] Validações funcionando corretamente
- [ ] Padrões do projeto seguidos

## 📊 Métricas

### **Estimativa vs Real:**
- **Estimativa:** 3 horas
- **Real:** ___ horas *(a ser preenchido após implementação)*

### **Complexidade:**
- **Estimada:** Média
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
- Tarefa 02: Criar DTO NewsletterSubscriptionRequest
- Tarefa 03: Implementar NewsletterRepository

---

**Criado em:** Agosto 2025  
**Última Atualização:** Agosto 2025  
**Responsável:** AI-Driven Development