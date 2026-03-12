import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class DashboardTest {
    public static void main(String[] args) {
        WebDriver driver = new EdgeDriver();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        try {
            String emailValue = "admin@mealsubscription.com";
            String passwordValue = "Admin@1234";

            // Login first
            driver.get("http://localhost:9090/login");
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("email"))).sendKeys(emailValue);
            driver.findElement(By.id("password")).sendKeys(passwordValue);
            driver.findElement(By.cssSelector("button[type='submit']")).click();

            wait.until(ExpectedConditions.urlContains("/dashboard"));

            // Test Browse Meals link
            wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[href='/meals']"))).click();
            wait.until(ExpectedConditions.urlContains("/meals"));

            if (driver.getCurrentUrl().contains("/meals")) {
                System.out.println("PASS: Browse Meals navigated to /meals.");
            } else {
                System.out.println("FAIL: Browse Meals did not navigate to /meals.");
            }

            // Go back to dashboard and logout
            driver.navigate().back();
            wait.until(ExpectedConditions.elementToBeClickable(By.id("logout-link"))).click();
            wait.until(ExpectedConditions.urlContains("/login"));

            if (driver.getCurrentUrl().contains("/login")) {
                System.out.println("PASS: Logout redirected to /login.");
            } else {
                System.out.println("FAIL: Logout did not redirect to /login.");
            }

        } finally {
            driver.quit();
        }
    }
}
