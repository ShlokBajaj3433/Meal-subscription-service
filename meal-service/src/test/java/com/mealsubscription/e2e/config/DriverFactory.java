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
 *  1. System property  -Dbrowser=chrome|edge   (explicit override)
 *  2. Auto-detect: Chrome binary → Edge binary  (whichever is found first)
 *
 * Reads the 'headless' system property to switch between headed (dev) and
 * headless (CI) modes transparently.
 */
public class DriverFactory {

    private static final String CHROME_BINARY_WINDOWS =
            "C:\\Program Files\\Google\\Chrome\\Application\\chrome.exe";
    private static final String EDGE_BINARY_WINDOWS =
            "C:\\Program Files (x86)\\Microsoft\\Edge\\Application\\msedge.exe";

    private DriverFactory() {}

    public static WebDriver createChromeDriver() {
        String browser = System.getProperty("browser", "auto").toLowerCase();
        boolean headless = Boolean.parseBoolean(System.getProperty("headless", "true"));

        if ("edge".equals(browser) || ("auto".equals(browser) && !chromeExists())) {
            return createEdgeDriver(headless);
        }
        return createChromeDriverInternal(headless);
    }

    private static boolean chromeExists() {
        java.io.File chrome = new java.io.File(CHROME_BINARY_WINDOWS);
        return chrome.exists();
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

    private static WebDriver createEdgeDriver(boolean headless) {
        WebDriverManager.edgedriver().setup();
        EdgeOptions options = new EdgeOptions();
        if (headless) {
            options.addArguments("--headless=new", "--no-sandbox",
                    "--disable-dev-shm-usage", "--disable-gpu");
        }
        options.addArguments("--window-size=1920,1080", "--remote-allow-origins=*");
        // Point to the Edge binary explicitly (avoids PATH issues on Windows)
        if (new java.io.File(EDGE_BINARY_WINDOWS).exists()) {
            options.setBinary(EDGE_BINARY_WINDOWS);
        }
        return new EdgeDriver(options);
    }
}

