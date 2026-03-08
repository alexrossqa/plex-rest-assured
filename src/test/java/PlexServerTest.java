import org.testng.annotations.Test;

import java.util.Properties;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

public class PlexServerTest extends BaseTest {

    @Test
    public void testServerIdentity() {
        given()
                .header("X-Plex-Token", config.getProperty("token"))
                .when()
                .get("/identity")
                .then()
                .statusCode(200)
                .body(containsString("MediaContainer"));
    }

    @Test
    public void testGetLibraries() {
        given()
                .header("X-Plex-Token", config.getProperty("token"))
                .when()
                .get("/library/sections")
                .then()
                .statusCode(200)
                .body(containsString("MediaContainer"))
                .body(containsString("Newly Released"));
    }

    @Test
    public void testGetRecentlyAdded() {
        given()
                .header("X-Plex-Token", config.getProperty("token"))
                .when()
                .get("/library/recentlyAdded?X-Plex-Container-Size=100&type=1")
                .then()
                .statusCode(200)
                .body(containsString("MediaContainer"))
                .body(containsString("type=\"movie\""));
    }

    @Test
    public void testItemMetadata() {
        given()
                .header("X-Plex-Token", config.getProperty("token"))
                .when()
                .get("/library/metadata/8659")
                .then()
                .statusCode(200)
                .body(containsString("imdb://"))
                .body(containsString("tmdb://"));
    }
}
