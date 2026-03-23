public class PlexVideo {

    private String ratingKey;
    private String title;
    private String year;
    private String addedAt;
    private String librarySectionID;
    private String librarySectionTitle;
    private String imdbId;
    private String tmdbId;
    private String thumb;

    // Constructor
    public PlexVideo(String ratingKey, String title, String year, String addedAt,
                     String librarySectionID, String librarySectionTitle, String thumb) {
        this.ratingKey = ratingKey;
        this.title = title;
        this.year = year;
        this.addedAt = addedAt;
        this.librarySectionID = librarySectionID;
        this.librarySectionTitle = librarySectionTitle;
        this.thumb = thumb;
    }

    // Getters
    public String getRatingKey() { return ratingKey; }
    public String getTitle() { return title; }
    public String getYear() { return year; }
    public String getAddedAt() { return addedAt; }
    public String getLibrarySectionID() { return librarySectionID; }
    public String getLibrarySectionTitle() { return librarySectionTitle; }
    public String getImdbId() { return imdbId; }
    public String getTmdbId() { return tmdbId; }
    public String getThumb() { return thumb; }

    // Setters for IDs added later from metadata call
    public void setImdbId(String imdbId) { this.imdbId = imdbId; }
    public void setTmdbId(String tmdbId) { this.tmdbId = tmdbId; }

    @Override
    public String toString() {
        return "PlexVideo{" +
                "ratingKey='" + ratingKey + '\'' +
                ", title='" + title + '\'' +
                ", year='" + year + '\'' +
                ", imdbId='" + imdbId + '\'' +
                ", tmdbId='" + tmdbId + '\'' +
                '}';
    }
}