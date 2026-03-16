import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class RegisterTest {
    public static void main(String[] args) {
        WebDriver driver = new EdgeDriver();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(1));

        try {
            driver.get("http://localhost:9090/register");

            String uniqueEmail = "testuser+" + System.currentTimeMillis() + "@example.com";

            driver.findElement(By.id("name")).sendKeys("Test User");
            driver.findElement(By.id("email")).sendKeys(uniqueEmail);
            driver.findElement(By.id("password")).sendKeys("testpassword");

            driver.findElement(By.cssSelector("button[type='submit']")).click();
            wait.until(ExpectedConditions.or(
                    ExpectedConditions.urlContains("/login"),
                    ExpectedConditions.presenceOfElementLocated(By.cssSelector("[data-testid='register-error']"))
            ));

            if (driver.getCurrentUrl().contains("/login")) {
                System.out.println("PASS: Registration successful, redirected to login.");
            } else {
                System.out.println("FAIL: Registration did not redirect to login.");
            }

        } finally {
            driver.quit();
        }
    }
}
