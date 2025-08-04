# 44_US05_Envio_Automatico_Configurar_Redis_Queue.md

## 📋 Contexto da Tarefa
- **User Story:** US05 - Envio Automático de Newsletter para Novos Posts
- **Número da Tarefa:** 44/96
- **Complexidade:** Alta
- **Estimativa:** 6 horas
- **Dependências:** Tarefas 39, 40, 41, 42, 43
- **Sprint:** Sprint 2

## 🎯 Objetivo
Configurar sistema de filas Redis para processar envios de email de forma resiliente e escalável, permitindo retry automático em caso de falhas e garantindo que todos os emails sejam eventualmente enviados.

## 📝 Especificação Técnica

### **Componentes a Implementar:**
- [ ] Configurar RedisTemplate para sistema de filas
- [ ] Implementar EmailQueueService para gerenciar filas
- [ ] Criar EmailQueueMessage record para estrutura de dados
- [ ] Implementar consumer para processar mensagens da fila
- [ ] Configurar retry mechanism com exponential backoff
- [ ] Implementar dead letter queue para falhas persistentes

### **Integrações Necessárias:**
- **Com Redis:** Configurar conexão e serialização
- **Com EmailService:** Integrar processamento via fila
- **Com NewsletterEventListener:** Enfileirar ao invés de processar diretamente

## ✅ Acceptance Criteria
- [ ] **AC1:** Mensagens de email devem ser persistidas em fila Redis
- [ ] **AC2:** Sistema deve processar mensagens em ordem FIFO
- [ ] **AC3:** Falhas devem trigger retry automático com exponential backoff
- [ ] **AC4:** Após 3 tentativas, mensagens devem ir para dead letter queue
- [ ] **AC5:** Deve existir endpoint admin para monitorar status das filas

## 🧪 Testes Requeridos

### **Testes Unitários:**
- [ ] Teste de enfileiramento de mensagens
- [ ] Teste de processamento de mensagens
- [ ] Teste de retry mechanism
- [ ] Teste de dead letter queue

### **Testes de Integração:**
- [ ] Teste end-to-end de publish → queue → process → email
- [ ] Teste de falha de SMTP e recovery
- [ ] Teste de performance com alta carga

### **Testes de Performance:**
- [ ] Throughput de mensagens processadas por segundo
- [ ] Latência entre enfileiramento e processamento

## 🔗 Arquivos Afetados
- [ ] **src/main/java/com/newsletter/config/RedisQueueConfig.java:** Configuração Redis para filas
- [ ] **src/main/java/com/newsletter/service/EmailQueueService.java:** Serviço de gerenciamento de filas
- [ ] **src/main/java/com/newsletter/dto/EmailQueueMessage.java:** Record para estrutura de mensagens
- [ ] **src/main/java/com/newsletter/consumer/EmailQueueConsumer.java:** Consumer para processar fila
- [ ] **src/main/java/com/newsletter/listener/NewsletterEventListener.java:** Integrar com sistema de filas
- [ ] **src/main/resources/application.yml:** Configurações Redis e fila

## 📚 Documentação para IA

### **Contexto do Projeto:**
- **Stack:** Java 17 + Spring Boot 3.2 + PostgreSQL + Redis
- **Arquitetura:** Clean Architecture (Controller → Service → Repository)
- **Padrões:** Builder Pattern, Java Records para DTOs, Cache-First

### **Convenções de Código:**
```java
@Configuration
public class RedisQueueConfig {
    
    @Bean
    public RedisTemplate<String, Object> redisQueueTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        return template;
    }
}

public record EmailQueueMessage(
    String subscriberId,
    String postId,
    String subject,
    String content,
    LocalDateTime enqueuedAt,
    Integer retryCount
) {}

@Service
public class EmailQueueService {
    
    private static final String EMAIL_QUEUE = "newsletter:email:queue";
    private static final String EMAIL_DLQ = "newsletter:email:dlq";
    
    public void enqueueEmail(EmailQueueMessage message) {
        // Implementação de enfileiramento
    }
    
    @EventListener
    public void processEmailQueue() {
        // Implementação de processamento
    }
}
```

### **Implementação Esperada:**
- Usar Spring Data Redis com RedisTemplate
- Implementar padrão Producer-Consumer
- Configurar serialização JSON para mensagens
- Implementar retry com exponential backoff
- Usar Redis Lists para FIFO queue behavior

### **Exemplos de Código Existente:**
- **Referência 1:** Configuração Redis existente no projeto
- **Referência 2:** EmailService.java para integração com queue processing

## ⚙️ Configuration & Setup

### **Properties/Configuration:**
```yaml
# application.yml changes
spring:
  data:
    redis:
      host: ${REDIS_HOST:localhost}
      port: ${REDIS_PORT:6379}
      password: ${REDIS_PASSWORD:}
      database: 1
      timeout: 2000ms
      jedis:
        pool:
          max-active: 20
          max-idle: 10
          min-idle: 5

newsletter:
  queue:
    email:
      name: "newsletter:email:queue"
      dlq-name: "newsletter:email:dlq"
      batch-size: 10
      poll-interval: 1000  # 1 second
      max-retries: 3
      retry-delay-base: 1000  # 1 second base delay
      retry-delay-multiplier: 2
```

### **Dependencies:**
```xml
<!-- pom.xml dependencies -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>
<dependency>
    <groupId>redis.clients</groupId>
    <artifactId>jedis</artifactId>
</dependency>
```

## 🔍 Validação e Testes

### **Como Testar:**
1. Publicar post e verificar que mensagem é enfileirada no Redis
2. Verificar processamento automático da fila
3. Simular falha de SMTP e verificar retry mechanism
4. Verificar mensagens na dead letter queue após max retries

### **Critérios de Sucesso:**
- [ ] Redis CLI mostra mensagens sendo enfileiradas
- [ ] Logs mostram processamento sequencial das mensagens
- [ ] Retry mechanism funciona conforme configurado
- [ ] Dead letter queue recebe mensagens após falhas persistentes

### **Comandos de Teste:**
```bash
# Verificar fila no Redis
redis-cli LLEN newsletter:email:queue

# Testes unitários específicos
mvn test -Dtest="*EmailQueue*Test"

# Testes de integração
mvn test -Dtest="*QueueIntegration*Test"
```

## ✅ Definition of Done

### **Código:**
- [ ] RedisQueueConfig implementado
- [ ] EmailQueueService com enqueue/dequeue logic
- [ ] Consumer implementado com retry mechanism
- [ ] Integration com NewsletterEventListener

### **Testes:**
- [ ] Testes unitários para queue operations
- [ ] Testes de integração end-to-end
- [ ] Testes de falha e recovery
- [ ] Cobertura de código ≥ 85%

### **Documentação:**
- [ ] Javadoc para componentes de fila
- [ ] Documentação de configuração Redis
- [ ] Runbook para monitoramento de filas

### **Quality Gates:**
- [ ] Throughput mínimo de 100 mensagens/segundo
- [ ] Retry mechanism funciona conforme especificado
- [ ] Zero perda de mensagens em cenários de falha

## 📊 Métricas

### **Estimativa vs Real:**
- **Estimativa:** 6 horas
- **Real:** ___ horas *(a ser preenchido após implementação)*

### **Complexidade:**
- **Estimada:** Alta
- **Real:** ___ *(a ser preenchido após implementação)*

## 📝 Notas de Implementação
*[Este espaço será preenchido durante a implementação com descobertas, decisões técnicas, e observações importantes]*

### **Decisões Técnicas:**
- [Decisão 1: justificativa]
- [Decisão 2: justificativa]

### **Descobertas:**
- [Descoberta 1: impacto]
- [Descoberta 2: impacto]

### **Refactorings Necessários:**
- [Refactoring 1: razão]
- [Refactoring 2: razão]

## 📊 Status Tracking

### **Status Atual:**
- [x] 📋 **Todo** - Não iniciada
- [ ] 🔄 **In Progress** - Em desenvolvimento  
- [ ] 👀 **Code Review** - Aguardando revisão
- [ ] ✅ **Done** - Concluída e validada

### **Bloqueadores:**
*[Lista de impedimentos, se houver]*

### **Next Steps:**
*[Próximos passos após conclusão desta tarefa]*

---

**Criado em:** 2025-08-04
**Última Atualização:** 2025-08-04
**Responsável:** AI-Driven Development  
**Reviewer:** [Nome do reviewer, se aplicável]