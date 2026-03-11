import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.edge.EdgeDriver;

public class DashboardTest {
    public static void main(String[] args) throws InterruptedException {
        WebDriver driver = new EdgeDriver();

        try {
            // Login first
            driver.get("http://localhost:8080/login");
            Thread.sleep(2000);
            driver.findElement(By.id("email")).sendKeys("user@mealsubscription.com");
            driver.findElement(By.id("password")).sendKeys("User@1234");
            driver.findElement(By.cssSelector("button[type='submit']")).click();
            Thread.sleep(2000);

            // Test Browse Meals link
            driver.findElement(By.linkText("Browse Meals")).click();
            Thread.sleep(2000);

            if (driver.getCurrentUrl().contains("/meals")) {
                System.out.println("PASS: Browse Meals navigated to /meals.");
            } else {
                System.out.println("FAIL: Browse Meals did not navigate to /meals.");
            }

            // Go back to dashboard and logout
            driver.navigate().back();
            Thread.sleep(2000);
            driver.findElement(By.id("logout-link")).click();
            Thread.sleep(2000);

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
