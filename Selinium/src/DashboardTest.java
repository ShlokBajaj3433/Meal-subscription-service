import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.time.Duration;

public class DashboardTest extends BaseTest {
    @Test
    public void shouldBrowseMealsAndLogoutFromDashboard() {
        driver = createDriver();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(1));

        try {
            String emailValue = "admin@mealsubscription.com";
            String passwordValue = "Admin@1234";

            // Login first
            driver.get(getUrl("/login"));
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("email"))).sendKeys(emailValue);
            driver.findElement(By.id("password")).sendKeys(passwordValue);
            driver.findElement(By.cssSelector("button[type='submit']")).click();

            wait.until(ExpectedConditions.urlContains("/dashboard"));

            // Test Browse Meals link
            wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[href='/meals']"))).click();
            wait.until(ExpectedConditions.urlContains("/meals"));

            Assert.assertTrue(driver.getCurrentUrl().contains("/meals"),
                    "Browse Meals should navigate to meals page");

            // Go back to dashboard and logout
            driver.navigate().back();
            wait.until(ExpectedConditions.elementToBeClickable(By.id("logout-link"))).click();
            wait.until(ExpectedConditions.urlContains("/login"));

            Assert.assertTrue(driver.getCurrentUrl().contains("/login"),
                    "Logout should redirect to login page");

        } finally {
            closeDriver();
        }
    }
}
