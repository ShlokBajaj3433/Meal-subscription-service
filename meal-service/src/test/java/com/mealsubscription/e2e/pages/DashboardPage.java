package com.mealsubscription.e2e.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * Page Object for the Dashboard page (/dashboard).
 *
 * Covers:
 *  - Verifying the user landed on the dashboard
 *  - Navigating to the meal catalog via the "Browse Meals" link
 *  - Logging out via the logout link
 */
public class DashboardPage {

    private final WebDriver driver;
    private final WebDriverWait wait;

    private final By browseMealsLink = By.linkText("Browse Meals");
    private final By logoutLink      = By.id("logout-link");

    public DashboardPage(WebDriver driver, WebDriverWait wait) {
        this.driver = driver;
        this.wait   = wait;
    }

    public void clickBrowseMeals() {
        wait.until(ExpectedConditions.elementToBeClickable(browseMealsLink)).click();
    }

    public void clickLogout() {
        wait.until(ExpectedConditions.elementToBeClickable(logoutLink)).click();
    }

    public boolean isOnDashboard() {
        return driver.getCurrentUrl().contains("/dashboard");
    }
}
