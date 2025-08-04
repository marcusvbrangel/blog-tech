# 36_US04_Administracao_Implementar_Testes_Autorizacao.md

## 📋 Contexto da Tarefa
- **User Story:** US04 - Administração
- **Número da Tarefa:** 36/96  
- **Complexidade:** Média | **Estimativa:** 3h | **Sprint:** 2

## 🎯 Objetivo
Implementar testes de autorização para endpoints administrativos com diferentes roles.

## 📝 Componentes
- [ ] Testes com @WithMockUser
- [ ] Cenários ADMIN vs USER
- [ ] Testes de JWT
- [ ] Testes de rate limiting
- [ ] Security integration tests

## 📚 Implementação
```java
@WebMvcTest(AdminNewsletterController.class)
class AdminNewsletterControllerSecurityTest {
    
    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldAllowAdminAccess() {
        // Test admin access
    }
    
    @Test
    @WithMockUser(roles = "USER")
    void shouldDenyUserAccess() {
        // Test access denied
    }
    
    @Test
    void shouldRequireAuthentication() {
        // Test unauthenticated access
    }
}
```

## ✅ Definition of Done
- [ ] Testes de autorização implementados
- [ ] Cenários ADMIN/USER testados
- [ ] JWT tests implementados
- [ ] Cobertura ≥ 95%

---
**Responsável:** AI-Driven Development