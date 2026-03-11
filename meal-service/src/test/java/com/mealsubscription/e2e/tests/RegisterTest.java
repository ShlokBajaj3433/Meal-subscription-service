package com.mealsubscription.e2e.tests;

import com.mealsubscription.e2e.base.BaseTest;
import com.mealsubscription.e2e.pages.RegisterPage;
import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * E2E test for the registration flow.
 *
 * Scenario:
 *  1. A new user can register and is redirected to /login or shown a success message
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class RegisterTest extends BaseTest {

    @Test
    @Order(1)
    @DisplayName("New user can register successfully")
    void registerNewUser() {
        navigateTo("/register");
        RegisterPage registerPage = new RegisterPage(driver, wait);

        // Timestamp suffix ensures a unique email on every test run
        String uniqueEmail = "testuser+" + System.currentTimeMillis() + "@example.com";
        registerPage.fillForm("Test User", uniqueEmail, "testpassword");
        registerPage.submit();

        assertThat(registerPage.isRegistrationSuccessful())
            .as("Expected redirect to /login or a success message after registration")
            .isTrue();
    }
}
