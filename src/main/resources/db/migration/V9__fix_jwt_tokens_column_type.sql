ALTER TABLE clinic.google_sessions
  ALTER COLUMN jwt_access_token TYPE bytea USING jwt_access_token::bytea,
  ALTER COLUMN jwt_refresh_token TYPE bytea USING jwt_refresh_token::bytea;
