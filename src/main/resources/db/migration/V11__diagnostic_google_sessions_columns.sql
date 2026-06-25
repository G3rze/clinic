SELECT column_name, data_type, character_maximum_length
FROM information_schema.columns
WHERE table_name = 'google_sessions'
  AND column_name IN ('access_token', 'refresh_token', 'jwt_access_token', 'jwt_refresh_token');
