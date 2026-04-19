import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.edge.EdgeDriver;

public class BaseTest {
    protected static WebDriver driver;
    protected static final String BASE_URL = "http://localhost:9090";

    /**
     * Simple method to create WebDriver
     * Make sure you have chromedriver.exe or msedgedriver.exe in your PATH
     */
    protected static WebDriver createDriver() {
        String browser = System.getProperty("browser", "chrome").toLowerCase();
        
        if ("edge".equals(browser)) {
            driver = new EdgeDriver();
        } else {
            driver = new ChromeDriver();
        }
        
        return driver;
    }
    
    /**
     * Get base URL from system property or use default
     */
    protected static String getUrl(String path) {
        String baseUrl = System.getProperty("app.base.url", BASE_URL);
        return baseUrl + path;
    }

    /**
     * Close the browser
     */
    protected synchronized void closeDriver() {
        if (driver != null) {
            driver.quit();
            driver = null;
        }
    }
}
