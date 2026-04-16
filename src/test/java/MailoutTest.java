import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.testng.annotations.Test;

import java.util.*;

public class MailoutTest extends BaseTest {

    @Test(dependsOnGroups = {"daily"}, groups = {"mailout"})
    public void testSendMailout() throws MessagingException {

        List<PlexVideo> additions = DailySummaryTest.newAdditions.stream()
                .filter(v -> v.getTmdbId() != null && v.getImdbId() != null)
                .toList();

        if (additions.isEmpty()) {
            System.out.println("No new additions — skipping mailout.");
            return;
        }

        String username   = System.getProperty("gmail.username");
        String password   = System.getProperty("gmail.password");
        String recipients = System.getProperty("mail.recipients");

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });

        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(username));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(username));
        message.setRecipients(Message.RecipientType.BCC, InternetAddress.parse(recipients));
        message.setSubject("Plex — " + additions.size() + " new addition" + (additions.size() == 1 ? "" : "s"));
        message.setContent(buildHtml(additions), "text/html; charset=utf-8");

        Transport.send(message);
        System.out.println("Mailout sent to: " + recipients);
    }

    private String buildHtml(List<PlexVideo> additions) {

        int SUMMARY_LIMIT = 2000;
        String plexLogo    = "https://raw.githubusercontent.com/alexrossqa/plex-rest-assured/main/src/test/resources/plex-logo.jpg";
        String jenkinsLogo = "https://raw.githubusercontent.com/alexrossqa/plex-rest-assured/main/src/test/resources/jenkins-logo.png";
        String imdbLogo    = "https://raw.githubusercontent.com/alexrossqa/plex-rest-assured/main/src/test/resources/Imdb-logo.png";
        String newWaveLogo = "https://raw.githubusercontent.com/alexrossqa/plex-rest-assured/main/src/test/resources/new-wave-logo.png";
        String noPoster = "https://raw.githubusercontent.com/alexrossqa/plex-rest-assured/main/src/test/resources/no-poster.png";

        // Group by library
        Map<String, List<PlexVideo>> byLibrary = new TreeMap<>();
        for (PlexVideo video : additions) {
            String lib = video.getLibrarySectionTitle() != null ? video.getLibrarySectionTitle() : "Other";
            byLibrary.computeIfAbsent(lib, k -> new ArrayList<>()).add(video);
        }

        StringBuilder sb = new StringBuilder();

        sb.append("<!DOCTYPE html>")
                .append("<html><head><meta charset='utf-8'>")
                .append("<meta name='viewport' content='width=device-width, initial-scale=1.0'>")
                .append("</head>")
                .append("<body style='margin:0;padding:0;background:#181818;font-family:Arial,sans-serif;'>")

                // Outer wrapper
                .append("<table width='100%' cellpadding='0' cellspacing='0' border='0' style='background:#181818;'>")
                .append("<tr><td align='center' style='padding:20px;'>")

                // Inner container
                .append("<table width='620' cellpadding='0' cellspacing='0' border='0'>")

                // Header
                .append("<tr><td style='padding:20px 0 30px 0;'>")
                .append("<table width='100%' cellpadding='0' cellspacing='0' border='0'>")
                .append("<tr>")
                .append("<td valign='middle'>")
                .append("<img src='").append(plexLogo).append("' alt='Plex' height='120' style='display:block;'/>")
                .append("</td>")
                .append("<td align='right' valign='middle'>")
                .append("<img src='").append(newWaveLogo).append("' alt='Jenkins' height='70' style='display:block;'/>")
                .append("</td>")
                .append("</tr>")
                // Second row - title
                .append("<tr>")
                .append("<td colspan='2' style='padding-top:15px;color:#e0a800;font-size:22px;font-weight:bold;'>")
                .append("What's New on Plex?")
                .append("</td>")
                .append("</tr>")
                .append("</table>")
                .append("</td></tr>");


        // Libraries
        for (Map.Entry<String, List<PlexVideo>> entry : byLibrary.entrySet()) {
            String library = entry.getKey().replace(" - AD", "");
            List<PlexVideo> films = entry.getValue();

            // Library header
            sb.append("<tr><td style='padding:10px 0 8px 0;border-bottom:2px solid #e0a800;'>")
                    .append("<span style='color:#e0a800;font-size:16px;font-weight:bold;text-transform:uppercase;letter-spacing:2px;'>")
                    .append(library)
                    .append("</span></td></tr>");

            // Films
            for (PlexVideo video : films) {

                String posterUrl = video.getPosterPath() != null
                        ? "https://image.tmdb.org/t/p/w200" + video.getPosterPath()
                        : noPoster;
//                String imdbUrl   = video.getImdbId() != null ? "https://www.imdb.com/title/" + video.getImdbId() : null;
                String letterboxdUrl = video.getImdbId() != null ? "https://letterboxd.com/imdb/" + video.getImdbId() : null;
                String summary   = video.getSummary() != null ? video.getSummary() : "";
                if (summary.length() > SUMMARY_LIMIT) summary = summary.substring(0, SUMMARY_LIMIT) + "...";
                String rating = video.getRating() != null
                        ? "<img src='" + imdbLogo + "' height='14' style='display:inline;vertical-align:middle;'/> " + video.getRating()
                        : "No rating";

                sb.append("<tr><td style='padding:15px 0;border-bottom:1px solid #2a2a4a;'>")
                        .append("<table width='100%' cellpadding='0' cellspacing='0' border='0'>")
                        .append("<tr>")

                        // Left — text
                        .append("<td valign='top' style='padding-right:15px;'>")
                        .append("<p style='margin:0 0 4px 0;font-size:17px;font-weight:bold;color:#ffffff;'>")
                        .append(letterboxdUrl != null ? "<a href='" + letterboxdUrl + "' style='color:#e0a800;text-decoration:none;'>" : "")
                        .append(video.getTitle()).append(" (").append(video.getYear()).append(")")
                        .append(letterboxdUrl != null ? "</a>" : "")
                        .append("</p>")
                        .append("<p style='margin:0 0 8px 0;font-size:12px;color:#aaaaaa;'>").append(rating).append("</p>")
                        .append("<p style='margin:0;font-size:13px;color:#cccccc;line-height:1.6;'>").append(summary).append("</p>")
                        .append("</td>")

                        // Right — poster
                        .append("<td valign='top' width='100' style='min-width:100px;'>")
                        .append("<img src='").append(posterUrl).append("' width='100' style='display:block;border-radius:6px;' alt='").append(video.getTitle()).append("'/>")
                        .append("</td>")

                        .append("</tr>")
                        .append("</table>")
                        .append("</td></tr>");
            }
        }

        // Footer
        sb.append("<tr><td style='padding:20px 0;text-align:center;color:#555555;font-size:11px;'>")
                .append("Generated by New Wave IT Services &nbsp;|&nbsp; ").append(new java.util.Date())
                .append("</td></tr>");

        sb.append("</table>") // inner container
                .append("</td></tr>")
                .append("</table>") // outer wrapper
                .append("</body></html>");

        return sb.toString();
    }
}