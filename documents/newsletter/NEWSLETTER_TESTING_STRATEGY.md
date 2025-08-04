# 🧪 Newsletter Testing Strategy - Comprehensive Test Plan

**Projeto:** Blog API REST - Newsletter System  
**Cobertura Alvo:** 85%+ de code coverage  
**Tipos de Teste:** Unit, Integration, E2E, Performance, Security  

---

## 🎯 Estratégia de Testes

### **Filosofia de Testing**
- **Test-Driven Development (TDD)**: Testes escritos antes da implementação
- **Pyramid Testing**: Muitos unit tests, alguns integration tests, poucos E2E tests
- **Behavior-Driven Development (BDD)**: Testes refletem Acceptance Criteria
- **Continuous Testing**: Testes executados em cada commit/push
- **Fail-Fast**: Identificação rápida de regressões

### **Cobertura por Camada**
```
┌─────────────────┐ 10% - E2E Tests (Full flow)
│   E2E Tests     │
├─────────────────┤ 20% - Integration Tests  
│ Integration     │ (Controller + Service + Repository)
├─────────────────┤ 70% - Unit Tests
│   Unit Tests    │ (Service + Repository + Utils)
└─────────────────┘
```

---

## 🔍 Testes por User Story

### **US01 - Inscrição de Usuários (5 pts)**

#### **Unit Tests - NewsletterService**

```java
@ExtendWith(MockitoExtension.class)
class NewsletterServiceTest {
    
    @Mock private NewsletterSubscriberRepository subscriberRepository;
    @Mock private NewsletterConsentLogRepository consentLogRepository;
    @Mock private EmailService emailService;
    @Mock private NewsletterTokenService tokenService;
    
    @InjectMocks private NewsletterService newsletterService;
    
    @Test
    @DisplayName("Should subscribe new email successfully")
    void shouldSubscribeNewEmailSuccessfully() {
        // Given
        String email = "test@example.com";
        NewsletterSubscriptionRequest request = new NewsletterSubscriptionRequest(
            email, true, "v1.0", "192.168.1.1", "Mozilla/5.0"
        );
        
        when(subscriberRepository.findByEmail(email)).thenReturn(Optional.empty());
        when(subscriberRepository.save(any(NewsletterSubscriber.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));
        when(tokenService.generateConfirmationToken(email)).thenReturn("token-123");
        
        // When
        NewsletterSubscriptionResponse response = newsletterService.subscribe(request);
        
        // Then
        assertThat(response.email()).isEqualTo(email);
        assertThat(response.status()).isEqualTo(SubscriptionStatus.PENDING);
        assertThat(response.message()).contains("sucesso");
        
        verify(subscriberRepository).save(argThat(subscriber -> 
            subscriber.getEmail().equals(email) &&
            subscriber.getStatus() == SubscriptionStatus.PENDING &&
            subscriber.getConsentIpAddress().equals("192.168.1.1")
        ));
        
        verify(consentLogRepository).save(argThat(log ->
            log.getEmail().equals(email) &&
            log.getAction() == ConsentAction.SUBSCRIBE
        ));
        
        verify(emailService).sendNewsletterConfirmation(email, "token-123");
    }
    
    @Test
    @DisplayName("Should throw exception when email already subscribed")
    void shouldThrowExceptionWhenEmailAlreadySubscribed() {
        // Given
        String email = "existing@example.com";
        NewsletterSubscriptionRequest request = new NewsletterSubscriptionRequest(
            email, true, "v1.0", "192.168.1.1", "Mozilla/5.0"
        );
        
        NewsletterSubscriber existingSubscriber = NewsletterSubscriber.builder()
            .email(email)
            .status(SubscriptionStatus.CONFIRMED)
            .build();
            
        when(subscriberRepository.findByEmail(email))
            .thenReturn(Optional.of(existingSubscriber));
        
        // When & Then
        assertThatThrownBy(() -> newsletterService.subscribe(request))
            .isInstanceOf(BadRequestException.class)
            .hasMessageContaining("já está inscrito");
            
        verify(subscriberRepository, never()).save(any());
        verify(emailService, never()).sendNewsletterConfirmation(anyString(), anyString());
    }
    
    @Test
    @DisplayName("Should allow resubscription for unsubscribed email")
    void shouldAllowResubscriptionForUnsubscribedEmail() {
        // Given
        String email = "unsubscribed@example.com";
        NewsletterSubscriptionRequest request = new NewsletterSubscriptionRequest(
            email, true, "v1.0", "192.168.1.1", "Mozilla/5.0"
        );
        
        NewsletterSubscriber unsubscribedSubscriber = NewsletterSubscriber.builder()
            .email(email)
            .status(SubscriptionStatus.UNSUBSCRIBED)
            .build();
            
        when(subscriberRepository.findByEmail(email))
            .thenReturn(Optional.of(unsubscribedSubscriber));
        when(subscriberRepository.save(any(NewsletterSubscriber.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));
        
        // When
        NewsletterSubscriptionResponse response = newsletterService.subscribe(request);
        
        // Then
        assertThat(response.status()).isEqualTo(SubscriptionStatus.PENDING);
        verify(subscriberRepository).save(any(NewsletterSubscriber.class));
    }
}
```

#### **Integration Tests - NewsletterController**

```java
@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(locations = "classpath:application-test.yml")
@Transactional
class NewsletterControllerIntegrationTest {
    
    @Autowired private TestRestTemplate restTemplate;
    @Autowired private NewsletterSubscriberRepository subscriberRepository;
    @Autowired private NewsletterConsentLogRepository consentLogRepository;
    
    @Test
    @DisplayName("POST /api/newsletter/subscribe - Should create subscription successfully")
    void shouldCreateSubscriptionSuccessfully() {
        // Given
        NewsletterSubscriptionRequest request = new NewsletterSubscriptionRequest(
            "integration@test.com", true, "v1.0", null, null
        );
        
        // When
        ResponseEntity<NewsletterSubscriptionResponse> response = restTemplate.postForEntity(
            "/api/newsletter/subscribe", request, NewsletterSubscriptionResponse.class
        );
        
        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
        assertThat(response.getBody().email()).isEqualTo("integration@test.com");
        assertThat(response.getBody().status()).isEqualTo(SubscriptionStatus.PENDING);
        
        // Verify database
        Optional<NewsletterSubscriber> saved = subscriberRepository.findByEmail("integration@test.com");
        assertThat(saved).isPresent();
        assertThat(saved.get().getStatus()).isEqualTo(SubscriptionStatus.PENDING);
        assertThat(saved.get().getConsentGivenAt()).isNotNull();
        
        // Verify consent log
        List<NewsletterConsentLog> logs = consentLogRepository.findByEmailOrderByCreatedAtDesc("integration@test.com");
        assertThat(logs).hasSize(1);
        assertThat(logs.get(0).getAction()).isEqualTo(ConsentAction.SUBSCRIBE);
    }
    
    @Test
    @DisplayName("POST /api/newsletter/subscribe - Should return 409 for duplicate email")
    void shouldReturn409ForDuplicateEmail() {
        // Given
        String email = "duplicate@test.com";
        NewsletterSubscriber existing = NewsletterSubscriber.builder()
            .email(email)
            .status(SubscriptionStatus.CONFIRMED)
            .subscribedAt(LocalDateTime.now())
            .consentGivenAt(LocalDateTime.now())
            .build();
        subscriberRepository.save(existing);
        
        NewsletterSubscriptionRequest request = new NewsletterSubscriptionRequest(
            email, true, "v1.0", null, null
        );
        
        // When
        ResponseEntity<ErrorResponse> response = restTemplate.postForEntity(
            "/api/newsletter/subscribe", request, ErrorResponse.class
        );
        
        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody().getMessage()).contains("já está inscrito");
    }
    
    @Test
    @DisplayName("POST /api/newsletter/subscribe - Should validate email format")
    void shouldValidateEmailFormat() {
        // Given
        NewsletterSubscriptionRequest request = new NewsletterSubscriptionRequest(
            "invalid-email", true, "v1.0", null, null
        );
        
        // When
        ResponseEntity<ValidationErrorResponse> response = restTemplate.postForEntity(
            "/api/newsletter/subscribe", request, ValidationErrorResponse.class
        );
        
        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().getFieldErrors()).containsKey("email");
    }
}
```

---

### **US02 - Confirmação de E-mail (5 pts)**

#### **Unit Tests - Token Validation**

```java
@ExtendWith(MockitoExtension.class)
class NewsletterConfirmationTest {
    
    @Mock private NewsletterTokenRepository tokenRepository;
    @Mock private NewsletterSubscriberRepository subscriberRepository;
    
    @InjectMocks private NewsletterService newsletterService;
    
    @Test
    @DisplayName("Should confirm subscription with valid token")
    void shouldConfirmSubscriptionWithValidToken() {
        // Given
        String token = "valid-token-123";
        String email = "confirm@test.com";
        
        NewsletterToken confirmationToken = NewsletterToken.builder()
            .token(token)
            .email(email)
            .type(TokenType.CONFIRMATION)
            .expiresAt(LocalDateTime.now().plusHours(24))
            .used(false)
            .build();
            
        NewsletterSubscriber subscriber = NewsletterSubscriber.builder()
            .email(email)
            .status(SubscriptionStatus.PENDING)
            .subscribedAt(LocalDateTime.now().minusHours(1))
            .build();
        
        when(tokenRepository.findByTokenAndType(token, TokenType.CONFIRMATION))
            .thenReturn(Optional.of(confirmationToken));
        when(subscriberRepository.findByEmail(email))
            .thenReturn(Optional.of(subscriber));
        when(subscriberRepository.save(any(NewsletterSubscriber.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));
        
        // When
        NewsletterConfirmationResponse response = newsletterService.confirmSubscription(token);
        
        // Then
        assertThat(response.email()).isEqualTo(email);
        assertThat(response.status()).isEqualTo(SubscriptionStatus.CONFIRMED);
        assertThat(response.confirmedAt()).isNotNull();
        
        verify(subscriberRepository).save(argThat(sub -> 
            sub.getStatus() == SubscriptionStatus.CONFIRMED &&
            sub.getConfirmedAt() != null
        ));
        
        verify(tokenRepository).save(argThat(t -> 
            t.getUsed() == true && t.getUsedAt() != null
        ));
    }
    
    @Test
    @DisplayName("Should throw exception for expired token")
    void shouldThrowExceptionForExpiredToken() {
        // Given
        String expiredToken = "expired-token-123";
        
        NewsletterToken token = NewsletterToken.builder()
            .token(expiredToken)
            .email("test@example.com")
            .type(TokenType.CONFIRMATION)
            .expiresAt(LocalDateTime.now().minusHours(1)) // Expired
            .used(false)
            .build();
        
        when(tokenRepository.findByTokenAndType(expiredToken, TokenType.CONFIRMATION))
            .thenReturn(Optional.of(token));
        
        // When & Then
        assertThatThrownBy(() -> newsletterService.confirmSubscription(expiredToken))
            .isInstanceOf(BadRequestException.class)
            .hasMessageContaining("expirado");
    }
    
    @Test
    @DisplayName("Should throw exception for already used token")
    void shouldThrowExceptionForAlreadyUsedToken() {
        // Given
        String usedToken = "used-token-123";
        
        NewsletterToken token = NewsletterToken.builder()
            .token(usedToken)
            .email("test@example.com")
            .type(TokenType.CONFIRMATION)
            .expiresAt(LocalDateTime.now().plusHours(24))
            .used(true) // Already used
            .usedAt(LocalDateTime.now().minusMinutes(30))
            .build();
        
        when(tokenRepository.findByTokenAndType(usedToken, TokenType.CONFIRMATION))
            .thenReturn(Optional.of(token));
        
        // When & Then
        assertThatThrownBy(() -> newsletterService.confirmSubscription(usedToken))
            .isInstanceOf(BadRequestException.class)
            .hasMessageContaining("já foi utilizado");
    }
}
```

#### **Integration Test - Email Integration**

```java
@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.yml")
class NewsletterEmailIntegrationTest {
    
    @Autowired private NewsletterService newsletterService;
    @Autowired private EmailService emailService;
    @MockBean private JavaMailSender mailSender;
    
    @Test
    @DisplayName("Should send confirmation email after subscription")
    void shouldSendConfirmationEmailAfterSubscription() {
        // Given
        NewsletterSubscriptionRequest request = new NewsletterSubscriptionRequest(
            "email@test.com", true, "v1.0", "127.0.0.1", "Test-Agent"
        );
        
        // When
        newsletterService.subscribe(request);
        
        // Then
        verify(mailSender, timeout(5000)).send(any(MimeMessage.class));
    }
}
```

---

### **US03 - Descadastro (3 pts)**

#### **Unit Tests - Unsubscribe Flow**

```java
@ExtendWith(MockitoExtension.class)
class NewsletterUnsubscribeTest {
    
    @Mock private NewsletterTokenRepository tokenRepository;
    @Mock private NewsletterSubscriberRepository subscriberRepository;
    @Mock private NewsletterConsentLogRepository consentLogRepository;
    
    @InjectMocks private NewsletterService newsletterService;
    
    @Test
    @DisplayName("Should unsubscribe user successfully")
    void shouldUnsubscribeUserSuccessfully() {
        // Given
        String token = "unsubscribe-token-123";
        String email = "unsubscribe@test.com";
        
        NewsletterToken unsubToken = NewsletterToken.builder()
            .token(token)
            .email(email)
            .type(TokenType.UNSUBSCRIBE)
            .expiresAt(LocalDateTime.now().plusDays(365))
            .used(false)
            .build();
            
        NewsletterSubscriber subscriber = NewsletterSubscriber.builder()
            .email(email)
            .status(SubscriptionStatus.CONFIRMED)
            .build();
        
        when(tokenRepository.findByTokenAndType(token, TokenType.UNSUBSCRIBE))
            .thenReturn(Optional.of(unsubToken));
        when(subscriberRepository.findByEmail(email))
            .thenReturn(Optional.of(subscriber));
        
        // When
        String result = newsletterService.unsubscribe(token);
        
        // Then
        assertThat(result).contains("removido da newsletter");
        
        verify(subscriberRepository).save(argThat(sub ->
            sub.getStatus() == SubscriptionStatus.UNSUBSCRIBED &&
            sub.getUnsubscribedAt() != null
        ));
        
        verify(consentLogRepository).save(argThat(log ->
            log.getEmail().equals(email) &&
            log.getAction() == ConsentAction.UNSUBSCRIBE
        ));
    }
}
```

---

### **US04 - Administração (3 pts)**

#### **Integration Tests - Admin Endpoints**

```java
@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(locations = "classpath:application-test.yml")
class NewsletterAdminIntegrationTest {
    
    @Autowired private TestRestTemplate restTemplate;
    @Autowired private NewsletterSubscriberRepository subscriberRepository;
    
    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should list subscribers for admin user")
    void shouldListSubscribersForAdminUser() {
        // Given
        createTestSubscribers();
        
        // When
        ResponseEntity<PagedResponse<AdminNewsletterSubscriberResponse>> response = 
            restTemplate.exchange(
                "/api/newsletter/subscribers?page=0&size=10",
                HttpMethod.GET,
                new HttpEntity<>(createAuthHeaders()),
                new ParameterizedTypeReference<PagedResponse<AdminNewsletterSubscriberResponse>>() {}
            );
        
        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getContent()).hasSize(3);
    }
    
    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("Should return 403 for non-admin user")
    void shouldReturn403ForNonAdminUser() {
        // When
        ResponseEntity<String> response = restTemplate.exchange(
            "/api/newsletter/subscribers",
            HttpMethod.GET,
            new HttpEntity<>(createAuthHeaders()),
            String.class
        );
        
        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }
    
    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should filter subscribers by status")
    void shouldFilterSubscribersByStatus() {
        // Given
        createTestSubscribers();
        
        // When
        ResponseEntity<PagedResponse<AdminNewsletterSubscriberResponse>> response = 
            restTemplate.exchange(
                "/api/newsletter/subscribers?status=CONFIRMED",
                HttpMethod.GET,
                new HttpEntity<>(createAuthHeaders()),
                new ParameterizedTypeReference<PagedResponse<AdminNewsletterSubscriberResponse>>() {}
            );
        
        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getContent())
            .allSatisfy(subscriber -> 
                assertThat(subscriber.status()).isEqualTo(SubscriptionStatus.CONFIRMED)
            );
    }
    
    private void createTestSubscribers() {
        List<NewsletterSubscriber> subscribers = Arrays.asList(
            NewsletterSubscriber.builder()
                .email("confirmed@test.com")
                .status(SubscriptionStatus.CONFIRMED)
                .subscribedAt(LocalDateTime.now().minusDays(5))
                .confirmedAt(LocalDateTime.now().minusDays(4))
                .consentGivenAt(LocalDateTime.now().minusDays(5))
                .build(),
            NewsletterSubscriber.builder()
                .email("pending@test.com")
                .status(SubscriptionStatus.PENDING)
                .subscribedAt(LocalDateTime.now().minusDays(2))
                .consentGivenAt(LocalDateTime.now().minusDays(2))
                .build(),
            NewsletterSubscriber.builder()
                .email("unsubscribed@test.com")
                .status(SubscriptionStatus.UNSUBSCRIBED)
                .subscribedAt(LocalDateTime.now().minusDays(10))
                .confirmedAt(LocalDateTime.now().minusDays(9))
                .unsubscribedAt(LocalDateTime.now().minusDays(1))
                .consentGivenAt(LocalDateTime.now().minusDays(10))
                .build()
        );
        
        subscriberRepository.saveAll(subscribers);
    }
}
```

---

### **US05 - Envio Automático (8 pts)**

#### **Unit Tests - Event Handling**

```java
@ExtendWith(MockitoExtension.class)
class NewsletterEventListenerTest {
    
    @Mock private NewsletterSubscriberRepository subscriberRepository;
    @Mock private EmailService emailService;
    @Mock private NewsletterTokenService tokenService;
    @Mock private NewsletterEmailLogRepository emailLogRepository;
    
    @InjectMocks private NewsletterService newsletterService;
    
    @Test
    @DisplayName("Should send emails to confirmed subscribers on new post")
    void shouldSendEmailsToConfirmedSubscribersOnNewPost() {
        // Given
        Post newPost = Post.builder()
            .id(1L)
            .title("New Blog Post")
            .content("Content of the post")
            .published(true)
            .build();
            
        List<NewsletterSubscriber> confirmedSubscribers = Arrays.asList(
            createSubscriber("user1@test.com", SubscriptionStatus.CONFIRMED),
            createSubscriber("user2@test.com", SubscriptionStatus.CONFIRMED)
        );
        
        when(subscriberRepository.findByStatus(SubscriptionStatus.CONFIRMED))
            .thenReturn(confirmedSubscribers);
        when(tokenService.generateUnsubscribeToken(anyString()))
            .thenReturn("unsubscribe-token");
        
        PostPublishedEvent event = new PostPublishedEvent(this, newPost);
        
        // When
        newsletterService.handleNewPostPublished(event);
        
        // Then
        verify(emailService, times(2)).sendNewPostNotification(
            anyString(), eq(newPost), eq("unsubscribe-token")
        );
        
        verify(emailLogRepository, times(2)).save(argThat(log ->
            log.getEmailType() == EmailType.NEW_POST &&
            log.getPostId().equals(1L)
        ));
    }
    
    @Test
    @DisplayName("Should not send emails to pending subscribers")
    void shouldNotSendEmailsToPendingSubscribers() {
        // Given
        Post newPost = Post.builder().id(1L).title("Test Post").build();
        
        List<NewsletterSubscriber> allSubscribers = Arrays.asList(
            createSubscriber("confirmed@test.com", SubscriptionStatus.CONFIRMED),
            createSubscriber("pending@test.com", SubscriptionStatus.PENDING),
            createSubscriber("unsubscribed@test.com", SubscriptionStatus.UNSUBSCRIBED)
        );
        
        when(subscriberRepository.findByStatus(SubscriptionStatus.CONFIRMED))
            .thenReturn(allSubscribers.stream()
                .filter(s -> s.getStatus() == SubscriptionStatus.CONFIRMED)
                .collect(Collectors.toList()));
        
        PostPublishedEvent event = new PostPublishedEvent(this, newPost);
        
        // When
        newsletterService.handleNewPostPublished(event);
        
        // Then
        verify(emailService, times(1)).sendNewPostNotification(
            eq("confirmed@test.com"), eq(newPost), anyString()
        );
        verify(emailService, never()).sendNewPostNotification(
            eq("pending@test.com"), any(), anyString()
        );
        verify(emailService, never()).sendNewPostNotification(
            eq("unsubscribed@test.com"), any(), anyString()
        );
    }
    
    private NewsletterSubscriber createSubscriber(String email, SubscriptionStatus status) {
        return NewsletterSubscriber.builder()
            .email(email)
            .status(status)
            .subscribedAt(LocalDateTime.now())
            .consentGivenAt(LocalDateTime.now())
            .build();
    }
}
```

#### **Integration Test - Event Publishing**

```java
@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.yml")
class PostPublishEventIntegrationTest {
    
    @Autowired private PostService postService;
    @Autowired private NewsletterService newsletterService;
    @MockBean private EmailService emailService;
    
    @Test
    @DisplayName("Should trigger newsletter event when post is published")
    void shouldTriggerNewsletterEventWhenPostIsPublished() {
        // Given
        Post post = createTestPost();
        createConfirmedSubscribers();
        
        // When
        postService.publishPost(post.getId());
        
        // Then
        // Allow async processing to complete
        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
            verify(emailService, atLeast(1)).sendNewPostNotification(
                anyString(), any(Post.class), anyString()
            );
        });
    }
}
```

---

### **US06 - Envio Semanal (5 pts)**

#### **Unit Tests - Scheduler**

```java
@ExtendWith(MockitoExtension.class)
class NewsletterSchedulerTest {
    
    @Mock private PostRepository postRepository;
    @Mock private NewsletterSubscriberRepository subscriberRepository;
    @Mock private EmailService emailService;
    @Mock private NewsletterTokenService tokenService;
    
    @InjectMocks private NewsletterService newsletterService;
    
    @Test
    @DisplayName("Should send weekly digest with recent posts")
    void shouldSendWeeklyDigestWithRecentPosts() {
        // Given
        LocalDateTime oneWeekAgo = LocalDateTime.now().minusWeeks(1);
        List<Post> recentPosts = Arrays.asList(
            createPost("Post 1", LocalDateTime.now().minusDays(2)),
            createPost("Post 2", LocalDateTime.now().minusDays(5))
        );
        
        List<NewsletterSubscriber> subscribers = Arrays.asList(
            createSubscriber("subscriber1@test.com"),
            createSubscriber("subscriber2@test.com")
        );
        
        when(postRepository.findPublishedPostsSince(any(LocalDateTime.class)))
            .thenReturn(recentPosts);
        when(subscriberRepository.findByStatus(SubscriptionStatus.CONFIRMED))
            .thenReturn(subscribers);
        when(tokenService.generateUnsubscribeToken(anyString()))
            .thenReturn("unsubscribe-token");
        
        // When
        newsletterService.sendWeeklyDigest();
        
        // Then
        verify(emailService, times(2)).sendWeeklyDigest(
            anyString(), eq(recentPosts), eq("unsubscribe-token")
        );
    }
    
    @Test
    @DisplayName("Should skip weekly digest when no recent posts")
    void shouldSkipWeeklyDigestWhenNoRecentPosts() {
        // Given
        when(postRepository.findPublishedPostsSince(any(LocalDateTime.class)))
            .thenReturn(Collections.emptyList());
        
        // When
        newsletterService.sendWeeklyDigest();
        
        // Then
        verify(emailService, never()).sendWeeklyDigest(anyString(), anyList(), anyString());
        verify(subscriberRepository, never()).findByStatus(any());
    }
}
```

---

### **US07-US09 - LGPD Compliance (9 pts)**

#### **Unit Tests - Data Deletion**

```java
@ExtendWith(MockitoExtension.class)
class NewsletterLGPDTest {
    
    @Mock private NewsletterTokenRepository tokenRepository;
    @Mock private NewsletterSubscriberRepository subscriberRepository;
    @Mock private NewsletterConsentLogRepository consentLogRepository;
    
    @InjectMocks private NewsletterService newsletterService;
    
    @Test
    @DisplayName("Should delete personal data on valid request")
    void shouldDeletePersonalDataOnValidRequest() {
        // Given
        String token = "delete-token-123";
        String email = "delete@test.com";
        
        NewsletterToken deleteToken = NewsletterToken.builder()
            .token(token)
            .email(email)
            .type(TokenType.DATA_REQUEST)
            .expiresAt(LocalDateTime.now().plusHours(24))
            .used(false)
            .build();
        
        when(tokenRepository.findByTokenAndType(token, TokenType.DATA_REQUEST))
            .thenReturn(Optional.of(deleteToken));
        
        // When
        newsletterService.deletePersonalData(token);
        
        // Then
        verify(subscriberRepository).markAsDeleted(email);
        verify(consentLogRepository).save(argThat(log ->
            log.getEmail().equals(email) &&
            log.getAction() == ConsentAction.DELETE
        ));
        
        verify(tokenRepository).save(argThat(t ->
            t.getUsed() == true && t.getUsedAt() != null
        ));
    }
    
    @Test
    @DisplayName("Should return personal data for valid request")
    void shouldReturnPersonalDataForValidRequest() {
        // Given
        String token = "data-request-token";
        String email = "data@test.com";
        
        NewsletterToken dataToken = NewsletterToken.builder()
            .token(token)
            .email(email)
            .type(TokenType.DATA_REQUEST)
            .expiresAt(LocalDateTime.now().plusHours(24))
            .used(false)
            .build();
            
        NewsletterSubscriber subscriber = createSubscriber(email);
        List<NewsletterConsentLog> consentLogs = Arrays.asList(
            createConsentLog(email, ConsentAction.SUBSCRIBE),
            createConsentLog(email, ConsentAction.CONFIRM)
        );
        
        when(tokenRepository.findByTokenAndType(token, TokenType.DATA_REQUEST))
            .thenReturn(Optional.of(dataToken));
        when(subscriberRepository.findByEmail(email))
            .thenReturn(Optional.of(subscriber));
        when(consentLogRepository.findByEmailOrderByCreatedAtDesc(email))
            .thenReturn(consentLogs);
        
        // When
        PersonalDataResponse response = newsletterService.getPersonalData(token);
        
        // Then
        assertThat(response.email()).isEqualTo(email);
        assertThat(response.consentHistory()).hasSize(2);
        assertThat(response.status()).isEqualTo(SubscriptionStatus.CONFIRMED);
    }
}
```

---

## 🚀 Performance Tests

### **Load Testing - JMeter Script**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<jmeterTestPlan version="1.2">
  <hashTree>
    <TestPlan guiclass="TestPlanGui" testclass="TestPlan" testname="Newsletter Load Test">
      <stringProp name="TestPlan.comments">Newsletter subscription load test</stringProp>
      <boolProp name="TestPlan.functional_mode">false</boolProp>
      <boolProp name="TestPlan.serialize_threadgroups">false</boolProp>
      <elementProp name="TestPlan.arguments" elementType="Arguments" guiclass="ArgumentsPanel">
        <collectionProp name="Arguments.arguments"/>
      </elementProp>
      <stringProp name="TestPlan.user_define_classpath"></stringProp>
    </TestPlan>
    <hashTree>
      <!-- Thread Group for Newsletter Subscription -->
      <ThreadGroup guiclass="ThreadGroupGui" testclass="ThreadGroup" testname="Newsletter Subscription Load">
        <stringProp name="ThreadGroup.num_threads">50</stringProp>
        <stringProp name="ThreadGroup.ramp_time">30</stringProp>
        <stringProp name="ThreadGroup.duration">300</stringProp>
        <stringProp name="ThreadGroup.delay"></stringProp>
        <boolProp name="ThreadGroup.scheduler">true</boolProp>
        <elementProp name="ThreadGroup.main_controller" elementType="LoopController">
          <boolProp name="LoopController.continue_forever">false</boolProp>
          <intProp name="LoopController.loops">-1</intProp>
        </elementProp>
      </ThreadGroup>
      <hashTree>
        <HTTPSamplerProxy guiclass="HttpTestSampleGui" testclass="HTTPSamplerProxy" testname="Subscribe to Newsletter">
          <elementProp name="HTTPsampler.Arguments" elementType="Arguments">
            <collectionProp name="Arguments.arguments">
              <elementProp name="" elementType="HTTPArgument">
                <boolProp name="HTTPArgument.always_encode">false</boolProp>
                <stringProp name="Argument.value">{
  "email": "loadtest${__threadNum}@test.com",
  "consentToReceiveEmails": true,
  "privacyPolicyVersion": "v1.0"
}</stringProp>
                <stringProp name="Argument.metadata">=</stringProp>
              </elementProp>
            </collectionProp>
          </elementProp>
          <stringProp name="HTTPSampler.domain">localhost</stringProp>
          <stringProp name="HTTPSampler.port">8080</stringProp>
          <stringProp name="HTTPSampler.protocol">http</stringProp>
          <stringProp name="HTTPSampler.path">/api/newsletter/subscribe</stringProp>
          <stringProp name="HTTPSampler.method">POST</stringProp>
          <boolProp name="HTTPSampler.use_keepalive">true</boolProp>
        </HTTPSamplerProxy>
        <hashTree>
          <HeaderManager guiclass="HeaderPanel" testclass="HeaderManager" testname="HTTP Header Manager">
            <collectionProp name="HeaderManager.headers">
              <elementProp name="" elementType="Header">
                <stringProp name="Header.name">Content-Type</stringProp>
                <stringProp name="Header.value">application/json</stringProp>
              </elementProp>
            </collectionProp>
          </HeaderManager>
          <hashTree/>
          <!-- Response Assertions -->
          <ResponseAssertion guiclass="AssertionGui" testclass="ResponseAssertion" testname="Response Assertion">
            <collectionProp name="Asserion.test_strings">
              <stringProp>202</stringProp>
            </collectionProp>
            <stringProp name="Assertion.test_field">Assertion.response_code</stringProp>
            <boolProp name="Assertion.assume_success">false</boolProp>
            <intProp name="Assertion.test_type">1</intProp>
          </ResponseAssertion>
          <hashTree/>
        </hashTree>
      </hashTree>
    </hashTree>
  </hashTree>
</jmeterTestPlan>
```

### **Performance Test Class**

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations = "classpath:application-test.yml")
class NewsletterPerformanceTest {
    
    @Autowired private TestRestTemplate restTemplate;
    @Autowired private NewsletterSubscriberRepository subscriberRepository;
    
    @Test
    @DisplayName("Should handle 100 concurrent subscriptions")
    void shouldHandle100ConcurrentSubscriptions() throws InterruptedException {
        // Given
        int numberOfThreads = 100;
        CountDownLatch latch = new CountDownLatch(numberOfThreads);
        ExecutorService executor = Executors.newFixedThreadPool(20);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger errorCount = new AtomicInteger(0);
        
        // When
        for (int i = 0; i < numberOfThreads; i++) {
            final int threadIndex = i;
            executor.submit(() -> {
                try {
                    NewsletterSubscriptionRequest request = new NewsletterSubscriptionRequest(
                        "perf" + threadIndex + "@test.com", true, "v1.0", null, null
                    );
                    
                    ResponseEntity<NewsletterSubscriptionResponse> response = 
                        restTemplate.postForEntity("/api/newsletter/subscribe", request, 
                                                 NewsletterSubscriptionResponse.class);
                    
                    if (response.getStatusCode() == HttpStatus.ACCEPTED) {
                        successCount.incrementAndGet();
                    } else {
                        errorCount.incrementAndGet();
                    }
                } catch (Exception e) {
                    errorCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }
        
        // Then
        latch.await(30, TimeUnit.SECONDS);
        executor.shutdown();
        
        assertThat(successCount.get()).isGreaterThan(95); // 95% success rate
        assertThat(errorCount.get()).isLessThan(5);
        
        // Verify database
        long subscriberCount = subscriberRepository.count();
        assertThat(subscriberCount).isEqualTo(successCount.get());
    }
}
```

---

## 🔒 Security Tests

### **Security Testing Class**

```java
@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.yml")
class NewsletterSecurityTest {
    
    @Autowired private TestRestTemplate restTemplate;
    
    @Test
    @DisplayName("Should prevent SQL injection in email field")
    void shouldPreventSQLInjectionInEmailField() {
        // Given
        NewsletterSubscriptionRequest maliciousRequest = new NewsletterSubscriptionRequest(
            "test@test.com'; DROP TABLE newsletter_subscribers; --", 
            true, "v1.0", null, null
        );
        
        // When
        ResponseEntity<ValidationErrorResponse> response = restTemplate.postForEntity(
            "/api/newsletter/subscribe", maliciousRequest, ValidationErrorResponse.class
        );
        
        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().getFieldErrors()).containsKey("email");
    }
    
    @Test
    @DisplayName("Should prevent XSS in email field")
    void shouldPreventXSSInEmailField() {
        // Given
        NewsletterSubscriptionRequest xssRequest = new NewsletterSubscriptionRequest(
            "<script>alert('xss')</script>@test.com", 
            true, "v1.0", null, null
        );
        
        // When
        ResponseEntity<ValidationErrorResponse> response = restTemplate.postForEntity(
            "/api/newsletter/subscribe", xssRequest, ValidationErrorResponse.class
        );
        
        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }
    
    @Test
    @DisplayName("Should enforce rate limiting on subscription endpoint")
    void shouldEnforceRateLimitingOnSubscriptionEndpoint() {
        // Given
        String baseEmail = "ratelimit@test.com";
        
        // When - Make multiple rapid requests
        List<ResponseEntity<Object>> responses = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            NewsletterSubscriptionRequest request = new NewsletterSubscriptionRequest(
                i + baseEmail, true, "v1.0", null, null
            );
            responses.add(restTemplate.postForEntity("/api/newsletter/subscribe", request, Object.class));
        }
        
        // Then - Some requests should be rate limited
        long rateLimitedCount = responses.stream()
            .mapToInt(r -> r.getStatusCode().value())
            .filter(status -> status == 429)
            .count();
            
        assertThat(rateLimitedCount).isGreaterThan(0);
    }
}
```

---

## 📊 Test Coverage Configuration

### **JaCoCo Configuration (pom.xml)**

```xml
<plugin>
    <groupId>org.jacoco</groupId>
    <artifactId>jacoco-maven-plugin</artifactId>
    <version>0.8.8</version>
    <configuration>
        <rules>
            <rule>
                <element>BUNDLE</element>
                <limits>
                    <limit>
                        <counter>LINE</counter>
                        <value>COVEREDRATIO</value>
                        <minimum>0.85</minimum>
                    </limits>
                    <limit>
                        <counter>BRANCH</counter>
                        <value>COVEREDRATIO</value>
                        <minimum>0.80</minimum>
                    </limits>
                </limits>
            </rule>
        </rules>
        <excludes>
            <exclude>**/dto/**</exclude>
            <exclude>**/entity/**</exclude>
            <exclude>**/config/**</exclude>
        </excludes>
    </configuration>
    <executions>
        <execution>
            <goals>
                <goal>prepare-agent</goal>
            </goals>
        </execution>
        <execution>
            <id>report</id>
            <phase>test</phase>
            <goals>
                <goal>report</goal>
            </goals>
        </execution>
        <execution>
            <id>check</id>
            <goals>
                <goal>check</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```

---

## 🎯 Test Execution Strategy

### **CI/CD Pipeline Tests**

```yaml
# .github/workflows/newsletter-tests.yml
name: Newsletter Tests

on:
  push:
    branches: [ main, develop, feature/newsletter-* ]
  pull_request:
    branches: [ main, develop ]

jobs:
  test:
    runs-on: ubuntu-latest
    
    services:
      postgres:
        image: postgres:15
        env:
          POSTGRES_PASSWORD: testpass
          POSTGRES_DB: newsletter_test
        options: >-
          --health-cmd pg_isready
          --health-interval 10s
          --health-timeout 5s
          --health-retries 5
      
      redis:
        image: redis:7
        options: >-
          --health-cmd "redis-cli ping"
          --health-interval 10s
          --health-timeout 5s
          --health-retries 5
    
    steps:
    - uses: actions/checkout@v3
    
    - name: Set up JDK 17
      uses: actions/setup-java@v3
      with:
        java-version: '17'
        distribution: 'temurin'
    
    - name: Cache Maven dependencies
      uses: actions/cache@v3
      with:
        path: ~/.m2
        key: ${{ runner.os }}-m2-${{ hashFiles('**/pom.xml') }}
    
    - name: Run Newsletter Unit Tests
      run: mvn test -Dtest="*Newsletter*Test"
    
    - name: Run Newsletter Integration Tests
      run: mvn test -Dtest="*Newsletter*IntegrationTest"
    
    - name: Generate Test Report
      run: mvn jacoco:report
    
    - name: Upload Coverage to Codecov
      uses: codecov/codecov-action@v3
      with:
        file: ./target/site/jacoco/jacoco.xml
    
    - name: Performance Tests
      run: |
        docker-compose up -d
        mvn test -Dtest="*Newsletter*PerformanceTest"
        docker-compose down
```

### **Local Test Commands**

```bash
# Newsletter-specific tests
mvn test -Dtest="*Newsletter*Test"

# Integration tests only
mvn test -Dtest="*Newsletter*IntegrationTest"

# Performance tests
mvn test -Dtest="*Newsletter*PerformanceTest"

# Security tests
mvn test -Dtest="*Newsletter*SecurityTest"

# All newsletter tests
mvn test -Dtest="*Newsletter*"

# With coverage report
mvn test jacoco:report -Dtest="*Newsletter*"

# Specific User Story tests
mvn test -Dtest="*NewsletterSubscription*Test"  # US01
mvn test -Dtest="*NewsletterConfirmation*Test"  # US02
mvn test -Dtest="*NewsletterUnsubscribe*Test"   # US03
```

---

## 📋 Test Checklist por User Story

### **US01 - Inscrição de Usuários**
- [ ] Unit: NewsletterService.subscribe() - happy path
- [ ] Unit: NewsletterService.subscribe() - email já cadastrado  
- [ ] Unit: NewsletterService.subscribe() - reinscrição após unsubscribe
- [ ] Integration: POST /api/newsletter/subscribe - success
- [ ] Integration: POST /api/newsletter/subscribe - conflict
- [ ] Integration: POST /api/newsletter/subscribe - validation
- [ ] Database: Subscriber persistido corretamente
- [ ] Database: ConsentLog criado
- [ ] Email: Confirmação enviada
- [ ] Cache: Invalidação após subscription

### **US02 - Confirmação de E-mail**
- [ ] Unit: confirmSubscription() - token válido
- [ ] Unit: confirmSubscription() - token expirado
- [ ] Unit: confirmSubscription() - token já usado
- [ ] Unit: confirmSubscription() - token inválido
- [ ] Integration: GET /confirm?token= - success
- [ ] Integration: GET /confirm?token= - error cases
- [ ] Database: Status atualizado para CONFIRMED
- [ ] Database: Token marcado como usado
- [ ] Cache: Invalidação de confirmed subscribers

### **US03 - Descadastro**
- [ ] Unit: unsubscribe() - token válido
- [ ] Unit: unsubscribe() - token inválido
- [ ] Integration: GET /unsubscribe?token= - success
- [ ] Database: Status atualizado para UNSUBSCRIBED
- [ ] Database: ConsentLog de unsubscribe
- [ ] Cache: Invalidação de subscribers

### **US04 - Administração**
- [ ] Integration: GET /subscribers - admin success
- [ ] Integration: GET /subscribers - non-admin forbidden
- [ ] Integration: GET /subscribers - filtros funcionando
- [ ] Integration: GET /subscribers - paginação
- [ ] Security: Autorização ADMIN enforçada
- [ ] Response: Dados sensíveis não expostos

### **US05 - Envio Automático**
- [ ] Unit: Event listener - confirmed subscribers
- [ ] Unit: Event listener - skip non-confirmed
- [ ] Unit: Event listener - error handling
- [ ] Integration: Post published triggers event
- [ ] Performance: Async processing
- [ ] Database: EmailLog persisted
- [ ] Email: Templates corretos

### **US06 - Envio Semanal**
- [ ] Unit: Scheduler - posts recentes
- [ ] Unit: Scheduler - sem posts
- [ ] Unit: Scheduler - apenas confirmed
- [ ] Integration: Scheduled job execution
- [ ] Performance: Batch processing
- [ ] Database: Posts query otimizada

### **US07-US09 - LGPD**
- [ ] Unit: deletePersonalData() - token válido
- [ ] Unit: getPersonalData() - retorno completo
- [ ] Integration: DELETE /delete?token= - success
- [ ] Integration: GET /my-data?token= - success
- [ ] Database: Soft delete implementado
- [ ] Compliance: Todos dados retornados
- [ ] Security: Token validation

---

## 🎯 Definition of Done - Testing

### **Critérios de Qualidade**
1. ✅ **85%+ Code Coverage** (lines + branches)
2. ✅ **Todos Unit Tests** passando (green)
3. ✅ **Todos Integration Tests** passando
4. ✅ **Performance Tests** dentro dos SLAs
5. ✅ **Security Tests** sem vulnerabilidades
6. ✅ **E2E Tests** cobrindo user journeys
7. ✅ **Test Documentation** atualizada

### **Automação**
1. ✅ **CI Pipeline** executando todos testes
2. ✅ **Coverage Report** gerado automaticamente
3. ✅ **Quality Gates** configurados
4. ✅ **Notification** em caso de falha
5. ✅ **Regression Testing** em cada PR

**Próximo Passo**: Iniciar implementação seguindo TDD - começar pelos testes da US01 (Inscrição de Usuários).