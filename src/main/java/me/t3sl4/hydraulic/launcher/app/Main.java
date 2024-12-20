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

        try {
            Thread.currentThread().join();
        } catch (InterruptedException e) {
            System.err.println("Servis durduruldu.");
        }
    }

    private void checkVersionFromPrefs() {
        GeneralUtil.prefs = Preferences.userRoot().node(this.getClass().getName());

        String versionKey = "onderGrup_hydraulic_launcher_versionNumber";
        String currentVersion = SystemVariables.CURRENT_VERSION;
        String savedVersion = GeneralUtil.prefs.get(versionKey, null);

        if (savedVersion == null || !savedVersion.equals(currentVersion)) {
            GeneralUtil.prefs.put(versionKey, currentVersion);
            System.out.println("Version updated in preferences: " + currentVersion);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}