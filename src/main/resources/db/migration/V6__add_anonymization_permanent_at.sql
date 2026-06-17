ALTER TABLE clinic.users
ADD COLUMN anonymization_permanent_at TIMESTAMP WITH TIME ZONE;

CREATE INDEX idx_users_anonymization_permanent ON clinic.users (anonymization_permanent_at)
  WHERE anonymization_permanent_at IS NOT NULL;
