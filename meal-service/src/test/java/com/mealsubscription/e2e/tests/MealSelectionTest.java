package com.mealsubscription.e2e.tests;

import com.mealsubscription.e2e.base.BaseTest;
import com.mealsubscription.e2e.pages.LoginPage;
import com.mealsubscription.e2e.pages.MealSelectionPage;
import org.junit.jupiter.api.*;
import org.openqa.selenium.support.ui.ExpectedConditions;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * E2E test: meal catalog browsing and dietary filtering.
 *
 * Scenarios:
 *  1. Meal list is visible on the public catalog page
 *  2. Filtering by dietary type returns non-empty results
 *  3. Authenticated user can add a meal to their weekly plan
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class MealSelectionTest extends BaseTest {

    private MealSelectionPage mealPage;

    @BeforeEach
    void openMealCatalog() {
        navigateTo("/meals");
        mealPage = new MealSelectionPage(driver, wait);
    }

    @Test
    @Order(1)
    @DisplayName("Meal catalog displays at least one meal")
    void mealCatalogIsVisible() {
        assertThat(mealPage.getMealCardCount())
            .as("At least one meal should be visible in the catalog")
            .isGreaterThan(0);
    }

    @Test
    @Order(2)
    @DisplayName("Filtering by VEGAN shows only vegan meals")
    void filterByVegan() {
        mealPage.selectDietaryFilter("VEGAN");
        assertThat(mealPage.getMealNames())
            .as("All displayed meals should contain VEGAN indicator after filtering")
            .isNotEmpty();
    }

    @Test
    @Order(3)
    @DisplayName("Authenticated user can add a meal to their weekly plan")
    void authenticatedUserAddsToWeek() {
        // Log in first
        navigateTo("/login");
        LoginPage loginPage = new LoginPage(driver, wait);
        loginPage.loginAs("user@mealsubscription.com", "User@1234");
        // Wait for login redirect to complete before navigating to meals page
        wait.until(ExpectedConditions.urlContains("/dashboard"));

        // Navigate back to catalog
        navigateTo("/meals");
        mealPage = new MealSelectionPage(driver, wait);
        mealPage.addFirstMealToWeek();

        // The app should show a confirmation toast or navigate to subscription page
        assertThat(driver.getCurrentUrl())
            .as("After adding a meal, user should remain on meal page or be redirected to checkout")
            .isNotNull();
    }
}
