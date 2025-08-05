# 26_US04_Administracao_Configurar_Spring_Security_Admin.md

### ✅ US04 – Administração
*Como administrador, quero visualizar e gerenciar todos os inscritos da newsletter, para ter controle administrativo do sistema.*

## 📋 Descrição da Tarefa
**Configurar Spring Security Admin**

Configurar Spring Security para proteger todos os endpoints administrativos (/api/admin/**) com autorização ROLE_ADMIN.
Implementar validação de JWT tokens, method-level security e tratamento customizado de erros de autorização.

## 📋 Contexto da Tarefa
- **User Story:** US04 - Administração
- **Número da Tarefa:** 26/95
- **Complexidade:** Baixa
- **Estimativa:** 2 horas
- **Dependências:** Tarefa 24
- **Sprint:** Sprint 2

## 🎯 Objetivo
Configurar Spring Security para proteger endpoints administrativos com ROLE_ADMIN.

## 📝 Especificação Técnica

### **Componentes a Implementar:**
- [ ] Atualizar SecurityConfig para proteger endpoints /api/admin/**
- [ ] Configurar authority mapping para ROLE_ADMIN
- [ ] Implementar AdminAccessDeniedHandler para respostas 403
- [ ] Adicionar @PreAuthorize nos controllers administrativos
- [ ] Configurar CORS para endpoints administrativos

### **Integrações Necessárias:**
- **Com JWT Authentication:** Verificar roles no token JWT
- **Com UserDetailsService:** Carregar authorities do usuário
- **Com Method Security:** @PreAuthorize e @EnableMethodSecurity
- **Com Error Handling:** Custom AccessDeniedHandler
- **Com AdminNewsletterController:** Proteção de endpoints

## ✅ Acceptance Criteria
- [ ] **AC1:** Endpoints /api/admin/** só acessíveis com ROLE_ADMIN
- [ ] **AC2:** Usuários sem ROLE_ADMIN recebem 403 Forbidden
- [ ] **AC3:** Usuários não autenticados recebem 401 Unauthorized
- [ ] **AC4:** @PreAuthorize funciona corretamente nos controllers
- [ ] **AC5:** JWT token contém authority 'ROLE_ADMIN' para admins
- [ ] **AC6:** Logs de segurança registram tentativas de acesso negado

## 🧪 Testes Requeridos

### **Testes Unitários:**
- [ ] Teste SecurityConfig com MockMvc e diferentes roles
- [ ] Teste @PreAuthorize com @WithMockUser(roles = "ADMIN")
- [ ] Teste acesso negado com @WithMockUser(roles = "USER")
- [ ] Teste sem autenticação retorna 401
- [ ] Teste AdminAccessDeniedHandler personalizado
- [ ] Teste JWT claims com ROLE_ADMIN

### **Testes de Integração:**
- [ ] Teste end-to-end com usuário admin real via TestRestTemplate
- [ ] Teste de segurança com diferentes níveis de autorização
- [ ] Teste de JWT token válido com ROLE_ADMIN
- [ ] Teste de JWT token expirado ou inválido
- [ ] Teste de CORS para endpoints administrativos

## 🔗 Arquivos Afetados
- [ ] **src/main/java/com/blog/api/config/SecurityConfig.java:** Configurações de segurança
- [ ] **src/main/java/com/blog/api/security/AdminAccessDeniedHandler.java:** Handler personalizado
- [ ] **src/main/java/com/blog/api/controller/admin/AdminNewsletterController.java:** Anotações @PreAuthorize
- [ ] **src/main/java/com/blog/api/service/UserDetailsServiceImpl.java:** Carregar authorities
- [ ] **src/test/java/com/blog/api/security/AdminSecurityTest.java:** Testes de segurança

## 📚 Documentação para IA

### **Contexto do Projeto:**
- **Stack:** Java 21 + Spring Boot 3.2 + PostgreSQL + Redis
- **Arquitetura:** Clean Architecture (Controller → Service → Repository)
- **Padrões:** Builder Pattern, Java Records para DTOs, Cache-First

### **Implementação Esperada:**
Configurar Spring Security para:
- Proteger endpoints /api/admin/** com requiresAuthentication() e hasRole('ADMIN')
- Adicionar @PreAuthorize("hasRole('ADMIN')") nos controllers administrativos
- Implementar AdminAccessDeniedHandler para respostas JSON 403
- Configurar authority mapping no JWT token
- Habilitar @EnableMethodSecurity(prePostEnabled = true)
- Adicionar logs de auditoria para tentativas de acesso

### **Exemplos de Código Existente:**
- **Referência 1:** SecurityConfig.java - configuração existente de segurança
- **Referência 2:** JwtAuthenticationFilter.java - extração de authorities
- **Referência 3:** UserController.java - exemplo de @PreAuthorize
- **Referência 4:** CustomAccessDeniedHandler.java - pattern de error handling

## 🔍 Validação e Testes

### **Como Testar:**
1. Criar usuário admin com ROLE_ADMIN no banco
2. Obter JWT token para usuário admin
3. Testar GET /api/admin/newsletter/subscribers com token admin (deve retornar 200)
4. Testar mesmo endpoint com usuário comum (deve retornar 403)
5. Testar sem Authorization header (deve retornar 401)
6. Verificar logs de segurança para tentativas negadas
7. Testar token expirado ou malformado

### **Critérios de Sucesso:**
- [ ] Apenas ROLE_ADMIN acessa endpoints administrativos
- [ ] Responses 401/403 corretas para diferentes cenários
- [ ] JWT token contém authorities corretas
- [ ] Method-level security funciona com @PreAuthorize
- [ ] Logs de auditoria capturam tentativas de acesso
- [ ] Testes de segurança cobrem todos os cenários
- [ ] Performance não impactada pela camada de segurança

## ✅ Definition of Done

### **Código:**
- [ ] Implementação completa seguindo padrões do projeto
- [ ] Code review interno (self-review)
- [ ] Sem warnings ou erros de compilação
- [ ] Logging apropriado implementado

### **Testes:**
- [ ] Testes unitários implementados e passando
- [ ] Testes de integração implementados (se aplicável)
- [ ] Cobertura de código ≥ 85% para componentes novos
- [ ] Todos os ACs validados via testes

### **Documentação:**
- [ ] Javadoc atualizado para métodos públicos
- [ ] Swagger/OpenAPI atualizado (se endpoint)
- [ ] README atualizado (se necessário)
- [ ] Este arquivo de tarefa atualizado com notas de implementação

### **Quality Gates:**
- [ ] Performance dentro dos SLAs (< 200ms para endpoints)
- [ ] Security validation (input validation, authorization)
- [ ] OWASP compliance (se aplicável)
- [ ] Cache strategy implementada (se aplicável)

## 📊 Métricas

### **Estimativa vs Real:**
- **Estimativa:** 2 horas
- **Real:** ___ horas *(a ser preenchido após implementação)*

### **Complexidade:**
- **Estimada:** Baixa
- **Real:** _____ *(a ser preenchido após implementação)*

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
*[Próxima tarefa da sequência]*

---

**Criado em:** Agosto 2025  
**Última Atualização:** Agosto 2025  
**Responsável:** AI-Driven Development  
**Reviewer:** [Nome do reviewer, se aplicável]
