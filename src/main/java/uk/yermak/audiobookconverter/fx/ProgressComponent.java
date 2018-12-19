package uk.yermak.audiobookconverter.fx;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.GridPane;
import uk.yermak.audiobookconverter.Conversion;
import uk.yermak.audiobookconverter.ProgressStatus;
import uk.yermak.audiobookconverter.Utils;

import java.io.IOException;

import static uk.yermak.audiobookconverter.ProgressStatus.PAUSED;

/**
 * Created by yermak on 08-Feb-18.
 */
public class ProgressComponent extends GridPane {

    @FXML
    private Label title;
    @FXML
    private Label elapsedTime;
    @FXML
    private Label remainingTime;
    @FXML
    private Label estimatedSize;
    @FXML
    private Label state;
    @FXML
    private Label filesCount;
    @FXML
    private ProgressBar progressBar;

    @FXML
    private Button pauseButton;

    @FXML
    private Button stopButton;


    private ConversionProgress conversionProgress;


    public ProgressComponent(ConversionProgress conversionProgress) {
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
        String fileName = conversionProgress.fileName;
        if (fileName.length() > 80) {
            fileName = fileName.substring(0, 80) + "...";
        }
        title.setText(fileName);

        pauseButton.setOnAction(event -> pause());
        stopButton.setOnAction(event -> stop());
        this.conversionProgress = conversionProgress;

        assignListeners(conversionProgress);
        Conversion conversion = conversionProgress.getConversion();
        conversion.addStatusChangeListener((observable, oldValue, newValue) -> updateButtons(newValue));
    }

    private void updateButtons(ProgressStatus newValue) {
        Platform.runLater(() -> {
            switch (newValue) {
                case PAUSED:
                    pauseButton.setText("Resume");
                    break;
                case FINISHED:
                    pauseButton.setDisable(true);
                    stopButton.setDisable(true);
                    break;
                case CANCELLED:
                    pauseButton.setDisable(true);
                    stopButton.setDisable(true);
                    break;
                case IN_PROGRESS:
                    pauseButton.setText("Pause");
                    pauseButton.setDisable(false);
                    stopButton.setDisable(false);
                    break;
                default: {
                }
            }
        });
    }

    private void stop() {
        conversionProgress.getConversion().stop();
    }

    private void pause() {
        Conversion conversion = conversionProgress.getConversion();
        if (conversion.getStatus().equals(PAUSED)) {
            conversion.resume();
        } else {
            conversion.pause();
        }
    }


    void assignListeners(ConversionProgress conversionProgress) {
        conversionProgress.filesCount.addListener((observable, oldValue, newValue) -> Platform.runLater(() -> filesCount.setText(newValue)));
        conversionProgress.progress.addListener((observable, oldValue, newValue) -> Platform.runLater(() -> progressBar.progressProperty().set(newValue.doubleValue())));
        conversionProgress.size.addListener((observable, oldValue, newValue) -> Platform.runLater(() -> estimatedSize.setText(Utils.formatSize(newValue.longValue()))));
        conversionProgress.elapsed.addListener((observable, oldValue, newValue) -> Platform.runLater(() -> elapsedTime.setText(Utils.formatTime(newValue.longValue()))));
        conversionProgress.remaining.addListener((observable, oldValue, newValue) -> Platform.runLater(() -> remainingTime.setText(Utils.formatTime(newValue.longValue()))));
        conversionProgress.state.addListener((observable, oldValue, newValue) -> Platform.runLater(() -> state.setText(newValue)));
    }

}
