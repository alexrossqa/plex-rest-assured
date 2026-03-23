import io.restassured.path.xml.XmlPath;
import org.testng.annotations.Test;

import java.util.List;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

public class PlexServerTest extends BaseTest {

    @Test(groups = {"smoke", "regression"})
    public void testServerIdentity() {
        given()
                .header("X-Plex-Token", token)
                .when()
                .get("/identity")
                .then()
                .statusCode(200)
                .body(containsString("MediaContainer"));
    }

    @Test(groups = {"smoke", "regression"})
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
  }
