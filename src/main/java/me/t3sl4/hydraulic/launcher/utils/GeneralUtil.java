package me.t3sl4.hydraulic.launcher.utils;

import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import me.t3sl4.hydraulic.launcher.Launcher;
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

public class GeneralUtil {

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