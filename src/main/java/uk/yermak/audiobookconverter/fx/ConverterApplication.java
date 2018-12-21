package uk.yermak.audiobookconverter.fx;/**
 * Created by Yermak on 06-Jan-18.
 */

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;
import uk.yermak.audiobookconverter.ConversionContext;
import uk.yermak.audiobookconverter.Version;

import java.io.IOException;
import java.net.URL;

public class ConverterApplication extends Application {
    private static JfxEnv env;
    private static ConversionContext context = new ConversionContext();

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        Parent root = null;
        try {
            URL resource = ConverterApplication.class.getResource("/uk/yermak/audiobookconverter/fx/fxml_converter.fxml");
            root = FXMLLoader.load(resource);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Scene scene = new Scene(root);
        stage.setTitle(Version.getVersionString());
        stage.setScene(scene);
        Screen primary = Screen.getPrimary();
        stage.setMinHeight(primary.getVisualBounds().getHeight() * 0.5);
        stage.setMinWidth(primary.getVisualBounds().getWidth() * 0.4);
        stage.show();
        env = new JfxEnv(scene, getHostServices());


        stage.setOnCloseRequest(event -> {
            ConverterApplication.getContext().stopConversions();
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.exit(0);
        });


    }

    public static ConversionContext getContext() {
        return context;
    }

    public static JfxEnv getEnv() {
        return env;
    }
}
