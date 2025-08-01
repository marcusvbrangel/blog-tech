-- Terms Acceptance System
-- Feature: Consentimento e aceite de termos
-- Data: Janeiro 2025

-- Criar tabela para armazenar histórico de aceites de termos
CREATE TABLE IF NOT EXISTS terms_acceptance (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    terms_version VARCHAR(10) NOT NULL,
    accepted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    ip_address INET,
    user_agent TEXT,
    
    -- Índices para performance
    CONSTRAINT fk_terms_acceptance_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Criar índices para otimizar consultas
CREATE INDEX IF NOT EXISTS idx_terms_acceptance_user_id ON terms_acceptance(user_id);
CREATE INDEX IF NOT EXISTS idx_terms_acceptance_version ON terms_acceptance(terms_version);
CREATE INDEX IF NOT EXISTS idx_terms_acceptance_accepted_at ON terms_acceptance(accepted_at);
CREATE INDEX IF NOT EXISTS idx_terms_acceptance_user_version ON terms_acceptance(user_id, terms_version);

-- Adicionar coluna na tabela users para versão atual aceita
ALTER TABLE users ADD COLUMN IF NOT EXISTS terms_accepted_version VARCHAR(10);

-- Criar índice na nova coluna
CREATE INDEX IF NOT EXISTS idx_users_terms_version ON users(terms_accepted_version);

-- Comentários para documentação
COMMENT ON TABLE terms_acceptance IS 'Histórico de aceites de termos de uso e política de privacidade pelos usuários';
COMMENT ON COLUMN terms_acceptance.user_id IS 'ID do usuário que aceitou os termos';
COMMENT ON COLUMN terms_acceptance.terms_version IS 'Versão dos termos aceitos (ex: v1.0, v1.1)';
COMMENT ON COLUMN terms_acceptance.accepted_at IS 'Data e hora do aceite';
COMMENT ON COLUMN terms_acceptance.ip_address IS 'Endereço IP do usuário no momento do aceite';
COMMENT ON COLUMN terms_acceptance.user_agent IS 'User-Agent do navegador para auditoria';

COMMENT ON COLUMN users.terms_accepted_version IS 'Última versão dos termos aceita pelo usuário';

-- Inserir dados de exemplo para desenvolvimento (versão inicial dos termos)
-- Usuários existentes receberão NULL, forçando aceite na próxima interação
INSERT INTO terms_acceptance (user_id, terms_version, accepted_at, ip_address, user_agent)
SELECT 
    id, 
    'v1.0', 
    created_at, 
    '127.0.0.1'::inet,
    'Initial migration - Blog API v1.0'
FROM users 
WHERE created_at IS NOT NULL
ON CONFLICT DO NOTHING;

-- Atualizar usuários existentes com versão inicial (opcional para desenvolvimento)
-- Em produção, preferível deixar NULL para forçar novo aceite
-- UPDATE users SET terms_accepted_version = 'v1.0' WHERE terms_accepted_version IS NULL;

-- Verificar integridade dos dados
DO $$
DECLARE
    terms_count INTEGER;
    users_count INTEGER;
BEGIN
    SELECT COUNT(*) INTO terms_count FROM terms_acceptance;
    SELECT COUNT(*) INTO users_count FROM users;
    
    RAISE NOTICE 'Terms Acceptance System initialized successfully';
    RAISE NOTICE 'Total terms acceptance records: %', terms_count;
    RAISE NOTICE 'Total users: %', users_count;
END $$;