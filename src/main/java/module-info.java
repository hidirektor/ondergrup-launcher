module me.t3sl4.hydraulic.launcher {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires java.logging;
    requires javafx.swing;
    requires okhttp3;
    requires org.yaml.snakeyaml;
    requires mslinks;
    requires com.sun.jna.platform;
    requires java.management;
    requires me.t3sl4.util.version;
    requires me.t3sl4.util.os;
    requires org.json;

    opens me.t3sl4.hydraulic.launcher to javafx.fxml;
    exports me.t3sl4.hydraulic.launcher;
    exports me.t3sl4.hydraulic.launcher.app;
    opens me.t3sl4.hydraulic.launcher.app to javafx.fxml;
    exports me.t3sl4.hydraulic.launcher.controller;
    opens me.t3sl4.hydraulic.launcher.controller to javafx.fxml;
}