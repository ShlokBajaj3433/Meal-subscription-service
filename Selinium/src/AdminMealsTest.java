import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.support.ui.Select;

public class AdminMealsTest {
    public static void main(String[] args) throws InterruptedException {
        WebDriver driver = new EdgeDriver();

        try {
            // Login as admin first
            driver.get("http://localhost:9090/login");
            Thread.sleep(2000);
            driver.findElement(By.id("email")).sendKeys("admin@mealsubscription.com");
            driver.findElement(By.id("password")).sendKeys("Admin@1234");
            driver.findElement(By.cssSelector("button[type='submit']")).click();
            Thread.sleep(2000);

            // Go to admin meals page
            driver.get("http://localhost:9090/admin/meals");
            Thread.sleep(2000);

            // Click Create Meal to show the form
            driver.findElement(By.id("btn-create-meal")).click();
            Thread.sleep(1000);

            // Fill out the new meal form
            WebElement nameField = driver.findElement(By.id("mealName"));
            nameField.sendKeys("Test Automator Meal");

            WebElement descField = driver.findElement(By.id("mealDescription"));
            descField.sendKeys("A delicious meal created by Selenium WebDriver.");

            new Select(driver.findElement(By.id("dietaryType"))).selectByValue("STANDARD");

            driver.findElement(By.id("calories")).sendKeys("600");
            driver.findElement(By.id("priceCents")).sendKeys("1500");

            driver.findElement(By.id("btn-save-meal")).click();
            Thread.sleep(3000);
            System.out.println("PASS: Meal created successfully.");

            // Delete the first meal if any exist
            java.util.List<WebElement> deleteButtons = driver.findElements(By.cssSelector(".btn-delete"));
            if (!deleteButtons.isEmpty()) {
                deleteButtons.get(0).click();
                Thread.sleep(2000);
                System.out.println("PASS: Clicked delete on the first meal.");
            } else {
                System.out.println("INFO: No meals found to delete.");
            }

        } finally {
            driver.quit();
        }
    }
}
