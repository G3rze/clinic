ALTER TABLE clinic.users
ADD COLUMN is_access_revoked BOOLEAN NOT NULL DEFAULT FALSE;

CREATE INDEX idx_users_is_access_revoked ON clinic.users (is_access_revoked) WHERE is_access_revoked = TRUE;
