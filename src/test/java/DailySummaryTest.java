import io.restassured.path.xml.XmlPath;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;

public class DailySummaryTest extends BaseTest {

    private List<String> ratingKeys = new ArrayList<>();
    private List<String> tmdbIds = new ArrayList<>();
    private List<String> imdbIds = new ArrayList<>();

    @Test
    public void testGetNewAdditions() {
        String response = given()
                .header("X-Plex-Token", token)
                .when()
                .get("/library/recentlyAdded?X-Plex-Container-Size=100&type=1")
                .then()
                .statusCode(200)
                .body(containsString("MediaContainer"))
                .extract().body().asString();

        XmlPath xmlPath = new XmlPath(response);
        List<String> allRatingKeys = xmlPath.getList("MediaContainer.Video.@ratingKey");
        List<String> allAddedAts = xmlPath.getList("MediaContainer.Video.@addedAt");
        List<String> allLibraryIds = xmlPath.getList("MediaContainer.Video.@librarySectionID");

        for (int i = 0; i < allRatingKeys.size(); i++) {
            long addedAt = Long.parseLong(allAddedAts.get(i));
            String libraryId = allLibraryIds.get(i);

            // exclude FILMS TEMP (46) and AD TV 2 (44)
            if (libraryId.equals("46") || libraryId.equals("44")) {
                continue;
            }

            // only include items added since last run
            if (addedAt > lastRun) {
                ratingKeys.add(allRatingKeys.get(i));
            }
        }

        System.out.println("New additions since last run: " + ratingKeys.size());
        System.out.println("Rating keys: " + ratingKeys);
    }

    @Test(dependsOnMethods = "testGetNewAdditions")
    public void testGetMetadataForNewAdditions() {
        for (String ratingKey : ratingKeys) {
            String response = given()
                    .header("X-Plex-Token", token)
                    .when()
                    .get("/library/metadata/" + ratingKey)
                    .then()
                    .statusCode(200)
                    .extract().body().asString();

            XmlPath xmlPath = new XmlPath(response);
            List<String> guids = xmlPath.getList("MediaContainer.Video.Guid.@id");

            String tmdbId = null;
            String imdbId = null;

            for (String guid : guids) {
                if (guid.startsWith("tmdb://")) tmdbId = guid.replace("tmdb://", "");
                if (guid.startsWith("imdb://")) imdbId = guid.replace("imdb://", "");
            }

            if (tmdbId != null) tmdbIds.add(tmdbId);
            if (imdbId != null) imdbIds.add(imdbId);

            System.out.println("Title ratingKey=" + ratingKey + " TMDB=" + tmdbId + " IMDB=" + imdbId);
        }

        System.out.println("Total with TMDB ID: " + tmdbIds.size());
        System.out.println("Total with IMDB ID: " + imdbIds.size());
    }
}
