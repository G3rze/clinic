-- Migration: Create google_sessions table
-- Description: Stores Google OAuth2 sessions for authenticated users
-- Run this script manually or through your migration tool

CREATE TABLE clinic.google_sessions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID,
    google_user_id VARCHAR(255) UNIQUE NOT NULL,
    access_token BYTEA NOT NULL,
    refresh_token BYTEA,
    expires_at TIMESTAMPTZ NOT NULL,
    google_email VARCHAR(255),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ,
    created_by UUID NOT NULL DEFAULT '00000000-0000-0000-0000-000000000000',
    updated_by UUID
);

-- Index for efficient expired session cleanup
CREATE INDEX idx_google_sessions_expires_at ON clinic.google_sessions (expires_at);

-- Index for user lookup
CREATE INDEX idx_google_sessions_user_id ON clinic.google_sessions (user_id);

-- Index for google user lookup
CREATE INDEX idx_google_sessions_google_user_id ON clinic.google_sessions (google_user_id);

COMMENT ON TABLE clinic.google_sessions IS 'Stores Google OAuth2 sessions - tokens are encrypted';
COMMENT ON COLUMN clinic.google_sessions.access_token IS 'Encrypted OAuth2 access token';
COMMENT ON COLUMN clinic.google_sessions.refresh_token IS 'Encrypted OAuth2 refresh token';
