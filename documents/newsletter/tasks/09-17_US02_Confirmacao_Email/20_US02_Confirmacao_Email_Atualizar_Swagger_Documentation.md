# 20_US02_Confirmacao_Email_Atualizar_Swagger_Documentation.md

## 📋 Contexto da Tarefa
- **User Story:** US02 - Confirmação de Email
- **Número da Tarefa:** 20/96
- **Complexidade:** Baixa
- **Estimativa:** 2 horas
- **Dependências:** Tarefa 19 (Testar MailHog)
- **Sprint:** Sprint 1

## 🎯 Objetivo
Atualizar documentação Swagger/OpenAPI com novos endpoints de confirmação de email, incluindo exemplos de request/response e códigos de erro.

## 📝 Especificação Técnica
### **Componentes a Implementar:**
- [ ] Documentação endpoint /newsletter/confirm/{token}
- [ ] Exemplos de responses de sucesso e erro
- [ ] Descrições detalhadas dos parâmetros
- [ ] Códigos de status HTTP documentados
- [ ] Schemas dos DTOs atualizados

## ✅ Acceptance Criteria
- [ ] **AC1:** Endpoint documentado no Swagger
- [ ] **AC2:** Exemplos de request/response incluídos
- [ ] **AC3:** Códigos de erro documentados
- [ ] **AC4:** Schemas dos DTOs atualizados
- [ ] **AC5:** Documentação acessível via /swagger-ui

## 📚 Implementação Esperada
```java
@Operation(
    summary = "Confirm newsletter subscription",
    description = "Confirms a newsletter subscription using the token sent via email"
)
@ApiResponses(value = {
    @ApiResponse(
        responseCode = "200", 
        description = "Subscription confirmed successfully",
        content = @Content(schema = @Schema(implementation = ConfirmationResponse.class))
    ),
    @ApiResponse(
        responseCode = "400", 
        description = "Invalid or expired token",
        content = @Content(schema = @Schema(implementation = ErrorResponse.class))
    ),
    @ApiResponse(
        responseCode = "404", 
        description = "Token not found"
    )
})
@GetMapping("/confirm/{token}")
public ResponseEntity<ConfirmationResponse> confirmSubscription(
    @Parameter(description = "Confirmation token received via email", required = true)
    @PathVariable String token
) {
    // Implementation
}
```

## ✅ Definition of Done
- [ ] Swagger documentation atualizada
- [ ] Exemplos incluídos
- [ ] Schemas atualizados
- [ ] Documentação acessível
- [ ] Review da documentação completa

---
**Criado em:** Agosto 2025  
**Responsável:** AI-Driven Development