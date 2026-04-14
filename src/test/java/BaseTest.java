import io.restassured.RestAssured;
import org.testng.annotations.BeforeSuite;

public class BaseTest {

    protected static String token;
    protected static String baseUrl;
    protected static String env;
    protected static long lastRun;
    protected static String tmdbBaseUrl;
    protected static String tmdbApiKey;

    @BeforeSuite
    public void setup() {
        env         = System.getProperty("env", "ad");
        baseUrl     = System.getProperty("plex.baseUrl");
        token       = System.getProperty("plex.token");
        lastRun     = Long.parseLong(System.getProperty("lastRun"));
        tmdbBaseUrl = System.getProperty("tmdb.baseUrl");
        tmdbApiKey  = System.getProperty("tmdb.apiKey");

        System.out.println("DEBUG plextoken = " + System.getProperty("plextoken"));
        System.out.println("DEBUG plex.token = " + System.getProperty("plex.token"));

        RestAssured.baseURI = baseUrl;
    }
}