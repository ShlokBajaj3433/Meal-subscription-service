package com.mealsubscription.e2e.tests;

import com.mealsubscription.e2e.base.BaseTest;
import com.mealsubscription.e2e.pages.AdminMealPage;
import com.mealsubscription.e2e.pages.LoginPage;
import org.junit.jupiter.api.*;
import org.openqa.selenium.support.ui.ExpectedConditions;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * E2E tests for the admin meal management workflow.
 *
 * Scenarios:
 *  1. Admin can create a new meal via the form
 *  2. Admin can delete a meal from the list
 *
 * Prerequisite: admin user must be seeded (admin@mealsubscription.com / Admin@1234)
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AdminMealsTest extends BaseTest {

    private AdminMealPage adminMealPage;

    @BeforeEach
    void loginAsAdminAndOpenMeals() {
        navigateTo("/login");
        LoginPage loginPage = new LoginPage(driver, wait);
        loginPage.loginAs("admin@mealsubscription.com", "Admin@1234");
        wait.until(ExpectedConditions.urlContains("/dashboard"));

        navigateTo("/admin/meals");
        adminMealPage = new AdminMealPage(driver, wait);
    }

    @Test
    @Order(1)
    @DisplayName("Admin can create a new meal via the form")
    void adminCreatesMeal() {
        int initialCount = adminMealPage.getMealRowCount();

        adminMealPage.clickCreateMeal();
        adminMealPage.fillMealForm(
            "Test Automator Meal",
            "A delicious meal created by Selenium WebDriver.",
            "STANDARD",
            600,
            1500L
        );
        adminMealPage.saveMeal();

        assertThat(adminMealPage.getMealRowCount())
            .as("Meal count should increase by 1 after creation")
            .isGreaterThan(initialCount);
    }

    @Test
    @Order(2)
    @DisplayName("Admin can delete a meal from the list")
    void adminDeletesMeal() {
        int initialCount = adminMealPage.getMealRowCount();
        if (initialCount > 0) {
            adminMealPage.deleteFirstMeal();
            assertThat(adminMealPage.getMealRowCount())
                .as("Meal count should decrease by 1 after deletion")
                .isLessThan(initialCount);
        }
    }
}
