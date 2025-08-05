# 43_US06_Envio_Semanal_Implementar_Logic_Multiplos_Posts.md

## 📋 Contexto da Tarefa
- **User Story:** US06 - Envio Semanal
- **Número da Tarefa:** 43/95
- **Complexidade:** Média
- **Estimativa:** 3 horas
- **Dependências:** Tarefas 40, 41, 42
- **Sprint:** Sprint 2

## 🎯 Objetivo
Implementar lógica para processar múltiplos posts no digest.

## 📝 Especificação Técnica

### **Componentes a Implementar:**
- [ ] Lógica de agrupamento e ordenação de posts por relevância
- [ ] Algoritmo de sumarização automática de conteúdo
- [ ] Sistema de categorização de posts por tags/categorias
- [ ] Lógica de limitação (máx 10 posts por digest)
- [ ] Processamento em lote otimizado para performance
- [ ] Deduýlicação de posts similares

### **Integrações Necessárias:**
- **Com PostService:** Busca e processamento de posts
- **Com ContentSummarizer:** Geração de resumos automáticos
- **Com Template Engine:** Organização dos dados para o template
- **Com Cache System:** Cache de posts processados

## ✅ Acceptance Criteria
- [ ] **AC1:** Processa corretamente lista de 1 a 50+ posts
- [ ] **AC2:** Ordena posts por relevância (visualizações, comments, data)
- [ ] **AC3:** Limita digest a máximo 10 posts mais relevantes
- [ ] **AC4:** Gera resumo automático de 150 caracteres por post
- [ ] **AC5:** Agrupa posts por categoria quando aplicável
- [ ] **AC6:** Remove posts duplicados ou muito similares
- [ ] **AC7:** Performance adequada mesmo com 100+ posts de entrada

## 🧪 Testes Requeridos

### **Testes Unitários:**
- [ ] Teste de ordenação por relevância (views, comments, likes)
- [ ] Teste de limitação a 10 posts
- [ ] Teste de geração de resumos automáticos
- [ ] Teste de deduílicação de posts similares
- [ ] Teste de agrupamento por categoria
- [ ] Teste com lista vazia ou nula

### **Testes de Integração:**
- [ ] Teste end-to-end com dados reais do banco
- [ ] Teste de performance com 1000+ posts
- [ ] Teste de integração com sistema de cache
- [ ] Teste de processamento completo do digest

## 🔗 Arquivos Afetados
- [ ] **src/main/java/com/blog/api/newsletter/service/DigestProcessorService.java:** Lógica principal
- [ ] **src/main/java/com/blog/api/newsletter/processor/ContentSummarizer.java:** Sumarização de conteúdo
- [ ] **src/main/java/com/blog/api/newsletter/processor/PostRelevanceCalculator.java:** Cálculo de relevância
- [ ] **src/main/java/com/blog/api/newsletter/dto/ProcessedDigestData.java:** DTO com posts processados
- [ ] **src/test/java/com/blog/api/newsletter/service/DigestProcessorServiceTest.java:** Testes unitários

## 📚 Documentação para IA

### **Contexto do Projeto:**
- **Stack:** Java 21 + Spring Boot 3.2 + PostgreSQL + Redis
- **Arquitetura:** Clean Architecture (Controller → Service → Repository)
- **Padrões:** Builder Pattern, Java Records para DTOs, Cache-First

### **Implementação Esperada:**
1. Criar DigestProcessorService com métodos:
   - processPostsForDigest(List<Post> posts): ProcessedDigestData
   - calculateRelevanceScore(Post post): Double
   - summarizeContent(String content, int maxLength): String
   - deduplicateSimilarPosts(List<Post> posts): List<Post>
2. Implementar algoritmo de relevância:
   - Score = (views * 0.4) + (comments * 0.3) + (likes * 0.2) + (recentness * 0.1)
   - Ordenar por score decrescente
3. Criar ContentSummarizer:
   - Extrair primeiras 150 palavras significativas
   - Remover HTML tags e caracteres especiais
   - Garantir frases completas
4. Implementar deduílicação por similaridade de título (70%+)
5. Agrupar por categoria se mais de 3 posts da mesma categoria

### **Exemplos de Código Existente:**
- **Referência 1:** PostService para padrões de processamento de conteúdo
- **Referência 2:** Utilitários de string no projeto para text processing

## 🔍 Validação e Testes

### **Como Testar:**
1. Criar 15-20 posts de teste com variáveis métricas (views, comments, likes)
2. Executar processPostsForDigest() e verificar ordenação
3. Validar que apenas 10 posts mais relevantes são retornados
4. Testar sumarização de conteúdo com HTML complexo
5. Criar posts com títulos similares e testar deduílicação
6. Verificar agrupamento por categoria
7. Medir performance com 1000+ posts
8. Validar que resumos estão gramaticalmente corretos

### **Critérios de Sucesso:**
- [ ] Ordenação correta por score de relevância
- [ ] Limitação a 10 posts funcionando
- [ ] Resumos gerados são legíveis e informativos
- [ ] Deduílicação remove posts redundantes
- [ ] Performance < 500ms para processar 100 posts
- [ ] Agrupamento por categoria quando aplicável
- [ ] Tratamento adequado de edge cases (zero posts, posts sem conteúdo)

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
- **Estimativa:** 3 horas
- **Real:** ___ horas *(a ser preenchido após implementação)*

### **Complexidade:**
- **Estimada:** Média
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
