package com.mealsubscription.e2e.tests;

import com.mealsubscription.e2e.base.BaseTest;
import com.mealsubscription.e2e.pages.LoginPage;
import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * E2E test for the basic login flow.
 *
 * Scenario:
 *  1. Valid credentials redirect to /dashboard
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class LoginTest extends BaseTest {

    private LoginPage loginPage;

    @BeforeEach
    void openLoginPage() {
        navigateTo("/login");
        loginPage = new LoginPage(driver, wait);
    }

    @Test
    @Order(1)
    @DisplayName("Valid credentials redirect to /dashboard")
    void loginWithValidCredentials() {
        loginPage.loginAs("user@mealsubscription.com", "User@1234");
        assertThat(loginPage.isLoginSuccessful())
            .as("Expected redirect to /dashboard after successful login")
            .isTrue();
    }
}
