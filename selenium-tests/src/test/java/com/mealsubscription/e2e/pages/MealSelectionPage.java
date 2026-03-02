package com.mealsubscription.e2e.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.List;

public class MealSelectionPage {

    private final WebDriver driver;
    private final WebDriverWait wait;

    private final By dietaryFilter   = By.id("dietaryTypeFilter");
    private final By mealCards       = By.cssSelector(".meal-card");
    private final By mealCardNames   = By.cssSelector(".meal-card .meal-name");
    private final By addToWeekButton = By.cssSelector(".meal-card .btn-add");

    public MealSelectionPage(WebDriver driver, WebDriverWait wait) {
        this.driver = driver;
        this.wait   = wait;
    }

    public void selectDietaryFilter(String dietaryType) {
        WebElement select = wait.until(ExpectedConditions.visibilityOfElementLocated(dietaryFilter));
        new Select(select).selectByValue(dietaryType);
        // Wait for page/results to refresh
        wait.until(ExpectedConditions.stalenessOf(driver.findElement(mealCards)));
    }

    public int getMealCardCount() {
        return driver.findElements(mealCards).size();
    }

    public List<String> getMealNames() {
        return driver.findElements(mealCardNames)
            .stream()
            .map(WebElement::getText)
            .toList();
    }

    public void addFirstMealToWeek() {
        List<WebElement> addButtons = wait.until(
            ExpectedConditions.presenceOfAllElementsLocatedBy(addToWeekButton));
        if (!addButtons.isEmpty()) {
            addButtons.get(0).click();
        }
    }

    public boolean isMealListVisible() {
        return !driver.findElements(mealCards).isEmpty();
    }
}
