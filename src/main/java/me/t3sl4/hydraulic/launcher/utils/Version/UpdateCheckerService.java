package me.t3sl4.hydraulic.launcher.utils.Version;

import me.t3sl4.hydraulic.launcher.utils.GeneralUtil;
import me.t3sl4.hydraulic.launcher.utils.SystemVariables;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.prefs.Preferences;

public class UpdateCheckerService {

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private static final String PREF_NODE = "onderGrupUpdater";
    private static final String HYDRAULIC_VERSION_KEY = "hydraulic_version";
    private final Preferences prefs = Preferences.userRoot().node(PREF_NODE);

    /**
     * Servisi başlatır. 24 saatte bir güncelleme kontrolü yapar.
     */
    public void start() {
        scheduler.scheduleAtFixedRate(this::checkForUpdates, 0, 24, TimeUnit.HOURS);
    }

    /**
     * Hydraulic sürümünü kontrol eder ve güncelleme gerekiyorsa indirir.
     */
    private void checkForUpdates() {
        System.out.println("Hydraulic sürüm kontrolü başlıyor...");

        // Preferences'taki kaydedilmiş sürümü oku
        String savedVersion = prefs.get(HYDRAULIC_VERSION_KEY, null);

        // GitHub'dan en son sürümü al
        String latestVersion = getLatestVersionFromGitHub();

        if (latestVersion == null) {
            System.out.println("GitHub'dan sürüm bilgisi alınamadı.");
            return;
        }

        // Sürümler farklı mı?
        if (savedVersion == null || !savedVersion.equals(latestVersion)) {
            System.out.println("Yeni bir sürüm bulundu: " + latestVersion);
            handleDownload();

            // Yeni sürümü preferences'a kaydet
            prefs.put(HYDRAULIC_VERSION_KEY, latestVersion);
            System.out.println("Hydraulic sürümü güncellendi: " + latestVersion);
        } else {
            System.out.println("HydraulicTool zaten güncel: " + savedVersion);
        }
    }

    /**
     * GitHub'dan en son sürüm bilgilerini alır.
     * @return En son sürüm numarası veya null.
     */
    private String getLatestVersionFromGitHub() {
        try {
            String releaseUrl = SystemVariables.RELEASE_URL; // GitHub API URL'si
            HttpResponse<String> response = httpGet(releaseUrl);

            if (response.statusCode() != 200) {
                System.out.println("GitHub API çağrısı başarısız oldu: " + response.statusCode());
                return null;
            }

            // HTML içeriğinden sürüm bilgisini çıkar
            return extractTagFromHTML(response.body());
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * HTTP GET isteği gönderir ve yanıtı döndürür.
     *
     * @param url İstek yapılacak URL.
     * @return HttpResponse<String> formatında yanıt.
     * @throws IOException Eğer bir ağ hatası olursa.
     * @throws InterruptedException Eğer işlem kesilirse.
     */
    public static HttpResponse<String> httpGet(String url) throws IOException, InterruptedException {
        // HTTP istemcisi oluştur
        HttpClient client = HttpClient.newHttpClient();

        // HTTP GET isteği oluştur
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        // İsteği gönder ve yanıtı döndür
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    /**
     * Yeni HydraulicTool sürümünü indirir.
     */
    private void handleDownload() {
        File selectedDirectory = new File(SystemVariables.downloadPath);

        if (!selectedDirectory.exists()) {
            boolean created = selectedDirectory.mkdirs();
            if (!created) {
                System.out.println("İndirme dizini oluşturulamadı: " + SystemVariables.downloadPath);
                return;
            }
        }

        // Eski dosyaları temizle
        File[] matchingFiles = selectedDirectory.listFiles(file -> file.getName().startsWith("windows_Hydraulic"));

        if (matchingFiles != null && matchingFiles.length > 0) {
            for (File file : matchingFiles) {
                if (file.delete()) {
                    System.out.println("Eski dosya silindi: " + file.getAbsolutePath());
                } else {
                    System.err.println("Dosya silinemedi: " + file.getAbsolutePath());
                    return;
                }
            }
            System.out.println("Eski 'windows_Hydraulic' dosyaları başarıyla temizlendi.");
        }

        // Yeni dosyayı indir
        try {
            GeneralUtil.downloadLatestVersion(selectedDirectory);
            System.out.println("Yeni sürüm başarıyla indirildi.");
        } catch (IOException e) {
            System.err.println("İndirme sırasında bir hata oluştu: " + e.getMessage());
        }
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

    /**
     * Servisi durdurur.
     */
    public void stop() {
        scheduler.shutdownNow();
        System.out.println("UpdateCheckerService durduruldu.");
    }
}