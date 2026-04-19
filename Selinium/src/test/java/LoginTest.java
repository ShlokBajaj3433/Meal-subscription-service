import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.time.Duration;

public class LoginTest extends BaseTest {
    @Test
    public void shouldLoginAsAdmin() {
        driver = createDriver();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(1));

        try {
            String emailValue = "admin@mealsubscription.com";
            String passwordValue = "Admin@1234";

            driver.get(getUrl("/login"));

            wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("email")));
            driver.findElement(By.id("email")).sendKeys(emailValue);

            driver.findElement(By.id("password")).sendKeys(passwordValue);

            driver.findElement(By.cssSelector("button[type='submit']")).click();
            wait.until(ExpectedConditions.or(
                    ExpectedConditions.urlContains("/dashboard"),
                    ExpectedConditions.presenceOfElementLocated(By.id("login-error"))
            ));

            Assert.assertTrue(driver.getCurrentUrl().contains("/dashboard"),
                    "Login should redirect admin user to dashboard");

        } finally {
            closeDriver();
        }
    }
}
