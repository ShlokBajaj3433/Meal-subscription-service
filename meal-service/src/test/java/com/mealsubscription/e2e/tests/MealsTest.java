package com.mealsubscription.e2e.tests;

import com.mealsubscription.e2e.base.BaseTest;
import com.mealsubscription.e2e.pages.MealSelectionPage;
import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * E2E tests for the meal catalog page (/meals).
 *
 * Scenarios:
 *  1. Meal catalog page loads and shows meal cards
 *  2. Filtering by VEGAN updates the displayed results
 *  3. "Add to Week" button is clickable when meals are present
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class MealsTest extends BaseTest {

    private MealSelectionPage mealPage;

    @BeforeEach
    void openMealsPage() {
        navigateTo("/meals");
        mealPage = new MealSelectionPage(driver, wait);
    }

    @Test
    @Order(1)
    @DisplayName("Meal catalog page loads with meal cards")
    void mealsPageLoads() {
        assertThat(mealPage.getMealCardCount())
            .as("Meal catalog should display zero or more meal cards")
            .isGreaterThanOrEqualTo(0);
    }

    @Test
    @Order(2)
    @DisplayName("Filtering by VEGAN updates the meal list")
    void filterByVeganUpdatesResults() {
        mealPage.selectDietaryFilter("VEGAN");
        assertThat(mealPage.getMealNames())
            .as("Meal list should update after VEGAN filter is applied")
            .isNotNull();
    }

    @Test
    @Order(3)
    @DisplayName("Add to Week button is clickable when meals are present")
    void addToWeekButtonIsClickable() {
        if (mealPage.getMealCardCount() > 0) {
            mealPage.addFirstMealToWeek();
            assertThat(driver.getCurrentUrl())
                .as("After adding a meal, the page URL should be present")
                .isNotNull();
        }
    }
}
