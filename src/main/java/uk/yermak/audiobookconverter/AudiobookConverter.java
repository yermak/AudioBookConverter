package uk.yermak.audiobookconverter;/**
 * Created by Yermak on 06-Jan-18.
 */

import javafx.application.Application;
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
import uk.yermak.audiobookconverter.fx.ConversionContext;
import uk.yermak.audiobookconverter.fx.FilesController;
import uk.yermak.audiobookconverter.fx.JfxEnv;
import uk.yermak.audiobookconverter.fx.WizardDialog;

import java.io.File;
import java.lang.invoke.MethodHandles;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.Executors;

public class AudiobookConverter extends Application {
    private static JfxEnv env;
    private static ResourceBundle bundle;

    //TODO clean-up
    static {
        initAppHome();
    }

    public static void initAppHome() {
        String appdata = System.getenv("APPDATA");
        File appDir;
        if (appdata != null) {
            File file = new File("AudioBookConverter", Version.getVersionString());
            appDir = new File(appdata, file.getPath());
        } else {
            File file = new File(".abc", Version.getVersionString());
            appDir = new File(System.getProperty("user.home"), file.getPath());
        }
        if (appDir.exists() || appDir.mkdirs()) {
            System.setProperty("APP_HOME", appDir.getAbsolutePath());
        } else {
            System.setProperty("APP_HOME", System.getProperty("user.home"));
        }
    }


    private static final ConversionContext context = new ConversionContext();

    public static void main(String[] args) {
        //GitHub certificate issue
        System.setProperty("https.protocols", "TLSv1,TLSv1.1,TLSv1.2");

        //-Dprism.allowhidpi=false
        //below does not work
//        System.setProperty("prism.allowhidpi", "false");
//        StdOutErrLog.tieSystemOutAndErrToLog();
        launch(args);
    }

    final static Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public static void checkNewVersion(ResourceBundle bundle) {
        Executors.newSingleThreadExecutor().submit(new VersionChecker(bundle));
    }

    private ResourceBundle getBundleWithFallback(Locale locale) {
        ResourceBundle bundle = null;
        ResourceBundle.Control control = ResourceBundle.Control.getControl(ResourceBundle.Control.FORMAT_DEFAULT);
        try {
            bundle = ResourceBundle.getBundle("locales/messages", locale, control);
        } catch (MissingResourceException e) {
            logger.error("Error getBundle", e);
        }
        return bundle != null ? bundle : ResourceBundle.getBundle("locales/messages", new Locale("en"), control);
    }

    @Override
    public void start(Stage stage) {
        Parent root;

        logger.info("Initialising application");

        Settings settings = Settings.loadSetting();
        Locale selectedLocale = Optional.ofNullable(settings.getLanguage())
                .filter(StringUtils::isNotBlank)
                .map(Locale::forLanguageTag)
                .orElse(Locale.getDefault());
        Locale.setDefault(selectedLocale);
        bundle = getBundleWithFallback(selectedLocale);

        try {
            root = new FilesController(bundle);

            Scene scene = new Scene(root);
            stage.setTitle("AudioBookConverter-"+Version.getVersionString());
            stage.setScene(scene);
            Screen primary = Screen.getPrimary();
            stage.setMinHeight(primary.getVisualBounds().getHeight() * 0.7);
            stage.setMinWidth(primary.getVisualBounds().getWidth() * 0.4);
            env = new JfxEnv(scene, getHostServices());

            boolean dark = settings.isDarkMode();
            if (dark) {
                env.setDarkMode(dark);
            }

            stage.show();

            stage.setOnCloseRequest(event -> {
                logger.info("Closing application");
                AudiobookConverter.getContext().stopConversions();
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.exit(0);
            });

            checkNewVersion(bundle);

            if (settings.isShowHints()) {
                loadHints();
            }

        } catch (IOException e) {
            logger.error("Error initiating application", e);
            e.printStackTrace();
        }
    }

    public static ResourceBundle getBundle() {
        return bundle;
    }

    public static void loadHints() throws IOException {
        List<String> hints = readListFromURL("https://raw.githubusercontent.com/yermak/AudioBookConverter/version/hints.txt");
        if (!hints.isEmpty()) {
            Collections.shuffle(hints);
            WizardDialog wizardDialog = new WizardDialog(hints);
            wizardDialog.show();
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
        ResourceBundle resources = getBundle();
        Notifications.create()
                .title(resources.getString("notification.conversion_complete.title"))
                .text(MessageFormat.format(resources.getString("notification.conversion_complete.text"), finalOutputDestination))
                .show();
    }

    private static List<String> readListFromURL(String requestURL) throws IOException {
        try {
            try (Scanner scanner = new Scanner(new URL(requestURL).openStream(), StandardCharsets.UTF_8.toString())) {
                scanner.useDelimiter("\\n");
                List<String> result = new ArrayList<>();
                while (scanner.hasNext()) {
                    result.add(scanner.next());
                }
                return result;
            }
        }catch (Exception e){
            logger.error("Failed to read hints", e);
            return Collections.emptyList();
        }
    }

    static class VersionChecker implements Runnable {
        private final ResourceBundle resources;

        VersionChecker(ResourceBundle resources) {
            this.resources = resources;
        }

        @Override
        public void run() {
            try {
                String platform = Platform.current.loadAppProperties().getProperty("platform");
                if (platform == null) platform = "version";
                if ("steam".equals(platform)) return;
                String version = readStringFromURL("https://raw.githubusercontent.com/yermak/AudioBookConverter/version/" + platform + ".txt");
                if (!Version.getVersionString().equals(StringUtils.trim(version))) {
                    logger.info("New version found: {}", version);
                    javafx.application.Platform.runLater(() -> {
                        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                        alert.setTitle(resources.getString("alert.update_available.title"));
                        alert.setContentText(resources.getString("alert.update_available.message"));
                        Optional<ButtonType> result = alert.showAndWait();
                        if ((result.isPresent()) && (result.get() == ButtonType.OK)) {
                            AudiobookConverter.getEnv().showDocument("https://store.steampowered.com/app/1529240/AudioBookConverter/");
                        }
                    });
                }
            } catch (IOException e) {
                logger.info(e.getMessage());
            }
        }
    }
}
