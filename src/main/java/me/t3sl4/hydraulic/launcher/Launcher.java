package me.t3sl4.hydraulic.launcher;

import me.t3sl4.hydraulic.launcher.app.Main;
import me.t3sl4.util.os.desktop.DesktopUtil;

public class Launcher {
    public static void main(String[] args) {
        DesktopUtil.configureSystemProperties();

        Main.main(args);
    }
}