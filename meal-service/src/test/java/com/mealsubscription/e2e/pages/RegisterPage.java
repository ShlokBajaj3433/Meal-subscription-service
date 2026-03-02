package com.mealsubscription.e2e.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class RegisterPage {

    private final WebDriver driver;
    private final WebDriverWait wait;

    private final By nameInput     = By.id("name");
    private final By emailInput    = By.id("email");
    private final By passwordInput = By.id("password");
    private final By submitButton  = By.cssSelector("button[type='submit']");
    private final By successAlert  = By.cssSelector(".alert-success, [data-testid='register-success']");
    private final By errorAlert    = By.cssSelector(".alert-danger, [data-testid='register-error']");

    public RegisterPage(WebDriver driver, WebDriverWait wait) {
        this.driver = driver;
        this.wait   = wait;
    }

    public void fillForm(String name, String email, String password) {
        WebElement nameEl = wait.until(ExpectedConditions.visibilityOfElementLocated(nameInput));
        nameEl.clear();
        nameEl.sendKeys(name);

        driver.findElement(emailInput).sendKeys(email);
        driver.findElement(passwordInput).sendKeys(password);
    }

    public void submit() {
        wait.until(ExpectedConditions.elementToBeClickable(submitButton)).click();
    }

    public boolean isRegistrationSuccessful() {
        return wait.until(ExpectedConditions.urlContains("/login")) ||
               !driver.findElements(successAlert).isEmpty();
    }

    public String getErrorMessage() {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(errorAlert)).getText();
    }
}
