import io.restassured.path.xml.XmlPath;
import org.testng.annotations.Test;

import java.util.List;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

public class PlexServerTest extends BaseTest {

    private String ratingKey;
    private String tmdbId;
    private String imdbId;

    @Test
    public void testServerIdentity() {
        given()
                .header("X-Plex-Token", token)
                .when()
                .get("/identity")
                .then()
                .statusCode(200)
                .body(containsString("MediaContainer"));
    }

    @Test
    public void testGetLibraries() {
        given()
                .header("X-Plex-Token", token)
                .when()
                .get("/library/sections")
                .then()
                .statusCode(200)
                .body(containsString("MediaContainer"))
                .body(containsString("Newly Released"));
    }

    @Test
    public void testGetRecentlyAdded() {
        String response = given()
                .header("X-Plex-Token", token)
            .when()
                .get("/library/recentlyAdded?X-Plex-Container-Size=100&type=1")
            .then()
                .statusCode(200)
                .body(containsString("MediaContainer"))
                .body(containsString("type=\"movie\""))
                .extract().body().asString();

        XmlPath xmlPath = new XmlPath(response);
        ratingKey = xmlPath.getString("MediaContainer.Video[0].@ratingKey");

        System.out.println("Rating key extracted: " + ratingKey);
    }

    @Test
    public void testItemMetadata() {
        given()
                .header("X-Plex-Token", token)
                .when()
                .get("/library/metadata/8659")
                .then()
                .statusCode(200)
                .body(containsString("imdb://"))
                .body(containsString("tmdb://"));
    }

    @Test(dependsOnMethods = "testGetRecentlyAdded")
    public void testGetItemMetadata() {
        String response = given()
                .header("X-Plex-Token", token)
                .when()
                .get("/library/metadata/" + ratingKey)
                .then()
                .statusCode(200)
                .body(containsString("imdb://"))
                .body(containsString("tmdb://"))
                .extract().body().asString();
        System.out.println(TestUtils.prettyPrintXml(response));

        XmlPath xmlPath = new XmlPath(response);

        List<String> guids = xmlPath.getList("MediaContainer.Video.Guid.@id");
        for (String guid : guids) {
            if (guid.startsWith("tmdb://")) {
                tmdbId = guid.replace("tmdb://", "");
            }
            if (guid.startsWith("imdb://")) {
                imdbId = guid.replace("imdb://", "");
            }
        }
        System.out.println("TMDB ID extracted: " + tmdbId);
        System.out.println("TMDB ID extracted: " + imdbId);
    }

    @Test(dependsOnMethods = "testGetItemMetadata")
    public void testTmdbLookup() {
        given()
                .queryParam("api_key", config.getProperty("tmdb.apiKey"))
                .when()
                .get(config.getProperty("tmdb.baseUrl") + "/movie/" + tmdbId)
                .then()
                .statusCode(200)
                .body(containsString("imdb_id"))
                .body(containsString("poster_path"));
    }

}
