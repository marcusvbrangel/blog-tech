# 21_US03_Descadastro_Criar_UnsubscribeToken_Security.md

## 📋 Contexto da Tarefa
- **User Story:** US03 - Descadastro de Usuários
- **Número da Tarefa:** 21/96
- **Complexidade:** Média
- **Estimativa:** 3 horas
- **Dependências:** Tarefa 09 (ConfirmationToken entity)
- **Sprint:** Sprint 1

## 🎯 Objetivo
Implementar sistema seguro de tokens para descadastro, incluindo assinatura criptográfica e validação de integridade para prevenir ataques.

## 📝 Especificação Técnica
### **Componentes a Implementar:**
- [ ] Entidade UnsubscribeToken com assinatura
- [ ] Geração de token com HMAC-SHA256
- [ ] Validação de integridade do token
- [ ] Expiração configurável (default 30 dias)
- [ ] Proteção contra tampering
- [ ] UnsubscribeTokenService

## ✅ Acceptance Criteria
- [ ] **AC1:** Token seguro com assinatura HMAC
- [ ] **AC2:** Validação de integridade funcionando
- [ ] **AC3:** Expiração configurável
- [ ] **AC4:** Proteção contra tampering
- [ ] **AC5:** Logs de segurança

## 📚 Implementação Esperada
```java
@Service
public class UnsubscribeTokenService {
    
    @Value("${newsletter.unsubscribe.secret-key}")
    private String secretKey;
    
    public String generateUnsubscribeToken(NewsletterSubscriber subscriber) {
        String payload = subscriber.getId() + ":" + subscriber.getEmail() + ":" + System.currentTimeMillis();
        String signature = generateHMAC(payload, secretKey);
        
        return Base64.getEncoder().encodeToString((payload + ":" + signature).getBytes());
    }
    
    public boolean validateToken(String token, Long subscriberId) {
        // Validate HMAC signature and expiration
    }
}
```

## ✅ Definition of Done
- [ ] UnsubscribeToken entity criada
- [ ] HMAC signature implementado
- [ ] Validação de integridade funcionando
- [ ] Testes de segurança passando
- [ ] Documentação de segurança criada

---
**Criado em:** Agosto 2025  
**Responsável:** AI-Driven Development