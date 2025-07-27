# Blog API REST

API REST para sistema de blog desenvolvida com Java e Spring Boot seguindo metodologia de desenvolvimento com IA.

## 🚀 Tecnologias

- **Java 17+**
- **Spring Boot 3.2**
- **Spring Security** (JWT Authentication)
- **Spring Data JPA**
- **PostgreSQL**
- **Maven**
- **Swagger/OpenAPI**
- **JUnit 5**

## 📋 Funcionalidades

### Autenticação
- [x] Registro de usuários
- [x] Login com JWT
- [x] Controle de roles (USER, AUTHOR, ADMIN)

### Gestão de Usuários
- [x] CRUD de usuários
- [x] Busca por username/email
- [x] Controle de acesso por role

### Gestão de Posts
- [x] CRUD completo de posts
- [x] Publicação/despublicação
- [x] Busca por palavra-chave
- [x] Filtro por categoria e autor
- [x] Paginação

### Sistema de Comentários
- [x] Comentários em posts
- [x] Respostas aninhadas
- [x] CRUD de comentários

### Categorias
- [x] CRUD de categorias
- [x] Filtros por categoria

## 🏗️ Arquitetura

```
src/main/java/com/blog/api/
├── config/          # Configurações (Security, JWT, Swagger)
├── controller/      # REST Controllers
├── service/         # Business Logic
├── repository/      # Data Access Layer
├── entity/          # JPA Entities
├── dto/             # Data Transfer Objects
├── exception/       # Exception Handling
└── util/            # Utilities (JWT)
```

## 🔐 Endpoints da API

### Autenticação
- `POST /api/v1/auth/register` - Registrar usuário
- `POST /api/v1/auth/login` - Login

### Usuários
- `GET /api/v1/users` - Listar usuários (ADMIN)
- `GET /api/v1/users/{id}` - Buscar usuário por ID
- `GET /api/v1/users/username/{username}` - Buscar por username

### Posts
- `GET /api/v1/posts` - Listar posts publicados
- `GET /api/v1/posts/{id}` - Buscar post por ID
- `GET /api/v1/posts/search?keyword=` - Buscar posts
- `POST /api/v1/posts` - Criar post (AUTHOR+)
- `PUT /api/v1/posts/{id}` - Atualizar post
- `DELETE /api/v1/posts/{id}` - Deletar post

### Categorias
- `GET /api/v1/categories` - Listar categorias
- `POST /api/v1/categories` - Criar categoria (ADMIN)
- `PUT /api/v1/categories/{id}` - Atualizar categoria (ADMIN)

### Comentários
- `GET /api/v1/comments/post/{postId}` - Comentários do post
- `POST /api/v1/comments` - Criar comentário
- `PUT /api/v1/comments/{id}` - Atualizar comentário
- `DELETE /api/v1/comments/{id}` - Deletar comentário

## ⚙️ Configuração

### Database (PostgreSQL)
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/blogdb
    username: bloguser
    password: blogpass
```

### JWT
```yaml
jwt:
  secret: mySecretKey123456789012345678901234567890
  expiration: 86400000  # 24 horas
```

## 🔧 Como Executar

### Opção 1: Docker (Recomendado)

1. **Clone o projeto**
2. **Execute com Docker Compose:**
   ```bash
   docker-compose up -d
   ```
3. **Acesse a aplicação:** http://localhost:8080/swagger-ui.html

### Opção 2: Execução Local

1. **Clone o projeto**
2. **Configure o PostgreSQL** com as credenciais do `application.yml`
3. **Execute:**
   ```bash
   mvn spring-boot:run
   ```
4. **Acesse a documentação:** http://localhost:8080/swagger-ui.html

## 🐳 Docker

### Comandos Úteis

```bash
# Subir toda a aplicação (PostgreSQL + API)
docker-compose up -d

# Ver logs da aplicação
docker-compose logs -f blog-api

# Ver logs do banco
docker-compose logs -f postgres

# Parar todos os serviços
docker-compose down

# Rebuild da aplicação
docker-compose up --build blog-api

# Limpar volumes (dados do banco)
docker-compose down -v

# Acessar container do PostgreSQL
docker-compose exec postgres psql -U bloguser -d blogdb
```

### Estrutura Docker
- **Dockerfile**: Multi-stage build para otimização
- **docker-compose.yml**: Orquestração PostgreSQL + API
- **application-docker.yml**: Configurações específicas para Docker
- **Health checks**: Monitoramento automático dos serviços

## 🚀 CI/CD com GitHub Actions

### Workflows Implementados

#### 1. **Continuous Integration** (`.github/workflows/ci.yml`)
Executa automaticamente em push/PR para validar qualidade do código:

- ✅ **Testes Unitários**: JUnit 5 com PostgreSQL TestContainer
- ✅ **Cobertura de Código**: JaCoCo com upload para Codecov
- ✅ **Build da Aplicação**: Maven compile e package
- ✅ **Análise de Qualidade**: SpotBugs, Checkstyle, PMD
- ✅ **Scan de Segurança**: OWASP Dependency Check

**Triggers:**
- Push nas branches: `main`, `develop`, `feature/*`
- Pull Requests para: `main`, `develop`

#### 2. **Docker Build & Publish** (`.github/workflows/docker-build.yml`)
Constrói e publica imagens Docker automaticamente:

- 🐳 **Multi-platform Build**: linux/amd64, linux/arm64
- 📦 **GitHub Container Registry**: Versionamento automático
- 🔍 **Vulnerability Scan**: Trivy security scanner
- ✅ **Integration Tests**: Teste da imagem com docker-compose

**Triggers:**
- Push na branch `main`
- Tags `v*` (releases)
- Pull Requests para `main`

#### 3. **Deploy Pipeline** (`.github/workflows/deploy.yml`)
Deploy automatizado para múltiplos ambientes:

**Staging:**
- 🔄 Deploy automático via ECS/Kubernetes
- 🧪 Smoke tests automáticos
- 📢 Notificações Slack

**Production:**
- 🏷️ Deploy apenas com tags de release
- 💾 Backup automático do banco
- 🔄 Rollback automático em caso de falha
- ☁️ Invalidação de cache CloudFront

**Triggers:**
- Push na `main` → Staging
- Tags `v*` → Production
- Manual dispatch

#### 4. **Performance Testing** (`.github/workflows/performance-test.yml`)
Testes de performance automatizados:

- ⚡ **JMeter Load Tests**: 20 usuários simultâneos
- 📊 **Métricas de Performance**: Tempo de resposta e taxa de sucesso
- 📈 **Reports Automáticos**: Comentários em PRs
- ⏰ **Testes Diários**: Cron schedule às 2h UTC

**Thresholds:**
- Tempo de resposta médio: < 200ms
- Taxa de sucesso: > 95%

### Configuração de Secrets

Para ativar todos os workflows, configure os seguintes secrets no GitHub:

```bash
# AWS Credentials
AWS_ACCESS_KEY_ID
AWS_SECRET_ACCESS_KEY

# Notifications
SLACK_WEBHOOK_URL

# CodeCov (opcional)
CODECOV_TOKEN

# CloudFront (production)
CLOUDFRONT_DISTRIBUTION_ID
```

### Status Badges

Adicione badges no README para mostrar status dos builds:

```markdown
![CI](https://github.com/marcusvbrangel/blog-tech/workflows/Continuous%20Integration/badge.svg)
![Docker](https://github.com/marcusvbrangel/blog-tech/workflows/Docker%20Build%20and%20Publish/badge.svg)
![Deploy](https://github.com/marcusvbrangel/blog-tech/workflows/Deploy%20to%20Production/badge.svg)
```

## 🧪 Testes

### Testes Locais
```bash
# Testes unitários
mvn test

# Testes com cobertura
mvn test jacoco:report

# Testes de integração
mvn verify

# Performance tests (requer Docker)
docker-compose up -d
# Execute performance-test workflow manualmente
```

### Tipos de Teste
- **Unit Tests**: Service layer, Repository layer
- **Integration Tests**: Controller endpoints, Database
- **Performance Tests**: Load testing com JMeter
- **Security Tests**: OWASP dependency check

## 📚 Documentação

A documentação completa da API está disponível via Swagger UI em:
- **Swagger UI:** http://localhost:8080/swagger-ui.html
- **OpenAPI JSON:** http://localhost:8080/v3/api-docs

## 👥 Roles e Permissões

- **USER:** Pode comentar em posts
- **AUTHOR:** Pode criar, editar e deletar próprios posts + USER
- **ADMIN:** Acesso total + gerenciar categorias e usuários

## 🔄 Metodologia de Desenvolvimento

Este projeto foi desenvolvido seguindo uma metodologia de desenvolvimento com IA:

1. ✅ **PRD (Product Requirements Document)**
2. ✅ **Tech Specs** 
3. ✅ **Geração de Tarefas**
4. ✅ **Implementação**
5. ✅ **Testes**
6. ✅ **Revisão de Código**