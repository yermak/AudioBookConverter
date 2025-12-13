package uk.yermak.audiobookconverter.fx;

import javafx.application.Platform;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.ImageView;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.text.Font;
import javafx.stage.Screen;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.yermak.audiobookconverter.ConversionJob;
import uk.yermak.audiobookconverter.ProgressStatus;
import uk.yermak.audiobookconverter.Utils;
import uk.yermak.audiobookconverter.book.ArtWork;
import uk.yermak.audiobookconverter.AudiobookConverter;

import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.ResourceBundle;

import static uk.yermak.audiobookconverter.ProgressStatus.PAUSED;

/**
 * Created by yermak on 08-Feb-18.
 */
public class ProgressComponent extends GridPane {
    final static Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final Label title;
    private final Label elapsedTime;
    private final Label remainingTime;
    private final Label estimatedSize;
    private final Label state;
    private final Label filesCount;
    private final ProgressBar progressBar;

    private final Button pauseButton;

    private final Button stopButton;

    private final ImageView imageView;

    private final ConversionProgress conversionProgress;
    private final ResourceBundle resources;


    public ProgressComponent(ConversionProgress conversionProgress) {
        resources = AudiobookConverter.getBundle();
        setHgap(5);
        setVgap(2);

        ColumnConstraints col0 = new ColumnConstraints();
        ColumnConstraints col1 = new ColumnConstraints();
        col1.setHgrow(Priority.ALWAYS);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setHgrow(Priority.ALWAYS);
        ColumnConstraints col3 = new ColumnConstraints();
        ColumnConstraints col4 = new ColumnConstraints();
        ColumnConstraints col5 = new ColumnConstraints();
        getColumnConstraints().addAll(col0, col1, col2, col3, col4, col5);

        imageView = new ImageView();
        title = new Label(resources.getString("progress.label.title"));
        state = new Label();
        progressBar = new ProgressBar();
        Label convertedFilesLabel = new Label(resources.getString("progress.label.converted_files"));
        filesCount = new Label("0/0");
        Label estimatedSizeLabel = new Label(resources.getString("progress.label.estimated_size"));
        estimatedSize = new Label("0MB");
        pauseButton = new Button(resources.getString("progress.button.pause"));
        pauseButton.setTooltip(new javafx.scene.control.Tooltip(resources.getString("progress.tooltip.pause")));
        Label elapsedLabel = new Label(resources.getString("progress.label.time_elapsed"));
        elapsedTime = new Label("0:00:00");
        Label remainingLabel = new Label(resources.getString("progress.label.time_remaining"));
        remainingTime = new Label("0:00:00");
        stopButton = new Button(resources.getString("progress.button.stop"));
        stopButton.setTooltip(new javafx.scene.control.Tooltip(resources.getString("progress.tooltip.stop")));

        add(imageView, 0, 0, 1, 4);
        imageView.setPreserveRatio(true);
        imageView.setFitHeight(Screen.getPrimary().getVisualBounds().getHeight() * 0.075);

        title.setFont(Font.font("Arial Black", 12));
        add(title, 1, 0, 4, 1);
        GridPane.setValignment(title, VPos.TOP);
        add(state, 5, 0);

        add(progressBar, 1, 1, 5, 1);
        GridPane.setHalignment(progressBar, HPos.CENTER);

        add(convertedFilesLabel, 1, 2);
        add(filesCount, 2, 2, 3, 1);
        add(estimatedSizeLabel, 3, 2);
        add(estimatedSize, 4, 2);
        add(pauseButton, 5, 2);
        pauseButton.setMinWidth(Screen.getPrimary().getVisualBounds().getWidth() * 0.05);

        add(elapsedLabel, 1, 3);
        add(elapsedTime, 2, 3);
        add(remainingLabel, 3, 3);
        add(remainingTime, 4, 3);
        add(stopButton, 5, 3);
        stopButton.setMinWidth(Screen.getPrimary().getVisualBounds().getWidth() * 0.05);

        pauseButton.setOnAction(event -> pause());
        stopButton.setOnAction(event -> stop());

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
