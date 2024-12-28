package me.t3sl4.hydraulic.launcher.app;

import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinUser;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Screen;
import javafx.stage.Stage;
import me.t3sl4.hydraulic.launcher.utils.FileUtil;
import me.t3sl4.hydraulic.launcher.utils.GeneralUtil;
import me.t3sl4.hydraulic.launcher.utils.SceneUtil;
import me.t3sl4.hydraulic.launcher.utils.SystemVariables;

import java.awt.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.util.List;
import java.util.prefs.Preferences;

public class Main extends Application {
    List<Screen> screens = Screen.getScreens();

    public static Screen defaultScreen;

    @Override
    public void start(Stage primaryStage) throws IOException {
        GeneralUtil.prefs = Preferences.userRoot().node("onderGrupUpdater");
        FileUtil.criticalFileSystem();

        if(!System.getProperty("os.name").toLowerCase().contains("win")) {
            if (!checkSingleInstance()) {
                System.out.println("Program zaten çalışıyor. Odaklanıyor...");
                focusApp("Önder Grup Launcher " + SystemVariables.getVersion());
                Platform.exit();
                return;
            }
        }

        checkVersionFromPrefs();

        Platform.setImplicitExit(false);

        defaultScreen = screens.get(0);
        SceneUtil.openMainScreen(screens.get(0));

        System.out.println("Önder Grup Updater servisi başlatıldı.");
    }

    private void checkVersionFromPrefs() {
        String launcherVersionKey = "launcher_version";
        String hydraulicVersionKey = "hydraulic_version";

        String currentVersion = SystemVariables.CURRENT_VERSION;

        String savedLauncherVersion = GeneralUtil.prefs.get(launcherVersionKey, null);
        String savedHydraulicVersion = GeneralUtil.prefs.get(hydraulicVersionKey, "unknown");

        if (savedLauncherVersion == null || !savedLauncherVersion.equals(currentVersion)) {
            GeneralUtil.prefs.put(launcherVersionKey, currentVersion);
            savedLauncherVersion = GeneralUtil.prefs.get(launcherVersionKey, null);
        }

        System.out.println("Launcher sürümü: " + savedLauncherVersion);
    }

    private static boolean checkSingleInstance() {
        String pid = java.lang.management.ManagementFactory.getRuntimeMXBean().getName().split("@")[0];
        File lockFile = new File(System.getProperty("user.home"), ".onder_grup_launcher.pid");

        try {
            if (lockFile.exists()) {
                List<String> lines = Files.readAllLines(lockFile.toPath());
                if (!lines.isEmpty()) {
                    String existingPid = lines.get(0);
                    if (isProcessRunning(existingPid)) {
                        return false;
                    }
                }
            }

            Files.write(lockFile.toPath(), pid.getBytes());
            lockFile.deleteOnExit();
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    private static boolean isProcessRunning(String pid) {
        try {
            Process process = Runtime.getRuntime().exec("tasklist");
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains(pid)) {
                    return true;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static void focusApp(String windowTitle) {
        Platform.runLater(() -> {
            try {
                SystemTray tray = SystemTray.getSystemTray();
                for (TrayIcon icon : tray.getTrayIcons()) {
                    if (icon.getToolTip().equals("Önder Grup Launcher")) {
                        icon.displayMessage(
                                "Önder Grup Launcher",
                                "Restoring from system tray...",
                                TrayIcon.MessageType.INFO
                        );
                        icon.getActionListeners()[0].actionPerformed(null);
                        return;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            User32 user32 = User32.INSTANCE;
            WinDef.HWND hwnd = user32.FindWindow(null, windowTitle);
            if (hwnd != null) {
                user32.ShowWindow(hwnd, WinUser.SW_RESTORE);
                user32.SetForegroundWindow(hwnd);
            } else {
                System.out.println("Pencere bulunamadı.");
            }
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}