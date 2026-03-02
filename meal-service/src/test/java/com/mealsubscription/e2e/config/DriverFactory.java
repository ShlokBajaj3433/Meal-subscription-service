package com.mealsubscription.e2e.config;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

/**
 * Factory for creating configured WebDriver instances.
 * Reads the 'headless' system property to switch between headed (dev) and
 * headless (CI) modes transparently.
 */
public class DriverFactory {

    private DriverFactory() {}

    public static WebDriver createChromeDriver() {
        WebDriverManager.chromedriver().setup();

        ChromeOptions options = new ChromeOptions();
        boolean headless = Boolean.parseBoolean(System.getProperty("headless", "true"));

        if (headless) {
            // --headless=new is the modern headless mode (Chrome 112+)
            options.addArguments("--headless=new");
            options.addArguments("--no-sandbox");
            options.addArguments("--disable-dev-shm-usage");
            options.addArguments("--disable-gpu");
        }

        options.addArguments("--window-size=1920,1080");
        options.addArguments("--remote-allow-origins=*");

        return new ChromeDriver(options);
    }
}
