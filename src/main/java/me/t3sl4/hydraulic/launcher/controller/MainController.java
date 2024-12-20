package me.t3sl4.hydraulic.launcher.controller;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.Pane;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.ImageView;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javafx.util.Duration;
import me.t3sl4.hydraulic.launcher.utils.GeneralUtil;
import me.t3sl4.hydraulic.launcher.utils.SystemVariables;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class MainController implements Initializable {

    private Stage currentStage;

    @FXML
    private Button accountsButton;

    @FXML
    private ImageView closeIcon, minimizeIcon, expandIcon;

    @FXML
    private Button mainProgramFolder, localHydraulicDataFolder, userDataFolder, partListFolder;

    @FXML
    private Pane changeLogPane, updatePane;

    @FXML
    private WebView changeLogWebView;

    @FXML
    private ImageView progressIndicator;

    @FXML
    private Label updateStatusLabel;

    //Ekran büyütüp küçültme
    private boolean stageMaximized = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        /*
        Burası sonradan tamamlanacak. Verilerin çekilmesi güncelleme kontrolü bildirim vb.
         */

        Platform.runLater(() -> {
            currentStage = (Stage) accountsButton.getScene().getWindow();

            // Program büyültme, küçültme ve kapatma için hover efekti
            addHoverEffect(closeIcon, minimizeIcon, expandIcon);

            changeLogWebView.getEngine().load("https://github.com/hidirektor/ondergrup-hydraulic-tool/releases");
        });
    }

    @FXML
    public void closeProgram() {
        String osName = System.getProperty("os.name").toLowerCase();

        if (osName.contains("win")) {
            GeneralUtil.minimizeToSystemTray(currentStage);
        } else {
            currentStage.setIconified(true);
        }
    }

    @FXML
    public void minimizeProgram() {
        currentStage.setIconified(true);
    }

    @FXML
    public void expandProgram() {
        if(stageMaximized) {
            currentStage.setMaximized(false);
            stageMaximized = false;
        } else {
            currentStage.setMaximized(true);
            stageMaximized = true;
        }
    }

    @FXML
    public void runHydraulic() {
        String hydraulicPath = SystemVariables.hydraulicPath;
        File hydraulicFile = new File(hydraulicPath);

        if (!hydraulicFile.exists()) {
            System.err.println("Hydraulic file not found: " + hydraulicPath);
            /*
            Önce hata mesajı ver ardından otomatik indir
             */
            return;
        }

        // Dosyayı çalıştır
        try {
            if (hydraulicPath.endsWith(".exe")) {
                // Windows için çalıştırma
                new ProcessBuilder("cmd.exe", "/c", hydraulicPath).start();
            } else if (hydraulicPath.endsWith(".jar")) {
                // Unix için çalıştırma
                new ProcessBuilder("java", "-jar", hydraulicPath).start();
            } else {
                System.err.println("Unsupported file type for: " + hydraulicPath);
            }
            GeneralUtil.minimizeToSystemTray(currentStage);
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Failed to execute hydraulic file: " + hydraulicPath);
        }
    }

    @FXML
    public void checkForUpdates() {
        paneSwitch(5);
        handleDownload();
    }

    @FXML
    public void openChangelog() {
        paneSwitch(6);
    }

    @FXML
    public void openFolder(ActionEvent actionEvent) {
        if(actionEvent.getSource() == mainProgramFolder) {
            GeneralUtil.openFolder(SystemVariables.mainPath);
        } else if(actionEvent.getSource() == localHydraulicDataFolder) {
            GeneralUtil.openFolder(SystemVariables.localHydraulicDataPath);
        } else if(actionEvent.getSource() == userDataFolder) {
            GeneralUtil.openFolder(SystemVariables.userDataPath);
        } else if(actionEvent.getSource() == partListFolder) {
            GeneralUtil.openFolder(SystemVariables.partListDataPath);
        } else {
            System.err.println("Error: Unknown source triggered openFolder: " + actionEvent.getSource());
        }
    }

    @FXML
    public void openPortfolio() {
        GeneralUtil.openURL(SystemVariables.PORTFOLIO_URL);
    }

    @FXML
    public void handleDownload() {
        // Otomatik indirme yolu
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

        // ProgressIndicator ekle
        progressIndicator.setVisible(true);
        progressIndicator.setStyle("-fx-pref-width: 100; -fx-pref-height: 100;");
        ColorAdjust colorAdjust = new ColorAdjust();
        colorAdjust.setBrightness(1.0);
        progressIndicator.setEffect(colorAdjust);

        Task<Void> downloadTask = new Task<>() {
            @Override
            protected Void call() {
                try {
                    GeneralUtil.downloadLatestVersion(selectedDirectory);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void succeeded() {
                super.succeeded();
                Platform.runLater(() -> {
                    updateStatusLabel.setText("İndirme tamamlandı!");
                    progressIndicator.setVisible(false);
                });

                new Timeline(new KeyFrame(Duration.seconds(2), e -> paneSwitch(1))).play();
            }

            @Override
            protected void failed() {
                super.failed();
                Platform.runLater(() -> {
                    updateStatusLabel.setText("İndirme sırasında bir hata oluştu.");
                    progressIndicator.setVisible(false);
                });

                new Timeline(new KeyFrame(Duration.seconds(2), e -> paneSwitch(1))).play();
            }

            @Override
            protected void cancelled() {
                super.cancelled();
                Platform.runLater(() -> {
                    updateStatusLabel.setText("İndirme iptal edildi.");
                    progressIndicator.setVisible(false);
                });

                new Timeline(new KeyFrame(Duration.seconds(2), e -> paneSwitch(1))).play();
            }
        };

        Thread downloadThread = new Thread(downloadTask);
        downloadThread.setDaemon(true);
        downloadThread.start();
    }

    private void paneSwitch(int paneType) {
        updatePane.setVisible(false);
        updatePane.toBack();
        changeLogPane.setVisible(false);
        changeLogPane.toBack();
        switch (paneType) {
            case 1: //Aktif Lisanslar & Hesaplar
                break;
            case 2: //Favori Hesaplar
                break;
            case 3: //Silinen Hesaplar
                break;
            case 4: //Profil
                break;
            case 5: //Check Updates
                updatePane.setVisible(true);
                updatePane.toFront();
                break;
            case 6: //ChangeLog
                changeLogPane.setVisible(true);
                changeLogPane.toFront();
                break;
        }
    }

    private void addHoverEffect(ImageView... imageViews) {
        ColorAdjust darkenEffect = new ColorAdjust();
        darkenEffect.setBrightness(-0.5); // Karartma seviyesi

        for (ImageView imageView : imageViews) {
            imageView.setOnMouseEntered(event -> imageView.setEffect(darkenEffect));
            imageView.setOnMouseExited(event -> imageView.setEffect(null));
        }
    }
}