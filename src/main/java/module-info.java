module me.t3sl4.hydraulic.launcher {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires com.almasb.fxgl.all;
    requires java.prefs;
    requires java.logging;
    requires javafx.swing;
    requires org.json;
    requires okhttp3;
    requires org.yaml.snakeyaml;
    requires java.net.http;

    opens me.t3sl4.hydraulic.launcher to javafx.fxml;
    exports me.t3sl4.hydraulic.launcher;
    exports me.t3sl4.hydraulic.launcher.app;
    opens me.t3sl4.hydraulic.launcher.app to javafx.fxml;
    exports me.t3sl4.hydraulic.launcher.controller;
    opens me.t3sl4.hydraulic.launcher.controller to javafx.fxml;
}