package me.t3sl4.hydraulic.launcher.utils.Version;

import me.t3sl4.hydraulic.launcher.utils.GeneralUtil;
import me.t3sl4.hydraulic.launcher.utils.SystemVariables;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class UpdateCheckerService {

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    /**
     * Servisi başlatır. 24 saatte bir güncelleme kontrolü yapar.
     */
    public void start() {
        scheduler.scheduleAtFixedRate(this::checkForUpdates, 0, 24, TimeUnit.HOURS);
    }

    /**
     * Güncelleme kontrolü yapılır ve yeni sürüm bulunduysa otomatik indirme başlatılır.
     */
    private void checkForUpdates() {
        System.out.println("Güncelleme kontrolü başlıyor...");

        String[] updateInfo = VersionUtility.checkForUpdate();
        if (updateInfo != null) {
            System.out.println("Yeni bir güncelleme bulundu: " + updateInfo[0]);
            handleDownload();
        } else {
            System.out.println("Uygulamanız güncel.");
        }
    }

    /**
     * Güncelleme dosyasını indirir.
     */
    public void handleDownload() {
        File selectedDirectory = new File(SystemVariables.downloadPath);

        if (!selectedDirectory.exists()) {
            boolean created = selectedDirectory.mkdirs();
            if (!created) {
                System.out.println("İndirme dizini oluşturulamadı: " + SystemVariables.downloadPath);
                return;
            }
        }

        File[] matchingFiles = selectedDirectory.listFiles(file -> file.getName().startsWith("windows_Hydraulic"));

        if (matchingFiles != null && matchingFiles.length > 0) {
            for (File file : matchingFiles) {
                if (file.delete()) {
                    System.out.println("Dosya silindi: " + file.getAbsolutePath());
                } else {
                    System.err.println("Dosya silinemedi: " + file.getAbsolutePath());
                    return;
                }
            }
            System.out.println("Tüm 'windows_Hydraulic' dosyaları başarıyla silindi.");
        }

        // Dosya indirme işlemi
        try {
            GeneralUtil.downloadLatestVersion(selectedDirectory);
            System.out.println("İndirme tamamlandı: " + selectedDirectory.getAbsolutePath());
        } catch (IOException e) {
            System.err.println("İndirme sırasında bir hata oluştu: " + e.getMessage());
        }
    }

    /**
     * Servisi durdurur.
     */
    public void stop() {
        scheduler.shutdownNow();
        System.out.println("UpdateCheckerService durduruldu.");
    }
}

