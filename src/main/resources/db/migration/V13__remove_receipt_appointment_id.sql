-- V13: Remove appointment_id column from receipts table
-- The FK relationship is on Appointment side only (receipt_id in appointments table)
-- Receipt entity does not have appointment_id field - removing the unused column
ALTER TABLE clinic.receipts DROP COLUMN IF EXISTS appointment_id;
