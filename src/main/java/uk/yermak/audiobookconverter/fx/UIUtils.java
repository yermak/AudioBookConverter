package uk.yermak.audiobookconverter.fx;

import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.yermak.audiobookconverter.AppProperties;

import java.io.File;
import java.lang.invoke.MethodHandles;

public final class UIUtils {
    private final Logger logger;


    private Logger logger() {
        return this.logger;
    }

    public Object createFileChooser(final String title) {
        FileChooser fileChooser = new FileChooser();
        String sourceFolder = AppProperties.getProperty("source.folder");
        fileChooser.setInitialDirectory(this.getInitialDirecotory(sourceFolder));
        fileChooser.setTitle(title);
        fileChooser.getExtensionFilters().addAll(
                new ExtensionFilter("jpg",
                        "*.jpg", "*.jpeg", "*.jfif"),
                new ExtensionFilter("png", "*.png"),
                new ExtensionFilter("bmp", "*.bmp"));
        File file = fileChooser.showOpenDialog(ConverterApplication.getEnv().getWindow());
        this.logger().debug("Opened dialog in folder: {}", new Object[]{sourceFolder});
        return file;
    }

    public File getInitialDirecotory(String sourceFolder) {
        while (sourceFolder != null) {
            File file = new File(sourceFolder);
            if (file.exists()) {
                return file;
            } else {
                sourceFolder = file.getParent();
            }

        }

        return new File(System.getProperty("user.home"));
    }

    private UIUtils() {
        this.logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    }
}

        