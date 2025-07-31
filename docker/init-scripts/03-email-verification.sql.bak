-- Email Verification Tables
-- This script adds email verification functionality to the blog API
-- All business logic is handled in the Java application, not in the database

-- 1. Create verification_tokens table
CREATE TABLE verification_tokens (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token VARCHAR(255) NOT NULL UNIQUE,
    token_type VARCHAR(20) NOT NULL CHECK (token_type IN ('EMAIL_VERIFICATION', 'PASSWORD_RESET', 'PHONE_VERIFICATION')),
    expires_at TIMESTAMP NOT NULL,
    used_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 2. Add email verification columns to users table
ALTER TABLE users 
    ADD COLUMN email_verified BOOLEAN DEFAULT FALSE,
    ADD COLUMN email_verified_at TIMESTAMP,
    ADD COLUMN account_locked BOOLEAN DEFAULT FALSE,
    ADD COLUMN locked_until TIMESTAMP,
    ADD COLUMN password_changed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    ADD COLUMN last_login TIMESTAMP,
    ADD COLUMN failed_login_attempts INTEGER DEFAULT 0;

-- 3. Create indexes for performance
CREATE INDEX idx_verification_tokens_user_id ON verification_tokens(user_id);
CREATE INDEX idx_verification_tokens_token ON verification_tokens(token);
CREATE INDEX idx_verification_tokens_expires_at ON verification_tokens(expires_at);
CREATE INDEX idx_verification_tokens_type ON verification_tokens(token_type);

CREATE INDEX idx_users_email_verified ON users(email_verified);
CREATE INDEX idx_users_account_locked ON users(account_locked);

-- 4. Comments for documentation
COMMENT ON TABLE verification_tokens IS 'Stores tokens for email verification, password reset, and other verification processes';
COMMENT ON COLUMN verification_tokens.token_type IS 'Type of verification: EMAIL_VERIFICATION, PASSWORD_RESET, PHONE_VERIFICATION';
COMMENT ON COLUMN verification_tokens.expires_at IS 'Token expiration timestamp - tokens are invalid after this time';
COMMENT ON COLUMN verification_tokens.used_at IS 'Timestamp when token was used - prevents reuse';

COMMENT ON COLUMN users.email_verified IS 'Whether user has verified their email address';
COMMENT ON COLUMN users.email_verified_at IS 'Timestamp when email was verified';
COMMENT ON COLUMN users.account_locked IS 'Whether account is locked due to security issues';
COMMENT ON COLUMN users.locked_until IS 'Timestamp until which account is locked';
COMMENT ON COLUMN users.failed_login_attempts IS 'Number of consecutive failed login attempts';