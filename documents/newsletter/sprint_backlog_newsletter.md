# Sprint Backlog – Newsletter para o Blog Tech

---

## 📌 Contexto da Sprint

- **Time:** Backend (Java + Spring + REST API + DB)
- **Duração da Sprint:** 2 semanas
- **Capacidade da Equipe:** 30 pontos
- **Feature:** Newsletter
- **Objetivo da Sprint:** Implementar as principais funcionalidades da newsletter, incluindo inscrição, confirmação, cancelamento, administração, envio automático e conformidade com LGPD.

---

## 🗂️ Sprint Backlog – Estrutura Completa com Critérios e Testes

### 🎯 Feature: Newsletter para o Blog Tech

---

### ✅ US01 – Inscrição de Usuários
*Como visitante, quero me inscrever na newsletter informando meu e-mail, para receber novos conteúdos.*

- **Estimativa:** 5 pontos

#### 🔨 Tarefas técnicas:
- Criar tabela `newsletter_subscriber`
- Criar DTO `NewsletterSubscriptionRequest`
- Criar endpoint POST `/api/newsletter/subscribe`
- Validar e-mail (formato e unicidade)
- Salvar inscrição com status `"PENDING"`

#### ✅ Critérios de Aceitação (ACs):
- AC1: Um e-mail válido registra o status como `"PENDING"`
- AC2: E-mail já inscrito retorna HTTP 409 Conflict
- AC3: E-mail inválido retorna HTTP 400 Bad Request
- AC4: Retorno padrão: HTTP 202 Accepted + mensagem amigável

#### 🧪 Testes Automatizados:
- POST `/subscribe` com e-mail válido → 202 Accepted
- POST com e-mail duplicado → 409 Conflict
- POST com e-mail inválido → 400 Bad Request
- Verificação no banco com status `"PENDING"`

---

### ✅ US02 – Confirmação de E-mail (Double Opt-in)
*Como sistema, quero enviar um e-mail de confirmação após o cadastro, para validar a propriedade do e-mail.*

- **Estimativa:** 5 pontos

#### 🔨 Tarefas técnicas:
- Gerar token (UUID com expiração)
- Criar endpoint GET `/api/newsletter/confirm?token=...`
- Atualizar status para `"CONFIRMED"`
- Integrar com serviço de envio de e-mails
- Criar template de e-mail com link de confirmação

#### ✅ Critérios de Aceitação:
- AC1: Após inscrição, é enviado e-mail de confirmação
- AC2: Token de confirmação tem validade (ex: 48h)
- AC3: Link de confirmação atualiza status para `"CONFIRMED"`
- AC4: Token inválido → 400 / expirado → 410

#### 🧪 Testes Automatizados:
- Envio de e-mail com token
- GET com token válido → 200 OK
- GET com token inválido → 400
- GET com token expirado → 410 Gone
- Verificação no banco com status atualizado

---

### ✅ US03 – Descadastro (Unsubscribe)
*Como assinante, quero poder cancelar minha inscrição, para não receber mais e-mails.*

- **Estimativa:** 3 pontos

#### 🔨 Tarefas técnicas:
- Criar endpoint GET `/api/newsletter/unsubscribe?token=...`
- Atualizar status para `"UNSUBSCRIBED"`
- Registrar log do evento

#### ✅ Critérios de Aceitação:
- AC1: Link de descadastro nos e-mails
- AC2: Status atualizado para `"UNSUBSCRIBED"`
- AC3: Mensagem de sucesso (via frontend)
- AC4: Token inválido retorna HTTP 400

#### 🧪 Testes Automatizados:
- GET com token válido → status `"UNSUBSCRIBED"`
- GET com token inválido → 400
- Log de descadastro gerado
- Verifica se não há mais envio para este e-mail

---

### ✅ US04 – Administração e Gestão de Inscritos
*Como admin, quero listar todos os inscritos da newsletter via API, para gestão.*

- **Estimativa:** 3 pontos

#### 🔨 Tarefas técnicas:
- Criar endpoint GET `/api/newsletter/subscribers`
- Paginação e filtro por status/data
- Proteger com Spring Security (role ADMIN)

#### ✅ Critérios de Aceitação:
- AC1: Apenas usuários ADMIN acessam
- AC2: Suporta paginação e filtros
- AC3: Não expor dados sensíveis

#### 🧪 Testes Automatizados:
- GET com usuário ADMIN → 200 OK
- GET sem autenticação → 401
- GET com ROLE inválido → 403
- Testes de paginação e filtros

---

### ✅ US05 – Envio Automático de Novos Conteúdos
*Como sistema, quero disparar um e-mail para inscritos quando houver novo post.*

- **Estimativa:** 8 pontos

#### 🔨 Tarefas técnicas:
- Criar evento de publicação de post
- Criar ApplicationListener assíncrono
- Consultar inscritos CONFIRMED
- Enviar e-mail com resumo do post

#### ✅ Critérios de Aceitação:
- AC1: Post publicado → evento disparado
- AC2: Apenas CONFIRMED recebem
- AC3: E-mail contém título, resumo, link
- AC4: Envio assíncrono

#### 🧪 Testes Automatizados:
- Listener ativado ao publicar
- Consulta apenas CONFIRMED
- Validação do e-mail
- Teste de performance de envio

---

### ✅ US06 – Envio Semanal com Últimos Posts
*Como assinante, quero receber semanalmente os últimos posts do blog.*

- **Estimativa:** 5 pontos

#### 🔨 Tarefas técnicas:
- Agendar job com Spring Scheduler/Quartz
- Buscar posts dos últimos 7 dias
- Gerar e-mail com resumos
- Enviar para CONFIRMED

#### ✅ Critérios de Aceitação:
- AC1: Job executa semanalmente
- AC2: Busca correta por data
- AC3: E-mail com lista de links
- AC4: Apenas CONFIRMED recebem

#### 🧪 Testes Automatizados:
- Mock do agendador
- Consulta de posts por data
- Validação do e-mail
- Garantir envio só a CONFIRMED

---

### ✅ US07 – Segurança e LGPD (Ajustado para Backend)
*Como administrador, quero garantir que a newsletter esteja em conformidade com a LGPD para proteger os dados dos usuários.*

- **Estimativa:** 4 pontos

#### 🔨 Tarefas técnicas:
- Armazenar flag de consentimento e timestamp no banco
- Criar DTO `ConsentimentoRequest`
- Criar endpoint DELETE `/api/newsletter/delete?token=...` para exclusão total
- Criar endpoint GET `/api/newsletter/consent-history` (restrito a ADMIN)
- Garantir criptografia de dados sensíveis no banco (se necessário)
- Adicionar logs de acesso às informações pessoais

#### ✅ Critérios de Aceitação:
- AC1: Consentimento registrado com data/hora no momento da inscrição
- AC2: Dados do assinante podem ser excluídos via token
- AC3: Dados sensíveis armazenados de forma segura (criptografia/transparência)
- AC4: Histórico de consentimento pode ser consultado via API com ROLE ADMIN

#### 🧪 Testes Automatizados:
- Teste de persistência do consentimento com timestamp
- DELETE com token válido → 204 No Content
- DELETE com token inválido → 400
- Validação de criptografia dos dados (mock/fake layer)
- GET histórico com ADMIN → 200
- GET sem permissão → 403

---

### ✅ US08 – Histórico de Consentimento
*Como administrador, quero manter um log de quando e como o usuário deu consentimento, para atender à LGPD/GDPR.*

#### Critérios de Aceitação:
- AC1: Cada inscrição registra IP, data, agente do navegador
- AC2: Logs podem ser acessados via API (ROLE ADMIN)

#### 🧪 Testes Automatizados:
- Verificação de persistência do log
- GET com ADMIN → 200 OK
- Extração dos dados para auditoria

---

### ✅ US09 – Solicitação de Dados Pessoais
*Como assinante, quero poder solicitar os meus dados armazenados, para saber o que está sendo guardado.*

#### Critérios de Aceitação:
- AC1: Usuário envia request com e-mail/token
- AC2: Sistema retorna dados relacionados a ele

#### 🧪 Testes Automatizados:
- Validação do token
- Retorno dos dados corretos
- Endpoint protegido contra acesso não autorizado

---

# 📋 Sprint Board (Kanban Style)

# 📋 Sprint Board (Kanban Style)

| To Do                                                      | Doing                          | Done                          |
|------------------------------------------------------------|-------------------------------|-------------------------------|
| **US01 – Inscrição de Usuários**                           | Time puxa conforme prioridade | Conforme entrega validada       |
| - Criar tabela newsletter_subscriber                        |                               |                               |
| - Criar DTO NewsletterSubscriptionRequest                   |                               |                               |
| - Criar endpoint POST /api/newsletter/subscribe             |                               |                               |
| - Validar e-mail (formato e unicidade)                      |                               |                               |
| - Salvar inscrição com status "PENDING"                     |                               |                               |
| **US02 – Confirmação de E-mail (Double Opt-in)**            |                               |                               |
| - Gerar token de confirmação (UUID + expiração)             |                               |                               |
| - Criar endpoint GET /api/newsletter/confirm?token=...      |                               |                               |
| - Atualizar status para "CONFIRMED"                         |                               |                               |
| - Integrar com serviço de envio de e-mail                    |                               |                               |
| - Criar template de e-mail com link de confirmação           |                               |                               |
| **US03 – Descadastro (Unsubscribe)**                        |                               |                               |
| - Criar endpoint GET /api/newsletter/unsubscribe?token=...  |                               |                               |
| - Atualizar status para "UNSUBSCRIBED"                      |                               |                               |
| - Registrar log do evento de descadastro                     |                               |                               |
| **US04 – Administração e Gestão de Inscritos**              |                               |                               |
| - Criar endpoint GET /api/newsletter/subscribers             |                               |                               |
| - Paginação e filtros (status, data)                        |                               |                               |
| - Proteger endpoint com Spring Security (role ADMIN)        |                               |                               |
| **US05 – Envio Automático de Novos Conteúdos**              |                               |                               |
| - Criar evento de publicação de post                         |                               |                               |
| - Criar ApplicationListener assíncrono                      |                               |                               |
| - Consultar inscritos com status "CONFIRMED"                |                               |                               |
| - Enviar e-mail com resumo do post                           |                               |                               |
| **US06 – Envio Semanal Automático com Últimos Posts**       |                               |                               |
| - Agendar job semanal (Spring Scheduler ou Quartz)          |                               |                               |
| - Buscar posts dos últimos 7 dias                            |                               |                               |
| - Gerar e-mail com resumos e links                           |                               |                               |
| - Enviar para todos os inscritos CONFIRMED                  |                               |                               |
| **US07 – Segurança e LGPD (Backend)**                       |                               |                               |
| - Armazenar flag de consentimento e timestamp no banco      |                               |                               |
| - Criar DTO ConsentimentoRequest                             |                               |                               |
| - Criar endpoint DELETE /api/newsletter/delete?token=...    |                               |                               |
| - Criar endpoint GET /api/newsletter/consent-history (ADMIN)|                               |                               |
| - Garantir criptografia e proteção de dados sensíveis       |                               |                               |
| - Criar logs de acesso às informações pessoais               |                               |                               |
| **US08 – Histórico de Consentimento**                       |                               |                               |
| - Registrar IP, data, agente do navegador no consentimento  |                               |                               |
| - Disponibilizar logs via API (ROLE ADMIN)                   |                               |                               |
| **US09 – Solicitação de Dados Pessoais (LGPD)**             |                               |                               |
| - Criar endpoint para solicitação de dados via token/e-mail |                               |                               |
| - Validar token de acesso                                    |                               |                               |
| - Gerar relatório dos dados armazenados                      |                               |                               |
