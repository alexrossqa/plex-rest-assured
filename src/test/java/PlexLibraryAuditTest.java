import io.restassured.path.xml.XmlPath;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

import static io.restassured.RestAssured.given;

public class PlexLibraryAuditTest extends BaseTest {

    @Test
    public void testFindUnmatchedItems() {
        String response = given()
                .header("X-Plex-Token", token)
                .when()
                .get("/library/sections")
                .then()
                .statusCode(200)
                .extract().body().asString();

        XmlPath xmlPath = new XmlPath(response);
        List<String> libraryKeys = xmlPath.getList("MediaContainer.Directory.@key");
        List<String> libraryTitles = xmlPath.getList("MediaContainer.Directory.@title");

        List<String> unmatched = new ArrayList<>();

        for (int i = 0; i < libraryKeys.size(); i++) {
            String key = libraryKeys.get(i);
            String libraryTitle = libraryTitles.get(i);

            String libraryResponse = given()
                    .header("X-Plex-Token", token)
                    .when()
                    .get("/library/sections/" + key + "/all?type=1")
                    .then()
                    .statusCode(200)
                    .extract().body().asString();

            XmlPath libraryXml = new XmlPath(libraryResponse);
            int size = libraryXml.getInt("MediaContainer.@size");

            for (int j = 0; j < size; j++) {
                String prefix = "MediaContainer.Video[" + j + "].";
                String guid = libraryXml.getString(prefix + "@guid");
                String title = libraryXml.getString(prefix + "@title");

                if (guid != null && guid.startsWith("local://")) {
                    unmatched.add(libraryTitle + " | " + title + " | " + guid);
                }
            }
        }

        System.out.println("\n=== UNMATCHED ITEMS ===");
        System.out.println("Total: " + unmatched.size());
        for (String item : unmatched) {
            System.out.println(item);
        }
    }
}