# 40_US05_Envio_Automatico_Implementar_NewsletterEventListener.md

## 📋 Contexto da Tarefa
- **User Story:** US05 - Envio Automático | **Tarefa:** 40/96
- **Complexidade:** Média | **Estimativa:** 3h | **Sprint:** 2

## 🎯 Objetivo
Implementar listener para PostPublishedEvent que dispara envio automático de newsletter.

## 📝 Componentes
- [ ] NewsletterEventListener
- [ ] @EventListener para PostPublishedEvent
- [ ] Async processing
- [ ] Error handling e retry
- [ ] Logs de auditoria

## 📚 Implementação
```java
@Component
@Slf4j
@RequiredArgsConstructor
public class NewsletterEventListener {
    
    private final NewsletterEmailService emailService;
    private final NewsletterRepository newsletterRepository;
    
    @EventListener
    @Async
    @Retryable(value = {Exception.class}, maxAttempts = 3)
    public void handlePostPublished(PostPublishedEvent event) {
        log.info("Handling post published event for post: {}", event.getPostId());
        
        try {
            var confirmedSubscribers = newsletterRepository
                .findByStatus(SubscriptionStatus.CONFIRMED);
            
            emailService.sendNewPostNotification(confirmedSubscribers, event);
            
            log.info("Newsletter sent to {} subscribers for post: {}", 
                confirmedSubscribers.size(), event.getPostId());
                
        } catch (Exception e) {
            log.error("Failed to send newsletter for post: {}", event.getPostId(), e);
            throw e;
        }
    }
}
```

## ✅ Definition of Done
- [ ] Event listener implementado
- [ ] Async processing funcionando  
- [ ] Error handling implementado
- [ ] Logs de auditoria registrados

---
**Responsável:** AI-Driven Development