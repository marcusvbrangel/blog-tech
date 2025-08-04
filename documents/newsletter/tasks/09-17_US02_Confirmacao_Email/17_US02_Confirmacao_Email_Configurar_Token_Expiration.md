# 17_US02_Confirmacao_Email_Configurar_Token_Expiration.md

## 📋 Contexto da Tarefa
- **User Story:** US02 - Confirmação de Email
- **Número da Tarefa:** 17/96
- **Complexidade:** Baixa
- **Estimativa:** 2 horas
- **Dependências:** Tarefa 16 (Atualizar Status)
- **Sprint:** Sprint 1

## 🎯 Objetivo
Configurar sistema de expiração de tokens configurável via properties, incluindo limpeza automática e notificação de expiração.

## 📝 Especificação Técnica
### **Componentes a Implementar:**
- [ ] Configuração de expiração via properties
- [ ] Cálculo dinâmico de expiração
- [ ] Job scheduled para limpeza
- [ ] Métrica de tokens expirados
- [ ] Logs de limpeza

## ✅ Acceptance Criteria
- [ ] **AC1:** Expiração configurável (default 24h)
- [ ] **AC2:** Limpeza automática de tokens expirados
- [ ] **AC3:** Métricas de expiração
- [ ] **AC4:** Logs de auditoria

## 📚 Implementação Esperada
```yaml
newsletter:
  token:
    expiration-hours: 24
    cleanup-cron: "0 0 2 * * ?"
```

```java
@Scheduled(cron = "${newsletter.token.cleanup-cron}")
public void cleanupExpiredTokens() {
    int deleted = tokenRepository.deleteExpiredTokens(LocalDateTime.now());
    log.info("Cleaned up {} expired tokens", deleted);
}
```

## ✅ Definition of Done
- [ ] Configuração implementada
- [ ] Limpeza automática funcionando
- [ ] Métricas coletadas
- [ ] Logs implementados

---
**Criado em:** Agosto 2025  
**Responsável:** AI-Driven Development