# 03_US01_Inscricao_Usuarios_Implementar_NewsletterRepository.md

## 📋 Contexto da Tarefa
- **User Story:** US01 - Inscrição de Usuários
- **Número da Tarefa:** 03/95
- **Complexidade:** Baixa
- **Estimativa:** 2 horas
- **Dependências:** Tarefa 01 (NewsletterSubscriber entity)
- **Sprint:** Sprint 1

## 🎯 Objetivo
Criar o repository interface `NewsletterSubscriberRepository` usando Spring Data JPA com queries customizadas necessárias para gerenciar inscrições da newsletter, incluindo métodos para busca, filtros e operações LGPD.

## 📝 Especificação Técnica

### **Componentes a Implementar:**
- [ ] Interface NewsletterSubscriberRepository
- [ ] Queries customizadas com @Query
- [ ] Métodos de busca por email e status
- [ ] Métodos para filtros administrativos
- [ ] Métodos para compliance LGPD (soft delete)
- [ ] Métodos para métricas e relatórios


### **Integrações Necessárias:**
- **Com Spring Data JPA:** Para operações de banco automáticas
- **Com NewsletterSubscriber:** Entity principal
- **Com Pagination:** Para APIs administrativas

## ✅ Acceptance Criteria
- [ ] **AC1:** Repository interface criado estendendo JpaRepository
- [ ] **AC2:** Método findByEmail implementado para busca por email único
- [ ] **AC3:** Método findByStatus para filtrar por status de inscrição
- [ ] **AC4:** Query customizada com filtros para admin implementada
- [ ] **AC5:** Métodos LGPD (soft delete) implementados
- [ ] **AC6:** Métodos para métricas e relatórios implementados

## 🧪 Testes Requeridos

### **Testes Unitários:**
- [ ] Teste findByEmail - encontrar subscriber existente
- [ ] Teste findByEmail - não encontrar subscriber inexistente
- [ ] Teste findByStatus - filtrar por CONFIRMED
- [ ] Teste findByStatus - filtrar por PENDING
- [ ] Teste existsByEmail - email existente retorna true
- [ ] Teste existsByEmail - email inexistente retorna false

### **Testes de Integração:**
- [ ] Teste findWithFilters - todos os filtros
- [ ] Teste findWithFilters - apenas status
- [ ] Teste findWithFilters - apenas data range
- [ ] Teste markAsDeleted - soft delete funcionando
- [ ] Teste countConfirmedSince - contagem correta
- [ ] Teste paginação funcionando

## 🔗 Arquivos Afetados
- [ ] **src/main/java/com/blog/api/repository/NewsletterSubscriberRepository.java:** Novo repository
- [ ] **src/test/java/com/blog/api/repository/NewsletterSubscriberRepositoryTest.java:** Testes unitários

## 📚 Documentação para IA

### **Contexto do Projeto:**
- **Stack:** Spring Data JPA + PostgreSQL + H2 (testes)
- **Padrões:** Repository pattern com JpaRepository
- **Queries:** @Query para custom queries complexas

### **Convenções de Código:**
```java
// Padrão seguido no projeto - exemplo UserRepository
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    
    @Query("SELECT u FROM User u WHERE u.role = :role")
    List<User> findByRole(@Param("role") UserRole role);
}
```

### **Implementação Esperada:**
- Seguir padrão dos outros repositories do projeto
- Usar @Query para queries complexas
- Implementar paginação corretamente
- Incluir métodos para LGPD compliance

### **Exemplos de Código Existente:**
- **Referência 1:** src/main/java/com/blog/api/repository/UserRepository.java
- **Referência 2:** src/main/java/com/blog/api/repository/PostRepository.java (queries complexas)

## ⚙️ Configuration & Setup

### **Database Changes:**
```sql
-- Indexes serão criados em migration posterior (tarefa 79)
-- Por enquanto, apenas definir queries JPA
```

### **Dependencies:**
```xml
<!-- Dependências já existem no projeto -->
<!-- spring-boot-starter-data-jpa -->
```

## 🔍 Validação e Testes

### **Como Testar:**
1. Criar subscribers de teste no banco H2
2. Testar cada método do repository
3. Verificar se queries customizadas funcionam
4. Testar paginação e filtros
5. Verificar performance das queries

### **Critérios de Sucesso:**
- [ ] Todos os métodos funcionam corretamente
- [ ] Queries customizadas retornam dados corretos
- [ ] Paginação funciona
- [ ] Soft delete funciona
- [ ] Não há N+1 query problems

### **Comandos de Teste:**
```bash
# Testes unitários específicos
mvn test -Dtest="NewsletterSubscriberRepositoryTest"

# Testes de integração JPA
mvn test -Dtest="*Repository*Test"
```

## ✅ Definition of Done

### **Código:**
- [ ] Interface NewsletterSubscriberRepository implementada
- [ ] Todos os métodos necessários criados
- [ ] Queries customizadas funcionando
- [ ] @Repository annotation aplicada

### **Testes:**
- [ ] Testes unitários passando
- [ ] Testes de integração JPA passando
- [ ] Cobertura ≥ 85% para métodos testáveis

### **Documentação:**
- [ ] Javadoc para métodos customizados
- [ ] Comentários sobre queries complexas

### **Quality Gates:**
- [ ] Compilação sem warnings
- [ ] Queries executando corretamente
- [ ] Performance aceitável

## 📊 Métricas

### **Estimativa vs Real:**
- **Estimativa:** 2 horas
- **Real:** ___ horas *(a ser preenchido após implementação)*

### **Complexidade:**
- **Estimada:** Baixa
- **Real:** _____ *(a ser preenchido após implementação)*

## 📝 Notas de Implementação
*[Este espaço será preenchido durante a implementação]*

### **Queries Performance:**
*[Notas sobre performance das queries customizadas]*

### **Indexing Strategy:**
*[Estratégia de índices para otimização]*

## 📊 Status Tracking

### **Status Atual:**
- [x] 📋 **Todo** - Não iniciada
- [ ] 🔄 **In Progress** - Em desenvolvimento  
- [ ] 👀 **Code Review** - Aguardando revisão
- [ ] ✅ **Done** - Concluída e validada

### **Next Steps:**
- Tarefa 04: Implementar NewsletterService.subscribe() (usará este repository)
- Tarefa 24: Criar endpoint admin (usará queries de filtro)

---

**Criado em:** Agosto 2025  
**Última Atualização:** Agosto 2025  
**Responsável:** AI-Driven Development