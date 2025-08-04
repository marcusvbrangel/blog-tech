-- =====================================================================
-- V10__create_audit_logs_table.sql
-- 
-- Creates the audit_logs table for comprehensive system audit trail.
-- Includes optimized indexes for security monitoring and compliance.
-- =====================================================================

-- Create audit_logs table
CREATE TABLE audit_logs (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NULL,
    username VARCHAR(100) NULL,
    action VARCHAR(50) NOT NULL,
    resource_type VARCHAR(50) NULL,
    resource_id BIGINT NULL,
    details TEXT NULL,
    ip_address VARCHAR(45) NULL,
    user_agent VARCHAR(500) NULL,
    timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    result VARCHAR(20) NOT NULL,
    error_message VARCHAR(1000) NULL,
    
    -- Foreign key constraint (optional - user might be deleted)
    CONSTRAINT fk_audit_logs_user_id 
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL
);

-- =====================================================================
-- Performance Indexes for Audit Queries
-- =====================================================================

-- Primary index for user activity queries
-- Used by: findByUserIdOrderByTimestampDesc()
CREATE INDEX idx_audit_logs_user_timestamp 
ON audit_logs (user_id, timestamp DESC);

-- Index for action-based queries
-- Used by: findByActionOrderByTimestampDesc()
CREATE INDEX idx_audit_logs_action_timestamp 
ON audit_logs (action, timestamp DESC);

-- Primary timestamp index for time-range queries
-- Used by: findByTimestampBetweenOrderByTimestampDesc()
CREATE INDEX idx_audit_logs_timestamp 
ON audit_logs (timestamp DESC);

-- Security monitoring index for IP-based analysis
-- Used by: findByIpAddressOrderByTimestampDesc(), countFailedLoginAttemptsByIp()
CREATE INDEX idx_audit_logs_ip_timestamp 
ON audit_logs (ip_address, timestamp DESC);

-- Index for failed login monitoring
-- Used by: findFailedLoginAttempts()
CREATE INDEX idx_audit_logs_failed_logins 
ON audit_logs (user_id, action, result, timestamp DESC) 
WHERE action = 'LOGIN' AND result = 'FAILURE';

-- Index for security violations
-- Used by: findSecurityViolations()
CREATE INDEX idx_audit_logs_security_violations 
ON audit_logs (action, result, timestamp DESC) 
WHERE action IN ('SECURITY_VIOLATION', 'RATE_LIMIT_EXCEEDED') OR result = 'BLOCKED';

-- Index for admin actions monitoring
-- Used by: findAdminActions()
CREATE INDEX idx_audit_logs_admin_actions 
ON audit_logs (action, timestamp DESC) 
WHERE action IN ('ADMIN_ACCESS', 'PERMISSION_CHANGE', 'USER_DELETE');

-- Index for resource access logging
-- Used by: findByResourceTypeAndResourceIdOrderByTimestampDesc()
CREATE INDEX idx_audit_logs_resource_access 
ON audit_logs (resource_type, resource_id, timestamp DESC);

-- Composite index for user and action queries
-- Used by: findByUserIdAndActionOrderByTimestampDesc()
CREATE INDEX idx_audit_logs_user_action_timestamp 
ON audit_logs (user_id, action, timestamp DESC);

-- Index for cleanup operations
-- Used by: deleteOldAuditLogs()
CREATE INDEX idx_audit_logs_cleanup 
ON audit_logs (timestamp);

-- =====================================================================
-- Table Comments and Documentation
-- =====================================================================

-- Table comment
COMMENT ON TABLE audit_logs IS 
'Comprehensive audit trail for all system operations. Supports security monitoring, 
compliance reporting, and forensic analysis.';

-- Column comments
COMMENT ON COLUMN audit_logs.id IS 'Primary key';
COMMENT ON COLUMN audit_logs.user_id IS 'Foreign key to users table (nullable for anonymous actions)';
COMMENT ON COLUMN audit_logs.username IS 'Username at time of action (preserved even if user deleted)';
COMMENT ON COLUMN audit_logs.action IS 'Type of action performed (enum values)';
COMMENT ON COLUMN audit_logs.resource_type IS 'Type of resource affected (POST, USER, COMMENT, etc)';
COMMENT ON COLUMN audit_logs.resource_id IS 'ID of the specific resource affected';
COMMENT ON COLUMN audit_logs.details IS 'Additional context and details about the action';
COMMENT ON COLUMN audit_logs.ip_address IS 'IP address of the client performing the action';
COMMENT ON COLUMN audit_logs.user_agent IS 'User agent string from the client';
COMMENT ON COLUMN audit_logs.timestamp IS 'When the action occurred';
COMMENT ON COLUMN audit_logs.result IS 'Result of the action (SUCCESS, FAILURE, BLOCKED, ERROR)';
COMMENT ON COLUMN audit_logs.error_message IS 'Error details if action failed';

-- Index comments
COMMENT ON INDEX idx_audit_logs_user_timestamp IS 
'Primary index for user activity and history queries';

COMMENT ON INDEX idx_audit_logs_action_timestamp IS 
'Index for action-based reporting and analysis';

COMMENT ON INDEX idx_audit_logs_security_violations IS 
'Critical index for security monitoring and threat detection';

-- =====================================================================
-- Constraints and Validation
-- =====================================================================

-- Add check constraints for enum values
ALTER TABLE audit_logs ADD CONSTRAINT chk_audit_logs_action 
CHECK (action IN (
    'LOGIN', 'LOGOUT', 'LOGOUT_ALL_DEVICES', 'REGISTER', 
    'PASSWORD_RESET_REQUEST', 'PASSWORD_RESET_CONFIRM', 'EMAIL_VERIFICATION',
    'TOKEN_REFRESH', 'TOKEN_REVOKE', 'USER_CREATE', 'USER_UPDATE', 'USER_DELETE',
    'POST_CREATE', 'POST_UPDATE', 'POST_DELETE',
    'COMMENT_CREATE', 'COMMENT_UPDATE', 'COMMENT_DELETE',
    'CATEGORY_CREATE', 'CATEGORY_UPDATE', 'CATEGORY_DELETE',
    'ADMIN_ACCESS', 'PERMISSION_CHANGE', 'SECURITY_VIOLATION', 'RATE_LIMIT_EXCEEDED'
));

ALTER TABLE audit_logs ADD CONSTRAINT chk_audit_logs_result 
CHECK (result IN ('SUCCESS', 'FAILURE', 'BLOCKED', 'ERROR'));

-- Add validation for required fields
ALTER TABLE audit_logs ADD CONSTRAINT chk_audit_logs_action_not_empty 
CHECK (action IS NOT NULL AND LENGTH(TRIM(action)) > 0);

ALTER TABLE audit_logs ADD CONSTRAINT chk_audit_logs_result_not_empty 
CHECK (result IS NOT NULL AND LENGTH(TRIM(result)) > 0);

-- =====================================================================
-- Initial Configuration and Validation
-- =====================================================================

-- Verify table structure
DO $$
BEGIN
    -- Check if table was created successfully
    IF NOT EXISTS (SELECT 1 FROM information_schema.tables 
                   WHERE table_name = 'audit_logs' AND table_schema = 'public') THEN
        RAISE EXCEPTION 'Failed to create audit_logs table';
    END IF;
    
    -- Check if critical indexes exist
    IF NOT EXISTS (SELECT 1 FROM pg_indexes 
                   WHERE tablename = 'audit_logs' AND indexname = 'idx_audit_logs_user_timestamp') THEN
        RAISE EXCEPTION 'Failed to create critical user timestamp index';
    END IF;
    
    IF NOT EXISTS (SELECT 1 FROM pg_indexes 
                   WHERE tablename = 'audit_logs' AND indexname = 'idx_audit_logs_security_violations') THEN
        RAISE EXCEPTION 'Failed to create security violations index';
    END IF;
    
    RAISE NOTICE 'Audit logs table created successfully with % indexes', 
        (SELECT COUNT(*) FROM pg_indexes WHERE tablename = 'audit_logs');
END $$;

-- =====================================================================
-- Security and Maintenance Notes
-- =====================================================================

/*
SECURITY CONSIDERATIONS:
1. This table contains sensitive operational data - ensure proper access controls
2. IP addresses and user agents are stored for security analysis - comply with privacy regulations
3. Details field may contain sensitive information - review data retention policies
4. Foreign key is nullable to preserve audit trail even after user deletion

PERFORMANCE NOTES:
1. Partial indexes are used extensively to improve performance for security queries
2. This table will grow rapidly - implement regular cleanup and archival
3. Consider partitioning by timestamp for very high-volume applications
4. Monitor index usage and adjust based on actual query patterns

MAINTENANCE:
1. Regular VACUUM ANALYZE recommended due to high insert volume
2. Automated cleanup of old records (configured in AuditLogService)
3. Monitor table size growth and cleanup effectiveness
4. Archive old audit logs for long-term compliance if required

COMPLIANCE:
1. Audit logs support SOX, HIPAA, PCI-DSS, and GDPR compliance requirements
2. Retention period configurable (default 90 days)
3. Immutable audit trail - no updates or deletes except for cleanup
4. All critical operations are logged for forensic analysis
*/