package com.mealsubscription.e2e.tests;

import com.mealsubscription.e2e.base.BaseTest;
import com.mealsubscription.e2e.pages.DashboardPage;
import com.mealsubscription.e2e.pages.LoginPage;
import org.junit.jupiter.api.*;
import org.openqa.selenium.support.ui.ExpectedConditions;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * E2E tests for the Dashboard page.
 *
 * Scenarios:
 *  1. Dashboard is accessible after login
 *  2. "Browse Meals" link navigates to the meal catalog
 *  3. Logout link signs the user out and redirects to /login
 *
 * Prerequisite: regular user must be seeded (user@mealsubscription.com / User@1234)
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class DashboardTest extends BaseTest {

    private DashboardPage dashboardPage;

    @BeforeEach
    void loginAndOpenDashboard() {
        navigateTo("/login");
        LoginPage loginPage = new LoginPage(driver, wait);
        loginPage.loginAs("user@mealsubscription.com", "User@1234");
        wait.until(ExpectedConditions.urlContains("/dashboard"));
        dashboardPage = new DashboardPage(driver, wait);
    }

    @Test
    @Order(1)
    @DisplayName("Dashboard page is accessible after login")
    void dashboardIsVisible() {
        assertThat(dashboardPage.isOnDashboard())
            .as("User should be on the dashboard after login")
            .isTrue();
    }

    @Test
    @Order(2)
    @DisplayName("Browse Meals link navigates to meal catalog")
    void browseMealsNavigatesToCatalog() {
        dashboardPage.clickBrowseMeals();
        assertThat(driver.getCurrentUrl())
            .as("Browse Meals link should navigate to /meals")
            .contains("/meals");
    }

    @Test
    @Order(3)
    @DisplayName("Logout link signs the user out and redirects to /login")
    void logoutRedirectsToLogin() {
        dashboardPage.clickLogout();
        assertThat(driver.getCurrentUrl())
            .as("After logout, user should be redirected to /login")
            .contains("/login");
    }
}
