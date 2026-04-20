import io.restassured.path.xml.XmlPath;
import java.util.ArrayList;
import java.util.List;

public class PlexResponseMapper {



    public static List<PlexVideo> mapRecentlyAdded(String xmlResponse, long lastRun,
                                                   List<String> excludedLibraryIds) {
        XmlPath xmlPath = new XmlPath(xmlResponse);
        List<PlexVideo> videos = new ArrayList<>();

        int size = xmlPath.getInt("MediaContainer.@size");

        for (int i = 0; i < size; i++) {
            String prefix = "MediaContainer.Video[" + i + "].";

            String libraryId = xmlPath.getString(prefix + "@librarySectionID");
            String addedAtStr = xmlPath.getString(prefix + "@addedAt");

            if (addedAtStr == null) continue;
            long addedAt = Long.parseLong(addedAtStr);

            if (excludedLibraryIds.contains(libraryId)) continue;
            if (addedAt <= lastRun) continue;

            videos.add(new PlexVideo(
                    xmlPath.getString(prefix + "@ratingKey"),
                    xmlPath.getString(prefix + "@title"),
                    xmlPath.getString(prefix + "@year"),
                    addedAtStr,
                    libraryId,
                    xmlPath.getString(prefix + "@librarySectionTitle"),
                    xmlPath.getString(prefix + "@thumb")
            ));
        }

        return videos;
    }

    public static void mapMetadata(PlexVideo video, String xmlResponse) {
        XmlPath xmlPath = new XmlPath(xmlResponse);
        List<String> guids = xmlPath.getList("MediaContainer.Video.Guid.@id");

        for (String guid : guids) {
            if (guid.startsWith("tmdb://")) video.setTmdbId(guid.replace("tmdb://", ""));
            if (guid.startsWith("imdb://")) video.setImdbId(guid.replace("imdb://", ""));
        }
        video.setRating(xmlPath.getString("MediaContainer.Video.@audienceRating"));
        video.setSummary(xmlPath.getString("MediaContainer.Video.@summary"));
    }
}