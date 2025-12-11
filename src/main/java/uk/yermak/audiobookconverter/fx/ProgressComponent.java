package uk.yermak.audiobookconverter.fx;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.yermak.audiobookconverter.ConversionJob;
import uk.yermak.audiobookconverter.ProgressStatus;
import uk.yermak.audiobookconverter.Utils;
import uk.yermak.audiobookconverter.book.ArtWork;
import uk.yermak.audiobookconverter.AudiobookConverter;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.ResourceBundle;

import static uk.yermak.audiobookconverter.ProgressStatus.PAUSED;

/**
 * Created by yermak on 08-Feb-18.
 */
public class ProgressComponent extends GridPane {
    final static Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

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

    @FXML
    private ImageView imageView;


    private final ConversionProgress conversionProgress;
    private final ResourceBundle resources;


    public ProgressComponent(ConversionProgress conversionProgress) {
        resources = AudiobookConverter.getBundle();
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("progress.fxml"), resources);
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);
        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }

        List<ArtWork> posters = conversionProgress.getConversionJob().getConversionGroup().getPosters();
        if (!posters.isEmpty()) {
            imageView.setImage(posters.get(0).image());
        }

        progressBar.progressProperty().setValue(0);
        progressBar.setMaxWidth(Double.MAX_VALUE);
        String fileName = conversionProgress.getConversionJob().getOutputDestination();
        if (fileName.length() > 80) {
            fileName = fileName.substring(0, 80) + "...";
        }
        title.setText(fileName);
        filesCount.setText(conversionProgress.filesCount.get());

        pauseButton.setOnAction(event -> pause());
        stopButton.setOnAction(event -> stop());
        this.conversionProgress = conversionProgress;

        assignListeners(conversionProgress);
        conversionProgress.getConversionJob().addStatusChangeListener((observable, oldValue, newValue) -> updateButtons(newValue));
    }


    private void updateButtons(ProgressStatus newValue) {
        Platform.runLater(() -> {
            switch (newValue) {
                case PAUSED -> pauseButton.setText(resources.getString("progress.button.resume"));
                case FINISHED -> {
                    pauseButton.setDisable(true);
                    stopButton.setDisable(true);
                }
                case CANCELLED -> {
                    pauseButton.setDisable(true);
                    stopButton.setDisable(true);
                }
                case IN_PROGRESS -> {
                    pauseButton.setText(resources.getString("progress.button.pause"));
                    pauseButton.setDisable(false);
                    stopButton.setDisable(false);
                }
                default -> {
                }
            }
        });
    }

    private void stop() {
        conversionProgress.getConversionJob().stop();
    }

    private void pause() {
        ConversionJob conversionGroup = conversionProgress.getConversionJob();
        if (conversionGroup.getStatus().equals(PAUSED)) {
            conversionGroup.resume();
        } else {
            conversionGroup.pause();
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

    public boolean isOver() {
        return conversionProgress.getConversionJob().getStatus().isOver();
    }
}
