import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.Select;

public class MealsTest {
    public static void main(String[] args) {
        WebDriver driver = new ChromeDriver();

        try {
            driver.get("http://localhost:8080/meals");
            Thread.sleep(2000);

            // Test filtering
            WebElement filterDropdown = driver.findElement(By.id("dietaryTypeFilter"));
            Select select = new Select(filterDropdown);
            select.selectByValue("VEGAN");
            Thread.sleep(2000); // Page might reload or JS will submit

            // Test clicking Add to Week on the first meal card if it exists
            java.util.List<WebElement> addButtons = driver.findElements(By.className("btn-add"));
            if (!addButtons.isEmpty()) {
                addButtons.get(0).click();
                System.out.println("Clicked 'Add to Week' button.");
            } else {
                System.out.println("No meals available to add.");
            }

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
