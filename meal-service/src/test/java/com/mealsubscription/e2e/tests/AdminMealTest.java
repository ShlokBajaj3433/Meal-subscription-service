package com.mealsubscription.e2e.tests;

import com.mealsubscription.e2e.base.BaseTest;
import com.mealsubscription.e2e.pages.AdminMealPage;
import com.mealsubscription.e2e.pages.LoginPage;
import org.junit.jupiter.api.*;
import org.openqa.selenium.support.ui.ExpectedConditions;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * E2E tests for ADMIN meal management workflow.
 *
 * Scenarios:
 *  1. Admin can navigate to meal admin panel
 *  2. Admin can create a new meal
 *  3. Admin can delete a meal (soft delete)
 *
 * Prerequisite: admin user must be seeded (admin@mealsubscription.com / Admin@1234)
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AdminMealTest extends BaseTest {

    private AdminMealPage adminMealPage;

    @BeforeEach
    void loginAsAdmin() {
        navigateTo("/login");
        LoginPage loginPage = new LoginPage(driver, wait);
        loginPage.loginAs("admin@mealsubscription.com", "Admin@1234");
        // Wait for the POST /web/login 302 redirect to /dashboard to fully complete
        // before navigating onwards — avoids a race where driver.get(/admin/meals)
        // fires while the previous redirect is still in-flight.
        wait.until(ExpectedConditions.urlContains("/dashboard"));

        navigateTo("/admin/meals");
        adminMealPage = new AdminMealPage(driver, wait);
    }

    @Test
    @Order(1)
    @DisplayName("Admin can view the meal management page")
    void adminCanViewMealManagement() {
        assertThat(driver.getTitle())
            .as("Admin page title should confirm meal management context")
            .isNotBlank();
        assertThat(driver.getCurrentUrl())
            .contains("/admin/meals");
    }

    @Test
    @Order(2)
    @DisplayName("Admin can create a new meal and it appears in the list")
    void adminCreatesMeal() {
        int initialCount = adminMealPage.getMealRowCount();

        adminMealPage.clickCreateMeal();
        adminMealPage.fillMealForm(
            "Test Meal " + System.currentTimeMillis(),
            "A meal created by automated test",
            "VEGAN",
            420,
            1099L
        );
        adminMealPage.saveMeal();

        assertThat(adminMealPage.getMealRowCount())
            .as("Meal count should increase by 1 after creation")
            .isGreaterThan(initialCount);
    }

    @Test
    @Order(3)
    @DisplayName("Admin can soft-delete a meal")
    void adminDeletesMeal() {
        int initialCount = adminMealPage.getMealRowCount();
        adminMealPage.deleteFirstMeal();

        assertThat(adminMealPage.getMealRowCount())
            .as("Meal count should decrease by 1 after deletion")
            .isLessThan(initialCount);
    }
}
