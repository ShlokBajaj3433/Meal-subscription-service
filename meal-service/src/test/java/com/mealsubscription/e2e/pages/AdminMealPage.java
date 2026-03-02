package com.mealsubscription.e2e.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
/**
 * Page Object for the Admin Meal Management page (/admin/meals).
 * Assumes standard HTML form structure matching planned Thymeleaf templates.
 */
public class AdminMealPage {

    private final WebDriver driver;
    private final WebDriverWait wait;

    // List page locators
    private final By createMealButton = By.id("btn-create-meal");
    private final By mealTableRows    = By.cssSelector("table#mealsTable tbody tr");

    // Form locators (for create / edit)
    private final By nameInput        = By.id("mealName");
    private final By descInput        = By.id("mealDescription");
    private final By dietarySelect    = By.id("dietaryType");
    private final By caloriesInput    = By.id("calories");
    private final By priceInput       = By.id("priceCents");
    private final By saveButton       = By.id("btn-save-meal");
    private final By successMessage   = By.cssSelector(".alert-success, [data-testid='meal-success']");
    private final By deleteFirstBtn   = By.cssSelector("table#mealsTable tbody tr:first-child .btn-delete");

    public AdminMealPage(WebDriver driver, WebDriverWait wait) {
        this.driver = driver;
        this.wait   = wait;
    }

    public void clickCreateMeal() {
        wait.until(ExpectedConditions.elementToBeClickable(createMealButton)).click();
    }

    public void fillMealForm(String name, String description, String dietary,
                              int calories, long priceCents) {
        WebElement nameEl = wait.until(ExpectedConditions.visibilityOfElementLocated(nameInput));
        nameEl.clear();
        nameEl.sendKeys(name);

        WebElement descEl = driver.findElement(descInput);
        descEl.clear();
        descEl.sendKeys(description);

        new Select(driver.findElement(dietarySelect)).selectByValue(dietary);

        WebElement calEl = driver.findElement(caloriesInput);
        calEl.clear();
        calEl.sendKeys(String.valueOf(calories));

        WebElement priceEl = driver.findElement(priceInput);
        priceEl.clear();
        priceEl.sendKeys(String.valueOf(priceCents));
    }

    public void saveMeal() {
        WebElement btn = wait.until(ExpectedConditions.elementToBeClickable(saveButton));
        btn.click();
        // POST /web/admin/meals/create redirects back to /admin/meals (PRG pattern).
        // Wait for the submit button to become stale (proves old DOM unloaded) then
        // wait for the meals table to be present in the fresh page.
        wait.until(ExpectedConditions.stalenessOf(btn));
        wait.until(ExpectedConditions.presenceOfElementLocated(mealTableRows));
    }

    public boolean isSuccessMessageVisible() {
        return !driver.findElements(successMessage).isEmpty();
    }

    public int getMealRowCount() {
        return driver.findElements(mealTableRows).size();
    }

    public void deleteFirstMeal() {
        WebElement btn = wait.until(ExpectedConditions.elementToBeClickable(deleteFirstBtn));
        btn.click();
        // Handle confirm dialog if present
        try {
            driver.switchTo().alert().accept();
        } catch (Exception ignored) {}
        // Wait for page to reload after the POST redirect.
        // Use the table element (always present, even when empty) rather than rows.
        wait.until(ExpectedConditions.stalenessOf(btn));
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("mealsTable")));
    }
}
