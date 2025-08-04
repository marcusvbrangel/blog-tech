# 10_US02_Confirmacao_Email_Implementar_TokenService.md

## 📋 Contexto da Tarefa
- **User Story:** US02 - Confirmação de Email
- **Número da Tarefa:** 10/96
- **Complexidade:** Alta
- **Estimativa:** 4 horas
- **Dependências:** Tarefa 09 (ConfirmationToken entity)
- **Sprint:** Sprint 1

## 🎯 Objetivo
Implementar o serviço TokenService responsável por gerenciar tokens de confirmação, incluindo geração, validação, expiração e limpeza automática de tokens expirados.

## 📝 Especificação Técnica

### **Componentes a Implementar:**
- [ ] Interface TokenService
- [ ] Implementação TokenServiceImpl
- [ ] Repository ConfirmationTokenRepository
- [ ] Método generateToken()
- [ ] Método validateToken()
- [ ] Método invalidateToken()
- [ ] Scheduled task para limpeza de tokens expirados
- [ ] Cache para tokens válidos (Redis)

### **Integrações Necessárias:**
- **Com ConfirmationTokenRepository:** Operações CRUD
- **Com Redis:** Cache de tokens válidos
- **Com Spring Scheduler:** Limpeza automática
- **Com NewsletterSubscriber:** Vinculação de tokens

## ✅ Acceptance Criteria
- [ ] **AC1:** Geração de tokens únicos com expiração de 24h
- [ ] **AC2:** Validação de tokens considerando expiração
- [ ] **AC3:** Invalidação de tokens após confirmação
- [ ] **AC4:** Limpeza automática de tokens expirados (diário)
- [ ] **AC5:** Cache Redis para performance
- [ ] **AC6:** Tratamento de erros e logs apropriados

## 🧪 Testes Requeridos

### **Testes Unitários:**
- [ ] Teste de geração de token único
- [ ] Teste de validação de token válido
- [ ] Teste de validação de token expirado
- [ ] Teste de invalidação de token
- [ ] Teste de limpeza de tokens expirados
- [ ] Teste de cache Redis

### **Testes de Integração:**
- [ ] Teste end-to-end de ciclo de vida do token
- [ ] Teste de scheduled task
- [ ] Teste de performance com cache

## 🔗 Arquivos Afetados
- [ ] **src/main/java/com/blog/api/service/TokenService.java:** Interface do serviço
- [ ] **src/main/java/com/blog/api/service/impl/TokenServiceImpl.java:** Implementação
- [ ] **src/main/java/com/blog/api/repository/ConfirmationTokenRepository.java:** Repository
- [ ] **src/test/java/com/blog/api/service/TokenServiceTest.java:** Testes unitários
- [ ] **src/test/java/com/blog/api/service/TokenServiceIntegrationTest.java:** Testes integração

## 📚 Documentação para IA

### **Contexto do Projeto:**
- **Stack:** Java 17 + Spring Boot 3.2 + PostgreSQL + Redis
- **Arquitetura:** Clean Architecture (Controller → Service → Repository)
- **Padrões:** Builder Pattern, Java Records para DTOs, Cache-First

### **Convenções de Código:**
```java
// Padrão para Services
@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
public class TokenServiceImpl implements TokenService {
    
    private final ConfirmationTokenRepository tokenRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    
    @Override
    public String generateToken(NewsletterSubscriber subscriber) {
        // Implementação seguindo padrões do projeto
    }
    
    @Scheduled(cron = "0 0 2 * * ?") // 2:00 AM daily
    public void cleanupExpiredTokens() {
        // Limpeza automática
    }
}
```

### **Implementação Esperada:**
- Usar UUID para tokens únicos
- Implementar cache Redis com TTL
- Scheduled task para limpeza noturna
- Logs detalhados para auditoria
- Tratamento de exceções customizadas

### **Exemplos de Código Existente:**
- **Referência 1:** src/main/java/com/blog/api/service/impl/PostServiceImpl.java (padrão service)
- **Referência 2:** src/main/java/com/blog/api/repository/PostRepository.java (padrão repository)

## ⚙️ Configuration & Setup

### **Properties/Configuration:**
```yaml
# application.yml changes
newsletter:
  token:
    expiration-hours: 24
    cleanup-cron: "0 0 2 * * ?"
    cache-ttl: 86400 # 24 hours

spring:
  data:
    redis:
      timeout: 2000ms
```

### **Dependencies:**
```xml
<!-- Dependências existentes -->
<!-- spring-boot-starter-data-redis -->
<!-- spring-boot-starter-cache -->
```

## 🔍 Validação e Testes

### **Como Testar:**
1. Gerar token para subscriber
2. Validar token dentro do prazo
3. Testar token expirado
4. Verificar invalidação após uso
5. Testar limpeza automática
6. Validar cache Redis

### **Critérios de Sucesso:**
- [ ] Tokens únicos gerados corretamente
- [ ] Validação respeitando expiração
- [ ] Cache funcionando adequadamente
- [ ] Scheduled task executando
- [ ] Performance adequada (< 100ms)

### **Comandos de Teste:**
```bash
# Testes unitários específicos
mvn test -Dtest="TokenServiceTest"

# Testes de integração
mvn test -Dtest="TokenServiceIntegrationTest"
```

## ✅ Definition of Done

### **Código:**
- [ ] Interface TokenService definida
- [ ] TokenServiceImpl implementado
- [ ] ConfirmationTokenRepository criado
- [ ] Scheduled task configurado
- [ ] Cache Redis implementado

### **Testes:**
- [ ] Testes unitários passando (≥ 90% cobertura)
- [ ] Testes de integração passando
- [ ] Testes de performance adequados

### **Documentação:**
- [ ] Javadoc completo para métodos públicos
- [ ] Documentação de configuração
- [ ] Logs apropriados implementados

### **Quality Gates:**
- [ ] Performance < 100ms para operações
- [ ] Memory usage adequado
- [ ] Cache hit rate > 80%

## 📊 Métricas

### **Estimativa vs Real:**
- **Estimativa:** 4 horas
- **Real:** ___ horas *(a ser preenchido após implementação)*

### **Complexidade:**
- **Estimada:** Alta
- **Real:** _____ *(a ser preenchido após implementação)*

## 📝 Notas de Implementação
*[Este espaço será preenchido durante a implementação]*

## 📊 Status Tracking

### **Status Atual:**
- [x] 📋 **Todo** - Não iniciada
- [ ] 🔄 **In Progress** - Em desenvolvimento  
- [ ] 👀 **Code Review** - Aguardando revisão
- [ ] ✅ **Done** - Concluída e validada

### **Next Steps:**
- Tarefa 11: Criar Template HTML Confirmação
- Tarefa 12: Implementar EmailService Newsletter

---

**Criado em:** Agosto 2025  
**Última Atualização:** Agosto 2025  
**Responsável:** AI-Driven Development