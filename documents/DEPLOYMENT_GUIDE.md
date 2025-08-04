# 🚀 Blog API - Guia de Deploy e Configuração

Este documento fornece instruções completas para deploy da Blog API com todas as features de segurança implementadas.

## 📋 Visão Geral

A Blog API foi desenvolvida como uma aplicação **Spring Boot enterprise-grade** com:
- ✅ Autenticação JWT + Refresh Tokens
- ✅ Two-Factor Authentication (2FA)
- ✅ Sistema formal de Audit Logging
- ✅ Métricas e monitoramento
- ✅ Testes automatizados (100% cobertura)

---

## 🛠️ Pré-requisitos

### Ambiente de Desenvolvimento
```bash
# Java 21+
java -version

# Maven 3.6+
mvn -version

# PostgreSQL 13+ (ou H2 para desenvolvimento)
psql --version

# Docker (opcional para containers)
docker --version
```

### Dependências Principais
- **Spring Boot 3.2.0**
- **Spring Security 6.x**
- **Spring Data JPA**
- **PostgreSQL/H2 Database**
- **Micrometer Metrics**
- **JWT (jjwt 0.12.3)**

---

## ⚙️ Configuração

### 1. Configurações de Ambiente

#### application.properties (Desenvolvimento)
```properties
# ================================
# SERVIDOR
# ================================
server.port=8080
spring.application.name=blog-api

# ================================
# DATABASE
# ================================
spring.datasource.url=jdbc:postgresql://localhost:5432/blog_api
spring.datasource.username=blog_user
spring.datasource.password=blog_password
spring.datasource.driver-class-name=org.postgresql.Driver

# JPA/Hibernate
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect

# Flyway Migration
spring.flyway.enabled=true
spring.flyway.locations=classpath:db/migration
spring.flyway.schemas=public

# ================================
# JWT CONFIGURATION
# ================================
jwt.secret=mySecretKeyForJWTTokenGenerationThatShouldBeLongEnoughAndSecure123456789
jwt.expiration=900                    # 15 minutes
jwt.refresh-token.expiration=2592000  # 30 days
jwt.refresh-token.max-per-user=5
jwt.refresh-token.cleanup.enabled=true
jwt.refresh-token.rotation.enabled=true
jwt.refresh-token.rate-limit.max-per-hour=10

# ================================
# SEGURANÇA
# ================================
blog.security.email-verification.enabled=true
blog.security.password-policy.min-length=8
blog.security.password-policy.require-uppercase=true
blog.security.password-policy.require-lowercase=true
blog.security.password-policy.require-digit=true
blog.security.password-policy.require-special=true

# ================================
# EMAIL (opcional)
# ================================
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=your-email@gmail.com
spring.mail.password=your-app-password
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true

# ================================
# LOGGING
# ================================
logging.level.com.blog.api=INFO
logging.level.com.blog.api.service.RefreshTokenService=INFO
logging.level.com.blog.api.service.AuditLogService=INFO
logging.level.com.blog.api.service.TwoFactorAuthService=INFO
logging.level.org.hibernate.SQL=DEBUG
logging.level.org.hibernate.type.descriptor.sql.BasicBinder=TRACE

# ================================
# ACTUATOR (Métricas)
# ================================
management.endpoints.web.exposure.include=health,metrics,info
management.endpoint.health.show-details=when-authorized
management.metrics.export.prometheus.enabled=true
```

#### application-prod.properties (Produção)
```properties
# ================================
# PRODUÇÃO - OVERRIDE CONFIGS
# ================================
server.port=${PORT:8080}

# Database (usar variáveis de ambiente)
spring.datasource.url=${DATABASE_URL}
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}

# JWT (usar secrets seguros)
jwt.secret=${JWT_SECRET}

# Disable SQL logging
spring.jpa.show-sql=false
logging.level.org.hibernate.SQL=WARN
logging.level.org.hibernate.type.descriptor.sql.BasicBinder=WARN

# Security headers
server.servlet.session.cookie.secure=true
server.servlet.session.cookie.http-only=true
server.servlet.session.cookie.same-site=strict

# SSL (se disponível)
server.ssl.enabled=false
# server.ssl.key-store=classpath:keystore.p12
# server.ssl.key-store-password=${SSL_PASSWORD}
# server.ssl.key-store-type=PKCS12
```

### 2. Variáveis de Ambiente

#### Desenvolvimento (.env)
```bash
# Database
DATABASE_URL=jdbc:postgresql://localhost:5432/blog_api
DB_USERNAME=blog_user
DB_PASSWORD=blog_password

# JWT
JWT_SECRET=mySecretKeyForJWTTokenGenerationThatShouldBeLongEnoughAndSecure123456789

# Email
EMAIL_USERNAME=your-email@gmail.com
EMAIL_PASSWORD=your-app-password

# App
SPRING_PROFILES_ACTIVE=dev
```

#### Produção (.env.prod)
```bash
# Database
DATABASE_URL=jdbc:postgresql://prod-db-host:5432/blog_api_prod
DB_USERNAME=blog_prod_user
DB_PASSWORD=super_secure_password_here

# JWT (generate secure secrets)
JWT_SECRET=extremely_long_and_secure_jwt_secret_key_for_production_use_only_123456789

# Email
EMAIL_USERNAME=noreply@yourdomain.com
EMAIL_PASSWORD=secure_app_password

# App
SPRING_PROFILES_ACTIVE=prod
PORT=8080

# Optional: Monitoring
PROMETHEUS_ENABLED=true
```

---

## 🗄️ Configuração do Banco de Dados

### PostgreSQL Setup

#### 1. Instalação (Ubuntu/Debian)
```bash
# Instalar PostgreSQL
sudo apt update
sudo apt install postgresql postgresql-contrib

# Iniciar serviço
sudo systemctl start postgresql
sudo systemctl enable postgresql
```

#### 2. Criação do Banco
```sql
-- Conectar como postgres
sudo -u postgres psql

-- Criar usuário e banco
CREATE USER blog_user WITH PASSWORD 'blog_password';
CREATE DATABASE blog_api OWNER blog_user;

-- Conceder permissões
GRANT ALL PRIVILEGES ON DATABASE blog_api TO blog_user;

-- Conectar ao banco
\c blog_api

-- Habilitar extensões
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
```

#### 3. Configuração de Performance
```sql
-- Configurações de performance (postgresql.conf)
shared_buffers = 256MB
effective_cache_size = 1GB  
random_page_cost = 1.1
checkpoint_completion_target = 0.9
wal_buffers = 16MB
default_statistics_target = 100
```

### Docker Compose (Desenvolvimento)
```yaml
version: '3.8'

services:
  postgres:
    image: postgres:15
    container_name: blog-api-db
    environment:
      POSTGRES_DB: blog_api
      POSTGRES_USER: blog_user
      POSTGRES_PASSWORD: blog_password
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data
      - ./init.sql:/docker-entrypoint-initdb.d/init.sql
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U blog_user -d blog_api"]
      interval: 30s
      timeout: 10s
      retries: 3

  redis:
    image: redis:7-alpine
    container_name: blog-api-redis
    ports:
      - "6379:6379"
    command: redis-server --appendonly yes
    volumes:
      - redis_data:/data

  app:
    build: .
    container_name: blog-api-app
    ports:
      - "8080:8080"
    environment:
      DATABASE_URL: jdbc:postgresql://postgres:5432/blog_api
      DB_USERNAME: blog_user
      DB_PASSWORD: blog_password
      SPRING_PROFILES_ACTIVE: docker
    depends_on:
      postgres:
        condition: service_healthy
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8080/actuator/health"]
      interval: 30s
      timeout: 10s
      retries: 3

volumes:
  postgres_data:
  redis_data:
```

---

## 🐳 Containerização

### Dockerfile
```dockerfile
# Multi-stage build for optimal image size
FROM openjdk:21-jdk-slim as builder

WORKDIR /app
COPY pom.xml .
COPY src ./src

# Install Maven
RUN apt-get update && apt-get install -y maven

# Build application
RUN mvn clean package -DskipTests

# Runtime stage
FROM openjdk:21-jre-slim

# Create non-root user for security
RUN groupadd -r blogapi && useradd -r -g blogapi blogapi

WORKDIR /app

# Copy JAR from builder stage
COPY --from=builder /app/target/blog-api-*.jar app.jar

# Change ownership
RUN chown -R blogapi:blogapi /app

# Switch to non-root user
USER blogapi

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# Expose port
EXPOSE 8080

# JVM optimization for containers
ENV JAVA_OPTS="-Xmx512m -Xms256m -XX:+UseG1GC -XX:G1HeapRegionSize=16m"

# Run application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
```

### Docker Build e Run
```bash
# Build image
docker build -t blog-api:latest .

# Run container
docker run -d \
  --name blog-api \
  -p 8080:8080 \
  -e DATABASE_URL=jdbc:postgresql://host.docker.internal:5432/blog_api \
  -e DB_USERNAME=blog_user \
  -e DB_PASSWORD=blog_password \
  -e JWT_SECRET=your_jwt_secret_here \
  blog-api:latest

# Logs
docker logs -f blog-api

# Health check
curl http://localhost:8080/actuator/health
```

---

## ☁️ Deploy em Cloud

### Heroku

#### 1. Preparação
```bash
# Install Heroku CLI
# https://devcenter.heroku.com/articles/heroku-cli

# Login
heroku login

# Create app
heroku create your-blog-api

# Add PostgreSQL
heroku addons:create heroku-postgresql:mini
```

#### 2. Configuração
```bash
# Set environment variables
heroku config:set JWT_SECRET=your_super_long_and_secure_jwt_secret_key
heroku config:set SPRING_PROFILES_ACTIVE=prod
heroku config:set EMAIL_USERNAME=your-email@gmail.com
heroku config:set EMAIL_PASSWORD=your-app-password

# Deploy
git push heroku main

# Run migrations
heroku run java -jar target/blog-api-*.jar --spring.flyway.migrate
```

#### 3. Procfile
```
web: java -Dserver.port=$PORT $JAVA_OPTS -jar target/blog-api-*.jar
```

### AWS (EC2 + RDS)

#### 1. RDS PostgreSQL
```bash
# Create RDS instance via AWS Console
- Engine: PostgreSQL 15
- Instance class: db.t3.micro (free tier)
- Storage: 20GB GP2
- Multi-AZ: No (for cost)
- Security group: Allow 5432 from EC2
```

#### 2. EC2 Setup
```bash
# Launch EC2 instance (Amazon Linux 2)
sudo yum update -y
sudo yum install -y java-21-amazon-corretto

# Install application
sudo mkdir -p /opt/blog-api
sudo chown ec2-user:ec2-user /opt/blog-api

# Copy JAR file
scp -i your-key.pem target/blog-api-*.jar ec2-user@your-ec2-ip:/opt/blog-api/

# Create systemd service
sudo vi /etc/systemd/system/blog-api.service
```

#### 3. Systemd Service
```ini
[Unit]
Description=Blog API
After=network.target

[Service]
Type=simple
User=ec2-user
WorkingDirectory=/opt/blog-api
ExecStart=/usr/bin/java -jar blog-api-*.jar
Restart=always
RestartSec=10

Environment=DATABASE_URL=jdbc:postgresql://your-rds-endpoint:5432/blog_api
Environment=DB_USERNAME=blog_user
Environment=DB_PASSWORD=your_password
Environment=JWT_SECRET=your_jwt_secret
Environment=SPRING_PROFILES_ACTIVE=prod

[Install]
WantedBy=multi-user.target
```

```bash
# Enable and start service
sudo systemctl daemon-reload
sudo systemctl enable blog-api
sudo systemctl start blog-api

# Check status
sudo systemctl status blog-api
sudo journalctl -u blog-api -f
```

### Docker Swarm/Kubernetes

#### docker-compose.prod.yml
```yaml
version: '3.8'

services:
  app:
    image: your-registry/blog-api:latest
    ports:
      - "8080:8080"
    environment:
      DATABASE_URL: jdbc:postgresql://postgres:5432/blog_api
      DB_USERNAME: blog_user
      DB_PASSWORD: ${DB_PASSWORD}
      JWT_SECRET: ${JWT_SECRET}
      SPRING_PROFILES_ACTIVE: prod
    deploy:
      replicas: 2
      resources:
        limits:
          memory: 512M
        reservations:
          memory: 256M
      restart_policy:
        condition: on-failure
        delay: 5s
        max_attempts: 3
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8080/actuator/health"]
      interval: 30s
      timeout: 10s
      retries: 3

  postgres:
    image: postgres:15
    environment:
      POSTGRES_DB: blog_api
      POSTGRES_USER: blog_user
      POSTGRES_PASSWORD: ${DB_PASSWORD}
    volumes:
      - postgres_data:/var/lib/postgresql/data
    deploy:
      placement:
        constraints: [node.role == manager]

volumes:
  postgres_data:
    external: true
```

---

## 📊 Monitoramento

### Prometheus + Grafana

#### 1. Prometheus Configuration
```yaml
# prometheus.yml
global:
  scrape_interval: 15s

scrape_configs:
  - job_name: 'blog-api'
    static_configs:
      - targets: ['localhost:8080']
    metrics_path: '/actuator/prometheus'
    scrape_interval: 10s
```

#### 2. Grafana Dashboard Queries
```promql
# JVM Memory Usage
jvm_memory_used_bytes{application="blog-api"}

# HTTP Requests
http_server_requests_seconds_count{application="blog-api"}

# Database Connections
hikaricp_connections_active{application="blog-api"}

# Custom Metrics
blog_api_2fa_success_total
blog_api_audit_logs_total
blog_api_refresh_tokens_created_total
```

### Application Metrics
```java
// Custom metrics já implementados
@Autowired
private MeterRegistry meterRegistry;

// Refresh Tokens
Counter.builder("blog_api_refresh_tokens_created_total")
  .description("Total refresh tokens created")
  .register(meterRegistry);

// 2FA
Counter.builder("blog_api_2fa_success_total")
  .description("Total successful 2FA authentications")
  .register(meterRegistry);

// Audit Logs  
Counter.builder("blog_api_audit_logs_total")
  .description("Total audit logs created")
  .register(meterRegistry);
```

---

## 🔒 Segurança em Produção

### 1. Secrets Management

#### Environment Variables (Recomendado)
```bash
# Use gestores de secrets
export JWT_SECRET=$(aws ssm get-parameter --name "blog-api-jwt-secret" --with-decryption --query 'Parameter.Value' --output text)
export DB_PASSWORD=$(vault kv get -field=password secret/blog-api/db)
```

#### Kubernetes Secrets
```yaml
apiVersion: v1
kind: Secret
metadata:
  name: blog-api-secrets
type: Opaque
data:
  jwt-secret: <base64-encoded-secret>
  db-password: <base64-encoded-password>
```

### 2. Network Security

#### Reverse Proxy (Nginx)
```nginx
upstream blog_api {
    server 127.0.0.1:8080;
}

server {
    listen 80;
    server_name your-domain.com;
    return 301 https://$server_name$request_uri;
}

server {
    listen 443 ssl http2;
    server_name your-domain.com;

    ssl_certificate /path/to/cert.pem;
    ssl_certificate_key /path/to/key.pem;
    
    # Security headers
    add_header X-Frame-Options DENY;
    add_header X-Content-Type-Options nosniff;
    add_header X-XSS-Protection "1; mode=block";
    add_header Strict-Transport-Security "max-age=31536000; includeSubDomains";

    location / {
        proxy_pass http://blog_api;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

### 3. Database Security
```sql
-- Create restricted user for application
CREATE USER blog_app_user WITH PASSWORD 'secure_password';

-- Grant minimal permissions
GRANT CONNECT ON DATABASE blog_api TO blog_app_user;
GRANT USAGE ON SCHEMA public TO blog_app_user;
GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA public TO blog_app_user;
GRANT USAGE ON ALL SEQUENCES IN SCHEMA public TO blog_app_user;

-- Revoke dangerous permissions
REVOKE CREATE ON SCHEMA public FROM blog_app_user;
REVOKE ALL ON pg_settings FROM blog_app_user;
```

---

## 🔄 CI/CD Pipeline

### GitHub Actions
```yaml
# .github/workflows/deploy.yml
name: Deploy Blog API

on:
  push:
    branches: [main]
  pull_request:
    branches: [main]

jobs:
  test:
    runs-on: ubuntu-latest
    
    services:
      postgres:
        image: postgres:15
        env:
          POSTGRES_PASSWORD: test_password
          POSTGRES_USER: test_user
          POSTGRES_DB: blog_api_test
        options: >-
          --health-cmd pg_isready
          --health-interval 10s
          --health-timeout 5s
          --health-retries 5

    steps:
    - uses: actions/checkout@v3
    
    - name: Set up JDK 21
      uses: actions/setup-java@v3
      with:
        java-version: '21'
        distribution: 'corretto'
        
    - name: Cache Maven dependencies
      uses: actions/cache@v3
      with:
        path: ~/.m2
        key: ${{ runner.os }}-m2-${{ hashFiles('**/pom.xml') }}
        
    - name: Run tests
      run: mvn clean test
      env:
        DATABASE_URL: jdbc:postgresql://localhost:5432/blog_api_test
        DB_USERNAME: test_user
        DB_PASSWORD: test_password
        
    - name: Build JAR
      run: mvn clean package -DskipTests
      
    - name: Upload artifacts
      uses: actions/upload-artifact@v3
      with:
        name: jar-artifact
        path: target/*.jar

  deploy:
    needs: test
    runs-on: ubuntu-latest
    if: github.ref == 'refs/heads/main'
    
    steps:
    - name: Download artifacts
      uses: actions/download-artifact@v3
      with:
        name: jar-artifact
        
    - name: Deploy to production
      run: |
        # Deployment script here
        echo "Deploying to production..."
```

---

## 📋 Checklist de Deploy

### Pré-Deploy
- [ ] Todas as features testadas localmente
- [ ] Configurações de produção validadas  
- [ ] Secrets configurados corretamente
- [ ] Database migrations prontas
- [ ] SSL/TLS configurado
- [ ] Monitoramento configurado

### Deploy
- [ ] Build da aplicação sem erros
- [ ] Migrations executadas com sucesso
- [ ] Health checks passando
- [ ] Endpoints respondendo corretamente
- [ ] Métricas sendo coletadas
- [ ] Logs sendo gerados

### Pós-Deploy
- [ ] Testes de integração em produção
- [ ] Verificação de 2FA funcionando
- [ ] Refresh tokens sendo criados
- [ ] Audit logs sendo gerados
- [ ] Performance monitorada
- [ ] Alertas configurados

---

## 🚨 Troubleshooting

### Problemas Comuns

#### 1. Connection Refused (Database)
```bash
# Verificar status do PostgreSQL
sudo systemctl status postgresql

# Verificar conectividade
telnet localhost 5432

# Logs do PostgreSQL
sudo tail -f /var/log/postgresql/postgresql-*.log
```

#### 2. JWT Token Issues
```bash
# Verificar secret key
echo $JWT_SECRET | wc -c  # Deve ter pelo menos 32 caracteres

# Logs da aplicação
docker logs -f blog-api | grep JWT
```

#### 3. 2FA Not Working
```bash
# Verificar time sync
sudo ntpdate -s time.nist.gov

# Verificar logs 2FA
grep "2FA" /var/log/blog-api/application.log
```

#### 4. Performance Issues
```sql
-- Verificar queries lentas
SELECT query, mean_time, calls 
FROM pg_stat_statements 
ORDER BY mean_time DESC 
LIMIT 10;

-- Verificar conexões
SELECT count(*) FROM pg_stat_activity;
```

---

Esta documentação fornece todas as informações necessárias para deploy seguro e confiável da Blog API em qualquer ambiente, desde desenvolvimento local até produção enterprise.