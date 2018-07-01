package uk.yermak.audiobookconverter.fx;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.GridPane;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.concurrent.TimeUnit;

/**
 * Created by yermak on 08-Feb-18.
 */
public class ProgressComponent extends GridPane {

    @FXML
    public Label elapsedTime;
    @FXML
    public Label remainingTime;
    @FXML
    public Label estimatedSize;
    @FXML
    private Label state;
    @FXML
    private Label filesCount;
    @FXML
    private ProgressBar progressBar;


    private ConversionProgress conversionProgress;

    public ProgressComponent() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(
                "progress.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);
        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
        progressBar.progressProperty().setValue(0);
        progressBar.setMaxWidth(Double.MAX_VALUE);
    }


    public void setConversionProgress(ConversionProgress conversionProgress) {
        conversionProgress.filesCount.addListener((observable, oldValue, newValue) -> Platform.runLater(() -> filesCount.setText(newValue)));
        conversionProgress.progress.addListener((observable, oldValue, newValue) -> Platform.runLater(() -> progressBar.progressProperty().set(newValue.doubleValue())));
        conversionProgress.size.addListener((observable, oldValue, newValue) -> Platform.runLater(() -> estimatedSize.setText(formatSize(newValue.longValue()))));
        conversionProgress.elapsed.addListener((observable, oldValue, newValue) -> Platform.runLater(() -> elapsedTime.setText(formatTime(newValue.longValue()))));
        conversionProgress.remaining.addListener((observable, oldValue, newValue) -> Platform.runLater(() -> remainingTime.setText(formatTime(newValue.longValue()))));
        conversionProgress.state.addListener((observable, oldValue, newValue) -> Platform.runLater(() -> state.setText(newValue)));
    }

    private static String formatTime(long millis) {
        String hms = String.format("%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(millis),
                TimeUnit.MILLISECONDS.toMinutes(millis) % TimeUnit.HOURS.toMinutes(1),
                TimeUnit.MILLISECONDS.toSeconds(millis) % TimeUnit.MINUTES.toSeconds(1));
        return hms;
    }

    private static String formatSize(long bytes) {
        if (bytes == -1L) {
            return "---";
        } else {
            DecimalFormat mbFormat = new DecimalFormat("0");
            return mbFormat.format((double) bytes / 1048576.0D) + " MB";
        }
    }
}
