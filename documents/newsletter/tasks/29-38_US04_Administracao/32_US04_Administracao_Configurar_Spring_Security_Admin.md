# 32_US04_Administracao_Configurar_Spring_Security_Admin.md

## 📋 Contexto da Tarefa
- **User Story:** US04 - Administração
- **Número da Tarefa:** 32/96
- **Complexidade:** Alta | **Estimativa:** 4h | **Sprint:** 2

## 🎯 Objetivo
Configurar Spring Security para endpoints administrativos com role ADMIN e JWT.

## 📝 Componentes
- [ ] SecurityConfig para /admin/**
- [ ] Role ADMIN configurada
- [ ] JWT authentication
- [ ] CORS para admin panel
- [ ] Rate limiting

## ✅ Acceptance Criteria
- [ ] Apenas ADMIN acessa endpoints /admin/**
- [ ] JWT authentication funcionando
- [ ] CORS configurado
- [ ] Rate limiting implementado

## 📚 Implementação
```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class AdminSecurityConfig {
    
    @Bean
    @Order(1)
    public SecurityFilterChain adminFilterChain(HttpSecurity http) throws Exception {
        return http
            .securityMatcher("/admin/**")
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/admin/**").hasRole("ADMIN")
            )
            .oauth2ResourceServer(oauth2 -> oauth2.jwt())
            .build();
    }
}
```

## ✅ Definition of Done
- [ ] Spring Security configurado
- [ ] Role ADMIN funcionando
- [ ] JWT validation implementada
- [ ] Testes de autorização passando

---
**Responsável:** AI-Driven Development