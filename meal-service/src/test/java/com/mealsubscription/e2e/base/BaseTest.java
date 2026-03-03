package com.mealsubscription.e2e.base;

import com.mealsubscription.e2e.config.DriverFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

/**
 * Base class for all Selenium tests.
 *
 * Lifecycle:
 *  @BeforeEach — creates a fresh ChromeDriver; sets page load and wait timeouts
 *  @AfterEach  — always quits the driver (frees port + process)
 *
 * All page objects receive the same driver + wait instances via constructor injection.
 */
public abstract class BaseTest {

    protected WebDriver driver;
    protected WebDriverWait wait;

    /** Base URL for the running application — override with -Dapp.base.url in CI */
    protected static final String BASE_URL =
        System.getProperty("app.base.url", "http://localhost:8080");

    /** Explicit wait timeout — long enough for CI, tight enough to catch real failures */
    private static final Duration WAIT_TIMEOUT = Duration.ofSeconds(15);

    @BeforeEach
    void setUp() {
        driver = DriverFactory.createChromeDriver();
        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(30));
        wait   = new WebDriverWait(driver, WAIT_TIMEOUT);
    }

    @AfterEach
    void tearDown() {
        if (driver != null) {
            driver.quit();
            driver = null;
        }
    }

    /**
     * Convenience method to pause execution for the given number of milliseconds.
     * Tests can call this directly when they need an explicit delay.
     */
    protected void delay(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Navigate to a path relative to BASE_URL.
     * Example: navigateTo("/login")
     */
    protected void navigateTo(String path) {
        driver.get(BASE_URL + path);
        // pause after navigation to make it easier to follow - default 3000ms
        long delay = 1000;
        String delayProp = System.getProperty("test.delay.ms");
        if (delayProp != null) {
            try {
                delay = Long.parseLong(delayProp);
            } catch (NumberFormatException ignored) {
                // ignore invalid values and keep default
            }
        }
        try {
            Thread.sleep(delay);
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        }
    }
}
