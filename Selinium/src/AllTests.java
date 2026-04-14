import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;


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
        System.out.println("Running all Selenium tests");
        System.out.println("========================================\n");

        try {
            System.out.println("1. Running LoginTest...");
            LoginTest.main(args);
            System.out.println();
        } catch (Exception e) {
            System.err.println("LoginTest failed: " + e.getMessage());
        }

        try {
            System.out.println("2. Running RegisterTest...");
            RegisterTest.main(args);
            System.out.println();
        } catch (Exception e) {
            System.err.println("RegisterTest failed: " + e.getMessage());
        }

        try {
            System.out.println("3. Running MealsTest...");
            MealsTest.main(args);
            System.out.println();
        } catch (Exception e) {
            System.err.println("MealsTest failed: " + e.getMessage());
        }

        try {
            System.out.println("4. Running DashboardTest...");
            DashboardTest.main(args);
            System.out.println();
        } catch (Exception e) {
            System.err.println("DashboardTest failed: " + e.getMessage());
        }

        try {
            System.out.println("5. Running AdminMealsTest...");
            AdminMealsTest.main(args);
            System.out.println();
        } catch (Exception e) {
            System.err.println("AdminMealsTest failed: " + e.getMessage());
        }

        System.out.println("========================================");
        System.out.println("All tests completed");
        System.out.println("========================================");
    }
}
