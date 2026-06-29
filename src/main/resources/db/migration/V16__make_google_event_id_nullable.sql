-- V16: Make google_event_id nullable
-- This allows appointments to be created even if Google Calendar event ID is not available
-- Meet link can still be created without the event ID being stored

ALTER TABLE clinic.appointments ALTER COLUMN google_event_id DROP NOT NULL;