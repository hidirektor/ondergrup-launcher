package me.t3sl4.hydraulic.launcher.app;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Screen;
import javafx.stage.Stage;
import me.t3sl4.hydraulic.launcher.utils.FileUtil;
import me.t3sl4.hydraulic.launcher.utils.SceneUtil;
import me.t3sl4.hydraulic.launcher.utils.GeneralUtil;
import me.t3sl4.hydraulic.launcher.utils.SystemVariables;
import me.t3sl4.hydraulic.launcher.utils.Version.UpdateCheckerService;

import java.io.IOException;
import java.util.List;
import java.util.prefs.Preferences;

public class Main extends Application {
    List<Screen> screens = Screen.getScreens();

    public static Screen defaultScreen;

    @Override
    public void start(Stage primaryStage) throws IOException {
        GeneralUtil.prefs = Preferences.userRoot().node(this.getClass().getName());
        FileUtil.criticalFileSystem();

        checkVersionFromPrefs();

        Platform.setImplicitExit(false);

        defaultScreen = screens.get(0);
        SceneUtil.openMainScreen(screens.get(0));

        UpdateCheckerService updateService = new UpdateCheckerService();
        updateService.start();

        System.out.println("Önder Grup Updater servisi başlatıldı.");
    }

    private void checkVersionFromPrefs() {
        GeneralUtil.prefs = Preferences.userRoot().node("onderGrupUpdater");

        // Launcher için key
        String launcherVersionKey = "launcher_version";
        // HydraulicTool için key
        String hydraulicVersionKey = "hydraulic_version";

        // Mevcut sürüm (Launcher için)
        String currentVersion = SystemVariables.CURRENT_VERSION;

        // Kaydedilmiş sürümleri oku
        String savedLauncherVersion = GeneralUtil.prefs.get(launcherVersionKey, null);
        String savedHydraulicVersion = GeneralUtil.prefs.get(hydraulicVersionKey, "unknown");

        // Launcher sürümünü kontrol et ve güncelle
        if (savedLauncherVersion == null || !savedLauncherVersion.equals(currentVersion)) {
            GeneralUtil.prefs.put(launcherVersionKey, currentVersion);
            savedLauncherVersion = GeneralUtil.prefs.get(launcherVersionKey, null);
        }

        // HydraulicTool sürümünü logla
        System.out.println("Launcher sürümü: " + savedLauncherVersion);
    }

    public static void main(String[] args) {
        launch(args);
    }
}