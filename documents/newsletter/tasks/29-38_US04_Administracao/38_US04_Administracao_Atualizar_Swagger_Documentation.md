# 38_US04_Administracao_Atualizar_Swagger_Documentation.md

## 📋 Contexto da Tarefa
- **User Story:** US04 - Administração
- **Número da Tarefa:** 38/96
- **Complexidade:** Baixa | **Estimativa:** 2h | **Sprint:** 2

## 🎯 Objetivo
Atualizar documentação Swagger para endpoints administrativos com exemplos e autorização.

## 📝 Componentes
- [ ] Documentação endpoints /admin/**
- [ ] Security requirements (JWT)
- [ ] Exemplos de requests/responses
- [ ] Rate limiting documentation
- [ ] Admin-specific schemas

## 📚 Implementação
```java
@SecurityRequirement(name = "bearer-jwt")
@Tag(name = "Admin - Newsletter", description = "Administrative operations for newsletter management")
@RestController
@RequestMapping("/admin/newsletter")
public class AdminNewsletterController {
    
    @Operation(
        summary = "List subscribers with filters (Admin only)",
        description = "Returns paginated list of newsletter subscribers with filtering options"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Subscribers retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Authentication required"),
        @ApiResponse(responseCode = "403", description = "Admin role required"),
        @ApiResponse(responseCode = "429", description = "Rate limit exceeded")
    })
    @RateLimit(requests = 100, perMinutes = 60)
    @GetMapping("/subscribers")
    public ResponseEntity<Page<AdminSubscriberResponse>> listSubscribers(
        // Parameters with @Parameter annotations
    ) {
        // Implementation
    }
}
```

## ✅ Definition of Done
- [ ] Swagger documentation completa
- [ ] Security requirements configurados
- [ ] Exemplos incluídos
- [ ] Rate limiting documentado

---
**Responsável:** AI-Driven Development