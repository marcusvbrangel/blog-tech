-- =====================================================================
-- V11__create_two_factor_auth_table.sql
-- 
-- Creates the two_factor_auth table for TOTP-based 2FA implementation.
-- Includes optimized indexes for security monitoring and performance.
-- =====================================================================

-- Create two_factor_auth table
CREATE TABLE two_factor_auth (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    secret_key VARCHAR(100) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT FALSE,
    backup_codes TEXT NULL,
    backup_codes_used TEXT NULL DEFAULT '',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    enabled_at TIMESTAMP NULL,
    last_used TIMESTAMP NULL,
    last_backup_code_used TIMESTAMP NULL,
    
    -- Foreign key constraint
    CONSTRAINT fk_two_factor_auth_user_id 
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- =====================================================================
-- Performance Indexes
-- =====================================================================

-- Primary index for user lookup (unique constraint already creates this)
-- Used by: findByUserId(), isEnabledByUserId()
CREATE INDEX idx_2fa_user_id ON two_factor_auth (user_id);

-- Index for finding enabled 2FA users
-- Used by: findAllEnabled(), countEnabled()
CREATE INDEX idx_2fa_enabled ON two_factor_auth (enabled, enabled_at DESC) 
WHERE enabled = TRUE;

-- Index for recent usage tracking
-- Used by: findRecentlyUsed()
CREATE INDEX idx_2fa_last_used ON two_factor_auth (enabled, last_used DESC) 
WHERE enabled = TRUE AND last_used IS NOT NULL;

-- Index for backup code usage monitoring
-- Used by: findRecentBackupCodeUsage()
CREATE INDEX idx_2fa_backup_code_usage ON two_factor_auth (enabled, last_backup_code_used DESC) 
WHERE enabled = TRUE AND last_backup_code_used IS NOT NULL;

-- =====================================================================
-- Table Comments and Documentation
-- =====================================================================

-- Table comment
COMMENT ON TABLE two_factor_auth IS 
'TOTP-based Two-Factor Authentication configuration. Stores secret keys, 
backup codes, and usage tracking for enhanced security.';

-- Column comments
COMMENT ON COLUMN two_factor_auth.id IS 'Primary key';
COMMENT ON COLUMN two_factor_auth.user_id IS 'Foreign key to users table (unique)';
COMMENT ON COLUMN two_factor_auth.secret_key IS 'Base64-encoded TOTP secret key';
COMMENT ON COLUMN two_factor_auth.enabled IS 'Whether 2FA is enabled for the user';
COMMENT ON COLUMN two_factor_auth.backup_codes IS 'Comma-separated backup codes';
COMMENT ON COLUMN two_factor_auth.backup_codes_used IS 'Comma-separated used backup codes';
COMMENT ON COLUMN two_factor_auth.created_at IS 'When 2FA was first set up';
COMMENT ON COLUMN two_factor_auth.enabled_at IS 'When 2FA was enabled';
COMMENT ON COLUMN two_factor_auth.last_used IS 'Last successful TOTP verification';
COMMENT ON COLUMN two_factor_auth.last_backup_code_used IS 'Last successful backup code usage';

-- Index comments
COMMENT ON INDEX idx_2fa_enabled IS 
'Index for finding users with 2FA enabled';

COMMENT ON INDEX idx_2fa_last_used IS 
'Index for tracking recent 2FA usage';

-- =====================================================================
-- Constraints and Validation
-- =====================================================================

-- Ensure secret key is not empty
ALTER TABLE two_factor_auth ADD CONSTRAINT chk_2fa_secret_key_not_empty 
CHECK (secret_key IS NOT NULL AND LENGTH(TRIM(secret_key)) > 0);

-- Ensure enabled_at is set when enabled is true
ALTER TABLE two_factor_auth ADD CONSTRAINT chk_2fa_enabled_at_consistency
CHECK ((enabled = TRUE AND enabled_at IS NOT NULL) OR (enabled = FALSE));

-- =====================================================================
-- Initial Configuration and Validation
-- =====================================================================

-- Verify table structure
DO $$
BEGIN
    -- Check if table was created successfully
    IF NOT EXISTS (SELECT 1 FROM information_schema.tables 
                   WHERE table_name = 'two_factor_auth' AND table_schema = 'public') THEN
        RAISE EXCEPTION 'Failed to create two_factor_auth table';
    END IF;
    
    -- Check if unique constraint exists on user_id
    IF NOT EXISTS (SELECT 1 FROM information_schema.table_constraints 
                   WHERE table_name = 'two_factor_auth' 
                   AND constraint_type = 'UNIQUE' 
                   AND constraint_name LIKE '%user_id%') THEN
        RAISE EXCEPTION 'Failed to create unique constraint on user_id';
    END IF;
    
    -- Check if critical indexes exist
    IF NOT EXISTS (SELECT 1 FROM pg_indexes 
                   WHERE tablename = 'two_factor_auth' AND indexname = 'idx_2fa_enabled') THEN
        RAISE EXCEPTION 'Failed to create enabled users index';
    END IF;
    
    RAISE NOTICE '2FA table created successfully with % indexes', 
        (SELECT COUNT(*) FROM pg_indexes WHERE tablename = 'two_factor_auth');
END $$;

-- =====================================================================
-- Security and Maintenance Notes
-- =====================================================================

/*
SECURITY CONSIDERATIONS:
1. The secret_key column contains sensitive cryptographic material
2. Backup codes should only be displayed once during setup
3. Regular monitoring of backup code usage is recommended
4. Consider encrypting secret keys at application level if required

PERFORMANCE NOTES:
1. User_id has unique constraint for efficient lookups
2. Partial indexes optimize queries for enabled users only
3. Monitor backup_codes column size growth over time
4. Consider archiving old 2FA configurations if users disable

COMPLIANCE:
1. Supports multi-factor authentication requirements
2. Audit trail through last_used and last_backup_code_used timestamps
3. Backup codes provide accessibility compliance
4. Secret key rotation supported through application logic

MAINTENANCE:
1. Monitor users with many used backup codes
2. Encourage backup code regeneration when low
3. Track 2FA adoption rates through enabled flag
4. Regular cleanup of disabled 2FA configurations if needed
*/