-- V15: Create oauth_tokens table for storing Google OAuth tokens persistently
-- Tokens are stored when user authenticates via Google OAuth2
-- This enables Google Calendar API access (Meet links) for appointments

CREATE TABLE clinic.oauth_tokens (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    google_user_id VARCHAR(255) NOT NULL,
    access_token TEXT NOT NULL,
    refresh_token TEXT,
    expires_at BIGINT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ,
    created_by UUID DEFAULT '00000000-0000-0000-0000-000000000000',
    updated_by UUID,
    CONSTRAINT uk_oauth_tokens_google_user_id UNIQUE (google_user_id)
);

CREATE INDEX idx_oauth_tokens_google_user_id ON clinic.oauth_tokens(google_user_id);
CREATE INDEX idx_oauth_tokens_expires_at ON clinic.oauth_tokens(expires_at);
