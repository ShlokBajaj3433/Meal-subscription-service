import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.support.ui.Select;

public class MealsTest {
    public static void main(String[] args) throws InterruptedException {
        WebDriver driver = new EdgeDriver();

        try {
            driver.get("http://localhost:9090/meals");
            Thread.sleep(2000);

            // Test filtering by dietary type
            WebElement filterDropdown = driver.findElement(By.id("dietaryTypeFilter"));
            new Select(filterDropdown).selectByValue("VEGAN");
            Thread.sleep(2000);

            // Click Add to Week on the first meal card if any exist
            java.util.List<WebElement> addButtons = driver.findElements(By.className("btn-add"));
            if (!addButtons.isEmpty()) {
                addButtons.get(0).click();
                System.out.println("PASS: Clicked 'Add to Week' button.");
            } else {
                System.out.println("INFO: No meals available to add.");
            }

            Thread.sleep(2000);

        } finally {
            driver.quit();
        }
    }
}
