package me.t3sl4.hydraulic.launcher.controller;

import javafx.animation.KeyFrame;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javafx.util.Duration;
import me.t3sl4.hydraulic.launcher.Launcher;
import me.t3sl4.hydraulic.launcher.utils.FileUtil;
import me.t3sl4.hydraulic.launcher.utils.GeneralUtil;
import me.t3sl4.hydraulic.launcher.utils.HTTP.HttpUtil;
import me.t3sl4.hydraulic.launcher.utils.Model.User;
import me.t3sl4.hydraulic.launcher.utils.SystemVariables;
import me.t3sl4.hydraulic.launcher.utils.Version.UpdateCheckerService;
import mslinks.ShellLink;

import javax.swing.filechooser.FileSystemView;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
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
    
    @FXML
    private VBox savedUsersVBox;

    @FXML
    private TextField accountSearchBar;

    @FXML
    private Button accountEditButton, accountDeleteButton, accountLoginButton;

    @FXML
    private TextField userNameTextField, licenseKeyTextField;

    @FXML
    private PasswordField passwordTextField;

    @FXML
    private TextField visiblePasswordTextField;

    @FXML
    private Pane createAccountPane, customDataPane;

    @FXML
    private Pane settingsPane;

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

    //Ekran büyütüp küçültme
    private boolean stageMaximized = false;
    private boolean isHidden = true;
    private String girilenSifre = "";

    //Kaydedilen user Accountslar
    private List<User> savedUserAccounts = new ArrayList<>();

    //Tablodan seçilen kullanıcı
    private User selectedUser;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Platform.runLater(() -> {
            currentStage = (Stage) accountsButton.getScene().getWindow();

            // Program büyültme, küçültme ve kapatma için hover efekti
            addHoverEffect(closeIcon, minimizeIcon, expandIcon);

            changeLogWebView.getEngine().load("https://github.com/hidirektor/ondergrup-hydraulic-tool/releases");
        });

        FileUtil.readUserAccountData(savedUserAccounts);
        populateUIWithCachedData(savedUserAccounts, "all");
        setupSearchBar(accountSearchBar, savedUserAccounts, "all");
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

        UpdateCheckerService updateService = new UpdateCheckerService();
        updateService.start();

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
                String fileName = "windows_Launcher";
                if (newVal) {
                    createDesktopShortcut(fileName + ".exe", SystemVariables.mainPath + "\\" + fileName + ".exe", SystemVariables.mainPath);
                } else {
                    deleteShortcut(FileSystemView.getFileSystemView().getHomeDirectory().getAbsolutePath() + "\\" + fileName + ".exe" + ".lnk");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        onderLauncherAutoStartCheck.selectedProperty().addListener((obs, oldVal, newVal) -> {
            try {
                String fileName = "windows_Launcher";
                if (newVal) {
                    addToStartup(fileName + ".exe", SystemVariables.mainPath + "\\" + fileName + ".exe", SystemVariables.mainPath);
                } else {
                    deleteShortcut(System.getProperty("user.home") + "\\AppData\\Roaming\\Microsoft\\Windows\\Start Menu\\Programs\\Startup\\" + fileName + ".exe" + ".lnk");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        hydraulicToolShortcutCheck.selectedProperty().addListener((obs, oldVal, newVal) -> {
            try {
                String fileName = "windows_Hydraulic";
                if (newVal) {
                    createDesktopShortcut(fileName + ".exe", SystemVariables.mainPath + "\\" + fileName + ".exe", SystemVariables.mainPath);
                } else {
                    deleteShortcut(FileSystemView.getFileSystemView().getHomeDirectory().getAbsolutePath() + "\\" + fileName + ".exe" + ".lnk");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        hydraulicToolAutoStartCheck.selectedProperty().addListener((obs, oldVal, newVal) -> {
            try {
                String fileName = "windows_Hydraulic";
                if (newVal) {
                    addToStartup(fileName + ".exe", SystemVariables.mainPath + "\\" + fileName + ".exe", SystemVariables.mainPath);
                } else {
                    deleteShortcut(System.getProperty("user.home") + "\\AppData\\Roaming\\Microsoft\\Windows\\Start Menu\\Programs\\Startup\\" + fileName + ".exe" + ".lnk");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
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
    public void addAccount() {
        createAccountPane.setVisible(true);
        createAccountPane.toFront();
        userNameTextField.clear();
        passwordTextField.clear();
        visiblePasswordTextField.clear();
        licenseKeyTextField.clear();

        passwordTextField.setPrefWidth(290.0);
        visiblePasswordTextField.setPrefWidth(0);

        accountLoginButton.setOnAction(_ -> {
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

        showHidePasswordImageView.setOnMouseClicked(_ -> togglePasswordVisibility());
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

    @FXML
    public void favouritedAccounts() {
        populateUIWithCachedData(savedUserAccounts, "favourite");
        setupSearchBar(accountSearchBar, savedUserAccounts, "favourite");
    }

    @FXML
    public void deletedAccounts() {
        populateUIWithCachedData(savedUserAccounts, "deleted");
        setupSearchBar(accountSearchBar, savedUserAccounts, "deleted");
    }

    private void paneSwitch(int paneType) {
        updatePane.setVisible(false);
        updatePane.toBack();
        changeLogPane.setVisible(false);
        changeLogPane.toBack();
        settingsPane.setVisible(false);
        settingsPane.toBack();
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
            if (filterCriteria.equals("favourite") && (!userData.isFavourite() || userData.isDeleted())) {
                continue;
            } else if (filterCriteria.equals("deleted") && !userData.isDeleted()) {
                continue;
            } else if (filterCriteria.equals("all")) {
                // Show all users without further filtering
            } else if (!filterCriteria.equals("favourite") && !filterCriteria.equals("deleted") && (userData.isFavourite() || userData.isDeleted())) {
                continue;
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

                node.setOnMouseClicked(_ -> {
                    selectedUser = userData;
                    initializeCustomUserData(selectedUser);
                });

                node.setOnMouseEntered(_ -> node.setStyle("-fx-background-color : #6393E7"));
                node.setOnMouseExited(_ -> node.setStyle("-fx-background-color : transparent"));

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
            customDataComponentVisualize(true);
            firstNode.fireEvent(new javafx.scene.input.MouseEvent(
                    javafx.scene.input.MouseEvent.MOUSE_CLICKED,
                    0, 0, 0, 0,
                    javafx.scene.input.MouseButton.PRIMARY, 1,
                    true, true, true, true, true, true, true, true, true, true, null
            ));
        } else {
            customDataComponentVisualize(false);
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

        customFavouriteButton.setOnMouseClicked(_ -> {
            boolean newFavouriteStatus = !selectedUser.isFavourite();
            selectedUser.setFavourite(newFavouriteStatus);

            Image newIcon = newFavouriteStatus
                    ? new Image(Objects.requireNonNull(Launcher.class.getResourceAsStream("/assets/icons/icon_star_favourite.png")))
                    : new Image(Objects.requireNonNull(Launcher.class.getResourceAsStream("/assets/icons/icon_star_normal.png")));

            customFavouriteButton.setImage(newIcon);

            FileUtil.updateUserFavouriteStatusInFile(selectedUser);
            System.out.println("User favourite status updated: " + selectedUser);
        });

        copyLicenseButton.setOnMouseClicked(_ -> {
            Clipboard clipboard = Clipboard.getSystemClipboard();
            ClipboardContent content = new ClipboardContent();
            content.putString(selectedUser.getLicenseKey());
            clipboard.setContent(content);
            System.out.println("License key copied to clipboard: " + selectedUser.getLicenseKey());
        });

        copyPasswordButton.setOnMouseClicked(_ -> {
            Clipboard clipboard = Clipboard.getSystemClipboard();
            ClipboardContent content = new ClipboardContent();
            content.putString(selectedUser.getPassword());
            clipboard.setContent(content);
            System.out.println("Password copied to clipboard: " + selectedUser.getPassword());
        });

        customShowHidePass.setOnMouseClicked(_ -> {
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

        loginURL.setOnMouseClicked(_ -> GeneralUtil.openURL(SystemVariables.BASE_LOGIN_URL));
    }

    private void setupSearchBar(TextField accountSearchBar, List<User> savedUserAccounts, String filterCriteria) {
        final PauseTransition pause = new PauseTransition(Duration.millis(500));
        accountSearchBar.textProperty().addListener((observable, oldValue, newValue) -> {
            pause.setOnFinished(_ -> {
                String searchText = newValue.toLowerCase();
                List<User> filteredUsers = savedUserAccounts.stream()
                        .filter(user -> {
                            boolean matchesSearch = user.getUserName().toLowerCase().contains(searchText) || user.getNameSurname().toLowerCase().contains(searchText);
                            if (filterCriteria.equals("favourite")) {
                                return matchesSearch && user.isFavourite() && !user.isDeleted();
                            } else if (filterCriteria.equals("deleted")) {
                                return matchesSearch && user.isDeleted();
                            } else {
                                return matchesSearch && !user.isFavourite() && !user.isDeleted();
                            }
                        })
                        .toList();
                populateUIWithCachedData(filteredUsers, "all");
            });
            pause.playFromStart();
        });
    }

    private void customDataComponentVisualize(boolean isVisual) {
        if(isVisual) {
            customImageView.setVisible(true);
            customNameSurname.setVisible(true);
            customUserName.setVisible(true);
            accessTokenLabel.setVisible(true);
            passwordLabel.setVisible(true);
            licenseLabel.setVisible(true);
        } else {
            customImageView.setVisible(false);
            customNameSurname.setVisible(false);
            customUserName.setVisible(false);
            accessTokenLabel.setVisible(false);
            passwordLabel.setVisible(false);
            licenseLabel.setVisible(false);
        }
    }

    public static void createDesktopShortcut(String fileName, String targetPath, String workingDirectory) throws IOException {
        // Masaüstü dizinini al
        File home = FileSystemView.getFileSystemView().getHomeDirectory();
        String desktopPath = home.getAbsolutePath();
        File desktopDir = new File(desktopPath);

        // Masaüstü dizinini kontrol et ve gerekirse oluştur
        if (!desktopDir.exists() && !desktopDir.mkdirs()) {
            throw new IOException("Masaüstü dizini oluşturulamadı: " + desktopPath);
        }

        String shortcutPath = desktopPath + "\\" + fileName + ".lnk";

        // Kısayolu oluştur
        ShellLink sl = new ShellLink()
                .setTarget(targetPath)
                .setWorkingDir(workingDirectory)
                .setIconLocation(targetPath); // İkon olarak aynı dosya ayarlanıyor
        sl.getHeader().setIconIndex(0); // İkonun dizin numarası

        // Kısayolu kaydet
        sl.saveTo(shortcutPath);
        System.out.println("Kısayol oluşturuldu: " + shortcutPath);
    }

    public static void addToStartup(String fileName, String targetPath, String workingDirectory) throws IOException {
        // Windows başlangıç klasörünü al
        String startupPath = System.getProperty("user.home") + "\\AppData\\Roaming\\Microsoft\\Windows\\Start Menu\\Programs\\Startup";
        File startupDir = new File(startupPath);

        // Başlangıç dizinini kontrol et ve gerekirse oluştur
        if (!startupDir.exists() && !startupDir.mkdirs()) {
            throw new IOException("Başlangıç dizini oluşturulamadı: " + startupPath);
        }

        String shortcutPath = startupPath + "\\" + fileName + ".lnk";

        // Kısayolu oluştur
        ShellLink sl = new ShellLink()
                .setTarget(targetPath)
                .setWorkingDir(workingDirectory)
                .setIconLocation(targetPath); // İkon olarak aynı dosya ayarlanıyor
        sl.getHeader().setIconIndex(0); // İkonun dizin numarası

        // Kısayolu kaydet
        sl.saveTo(shortcutPath);
        System.out.println("Başlangıç klasörüne eklendi: " + shortcutPath);
    }

    private void deleteShortcut(String path) throws IOException {
        File shortcut = new File(path);
        if (shortcut.exists()) {
            if (!shortcut.delete()) {
                throw new IOException("Kısayol silinemedi: " + path);
            }
        } else {
            System.out.println("Kısayol bulunamadı: " + path);
        }
    }
}