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

        mealRepository.saveAll(List.of(
            meal("Grilled Chicken Bowl",
                 "Lean chicken breast with brown rice and roasted veg",
                 DietaryType.STANDARD, 520, 1299L),
            meal("Avocado Buddha Bowl",
                 "Quinoa, chickpeas, avocado, tahini dressing",
                 DietaryType.VEGAN, 480, 1399L),
            meal("Keto Steak Plate",
                 "Grass-fed sirloin with cauliflower mash and spinach",
                 DietaryType.KETO, 650, 1699L),
            meal("Gluten-Free Pasta",
                 "Brown rice pasta with marinara and sauteed mushrooms",
                 DietaryType.GLUTEN_FREE, 540, 1199L),
            meal("Veggie Stir Fry",
                 "Seasonal vegetables in teriyaki sauce with jasmine rice",
                 DietaryType.VEGETARIAN, 430, 1099L),
            meal("Salmon Teriyaki",
                 "Atlantic salmon fillet with edamame and jasmine rice",
                 DietaryType.STANDARD, 590, 1799L),
            meal("Vegan Lentil Soup",
                 "Red lentil soup with cumin and crusty sourdough bread",
                 DietaryType.VEGAN, 380, 999L),
            meal("Turkey and Sweet Potato",
                 "Ground turkey with roasted sweet potato and kale",
                 DietaryType.GLUTEN_FREE, 510, 1299L)
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
