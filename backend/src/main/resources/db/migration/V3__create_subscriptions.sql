-- V3__create_subscriptions.sql
-- ─────────────────────────────────────────────────────────────────────────────
-- Subscriptions + junction table for meal-day selections
-- ─────────────────────────────────────────────────────────────────────────────

CREATE TABLE subscriptions (
    id                      BIGSERIAL       PRIMARY KEY,
    user_id                 BIGINT          NOT NULL
                                REFERENCES users(id) ON DELETE CASCADE,
    plan_type               VARCHAR(20)     NOT NULL
                                CHECK (plan_type IN ('WEEKLY', 'MONTHLY')),
    status                  VARCHAR(30)     NOT NULL DEFAULT 'ACTIVE'
                                CHECK (status IN ('ACTIVE', 'PAUSED', 'CANCELLED', 'EXPIRED')),
    start_date              DATE            NOT NULL,
    end_date                DATE,
    stripe_subscription_id  VARCHAR(255),
    created_at              TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_subs_user_id ON subscriptions(user_id);
CREATE INDEX idx_subs_status  ON subscriptions(status);
CREATE UNIQUE INDEX idx_subs_stripe_id
    ON subscriptions(stripe_subscription_id)
    WHERE stripe_subscription_id IS NOT NULL;

CREATE TRIGGER trg_subscriptions_updated_at
    BEFORE UPDATE ON subscriptions
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- ─────────────────────────────────────────────────────────────────────────────
-- subscription_meals: many-to-many bridge — which meal on which delivery day
-- ─────────────────────────────────────────────────────────────────────────────
CREATE TABLE subscription_meals (
    id              BIGSERIAL   PRIMARY KEY,
    subscription_id BIGINT      NOT NULL
                        REFERENCES subscriptions(id) ON DELETE CASCADE,
    meal_id         BIGINT      NOT NULL
                        REFERENCES meals(id),
    delivery_date   DATE        NOT NULL,
    quantity        INT         NOT NULL DEFAULT 1 CHECK (quantity > 0),
    UNIQUE (subscription_id, meal_id, delivery_date)
);

CREATE INDEX idx_sub_meals_sub_id   ON subscription_meals(subscription_id);
CREATE INDEX idx_sub_meals_meal_id  ON subscription_meals(meal_id);
CREATE INDEX idx_sub_meals_delivery ON subscription_meals(delivery_date);
