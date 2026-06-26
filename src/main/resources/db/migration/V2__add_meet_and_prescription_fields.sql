-- V2: Campos para video Meet y trazabilidad de recetas

-- Enlace de Google Meet generado para la cita (JC4)
ALTER TABLE clinic.appointments
    ADD COLUMN IF NOT EXISTS meet_link VARCHAR(512);

-- Timestamp en que el paciente confirmó su unión (JC5)
ALTER TABLE clinic.appointments
    ADD COLUMN IF NOT EXISTS patient_joined_at TIMESTAMPTZ;

-- Índice para buscar prescripciones dispensadas (JC2 — auditoría)
CREATE INDEX IF NOT EXISTS idx_prescriptions_usage
    ON clinic.prescriptions (appointment_id, usage_count, max_usages);
