import io.restassured.RestAssured;
import org.testng.annotations.BeforeClass;

import java.io.InputStream;
import java.util.Properties;

public class BaseTest {

    protected static Properties config = new Properties();
    protected static String token;
    protected static String baseUrl;
    protected static String env;

    @BeforeClass
    public void setup() throws Exception {
        InputStream input = getClass()
                .getClassLoader()
                .getResourceAsStream("config.properties");
        config.load(input);

        env = System.getProperty("env", "ad");
        baseUrl = config.getProperty("plex." + env + ".baseUrl");
        token = config.getProperty("plex." + env + ".token");

        RestAssured.baseURI = baseUrl;
    }
}