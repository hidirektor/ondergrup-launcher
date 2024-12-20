package me.t3sl4.hydraulic.launcher.utils.Version;

import me.t3sl4.hydraulic.launcher.utils.SystemVariables;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.prefs.Preferences;

public class VersionUtility {

    private static final String CURRENT_VERSION = SystemVariables.getVersion();
    private static final String VERSION_KEY = "onderGrup_hydraulic_versionNumber";
    private static final String LAST_CHECK_TIME_KEY = "lastCheckTime";
    private static final long ONE_DAY_MILLIS = 24 * 60 * 60 * 1000;

    private static Preferences prefs = Preferences.userRoot().node(VersionUtility.class.getName());

    /**
     * Güncelleme kontrolü başlatır. 24 saat aralığını kontrol eder ve yeni bir sürüm varsa kullanıcıyı bilgilendirir.
     */
    public static void startUpdateCheck() {
        long lastCheckTime = prefs.getLong(LAST_CHECK_TIME_KEY, 0);
        long currentTime = System.currentTimeMillis();

        // Eğer son kontrol üzerinden 24 saat geçmediyse kontrol yapma
        if ((currentTime - lastCheckTime) < ONE_DAY_MILLIS) {
            System.out.println("Son kontrol üzerinden 24 saat geçmedi. Güncelleme kontrolü yapılmadı.");
            return;
        }

        String[] updateInfo = checkForUpdate();

        if (updateInfo != null) {
            System.out.println("Yeni güncelleme mevcut!");
            System.out.println("Sürüm: " + updateInfo[0]);
            System.out.println("Detaylar:\n" + updateInfo[1]);
            // Burada güncelleme işlemi başlatabilirsiniz.
        } else {
            System.out.println("Uygulamanız güncel.");
        }

        // Kontrol zamanını güncelle
        prefs.putLong(LAST_CHECK_TIME_KEY, currentTime);
    }

    /**
     * GitHub releases/latest URL'inden en son sürümün tag'ini alır.
     * @return Son sürüm mevcutsa bir String dizisi (0: versiyon, 1: detay), değilse null.
     */
    public static String[] checkForUpdate() {
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(SystemVariables.RELEASE_URL))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            int statusCode = response.statusCode();
            if (statusCode != 200) {
                System.out.println("Error: Unexpected status code " + statusCode);
                return null;
            }

            String responseBody = response.body();

            String latestVersion = extractTagFromHTML(responseBody);
            String releaseDetails = extractReleaseDetailsFromHTML(responseBody);

            if (latestVersion == null) {
                System.out.println("Error: Could not extract version tag from HTML");
                return null;
            }

            if (!CURRENT_VERSION.equals(latestVersion)) {
                System.out.println("Güncelleme mevcut: " + latestVersion);
                prefs.put(VERSION_KEY, latestVersion); // Yeni sürüm numarasını kaydet
                return new String[]{latestVersion, releaseDetails};
            }

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * HTML yanıtında "releases/tag/{version}" bilgisini bulur.
     * @param html HTML yanıtı
     * @return Tag değeri (örneğin, "v1.2.3"), ya da null
     */
    private static String extractTagFromHTML(String html) {
        String tagPrefix = "/releases/tag/";
        int tagIndex = html.indexOf(tagPrefix);
        if (tagIndex == -1) {
            return null;
        }

        int startIndex = tagIndex + tagPrefix.length();
        int endIndex = html.indexOf("\"", startIndex);
        if (endIndex == -1) {
            return null;
        }

        return html.substring(startIndex, endIndex);
    }

    /**
     * HTML yanıtından sürüm detaylarını çıkarır.
     * @param html HTML yanıtı
     * @return Sürüm detayları, ya da boş bir string
     */
    private static String extractReleaseDetailsFromHTML(String html) {
        String selectorPrefix = "data-test-selector=\"body-content\"";
        int selectorIndex = html.indexOf(selectorPrefix);

        if (selectorIndex == -1) {
            return ""; // Return empty string if the selector is not found
        }

        int divStartIndex = html.lastIndexOf("<div", selectorIndex);
        if (divStartIndex == -1) {
            return ""; // Return empty string if the <div> is not found
        }

        int divEndIndex = html.indexOf("</div>", selectorIndex);
        if (divEndIndex == -1) {
            return ""; // Return empty string if the closing </div> is not found
        }

        String detailsHtml = html.substring(divStartIndex, divEndIndex);
        StringBuilder detailsBuilder = new StringBuilder();

        String liPrefix = "<li>";
        int liIndex = detailsHtml.indexOf(liPrefix);

        while (liIndex != -1) {
            int liStartIndex = liIndex + liPrefix.length();
            int liEndIndex = detailsHtml.indexOf("</li>", liStartIndex);
            if (liEndIndex == -1) {
                break;
            }

            String detail = detailsHtml.substring(liStartIndex, liEndIndex).trim();
            detailsBuilder.append("- ").append(detail).append("\n");

            liIndex = detailsHtml.indexOf(liPrefix, liEndIndex);
        }

        return detailsBuilder.toString().trim();
    }
}
