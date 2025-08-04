# 🌐 Blog API - Documentação dos Endpoints

Este documento detalha todos os endpoints da API, incluindo os novos endpoints de segurança implementados.

## 📋 Visão Geral

A Blog API possui endpoints organizados por funcionalidade:
- **Autenticação** - Login, registro, 2FA, refresh tokens
- **Usuários** - Gestão de perfis e preferências  
- **Posts** - CRUD de posts do blog
- **Comentários** - Sistema de comentários
- **Categorias** - Organização de conteúdo

---

## 🔐 Autenticação

### Base URL: `/api/v1/auth`

#### 1. Registro de Usuário
```http
POST /api/v1/auth/register
Content-Type: application/json

{
    "username": "johndoe",
    "email": "john@example.com", 
    "password": "SecurePass123!",
    "role": "USER"
}
```

**Response 201 Created:**
```json
{
    "id": 1,
    "username": "johndoe",
    "email": "john@example.com",
    "role": "USER",
    "createdAt": "2025-01-15T10:30:00",
    "emailVerified": false,
    "termsVersion": "1.0",
    "termsAccepted": true
}
```

**Audit Log:** `REGISTER` action logged with user details

---

#### 2. Login
```http
POST /api/v1/auth/login
Content-Type: application/json

{
    "username": "johndoe",
    "password": "SecurePass123!"
}
```

**Response 200 OK:**
```json
{
    "token": "eyJhbGciOiJIUzUxMiJ9...",
    "type": "Bearer",
    "refreshToken": "550e8400-e29b-41d4-a716-446655440000",
    "user": {
        "id": 1,
        "username": "johndoe",
        "email": "john@example.com",
        "role": "USER"
    }
}
```

**Features:**
- ✅ Gera access token JWT (15 min)
- ✅ Gera refresh token (30 dias)
- ✅ Rastreamento de device/IP
- ✅ Verificação de 2FA se habilitado
- ✅ Rate limiting de tentativas

**Audit Log:** `LOGIN` action (SUCCESS/FAILURE)

---

#### 3. Refresh Token
```http
POST /api/v1/auth/refresh
Content-Type: application/json

{
    "refreshToken": "550e8400-e29b-41d4-a716-446655440000"
}
```

**Response 200 OK:**
```json
{
    "access_token": "eyJhbGciOiJIUzUxMiJ9...",
    "refresh_token": "660f9500-f3ac-52e5-b827-557766551111",
    "token_type": "Bearer",
    "expires_in": 900
}
```

**Features:**
- ✅ Token rotation (novo refresh token)
- ✅ Validação de token ativo
- ✅ Rate limiting
- ✅ Tracking de uso

**Audit Log:** `TOKEN_REFRESH` action

---

#### 4. Logout
```http
POST /api/v1/auth/logout
Authorization: Bearer {token}
```

**Response 200 OK:**
```json
{
    "success": true,
    "message": "Logout successful"
}
```

**Features:**
- ✅ Revoga access token (blacklist)
- ✅ Rate limiting de logout
- ✅ Security-first response (sempre sucesso)

**Audit Log:** `LOGOUT` action

---

#### 5. Revogar Refresh Token
```http
POST /api/v1/auth/revoke-refresh-token
Content-Type: application/json

{
    "refreshToken": "550e8400-e29b-41d4-a716-446655440000"
}
```

**Response 200 OK:**
```json
{
    "success": true,
    "message": "Refresh token revoked successfully"
}
```

**Audit Log:** `TOKEN_REVOKE` action

---

#### 6. Logout de Todos Dispositivos
```http
POST /api/v1/auth/logout-all-devices
Authorization: Bearer {token}
```

**Response 200 OK:**
```json
{
    "success": true,
    "message": "Logged out from all devices successfully"
}
```

**Features:**
- ✅ Revoga TODOS refresh tokens do usuário
- ✅ Revoga access token atual
- ✅ Útil para casos de comprometimento

**Audit Log:** `LOGOUT_ALL_DEVICES` action

---

### Verificação de Email

#### 7. Verificar Email
```http
GET /api/v1/auth/verify-email?token={verification_token}
```

**Response 200 OK:**
```json
{
    "success": true,
    "message": "Email verified successfully! Your account is now active.",
    "user": {
        "id": 1,
        "username": "johndoe",
        "emailVerified": true
    }
}
```

**Audit Log:** `EMAIL_VERIFICATION` action

---

#### 8. Reenviar Verificação
```http
POST /api/v1/auth/resend-verification
Content-Type: application/json

{
    "email": "john@example.com"
}
```

---

### Reset de Senha

#### 9. Solicitar Reset
```http
POST /api/v1/auth/forgot-password
Content-Type: application/json

{
    "email": "john@example.com"
}
```

**Response 200 OK:**
```json
{
    "success": true,
    "message": "If an account with this email exists, you will receive a password reset link shortly."
}
```

**Audit Log:** `PASSWORD_RESET_REQUEST` action

---

#### 10. Confirmar Reset
```http
POST /api/v1/auth/reset-password
Content-Type: application/json

{
    "token": "reset-token-here",
    "newPassword": "NewSecurePass456!"
}
```

**Audit Log:** `PASSWORD_RESET_CONFIRM` action

---

## 🔐 Two-Factor Authentication (2FA)

### Base URL: `/api/v1/auth/2fa`

#### 1. Setup 2FA
```http
POST /api/v1/auth/2fa/setup
Authorization: Bearer {token}
```

**Response 200 OK:**
```json
{
    "secretKey": "JBSWY3DPEHPK3PXP",
    "qrCodeUrl": "otpauth://totp/BlogAPI:johndoe?secret=JBSWY3DPEHPK3PXP&issuer=BlogAPI&digits=6&period=30",
    "backupCodes": [
        "12345678",
        "87654321",
        "11223344",
        "44332211",
        "56789012",
        "21098765",
        "13579246",
        "86420135",
        "97531864",
        "46813579"
    ]
}
```

**⚠️ Importante:** 
- Secret key e backup codes só são mostrados UMA vez
- Usuário deve salvar backup codes em local seguro
- QR code deve ser escaneado no app authenticator

---

#### 2. Habilitar 2FA
```http
POST /api/v1/auth/2fa/enable
Authorization: Bearer {token}
Content-Type: application/json

{
    "verificationCode": "123456"
}
```

**Response 200 OK:**
```json
{
    "success": true,
    "message": "Two-factor authentication enabled successfully"
}
```

**Features:**
- ✅ Verifica código TOTP antes de habilitar
- ✅ Marca 2FA como obrigatório no login
- ✅ Timestamp de habilitação

---

#### 3. Desabilitar 2FA
```http
POST /api/v1/auth/2fa/disable
Authorization: Bearer {token}
Content-Type: application/json

{
    "verificationCode": "123456"
}
```

**Response 200 OK:**
```json
{
    "success": true,
    "message": "Two-factor authentication disabled successfully"
}
```

**Features:**
- ✅ Aceita código TOTP ou backup code
- ✅ Remove obrigatoriedade no login

---

#### 4. Status 2FA
```http
GET /api/v1/auth/2fa/status
Authorization: Bearer {token}
```

**Response 200 OK:**
```json
{
    "configured": true,
    "enabled": true,
    "availableBackupCodes": 8,
    "usedBackupCodes": 2,
    "enabledAt": "2025-01-10T14:30:00",
    "lastUsed": "2025-01-15T09:15:00"
}
```

---

#### 5. Regenerar Backup Codes
```http
POST /api/v1/auth/2fa/regenerate-backup-codes
Authorization: Bearer {token}
Content-Type: application/json

{
    "verificationCode": "123456"
}
```

**Response 200 OK:**
```json
{
    "backupCodes": [
        "98765432",
        "23456789",
        "34567890",
        "45678901",
        "56789012",
        "67890123",
        "78901234",
        "89012345",
        "90123456",
        "01234567"
    ]
}
```

**Features:**
- ✅ Invalidates códigos antigos
- ✅ Requer verificação TOTP
- ✅ Reset contador de códigos usados

---

## 👤 Usuários

### Base URL: `/api/v1/users`

#### 1. Listar Usuários (Admin)
```http
GET /api/v1/users?page=0&size=10&sort=createdAt,desc
Authorization: Bearer {admin_token}
```

#### 2. Buscar Usuário
```http
GET /api/v1/users/{id}
Authorization: Bearer {token}
```

#### 3. Atualizar Perfil
```http
PUT /api/v1/users/{id}
Authorization: Bearer {token}
Content-Type: application/json

{
    "email": "newemail@example.com"
}
```

#### 4. Deletar Usuário
```http
DELETE /api/v1/users/{id}
Authorization: Bearer {token}
```

---

## 📝 Posts

### Base URL: `/api/v1/posts`

#### 1. Listar Posts
```http
GET /api/v1/posts?page=0&size=10&category=tech&sort=createdAt,desc
```

**Response 200 OK:**
```json
{
    "content": [
        {
            "id": 1,
            "title": "Implementing 2FA in Spring Boot",
            "content": "In this post we'll explore...",
            "author": {
                "id": 1,
                "username": "johndoe"
            },
            "category": {
                "id": 1,
                "name": "Technology"
            },
            "createdAt": "2025-01-15T10:00:00",
            "commentsCount": 5
        }
    ],
    "totalElements": 50,
    "totalPages": 5,
    "size": 10,
    "number": 0
}
```

#### 2. Buscar Post
```http
GET /api/v1/posts/{id}
```

#### 3. Criar Post
```http
POST /api/v1/posts
Authorization: Bearer {token}
Content-Type: application/json

{
    "title": "New Security Features",
    "content": "Today we implemented...",
    "categoryId": 1
}
```

**Audit Log:** `POST_CREATE` action

#### 4. Atualizar Post
```http
PUT /api/v1/posts/{id}
Authorization: Bearer {token}
Content-Type: application/json

{
    "title": "Updated Title",
    "content": "Updated content..."
}
```

**Audit Log:** `POST_UPDATE` action

#### 5. Deletar Post
```http
DELETE /api/v1/posts/{id}
Authorization: Bearer {token}
```

**Audit Log:** `POST_DELETE` action

---

## 💬 Comentários

### Base URL: `/api/v1/comments`

#### 1. Listar Comentários do Post
```http
GET /api/v1/comments?postId={postId}&page=0&size=10
```

#### 2. Criar Comentário
```http
POST /api/v1/comments
Authorization: Bearer {token}
Content-Type: application/json

{
    "postId": 1,
    "content": "Great post! Very informative."
}
```

**Audit Log:** `COMMENT_CREATE` action

#### 3. Atualizar Comentário
```http
PUT /api/v1/comments/{id}
Authorization: Bearer {token}
Content-Type: application/json

{
    "content": "Updated comment content"
}
```

**Audit Log:** `COMMENT_UPDATE` action

#### 4. Deletar Comentário
```http
DELETE /api/v1/comments/{id}
Authorization: Bearer {token}
```

**Audit Log:** `COMMENT_DELETE` action

---

## 📂 Categorias

### Base URL: `/api/v1/categories`

#### 1. Listar Categorias
```http
GET /api/v1/categories?page=0&size=10
```

#### 2. Criar Categoria (Admin)
```http
POST /api/v1/categories
Authorization: Bearer {admin_token}
Content-Type: application/json

{
    "name": "Security",
    "description": "Posts about security implementations"
}
```

**Audit Log:** `CATEGORY_CREATE` action

#### 3. Atualizar Categoria (Admin)
```http
PUT /api/v1/categories/{id}
Authorization: Bearer {admin_token}
Content-Type: application/json

{
    "name": "Cybersecurity",
    "description": "Updated description"
}
```

**Audit Log:** `CATEGORY_UPDATE` action

#### 4. Deletar Categoria (Admin)
```http
DELETE /api/v1/categories/{id}
Authorization: Bearer {admin_token}
```

**Audit Log:** `CATEGORY_DELETE` action

---

## 📊 Monitoramento e Métricas

### Base URL: `/actuator`

#### 1. Health Check
```http
GET /actuator/health
```

**Response 200 OK:**
```json
{
    "status": "UP",
    "components": {
        "db": {
            "status": "UP",
            "details": {
                "database": "PostgreSQL",
                "validationQuery": "isValid()"
            }
        },
        "diskSpace": {
            "status": "UP"
        }
    }
}
```

#### 2. Métricas
```http
GET /actuator/metrics
```

#### 3. Métricas Específicas
```http
GET /actuator/metrics/blog_api_2fa_success_total
GET /actuator/metrics/blog_api_audit_logs_total
GET /actuator/metrics/blog_api_refresh_tokens_created_total
```

---

## 🔒 Autenticação e Autorização

### Headers Obrigatórios

#### Para Endpoints Protegidos
```http
Authorization: Bearer {jwt_token}
Content-Type: application/json
```

#### Para 2FA (quando habilitado)
```http
Authorization: Bearer {jwt_token}
X-2FA-Code: {totp_code}
```

### Roles e Permissões

| Endpoint | Role Necessária | Descrição |
|----------|----------------|-----------|
| `POST /auth/register` | Público | Registro aberto |
| `POST /auth/login` | Público | Login público |
| `GET /posts` | Público | Leitura pública |
| `POST /posts` | USER+ | Usuários logados |
| `POST /categories` | ADMIN | Apenas admins |
| `DELETE /users/{id}` | ADMIN ou OWNER | Admin ou próprio usuário |

### Códigos de Status

| Status | Descrição | Quando Ocorre |
|--------|-----------|---------------|
| 200 | Success | Operação bem-sucedida |
| 201 | Created | Recurso criado |
| 400 | Bad Request | Dados inválidos |
| 401 | Unauthorized | Token inválido/ausente |
| 403 | Forbidden | Sem permissão |
| 404 | Not Found | Recurso não encontrado |
| 429 | Too Many Requests | Rate limit excedido |
| 500 | Internal Server Error | Erro interno |

---

## 🛡️ Rate Limiting

### Limites Implementados

| Operação | Limite | Janela | Descrição |
|----------|--------|--------|-----------|
| Login | 5 tentativas | 15 min | Por IP |
| Refresh Token | 10 tokens | 1 hora | Por usuário |
| Logout | 20 requests | 1 hora | Por usuário |
| Password Reset | 3 requests | 1 hora | Por email |
| 2FA Setup | 3 tentativas | 1 hora | Por usuário |

### Headers de Rate Limit
```http
X-RateLimit-Limit: 10        # Limite total
X-RateLimit-Remaining: 7     # Tentativas restantes  
X-RateLimit-Reset: 1642234567 # Unix timestamp do reset
```

---

## 📝 Exemplos de Uso

### Fluxo Completo de Autenticação

```bash
# 1. Registro
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "johndoe",
    "email": "john@example.com",
    "password": "SecurePass123!",
    "role": "USER"
  }'

# 2. Login (obtém tokens)
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "johndoe",
    "password": "SecurePass123!"
  }'

# 3. Usar API com token
curl -X GET http://localhost:8080/api/v1/posts \
  -H "Authorization: Bearer eyJhbGciOiJIUzUxMiJ9..."

# 4. Refresh token quando expira
curl -X POST http://localhost:8080/api/v1/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{
    "refreshToken": "550e8400-e29b-41d4-a716-446655440000"
  }'

# 5. Logout
curl -X POST http://localhost:8080/api/v1/auth/logout \
  -H "Authorization: Bearer eyJhbGciOiJIUzUxMiJ9..."
```

### Setup de 2FA

```bash
# 1. Setup inicial (obtém QR code)
curl -X POST http://localhost:8080/api/v1/auth/2fa/setup \
  -H "Authorization: Bearer {token}"

# 2. Habilitar após escanear QR code
curl -X POST http://localhost:8080/api/v1/auth/2fa/enable \
  -H "Authorization: Bearer {token}" \
  -H "Content-Type: application/json" \
  -d '{"verificationCode": "123456"}'

# 3. Status do 2FA
curl -X GET http://localhost:8080/api/v1/auth/2fa/status \
  -H "Authorization: Bearer {token}"
```

---

Esta documentação cobre todos os endpoints implementados na Blog API, incluindo as novas features de segurança. Todos os endpoints são monitorados via audit logs e métricas para compliance e observabilidade.