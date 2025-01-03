package me.t3sl4.hydraulic.launcher;

import me.t3sl4.hydraulic.launcher.app.Main;

public class Launcher {
    public static void main(String[] args) {
        System.setProperty("prism.allowhidpi", "false");

        System.setProperty("java.util.logging.level", "WARNING");

        Main.main(args);
    }
}