module uk.yermak.audiobookconverter {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.media;
    requires javafx.base;
    requires javafx.swing;
    requires javafx.graphics;

    requires org.controlsfx.controls;
    requires org.slf4j;
    requires org.apache.commons.io;
    requires org.apache.commons.lang3;
    requires ffmpeg;
    requires com.google.common;
    requires com.google.gson;
    requires java.prefs;
    requires ST4;

    opens uk.yermak.audiobookconverter to javafx.fxml,com.google.gson;
    opens uk.yermak.audiobookconverter.fx to javafx.fxml;
    opens uk.yermak.audiobookconverter.formats to com.google.gson;

    exports uk.yermak.audiobookconverter;
}