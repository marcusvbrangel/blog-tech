# 37_US04_Administracao_Configurar_Rate_Limiting.md

## 📋 Contexto da Tarefa
- **User Story:** US04 - Administração
- **Número da Tarefa:** 37/96
- **Complexidade:** Média | **Estimativa:** 3h | **Sprint:** 2

## 🎯 Objetivo
Configurar rate limiting para endpoints administrativos usando Redis para prevenir abuso.

## 📝 Componentes
- [ ] Rate limiting annotation
- [ ] Redis para contadores
- [ ] Diferentes limits por endpoint
- [ ] Headers de rate limit
- [ ] Admin dashboard metrics

## 📚 Implementação
```java
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimit {
    int requests() default 100;
    int perMinutes() default 60;
    String key() default "";
}

@Component
@RequiredArgsConstructor
public class RateLimitInterceptor implements HandlerInterceptor {
    
    private final RedisTemplate<String, Object> redisTemplate;
    
    @Override
    public boolean preHandle(HttpServletRequest request, 
                           HttpServletResponse response, 
                           Object handler) throws Exception {
        // Rate limiting logic
    }
}
```

## ✅ Definition of Done
- [ ] Rate limiting implementado
- [ ] Redis integration funcionando  
- [ ] Headers apropriados retornados
- [ ] Metrics coletadas

---
**Responsável:** AI-Driven Development