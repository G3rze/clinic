-- Seed roles if they don't exist
INSERT INTO clinic.roles (id, code, name, created_at, created_by)
SELECT
  gen_random_uuid(),
  code,
  name,
  NOW(),
  '00000000-0000-0000-0000-000000000000'
FROM (VALUES
  ('ADMIN', 'Administrator'),
  ('EMPLOYEE', 'Employee'),
  ('USER', 'Regular User')
) AS roles(code, name)
WHERE NOT EXISTS (SELECT 1 FROM clinic.roles WHERE code = 'ADMIN')
  AND NOT EXISTS (SELECT 1 FROM clinic.roles WHERE code = 'EMPLOYEE')
  AND NOT EXISTS (SELECT 1 FROM clinic.roles WHERE code = 'USER');
