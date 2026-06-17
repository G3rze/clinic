ALTER TABLE clinic.google_sessions
ADD COLUMN jwt_access_token TEXT,
ADD COLUMN jwt_refresh_token TEXT,
ADD COLUMN jwt_expires_at TIMESTAMPTZ,
ADD COLUMN refresh_token_updated_at TIMESTAMPTZ;

CREATE INDEX idx_google_sessions_jwt_expires_at ON clinic.google_sessions (jwt_expires_at);
CREATE INDEX idx_google_sessions_user_id_jwt ON clinic.google_sessions (user_id) WHERE jwt_access_token IS NOT NULL;