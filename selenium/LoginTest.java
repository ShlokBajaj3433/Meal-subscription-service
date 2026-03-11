import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;

public class LoginTest {
    public static void main(String[] args) {
        // WebDriver setup: Initialize the ChromeDriver
        // Note: With Selenium 4.6.0+, Selenium Manager automatically handles the driver executable.
        WebDriver driver = new ChromeDriver();

        try {
            // Use driver.get() to open the login page
            driver.get("URL_OF_MEAL_SUBSCRIPTION_LOGIN_PAGE");

            // Add basic wait to ensure the page has loaded before interacting
            Thread.sleep(2000); 

            // Locate the username field using driver.findElement(By.id("email"))
            WebElement username = driver.findElement(By.id("email"));
            
            // Use sendKeys() to enter the username
            username.sendKeys("testuser");

            // Locate the password field using driver.findElement(By.id("password"))
            WebElement password = driver.findElement(By.id("password"));
            
            // Use sendKeys() to enter the password
            password.sendKeys("testpassword");

            // Locate the login button and click it
            driver.findElement(By.id("login")).click();

            // Add basic wait to observe the result after clicking login
            Thread.sleep(3000);

        } catch (InterruptedException e) {
            System.out.println("Execution was interrupted: " + e.getMessage());
        } finally {
            // Close the browser at the end to free up resources
            if (driver != null) {
                driver.quit();
            }
        }
    }
}
