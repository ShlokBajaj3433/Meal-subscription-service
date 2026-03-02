package com.mealsubscription.e2e.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * Page Object for the Login page (/login).
 *
 * Design rules:
 * - Locators are private final — tests never touch raw By selectors.
 * - Page methods return void or other Page Objects (navigation flow).
 * - Assertions stay in the test class — page objects never assert.
 * - Always use explicit waits (WebDriverWait) — never Thread.sleep().
 */
public class LoginPage {

    private final WebDriver driver;
    private final WebDriverWait wait;

    // ── Locators ─────────────────────────────────────────────────────────────
    // Prefer id > name > CSS selector > XPath (maintainability order)
    private final By emailInput    = By.id("email");
    private final By passwordInput = By.id("password");
    private final By submitButton  = By.cssSelector("button[type='submit']");
    private final By errorMessage  = By.cssSelector(".alert-danger, [data-testid='login-error']");
    private final By registerLink  = By.linkText("Register");

    public LoginPage(WebDriver driver, WebDriverWait wait) {
        this.driver = driver;
        this.wait   = wait;
    }

    // ── Actions ───────────────────────────────────────────────────────────────

    public void enterEmail(String email) {
        WebElement el = wait.until(ExpectedConditions.visibilityOfElementLocated(emailInput));
        el.clear();
        el.sendKeys(email);
    }

    public void enterPassword(String password) {
        WebElement el = wait.until(ExpectedConditions.visibilityOfElementLocated(passwordInput));
        el.clear();
        el.sendKeys(password);
    }

    public void clickSubmit() {
        wait.until(ExpectedConditions.elementToBeClickable(submitButton)).click();
    }

    /** Convenience: enter credentials and submit in one call */
    public void loginAs(String email, String password) {
        enterEmail(email);
        enterPassword(password);
        clickSubmit();
    }

    public void clickRegisterLink() {
        driver.findElement(registerLink).click();
    }

    // ── Assertions support ────────────────────────────────────────────────────

    public boolean isLoginSuccessful() {
        return wait.until(ExpectedConditions.urlContains("/dashboard"));
    }

    public String getErrorMessage() {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(errorMessage)).getText();
    }

    public boolean isOnLoginPage() {
        return driver.getCurrentUrl().contains("/login");
    }
}
