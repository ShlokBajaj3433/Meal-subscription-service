import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class LoginTest {
    public static void main(String[] args) {
        WebDriver driver = new EdgeDriver();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(1));

        try {
            String emailValue = "admin@mealsubscription.com";
            String passwordValue = "Admin@1234";

            driver.get("http://localhost:9090/login");

            WebElement email = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("email")));
            email.sendKeys(emailValue);

            WebElement password = driver.findElement(By.id("password"));
            password.sendKeys(passwordValue);

            driver.findElement(By.cssSelector("button[type='submit']")).click();
            wait.until(ExpectedConditions.or(
                    ExpectedConditions.urlContains("/dashboard"),
                    ExpectedConditions.presenceOfElementLocated(By.id("login-error"))
            ));

            if (driver.getCurrentUrl().contains("/dashboard")) {
                System.out.println("PASS: Login successful, redirected to dashboard.");
            } else {
                System.out.println("FAIL: Login did not redirect to dashboard.");
            }

        } finally {
            driver.quit();
        }
    }
}
