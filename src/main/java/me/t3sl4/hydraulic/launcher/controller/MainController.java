package me.t3sl4.hydraulic.launcher.controller;

import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.CacheHint;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javafx.util.Duration;
import me.t3sl4.hydraulic.launcher.Launcher;
import me.t3sl4.hydraulic.launcher.utils.GeneralUtil;
import me.t3sl4.hydraulic.launcher.utils.HTTP.HttpUtil;
import me.t3sl4.hydraulic.launcher.utils.Model.User;
import me.t3sl4.hydraulic.launcher.utils.SystemVariables;
import me.t3sl4.util.file.FileUtil;
import me.t3sl4.util.os.OSUtil;
import me.t3sl4.util.os.desktop.DesktopUtil;
import me.t3sl4.util.version.DownloadProgressListener;
import me.t3sl4.util.version.VersionUtil;

import javax.swing.filechooser.FileSystemView;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;

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
    private ImageView progressIndicator, progressIndicatorDownload;

    @FXML
    private Label updateStatusLabel, updateStatusLabelDownload;
    
    @FXML
    private VBox savedUsersVBox;

    @FXML
    private TextField accountSearchBar;

    @FXML
    private Button accountLoginButton;

    @FXML
    private TextField userNameTextField, licenseKeyTextField;

    @FXML
    private PasswordField passwordTextField;

    @FXML
    private TextField visiblePasswordTextField;

    @FXML
    private Pane createAccountPane, customDataPane;

    @FXML
    private Pane settingsPane, downloadPane;

    @FXML
    private Label requestResponse, accountAddUserNameLabel, accountAddEmailLabel, loginURL;

    @FXML
    private ImageView profilePhotoImageView, showHidePasswordImageView, customShowHidePass;

    @FXML
    private ImageView customImageView, customFavouriteButton;

    @FXML
    private Label customNameSurname, customUserName, accessTokenLabel, passwordLabel, licenseLabel;

    @FXML
    private ImageView copyLicenseButton, copyPasswordButton;

    @FXML
    private CheckBox onderLauncherShortcutCheck, onderLauncherAutoStartCheck, hydraulicToolShortcutCheck, hydraulicToolAutoStartCheck;

    @FXML
    private ProgressBar downloadProgress;

    @FXML
    private ImageView changeLogLauncherImageView, changeLogHydraulicImageView;

    @FXML
    private ImageView serverStatusIcon;

    //Ekran büyütüp küçültme
    private boolean stageMaximized = false;
    private boolean isHidden = true;
    private String girilenSifre = "";

    //Kaydedilen user Accountslar
    private List<User> savedUserAccounts = new ArrayList<>();

    //Tablodan seçilen kullanıcı
    private User selectedUser;

    private Image hydraulicWebImage = new Image(Objects.requireNonNull(Launcher.class.getResourceAsStream("/assets/images/logo-hydraulic.png")), 16, 16, true, true);
    private Image launcherWebImage = new Image(Objects.requireNonNull(Launcher.class.getResourceAsStream("/assets/images/logo.png")), 16, 16, true, true);

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Platform.runLater(() -> {
            currentStage = (Stage) accountsButton.getScene().getWindow();

            // Program büyültme, küçültme ve kapatma için hover efekti
            addHoverEffect(closeIcon, minimizeIcon, expandIcon);

            checkServerStatus();

            changeLogLauncherImageView.setImage(launcherWebImage);
            changeLogHydraulicImageView.setImage(hydraulicWebImage);

            changeLogWebView.setPrefSize(635.0, 622.0);
            changeLogWebView.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
            changeLogWebView.setContextMenuEnabled(false);

            Rectangle clip = new Rectangle(635.0, 622.0);
            clip.setArcWidth(32.0);
            clip.setArcHeight(32.0);
            changeLogWebView.setClip(clip);

            changeLogWebView.getEngine().setUserStyleSheetLocation(Launcher.class.getResource("styling/webview.css").toExternalForm());

            changeLogWebView.getEngine().load("https://github.com/hidirektor/ondergrup-hydraulic-tool/releases");

            changeLogWebView.addEventFilter(ScrollEvent.SCROLL, event -> {
                changeLogWebView.getEngine().executeScript("window.scrollBy(0, " + event.getDeltaY() + ")");
                event.consume();
            });
        });

        GeneralUtil.readUserAccountData(savedUserAccounts);
        populateUIWithCachedData(savedUserAccounts, null);
        setupSearchBar(accountSearchBar, savedUserAccounts);
    }

    @FXML
    public void changeLogHydraulic() {
        changeLogWebView.getEngine().load("https://github.com/hidirektor/ondergrup-hydraulic-tool/releases");
    }

    @FXML
    public void changeLogLauncher() {
        changeLogWebView.getEngine().load("https://github.com/hidirektor/ondergrup-launcher/releases");
    }

    @FXML
    public void changeLogUpdater() {
        changeLogWebView.getEngine().load("https://github.com/hidirektor/ondergrup-updater-service/releases");
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

        String localVersion = VersionUtil.getLocalVersion(SystemVariables.PREF_NODE_NAME, SystemVariables.PREF_HYDRAULIC_KEY);
        String latestVersion = VersionUtil.getLatestVersion(SystemVariables.REPO_OWNER, SystemVariables.HYDRAULIC_REPO_NAME);

        File hydraulicFile = new File(hydraulicPath);

        if (!hydraulicFile.exists() || localVersion != latestVersion) {
            System.err.println("Hydraulic file not found: " + hydraulicPath);
            handleDownload();
            return;
        }

        try {
            String os = System.getProperty("os.name").toLowerCase();

            if (os.contains("win") && hydraulicPath.endsWith(".exe")) {
                // Windows için çalıştırma
                new ProcessBuilder("cmd.exe", "/c", hydraulicPath).start();
            } else if (os.contains("nix") || os.contains("nux")) {
                // Unix/Linux için çalıştırma
                if (hydraulicPath.endsWith(".jar")) {
                    new ProcessBuilder("java", "-jar", hydraulicPath).start();
                } else {
                    System.err.println("Unsupported file type for Unix/Linux: " + hydraulicPath);
                }
            } else if (os.contains("mac")) {
                // MacOS için çalıştırma
                if (hydraulicPath.endsWith(".jar")) {
                    new ProcessBuilder("java", "-jar", hydraulicPath).start();
                } else {
                    System.err.println("Unsupported file type for MacOS: " + hydraulicPath);
                }
            } else {
                System.err.println("Unsupported OS or file type for: " + hydraulicPath);
            }

            GeneralUtil.minimizeToSystemTray(currentStage);

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Failed to execute hydraulic file: " + hydraulicPath);
        }
    }

    @FXML
    public void launcherSettings() {
        paneSwitch(7);

        try {
            // Masaüstü ve Başlangıç yolları
            File home = FileSystemView.getFileSystemView().getHomeDirectory();
            String desktopPath = home.getAbsolutePath();
            String startupPath = System.getProperty("user.home") + "\\AppData\\Roaming\\Microsoft\\Windows\\Start Menu\\Programs\\Startup";

            // Dosya kontrolleri
            onderLauncherShortcutCheck.setSelected(new File(desktopPath + "\\windows_Launcher.exe.lnk").exists());
            onderLauncherAutoStartCheck.setSelected(new File(startupPath + "\\windows_Launcher.exe.lnk").exists());
            hydraulicToolShortcutCheck.setSelected(new File(desktopPath + "\\windows_Hydraulic.exe.lnk").exists());
            hydraulicToolAutoStartCheck.setSelected(new File(startupPath + "\\windows_Hydraulic.exe.lnk").exists());

            // CheckBox listener ekle
            setupListeners();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setupListeners() {
        onderLauncherShortcutCheck.selectedProperty().addListener((obs, oldVal, newVal) -> {
            try {
                String fileName = "windows_Updater";
                String iconName = "windows_Launcher";
                String iconPath = SystemVariables.mainPath + "\\" + iconName + ".exe";
                String targetPath = SystemVariables.mainPath + "\\" + fileName + ".exe";
                if (newVal) {
                    DesktopUtil.createDesktopShortcut(fileName + ".exe", targetPath, iconPath, SystemVariables.mainPath);
                } else {
                    FileUtil.deleteFile(FileSystemView.getFileSystemView().getHomeDirectory().getAbsolutePath() + "\\" + fileName + ".exe" + ".lnk");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        onderLauncherAutoStartCheck.selectedProperty().addListener((obs, oldVal, newVal) -> {
            try {
                String fileName = "windows_Updater";
                String iconName = "windows_Launcher";
                String iconPath = SystemVariables.mainPath + "\\" + iconName + ".exe";
                String targetPath = SystemVariables.mainPath + "\\" + fileName + ".exe";
                if (newVal) {
                    DesktopUtil.addToStartup(fileName + ".exe", targetPath, iconPath, SystemVariables.mainPath);
                } else {
                    FileUtil.deleteFile(System.getProperty("user.home") + "\\AppData\\Roaming\\Microsoft\\Windows\\Start Menu\\Programs\\Startup\\" + fileName + ".exe" + ".lnk");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        hydraulicToolShortcutCheck.selectedProperty().addListener((obs, oldVal, newVal) -> {
            try {
                String fileName = "windows_Hydraulic";
                String iconPath = SystemVariables.mainPath + "\\" + fileName + ".exe";
                String targetPath = SystemVariables.mainPath + "\\" + fileName + ".exe";
                if (newVal) {
                    DesktopUtil.createDesktopShortcut(fileName + ".exe", targetPath, iconPath, SystemVariables.mainPath);
                } else {
                    FileUtil.deleteFile(FileSystemView.getFileSystemView().getHomeDirectory().getAbsolutePath() + "\\" + fileName + ".exe" + ".lnk");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        hydraulicToolAutoStartCheck.selectedProperty().addListener((obs, oldVal, newVal) -> {
            try {
                String fileName = "windows_Hydraulic";
                String iconPath = SystemVariables.mainPath + "\\" + fileName + ".exe";
                String targetPath = SystemVariables.mainPath + "\\" + fileName + ".exe";
                if (newVal) {
                    DesktopUtil.addToStartup(fileName + ".exe", targetPath, iconPath, SystemVariables.mainPath);
                } else {
                    FileUtil.deleteFile(System.getProperty("user.home") + "\\AppData\\Roaming\\Microsoft\\Windows\\Start Menu\\Programs\\Startup\\" + fileName + ".exe" + ".lnk");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    @FXML
    public void checkForUpdates() {
        paneSwitch(5);

        progressIndicator.setVisible(true);
        progressIndicator.setStyle("-fx-pref-width: 100; -fx-pref-height: 100;");
        ColorAdjust colorAdjust = new ColorAdjust();
        colorAdjust.setBrightness(1.0);
        progressIndicator.setEffect(colorAdjust);
        progressIndicator.setCache(true);
        progressIndicator.setCacheHint(CacheHint.SPEED);

        Task<Void> updateCheckTask = new Task<>() {
            @Override
            protected Void call() {
                try {
                    String currentVersion = SystemVariables.getVersion();
                    String latestVersion = VersionUtil.getLatestVersion(SystemVariables.REPO_OWNER, SystemVariables.HYDRAULIC_REPO_NAME);

                    if (latestVersion != null && !latestVersion.equals(currentVersion)) {
                        Platform.runLater(() -> updateStatusLabel.setText("Yeni sürüm mevcut: " + latestVersion));
                    } else {
                        Platform.runLater(() -> updateStatusLabel.setText("Güncel sürümdesiniz."));
                    }

                } catch (Exception e) {
                    Platform.runLater(() -> updateStatusLabel.setText("Güncelleme kontrolü sırasında hata oluştu."));
                    e.printStackTrace();
                }
                return null;
            }
        };

        Thread updateCheckThread = new Thread(updateCheckTask);
        updateCheckThread.setDaemon(true);
        updateCheckThread.start();
    }

    @FXML
    public void openChangelog() {
        paneSwitch(6);
    }

    @FXML
    public void addAccount() {
        createAccountPane.setVisible(true);
        createAccountPane.toFront();
        userNameTextField.clear();
        passwordTextField.clear();
        visiblePasswordTextField.clear();
        licenseKeyTextField.clear();

        passwordTextField.setPrefWidth(290.0);
        visiblePasswordTextField.setPrefWidth(0);

        accountLoginButton.setOnAction(event -> {
            String username = userNameTextField.getText();
            String password = passwordTextField.getText();
            String licenseKey = licenseKeyTextField.getText();

            if (username.isEmpty() || password.isEmpty() || licenseKey.isEmpty()) {
                requestResponse.setText("Username, password and licenseKey cannot be empty.");
                return;
            }

            String loginUrl = SystemVariables.BASE_URL + SystemVariables.loginURLPrefix;
            String jsonLoginBody = "{\"userName\": \"" + username + "\", \"password\": \"" + password + "\"}";

            try {
                HttpUtil.loginReq(loginUrl, jsonLoginBody, username, password, licenseKey, requestResponse, createAccountPane, profilePhotoImageView, accountAddUserNameLabel, accountAddEmailLabel);
            } catch (IOException e) {
                Platform.runLater(() -> requestResponse.setText("Error during login."));
            }
        });

        passwordTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            girilenSifre = newValue;
        });

        showHidePasswordImageView.setOnMouseClicked(event -> togglePasswordVisibility());
    }

    @FXML
    public void checkServerStatus() {
        Image ICON_SERVER_UP = new Image(Objects.requireNonNull(Launcher.class.getResourceAsStream("/assets/icons/icon_server_up.png")));
        Image ICON_SERVER_DOWN = new Image(Objects.requireNonNull(Launcher.class.getResourceAsStream("/assets/icons/icon_server_down.png")));

        new Thread(() -> {
            boolean isServerUp = GeneralUtil.pingServer(SystemVariables.DOCS_URL);
            Platform.runLater(() -> {
                if (isServerUp) {
                    serverStatusIcon.setImage(ICON_SERVER_UP);
                } else {
                    serverStatusIcon.setImage(ICON_SERVER_DOWN);
                }
            });
        }).start();
    }

    @FXML
    public void openFolder(ActionEvent actionEvent) throws IOException {
        if(actionEvent.getSource() == mainProgramFolder) {
            DesktopUtil.startExternalApplicationAsync(SystemVariables.mainPath);
        } else if(actionEvent.getSource() == localHydraulicDataFolder) {
            DesktopUtil.startExternalApplicationAsync(SystemVariables.localHydraulicDataPath);
        } else if(actionEvent.getSource() == userDataFolder) {
            DesktopUtil.startExternalApplicationAsync(SystemVariables.userDataPath);
        } else if(actionEvent.getSource() == partListFolder) {
            DesktopUtil.startExternalApplicationAsync(SystemVariables.partListDataPath);
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
        paneSwitch(4);

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
        progressIndicatorDownload.setVisible(true);
        progressIndicatorDownload.setStyle("-fx-pref-width: 100; -fx-pref-height: 100;");
        ColorAdjust colorAdjust = new ColorAdjust();
        colorAdjust.setBrightness(1.0);
        progressIndicatorDownload.setEffect(colorAdjust);
        progressIndicatorDownload.setCache(true);
        progressIndicatorDownload.setCacheHint(CacheHint.SPEED);

        downloadProgress.setProgress(0);

        String os = System.getProperty("os.name").toLowerCase(Locale.ENGLISH);
        String hydraulicFileName;

        if (os.contains("win")) {
            hydraulicFileName = "windows_Hydraulic.exe";
        } else if (os.contains("mac")) {
            hydraulicFileName = "mac_Hydraulic.jar";
        } else if (os.contains("nix") || os.contains("nux") || os.contains("aix")) {
            hydraulicFileName = "unix_Hydraulic.jar";
        } else {
            throw new UnsupportedOperationException("Bu işletim sistemi desteklenmiyor: " + os);
        }

        Task<Void> downloadTask = new Task<>() {
            @Override
            protected Void call() {
                try {
                    DownloadProgressListener downloadListener = (bytesRead, totalBytes) -> {
                        Platform.runLater(() -> downloadProgress.setProgress(0));

                        if (totalBytes > 0) {
                            double progress = (double) bytesRead / totalBytes;
                            Platform.runLater(() -> downloadProgress.setProgress(progress));
                        } else {
                            Platform.runLater(() -> downloadProgress.setProgress(ProgressBar.INDETERMINATE_PROGRESS)); // Indeterminate progress
                        }
                    };

                    VersionUtil.downloadLatestWithProgress(
                            SystemVariables.REPO_OWNER,
                            SystemVariables.HYDRAULIC_REPO_NAME,
                            SystemVariables.downloadPath,
                            hydraulicFileName,
                            downloadListener
                    );

                    OSUtil.updatePrefData(SystemVariables.PREF_NODE_NAME, SystemVariables.PREF_HYDRAULIC_KEY, VersionUtil.getLatestVersion(SystemVariables.REPO_OWNER, SystemVariables.HYDRAULIC_REPO_NAME));
                } catch (Exception e){
                    System.out.println(e.getMessage());
                }
                return null;
            }
        };

        try {
            Thread downloadThread = new Thread(downloadTask);
            downloadThread.setDaemon(true);
            downloadThread.start();
            downloadThread.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            e.printStackTrace();
        }
    }

    @FXML
    public void allAccounts() {
        populateUIWithCachedData(savedUserAccounts, null);
        setupSearchBar(accountSearchBar, savedUserAccounts);
        paneSwitch(1);
    }

    @FXML
    public void favouritedAccounts() {
        populateUIWithCachedData(savedUserAccounts, "favourite");
        setupSearchBar(accountSearchBar, savedUserAccounts);
        paneSwitch(2);
    }

    @FXML
    public void deletedAccounts() {
        populateUIWithCachedData(savedUserAccounts, "deleted");
        setupSearchBar(accountSearchBar, savedUserAccounts);
        paneSwitch(3);
    }

    @FXML
    public void deleteAccount() {
        selectedUser.setDeleted(true);
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, 20);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String formattedDate = sdf.format(calendar.getTime());
        selectedUser.setDeletionDate(formattedDate);

        GeneralUtil.deleteUserInFile(selectedUser);
    }

    @FXML
    public void hydraulicLicenses() {

    }

    @FXML
    public void signInAs() {

    }

    private void paneSwitch(int paneType) {
        updatePane.setVisible(false);
        updatePane.toBack();
        changeLogPane.setVisible(false);
        changeLogPane.toBack();
        settingsPane.setVisible(false);
        settingsPane.toBack();
        createAccountPane.setVisible(false);
        createAccountPane.toBack();
        downloadPane.setVisible(false);
        downloadPane.toBack();
        switch (paneType) {
            case 1: //Aktif Lisanslar & Hesaplar
                settingsPane.setVisible(false);
                settingsPane.toBack();
                break;
            case 2: //Favori Hesaplar
                settingsPane.setVisible(false);
                settingsPane.toBack();
                break;
            case 3: //Silinen Hesaplar
                settingsPane.setVisible(false);
                settingsPane.toBack();
                break;
            case 4: //Hidroliği İndir
                downloadPane.setVisible(true);
                downloadPane.toFront();
                break;
            case 5: //Check Updates
                updatePane.setVisible(true);
                updatePane.toFront();
                break;
            case 6: //ChangeLog
                changeLogPane.setVisible(true);
                changeLogPane.toFront();
                break;
            case 7: //Settings
                settingsPane.setVisible(true);
                settingsPane.toFront();
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

    private void togglePasswordVisibility() {
        if (passwordTextField.isVisible()) {
            passwordTextField.setManaged(false);
            passwordTextField.setVisible(false);
            visiblePasswordTextField.setManaged(true);
            visiblePasswordTextField.setVisible(true);
            visiblePasswordTextField.setPrefWidth(290.0);
            passwordTextField.setPrefWidth(0);
            visiblePasswordTextField.setText(girilenSifre);
            showHidePasswordImageView.setImage(new Image(Objects.requireNonNull(Launcher.class.getResourceAsStream("/assets/icons/icon_hide_pass.png"))));
        } else {
            passwordTextField.setManaged(true);
            passwordTextField.setVisible(true);
            visiblePasswordTextField.setManaged(false);
            visiblePasswordTextField.setVisible(false);
            visiblePasswordTextField.setPrefWidth(0);
            passwordTextField.setPrefWidth(290.0);
            showHidePasswordImageView.setImage(new Image(Objects.requireNonNull(Launcher.class.getResourceAsStream("/assets/icons/icon_show_pass.png"))));
        }
    }

    private void populateUIWithCachedData(List<User> savedUserAccounts, String filterCriteria) {
        savedUsersVBox.getChildren().clear();

        Node firstNode = null;

        for (User userData : savedUserAccounts) {
            if(filterCriteria != null) {
                if(filterCriteria.equals("favourite") && !userData.isFavourite()) {
                    continue;
                } else {
                    if(filterCriteria.equals("deleted") && !userData.isDeleted()) {
                        continue;
                    }
                }
            }

            try {
                FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(Launcher.class.getResource("fxml/mainitem.fxml")));
                Node node = loader.load();

                ImageView profilePhoto = (ImageView) loader.getNamespace().get("profilePhotoImageView");
                Label nameLabel = (Label) loader.getNamespace().get("nameLabel");
                Label userNameLabel = (Label) loader.getNamespace().get("userNameLabel");

                nameLabel.setText(userData.getNameSurname());
                userNameLabel.setText(userData.getUserName());

                HttpUtil.setProfilePhoto(userData.getUserName(), profilePhoto);

                node.setOnMouseClicked(event -> {
                    selectedUser = userData;
                    initializeCustomUserData(selectedUser);
                });

                node.setOnMouseEntered(event -> node.setStyle("-fx-background-color : #6393E7"));
                node.setOnMouseExited(event -> node.setStyle("-fx-background-color : transparent"));

                savedUsersVBox.getChildren().add(node);

                if (firstNode == null) {
                    firstNode = node;
                    selectedUser = userData;
                }
            } catch (IOException e) {
                System.out.println("Error: " + e.getMessage());
            }
        }

        if (firstNode != null) {
            customDataPane.setVisible(true);
            customDataPane.toFront();
            firstNode.fireEvent(new javafx.scene.input.MouseEvent(
                    javafx.scene.input.MouseEvent.MOUSE_CLICKED,
                    0, 0, 0, 0,
                    javafx.scene.input.MouseButton.PRIMARY, 1,
                    true, true, true, true, true, true, true, true, true, true, null
            ));
        }
    }

    private void initializeCustomUserData(User selectedUser) {
        customDataPane.setVisible(true);
        customDataPane.toFront();

        customNameSurname.setText(selectedUser.getNameSurname());
        customUserName.setText(selectedUser.getUserName());
        HttpUtil.setProfilePhoto(selectedUser.getUserName(), customImageView);

        accessTokenLabel.setText(selectedUser.getAccessToken());
        passwordLabel.setText("***");
        licenseLabel.setText(selectedUser.getLicenseKey());

        Image favouriteIcon = selectedUser.isFavourite()
                ? new Image(Objects.requireNonNull(Launcher.class.getResourceAsStream("/assets/icons/icon_star_favourite.png")))
                : new Image(Objects.requireNonNull(Launcher.class.getResourceAsStream("/assets/icons/icon_star_normal.png")));

        customFavouriteButton.setImage(favouriteIcon);

        customFavouriteButton.setOnMouseClicked(event -> {
            boolean newFavouriteStatus = !selectedUser.isFavourite();
            selectedUser.setFavourite(newFavouriteStatus);

            Image newIcon = newFavouriteStatus
                    ? new Image(Objects.requireNonNull(Launcher.class.getResourceAsStream("/assets/icons/icon_star_favourite.png")))
                    : new Image(Objects.requireNonNull(Launcher.class.getResourceAsStream("/assets/icons/icon_star_normal.png")));

            customFavouriteButton.setImage(newIcon);

            GeneralUtil.updateUserFavouriteStatusInFile(selectedUser);
            System.out.println("User favourite status updated: " + selectedUser);
        });

        copyLicenseButton.setOnMouseClicked(event -> {
            Clipboard clipboard = Clipboard.getSystemClipboard();
            ClipboardContent content = new ClipboardContent();
            content.putString(selectedUser.getLicenseKey());
            clipboard.setContent(content);
            System.out.println("License key copied to clipboard: " + selectedUser.getLicenseKey());
        });

        copyPasswordButton.setOnMouseClicked(event -> {
            Clipboard clipboard = Clipboard.getSystemClipboard();
            ClipboardContent content = new ClipboardContent();
            content.putString(selectedUser.getPassword());
            clipboard.setContent(content);
            System.out.println("Password copied to clipboard: " + selectedUser.getPassword());
        });

        customShowHidePass.setOnMouseClicked(event -> {
            if (isHidden) {
                isHidden = false;
                passwordLabel.setText(selectedUser.getPassword());
                customShowHidePass.setImage(new Image(Objects.requireNonNull(Launcher.class.getResourceAsStream("/assets/icons/icon_hide_pass.png"))));
            } else {
                isHidden = true;
                passwordLabel.setText("***");
                customShowHidePass.setImage(new Image(Objects.requireNonNull(Launcher.class.getResourceAsStream("/assets/icons/icon_show_pass.png"))));
            }
        });

        loginURL.setOnMouseClicked(event -> GeneralUtil.openURL(SystemVariables.BASE_LOGIN_URL));
    }

    private void setupSearchBar(TextField accountSearchBar, List<User> savedUserAccounts) {
        final PauseTransition pause = new PauseTransition(Duration.millis(500));
        accountSearchBar.textProperty().addListener((observable, oldValue, newValue) -> {
            pause.setOnFinished(event -> {
                String searchText = newValue.toLowerCase();
                List<User> filteredUsers = savedUserAccounts.stream()
                        .filter(user -> user.getUserName().toLowerCase().contains(searchText) || user.getNameSurname().toLowerCase().contains(searchText))
                        .toList();
                populateUIWithCachedData(filteredUsers, null);
            });
            pause.playFromStart();
        });
    }
}