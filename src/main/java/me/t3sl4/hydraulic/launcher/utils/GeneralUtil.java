package me.t3sl4.hydraulic.launcher.utils;

import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import me.t3sl4.hydraulic.launcher.Launcher;
import me.t3sl4.hydraulic.launcher.utils.Model.User;
import me.t3sl4.util.file.DirectoryUtil;
import me.t3sl4.util.file.FileUtil;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class GeneralUtil {

    public static void systemShutdown() {
        if(!System.getProperty("os.name").toLowerCase().contains("win")) {
            try {
                Path lockFilePath = Path.of(System.getProperty("user.home"), ".canicula_launcher.pid");
                Files.deleteIfExists(lockFilePath);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        Platform.exit();
        System.exit(0);
    }

    public static void openURL(String url) {
        try {
            // URI oluştur
            URI uri = new URI(url);

            // Desktop API ile URL'yi aç
            if (Desktop.isDesktopSupported()) {
                Desktop desktop = Desktop.getDesktop();
                if (desktop.isSupported(Desktop.Action.BROWSE)) {
                    desktop.browse(uri);
                    System.out.println("URL opened: " + url);
                } else {
                    System.err.println("Error: BROWSE action is not supported on this system.");
                }
            } else {
                System.err.println("Error: Desktop API is not supported on this system.");
            }
        } catch (URISyntaxException e) {
            System.err.println("Error: Invalid URL syntax: " + url);
            e.printStackTrace();
        } catch (IOException e) {
            System.err.println("Error opening URL: " + url);
            e.printStackTrace();
        }
    }

    public static void minimizeToSystemTray(Stage primaryStage) {
        if (!SystemTray.isSupported()) {
            System.out.println("System tray not supported!");
            primaryStage.setIconified(true);
            return;
        }

        Platform.runLater(() -> {
            try {
                // Create System Tray icon
                SystemTray tray = SystemTray.getSystemTray();
                Image fxImage = new Image(Objects.requireNonNull(
                        Launcher.class.getResourceAsStream("/assets/images/logo.png")
                ));

                // Convert JavaFX Image to BufferedImage
                BufferedImage awtImage = SwingFXUtils.fromFXImage(fxImage, null);

                TrayIcon trayIcon = new TrayIcon(awtImage, "Canicula Launcher");
                trayIcon.setImageAutoSize(true);

                PopupMenu popup = new PopupMenu();
                MenuItem exitItem = new MenuItem("Tamamen Kapat");
                MenuItem expandItem = new MenuItem("Öne Getir");

                exitItem.addActionListener(e -> {
                    tray.remove(trayIcon);
                    systemShutdown();
                });

                expandItem.addActionListener(e -> SwingUtilities.invokeLater(() -> Platform.runLater(() -> {
                    tray.remove(trayIcon);
                    primaryStage.show();
                    primaryStage.setIconified(false);
                    primaryStage.toFront();
                })));

                popup.add(expandItem);
                popup.add(exitItem);
                trayIcon.setPopupMenu(popup);

                tray.add(trayIcon);

                trayIcon.addActionListener(e -> SwingUtilities.invokeLater(() -> Platform.runLater(() -> {
                    tray.remove(trayIcon);
                    primaryStage.show();
                    primaryStage.setIconified(false);
                    primaryStage.toFront();
                })));

                SwingUtilities.invokeLater(() -> Platform.runLater(() -> { trayIcon.displayMessage(
                        "Canicula Launcher",
                        "Program arka planda çalışmaya devam ediyor.",
                        TrayIcon.MessageType.INFO
                );}));

                // Hide the JavaFX Stage
                primaryStage.hide();

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public static void criticalFileSystem() throws IOException {
        // İşletim sistemine göre dosya yollarını ayarla
        String userHome = System.getProperty("user.name");
        String os = System.getProperty("os.name").toLowerCase();
        String basePath;
        String programName;

        if (os.contains("win")) {
            basePath = "C:/Users/" + userHome + "/";
            programName = "windows_Hydraulic.exe";
        } else {
            basePath = "/Users/" + userHome + "/";
            programName = "unix_Hydraulic.jar";
        }

        // Dosya yollarını belirle
        SystemVariables.mainPath = basePath + "OnderGrup/";
        SystemVariables.userAccountsFolderPath = SystemVariables.mainPath + "userAccounts/";
        SystemVariables.userAccountsDataPath = SystemVariables.mainPath + "userAccounts/userAccounts.yml";
        SystemVariables.profilePhotoLocalPath = SystemVariables.mainPath + "userAccounts/";
        SystemVariables.userDataPath = SystemVariables.mainPath + "userData/";
        SystemVariables.localHydraulicDataPath = SystemVariables.mainPath + "userData/HydraulicUnits/";
        SystemVariables.partListDataPath = SystemVariables.mainPath + "data/";
        SystemVariables.licensePath = SystemVariables.userDataPath + "license.txt";
        SystemVariables.hydraulicPath = SystemVariables.mainPath + programName;
        SystemVariables.downloadPath = SystemVariables.mainPath;

        DirectoryUtil.createDirectory(SystemVariables.userAccountsFolderPath);
        FileUtil.createFile(SystemVariables.userAccountsDataPath);
    }

    @SuppressWarnings("unchecked")
    public static void createUserAccountData(String userName, String password, String nameSurname, String licenseKey, String accessToken, boolean isFavourite) {
        LoaderOptions loaderOptions = new LoaderOptions();
        Yaml yaml = new Yaml(loaderOptions);
        Map<String, Object> data;

        File yamlFile = new File(SystemVariables.userAccountsDataPath);
        if (!yamlFile.exists()) {
            data = new HashMap<>();
            data.put("user_accounts", new HashMap<>());
        } else {
            try (FileReader reader = new FileReader(yamlFile)) {
                data = yaml.load(reader);
                if (data == null) {
                    data = new HashMap<>();
                    data.put("user_accounts", new HashMap<>());
                }
            } catch (IOException e) {
                throw new RuntimeException("YAML dosyası okunurken bir hata oluştu", e);
            }
        }

        Map<String, Map<String, Object>> userAccountsMap = (Map<String, Map<String, Object>>) data.get("user_accounts");

        boolean found = false;
        for (Map.Entry<String, Map<String, Object>> entry : userAccountsMap.entrySet()) {
            if (entry.getValue().get("userName").equals(userName)) {
                found = true;
                break;
            }
        }

        if (!found) {
            int nextIndex = userAccountsMap.size();
            Map<String, Object> newEntry = new HashMap<>();
            newEntry.put("userName", userName);
            newEntry.put("password", password);
            newEntry.put("nameSurname", nameSurname);
            newEntry.put("isFavourite", String.valueOf(isFavourite));
            newEntry.put("isDeleted", String.valueOf(false));
            newEntry.put("deletionDate", "");
            newEntry.put("licenseKey", licenseKey == null ? "" : licenseKey);
            newEntry.put("accessToken", accessToken);

            userAccountsMap.put(String.valueOf(nextIndex), newEntry);
        }

        try (FileWriter writer = new FileWriter(yamlFile)) {
            DumperOptions options = new DumperOptions();
            options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
            options.setDefaultScalarStyle(DumperOptions.ScalarStyle.DOUBLE_QUOTED);
            yaml = new Yaml(options);
            yaml.dump(data, writer);
        } catch (IOException e) {
            throw new RuntimeException("YAML dosyasına yazılırken bir hata oluştu", e);
        }
    }

    @SuppressWarnings("unchecked")
    public static void readUserAccountData(List<User> savedUserAccounts) {
        LoaderOptions loaderOptions = new LoaderOptions();
        Yaml yaml = new Yaml(loaderOptions);
        Map<String, Object> data;

        File yamlFile = new File(SystemVariables.userAccountsDataPath);
        if (!yamlFile.exists()) {
            return; // Dosya yoksa işlem yapmadan geri dön
        } else {
            try (FileReader reader = new FileReader(yamlFile)) {
                data = yaml.load(reader);
                if (data == null || !data.containsKey("user_accounts")) {
                    return; // Geçerli bir veri yoksa işlem yapmadan geri dön
                }
            } catch (IOException e) {
                System.out.println("Hata: " + e.getMessage());
                e.printStackTrace();
                throw new RuntimeException("YAML dosyası okunurken bir hata oluştu", e);
            }
        }

        Map<String, Map<String, Object>> userAccountsMap = (Map<String, Map<String, Object>>) data.get("user_accounts");
        for (Map<String, Object> accountData : userAccountsMap.values()) {
            String userName = (String) accountData.get("userName");
            String password = (String) accountData.get("password");
            String nameSurname = (String) accountData.get("nameSurname");
            boolean isFavourite = Boolean.parseBoolean((String) accountData.get("isFavourite"));
            boolean isDeleted = Boolean.parseBoolean((String) accountData.get("isDeleted"));
            String deletionDate = (String) accountData.get("deletionDate");
            String licenseKey = (String) accountData.get("licenseKey");
            String accessToken = (String) accountData.get("accessToken");

            User user = new User(userName, password, nameSurname, isFavourite, isDeleted, deletionDate, licenseKey, accessToken);
            savedUserAccounts.add(user);
        }
    }

    public static void updateUserFavouriteStatusInFile(User userData) {
        File yamlFile = new File(SystemVariables.userAccountsDataPath);
        LoaderOptions loaderOptions = new LoaderOptions();
        Yaml yaml = new Yaml(loaderOptions);
        Map<String, Object> data;

        try (FileReader reader = new FileReader(yamlFile)) {
            data = yaml.load(reader);
            if (data != null && data.containsKey("user_accounts")) {
                Map<String, Map<String, Object>> userAccountsMap = (Map<String, Map<String, Object>>) data.get("user_accounts");

                for (Map.Entry<String, Map<String, Object>> entry : userAccountsMap.entrySet()) {
                    Map<String, Object> accountData = entry.getValue();
                    if (accountData.get("userName").equals(userData.getUserName())) {
                        accountData.put("isFavourite", String.valueOf(userData.isFavourite()));
                        break;
                    }
                }

                DumperOptions options = new DumperOptions();
                options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
                options.setDefaultScalarStyle(DumperOptions.ScalarStyle.DOUBLE_QUOTED);

                try (FileWriter writer = new FileWriter(yamlFile)) {
                    yaml = new Yaml(options);
                    yaml.dump(data, writer);
                }
            }
        } catch (IOException e) {
            System.out.println("Error updating user favourite status: " + e.getMessage());
        }
    }

    public static void deleteUserInFile(User userData) {
        File yamlFile = new File(SystemVariables.userAccountsDataPath);
        LoaderOptions loaderOptions = new LoaderOptions();
        Yaml yaml = new Yaml(loaderOptions);
        Map<String, Object> data;

        try (FileReader reader = new FileReader(yamlFile)) {
            data = yaml.load(reader);
            if (data != null && data.containsKey("user_accounts")) {
                Map<String, Map<String, Object>> userAccountsMap = (Map<String, Map<String, Object>>) data.get("user_accounts");

                for (Map.Entry<String, Map<String, Object>> entry : userAccountsMap.entrySet()) {
                    Map<String, Object> accountData = entry.getValue();
                    if (accountData.get("userName").equals(userData.getUserName())) {
                        accountData.put("isDeleted", String.valueOf(userData.isFavourite()));
                        accountData.put("deletionDate", userData.getDeletionDate());
                        break;
                    }
                }

                DumperOptions options = new DumperOptions();
                options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
                options.setDefaultScalarStyle(DumperOptions.ScalarStyle.DOUBLE_QUOTED);

                try (FileWriter writer = new FileWriter(yamlFile)) {
                    yaml = new Yaml(options);
                    yaml.dump(data, writer);
                }
            }
        } catch (IOException e) {
            System.out.println("Error updating user favourite status: " + e.getMessage());
        }
    }
}