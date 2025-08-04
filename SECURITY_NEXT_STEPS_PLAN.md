# 🚀 Plano de Próximos Passos - Features de Segurança

## 📊 **Situação Atual**

Baseado na análise completa documentada em `SECURITY_FEATURES_STATUS_REPORT.md`:

- **Fase 1 (Fundamentos):** ✅ **100% COMPLETA** 
- **Fase 2 (Controle Avançado):** 🔄 **50% COMPLETA**
- **Fase 3 (Features Premium):** ❌ **0% COMPLETA**

---

## 🎯 **Gaps Identificados e Prioridades**

### **📈 ALTA PRIORIDADE - Completar Fase 2**

#### 1. 🔄 **Completar Refresh Tokens**
**Status:** Parcialmente implementado (apenas `canTokenBeRefreshed()`)

**Faltando:**
- [ ] **RefreshToken entity** com Builder pattern
- [ ] **RefreshTokenRepository** com queries otimizadas  
- [ ] **RefreshTokenService** com rotação automática
- [ ] **Endpoint `/auth/refresh`** no AuthController
- [ ] **Device fingerprinting** para segurança
- [ ] **Limite de tokens por usuário**
- [ ] **Migração SQL** para tabela refresh_tokens
- [ ] **Testes completos** (unit + integration)

**Estimativa:** 3-4 dias de desenvolvimento

#### 2. 📝 **Implementar Audit Logging Formal**
**Status:** Não implementado (apenas logging básico)

**Faltando:**
- [ ] **AuditLog entity** com campos estruturados
- [ ] **AuditLogRepository** com queries de relatório
- [ ] **AuditService** com logging assíncrono
- [ ] **Event Listeners** para eventos de segurança
- [ ] **Migração SQL** para tabela audit_logs
- [ ] **Endpoint para consulta** de auditoria
- [ ] **Dashboard** básico de auditoria
- [ ] **Testes completos**

**Estimativa:** 4-5 dias de desenvolvimento

### **🔧 MÉDIA PRIORIDADE - Correções e Melhorias**

#### 3. 🧪 **Corrigir Testes das Features Implementadas**
**Problema:** Política de senha impedindo execução de testes

**Necessário:**
- [ ] **Atualizar senhas de teste** para atender política forte
- [ ] **Factory methods** para dados de teste válidos
- [ ] **Configuração de test profile** com políticas relaxadas
- [ ] **Validar funcionamento** de todas as features Fase 2

**Estimativa:** 1-2 dias

#### 4. ⚡ **Otimizações de Performance**
**Melhorias identificadas:**
- [ ] **Cache para rate limiting** (Redis em vez de DB direto)
- [ ] **Índices otimizados** para consultas de auditoria
- [ ] **Batch processing** para cleanup automático
- [ ] **Connection pooling** para operações críticas

**Estimativa:** 2-3 dias

### **🚀 BAIXA PRIORIDADE - Features Premium**

#### 5. 🔐 **Two-Factor Authentication (2FA)**
**Complexidade:** Alta - Sistema completo

**Componentes:**
- [ ] **TwoFactorAuth entity** para secrets
- [ ] **TOTP integration** com Google Authenticator
- [ ] **Backup codes** para recuperação
- [ ] **SMS integration** (opcional)
- [ ] **2FA enforcement** por role
- [ ] **Setup wizard** para usuários

**Estimativa:** 7-10 dias

---

## 📋 **Roadmap Recomendado**

### **🗓️ Sprint 1 (Semana 1-2): Completar Fase 2**

#### **Semana 1: Refresh Tokens**
- **Dia 1-2:** Implementar entidades e repositório
- **Dia 3-4:** Desenvolver service e endpoints
- **Dia 5:** Testes e documentação

#### **Semana 2: Audit Logging**
- **Dia 1-2:** Implementar estrutura de auditoria
- **Dia 3-4:** Event listeners e integração
- **Dia 5:** Dashboard básico e testes

### **🗓️ Sprint 2 (Semana 3): Consolidação**

#### **Semana 3: Correções e Otimizações**
- **Dia 1-2:** Corrigir testes existentes
- **Dia 3-4:** Otimizações de performance
- **Dia 5:** Documentação completa e review

### **🗓️ Sprint 3+ (Semana 4+): Features Premium**

#### **Opção A:** Implementar 2FA completo
#### **Opção B:** Advanced RBAC
#### **Opção C:** Suspicious Login Detection

---

## 🛠️ **Detalhamento Técnico das Próximas Implementações**

### **1. Refresh Tokens - Implementação Completa**

#### **Entidade RefreshToken:**
```java
@Entity
@Table(name = "refresh_tokens")
public class RefreshToken {
    @Id @GeneratedValue
    private Long id;
    
    @Column(nullable = false)
    private Long userId;
    
    @Column(unique = true, nullable = false)
    private String token;
    
    @Column(nullable = false)
    private LocalDateTime expiresAt;
    
    private LocalDateTime lastUsed;
    private String deviceInfo;
    private String ipAddress;
    
    // Builder pattern + métodos de negócio
}
```

#### **Service Layer:**
```java
@Service
public class RefreshTokenService {
    
    @Transactional
    public RefreshTokenResponse refreshAccessToken(String refreshToken);
    
    @Transactional  
    public void revokeRefreshToken(String token);
    
    @Transactional
    public void revokeAllUserTokens(Long userId);
    
    @Scheduled
    public void cleanupExpiredTokens();
}
```

#### **Endpoint:**
```java
@PostMapping("/refresh")
public ResponseEntity<JwtResponse> refreshToken(
    @RequestBody RefreshTokenRequest request) {
    // Implementação
}
```

### **2. Audit Logging - Sistema Formal**

#### **Entidade AuditLog:**
```java
@Entity
@Table(name = "audit_logs")
public class AuditLog {
    @Id @GeneratedValue
    private Long id;
    
    private Long userId;
    private String action;      // LOGIN_SUCCESS, TOKEN_REVOKED, etc.
    private String resourceType; // USER, POST, TOKEN
    private String resourceId;
    
    @Type(JsonType.class)
    private Map<String, Object> details;
    
    private String ipAddress;
    private String userAgent;
    private LocalDateTime createdAt;
}
```

#### **Event System:**
```java
@Component
@EventListener
public class SecurityAuditListener {
    
    @Async
    @EventListener
    public void onAuthenticationSuccess(AuthenticationSuccessEvent event) {
        auditService.logSecurityEvent("LOGIN_SUCCESS", event);
    }
    
    @Async
    @EventListener  
    public void onTokenRevoked(TokenRevokedEvent event) {
        auditService.logSecurityEvent("TOKEN_REVOKED", event);
    }
}
```

---

## 🎯 **Critérios de Sucesso**

### **Fase 2 Completa:**
- [ ] **Refresh tokens** funcionais com rotação automática
- [ ] **Audit logging** capturando todos eventos críticos
- [ ] **Testes passando** com 95%+ cobertura
- [ ] **Performance** mantida (< 50ms adicional por request)
- [ ] **Documentação** completa e atualizada

### **Métricas de Qualidade:**
- [ ] **Zero vulnerabilidades** críticas (OWASP scan)
- [ ] **Load testing** suportando 1000+ req/min
- [ ] **Audit trail** completo para compliance
- [ ] **Monitoring** funcionando com alertas

---

## 🤔 **Decisão Necessária**

### **Qual próximo passo você prefere?**

1. **🔄 Implementar Refresh Tokens** - Completar funcionalidade parcial
2. **📝 Implementar Audit Logging** - Sistema formal de auditoria  
3. **🧪 Corrigir Testes** - Garantir qualidade das features existentes
4. **🚀 Partir para 2FA** - Pular para features premium

### **Recomendação:**
**Começar com Refresh Tokens** - está 30% implementado, seria rápido completar e daria base sólida para Audit Logging.

---

## 📚 **Recursos Necessários**

### **Dependências Maven:**
```xml
<!-- Para Device Fingerprinting -->
<dependency>
    <groupId>com.github.ua-parser</groupId>
    <artifactId>uap-java</artifactId>
    <version>1.5.2</version>
</dependency>

<!-- Para IP Geolocation (se necessário) -->
<dependency>
    <groupId>com.maxmind.geoip2</groupId>
    <artifactId>geoip2</artifactId>
    <version>3.0.1</version>
</dependency>
```

### **Configurações:**
```yaml
blog:
  security:
    refresh-tokens:
      expiration: 30d
      max-per-user: 5
      rotation-enabled: true
    audit:
      enabled: true
      retention-days: 90
      async-processing: true
```

---

**📝 Documento criado:** 2025-01-30  
**🎯 Próxima ação:** Aguardando decisão sobre prioridade de implementação  
**📊 Status:** Plano detalhado pronto para execução