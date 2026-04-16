import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.time.Duration;

public class RegisterTest extends BaseTest {
    @Test
    public void shouldRegisterNewUser() {
        driver = createDriver();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(1));

        try {
            driver.get(getUrl("/register"));

            String uniqueEmail = "testuser+" + System.currentTimeMillis() + "@example.com";

            driver.findElement(By.id("name")).sendKeys("Test User");
            driver.findElement(By.id("email")).sendKeys(uniqueEmail);
            driver.findElement(By.id("password")).sendKeys("testpassword");

            driver.findElement(By.cssSelector("button[type='submit']")).click();
            wait.until(ExpectedConditions.or(
                    ExpectedConditions.urlContains("/login"),
                    ExpectedConditions.presenceOfElementLocated(By.cssSelector("[data-testid='register-error']"))
            ));

            Assert.assertTrue(driver.getCurrentUrl().contains("/login"),
                    "Successful registration should redirect to login");

        } finally {
            closeDriver();
        }
    }
}
