-- =====================================================================
-- V9__create_refresh_tokens_table.sql
-- 
-- Creates the refresh_tokens table for JWT refresh token management.
-- Includes optimized indexes for high-performance token operations.
-- =====================================================================

-- Create refresh_tokens table
CREATE TABLE refresh_tokens (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    token VARCHAR(255) NOT NULL UNIQUE,
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_used TIMESTAMP NULL,
    device_info VARCHAR(500) NULL,
    ip_address VARCHAR(45) NULL,
    revoked BOOLEAN DEFAULT FALSE,
    revoked_at TIMESTAMP NULL,
    
    -- Foreign key constraint
    CONSTRAINT fk_refresh_tokens_user_id 
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- =====================================================================
-- Performance Indexes
-- =====================================================================

-- Primary lookup index for token validation (most critical)
-- Used by: findActiveByToken(), existsActiveByToken()
CREATE INDEX idx_refresh_tokens_token_active 
ON refresh_tokens (token) 
WHERE revoked = FALSE AND expires_at > CURRENT_TIMESTAMP;

-- User tokens index for session management
-- Used by: findActiveByUserId(), countActiveByUserId()
CREATE INDEX idx_refresh_tokens_user_active 
ON refresh_tokens (user_id, created_at DESC) 
WHERE revoked = FALSE AND expires_at > CURRENT_TIMESTAMP;

-- Cleanup index for expired tokens
-- Used by: deleteExpiredTokens(), findExpiredTokens()
CREATE INDEX idx_refresh_tokens_expires_at 
ON refresh_tokens (expires_at);

-- Cleanup index for old revoked tokens  
-- Used by: deleteOldRevokedTokens()
CREATE INDEX idx_refresh_tokens_revoked_cleanup 
ON refresh_tokens (revoked_at) 
WHERE revoked = TRUE;

-- Security monitoring index for IP-based analysis
-- Used by: findTokensByIpAddress()
CREATE INDEX idx_refresh_tokens_ip_created 
ON refresh_tokens (ip_address, created_at DESC);

-- Rate limiting index for user token creation
-- Used by: hasUserExceededTokenCreationRate()
CREATE INDEX idx_refresh_tokens_user_created 
ON refresh_tokens (user_id, created_at DESC);

-- =====================================================================
-- Table Comments and Documentation
-- =====================================================================

-- Table comment
COMMENT ON TABLE refresh_tokens IS 
'Stores JWT refresh tokens for long-lived authentication sessions. 
Supports token rotation, rate limiting, and comprehensive cleanup.';

-- Column comments
COMMENT ON COLUMN refresh_tokens.id IS 'Primary key';
COMMENT ON COLUMN refresh_tokens.user_id IS 'Foreign key to users table';
COMMENT ON COLUMN refresh_tokens.token IS 'Unique refresh token string (UUID-based)';
COMMENT ON COLUMN refresh_tokens.expires_at IS 'Token expiration timestamp';
COMMENT ON COLUMN refresh_tokens.created_at IS 'Token creation timestamp';
COMMENT ON COLUMN refresh_tokens.last_used IS 'Last time token was used for refresh';
COMMENT ON COLUMN refresh_tokens.device_info IS 'Optional device/browser information';
COMMENT ON COLUMN refresh_tokens.ip_address IS 'IP address where token was created';
COMMENT ON COLUMN refresh_tokens.revoked IS 'Whether token has been manually revoked';
COMMENT ON COLUMN refresh_tokens.revoked_at IS 'Timestamp when token was revoked';

-- Index comments
COMMENT ON INDEX idx_refresh_tokens_token_active IS 
'Critical performance index for active token validation';

COMMENT ON INDEX idx_refresh_tokens_user_active IS 
'Performance index for user session management queries';

COMMENT ON INDEX idx_refresh_tokens_expires_at IS 
'Index for efficient cleanup of expired tokens';

-- =====================================================================
-- Initial Configuration and Validation
-- =====================================================================

-- Verify table structure
DO $$
BEGIN
    -- Check if table was created successfully
    IF NOT EXISTS (SELECT 1 FROM information_schema.tables 
                   WHERE table_name = 'refresh_tokens' AND table_schema = 'public') THEN
        RAISE EXCEPTION 'Failed to create refresh_tokens table';
    END IF;
    
    -- Check if primary indexes exist
    IF NOT EXISTS (SELECT 1 FROM pg_indexes 
                   WHERE tablename = 'refresh_tokens' AND indexname = 'idx_refresh_tokens_token_active') THEN
        RAISE EXCEPTION 'Failed to create critical token index';
    END IF;
    
    RAISE NOTICE 'Refresh tokens table created successfully with % indexes', 
        (SELECT COUNT(*) FROM pg_indexes WHERE tablename = 'refresh_tokens');
END $$;

-- =====================================================================
-- Security and Maintenance Notes
-- =====================================================================

/*
SECURITY CONSIDERATIONS:
1. The token column contains sensitive data - ensure application-level encryption if needed
2. IP addresses are stored for security monitoring - comply with privacy regulations
3. Device info may contain user-identifying information - handle according to privacy policy
4. Regular cleanup is essential - expired and old revoked tokens should be purged

PERFORMANCE NOTES:
1. The partial index on (token) WHERE active is critical for validation performance
2. Cleanup operations should run during low-traffic periods
3. Consider partitioning by created_at for very high-volume applications
4. Monitor index usage and adjust based on actual query patterns

MAINTENANCE:
1. Run VACUUM ANALYZE on this table periodically due to high churn
2. Monitor table size growth and cleanup effectiveness
3. Consider archiving old tokens for audit purposes before deletion
4. Review and update indexes based on application usage patterns
*/