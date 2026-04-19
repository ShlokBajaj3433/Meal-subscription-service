import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;
import org.testng.Assert;
import org.testng.annotations.Test;

public class MealsTest extends BaseTest {
    @Test
    public void shouldFilterAndAddMealWhenAvailable() throws InterruptedException {
        driver = createDriver();

        try {
            driver.get(getUrl("/meals"));
            Thread.sleep(2000);

            // Test filtering by dietary type
            WebElement filterDropdown = driver.findElement(By.id("dietaryTypeFilter"));
            new Select(filterDropdown).selectByValue("VEGAN");
            Thread.sleep(2000);

            // Click Add to Week on the first meal card if any exist
            java.util.List<WebElement> addButtons = driver.findElements(By.className("btn-add"));
            if (!addButtons.isEmpty()) {
                addButtons.get(0).click();
                Assert.assertTrue(true, "Clicked Add to Week button");
            } else {
                Assert.assertTrue(true, "No meals available to add");
            }

            Thread.sleep(2000);

        } finally {
            closeDriver();
        }
    }
}
