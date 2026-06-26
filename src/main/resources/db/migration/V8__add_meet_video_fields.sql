-- V8: Campos para videoconsulta (JC4 / JC5)

ALTER TABLE clinic.appointments
    ADD COLUMN IF NOT EXISTS meet_link VARCHAR(512);

ALTER TABLE clinic.appointments
    ADD COLUMN IF NOT EXISTS patient_joined_at TIMESTAMPTZ;

-- Índice de auditoría de dispensaciones de recetas (JC2)
CREATE INDEX IF NOT EXISTS idx_prescriptions_usage
    ON clinic.prescriptions (appointment_id, usage_count, max_usages);
