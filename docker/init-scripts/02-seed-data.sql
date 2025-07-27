-- Script de dados iniciais para desenvolvimento
-- Este script popula o banco com dados básicos para testes

-- Aguardar que as tabelas sejam criadas pelo JPA
-- Este script pode ser executado manualmente após a aplicação subir

-- Inserir categorias padrão
-- INSERT INTO categories (id, name, description, created_at, updated_at) VALUES
-- (1, 'Tecnologia', 'Posts sobre tecnologia e programação', NOW(), NOW()),
-- (2, 'Lifestyle', 'Posts sobre estilo de vida', NOW(), NOW()),
-- (3, 'Negócios', 'Posts sobre mundo dos negócios', NOW(), NOW()),
-- (4, 'Educação', 'Conteúdo educacional', NOW(), NOW());

-- Inserir usuário administrador padrão
-- Senha: admin123 (deve ser hasheada pela aplicação)
-- INSERT INTO users (id, username, email, password, role, created_at, updated_at) VALUES
-- (1, 'admin', 'admin@blogapi.com', '$2a$10$dummy_hash_here', 'ADMIN', NOW(), NOW());

-- Comentário informativo
SELECT 'Seed data script ready - execute manually after application startup' AS info;