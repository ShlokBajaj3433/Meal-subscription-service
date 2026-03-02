-- V2__create_meals.sql
-- ─────────────────────────────────────────────────────────────────────────────
-- Meals table: the product catalog
-- ─────────────────────────────────────────────────────────────────────────────
CREATE TABLE meals (
    id              BIGSERIAL       PRIMARY KEY,
    name            VARCHAR(200)    NOT NULL,
    description     TEXT,
    dietary_type    VARCHAR(50)     NOT NULL
                        CHECK (dietary_type IN (
                            'STANDARD', 'VEGETARIAN', 'VEGAN', 'GLUTEN_FREE', 'KETO')),
    calories        INT             CHECK (calories > 0),
    price_cents     BIGINT          NOT NULL CHECK (price_cents > 0),
    image_url       VARCHAR(500),
    is_available    BOOLEAN         NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

-- Indexes frequently used in filter / availability queries
CREATE INDEX idx_meals_dietary_type ON meals(dietary_type);
CREATE INDEX idx_meals_is_available ON meals(is_available);

CREATE TRIGGER trg_meals_updated_at
    BEFORE UPDATE ON meals
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
