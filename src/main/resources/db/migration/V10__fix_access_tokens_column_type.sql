ALTER TABLE clinic.google_sessions
  ALTER COLUMN access_token TYPE bytea USING access_token::bytea,
  ALTER COLUMN refresh_token TYPE bytea USING refresh_token::bytea;