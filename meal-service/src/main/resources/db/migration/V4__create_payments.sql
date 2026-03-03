-- V4__create_payments.sql
-- ─────────────────────────────────────────────────────────────────────────────
-- Payments table: immutable audit trail of every Stripe charge
-- Note: store amounts in minor currency units (cents) to avoid float rounding
-- NEVER store raw card numbers, CVV, or full PANs — Stripe handles all PCI data
-- ─────────────────────────────────────────────────────────────────────────────
CREATE TABLE payments (
    id                          BIGSERIAL       PRIMARY KEY,
    subscription_id             BIGINT          NOT NULL
                                    REFERENCES subscriptions(id),
    stripe_payment_intent_id    VARCHAR(255)    NOT NULL,
    stripe_invoice_id           VARCHAR(255),
    stripe_customer_id          VARCHAR(255),
    amount_cents                BIGINT          NOT NULL CHECK (amount_cents >= 0),
    currency                    VARCHAR(3)      NOT NULL DEFAULT 'usd',
    status                      VARCHAR(30)     NOT NULL
                                    CHECK (status IN (
                                        'PENDING', 'SUCCEEDED', 'FAILED', 'REFUNDED')),
    failure_message             TEXT,
    paid_at                     TIMESTAMPTZ,
    created_at                  TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

-- Idempotency: prevent duplicate webhook processing
CREATE UNIQUE INDEX idx_payments_pi_id ON payments(stripe_payment_intent_id);
CREATE INDEX idx_payments_sub_id       ON payments(subscription_id);
CREATE INDEX idx_payments_status       ON payments(status);
CREATE INDEX idx_payments_paid_at      ON payments(paid_at);
