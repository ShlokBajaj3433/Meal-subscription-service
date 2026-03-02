-- Seed: admin user (password: Admin@1234 — BCrypt hash cost 12, verified)
INSERT INTO users (name, email, password_hash, role)
VALUES ('Admin User', 'admin@mealsubscription.com',
        '$2a$12$yKF0nXjxVAPpxq/Lb1vBD.Fjw71UVI3auXbj4TufOnTJxhwPbHMKO', 'ADMIN')
ON CONFLICT (email) DO UPDATE SET password_hash = EXCLUDED.password_hash;

-- Seed: regular test user (password: User@1234 — BCrypt hash cost 12, verified)
INSERT INTO users (name, email, password_hash, role)
VALUES ('Test User', 'user@mealsubscription.com',
        '$2a$12$EFJ30geZyp3jVgf/mpVNQulmcmKQAmMDucD/utcKapUEM2UJuaD8i', 'USER')
ON CONFLICT (email) DO UPDATE SET password_hash = EXCLUDED.password_hash;

-- Seed: sample meals
INSERT INTO meals (name, description, dietary_type, calories, price_cents, is_available) VALUES
('Grilled Chicken Bowl',     'Lean chicken breast with brown rice and roasted veg', 'STANDARD',    520, 1299, true),
('Avocado Buddha Bowl',      'Quinoa, chickpeas, avocado, tahini dressing',          'VEGAN',       480, 1399, true),
('Keto Steak Plate',         'Grass-fed sirloin with cauliflower mash and spinach',  'KETO',        650, 1699, true),
('Gluten-Free Pasta',        'Brown rice pasta with marinara and sautéed mushrooms', 'GLUTEN_FREE', 540, 1199, true),
('Veggie Stir Fry',          'Seasonal vegetables in teriyaki sauce with jasmine rice', 'VEGETARIAN', 430, 1099, true),
('Salmon Teriyaki',          'Atlantic salmon fillet with edamame and jasmine rice',  'STANDARD',    590, 1799, true),
('Vegan Lentil Soup',        'Red lentil soup with cumin and crusty sourdough bread', 'VEGAN',       380, 999,  true),
('Turkey & Sweet Potato',    'Ground turkey with roasted sweet potato and kale',      'GLUTEN_FREE', 510, 1299, true);
