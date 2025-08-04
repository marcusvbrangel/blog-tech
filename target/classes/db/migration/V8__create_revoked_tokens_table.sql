-- =====================================================================
-- JWT Blacklist Implementation - Revoked Tokens Table
-- =====================================================================
-- Version: 8.0
-- Description: Creates table for storing revoked JWT tokens to enable 
--              logout functionality and token blacklisting
-- Author: AI-Driven Development Team
-- Date: 2025-08-02
-- =====================================================================

-- Create revoked_tokens table for JWT blacklist functionality
CREATE TABLE revoked_tokens (
    -- Primary key
    id BIGSERIAL PRIMARY KEY,
    
    -- JWT unique identifier (JTI claim)
    token_jti VARCHAR(36) NOT NULL UNIQUE,
    
    -- User who owned the token (nullable for flexibility)
    user_id BIGINT REFERENCES users(id) ON DELETE CASCADE,
    
    -- When the token was revoked
    revoked_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    -- Original expiration time of the token (for cleanup purposes)
    expires_at TIMESTAMP NOT NULL,
    
    -- Reason for revocation
    reason VARCHAR(20) NOT NULL CHECK (reason IN ('LOGOUT', 'ADMIN_REVOKE', 'PASSWORD_CHANGE', 'ACCOUNT_LOCKED', 'SECURITY_BREACH')),
    
    -- Record creation timestamp
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- =====================================================================
-- Performance Indexes
-- =====================================================================

-- Primary lookup index for blacklist checks (most critical for performance)
CREATE INDEX idx_revoked_tokens_jti ON revoked_tokens(token_jti);

-- Cleanup index for expired tokens removal
CREATE INDEX idx_revoked_tokens_expires_at ON revoked_tokens(expires_at);

-- User-based queries index
CREATE INDEX idx_revoked_tokens_user_id ON revoked_tokens(user_id);

-- Analytics and monitoring index
CREATE INDEX idx_revoked_tokens_reason ON revoked_tokens(reason);

-- Time-based queries index
CREATE INDEX idx_revoked_tokens_revoked_at ON revoked_tokens(revoked_at);

-- Composite index for user + time queries (rate limiting)
CREATE INDEX idx_revoked_tokens_user_time ON revoked_tokens(user_id, revoked_at);

-- Composite index for cleanup operations
CREATE INDEX idx_revoked_tokens_expires_reason ON revoked_tokens(expires_at, reason);

-- =====================================================================
-- Table Comments and Documentation
-- =====================================================================

COMMENT ON TABLE revoked_tokens IS 'Stores JWT tokens that have been revoked before their natural expiration';

COMMENT ON COLUMN revoked_tokens.id IS 'Primary key for the revoked token record';
COMMENT ON COLUMN revoked_tokens.token_jti IS 'JWT ID (jti claim) - unique identifier from the token';
COMMENT ON COLUMN revoked_tokens.user_id IS 'ID of the user who owned the token';
COMMENT ON COLUMN revoked_tokens.revoked_at IS 'Timestamp when the token was revoked';
COMMENT ON COLUMN revoked_tokens.expires_at IS 'Original expiration time of the JWT token';
COMMENT ON COLUMN revoked_tokens.reason IS 'Reason for token revocation: LOGOUT, ADMIN_REVOKE, PASSWORD_CHANGE, ACCOUNT_LOCKED, SECURITY_BREACH';
COMMENT ON COLUMN revoked_tokens.created_at IS 'Timestamp when this record was created';

-- =====================================================================
-- Security and Constraints
-- =====================================================================

-- Ensure JTI is always provided
ALTER TABLE revoked_tokens ADD CONSTRAINT chk_token_jti_not_empty 
    CHECK (token_jti IS NOT NULL AND length(trim(token_jti)) > 0);

-- Ensure expiration date is in the future when token is created
-- (Note: This constraint allows historical tokens for audit purposes)
ALTER TABLE revoked_tokens ADD CONSTRAINT chk_expires_at_reasonable 
    CHECK (expires_at >= revoked_at - INTERVAL '24 hours');

-- Ensure revoked_at is not in the future
ALTER TABLE revoked_tokens ADD CONSTRAINT chk_revoked_at_not_future 
    CHECK (revoked_at <= CURRENT_TIMESTAMP + INTERVAL '1 minute');

-- =====================================================================
-- Performance and Maintenance
-- =====================================================================

-- Set table storage parameters for optimal performance
ALTER TABLE revoked_tokens SET (
    fillfactor = 90,  -- Leave some space for updates
    autovacuum_vacuum_scale_factor = 0.1,  -- More frequent vacuum
    autovacuum_analyze_scale_factor = 0.05  -- More frequent analyze
);

-- =====================================================================
-- Initial Data and Configuration
-- =====================================================================

-- No initial data required - table starts empty

-- =====================================================================
-- Grant Permissions (adjust based on your security model)
-- =====================================================================

-- Grant necessary permissions to application user
-- GRANT SELECT, INSERT, DELETE ON revoked_tokens TO blog_api_user;
-- GRANT USAGE ON SEQUENCE revoked_tokens_id_seq TO blog_api_user;

-- =====================================================================
-- Statistics and Monitoring Setup
-- =====================================================================

-- Ensure PostgreSQL collects statistics on this table
ANALYZE revoked_tokens;

-- =====================================================================
-- Verification Queries (for testing)
-- =====================================================================

-- Verify table structure
-- SELECT column_name, data_type, is_nullable, column_default 
-- FROM information_schema.columns 
-- WHERE table_name = 'revoked_tokens' 
-- ORDER BY ordinal_position;

-- Verify indexes
-- SELECT indexname, indexdef 
-- FROM pg_indexes 
-- WHERE tablename = 'revoked_tokens';

-- Verify constraints
-- SELECT constraint_name, constraint_type 
-- FROM information_schema.table_constraints 
-- WHERE table_name = 'revoked_tokens';

-- =====================================================================
-- Migration Rollback (if needed)
-- =====================================================================

-- To rollback this migration:
-- DROP TABLE IF EXISTS revoked_tokens CASCADE;

-- =====================================================================
-- Performance Notes
-- =====================================================================

-- 1. The idx_revoked_tokens_jti index is critical for blacklist lookup performance
-- 2. The idx_revoked_tokens_expires_at index is essential for cleanup operations
-- 3. Consider partitioning by revoked_at if you expect very high volume
-- 4. Monitor index usage and adjust as needed based on query patterns
-- 5. Set up regular cleanup of expired tokens to maintain performance

-- =====================================================================
-- Security Notes
-- =====================================================================

-- 1. JTI values should be UUIDs for security (handled by application)
-- 2. Consider data retention policies for audit and compliance
-- 3. Monitor for unusual revocation patterns that might indicate attacks
-- 4. The table includes CASCADE DELETE to maintain referential integrity

-- =====================================================================
-- End of Migration
-- =====================================================================