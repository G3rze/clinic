ALTER TABLE clinic.pending_consents RENAME TO pending_user_config;

ALTER TABLE clinic.pending_user_config ADD COLUMN IF NOT EXISTS consent_given BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE clinic.pending_user_config ADD COLUMN IF NOT EXISTS profile_complete BOOLEAN NOT NULL DEFAULT FALSE;
