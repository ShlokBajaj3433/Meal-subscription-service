import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;

public class DashboardTest {
    public static void main(String[] args) {
        WebDriver driver = new ChromeDriver();

        try {
            // Usually dashboard requires login first, but we are automating just the page interaction
            driver.get("http://localhost:8080/dashboard");
            Thread.sleep(2000);

            // Test navigation to meals page
            driver.findElement(By.linkText("Browse Meals")).click();
            Thread.sleep(2000);

            // Go back and test logout
            driver.navigate().back();
            Thread.sleep(2000);
            driver.findElement(By.id("logout-link")).click();

            Thread.sleep(2000);

        } catch (InterruptedException e) {
            System.out.println("Execution was interrupted: " + e.getMessage());
        } finally {
            if (driver != null) {
                driver.quit();
            }
        }
    }
}
