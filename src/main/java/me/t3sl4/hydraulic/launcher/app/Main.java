package me.t3sl4.hydraulic.launcher.app;

import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinUser;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Screen;
import javafx.stage.Stage;
import me.t3sl4.hydraulic.launcher.utils.FileUtil;
import me.t3sl4.hydraulic.launcher.utils.SceneUtil;
import me.t3sl4.hydraulic.launcher.utils.SystemVariables;
import me.t3sl4.util.os.OSUtil;

import java.awt.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.util.List;

public class Main extends Application {
    List<Screen> screens = Screen.getScreens();

    public static Screen defaultScreen;

    @Override
    public void start(Stage primaryStage) throws IOException {
        FileUtil.criticalFileSystem();

        Platform.setImplicitExit(false);

        if(!System.getProperty("os.name").toLowerCase().contains("win")) {
            if (!checkSingleInstance()) {
                System.out.println("Program zaten çalışıyor. Odaklanıyor...");
                focusApp("Canicula Launcher " + SystemVariables.getVersion());
                Platform.exit();
                return;
            }
        }

        OSUtil.updateLocalVersion(SystemVariables.PREF_NODE_NAME, SystemVariables.PREF_LAUNCHER_KEY, SystemVariables.getVersion());

        defaultScreen = screens.get(0);
        SceneUtil.openMainScreen(screens.get(0));

        System.out.println("Canicula launcher başlatıldı.");
    }

    private static boolean checkSingleInstance() {
        String pid = java.lang.management.ManagementFactory.getRuntimeMXBean().getName().split("@")[0];
        File lockFile = new File(System.getProperty("user.home"), ".canicula_launcher.pid");

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
                    if (icon.getToolTip().equals("Canicula Launcher")) {
                        icon.displayMessage(
                                "Canicula Launcher",
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