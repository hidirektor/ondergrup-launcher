package me.t3sl4.hydraulic.launcher.utils;

import javafx.application.Platform;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import me.t3sl4.hydraulic.launcher.Launcher;
import me.t3sl4.hydraulic.launcher.app.Main;
import me.t3sl4.hydraulic.launcher.controller.MainController;

import java.awt.*;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Objects;

public class GeneralUtil {

    public static final Logger logger = Logger.getLogger(MainController.class.getName());

    private static final String REGISTRY_PATH = "SOFTWARE\\OnderGrup\\HydraulicCalculation";
    private static final String LICENSE_KEY_NAME = "LicenseKey";

    public static final String PREFERENCE_KEY = "defaultMonitor";
    public static Preferences prefs;

    public static boolean netIsAvailable() {
        try {
            final URL url = new URL("http://www.google.com");
            final URLConnection conn = url.openConnection();
            conn.connect();
            conn.getInputStream().close();
            return true;
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            return false;
        }
    }

    private static String getMonitorBrand(int index) {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice[] devices = ge.getScreenDevices();

        if (index < devices.length) {
            return devices[index].getIDstring();
        }

        return "Unknown Monitor";
    }

    private static void saveSelectedMonitor(String monitor) {
        prefs.put(PREFERENCE_KEY, monitor);
    }

    public static String checkDefaultMonitor() {
        return GeneralUtil.prefs.get(PREFERENCE_KEY, null);
    }

    private static void deleteFile(String filePath) {
        File file = new File(filePath);
        if (file.exists()) {
            if (file.delete()) {
                System.out.println("Dosya silindi: " + filePath);
            } else {
                System.err.println("Dosya silinemedi: " + filePath);
            }
        } else {
            System.out.println("Dosya bulunamadı: " + filePath);
        }
    }

    private static void deleteDirectory(File directory) {
        if (directory.exists()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        deleteDirectory(file);
                    } else {
                        deleteFile(file.getAbsolutePath());
                    }
                }
            }
            if (directory.delete()) {
                System.out.println("Dizin silindi: " + directory.getAbsolutePath());
            } else {
                System.err.println("Dizin silinemedi: " + directory.getAbsolutePath());
            }
        } else {
            System.out.println("Dizin bulunamadı: " + directory.getAbsolutePath());
        }
    }

    public static void openFile(String filePath) {
        File file = new File(filePath);

        if (!file.exists()) {
            System.out.println("Dosya bulunamadı: " + filePath);
            return;
        }

        if (Desktop.isDesktopSupported()) {
            Desktop desktop = Desktop.getDesktop();
            try {
                desktop.open(file);
                System.out.println("Dosya başarıyla açıldı: " + filePath);
            } catch (IOException e) {
                System.out.println("Dosya açılamadı: " + e.getMessage());
            }
        } else {
            System.out.println("Bu platform masaüstü fonksiyonlarını desteklemiyor.");
        }
    }

    public static void systemShutdown() {
        Platform.exit();

        System.exit(0);
    }

    public static void openFolder(String path) {
        try {
            // Dosya nesnesi oluştur
            File folder = new File(path);

            // Klasörün varlığını kontrol et
            if (!folder.exists()) {
                System.err.println("Error: Path does not exist: " + path);
                return;
            }

            // Desktop API ile klasörü aç
            if (Desktop.isDesktopSupported()) {
                Desktop desktop = Desktop.getDesktop();
                desktop.open(folder);
                System.out.println("Folder opened: " + path);
            } else {
                System.err.println("Error: Desktop API is not supported on this system.");
            }
        } catch (IOException e) {
            System.err.println("Error opening folder: " + path);
            e.printStackTrace();
        }
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

                TrayIcon trayIcon = new TrayIcon(awtImage, "Önder Grup Launcher");
                trayIcon.setImageAutoSize(true);

                // Create PopupMenu for Tray Icon
                PopupMenu popup = new PopupMenu();
                MenuItem exitItem = new MenuItem("Exit");
                MenuItem expandItem = new MenuItem("Expand Launcher");

                // Exit item action
                exitItem.addActionListener(e -> {
                    tray.remove(trayIcon);
                    systemShutdown();
                });

                // Expand item action
                expandItem.addActionListener(e -> SwingUtilities.invokeLater(() -> Platform.runLater(() -> {
                    tray.remove(trayIcon); // Remove tray icon
                    primaryStage.show();   // Show the stage
                    primaryStage.setIconified(false); // Ensure not minimized
                    primaryStage.toFront(); // Bring to front
                })));

                popup.add(expandItem);
                popup.add(exitItem);
                trayIcon.setPopupMenu(popup);

                // Add the Tray Icon
                tray.add(trayIcon);

                // Double-click action to restore application
                trayIcon.addActionListener(e -> SwingUtilities.invokeLater(() -> Platform.runLater(() -> {
                    tray.remove(trayIcon);
                    primaryStage.show();
                    primaryStage.setIconified(false);
                    primaryStage.toFront();
                })));

                // Show a notification when minimized
                SwingUtilities.invokeLater(() -> Platform.runLater(() -> { trayIcon.displayMessage(
                        "Önder Grup Launcher",
                        "Program is still running in the background.",
                        TrayIcon.MessageType.INFO
                );}));

                // Hide the JavaFX Stage
                primaryStage.hide();

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public static void downloadLatestVersion(File selectedDirectory) throws IOException {
        String os = System.getProperty("os.name").toLowerCase();
        String downloadURL = getDownloadURLForOS(os);

        if (downloadURL == null) {
            System.out.println("Uygun sürüm bulunamadı.");
            return;
        }

        File downloadFile = new File(selectedDirectory.getAbsolutePath() + "/" + getFileNameFromURL(downloadURL));
        try (BufferedInputStream in = new BufferedInputStream(new URL(downloadURL).openStream());
             FileOutputStream fileOutputStream = new FileOutputStream(downloadFile)) {

            byte[] dataBuffer = new byte[1024];
            int bytesRead;
            long totalBytesRead = 0;
            long fileSize = new URL(downloadURL).openConnection().getContentLengthLong();

            while ((bytesRead = in.read(dataBuffer, 0, dataBuffer.length)) != -1) {
                fileOutputStream.write(dataBuffer, 0, bytesRead);
                totalBytesRead += bytesRead;
            }

            System.out.println("Dosya başarıyla indirildi: " + downloadFile.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
            throw e;
        }
    }

    private static String getDownloadURLForOS(String os) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(SystemVariables.ASSET_URL).openConnection();
        connection.setRequestProperty("Accept", "application/vnd.github.v3+json");

        if (connection.getResponseCode() == 200) {
            String jsonResponse = new Scanner(connection.getInputStream(), "UTF-8").useDelimiter("\\A").next();
            JSONObject releaseData = new JSONObject(jsonResponse);
            JSONArray assets = releaseData.getJSONArray("assets");

            for (int i = 0; i < assets.length(); i++) {
                JSONObject asset = assets.getJSONObject(i);
                String assetName = asset.getString("name");

                if (os.contains("win") && assetName.contains("windows")) {
                    return asset.getString("browser_download_url");
                } else if (os.contains("mac") && assetName.contains("macOS")) {
                    return asset.getString("browser_download_url");
                } else if ((os.contains("nix") || os.contains("nux")) && assetName.contains("linux")) {
                    return asset.getString("browser_download_url");
                }
            }
        } else {
            System.out.println("GitHub API'ye erişilemedi: " + connection.getResponseMessage());
        }

        return null;
    }

    private static String getFileNameFromURL(String url) {
        return url.substring(url.lastIndexOf('/') + 1);
    }

    public static String getDeviceInfoAsJson() {
        try {
            String osName = System.getProperty("os.name");
            String osVersion = System.getProperty("os.version");
            String osArch = System.getProperty("os.arch");
            int availableProcessors = Runtime.getRuntime().availableProcessors();

            long maxMemory = Runtime.getRuntime().maxMemory();
            long totalMemory = Runtime.getRuntime().totalMemory();

            String ipAddress = getIpAddress();
            String externalIpAddress = getExternalIpAddress();
            String hwid = getHardwareId();

            JSONObject deviceInfoJson = new JSONObject();
            deviceInfoJson.put("osName", osName);
            deviceInfoJson.put("osVersion", osVersion);
            deviceInfoJson.put("osArch", osArch);
            deviceInfoJson.put("availableProcessors", availableProcessors);
            deviceInfoJson.put("maxMemory", maxMemory);
            deviceInfoJson.put("totalMemory", totalMemory);
            deviceInfoJson.put("ipAddress", ipAddress);
            deviceInfoJson.put("externalIpAddress", externalIpAddress);
            deviceInfoJson.put("hwid", hwid);

            return deviceInfoJson.toString(4);
        } catch (Exception e) {
            e.printStackTrace();
            return "{}";
        }
    }

    private static String getIpAddress() {
        try {
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            while (networkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = networkInterfaces.nextElement();

                if (networkInterface.isUp() && !networkInterface.isLoopback()) {
                    Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();
                    while (inetAddresses.hasMoreElements()) {
                        InetAddress inetAddress = inetAddresses.nextElement();
                        if (inetAddress instanceof Inet4Address) {
                            return inetAddress.getHostAddress();
                        }
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return "Unknown";
    }

    private static String getExternalIpAddress() {
        String ipAddress = "Unknown";
        try {
            URL url = new URL("http://api.ipify.org");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            ipAddress = reader.readLine();
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ipAddress;
    }

    private static String getHardwareId() {
        try {
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            while (networkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = networkInterfaces.nextElement();
                if (networkInterface != null && !networkInterface.isLoopback() && networkInterface.getHardwareAddress() != null) {
                    byte[] macBytes = networkInterface.getHardwareAddress();
                    StringBuilder macAddress = new StringBuilder();
                    for (byte b : macBytes) {
                        macAddress.append(String.format("%02X", b));
                    }
                    return macAddress.toString();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "Unknown";
    }
}