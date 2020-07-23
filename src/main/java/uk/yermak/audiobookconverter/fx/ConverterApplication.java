package uk.yermak.audiobookconverter.fx;/**
 * Created by Yermak on 06-Jan-18.
 */

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Screen;
import javafx.stage.Stage;
import org.apache.commons.lang3.StringUtils;
import org.controlsfx.control.Notifications;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.yermak.audiobookconverter.ConversionContext;
import uk.yermak.audiobookconverter.Version;

import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.Scanner;

public class ConverterApplication extends Application {
    private static JfxEnv env;
    static {
        File APP_DIR = new File(System.getenv("APPDATA"), Version.getVersionString());
        if (APP_DIR.exists() || APP_DIR.mkdir()) {
            System.setProperty("APPDATA", APP_DIR.getAbsolutePath());
        } else {
            System.setProperty("APPDATA", System.getProperty("user.home"));
        }
    }
    private static final ConversionContext context = new ConversionContext();

    public static void main(String[] args) {
        //-Dprism.allowhidpi=false
        //below does not work
//        System.setProperty("prism.allowhidpi", "false");
//        StdOutErrLog.tieSystemOutAndErrToLog();
        launch(args);
    }

    final static Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @Override
    public void start(Stage stage) {
        Parent root;

        logger.info("Initialising application");

        try {
            URL resource = ConverterApplication.class.getResource("/uk/yermak/audiobookconverter/fx/fxml_converter.fxml");
            root = FXMLLoader.load(resource);

            Scene scene = new Scene(root);
            stage.setTitle(Version.getVersionString());
            stage.setScene(scene);
            Screen primary = Screen.getPrimary();
            stage.setMinHeight(primary.getVisualBounds().getHeight() * 0.5);
            stage.setMinWidth(primary.getVisualBounds().getWidth() * 0.4);
            stage.show();
            env = new JfxEnv(scene, getHostServices());


            stage.setOnCloseRequest(event -> {
                logger.info("Closing application");
                ConverterApplication.getContext().stopConversions();
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.exit(0);
            });

            checkNewVersion();

        } catch (IOException e) {
            logger.error("Error initiating application", e);
            e.printStackTrace();
        }
    }

    public static void checkNewVersion() {
        try {
            String version = readStringFromURL("https://raw.githubusercontent.com/yermak/AudioBookConverter/master/version.txt");
            if (!Version.getVersionString().equals(StringUtils.trim(version))) {
                logger.info("New version found: {}", version);
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("New Version Available!");
                String s = "Would you like to download new version?";
                alert.setContentText(s);
                Optional<ButtonType> result = alert.showAndWait();
                if ((result.isPresent()) && (result.get() == ButtonType.OK)) {
                    ConverterApplication.getEnv().showDocument("https://github.com/yermak/AudioBookConverter/releases/latest");
                }
            }
        } catch (IOException e) {
            logger.info(e.getMessage());
        }
    }

    private static String readStringFromURL(String requestURL) throws IOException {
        try (Scanner scanner = new Scanner(new URL(requestURL).openStream(), StandardCharsets.UTF_8.toString())) {
            scanner.useDelimiter("\\A");
            return scanner.hasNext() ? scanner.next() : "";
        }
    }

    public static ConversionContext getContext() {
        return context;
    }

    public static JfxEnv getEnv() {
        return env;
    }

    public static void showNotification(String finalOutputDestination) {
        Notifications.create()
                .title("AudioBookConverter: Conversion is completed")
                .text(finalOutputDestination).show();
    }
}
