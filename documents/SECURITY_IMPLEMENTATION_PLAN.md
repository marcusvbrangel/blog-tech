# 🔐 **Plano de Implementação - Features de Segurança e Autenticação**

## 📊 **Visão Geral do Projeto**

### **Objetivo:** 
Implementar 12 features avançadas de segurança para transformar a Blog API em um sistema enterprise com autenticação robusta e controles de segurança de nível corporativo.

### **Impacto Esperado:**
- 🛡️ **Segurança**: Proteção contra ataques de força bruta, phishing e comprometimento de contas
- 📈 **Compliance**: Conformidade com regulamentações (LGPD, GDPR)
- 👥 **Experiência**: Melhor UX com recuperação de senha e sessões inteligentes
- 🔍 **Auditoria**: Rastreabilidade completa de ações críticas

---

## 🏗️ **Análise de Arquitetura Atual**

### **Estado Atual:**
- ✅ JWT Authentication básico implementado
- ✅ Role-based access (USER/AUTHOR/ADMIN)
- ✅ Password encryption com BCrypt
- ✅ Security configuration robusta
- ✅ Redis cache disponível para sessões

### **Gaps Identificados:**
- ❌ Sem verificação de email
- ❌ Sem proteção contra brute force
- ❌ Sem sistema de recuperação de senha
- ❌ Tokens não podem ser revogados
- ❌ Sem auditoria de ações
- ❌ Sem 2FA ou políticas de senha

---

## 📋 **Priorização e Roadmap**

### **🥇 FASE 1 - Fundamentos (2-3 semanas)**
**Prioridade ALTA - Base para outras features**

1. **Email Verification** - Base para comunicação
2. **Password Recovery** - Funcionalidade crítica
3. **Login Rate Limiting** - ✅ **IMPLEMENTADO** - Proteção imediata
4. **JWT Blacklist** - Controle de sessão

### **🥈 FASE 2 - Controle Avançado (2-3 semanas)**
**Prioridade MÉDIA - Melhorias de segurança**

5. **Refresh Tokens** - Sessões inteligentes
6. **Audit Logging** - Rastreabilidade
7. **Strong Password Policy** - Políticas de senha
8. **Terms Acceptance** - Compliance

### **🥉 FASE 3 - Features Premium (3-4 semanas)**
**Prioridade BAIXA - Diferenciadores**

9. **Two-Factor Authentication (2FA)** - Segurança máxima
10. **Advanced RBAC** - Controle granular
11. **Remote Logout** - Administração
12. **Suspicious Login Alerts** - Monitoramento

---

## 🗄️ **Mudanças no Banco de Dados**

### **Novas Tabelas Necessárias:**

```sql
-- 1. Tokens de verificação/recuperação
CREATE TABLE verification_tokens (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    token VARCHAR(255) NOT NULL UNIQUE,
    token_type VARCHAR(20) NOT NULL, -- EMAIL_VERIFICATION, PASSWORD_RESET
    expires_at TIMESTAMP NOT NULL,
    used_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 2. Controle de tentativas de login
CREATE TABLE login_attempts (
    id BIGSERIAL PRIMARY KEY,
    identifier VARCHAR(100) NOT NULL, -- email ou IP
    attempt_type VARCHAR(20) NOT NULL, -- EMAIL, IP
    attempts_count INTEGER DEFAULT 1,
    blocked_until TIMESTAMP,
    last_attempt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 3. JWT Blacklist
CREATE TABLE revoked_tokens (
    id BIGSERIAL PRIMARY KEY,
    token_jti VARCHAR(255) NOT NULL UNIQUE, -- JWT ID
    user_id BIGINT REFERENCES users(id),
    revoked_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP NOT NULL,
    reason VARCHAR(50) -- LOGOUT, ADMIN_REVOKE, PASSWORD_CHANGE
);

-- 4. Refresh Tokens
CREATE TABLE refresh_tokens (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    token VARCHAR(255) NOT NULL UNIQUE,
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_used TIMESTAMP,
    device_info TEXT,
    ip_address INET
);

-- 5. Auditoria
CREATE TABLE audit_logs (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(id),
    action VARCHAR(50) NOT NULL,
    resource_type VARCHAR(50),
    resource_id VARCHAR(100),
    details JSONB,
    ip_address INET,
    user_agent TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 6. 2FA
CREATE TABLE two_factor_auth (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) UNIQUE,
    secret VARCHAR(32) NOT NULL,
    enabled BOOLEAN DEFAULT FALSE,
    backup_codes TEXT[], -- códigos de recuperação
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_used TIMESTAMP
);

-- 7. Sessões ativas (para logout remoto)
CREATE TABLE active_sessions (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    session_token VARCHAR(255) NOT NULL UNIQUE,
    device_info TEXT,
    ip_address INET,
    location TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_activity TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 8. Aceite de termos
CREATE TABLE terms_acceptance (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    terms_version VARCHAR(10) NOT NULL,
    accepted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    ip_address INET
);
```

### **Alterações na Tabela Users:**

```sql
ALTER TABLE users ADD COLUMN email_verified BOOLEAN DEFAULT FALSE;
ALTER TABLE users ADD COLUMN email_verified_at TIMESTAMP;
ALTER TABLE users ADD COLUMN account_locked BOOLEAN DEFAULT FALSE;
ALTER TABLE users ADD COLUMN locked_until TIMESTAMP;
ALTER TABLE users ADD COLUMN password_changed_at TIMESTAMP;
ALTER TABLE users ADD COLUMN two_factor_enabled BOOLEAN DEFAULT FALSE;
ALTER TABLE users ADD COLUMN terms_accepted_version VARCHAR(10);
ALTER TABLE users ADD COLUMN last_login TIMESTAMP;
ALTER TABLE users ADD COLUMN failed_login_attempts INTEGER DEFAULT 0;
```

---

## 🔧 **Implementação Detalhada por Feature**

### **1. 📧 Email Verification**

**Entidades:**
```java
@Entity
public class VerificationToken {
    @Id @GeneratedValue
    private Long id;
    
    @ManyToOne
    private User user;
    
    private String token;
    
    @Enumerated(EnumType.STRING)
    private TokenType tokenType;
    
    private LocalDateTime expiresAt;
    private LocalDateTime usedAt;
    private LocalDateTime createdAt;
    
    public enum TokenType {
        EMAIL_VERIFICATION, PASSWORD_RESET, PHONE_VERIFICATION
    }
}
```

**Serviços:**
- `EmailService` - Integração com SMTP/SendGrid
- `VerificationTokenService` - Gestão de tokens
- Alteração no `AuthService` para gerar tokens

**Endpoints:**
- `POST /api/v1/auth/verify-email` - Confirmar email
- `POST /api/v1/auth/resend-verification` - Reenviar código

**Fluxo:**
1. Usuário se registra → Status: `email_verified = false`
2. Sistema gera token UUID/JWT e envia email
3. Usuário clica no link → Token validado → Conta ativada

---

### **2. 🔒 Login Rate Limiting** ✅ **IMPLEMENTADO**

**✅ Status:** Implementado e funcional desde versão 1.0

**Implementação Atual:**
- **Nível 1**: Por usuário (5 tentativas/15min) ✅ 
- **Storage**: Campos diretos na tabela `users` ✅
- **Auto-unlock**: Desbloqueio automático após expiração ✅

**Diferenças vs Plano Original:**
- ❌ **Nível 2**: Por IP (não implementado)
- ❌ **Nível 3**: Global (não implementado)  
- ❌ **Redis Cache**: Usa banco de dados direto
- ❌ **Configuração**: Hard-coded (não configurável)

**Localização:**
```java
// AuthService.java - linhas 230-245
private void incrementFailedLoginAttempts(User user) {
    int attempts = user.getFailedLoginAttempts() + 1;
    
    // Lock account after 5 failed attempts for 15 minutes
    if (attempts >= 5) {
        builder.accountLocked(true)
               .lockedUntil(LocalDateTime.now().plusMinutes(15));
    }
}
```

**Campos do Banco:**
- `failed_login_attempts` - Contador de tentativas
- `account_locked` - Flag de bloqueio
- `locked_until` - Timestamp de expiração

**Documentação Detalhada:** Veja `RATE_LIMITING_LOGIN_SYSTEM.md`

---

### **3. 🔑 Password Recovery**

**Fluxo Seguro:**
1. `POST /api/v1/auth/forgot-password` - Solicita reset
2. Sistema gera token seguro (6 dígitos + JWT)
3. Email enviado com token expirando em 15min
4. `POST /api/v1/auth/reset-password` - Nova senha + token
5. Token invalidado + logout de todas as sessões

**Segurança:**
- Token de uso único
- Expiração rápida (15 minutos)
- Rate limiting por email
- Invalidação de sessões ativas

---

### **4. ⚫ JWT Blacklist**

**Implementação:**
```java
@Component
public class JwtBlacklistService {
    
    @Cacheable("blacklisted_tokens")
    public boolean isTokenRevoked(String jti) {
        return revokedTokenRepository.existsByTokenJti(jti);
    }
    
    public void revokeToken(String jti, String reason) {
        // Adiciona ao blacklist no Redis + DB
    }
}
```

**Integração:**
- Modificar `JwtAuthenticationFilter` para verificar blacklist
- Revogar tokens em logout, mudança de senha, bloqueio admin
- Cleanup automático de tokens expirados

---

### **5. 🔄 Refresh Tokens**

**Arquitetura:**
- **Access Token**: JWT curto (15min)
- **Refresh Token**: UUID longo (30 dias)
- Armazenamento seguro no backend

**Endpoints:**
- `POST /api/v1/auth/refresh` - Renovar access token
- `POST /api/v1/auth/logout-all` - Invalidar todos os refresh tokens

**Segurança:**
- Rotação automática de refresh tokens
- Device fingerprinting
- Limite de tokens por usuário

---

### **6. 📝 Audit Logging**

**Eventos Auditados:**
- Login/Logout (sucesso/falha)
- Mudança de senha/email
- Alterações em posts/comentários críticos
- Ações administrativas
- Tentativas de acesso negado

**Implementação:**
```java
@Component
@EventListener
public class AuditEventListener {
    
    @Async
    public void onAuthenticationSuccess(AuthenticationSuccessEvent event) {
        auditService.logAction("LOGIN_SUCCESS", event.getAuthentication());
    }
    
    @Async
    public void onAccessDenied(AccessDeniedEvent event) {
        auditService.logAction("ACCESS_DENIED", event);
    }
}
```

**Features:**
- Log assíncrono para performance
- Retention policy configurável
- Export para sistemas SIEM
- Dashboard de auditoria

---

### **7. 🔐 Two-Factor Authentication (2FA)**

**Suporte:**
- **TOTP**: Google Authenticator, Authy
- **SMS**: Integração com Twilio/AWS SNS  
- **Email**: Código de 6 dígitos
- **Backup Codes**: Para recuperação

**Endpoints:**
- `POST /api/v1/auth/2fa/enable` - Ativar 2FA
- `POST /api/v1/auth/2fa/verify` - Verificar código
- `GET /api/v1/auth/2fa/backup-codes` - Gerar códigos backup

**Fluxo:**
1. Login normal → Sucesso
2. Se 2FA ativo → Solicita segundo fator
3. Token JWT parcial (sem permissões completas)
4. Após 2FA → Token completo

---

### **8. 🛡️ Strong Password Policy**

**Políticas:**
- Mínimo 8 caracteres
- Pelo menos 1 maiúscula, 1 minúscula, 1 número
- Símbolos obrigatórios
- Não pode ser senha comum (rockyou.txt)
- Não pode reutilizar últimas 5 senhas

**Implementação:**
```java
@Component
public class PasswordPolicyValidator {
    
    public ValidationResult validate(String password, User user) {
        // Múltiplas validações
        // Integração com HaveIBeenPwned API
        // Histórico de senhas
    }
}
```

---

### **9. 👑 Advanced RBAC**

**Evolução:**
```java
@Entity
public class Permission {
    private String name; // "posts:read", "users:admin"
    private String resource;
    private String action;
    private String scope; // "own", "all", "department"
}

@Entity 
public class Role {
    private String name;
    
    @ManyToMany
    private Set<Permission> permissions;
}
```

**Features:**
- Permissions granulares
- Role hierarchy
- Context-aware permissions
- Dynamic role assignment

---

### **10. 📋 Terms Acceptance**

**Implementação:**
- Versionamento de termos
- Tracking de aceite por usuário
- Force re-acceptance em updates
- Compliance audit trail

---

### **11. 🚪 Remote Logout**

**Features:**
- Admins podem forçar logout
- Usuário pode ver sessões ativas
- Logout seletivo por device
- Emergency logout (todas as sessões)

---

### **12. 🚨 Suspicious Login Alerts**

**Detecção:**
- Geolocalização incomum
- Device fingerprinting
- Horário atípico
- Múltiplos IPs simultâneos

**Ações:**
- Email de alerta
- SMS opcional
- Bloqueio temporário
- Força 2FA

---

## 📊 **Configurações e Integrações**

### **application.yml additions:**

```yaml
blog:
  security:
    email-verification:
      enabled: true
      token-expiration: 24h
    rate-limiting:
      login-attempts:
        max-per-email: 5
        max-per-ip: 15
        block-duration: 15m
    password-policy:
      min-length: 8
      require-special-chars: true
      check-breached: true
    jwt:
      access-token-expiration: 15m
      refresh-token-expiration: 30d
    two-factor:
      issuer: "Blog API"
      backup-codes-count: 10
      
  integrations:
    email:
      provider: smtp # smtp, sendgrid, ses
      from: "noreply@blogapi.com"
    sms:
      provider: twilio
      from: "+1234567890"
```

### **Dependências Maven:**

```xml
<!-- Email -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-mail</artifactId>
</dependency>

<!-- 2FA -->
<dependency>
    <groupId>com.warrenstrange</groupId>
    <artifactId>googleauth</artifactId>
    <version>1.5.0</version>
</dependency>

<!-- GeoIP -->
<dependency>
    <groupId>com.maxmind.geoip2</groupId>
    <artifactId>geoip2</artifactId>
    <version>3.0.1</version>
</dependency>

<!-- Rate Limiting -->
<dependency>
    <groupId>com.github.vladimir-bukhtoyarov</groupId>
    <artifactId>bucket4j-core</artifactId>
    <version>7.6.0</version>
</dependency>
```

---

## 🧪 **Estratégia de Testes**

### **Testes de Segurança:**
- **Penetration Testing**: Simulação de ataques
- **Rate Limiting**: Verificar bloqueios funcionam
- **Token Security**: Validar expiração e revogação
- **2FA Bypass**: Tentar contornar autenticação
- **SQL Injection**: Testes em novos endpoints

### **Testes de Performance:**
- **Load Testing**: Múltiplos logins simultâneos
- **Cache Performance**: Redis sob carga
- **Email Delivery**: Volume de emails
- **Database**: Queries de auditoria

### **Testes de Compliance:**
- **LGPD**: Direito ao esquecimento
- **GDPR**: Portabilidade de dados
- **Audit Trail**: Completude dos logs

---

## 📈 **Métricas e Monitoramento**

### **Novas Métricas Prometheus:**
```yaml
# Security Metrics
blog_login_attempts_total{status, method}
blog_tokens_revoked_total{reason}
blog_2fa_attempts_total{status}
blog_password_resets_total
blog_suspicious_logins_total
blog_active_sessions_gauge
```

### **Dashboards Grafana:**
- **Security Overview**: Tentativas de login, tokens revogados
- **User Activity**: Logins por hora, geolocalização
- **2FA Statistics**: Adoção, tentativas
- **Audit Summary**: Ações por tipo, usuários mais ativos

### **Alertas:**
- Rate limiting ativado > 10x/minuto
- Múltiplas tentativas de força bruta
- Tokens revogados em massa
- Falhas de 2FA consecutivas

---

## ⏱️ **Timeline de Implementação**

### **Semanas 1-2: Infraestrutura**
- Setup de email service
- Criação das tabelas
- Configuração do Redis para rate limiting

### **Semanas 3-4: Fase 1 Features**
- Email verification
- Password recovery  
- Login rate limiting
- JWT blacklist

### **Semanas 5-6: Fase 2 Features**
- Refresh tokens
- Audit logging
- Password policies
- Terms acceptance

### **Semanas 7-9: Fase 3 Features**
- Two-factor authentication
- Advanced RBAC
- Remote logout
- Suspicious login detection

### **Semanas 10-11: Integração e Testes**
- Testes de segurança
- Performance testing
- Documentation
- Deployment

---

## 🎯 **Critérios de Sucesso**

### **Funcional:**
- ✅ 100% dos endpoints protegidos funcionando
- ✅ Email delivery < 30 segundos
- ✅ Rate limiting ativo em < 100ms
- ✅ 2FA setup em < 2 minutos

### **Segurança:**
- ✅ Zero vulnerabilidades críticas (OWASP Top 10)
- ✅ Penetration test aprovado
- ✅ Compliance audit passed

### **Performance:**  
- ✅ Login com 2FA < 2 segundos
- ✅ Rate limiting não impacta usuários legítimos
- ✅ Audit logs não degradam API performance

### **Usabilidade:**
- ✅ Password reset < 5 cliques
- ✅ 2FA setup intuitivo
- ✅ Clear error messages

---

## 🚀 **Próximos Passos**

1. **Fase 1 - Setup Infraestrutura:**
   - Configurar email service (SMTP)
   - Executar scripts de criação de tabelas
   - Configurar Redis para rate limiting
   - Adicionar dependências Maven

2. **Fase 1 - Implementação Core:**
   - Email verification system
   - Password recovery flow
   - Login rate limiting
   - JWT blacklist mechanism

3. **Testes e Validação:**
   - Unit tests para cada feature
   - Integration tests de fluxos completos
   - Security testing
   - Performance benchmarks

4. **Documentação:**
   - API documentation updates
   - Security best practices guide
   - Deployment procedures
   - Monitoring setup

---

**Status:** 📋 Plano criado - Pronto para implementação
**Última atualização:** 2025-01-30
**Responsável:** Equipe de Desenvolvimento

---

*Este documento será atualizado conforme o progresso da implementação das features de segurança.*