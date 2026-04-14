import io.restassured.RestAssured;

public class BaseTest {
    protected static String token;
    protected static String baseUrl;
    protected static String env;
    protected static long lastRun;
    protected static String tmdbBaseUrl;
    protected static String tmdbApiKey;

    static {
        env         = System.getProperty("env", "ad");
        baseUrl     = System.getProperty("plex.baseUrl");
        token       = System.getProperty("plex.token");
        lastRun     = Long.parseLong(System.getProperty("lastRun"));
        tmdbBaseUrl = System.getProperty("tmdb.baseUrl");
        tmdbApiKey  = System.getProperty("tmdb.apiKey");
        RestAssured.baseURI = baseUrl;
    }
}