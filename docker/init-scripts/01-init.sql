-- Script de inicialização do banco de dados Blog API
-- Este script é executado automaticamente quando o container PostgreSQL é criado

-- O database blogdb já é criado automaticamente pelo docker-compose via POSTGRES_DB

-- Criar extensões úteis
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pg_trgm";

-- Configurações de performance
ALTER SYSTEM SET shared_preload_libraries = 'pg_stat_statements';
ALTER SYSTEM SET log_statement = 'all';
ALTER SYSTEM SET log_duration = on;

-- Configurar timezone
SET timezone = 'America/Sao_Paulo';

-- Criar índices úteis para performance (serão aplicados após JPA criar as tabelas)
-- Estes comandos falharão na primeira execução, mas serão úteis após a aplicação subir

-- Comentário informativo
SELECT 'Blog API Database initialized successfully!' AS status;