package com.mealsubscription.e2e.config;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;

/**
 * Factory for creating configured WebDriver instances.
 *
 * Browser selection (in priority order):
 *  1. System property  -Dbrowser=chrome|chromium|brave|edge   (explicit override)
 *  2. Default: Edge  (always kept up-to-date on Windows via Windows Update;
 *     avoids ChromeDriver/Chrome version-mismatch issues)
 *
 * Reads the 'headless' system property to switch between headed (dev) and
 * headless (CI) modes.
 *   -Dheadless=false   → visible browser window (default for local runs)
 *   -Dheadless=true    → headless mode (CI/CD)
 *
 * Examples:
 *   mvn test -Pe2e -pl meal-service -Dheadless=false
 *   mvn test -Pe2e -pl meal-service -Dbrowser=chrome  -Dheadless=false
 *   mvn test -Pe2e -pl meal-service -Dbrowser=edge    -Dheadless=false
 */
public class DriverFactory {

    private static final String CHROME_BINARY_WINDOWS =
            "C:\\Program Files\\Google\\Chrome\\Application\\chrome.exe";
    private static final String BRAVE_BINARY_WINDOWS =
            "C:\\Program Files\\BraveSoftware\\Brave-Browser\\Application\\brave.exe";
    private static final String EDGE_BINARY_WINDOWS =
            "C:\\Program Files (x86)\\Microsoft\\Edge\\Application\\msedge.exe";

    private DriverFactory() {}

    public static WebDriver createChromeDriver() {
        // Default browser is now Edge — avoids Chrome vs ChromeDriver version mismatches.
        // Override with -Dbrowser=chrome|chromium|brave|edge as needed.
        String browser = System.getProperty("browser", "edge").toLowerCase();
        // Default headless=false so tests are visible locally; CI should pass -Dheadless=true
        boolean headless = Boolean.parseBoolean(System.getProperty("headless", "false"));

        switch (browser) {
            case "chromium":
            case "brave":
                return createBraveDriver(headless);
            case "chrome":
                return createChromeDriverInternal(headless);
            case "auto":
                // Legacy auto-detect order retained when explicitly requested
                if (chromeExists()) return createChromeDriverInternal(headless);
                if (braveExists())  return createBraveDriver(headless);
                return createEdgeDriver(headless);
            default: // "edge" and anything unrecognised → Edge
                return createEdgeDriver(headless);
        }
    }

    private static boolean chromeExists() {
        return new java.io.File(CHROME_BINARY_WINDOWS).exists();
    }

    private static boolean braveExists() {
        return new java.io.File(BRAVE_BINARY_WINDOWS).exists();
    }

    private static WebDriver createChromeDriverInternal(boolean headless) {
        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        if (headless) {
            options.addArguments("--headless=new", "--no-sandbox",
                    "--disable-dev-shm-usage", "--disable-gpu");
        }
        options.addArguments("--window-size=1920,1080", "--remote-allow-origins=*");
        return new ChromeDriver(options);
    }

    /**
     * Brave is 100% Chromium-based — it uses ChromeDriver with the brave.exe binary.
     * We pin ChromeDriver to Brave's Chromium version (145) so WDM downloads
     * a compatible driver automatically. No bravedriver() needed.
     */
    private static WebDriver createBraveDriver(boolean headless) {
        WebDriverManager.chromedriver().browserVersion("145").setup();
        ChromeOptions options = new ChromeOptions();
        options.setBinary(BRAVE_BINARY_WINDOWS);
        if (headless) {
            options.addArguments("--headless=new", "--no-sandbox",
                    "--disable-dev-shm-usage", "--disable-gpu");
        }
        options.addArguments("--window-size=1920,1080", "--remote-allow-origins=*");
        return new ChromeDriver(options);
    }

    private static WebDriver createEdgeDriver(boolean headless) {
        WebDriverManager.edgedriver().setup();
        EdgeOptions options = new EdgeOptions();
        if (headless) {
            options.addArguments("--headless=new", "--no-sandbox",
                    "--disable-dev-shm-usage", "--disable-gpu");
        }
        options.addArguments("--window-size=1920,1080", "--remote-allow-origins=*");
        if (new java.io.File(EDGE_BINARY_WINDOWS).exists()) {
            options.setBinary(EDGE_BINARY_WINDOWS);
        }
        return new EdgeDriver(options);
    }
}

