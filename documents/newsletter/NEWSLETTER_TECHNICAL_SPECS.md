# 🏗️ Newsletter Technical Specifications

**Projeto:** Blog API REST - Newsletter System  
**Versão:** 1.0  
**Arquitetura:** Spring Boot 3.2 + PostgreSQL + Redis + Docker  

---

## 🎯 Visão Geral Técnica

### **Objetivo**
Especificar a arquitetura técnica completa da feature Newsletter, incluindo entidades, DTOs, services, controllers, integrações e componentes de infraestrutura.

### **Princípios Arquiteturais**
- **Clean Architecture**: Separação clara entre camadas
- **Domain-Driven Design**: Entidades refletem domínio do negócio
- **SOLID Principles**: Código maintenable e extensível  
- **Cache-First**: Performance via Redis distribuído
- **Event-Driven**: Desacoplamento via eventos Spring
- **Security-by-Design**: LGPD compliance desde o início

---

## 📊 Modelo de Dados

### **Entidades Principais**

#### 1. **NewsletterSubscriber**
```java
@Entity
@Table(name = "newsletter_subscribers")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class NewsletterSubscriber {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    @Email
    private String email;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SubscriptionStatus status; // PENDING, CONFIRMED, UNSUBSCRIBED
    
    @Column(name = "subscribed_at", nullable = false)
    private LocalDateTime subscribedAt;
    
    @Column(name = "confirmed_at")
    private LocalDateTime confirmedAt;
    
    @Column(name = "unsubscribed_at")
    private LocalDateTime unsubscribedAt;
    
    // LGPD Compliance Fields
    @Column(name = "consent_given_at", nullable = false)
    private LocalDateTime consentGivenAt;
    
    @Column(name = "consent_ip")
    private String consentIpAddress;
    
    @Column(name = "consent_user_agent")
    private String consentUserAgent;
    
    @Column(name = "privacy_policy_version")
    private String privacyPolicyVersion;
    
    // Audit Fields
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
```

#### 2. **NewsletterToken**
```java
@Entity
@Table(name = "newsletter_tokens")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class NewsletterToken {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String token; // UUID
    
    @Column(nullable = false)
    private String email;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TokenType type; // CONFIRMATION, UNSUBSCRIBE, DATA_REQUEST
    
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;
    
    @Column(name = "used_at")
    private LocalDateTime usedAt;
    
    @Builder.Default
    @Column(nullable = false)
    private Boolean used = false;
    
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
```

#### 3. **NewsletterConsentLog**
```java
@Entity
@Table(name = "newsletter_consent_logs")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class NewsletterConsentLog {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String email;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ConsentAction action; // SUBSCRIBE, CONFIRM, UNSUBSCRIBE, DATA_REQUEST, DELETE
    
    @Column(name = "ip_address")
    private String ipAddress;
    
    @Column(name = "user_agent")
    private String userAgent;
    
    @Column(name = "privacy_policy_version")
    private String privacyPolicyVersion;
    
    @Column(name = "additional_data", columnDefinition = "TEXT")
    private String additionalData; // JSON for extra context
    
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
```

#### 4. **NewsletterEmailLog**
```java
@Entity
@Table(name = "newsletter_email_logs")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class NewsletterEmailLog {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String email;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EmailType emailType; // CONFIRMATION, NEW_POST, WEEKLY_DIGEST
    
    @Column(name = "post_id")
    private Long postId; // For NEW_POST emails
    
    @Column(name = "sent_at", nullable = false)
    private LocalDateTime sentAt;
    
    @Builder.Default
    @Column(nullable = false)
    private Boolean successful = false;
    
    @Column(name = "error_message")
    private String errorMessage;
    
    @Column(name = "email_subject")
    private String emailSubject;
    
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
```

### **Enums**

```java
public enum SubscriptionStatus {
    PENDING,
    CONFIRMED, 
    UNSUBSCRIBED,
    DELETED // LGPD compliance
}

public enum TokenType {
    CONFIRMATION,
    UNSUBSCRIBE,
    DATA_REQUEST
}

public enum ConsentAction {
    SUBSCRIBE,
    CONFIRM,
    UNSUBSCRIBE,
    DATA_REQUEST,
    DELETE
}

public enum EmailType {
    CONFIRMATION,
    NEW_POST,
    WEEKLY_DIGEST,
    UNSUBSCRIBE_CONFIRMATION
}
```

---

## 📦 DTOs (Data Transfer Objects)

### **Request DTOs (Java Records)**

```java
// US01 - Subscription
public record NewsletterSubscriptionRequest(
    @NotBlank(message = "Email é obrigatório")
    @Email(message = "Email deve ter formato válido")
    String email,
    
    @NotNull(message = "Consentimento é obrigatório")
    Boolean consentToReceiveEmails,
    
    @NotBlank(message = "Versão da política de privacidade é obrigatória")
    String privacyPolicyVersion,
    
    // Campos capturados automaticamente pelo controller
    String ipAddress,
    String userAgent
) {}

// US07 - LGPD Data Request
public record NewsletterDataRequest(
    @NotBlank @Email String email,
    @NotBlank String token
) {}

// US04 - Admin Filters
public record NewsletterSubscriberFilter(
    SubscriptionStatus status,
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    LocalDate subscribedAfter,
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    LocalDate subscribedBefore,
    
    @Min(0) Integer page,
    @Min(1) @Max(100) Integer size
) {
    // Default values
    public NewsletterSubscriberFilter {
        page = page != null ? page : 0;
        size = size != null ? size : 20;
    }
}
```

### **Response DTOs**

```java
// US01 - Subscription Response
public record NewsletterSubscriptionResponse(
    String message,
    String email,
    SubscriptionStatus status,
    LocalDateTime subscribedAt,
    String nextStep
) {
    public static NewsletterSubscriptionResponse success(String email) {
        return new NewsletterSubscriptionResponse(
            "Inscrição realizada com sucesso! Verifique seu email para confirmar.",
            email,
            SubscriptionStatus.PENDING,
            LocalDateTime.now(),
            "Clique no link de confirmação enviado para seu email"
        );
    }
}

// US02 - Confirmation Response  
public record NewsletterConfirmationResponse(
    String message,
    String email,
    SubscriptionStatus status,
    LocalDateTime confirmedAt
) {}

// US04 - Admin Subscriber Response
public record AdminNewsletterSubscriberResponse(
    Long id,
    String email,
    SubscriptionStatus status,
    LocalDateTime subscribedAt,
    LocalDateTime confirmedAt,
    LocalDateTime unsubscribedAt,
    String privacyPolicyVersion
) {
    // Não expor dados sensíveis como IP/UserAgent para admin
    public static AdminNewsletterSubscriberResponse from(NewsletterSubscriber subscriber) {
        return new AdminNewsletterSubscriberResponse(
            subscriber.getId(),
            subscriber.getEmail(),
            subscriber.getStatus(),
            subscriber.getSubscribedAt(),
            subscriber.getConfirmedAt(),
            subscriber.getUnsubscribedAt(),
            subscriber.getPrivacyPolicyVersion()
        );
    }
}

// US09 - Personal Data Response (LGPD)
public record PersonalDataResponse(
    String email,
    SubscriptionStatus status,
    LocalDateTime subscribedAt,
    LocalDateTime confirmedAt,
    LocalDateTime consentGivenAt,
    String privacyPolicyVersion,
    List<ConsentLogEntry> consentHistory,
    List<EmailLogEntry> emailHistory
) {
    public record ConsentLogEntry(
        ConsentAction action,
        LocalDateTime timestamp,
        String privacyPolicyVersion
    ) {}
    
    public record EmailLogEntry(
        EmailType type,
        LocalDateTime sentAt,
        Boolean successful,
        String subject
    ) {}
}
```

---

## 🔧 Services Layer

### **1. NewsletterService**

```java
@Service
@Transactional
@Slf4j
public class NewsletterService {
    
    private final NewsletterSubscriberRepository subscriberRepository;
    private final NewsletterTokenRepository tokenRepository;
    private final NewsletterConsentLogRepository consentLogRepository;
    private final NewsletterEmailLogRepository emailLogRepository;
    private final EmailService emailService;
    
    // US01 - Subscribe
    @Timed(name = "newsletter.subscription.time", description = "Time taken to process newsletter subscription")
    @CacheEvict(value = "newsletter_confirmed_subscribers", allEntries = true)
    public NewsletterSubscriptionResponse subscribe(NewsletterSubscriptionRequest request) {
        
        // Validate email not already subscribed
        Optional<NewsletterSubscriber> existing = subscriberRepository.findByEmail(request.email());
        if (existing.isPresent() && existing.get().getStatus() != SubscriptionStatus.UNSUBSCRIBED) {
            throw new BadRequestException("Email já está inscrito na newsletter");
        }
        
        // Create subscriber
        NewsletterSubscriber subscriber = NewsletterSubscriber.builder()
            .email(request.email())
            .status(SubscriptionStatus.PENDING)
            .subscribedAt(LocalDateTime.now())
            .consentGivenAt(LocalDateTime.now())
            .consentIpAddress(request.ipAddress())
            .consentUserAgent(request.userAgent())
            .privacyPolicyVersion(request.privacyPolicyVersion())
            .build();
            
        subscriberRepository.save(subscriber);
        
        // Log consent
        logConsentAction(request.email(), ConsentAction.SUBSCRIBE, request.ipAddress(), 
                        request.userAgent(), request.privacyPolicyVersion());
        
        // Send confirmation email (async)
        sendConfirmationEmailAsync(subscriber);
        
        return NewsletterSubscriptionResponse.success(request.email());
    }
    
    // US02 - Confirm subscription
    @CacheEvict(value = "newsletter_confirmed_subscribers", allEntries = true)
    public NewsletterConfirmationResponse confirmSubscription(String token) {
        
        NewsletterToken confirmationToken = validateToken(token, TokenType.CONFIRMATION);
        
        NewsletterSubscriber subscriber = subscriberRepository.findByEmail(confirmationToken.getEmail())
            .orElseThrow(() -> new ResourceNotFoundException("Subscriber not found"));
            
        subscriber.setStatus(SubscriptionStatus.CONFIRMED);
        subscriber.setConfirmedAt(LocalDateTime.now());
        
        subscriberRepository.save(subscriber);
        
        // Mark token as used
        confirmationToken.setUsed(true);
        confirmationToken.setUsedAt(LocalDateTime.now());
        tokenRepository.save(confirmationToken);
        
        // Log consent confirmation
        logConsentAction(subscriber.getEmail(), ConsentAction.CONFIRM, null, null, null);
        
        return new NewsletterConfirmationResponse(
            "Email confirmado com sucesso! Você receberá nossas novidades.",
            survivor.getEmail(),
            SubscriptionStatus.CONFIRMED,
            LocalDateTime.now()
        );
    }
    
    // US05 - Send new post notification
    @Async
    @EventListener
    @Timed(name = "newsletter.new_post_notification.time")
    public void handleNewPostPublished(PostPublishedEvent event) {
        
        List<NewsletterSubscriber> confirmedSubscribers = getConfirmedSubscribers();
        
        for (NewsletterSubscriber subscriber : confirmedSubscribers) {
            try {
                emailService.sendNewPostNotification(
                    subscriber.getEmail(), 
                    event.getPost(),
                    generateUnsubscribeToken(subscriber.getEmail())
                );
                
                logEmailSent(subscriber.getEmail(), EmailType.NEW_POST, event.getPost().getId(), true);
                
            } catch (Exception e) {
                log.error("Failed to send new post notification to {}: {}", 
                         subscriber.getEmail(), e.getMessage());
                logEmailSent(subscriber.getEmail(), EmailType.NEW_POST, event.getPost().getId(), false, e.getMessage());
            }
        }
    }
    
    // US06 - Weekly digest
    @Scheduled(cron = "0 0 9 * * FRI") // Every Friday at 9 AM
    @Timed(name = "newsletter.weekly_digest.time")
    public void sendWeeklyDigest() {
        
        LocalDateTime oneWeekAgo = LocalDateTime.now().minusWeeks(1);
        List<Post> recentPosts = postRepository.findPublishedPostsSince(oneWeekAgo);
        
        if (recentPosts.isEmpty()) {
            log.info("No posts published in the last week, skipping weekly digest");
            return;
        }
        
        List<NewsletterSubscriber> confirmedSubscribers = getConfirmedSubscribers();
        
        for (NewsletterSubscriber subscriber : confirmedSubscribers) {
            try {
                emailService.sendWeeklyDigest(
                    subscriber.getEmail(),
                    recentPosts,
                    generateUnsubscribeToken(subscriber.getEmail())
                );
                
                logEmailSent(subscriber.getEmail(), EmailType.WEEKLY_DIGEST, null, true);
                
            } catch (Exception e) {
                log.error("Failed to send weekly digest to {}: {}", 
                         subscriber.getEmail(), e.getMessage());
                logEmailSent(subscriber.getEmail(), EmailType.WEEKLY_DIGEST, null, false, e.getMessage());
            }
        }
    }
    
    // Cache confirmed subscribers for performance
    @Cacheable(value = "newsletter_confirmed_subscribers", key = "'all'")
    public List<NewsletterSubscriber> getConfirmedSubscribers() {
        return subscriberRepository.findByStatus(SubscriptionStatus.CONFIRMED);
    }
    
    // Helper methods
    private NewsletterToken validateToken(String token, TokenType expectedType) {
        NewsletterToken newsletterToken = tokenRepository.findByTokenAndType(token, expectedType)
            .orElseThrow(() -> new BadRequestException("Token inválido"));
            
        if (newsletterToken.getUsed()) {
            throw new BadRequestException("Token já foi utilizado");
        }
        
        if (newsletterToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Token expirado");
        }
        
        return newsletterToken;
    }
    
    private void logConsentAction(String email, ConsentAction action, String ip, 
                                 String userAgent, String policyVersion) {
        NewsletterConsentLog log = NewsletterConsentLog.builder()
            .email(email)
            .action(action)
            .ipAddress(ip)
            .userAgent(userAgent)
            .privacyPolicyVersion(policyVersion)
            .build();
            
        consentLogRepository.save(log);
    }
}
```

### **2. NewsletterTokenService**

```java
@Service
@Transactional
public class NewsletterTokenService {
    
    private final NewsletterTokenRepository tokenRepository;
    
    public String generateConfirmationToken(String email) {
        return generateToken(email, TokenType.CONFIRMATION, Duration.ofHours(48));
    }
    
    public String generateUnsubscribeToken(String email) {
        return generateToken(email, TokenType.UNSUBSCRIBE, Duration.ofDays(365));
    }
    
    public String generateDataRequestToken(String email) {
        return generateToken(email, TokenType.DATA_REQUEST, Duration.ofHours(24));
    }
    
    private String generateToken(String email, TokenType type, Duration validity) {
        String token = UUID.randomUUID().toString();
        
        NewsletterToken newsletterToken = NewsletterToken.builder()
            .token(token)
            .email(email)
            .type(type)
            .expiresAt(LocalDateTime.now().plus(validity))
            .build();
            
        tokenRepository.save(newsletterToken);
        return token;
    }
    
    @Scheduled(cron = "0 0 2 * * *") // Daily at 2 AM
    public void cleanupExpiredTokens() {
        tokenRepository.deleteByExpiresAtBefore(LocalDateTime.now());
    }
}
```

---

## 🎮 Controllers Layer

### **NewsletterController**

```java
@RestController
@RequestMapping("/api/newsletter")
@Validated
@Slf4j
@Tag(name = "Newsletter", description = "Newsletter subscription and management API")
public class NewsletterController {
    
    private final NewsletterService newsletterService;
    private final HttpServletRequest request;
    
    // US01 - Subscribe
    @PostMapping("/subscribe")
    @Operation(summary = "Subscribe to newsletter", description = "Subscribe email to receive blog updates")
    @ApiResponses({
        @ApiResponse(responseCode = "202", description = "Subscription successful"),
        @ApiResponse(responseCode = "409", description = "Email already subscribed"),
        @ApiResponse(responseCode = "400", description = "Invalid email format")
    })
    public ResponseEntity<NewsletterSubscriptionResponse> subscribe(
            @Valid @RequestBody NewsletterSubscriptionRequest subscriptionRequest) {
        
        // Enrich request with IP and User-Agent
        NewsletterSubscriptionRequest enrichedRequest = new NewsletterSubscriptionRequest(
            subscriptionRequest.email(),
            subscriptionRequest.consentToReceiveEmails(),
            subscriptionRequest.privacyPolicyVersion(),
            getClientIP(),
            request.getHeader("User-Agent")
        );
        
        NewsletterSubscriptionResponse response = newsletterService.subscribe(enrichedRequest);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }
    
    // US02 - Confirm subscription
    @GetMapping("/confirm")
    @Operation(summary = "Confirm newsletter subscription")
    public ResponseEntity<NewsletterConfirmationResponse> confirmSubscription(
            @RequestParam @NotBlank String token) {
        
        NewsletterConfirmationResponse response = newsletterService.confirmSubscription(token);
        return ResponseEntity.ok(response);
    }
    
    // US03 - Unsubscribe
    @GetMapping("/unsubscribe")
    @Operation(summary = "Unsubscribe from newsletter")
    public ResponseEntity<String> unsubscribe(@RequestParam @NotBlank String token) {
        
        newsletterService.unsubscribe(token);
        return ResponseEntity.ok("Você foi removido da newsletter com sucesso.");
    }
    
    // US04 - Admin: List subscribers
    @GetMapping("/subscribers")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "List newsletter subscribers (Admin only)")
    public ResponseEntity<Page<AdminNewsletterSubscriberResponse>> getSubscribers(
            @Valid NewsletterSubscriberFilter filter) {
        
        Page<AdminNewsletterSubscriberResponse> subscribers = 
            newsletterService.getSubscribers(filter);
        return ResponseEntity.ok(subscribers);
    }
    
    // US07 - LGPD: Delete personal data
    @DeleteMapping("/delete")
    @Operation(summary = "Delete all personal data (LGPD compliance)")
    public ResponseEntity<Void> deletePersonalData(@RequestParam @NotBlank String token) {
        
        newsletterService.deletePersonalData(token);
        return ResponseEntity.noContent().build();
    }
    
    // US09 - LGPD: Get personal data
    @GetMapping("/my-data")
    @Operation(summary = "Get personal data (LGPD compliance)")
    public ResponseEntity<PersonalDataResponse> getPersonalData(@RequestParam @NotBlank String token) {
        
        PersonalDataResponse data = newsletterService.getPersonalData(token);
        return ResponseEntity.ok(data);
    }
    
    private String getClientIP() {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
```

---

## 🗄️ Repository Layer

### **Repository Interfaces**

```java
@Repository
public interface NewsletterSubscriberRepository extends JpaRepository<NewsletterSubscriber, Long> {
    
    Optional<NewsletterSubscriber> findByEmail(String email);
    
    List<NewsletterSubscriber> findByStatus(SubscriptionStatus status);
    
    @Query("SELECT ns FROM NewsletterSubscriber ns WHERE " +
           "(:status IS NULL OR ns.status = :status) AND " +
           "(:after IS NULL OR ns.subscribedAt >= :after) AND " +
           "(:before IS NULL OR ns.subscribedAt <= :before)")
    Page<NewsletterSubscriber> findWithFilters(
        @Param("status") SubscriptionStatus status,
        @Param("after") LocalDateTime after,
        @Param("before") LocalDateTime before,
        Pageable pageable
    );
    
    @Modifying
    @Query("UPDATE NewsletterSubscriber ns SET ns.status = 'DELETED' WHERE ns.email = :email")
    void markAsDeleted(@Param("email") String email);
    
    long countByStatusAndSubscribedAtBetween(
        SubscriptionStatus status, 
        LocalDateTime start, 
        LocalDateTime end
    );
}

@Repository
public interface NewsletterTokenRepository extends JpaRepository<NewsletterToken, Long> {
    
    Optional<NewsletterToken> findByTokenAndType(String token, TokenType type);
    
    void deleteByExpiresAtBefore(LocalDateTime dateTime);
    
    List<NewsletterToken> findByEmailAndTypeAndUsedFalse(String email, TokenType type);
}

@Repository  
public interface NewsletterConsentLogRepository extends JpaRepository<NewsletterConsentLog, Long> {
    
    List<NewsletterConsentLog> findByEmailOrderByCreatedAtDesc(String email);
    
    @Query("SELECT ncl FROM NewsletterConsentLog ncl WHERE " +
           "ncl.createdAt >= :start AND ncl.createdAt <= :end")
    List<NewsletterConsentLog> findByDateRange(
        @Param("start") LocalDateTime start,
        @Param("end") LocalDateTime end
    );
}

@Repository
public interface NewsletterEmailLogRepository extends JpaRepository<NewsletterEmailLog, Long> {
    
    List<NewsletterEmailLog> findByEmailOrderBySentAtDesc(String email);
    
    long countByEmailTypeAndSentAtBetween(
        EmailType emailType, 
        LocalDateTime start, 
        LocalDateTime end
    );
    
    @Query("SELECT COUNT(nel) FROM NewsletterEmailLog nel WHERE " +
           "nel.emailType = :type AND nel.successful = true AND " +
           "nel.sentAt >= :start AND nel.sentAt <= :end")
    long countSuccessfulEmailsByType(
        @Param("type") EmailType type,
        @Param("start") LocalDateTime start,
        @Param("end") LocalDateTime end
    );
}
```

---

## 🔧 Configuration & Integration

### **1. EmailService Extension**

```java
@Service
public class EmailService {
    
    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    
    // Newsletter-specific methods
    public void sendNewsletterConfirmation(String email, String confirmationToken) {
        Context context = new Context();
        context.setVariable("confirmationUrl", 
            "http://localhost:8080/api/newsletter/confirm?token=" + confirmationToken);
        context.setVariable("email", email);
        
        String htmlContent = templateEngine.process("newsletter/confirmation", context);
        
        sendHtmlEmail(email, "Confirme sua inscr in eção na Newsletter", htmlContent);
    }
    
    public void sendNewPostNotification(String email, Post post, String unsubscribeToken) {
        Context context = new Context();
        context.setVariable("post", post);
        context.setVariable("unsubscribeUrl", 
            "http://localhost:8080/api/newsletter/unsubscribe?token=" + unsubscribeToken);
        
        String htmlContent = templateEngine.process("newsletter/new-post", context);
        String subject = "Novo post: " + post.getTitle();
        
        sendHtmlEmail(email, subject, htmlContent);
    }
    
    public void sendWeeklyDigest(String email, List<Post> posts, String unsubscribeToken) {
        Context context = new Context();
        context.setVariable("posts", posts);
        context.setVariable("unsubscribeUrl", 
            "http://localhost:8080/api/newsletter/unsubscribe?token=" + unsubscribeToken);
        context.setVariable("weekStart", LocalDate.now().minusWeeks(1));
        context.setVariable("weekEnd", LocalDate.now());
        
        String htmlContent = templateEngine.process("newsletter/weekly-digest", context);
        String subject = "Digest Semanal - " + posts.size() + " novos posts";
        
        sendHtmlEmail(email, subject, htmlContent);
    }
}
```

### **2. Event System**

```java
// Event for new post publication
public class PostPublishedEvent extends ApplicationEvent {
    private final Post post;
    
    public PostPublishedEvent(Object source, Post post) {
        super(source);
        this.post = post;
    }
    
    public Post getPost() { return post; }
}

// In PostService - fire event when publishing
@Service
public class PostService {
    
    private final ApplicationEventPublisher eventPublisher;
    
    public Post publishPost(Long postId) {
        Post post = postRepository.findById(postId)
            .orElseThrow(() -> new ResourceNotFoundException("Post not found"));
            
        post.setPublished(true);
        post.setPublishedAt(LocalDateTime.now());
        
        Post savedPost = postRepository.save(post);
        
        // Fire event for newsletter
        eventPublisher.publishEvent(new PostPublishedEvent(this, savedPost));
        
        return savedPost;
    }
}
```

### **3. Cache Configuration**

```java
@Configuration
@EnableCaching
public class NewsletterCacheConfig {
    
    @Bean
    public CacheManager newsletterCacheManager() {
        RedisCacheManager.Builder builder = RedisCacheManager
            .RedisCacheManagerBuilder
            .fromConnectionFactory(redisConnectionFactory())
            .cacheDefaults(cacheConfiguration());
        
        return builder.build();
    }
    
    private RedisCacheConfiguration cacheConfiguration() {
        return RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofMinutes(10))
            .serializeKeysWith(RedisSerializationContext.SerializationPair
                .fromSerializer(new StringRedisSerializer()))
            .serializeValuesWith(RedisSerializationContext.SerializationPair
                .fromSerializer(new GenericJackson2JsonRedisSerializer()));
    }
}
```

### **4. Security Configuration Extension**

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(authz -> authz
                // Newsletter public endpoints
                .requestMatchers("/api/newsletter/subscribe").permitAll()
                .requestMatchers("/api/newsletter/confirm").permitAll()
                .requestMatchers("/api/newsletter/unsubscribe").permitAll()
                .requestMatchers("/api/newsletter/my-data").permitAll()
                .requestMatchers("/api/newsletter/delete").permitAll()
                
                // Newsletter admin endpoints
                .requestMatchers("/api/newsletter/subscribers").hasRole("ADMIN")
                .requestMatchers("/api/newsletter/consent-history").hasRole("ADMIN")
                
                // Other configurations...
            );
            
        return http.build();
    }
}
```

---

## 📊 Database Migrations

### **Migration Scripts (Flyway)**

```sql
-- V12__create_newsletter_tables.sql
CREATE TABLE newsletter_subscribers (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    status VARCHAR(50) NOT NULL CHECK (status IN ('PENDING', 'CONFIRMED', 'UNSUBSCRIBED', 'DELETED')),
    subscribed_at TIMESTAMP NOT NULL,
    confirmed_at TIMESTAMP,
    unsubscribed_at TIMESTAMP,
    consent_given_at TIMESTAMP NOT NULL,
    consent_ip VARCHAR(45),
    consent_user_agent TEXT,
    privacy_policy_version VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE newsletter_tokens (
    id BIGSERIAL PRIMARY KEY,
    token VARCHAR(255) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL,
    type VARCHAR(50) NOT NULL CHECK (type IN ('CONFIRMATION', 'UNSUBSCRIBE', 'DATA_REQUEST')),
    expires_at TIMESTAMP NOT NULL,
    used_at TIMESTAMP,
    used BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE newsletter_consent_logs (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL,
    action VARCHAR(50) NOT NULL CHECK (action IN ('SUBSCRIBE', 'CONFIRM', 'UNSUBSCRIBE', 'DATA_REQUEST', 'DELETE')),
    ip_address VARCHAR(45),
    user_agent TEXT,
    privacy_policy_version VARCHAR(50),
    additional_data TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE newsletter_email_logs (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL,
    email_type VARCHAR(50) NOT NULL CHECK (email_type IN ('CONFIRMATION', 'NEW_POST', 'WEEKLY_DIGEST', 'UNSUBSCRIBE_CONFIRMATION')),
    post_id BIGINT,
    sent_at TIMESTAMP NOT NULL,
    successful BOOLEAN NOT NULL DEFAULT FALSE,
    error_message TEXT,
    email_subject VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Indexes for performance
CREATE INDEX idx_newsletter_subscribers_email ON newsletter_subscribers(email);
CREATE INDEX idx_newsletter_subscribers_status ON newsletter_subscribers(status);
CREATE INDEX idx_newsletter_tokens_token ON newsletter_tokens(token);
CREATE INDEX idx_newsletter_tokens_email_type ON newsletter_tokens(email, type);
CREATE INDEX idx_newsletter_consent_logs_email ON newsletter_consent_logs(email);
CREATE INDEX idx_newsletter_email_logs_email ON newsletter_email_logs(email);
```

---

## 🔍 Monitoring & Metrics

### **Custom Metrics**

```java
@Component
public class NewsletterMetrics {
    
    private final Counter subscriptionCounter;
    private final Counter confirmationCounter;
    private final Counter unsubscribeCounter;
    private final Counter emailSentCounter;
    private final Timer emailSendingTimer;
    
    public NewsletterMetrics(MeterRegistry meterRegistry) {
        this.subscriptionCounter = Counter.builder("newsletter.subscriptions.total")
            .description("Total newsletter subscriptions")
            .register(meterRegistry);
            
        this.confirmationCounter = Counter.builder("newsletter.confirmations.total")
            .description("Total newsletter confirmations")
            .register(meterRegistry);
            
        this.unsubscribeCounter = Counter.builder("newsletter.unsubscribes.total")
            .description("Total newsletter unsubscribes")
            .register(meterRegistry);
            
        this.emailSentCounter = Counter.builder("newsletter.emails.sent.total")
            .description("Total newsletter emails sent")
            .tag("type", "unknown")
            .register(meterRegistry);
            
        this.emailSendingTimer = Timer.builder("newsletter.email.sending.time")
            .description("Time taken to send newsletter emails")
            .register(meterRegistry);
    }
    
    public void incrementSubscriptions() {
        subscriptionCounter.increment();
    }
    
    public void incrementConfirmations() {
        confirmationCounter.increment();
    }
    
    public void incrementUnsubscribes() {
        unsubscribeCounter.increment();
    }
    
    public void incrementEmailsSent(EmailType type) {
        emailSentCounter.increment(Tags.of("type", type.name().toLowerCase()));
    }
    
    public Timer.Sample startEmailTimer() {
        return Timer.start();
    }
    
    public void recordEmailSendingTime(Timer.Sample sample) {
        sample.stop(emailSendingTimer);
    }
}
```

---

## 📋 API Endpoints Summary

| Method | Endpoint | Description | Auth | Status |
|--------|----------|-------------|------|--------|
| POST | `/api/newsletter/subscribe` | Subscribe to newsletter | None | 202 |
| GET | `/api/newsletter/confirm?token=` | Confirm subscription | None | 200 |
| GET | `/api/newsletter/unsubscribe?token=` | Unsubscribe | None | 200 |
| GET | `/api/newsletter/subscribers` | List subscribers | ADMIN | 200 |
| DELETE | `/api/newsletter/delete?token=` | Delete personal data | None | 204 |
| GET | `/api/newsletter/my-data?token=` | Get personal data | None | 200 |

---

**Próximo Passo**: Implementar a estratégia de testes detalhada no documento NEWSLETTER_TESTING_STRATEGY.md