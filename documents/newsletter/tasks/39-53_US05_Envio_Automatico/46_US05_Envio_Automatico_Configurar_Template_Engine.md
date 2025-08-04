# 46_US05_Envio_Automatico_Configurar_Template_Engine.md

## 📋 Contexto da Tarefa
- **User Story:** US05 - Envio Automático de Newsletter para Novos Posts
- **Número da Tarefa:** 46/96
- **Complexidade:** Média
- **Estimativa:** 4 horas
- **Dependências:** Tarefas 39, 40, 41, 42
- **Sprint:** Sprint 2

## 🎯 Objetivo
Configurar engine de templates Thymeleaf para geração dinâmica de emails HTML personalizados, permitindo templates reutilizáveis com placeholders para dados do post e informações do subscriber.

## 📝 Especificação Técnica

### **Componentes a Implementar:**
- [ ] Configurar Thymeleaf para email templates
- [ ] Criar TemplateService para renderização
- [ ] Implementar EmailTemplateContext para dados
- [ ] Criar templates base reutilizáveis
- [ ] Implementar sistema de template versioning
- [ ] Configurar cache de templates

### **Integrações Necessárias:**
- **Com Thymeleaf:** Configurar engine para email context
- **Com EmailService:** Integrar renderização de templates
- **Com Spring Boot:** Configurar template resolver

## ✅ Acceptance Criteria
- [ ] **AC1:** Templates devem ser renderizados dinamicamente com dados do post
- [ ] **AC2:** Suporte a layouts base e fragments reutilizáveis
- [ ] **AC3:** Templates devem ser responsivos para diferentes clientes de email
- [ ] **AC4:** Sistema deve suportar personalização por subscriber
- [ ] **AC5:** Cache de templates deve melhorar performance

## 🧪 Testes Requeridos

### **Testes Unitários:**
- [ ] Teste de renderização de template básico
- [ ] Teste de substituição de placeholders
- [ ] Teste de layouts e fragments
- [ ] Teste de cache de templates

### **Testes de Integração:**
- [ ] Teste de renderização end-to-end
- [ ] Teste de templates com dados reais
- [ ] Teste de performance de cache

## 🔗 Arquivos Afetados
- [ ] **src/main/java/com/newsletter/config/ThymeleafEmailConfig.java:** Configuração Thymeleaf
- [ ] **src/main/java/com/newsletter/service/TemplateService.java:** Serviço de templates
- [ ] **src/main/java/com/newsletter/dto/EmailTemplateContext.java:** Context para dados
- [ ] **src/main/resources/templates/email/:** Diretório para templates
- [ ] **src/main/resources/templates/email/layouts/base.html:** Layout base
- [ ] **src/main/resources/templates/email/fragments/:** Fragments reutilizáveis
- [ ] **src/main/resources/application.yml:** Configurações Thymeleaf

## 📚 Documentação para IA

### **Contexto do Projeto:**
- **Stack:** Java 17 + Spring Boot 3.2 + PostgreSQL + Redis
- **Arquitetura:** Clean Architecture (Controller → Service → Repository)
- **Padrões:** Builder Pattern, Java Records para DTOs, Cache-First

### **Convenções de Código:**
```java
@Configuration
public class ThymeleafEmailConfig {
    
    @Bean(name = "emailTemplateEngine")
    public SpringTemplateEngine emailTemplateEngine() {
        SpringTemplateEngine templateEngine = new SpringTemplateEngine();
        templateEngine.addTemplateResolver(emailTemplateResolver());
        templateEngine.addDialect(new LayoutDialect());
        return templateEngine;
    }
    
    @Bean
    public ITemplateResolver emailTemplateResolver() {
        ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver();
        templateResolver.setPrefix("templates/email/");
        templateResolver.setSuffix(".html");
        templateResolver.setTemplateMode(TemplateMode.HTML);
        templateResolver.setCharacterEncoding("UTF-8");
        templateResolver.setCacheable(true);
        return templateResolver;
    }
}

@Service
public class TemplateService {
    
    @Qualifier("emailTemplateEngine")
    private final SpringTemplateEngine templateEngine;
    
    public String renderTemplate(String templateName, EmailTemplateContext context) {
        Context thymeleafContext = new Context();
        thymeleafContext.setVariables(context.toMap());
        return templateEngine.process(templateName, thymeleafContext);
    }
}

public record EmailTemplateContext(
    String subscriberName,
    String subscriberEmail,
    String postTitle,
    String postContent,
    String postUrl,
    LocalDateTime publishedAt,
    String unsubscribeUrl
) {
    public Map<String, Object> toMap() {
        // Converter record para Map para Thymeleaf
    }
}
```

### **Implementação Esperada:**
- Usar Thymeleaf com Spring Boot integration
- Criar layout base com header/footer padrão
- Implementar fragments para componentes reutilizáveis
- Configurar cache apropriado para templates
- Usar Context pattern para passar dados

### **Exemplos de Código Existente:**
- **Referência 1:** EmailService.java para integração
- **Referência 2:** Configurações Spring Boot existentes

## ⚙️ Configuration & Setup

### **Properties/Configuration:**
```yaml
# application.yml changes
spring:
  thymeleaf:
    enabled: true
    cache: true
    check-template: true
    check-template-location: true
    enable-spring-el-compiler: true
    encoding: UTF-8
    mode: HTML
    prefix: classpath:/templates/
    suffix: .html

newsletter:
  templates:
    cache:
      enabled: true
      size: 100
      ttl: 3600  # 1 hour
    default:
      layout: "layouts/base"
      new-post: "new-post"
      weekly-digest: "weekly-digest"
```

### **Dependencies:**
```xml
<!-- pom.xml dependencies -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-thymeleaf</artifactId>
</dependency>
<dependency>
    <groupId>nz.net.ultraq.thymeleaf</groupId>
    <artifactId>thymeleaf-layout-dialect</artifactId>
</dependency>
```

## 🔍 Validação e Testes

### **Como Testar:**
1. Criar template de teste e renderizar com dados mock
2. Verificar HTML gerado está bem formado
3. Testar responsividade em diferentes clientes de email
4. Validar cache funcionando corretamente

### **Critérios de Sucesso:**
- [ ] Template renderiza HTML válido
- [ ] Placeholders são substituídos corretamente
- [ ] Layout base e fragments funcionam
- [ ] Performance melhorada com cache

### **Comandos de Teste:**
```bash
# Testes unitários específicos
mvn test -Dtest="*Template*Test"

# Testes de integração
mvn test -Dtest="*EmailTemplate*IntegrationTest"

# Validar HTML gerado
# Use HTML validator online ou tools
```

## ✅ Definition of Done

### **Código:**
- [ ] ThymeleafEmailConfig implementado
- [ ] TemplateService funcionando
- [ ] Templates base criados
- [ ] Cache configurado e funcionando

### **Testes:**
- [ ] Testes unitários para template rendering
- [ ] Testes de integração end-to-end
- [ ] Testes de performance de cache
- [ ] Cobertura de código ≥ 85%

### **Documentação:**
- [ ] Javadoc para APIs de template
- [ ] Documentação de como criar novos templates
- [ ] Exemplos de uso dos fragments

### **Quality Gates:**
- [ ] Template rendering < 100ms
- [ ] HTML válido segundo W3C validator
- [ ] Compatibilidade com principais clientes de email

## 📊 Métricas

### **Estimativa vs Real:**
- **Estimativa:** 4 horas
- **Real:** ___ horas *(a ser preenchido após implementação)*

### **Complexidade:**
- **Estimada:** Média
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