# 📮 Postman Collection - Blog API

Esta pasta contém a coleção completa do Postman para testar a **Blog API REST**.

## 📁 Arquivos Incluídos

### 1. `Blog-API-Collection.postman_collection.json`
Coleção principal com todos os endpoints organizados por funcionalidade:

- 🔐 **Authentication** (2 requests)
  - Register User
  - Login User
- 👥 **Users** (3 requests) 
  - Get All Users
  - Get User by ID
  - Get User by Username
- 📚 **Categories** (4 requests)
  - Get All Categories
  - Create Category
  - Get Category by ID
  - Update Category
- 📝 **Posts** (6 requests)
  - Get All Published Posts
  - Create Post
  - Get Post by ID
  - Update Post
  - Search Posts
  - Get Posts by Category
- 💬 **Comments** (4 requests)
  - Get Comments by Post
  - Create Comment
  - Create Reply Comment
  - Update Comment
- 🔍 **Health & Monitoring** (3 requests)
  - Health Check
  - Prometheus Metrics
  - Application Info
- 🧪 **Test Scenarios** (1 request)
  - Complete User Journey

### 2. `Blog-API-Environment.postman_environment.json`
Arquivo de ambiente com variáveis pré-configuradas:

- `baseUrl`: http://localhost:8080
- `jwtToken`: (auto-preenchido após login)
- `currentUserId`: (auto-preenchido após login)
- `currentUsername`: testuser
- `testEmail`: test@example.com
- `testPassword`: password123
- `categoryId`: (auto-preenchido após criação)
- `postId`: (auto-preenchido após criação)
- `commentId`: (auto-preenchido após criação)

## 🚀 Como Usar

### 1. Importar no Postman

1. **Abra o Postman**
2. **Clique em "Import"**
3. **Selecione "Upload Files"**
4. **Importe os dois arquivos:**
   - `Blog-API-Collection.postman_collection.json`
   - `Blog-API-Environment.postman_environment.json`

### 2. Configurar Ambiente

1. **No canto superior direito, selecione o ambiente:**
   - "Blog API - Development"
2. **Verifique se `baseUrl` está correto:**
   - http://localhost:8080

### 3. Executar Testes

#### **Fluxo Recomendado:**

1. **🔐 Authentication**
   - Execute "Register User" (primeira vez)
   - Execute "Login User" (o token JWT será salvo automaticamente)

2. **📚 Categories**
   - Execute "Create Category" (o ID será salvo automaticamente)
   - Execute "Get All Categories"

3. **📝 Posts**
   - Execute "Create Post" (usa categoryId automaticamente)
   - Execute "Get All Published Posts"
   - Execute "Get Post by ID"

4. **💬 Comments**
   - Execute "Create Comment" (usa postId automaticamente)
   - Execute "Get Comments by Post"

5. **🔍 Monitoring**
   - Execute "Health Check"
   - Execute "Prometheus Metrics"

## ✨ Funcionalidades Automáticas

### **Autenticação Automática**
- O token JWT é extraído automaticamente após login
- Todas as requests protegidas usam `{{jwtToken}}` automaticamente

### **Variáveis Dinâmicas**
- IDs são salvos automaticamente após criação
- Permite testar relacionamentos entre entidades

### **Testes Automáticos**
Cada request inclui testes automáticos que verificam:
- ✅ Status codes corretos
- ✅ Estrutura das responses
- ✅ Dados específicos das entidades
- ✅ Relacionamentos entre recursos

### **Validações Incluídas**
- Verificação de paginação
- Validação de dados obrigatórios
- Checagem de autorização
- Testes de integridade referencial

## 🔧 Configurações Avançadas

### **Variáveis de Ambiente Customizadas**

Você pode ajustar as seguintes variáveis conforme necessário:

```json
{
  "baseUrl": "http://localhost:8080",     // URL da API
  "currentUsername": "testuser",          // Username para testes
  "testEmail": "test@example.com",        // Email para registro
  "testPassword": "password123"           // Senha para testes
}
```

### **Execução em Lote**

Para executar toda a coleção:

1. **Clique na coleção "Blog API - Complete Collection"**
2. **Clique em "Run collection"**
3. **Configure:**
   - Environment: "Blog API - Development"
   - Iterations: 1
   - Delay: 1000ms (entre requests)

## 📊 Relatórios de Teste

### **Console Logs**
O Postman mostrará logs detalhados de:
- Requests executadas
- Responses recebidas
- Testes aprovados/falhos
- Variáveis atualizadas

### **Test Results**
Cada teste mostra:
- ✅ Status codes verificados
- ✅ Estrutura de dados validada
- ✅ Conteúdo específico verificado
- ❌ Falhas com detalhes específicos

## 🐛 Troubleshooting

### **Problemas Comuns:**

1. **"Connection refused"**
   - Verifique se a aplicação está rodando: `docker-compose up -d`
   - Confirme a URL: http://localhost:8080

2. **"401 Unauthorized"**
   - Execute primeiro "Login User"
   - Verifique se o token foi salvo na variável `jwtToken`

3. **"404 Not Found"**
   - Verifique se os IDs existem (execute Create antes de Get/Update)
   - Confirme se as rotas estão corretas

4. **"Validation errors"**
   - Verifique os payloads JSON
   - Confirme campos obrigatórios

### **Reset do Ambiente:**

Para começar do zero:
1. Limpe todas as variáveis do ambiente
2. Execute "Register User" novamente
3. Execute "Login User"
4. Prossiga com o fluxo normal

## 🎯 Cenários de Teste Avançados

### **Teste de Carga Simples**
Execute a coleção com:
- Iterations: 10
- Delay: 500ms

### **Teste de Endpoints Públicos**
Execute apenas:
- Get All Categories
- Get All Published Posts
- Health Check

### **Teste de Autenticação**
Foque em:
- Register/Login flow
- Protected endpoints
- Token expiration

---

**💡 Dica:** Use a aba "Tests" em cada request para ver e customizar as validações automáticas implementadas!