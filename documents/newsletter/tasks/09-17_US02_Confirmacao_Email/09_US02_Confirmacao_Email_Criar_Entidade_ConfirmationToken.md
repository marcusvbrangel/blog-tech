# 09_US02_Confirmacao_Email_Criar_Entidade_ConfirmationToken.md

## 📋 Contexto da Tarefa
- **User Story:** US02 - Confirmação de Email
- **Número da Tarefa:** 09/96
- **Complexidade:** Média
- **Estimativa:** 3 horas
- **Dependências:** Tarefa 01 (NewsletterSubscriber entity)
- **Sprint:** Sprint 1

## 🎯 Objetivo
Criar a entidade JPA `ConfirmationToken` para gerenciar tokens de confirmação de email, incluindo relacionamento com NewsletterSubscriber, controle de expiração e status de validação.

## 📝 Especificação Técnica

### **Componentes a Implementar:**
- [ ] Entidade JPA ConfirmationToken
- [ ] Enum TokenStatus (PENDING, CONFIRMED, EXPIRED)
- [ ] Relacionamento @ManyToOne com NewsletterSubscriber
- [ ] Campos de controle de expiração
- [ ] UUID para token único
- [ ] Campos de auditoria automáticos

### **Integrações Necessárias:**
- **Com NewsletterSubscriber:** Relacionamento JPA @ManyToOne
- **Com Spring Data JPA:** Anotações de persistência
- **Com Java UUID:** Geração de tokens únicos

## ✅ Acceptance Criteria
- [ ] **AC1:** Entidade ConfirmationToken criada com todos os campos necessários
- [ ] **AC2:** Relacionamento com NewsletterSubscriber funcionando
- [ ] **AC3:** Token UUID gerado automaticamente
- [ ] **AC4:** Controle de expiração implementado (24h default)
- [ ] **AC5:** Enum TokenStatus com valores apropriados
- [ ] **AC6:** Campos de auditoria funcionando

## 🧪 Testes Requeridos

### **Testes Unitários:**
- [ ] Teste de criação de token com UUID único
- [ ] Teste de relacionamento com NewsletterSubscriber
- [ ] Teste de cálculo de expiração
- [ ] Teste de enum TokenStatus
- [ ] Teste de campos obrigatórios

### **Testes de Integração:**
- [ ] Teste de persistência com relacionamento
- [ ] Teste de constraint unique no token
- [ ] Teste de cascade operations

## 🔗 Arquivos Afetados
- [ ] **src/main/java/com/blog/api/entity/ConfirmationToken.java:** Nova entidade
- [ ] **src/main/java/com/blog/api/entity/TokenStatus.java:** Novo enum
- [ ] **src/test/java/com/blog/api/entity/ConfirmationTokenTest.java:** Testes unitários

## 📚 Documentação para IA

### **Contexto do Projeto:**
- **Stack:** Java 17 + Spring Boot 3.2 + PostgreSQL + Redis
- **Arquitetura:** Clean Architecture (Controller → Service → Repository)
- **Padrões:** Builder Pattern, Java Records para DTOs, Cache-First

### **Convenções de Código:**
```java
// Padrão para entidades com relacionamento
@Entity
@Table(name = "confirmation_tokens")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ConfirmationToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String token;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subscriber_id", nullable = false)
    private NewsletterSubscriber subscriber;
    
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "expires_at")
    private LocalDateTime expiresAt;
}
```

### **Implementação Esperada:**
- Usar UUID.randomUUID().toString() para gerar token
- Implementar método isExpired() para verificação
- Configurar expiração default de 24 horas
- Usar enum para status do token

### **Exemplos de Código Existente:**
- **Referência 1:** src/main/java/com/blog/api/entity/NewsletterSubscriber.java (padrão de entidade)
- **Referência 2:** src/main/java/com/blog/api/entity/User.java (relacionamentos)

## ⚙️ Configuration & Setup

### **Database Changes:**
```sql
-- Migration será criada em tarefa posterior
-- Definir estrutura JPA por enquanto
```

### **Dependencies:**
```xml
<!-- Dependências já existentes -->
<!-- spring-boot-starter-data-jpa -->
<!-- lombok -->
```

## 🔍 Validação e Testes

### **Como Testar:**
1. Criar ConfirmationToken com Builder
2. Verificar geração automática de UUID
3. Testar relacionamento com NewsletterSubscriber
4. Verificar cálculo de expiração
5. Testar enum TokenStatus

### **Critérios de Sucesso:**
- [ ] Token UUID gerado automaticamente
- [ ] Relacionamento JPA funcionando
- [ ] Expiração calculada corretamente
- [ ] Enum values corretos
- [ ] Campos obrigatórios validando

### **Comandos de Teste:**
```bash
# Testes unitários específicos
mvn test -Dtest="ConfirmationTokenTest"

# Compilação
mvn compile
```

## ✅ Definition of Done

### **Código:**
- [ ] Entidade ConfirmationToken implementada
- [ ] Enum TokenStatus implementado
- [ ] Relacionamento JPA configurado
- [ ] UUID generation funcionando
- [ ] Controle de expiração implementado

### **Testes:**
- [ ] Testes unitários passando
- [ ] Testes de relacionamento passando
- [ ] Cobertura ≥ 85%

### **Documentação:**
- [ ] Javadoc para classe e métodos principais
- [ ] Comentários sobre expiração de token

### **Quality Gates:**
- [ ] Compilação sem warnings
- [ ] Relacionamentos JPA funcionando
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
- Tarefa 10: Implementar TokenService
- Tarefa 11: Criar Template HTML Confirmação

---

**Criado em:** Agosto 2025  
**Última Atualização:** Agosto 2025  
**Responsável:** AI-Driven Development