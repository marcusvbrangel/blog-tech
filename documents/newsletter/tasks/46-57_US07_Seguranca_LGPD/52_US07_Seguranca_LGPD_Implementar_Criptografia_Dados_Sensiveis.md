# 52_US07_Seguranca_LGPD_Implementar_Criptografia_Dados_Sensiveis.md

### ✅ US07 – Segurança LGPD
*Como titular de dados, quero que meus dados pessoais sejam protegidos e gerenciados conforme a LGPD, para garantir minha privacidade e direitos.*

## 📋 Descrição da Tarefa
**Implementar Criptografia de Dados Sensíveis**

Estabelece sistema robusto de criptografia AES-256-GCM para proteção automática de dados pessoais no banco de dados.
Implementa rotação de chaves, conversores JPA transparentes e compatibilidade reversa para migração segura de dados existentes.

## 📋 Contexto da Tarefa
- **User Story:** US07 - Segurança LGPD
- **Número da Tarefa:** 52/95
- **Complexidade:** Alta
- **Estimativa:** 5 horas
- **Dependências:** Tarefas 01, 48
- **Sprint:** Sprint 3

## 🎯 Objetivo
Implementar criptografia de dados sensíveis no banco.

## 📝 Especificação Técnica

### **Componentes a Implementar:**
- [ ] EncryptedStringConverter para criptografia JPA automática
- [ ] CryptoService para criptografia/descriptografia de dados
- [ ] Configuração de chaves de criptografia (AES-256)
- [ ] Rotação de chaves de criptografia
- [ ] Migração de dados existentes para formato criptografado
- [ ] Key Management Service (KMS) integration
- [ ] Auditoria de operações de criptografia

### **Integrações Necessárias:**
- **Com JPA/Hibernate:** Conversores automáticos para campos sensíveis
- **Com NewsletterConsentLog:** Criptografia de email, IP, userAgent
- **Com AWS KMS/HashiCorp Vault:** Gerenciamento seguro de chaves

## ✅ Acceptance Criteria
- [ ] **AC1:** Criptografia AES-256-GCM para dados pessoais (email, IP, userAgent)
- [ ] **AC2:** EncryptedStringConverter aplicado automaticamente via @Convert
- [ ] **AC3:** Chaves de criptografia armazenadas externamente (não no código)
- [ ] **AC4:** Rotação automática de chaves a cada 90 dias
- [ ] **AC5:** Migração de dados existentes sem perda
- [ ] **AC6:** Performance: overhead máximo de 10ms para criptografia/descriptografia
- [ ] **AC7:** Logs de auditoria para todas as operações de criptografia
- [ ] **AC8:** Backup seguro das chaves de criptografia
- [ ] **AC9:** Fallback para dados não criptografados (compatibility mode)

## 🧪 Testes Requeridos

### **Testes Unitários:**
- [ ] Teste de criptografia/descriptografia básica
- [ ] Teste de EncryptedStringConverter
- [ ] Teste de geração e rotação de chaves
- [ ] Teste de compatibility mode (dados não criptografados)
- [ ] Teste de recuperação de erro (chave inválida)
- [ ] Teste de performance de criptografia

### **Testes de Integração:**
- [ ] Teste de persistência com dados criptografados
- [ ] Teste de migração de dados existentes
- [ ] Teste de integração com KMS externo
- [ ] Teste de backup e restore de chaves

## 🔗 Arquivos Afetados
- [ ] **src/main/java/com/blog/api/security/crypto/CryptoService.java** - Serviço principal
- [ ] **src/main/java/com/blog/api/security/crypto/EncryptedStringConverter.java** - Conversor JPA
- [ ] **src/main/java/com/blog/api/security/crypto/KeyManagementService.java** - Gerenciamento de chaves
- [ ] **src/main/java/com/blog/api/security/crypto/CryptoConfig.java** - Configuração
- [ ] **src/main/resources/db/migration/V009__encrypt_existing_data.sql** - Migração
- [ ] **src/test/java/com/blog/api/security/crypto/CryptoServiceTest.java** - Testes

## 📚 Documentação para IA

### **Contexto do Projeto:**
- **Stack:** Java 21 + Spring Boot 3.2 + PostgreSQL + Redis
- **Arquitetura:** Clean Architecture (Controller → Service → Repository)
- **Padrões:** Builder Pattern, Java Records para DTOs, Cache-First

### **Implementação Esperada:**

**CryptoService.java:**
```java
@Service
public class CryptoService {
    
    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int GCM_IV_LENGTH = 12;
    private static final int GCM_TAG_LENGTH = 16;
    
    @Autowired
    private KeyManagementService keyManagementService;
    
    public String encrypt(String plaintext) {
        try {
            SecretKey key = keyManagementService.getCurrentKey();
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            
            byte[] iv = new byte[GCM_IV_LENGTH];
            SecureRandom.getInstanceStrong().nextBytes(iv);
            
            GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv);
            cipher.init(Cipher.ENCRYPT_MODE, key, gcmSpec);
            
            byte[] ciphertext = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));
            
            // Combinar IV + dados criptografados
            byte[] encryptedWithIv = new byte[iv.length + ciphertext.length];
            System.arraycopy(iv, 0, encryptedWithIv, 0, iv.length);
            System.arraycopy(ciphertext, 0, encryptedWithIv, iv.length, ciphertext.length);
            
            return Base64.getEncoder().encodeToString(encryptedWithIv);
            
        } catch (Exception e) {
            throw new CryptoException("Erro na criptografia", e);
        }
    }
    
    public String decrypt(String encryptedData) {
        try {
            byte[] encryptedWithIv = Base64.getDecoder().decode(encryptedData);
            
            // Extrair IV e dados criptografados
            byte[] iv = new byte[GCM_IV_LENGTH];
            byte[] ciphertext = new byte[encryptedWithIv.length - GCM_IV_LENGTH];
            
            System.arraycopy(encryptedWithIv, 0, iv, 0, iv.length);
            System.arraycopy(encryptedWithIv, iv.length, ciphertext, 0, ciphertext.length);
            
            SecretKey key = keyManagementService.getCurrentKey();
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            
            GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv);
            cipher.init(Cipher.DECRYPT_MODE, key, gcmSpec);
            
            byte[] plaintext = cipher.doFinal(ciphertext);
            return new String(plaintext, StandardCharsets.UTF_8);
            
        } catch (Exception e) {
            // Tentar com chaves antigas (rotação)
            return decryptWithOldKeys(encryptedData);
        }
    }
}
```

**EncryptedStringConverter.java:**
```java
@Converter
public class EncryptedStringConverter implements AttributeConverter<String, String> {
    
    @Autowired
    private CryptoService cryptoService;
    
    @Override
    public String convertToDatabaseColumn(String attribute) {
        if (attribute == null || attribute.isEmpty()) {
            return attribute;
        }
        return cryptoService.encrypt(attribute);
    }
    
    @Override
    public String convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isEmpty()) {
            return dbData;
        }
        
        // Verificar se já é um dado criptografado
        if (isEncrypted(dbData)) {
            return cryptoService.decrypt(dbData);
        }
        
        // Compatibility mode - dados não criptografados
        return dbData;
    }
    
    private boolean isEncrypted(String data) {
        try {
            Base64.getDecoder().decode(data);
            return data.length() > 20; // Dados criptografados são sempre longos
        } catch (Exception e) {
            return false;
        }
    }
}
```

**Uso na Entidade:**
```java
@Entity
public class NewsletterConsentLog {
    
    @Convert(converter = EncryptedStringConverter.class)
    @Column(nullable = false)
    private String email;
    
    @Convert(converter = EncryptedStringConverter.class)
    private String ipAddress;
    
    @Convert(converter = EncryptedStringConverter.class)
    private String userAgent;
}
```

### **Referências de Código:**
- **Spring Security:** Padrões de segurança do projeto
- **JPA Converters:** Padrões de conversão de dados

## 🔍 Validação e Testes

### **Como Testar:**
1. Testar criptografia: `mvn test -Dtest=CryptoServiceTest`
2. Verificar persistência: salvar entidade e verificar dados criptografados no banco
3. Testar descriptografia: recuperar entidade e verificar dados descriptografados
4. Executar migração: `mvn flyway:migrate`
5. Testar performance: medir tempo de criptografia/descriptografia
6. Testar rotação de chaves
7. Verificar logs de auditoria de criptografia

### **Critérios de Sucesso:**
- [ ] Funcionalidade implementada
- [ ] Testes passando
- [ ] Performance adequada

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
- **Estimativa:** 5 horas
- **Real:** ___ horas *(a ser preenchido após implementação)*

### **Complexidade:**
- **Estimada:** Alta
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
