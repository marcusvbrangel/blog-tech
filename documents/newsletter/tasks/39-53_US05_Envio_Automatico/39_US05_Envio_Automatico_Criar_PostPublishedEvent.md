# 39_US05_Envio_Automatico_Criar_PostPublishedEvent.md

## 📋 Contexto da Tarefa
- **User Story:** US05 - Envio Automático de Newsletter
- **Número da Tarefa:** 39/96
- **Complexidade:** Baixa | **Estimativa:** 2h | **Sprint:** 2

## 🎯 Objetivo
Criar evento Spring para ser disparado quando um novo post é publicado, permitindo envio automático de newsletter.

## 📝 Componentes
- [ ] PostPublishedEvent class
- [ ] Integration com PostService
- [ ] Event data (post info, timestamp)
- [ ] Async event handling support

## ✅ Acceptance Criteria
- [ ] Evento criado como ApplicationEvent
- [ ] Dados do post incluídos no evento
- [ ] Timestamp de publicação registrado
- [ ] Integration com PostService existente

## 📚 Implementação
```java
public class PostPublishedEvent extends ApplicationEvent {
    
    private final Long postId;
    private final String title;
    private final String slug;
    private final String excerpt;
    private final LocalDateTime publishedAt;
    
    public PostPublishedEvent(Object source, Long postId, String title, 
                            String slug, String excerpt) {
        super(source);
        this.postId = postId;
        this.title = title;
        this.slug = slug;
        this.excerpt = excerpt;
        this.publishedAt = LocalDateTime.now();
    }
    
    // Getters
}

// Integration in PostService
@Service
public class PostServiceImpl {
    
    private final ApplicationEventPublisher eventPublisher;
    
    public PostResponse publishPost(Long postId) {
        var post = postRepository.findById(postId);
        post.setStatus(PostStatus.PUBLISHED);
        post.setPublishedAt(LocalDateTime.now());
        
        var savedPost = postRepository.save(post);
        
        // Publish event for newsletter
        eventPublisher.publishEvent(new PostPublishedEvent(
            this, savedPost.getId(), savedPost.getTitle(), 
            savedPost.getSlug(), savedPost.getExcerpt()
        ));
        
        return PostResponse.from(savedPost);
    }
}
```

## ✅ Definition of Done
- [ ] PostPublishedEvent criado
- [ ] Integration com PostService
- [ ] Event publishing funcionando
- [ ] Testes unitários passando

---
**Responsável:** AI-Driven Development