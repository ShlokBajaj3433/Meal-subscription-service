package com.mealsubscription.e2e.tests;

import com.mealsubscription.e2e.base.BaseTest;
import com.mealsubscription.e2e.pages.LoginPage;
import com.mealsubscription.e2e.pages.RegisterPage;
import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * End-to-end tests for the authentication flow.
 *
 * Test accounts used here must exist in the seed data or be created through
 * the register flow before login tests.
 *
 * Scenarios:
 *  1. Register a new user → redirected to login (or success message)
 *  2. Login with valid credentials → redirected to /dashboard
 *  3. Login with wrong password → error message shown
 *  4. Login with empty email → validation error shown
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AuthFlowTest extends BaseTest {

    private LoginPage loginPage;

    // These correspond to seed data in R__seed_dev_data.sql
    private static final String VALID_EMAIL    = "user@mealsubscription.com";
    private static final String VALID_PASSWORD = "User@1234";
    private static final String ADMIN_EMAIL    = "admin@mealsubscription.com";
    private static final String ADMIN_PASSWORD = "Admin@1234";

    @BeforeEach
    void openLoginPage() {
        navigateTo("/login");
        loginPage = new LoginPage(driver, wait);
    }

    @Test
    @Order(1)
    @DisplayName("New user registration completes successfully")
    void registerNewUser() {
        navigateTo("/register");
        RegisterPage registerPage = new RegisterPage(driver, wait);

        // Use timestamp to ensure unique email during repeated test runs
        String uniqueEmail = "newuser+" + System.currentTimeMillis() + "@test.com";
        registerPage.fillForm("New User", uniqueEmail, "NewPass@1!");
        registerPage.submit();

        assertThat(registerPage.isRegistrationSuccessful())
            .as("Expected redirect to /login or a success message after registration")
            .isTrue();
    }

    @Test
    @Order(2)
    @DisplayName("Valid credentials redirect to /dashboard")
    void loginWithValidCredentials() {
        loginPage.loginAs(VALID_EMAIL, VALID_PASSWORD);
        assertThat(loginPage.isLoginSuccessful())
            .as("Expected URL to contain /dashboard after successful login")
            .isTrue();
    }

    @Test
    @Order(3)
    @DisplayName("Wrong password shows error message")
    void loginWithWrongPassword() {
        loginPage.loginAs(VALID_EMAIL, "WrongPassword99!");
        assertThat(loginPage.getErrorMessage())
            .as("Expected an authentication error message")
            .isNotBlank();
    }

    @Test
    @Order(4)
    @DisplayName("Empty email field shows validation error")
    void loginWithEmptyEmail() {
        loginPage.loginAs("", VALID_PASSWORD);
        // Either HTML5 validation prevents submission or server returns error
        assertThat(loginPage.isOnLoginPage())
            .as("Should remain on login page when email is empty")
            .isTrue();
    }

    @Test
    @Order(5)
    @DisplayName("Admin user can log in successfully")
    void adminLogin() {
        loginPage.loginAs(ADMIN_EMAIL, ADMIN_PASSWORD);
        // Use the page-object wait (urlContains "/dashboard") so the assertion
        // is not evaluated before the 302 redirect from POST /web/login completes.
        assertThat(loginPage.isLoginSuccessful())
            .as("Admin should be redirected to /dashboard after login")
            .isTrue();
    }
}
