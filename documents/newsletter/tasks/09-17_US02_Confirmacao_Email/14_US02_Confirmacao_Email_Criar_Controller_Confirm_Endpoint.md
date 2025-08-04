# 14_US02_Confirmacao_Email_Criar_Controller_Confirm_Endpoint.md

## 📋 Contexto da Tarefa
- **User Story:** US02 - Confirmação de Email  
- **Número da Tarefa:** 14/96
- **Complexidade:** Média
- **Estimativa:** 3 horas
- **Dependências:** Tarefa 13 (NewsletterService SendConfirmation)
- **Sprint:** Sprint 1

## 🎯 Objetivo
Criar endpoint REST para confirmação de inscrição via token, incluindo validação de token, atualização de status e resposta adequada ao usuário.

## 📝 Especificação Técnica

### **Componentes a Implementar:**
- [ ] Endpoint GET /newsletter/confirm/{token}
- [ ] Validação de token via TokenService
- [ ] Atualização de status para CONFIRMED
- [ ] Response DTO para confirmação
- [ ] Error handling para tokens inválidos/expirados
- [ ] Swagger documentation

### **Integrações Necessárias:**
- **Com TokenService:** Validação de tokens
- **Com NewsletterService:** Confirmação de inscrição  
- **Com Swagger:** Documentação da API

## ✅ Acceptance Criteria
- [ ] **AC1:** Endpoint GET /newsletter/confirm/{token} funcionando
- [ ] **AC2:** Token válido confirma inscrição com sucesso
- [ ] **AC3:** Token inválido/expirado retorna erro 400
- [ ] **AC4:** Response adequada em ambos os casos
- [ ] **AC5:** Swagger documentation completa
- [ ] **AC6:** Logs de auditoria registrados

## 🧪 Testes Requeridos

### **Testes Unitários:**
- [ ] Teste de confirmação com token válido
- [ ] Teste com token inválido
- [ ] Teste com token expirado
- [ ] Teste de response DTOs
- [ ] Teste de error handling

### **Testes de Integração:**
- [ ] Teste end-to-end do fluxo completo
- [ ] Teste de integração com base de dados
- [ ] Teste de performance do endpoint

## 🔗 Arquivos Afetados
- [ ] **src/main/java/com/blog/api/controller/NewsletterController.java:** Novo endpoint
- [ ] **src/main/java/com/blog/api/dto/ConfirmationResponse.java:** Response DTO
- [ ] **src/test/java/com/blog/api/controller/NewsletterControllerTest.java:** Testes

## 📚 Documentação para IA

### **Implementação Esperada:**
```java
@RestController
@RequestMapping("/newsletter")
@Slf4j
@RequiredArgsConstructor
public class NewsletterController {
    
    private final NewsletterService newsletterService;
    private final TokenService tokenService;
    
    @GetMapping("/confirm/{token}")
    @Operation(summary = "Confirm newsletter subscription")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Subscription confirmed successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid or expired token"),
        @ApiResponse(responseCode = "404", description = "Token not found")
    })
    public ResponseEntity<ConfirmationResponse> confirmSubscription(
            @PathVariable String token) {
        
        log.info("Confirming subscription with token: {}", token);
        
        try {
            var result = newsletterService.confirmSubscription(token);
            return ResponseEntity.ok(result);
        } catch (InvalidTokenException e) {
            return ResponseEntity.badRequest()
                .body(ConfirmationResponse.error("Invalid or expired token"));
        }
    }
}
```

## ⚙️ Configuration & Setup

### **Swagger Configuration:**
```java
// Adicionar tags appropriadas
@Tag(name = "Newsletter", description = "Newsletter subscription management")
```

## 🔍 Validação e Testes

### **Como Testar:**
1. Obter token de confirmação válido
2. Fazer GET para /newsletter/confirm/{token}
3. Verificar response 200 e status confirmed
4. Testar com token inválido/expirado
5. Verificar response 400 apropriada

### **Comandos de Teste:**
```bash
# Testes do controller
mvn test -Dtest="NewsletterControllerTest#testConfirmSubscription"

# Teste manual
curl -X GET "localhost:8080/newsletter/confirm/{token}"
```

## ✅ Definition of Done
- [ ] Endpoint implementado e funcionando
- [ ] Validação de token implementada
- [ ] Error handling adequado
- [ ] Swagger documentation atualizada
- [ ] Testes unitários e integração passando

---

**Criado em:** Agosto 2025  
**Responsável:** AI-Driven Development