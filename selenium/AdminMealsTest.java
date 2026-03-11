import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.Select;

public class AdminMealsTest {
    public static void main(String[] args) {
        WebDriver driver = new ChromeDriver();

        try {
            // Depending on the app's security, you might need to login as admin first.
            // For automation scope, we navigate directly:
            driver.get("http://localhost:8080/admin/meals");
            Thread.sleep(2000);

            // Test clicking Create Meal to show the form
            driver.findElement(By.id("btn-create-meal")).click();
            Thread.sleep(1000); // Give JS time to unhide the form

            // Fill out new meal form
            WebElement nameField = driver.findElement(By.id("mealName"));
            nameField.sendKeys("Test Automator Meal");

            WebElement descField = driver.findElement(By.id("mealDescription"));
            descField.sendKeys("A delicious meal created by Selenium WebDriver.");

            WebElement dietaryDropdown = driver.findElement(By.id("dietaryType"));
            Select select = new Select(dietaryDropdown);
            select.selectByValue("STANDARD");

            WebElement caloriesField = driver.findElement(By.id("calories"));
            caloriesField.sendKeys("600");

            WebElement priceField = driver.findElement(By.id("priceCents"));
            priceField.sendKeys("1500"); // $15.00

            // Submit the form
            driver.findElement(By.id("btn-save-meal")).click();
            Thread.sleep(3000); // Wait for redirect and reload

            // Optionally test delete if a meal exists
            java.util.List<WebElement> deleteButtons = driver.findElements(By.cssSelector(".btn-delete"));
            if (!deleteButtons.isEmpty()) {
                // Warning: This deletes the first meal in the table
                deleteButtons.get(0).click();
                System.out.println("Clicked delete on the first meal.");
                Thread.sleep(2000);
            }

        } catch (InterruptedException e) {
            System.out.println("Execution was interrupted: " + e.getMessage());
        } finally {
            if (driver != null) {
                driver.quit();
            }
        }
    }
}
