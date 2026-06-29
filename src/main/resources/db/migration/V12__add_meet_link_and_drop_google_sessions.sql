-- V12: Add meetLink to appointment and drop google_sessions table
-- meetLink is already present in entity (V2/V8), just ensure column exists
ALTER TABLE clinic.appointments ADD COLUMN IF NOT EXISTS meet_link VARCHAR(500);

-- Drop google_sessions table (OAuth tokens now stored in HTTP session, JWT in cookies)
DROP TABLE IF EXISTS clinic.google_sessions;
