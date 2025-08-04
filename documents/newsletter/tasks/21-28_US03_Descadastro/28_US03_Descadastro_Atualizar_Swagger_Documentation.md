# 28_US03_Descadastro_Atualizar_Swagger_Documentation.md

## 📋 Contexto da Tarefa
- **User Story:** US03 - Descadastro
- **Número da Tarefa:** 28/96
- **Complexidade:** Baixa
- **Estimativa:** 2 horas
- **Dependências:** Tarefa 27 (Testes E2E)
- **Sprint:** Sprint 1

## 🎯 Objetivo
Atualizar documentação Swagger com endpoint de descadastro, incluindo exemplos e códigos de erro.

## 📝 Componentes a Implementar
- [ ] Documentação endpoint /newsletter/unsubscribe/{token}
- [ ] Exemplos de responses
- [ ] Códigos de status HTTP
- [ ] Schemas dos DTOs
- [ ] Descrições detalhadas

## ✅ Acceptance Criteria
- [ ] **AC1:** Endpoint documentado no Swagger
- [ ] **AC2:** Exemplos incluídos
- [ ] **AC3:** Códigos de erro documentados
- [ ] **AC4:** Schemas atualizados
- [ ] **AC5:** Documentação acessível

## 📚 Implementação Esperada
```java
@Operation(
    summary = "Unsubscribe from newsletter",
    description = "Unsubscribes user from newsletter using secure token"
)
@ApiResponses(value = {
    @ApiResponse(
        responseCode = "200", 
        description = "Successfully unsubscribed",
        content = @Content(schema = @Schema(implementation = UnsubscribeResponse.class))
    ),
    @ApiResponse(
        responseCode = "400", 
        description = "Invalid unsubscribe token"
    ),
    @ApiResponse(
        responseCode = "410", 
        description = "Token expired or already used"
    )
})
@GetMapping("/unsubscribe/{token}")
public ResponseEntity<UnsubscribeResponse> unsubscribe(
    @Parameter(description = "Secure unsubscribe token", required = true)
    @PathVariable String token
) {
    // Implementation
}
```

## ✅ Definition of Done
- [ ] Swagger documentation atualizada
- [ ] Exemplos incluídos  
- [ ] Códigos de erro documentados
- [ ] Schemas dos DTOs atualizados
- [ ] Review da documentação completa

---
**Criado em:** Agosto 2025  
**Responsável:** AI-Driven Development