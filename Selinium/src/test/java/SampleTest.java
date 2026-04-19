import org.testng.Assert;
import org.testng.annotations.Test;

public class SampleTest {
    @Test
    public void sampleTest() {
        System.out.println("TestNG is working!");
        Assert.assertTrue(true, "Sanity check should pass");
    }
}