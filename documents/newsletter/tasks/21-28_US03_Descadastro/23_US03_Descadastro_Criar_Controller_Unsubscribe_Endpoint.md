# 23_US03_Descadastro_Criar_Controller_Unsubscribe_Endpoint.md

## 📋 Contexto da Tarefa
- **User Story:** US03 - Descadastro
- **Número da Tarefa:** 23/96
- **Complexidade:** Baixa
- **Estimativa:** 2 horas
- **Dependências:** Tarefa 22 (NewsletterService Unsubscribe)
- **Sprint:** Sprint 1

## 🎯 Objetivo
Criar endpoint REST para descadastro via token, incluindo página de confirmação e response adequada.

## 📝 Componentes a Implementar
- [ ] Endpoint GET /newsletter/unsubscribe/{token}
- [ ] Validação de token
- [ ] Página de confirmação de descadastro
- [ ] Response DTOs
- [ ] Error handling
- [ ] Swagger documentation

## ✅ Acceptance Criteria
- [ ] **AC1:** Endpoint GET funcionando
- [ ] **AC2:** Token válido confirma descadastro
- [ ] **AC3:** Página de confirmação exibida
- [ ] **AC4:** Error handling para tokens inválidos
- [ ] **AC5:** Swagger documentation completa

## 📚 Implementação Esperada
```java
@GetMapping("/unsubscribe/{token}")
@Operation(summary = "Unsubscribe from newsletter")
public ResponseEntity<UnsubscribeResponse> unsubscribe(
    @PathVariable String token) {
    
    try {
        var result = newsletterService.unsubscribe(token);
        return ResponseEntity.ok(result);
    } catch (InvalidUnsubscribeTokenException e) {
        return ResponseEntity.badRequest()
            .body(UnsubscribeResponse.error("Invalid unsubscribe link"));
    }
}
```

## ✅ Definition of Done
- [ ] Endpoint implementado
- [ ] Validação funcionando
- [ ] Error handling implementado
- [ ] Swagger atualizado
- [ ] Testes passando

---
**Criado em:** Agosto 2025  
**Responsável:** AI-Driven Development