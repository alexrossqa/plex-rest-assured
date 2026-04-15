import io.restassured.path.xml.XmlPath;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.testng.Assert.assertFalse;

public class DailySummaryTest extends BaseTest {

    private List<PlexVideo> newAdditions = new ArrayList<>();
    private static final List<String> EXCLUDED_LIBRARIES = Arrays.asList("44", "46");

    @Test(groups = {"daily", "regression"})
    public void testGetNewAdditions() {
        String response = given()
                .header("X-Plex-Token", token)
                .when()
                .get("/library/recentlyAdded?X-Plex-Container-Size=100&type=1")
                .then()
                .statusCode(200)
                .body(containsString("MediaContainer"))
                .extract().body().asString();

        newAdditions = PlexResponseMapper.mapRecentlyAdded(response, lastRun, EXCLUDED_LIBRARIES);

        System.out.println("New additions since last run: " + newAdditions.size());
        assertFalse(newAdditions.isEmpty(), "Expected at least one new addition");
    }

    @Test(dependsOnMethods = "testGetNewAdditions", groups = {"daily", "regression"})
    public void testGetMetadataForNewAdditions() {
        for (PlexVideo video : newAdditions) {
            String response = given()
                    .header("X-Plex-Token", token)
                    .when()
                    .get("/library/metadata/" + video.getRatingKey())
                    .then()
                    .statusCode(200)
                    .extract().body().asString();

            PlexResponseMapper.mapMetadata(video, response);
            System.out.println(video);
        }

        long withImdb = newAdditions.stream().filter(v -> v.getImdbId() != null).count();
        long withTmdb = newAdditions.stream().filter(v -> v.getTmdbId() != null).count();

        System.out.println("With IMDB ID: " + withImdb + "/" + newAdditions.size());
        System.out.println("With TMDB ID: " + withTmdb + "/" + newAdditions.size());
    }

    @Test(dependsOnMethods = "testGetMetadataForNewAdditions", groups = {"daily", "regression"})
    public void testValidateTmdbRecords() {
        List<PlexVideo> matched = newAdditions.stream()
                .filter(v -> v.getTmdbId() != null && v.getImdbId() != null).toList();

        System.out.println("Validating " + matched.size() + " matched items against TMDB");

        for (PlexVideo video : matched) {
            System.out.println(video.getTmdbId());
            given()
                    .queryParam("api_key", tmdbApiKey)
                    .when()
                    .get(tmdbBaseUrl + "/movie/" + video.getTmdbId())
                    .then()
                    .statusCode(200)
                    .body(containsString("imdb_id"))
                    .body(containsString("poster_path"));
        }
    }
}
