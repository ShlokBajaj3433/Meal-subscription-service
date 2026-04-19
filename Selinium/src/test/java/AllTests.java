import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import org.testng.TestNG;


public class AllTests {
    public static void main(String[] args) throws Exception {
        // Suppress known Selenium CDP nearest-match warning noise for Edge 145.
        Logger rootLogger = LogManager.getLogManager().getLogger("");
        rootLogger.setLevel(Level.SEVERE);
        for (Handler handler : rootLogger.getHandlers()) {
            handler.setLevel(Level.SEVERE);
        }
        Logger.getLogger("org.openqa.selenium.devtools.CdpVersionFinder").setLevel(Level.SEVERE);

        System.out.println("========================================");
        System.out.println("Running all Selenium TestNG tests");
        System.out.println("========================================\n");

        TestNG testng = new TestNG();
        testng.setTestSuites(java.util.Collections.singletonList("testng.xml"));
        testng.run();

        System.out.println("========================================");
        System.out.println("All tests completed");
        System.out.println("========================================");
    }
}
