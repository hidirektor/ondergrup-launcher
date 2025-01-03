package me.t3sl4.hydraulic.launcher.utils;

import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import me.t3sl4.hydraulic.launcher.Launcher;
import me.t3sl4.hydraulic.launcher.controller.MainController;
import org.json.JSONObject;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Enumeration;
import java.util.Objects;
import java.util.logging.Logger;
import java.util.prefs.Preferences;

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
        if(!System.getProperty("os.name").toLowerCase().contains("win")) {
            try {
                Path lockFilePath = Path.of(System.getProperty("user.home"), ".onder_grup_launcher.pid");
                Files.deleteIfExists(lockFilePath);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

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
                        "Önder Grup Launcher",
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