# 07_US01_Inscricao_Usuarios_Implementar_Testes_Unitarios_Integracao.md

## 📋 Contexto da Tarefa
- **User Story:** US01 - Inscrição de Usuários
- **Número da Tarefa:** 07/95
- **Complexidade:** Alta
- **Estimativa:** 5 horas
- **Dependências:** Tarefas 01-06
- **Sprint:** Sprint 1

## 🎯 Objetivo
Implementar suite completa de testes unitários e de integração para toda a funcionalidade de inscrição da newsletter, garantindo cobertura ≥ 85% e validando todos os cenários dos Acceptance Criteria.

## 📝 Especificação Técnica

### **Testes Unitários a Implementar:**
- [ ] NewsletterSubscriberTest (entity)
- [ ] NewsletterSubscriptionRequestTest (DTO)
- [ ] NewsletterSubscriberRepositoryTest
- [ ] NewsletterServiceTest
- [ ] UniqueEmailValidatorTest

### **Testes de Integração a Implementar:**
- [ ] NewsletterControllerIntegrationTest
- [ ] NewsletterServiceIntegrationTest
- [ ] NewsletterRepositoryIntegrationTest

### **Exemplos de Testes:**
```java
@ExtendWith(MockitoExtension.class)
class NewsletterServiceTest {
    
    @Mock private NewsletterSubscriberRepository subscriberRepository;
    @InjectMocks private NewsletterService newsletterService;
    
    @Test
    @DisplayName("Should subscribe new email successfully")
    void shouldSubscribeNewEmailSuccessfully() {
        // Given, When, Then
    }
    
    @Test
    @DisplayName("Should throw exception when email already subscribed")
    void shouldThrowExceptionWhenEmailAlreadySubscribed() {
        // Given, When, Then
    }
}

@SpringBootTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
class NewsletterControllerIntegrationTest {
    
    @Autowired private TestRestTemplate restTemplate;
    
    @Test
    void shouldCreateSubscriptionSuccessfully() {
        // Integration test
    }
}
```

## ✅ Acceptance Criteria
- [ ] **AC1:** Cobertura de testes ≥ 85% para todos os componentes
- [ ] **AC2:** Todos os cenários happy path testados
- [ ] **AC3:** Todos os cenários de erro testados
- [ ] **AC4:** Testes de integração end-to-end funcionando
- [ ] **AC5:** Testes passando em ambiente CI/CD
- [ ] **AC6:** Performance tests básicos implementados

## 🧪 Cenários de Teste

### **Happy Path:**
- [ ] Inscrição com email válido novo
- [ ] Reinscrição após unsubscribe
- [ ] Captura correta de IP/User-Agent
- [ ] Logging de consentimento LGPD

### **Error Cases:**
- [ ] Email já inscrito (CONFIRMED)
- [ ] Email já inscrito (PENDING)
- [ ] Formato de email inválido
- [ ] Campos obrigatórios null/blank
- [ ] Consentimento false/null

## 🔗 Arquivos Afetados
- [ ] **src/test/java/com/blog/api/entity/NewsletterSubscriberTest.java**
- [ ] **src/test/java/com/blog/api/dto/NewsletterSubscriptionRequestTest.java**
- [ ] **src/test/java/com/blog/api/repository/NewsletterSubscriberRepositoryTest.java**
- [ ] **src/test/java/com/blog/api/service/NewsletterServiceTest.java**
- [ ] **src/test/java/com/blog/api/controller/NewsletterControllerIntegrationTest.java**
- [ ] **src/test/java/com/blog/api/validation/UniqueEmailValidatorTest.java**

## ✅ Definition of Done
- [ ] Todos os testes unitários implementados e passando
- [ ] Todos os testes de integração implementados e passando
- [ ] Cobertura ≥ 85% atingida
- [ ] Testes de performance básicos implementados
- [ ] Relatório JaCoCo gerado
- [ ] CI/CD pipeline executando testes com sucesso

---

**Criado em:** Agosto 2025