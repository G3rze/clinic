-- Add consult_duration_minutes column to employeesxspecialties table
-- This stores the appointment duration in minutes for each employee-specialty combination

ALTER TABLE clinic.employeesxspecialties
ADD COLUMN consult_duration_minutes INTEGER NOT NULL DEFAULT 60;
