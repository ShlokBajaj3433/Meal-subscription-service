import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.edge.EdgeDriver;

public class RegisterTest {
    public static void main(String[] args) throws InterruptedException {
        WebDriver driver = new EdgeDriver();

        try {
            driver.get("http://localhost:8080/register");
            Thread.sleep(2000);

            driver.findElement(By.id("name")).sendKeys("Test User");
            driver.findElement(By.id("email")).sendKeys("testuser" + System.currentTimeMillis() + "@example.com");
            driver.findElement(By.id("password")).sendKeys("testpassword");

            driver.findElement(By.cssSelector("button[type='submit']")).click();
            Thread.sleep(3000);

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
