import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.edge.EdgeDriver;

public class LoginTest {
    public static void main(String[] args) throws InterruptedException {
        WebDriver driver = new EdgeDriver();

        try {
            driver.get("http://localhost:8080/login");
            Thread.sleep(2000);

            WebElement email = driver.findElement(By.id("email"));
            email.sendKeys("user@mealsubscription.com");

            WebElement password = driver.findElement(By.id("password"));
            password.sendKeys("User@1234");

            driver.findElement(By.cssSelector("button[type='submit']")).click();
            Thread.sleep(3000);

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
