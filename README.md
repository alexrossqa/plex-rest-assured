# plex-rest-assured

**What does it do?**

A suite of automated API tests that interrogates a Plex Media Server for new additions, enriches each record using both the Plex and TMDB APIs, and sends a formatted HTML mailout to Plex users summarising what's new. A server audit test is also included to assist with library housekeeping.

**How does it work?**

The tests are written using the REST Assured Java library with TestNG as the test framework.

The Plex _recentlyAdded_ endpoint is called and a list of titles is built from the results, filtered by a _lastRun_ Unix timestamp that defines what counts as a new addition. Each title is then enriched individually using the Plex _metadata_ endpoint, which provides the IMDB and TMDB IDs, audience rating, and summary text. The TMDB ID is then used to validate each record against the TMDB API and retrieve the poster image URL.

Once the list is fully built, a formatted HTML email is constructed and sent to all recipients, grouping titles by their Plex library section.

The suite is structured and ordered using _testng.xml_, with _dependsOnMethods_ used within _DailySummaryTest_ to enforce the correct execution sequence — each test in the chain depends on the previous one having passed before it runs.

**Classes**

**BaseTest** contains a static initializer block that reads all system properties used throughout the suite — Plex URL, Plex token, TMDB credentials, Gmail credentials, _lastRun_ timestamp, and mail recipients. All test classes extend _BaseTest_ to inherit these values.

**DailySummaryTest** contains the three chained tests that build the mailout data:
- _testGetNewAdditions_ — calls the _recentlyAdded_ endpoint and builds the initial list of new titles filtered by lastRun and excluded library IDs
- _testGetMetadataForNewAdditions_ — loops through the list and enriches each record with IMDB/TMDB IDs, rating, and summary via the Plex _metadata_ endpoint
- _testValidateTmdbRecords_ — validates each matched record against the TMDB API and retrieves the poster path

_newAdditions_ is declared as a static field so it can be accessed by MailoutTest after the daily tests have completed.

**PlexVideo** is the data model for a single title and contains:
- A constructor populated from the recentlyAdded response — title, year, library section, added date, and thumb
- Getters for all fields
- Setters for fields populated in the second pass — IMDB ID, TMDB ID, rating, summary, and poster path

**PlexResponseMapper** contains two static methods for extracting data from API responses:
- _mapRecentlyAdded_ - parses the XML response from _testGetNewAdditions_, filters out excluded libraries and titles missing an added date, applies the _lastRun_ filter, and returns a populated list of _PlexVideo_ objects
- _mapMetadata_ - parses the XML response from _testGetMetadataForNewAdditions_ and updates each _PlexVideo_ with IMDB/TMDB IDs, audience rating, and summary

**MailoutTest** receives the _newAdditions_ list from _DailySummaryTest_ and filters it to matched titles only (those with both IMDB and TMDB IDs). Builds a dark-themed HTML email titled "What's New on Plex?" grouped by Plex library section. Each entry contains the title and year, IMDB rating (linking to the film's IMDB page), synopsis, and TMDB poster image. The title is hyperlinked to the corresponding Letterboxd entry via the IMDB ID. Sends via Gmail SMTP using the JavaMail library.

**PlexServerTest** contains smoke and regression tests verifying that the Plex server and its libraries are accessible and returning expected responses.

**PlexLibraryAuditTest** calls the Plex sections endpoint and reports any unmatched items in the library (titles that Plex has been unable to match to a metadata agent.) These require manual resolution.

**Relationship to the wider project**
| Repo | Purpose |
|---|---|
| `plex-api-testing` | Postman exploratory collection |
| `plex-rest-assured` | Automated REST Assured test suite (this repo) |
| `plex-sync-scripts` | PowerShell sync scripts |
| `plex-pipeline` | Jenkins CI/CD pipeline |
