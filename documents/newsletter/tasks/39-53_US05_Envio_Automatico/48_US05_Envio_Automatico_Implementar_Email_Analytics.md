# 48_US05_Envio_Automatico_Implementar_Email_Analytics.md

## 📋 Contexto da Tarefa
- **User Story:** US05 - Envio Automático de Newsletter para Novos Posts
- **Número da Tarefa:** 48/96
- **Complexidade:** Média
- **Estimativa:** 4 horas
- **Dependências:** Tarefas 39, 40, 41, 42, 45
- **Sprint:** Sprint 2

## 🎯 Objetivo
Implementar sistema de analytics para tracking de emails enviados, taxa de abertura, clicks, bounces e unsubscribes, fornecendo métricas detalhadas para otimização das campanhas.

## 📝 Especificação Técnica

### **Componentes a Implementar:**
- [ ] EmailAnalyticsService para coletar métricas
- [ ] EmailDeliveryStatus entity para tracking
- [ ] Pixel tracking para abertura de emails
- [ ] Link tracking para clicks
- [ ] Dashboard endpoint para visualização
- [ ] Scheduled job para gerar relatórios

### **Integrações Necessárias:**
- **Com EmailService:** Registrar envios
- **Com BulkEmailService:** Coletar métricas em massa
- **Com Templates:** Inserir pixels e links tracking

## ✅ Acceptance Criteria
- [ ] **AC1:** Sistema deve registrar todos os emails enviados
- [ ] **AC2:** Tracking de abertura via pixel invisível
- [ ] **AC3:** Tracking de clicks em links do email
- [ ] **AC4:** Dashboard com métricas agregadas
- [ ] **AC5:** Relatórios automáticos gerados diariamente

## 🧪 Testes Requeridos

### **Testes Unitários:**
- [ ] Teste de registro de email enviado
- [ ] Teste de tracking de abertura
- [ ] Teste de tracking de clicks
- [ ] Teste de cálculo de métricas

### **Testes de Integração:**
- [ ] Teste end-to-end de analytics
- [ ] Teste de dashboard APIs
- [ ] Teste de geração de relatórios

## 🔗 Arquivos Afetados
- [ ] **src/main/java/com/newsletter/service/EmailAnalyticsService.java:** Serviço de analytics
- [ ] **src/main/java/com/newsletter/entity/EmailDeliveryStatus.java:** Entity de tracking
- [ ] **src/main/java/com/newsletter/controller/AnalyticsController.java:** Controller para tracking
- [ ] **src/main/java/com/newsletter/controller/AdminAnalyticsController.java:** Dashboard admin
- [ ] **src/main/java/com/newsletter/dto/EmailAnalyticsReport.java:** DTO para relatórios
- [ ] **src/main/resources/db/migration/V7__add_email_analytics.sql:** Migration para analytics
- [ ] **src/main/resources/templates/email/fragments/tracking.html:** Fragment de tracking

## 📚 Documentação para IA

### **Contexto do Projeto:**
- **Stack:** Java 17 + Spring Boot 3.2 + PostgreSQL + Redis
- **Arquitetura:** Clean Architecture (Controller → Service → Repository)
- **Padrões:** Builder Pattern, Java Records para DTOs, Cache-First

### **Convenções de Código:**
```java
@Entity
@Table(name = "email_delivery_status")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailDeliveryStatus {
    
    @Id
    private String id;
    
    private String subscriberId;
    private String postId;
    private String emailSubject;
    
    @Enumerated(EnumType.STRING)
    private DeliveryStatus status;
    
    private LocalDateTime sentAt;
    private LocalDateTime openedAt;
    private LocalDateTime clickedAt;
    private Integer clickCount;
    
    private String userAgent;
    private String ipAddress;
}

@Service
public class EmailAnalyticsService {
    
    public void recordEmailSent(String emailId, String subscriberId, String postId) {
        EmailDeliveryStatus status = EmailDeliveryStatus.builder()
            .id(emailId)
            .subscriberId(subscriberId)
            .postId(postId)
            .status(DeliveryStatus.SENT)
            .sentAt(LocalDateTime.now())
            .build();
        
        deliveryStatusRepository.save(status);
    }
    
    public void recordEmailOpened(String emailId, String userAgent, String ipAddress) {
        // Implementação de tracking de abertura
    }
    
    public EmailAnalyticsReport generateReport(LocalDate startDate, LocalDate endDate) {
        // Implementação de relatório
    }
}

public record EmailAnalyticsReport(
    int totalEmailsSent,
    int totalOpened,
    int totalClicked,
    int totalBounced,
    int totalUnsubscribed,
    double openRate,
    double clickRate,
    double bounceRate,
    double unsubscribeRate,
    Map<String, Object> additionalMetrics
) {}
```

### **Implementação Esperada:**
- Usar UUID para tracking IDs únicos
- Implementar pixel tracking com 1x1 transparent GIF
- Usar URL shortening para link tracking
- Implementar rate limiting para evitar bot tracking
- Usar Redis para cache de métricas frequentes

## ⚙️ Configuration & Setup

### **Database Changes:**
```sql
-- Migration script necessária
CREATE TABLE email_delivery_status (
    id VARCHAR(255) PRIMARY KEY,
    subscriber_id VARCHAR(255) NOT NULL,
    post_id VARCHAR(255),
    email_subject VARCHAR(500),
    status VARCHAR(50) NOT NULL,
    sent_at TIMESTAMP NOT NULL,
    opened_at TIMESTAMP,
    clicked_at TIMESTAMP,
    click_count INTEGER DEFAULT 0,
    user_agent TEXT,
    ip_address VARCHAR(45),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (subscriber_id) REFERENCES newsletter_subscribers(id)
);

CREATE INDEX idx_email_delivery_subscriber ON email_delivery_status(subscriber_id);
CREATE INDEX idx_email_delivery_sent_at ON email_delivery_status(sent_at);
CREATE INDEX idx_email_delivery_status ON email_delivery_status(status);
```

### **Properties/Configuration:**
```yaml
# application.yml changes
newsletter:
  analytics:
    enabled: true
    tracking:
      pixel-endpoint: "/newsletter/track/open"
      click-endpoint: "/newsletter/track/click"
      base-url: "${app.base-url}"
    reports:
      schedule: "0 0 6 * * *"  # Daily at 6 AM
      retention-days: 365
    cache:
      metrics-ttl: 300  # 5 minutes
```

## 🔍 Validação e Testes

### **Como Testar:**
1. Enviar email e verificar registro no banco
2. Abrir email em cliente e verificar tracking de abertura
3. Clicar em link e verificar tracking de click
4. Verificar dashboard com métricas atualizadas

### **Critérios de Sucesso:**
- [ ] Emails são registrados no momento do envio
- [ ] Pixel tracking funciona em clientes de email
- [ ] Link tracking redireciona corretamente
- [ ] Dashboard mostra métricas precisas

### **Comandos de Teste:**
```bash
# Testes unitários específicos
mvn test -Dtest="*Analytics*Test"

# Testes de integração
mvn test -Dtest="*EmailAnalytics*IntegrationTest"

# Testar endpoints de tracking
curl http://localhost:8080/newsletter/track/open?id=email-123
```

## ✅ Definition of Done

### **Código:**
- [ ] EmailAnalyticsService implementado
- [ ] EmailDeliveryStatus entity criada
- [ ] Controllers de tracking implementados
- [ ] Dashboard APIs funcionando

### **Testes:**
- [ ] Testes unitários para analytics
- [ ] Testes de integração end-to-end
- [ ] Testes de performance de tracking
- [ ] Cobertura de código ≥ 85%

### **Documentação:**
- [ ] Javadoc para APIs de analytics
- [ ] Documentação de métricas disponíveis
- [ ] Guia de interpretação de relatórios

### **Quality Gates:**
- [ ] Tracking endpoints respondem em < 100ms
- [ ] Dashboard carrega em < 2 segundos
- [ ] Métricas são atualizadas em tempo real

---

**Criado em:** 2025-08-04
**Última Atualização:** 2025-08-04
**Responsável:** AI-Driven Development