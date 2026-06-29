-- V14: Make updated_by nullable in receipts table
-- StripeWebhookService updates receipts without setting updatedBy (webhook context)
ALTER TABLE clinic.receipts ALTER COLUMN updated_by DROP NOT NULL;
