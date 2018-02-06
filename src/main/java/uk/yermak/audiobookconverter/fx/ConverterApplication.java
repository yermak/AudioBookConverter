package uk.yermak.audiobookconverter.fx;/**
 * Created by Yermak on 06-Jan-18.
 */

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.io.IOException;
import java.net.URL;

public class ConverterApplication extends Application {
    static ConverterApplication instance = null;
    private Scene scene;

    public static void main(String[] args) {
        launch(args);
    }

    public static Window getWindow() {
        return instance.scene.getWindow();
    }

    @Override
    public void start(Stage stage) {
        instance = this;

        Parent root = null;
        try {
            URL resource = getClass().getClassLoader().getResource("uk/yermak/audiobookconverter/fx/fxml_converter.fxml");
            root = FXMLLoader.load(resource);
        } catch (IOException e) {
            e.printStackTrace();
        }

        scene = new Scene(root);

        stage.setTitle("AudioBookConverter V2");
        stage.setScene(scene);
        Screen primary = Screen.getPrimary();
        stage.setMinHeight(primary.getVisualBounds().getHeight() * 0.5);
        stage.setMinWidth(primary.getVisualBounds().getWidth() * 0.3);
        stage.show();
    }
}
