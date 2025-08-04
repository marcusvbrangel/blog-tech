# 31_US04_Administracao_Criar_Controller_List_Subscribers.md

## 📋 Contexto da Tarefa
- **User Story:** US04 - Administração
- **Número da Tarefa:** 31/96
- **Complexidade:** Média | **Estimativa:** 3h | **Sprint:** 2

## 🎯 Objetivo
Criar endpoint administrativo para listagem paginada de subscribers com filtros.

## 📝 Componentes
- [ ] Endpoint GET /admin/newsletter/subscribers
- [ ] Parâmetros de paginação e filtros
- [ ] Autorização ADMIN
- [ ] Response paginado
- [ ] Swagger documentation

## ✅ Acceptance Criteria
- [ ] Endpoint com paginação funcionando  
- [ ] Autorização ADMIN implementada
- [ ] Filtros por status e data
- [ ] Swagger documentado

## 📚 Implementação
```java
@RestController
@RequestMapping("/admin/newsletter")
@PreAuthorize("hasRole('ADMIN')")
public class AdminNewsletterController {
    
    @GetMapping("/subscribers")
    @Operation(summary = "List subscribers (Admin only)")
    public ResponseEntity<Page<AdminSubscriberResponse>> listSubscribers(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size,
        @RequestParam(defaultValue = "createdAt") String sortBy,
        @RequestParam(defaultValue = "desc") String sortDir,
        @RequestParam(required = false) SubscriptionStatus status,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate
    ) {
        // Implementation
    }
}
```

## ✅ Definition of Done
- [ ] Controller implementado
- [ ] Autorização funcionando
- [ ] Paginação implementada
- [ ] Swagger atualizado

---
**Responsável:** AI-Driven Development