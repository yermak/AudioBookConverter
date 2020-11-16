package uk.yermak.audiobookconverter.fx;/**
 * Created by Yermak on 06-Jan-18.
 */

import javafx.application.Application;
import javafx.application.Platform;
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
import java.util.concurrent.Executors;

public class ConverterApplication extends Application {
    private static JfxEnv env;

    //TODO clean-up
    static {
        initAppHome();
    }

    public static void initAppHome() {
        String appdata = System.getenv("APPDATA");
        File appDir;
        if (appdata != null) {
            appDir = new File(appdata, Version.getVersionString());
        } else {
            appDir = new File(System.getProperty("user.home"), Version.getVersionString());
        }
        if (appDir.exists() || appDir.mkdir()) {
            System.setProperty("APP_HOME", appDir.getAbsolutePath());
        } else {
            System.setProperty("APP_HOME", System.getProperty("user.home"));
        }
    }


    private static final ConversionContext context=new ConversionContext();

public static void main(String[]args){
        //-Dprism.allowhidpi=false
        //below does not work
//        System.setProperty("prism.allowhidpi", "false");
//        StdOutErrLog.tieSystemOutAndErrToLog();
        launch(args);
        }

final static Logger logger=LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

public static void checkNewVersion(){
        Executors.newSingleThreadExecutor().submit(new VersionChecker());
        }

@Override
public void start(Stage stage){
        Parent root;

        logger.info("Initialising application");

        try{
        URL resource=ConverterApplication.class.getResource("/uk/yermak/audiobookconverter/fx/fxml_converter.fxml");
        root=FXMLLoader.load(resource);

        Scene scene=new Scene(root);
        stage.setTitle(Version.getVersionString());
        stage.setScene(scene);
        Screen primary=Screen.getPrimary();
//            stage.setMinHeight(primary.getVisualBounds().getHeight() * 0.7);
//            stage.setMinWidth(primary.getVisualBounds().getWidth() * 0.4);
        stage.show();
        env=new JfxEnv(scene,getHostServices());


        stage.setOnCloseRequest(event->{
        logger.info("Closing application");
        ConverterApplication.getContext().stopConversions();
        try{
        Thread.sleep(500);
        }catch(InterruptedException e){
        e.printStackTrace();
        }
        System.exit(0);
        });

        checkNewVersion();

        }catch(IOException e){
        logger.error("Error initiating application",e);
        e.printStackTrace();
        }
        }


private static String readStringFromURL(String requestURL)throws IOException{
        try(Scanner scanner=new Scanner(new URL(requestURL).openStream(),StandardCharsets.UTF_8.toString())){
        scanner.useDelimiter("\\A");
        return scanner.hasNext()?scanner.next():"";
        }
        }

public static ConversionContext getContext(){
        return context;
        }

public static JfxEnv getEnv(){
        return env;
        }

public static void showNotification(String finalOutputDestination){
        Notifications.create()
        .title("AudioBookConverter: Conversion is completed")
        .text(finalOutputDestination).show();
        }

static class VersionChecker implements Runnable {
    @Override
    public void run() {
        try {
            String version = readStringFromURL("https://raw.githubusercontent.com/yermak/AudioBookConverter/master/version.txt");
            if (!Version.getVersionString().equals(StringUtils.trim(version))) {
                logger.info("New version found: {}", version);
                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                    alert.setTitle("New Version Available!");
                    alert.setContentText("Would you like to download new version?");
                    Optional<ButtonType> result = alert.showAndWait();
                    if ((result.isPresent()) && (result.get() == ButtonType.OK)) {
                        ConverterApplication.getEnv().showDocument("https://github.com/yermak/AudioBookConverter/releases/latest");
                    }
                });
            }
        } catch (IOException e) {
            logger.info(e.getMessage());
        }
    }
}
}
