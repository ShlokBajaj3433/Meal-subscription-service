import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;

public class RegisterTest {
    public static void main(String[] args) {
        WebDriver driver = new ChromeDriver();

        try {
            driver.get("http://localhost:8080/register"); 
            Thread.sleep(2000);

            WebElement nameField = driver.findElement(By.id("name"));
            nameField.sendKeys("Test User");

            WebElement emailField = driver.findElement(By.id("email"));
            emailField.sendKeys("testuser@example.com");

            WebElement passwordField = driver.findElement(By.id("password"));
            passwordField.sendKeys("testpassword");

            // Assuming button inside the form is the only one
            driver.findElement(By.cssSelector("button[type='submit']")).click();

            Thread.sleep(3000); // Wait to observe registration result

        } catch (InterruptedException e) {
            System.out.println("Execution was interrupted: " + e.getMessage());
        } finally {
            if (driver != null) {
                driver.quit();
            }
        }
    }
}
