package com.mealsubscription.config;

import com.mealsubscription.entity.DietaryType;
import com.mealsubscription.entity.Meal;
import com.mealsubscription.entity.Role;
import com.mealsubscription.entity.User;
import com.mealsubscription.repository.MealRepository;
import com.mealsubscription.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Seeds essential test data on every application startup.
 *
 * Safe on both H2 (default/test) and PostgreSQL (dev) — checks before inserting.
 *
 * Seed accounts (matching E2E test constants):
 *   admin@mealsubscription.com / Admin@1234  (ADMIN role)
 *   user@mealsubscription.com  / User@1234   (USER role)
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements ApplicationRunner {

    private final UserRepository userRepository;
    private final MealRepository mealRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        seedUsers();
        seedMeals();
    }

    // ── Users ─────────────────────────────────────────────────────────────────

    private void seedUsers() {
        if (!userRepository.existsByEmail("admin@mealsubscription.com")) {
            userRepository.save(User.builder()
                .name("Admin User")
                .email("admin@mealsubscription.com")
                .passwordHash(passwordEncoder.encode("Admin@1234"))
                .role(Role.ADMIN)
                .active(true)
                .build());
            log.info("[DataSeeder] Created admin user: admin@mealsubscription.com");
        }

        if (!userRepository.existsByEmail("user@mealsubscription.com")) {
            userRepository.save(User.builder()
                .name("Test User")
                .email("user@mealsubscription.com")
                .passwordHash(passwordEncoder.encode("User@1234"))
                .role(Role.USER)
                .active(true)
                .build());
            log.info("[DataSeeder] Created test user: user@mealsubscription.com");
        }
    }

    // ── Meals ─────────────────────────────────────────────────────────────────

    private void seedMeals() {
        if (mealRepository.count() > 0) {
            log.debug("[DataSeeder] Meals already present — skipping meal seed");
            return;
        }

        // Prices stored in paise (100 paise = ₹1)
        mealRepository.saveAll(List.of(
            meal("Chicken Biryani",
                 "Aromatic basmati rice slow-cooked with tender chicken, saffron and whole spices",
                 DietaryType.STANDARD, 620, 24900L),
            meal("Dal Makhani",
                 "Slow-simmered black lentils in a rich tomato-butter-cream gravy",
                 DietaryType.VEGETARIAN, 480, 19900L),
            meal("Keto Chicken Tikka",
                 "Tandoor-roasted chicken tikka with cucumber raita and mint chutney",
                 DietaryType.KETO, 510, 34900L),
            meal("Gluten-Free Idli Sambar",
                 "Soft steamed rice-lentil idlis served with piping-hot vegetable sambar",
                 DietaryType.GLUTEN_FREE, 390, 12900L),
            meal("Masala Dosa",
                 "Crispy rice-lentil crepe filled with spiced potato masala and coconut chutney",
                 DietaryType.VEGAN, 430, 14900L),
            meal("Paneer Butter Masala",
                 "Cottage-cheese cubes in a velvety tomato-cashew-cream sauce with naan",
                 DietaryType.VEGETARIAN, 560, 22900L),
            meal("Rajma Chawal",
                 "Hearty red-kidney-bean curry served over steamed basmati rice",
                 DietaryType.VEGAN, 500, 17900L),
            meal("Tandoori Grilled Fish",
                 "Fresh surmai fillet marinated in yoghurt and spices, chargrilled in the tandoor",
                 DietaryType.GLUTEN_FREE, 440, 29900L)
        ));

        log.info("[DataSeeder] Seeded {} meals", mealRepository.count());
    }

    private Meal meal(String name, String desc, DietaryType type, int cal, long cents) {
        return Meal.builder()
            .name(name)
            .description(desc)
            .dietaryType(type)
            .calories(cal)
            .priceCents(cents)
            .available(true)
            .build();
    }
}
