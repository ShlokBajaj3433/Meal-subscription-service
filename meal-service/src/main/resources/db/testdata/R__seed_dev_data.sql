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

-- Seed: sample meals (prices stored in paise; ÷100 = ₹)
INSERT INTO meals (name, description, dietary_type, calories, price_cents, is_available) VALUES
('Chicken Biryani',          'Aromatic basmati rice slow-cooked with tender chicken, saffron and whole spices', 'STANDARD',    620, 24900, true),
('Dal Makhani',              'Slow-simmered black lentils in a rich tomato-butter-cream gravy',                  'VEGETARIAN',  480, 19900, true),
('Keto Chicken Tikka',       'Tandoor-roasted chicken tikka with cucumber raita and mint chutney',              'KETO',        510, 34900, true),
('Gluten-Free Idli Sambar',  'Soft steamed rice-lentil idlis served with piping-hot vegetable sambar',          'GLUTEN_FREE', 390, 12900, true),
('Masala Dosa',              'Crispy rice-lentil crepe filled with spiced potato masala and coconut chutney',    'VEGAN',       430, 14900, true),
('Paneer Butter Masala',     'Cottage-cheese cubes in a velvety tomato-cashew-cream sauce with naan',            'VEGETARIAN',  560, 22900, true),
('Rajma Chawal',             'Hearty red-kidney-bean curry served over steamed basmati rice',                    'VEGAN',       500, 17900, true),
('Tandoori Grilled Fish',    'Fresh surmai fillet marinated in yoghurt and spices, chargrilled in the tandoor', 'GLUTEN_FREE', 440, 29900, true);
