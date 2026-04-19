import org.testng.TestNG;

public class App {
     public static void main(String[] args) throws Exception {
        TestNG testng = new TestNG();
        testng.setTestSuites(java.util.Collections.singletonList("testng.xml"));
        testng.run();
    }
}
