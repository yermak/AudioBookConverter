package uk.yermak.audiobookconverter.fx;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.GridPane;
import uk.yermak.audiobookconverter.Utils;

import java.io.IOException;

/**
 * Created by yermak on 08-Feb-18.
 */
public class ProgressComponent extends GridPane {

    @FXML
    private Label title;

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
        title.setText(conversionProgress.fileName);
        conversionProgress.filesCount.addListener((observable, oldValue, newValue) -> Platform.runLater(() -> filesCount.setText(newValue)));
        conversionProgress.progress.addListener((observable, oldValue, newValue) -> Platform.runLater(() -> progressBar.progressProperty().set(newValue.doubleValue())));
        conversionProgress.size.addListener((observable, oldValue, newValue) -> Platform.runLater(() -> estimatedSize.setText(Utils.formatSize(newValue.longValue()))));
        conversionProgress.elapsed.addListener((observable, oldValue, newValue) -> Platform.runLater(() -> elapsedTime.setText(Utils.formatTime(newValue.longValue()))));
        conversionProgress.remaining.addListener((observable, oldValue, newValue) -> Platform.runLater(() -> remainingTime.setText(Utils.formatTime(newValue.longValue()))));
        conversionProgress.state.addListener((observable, oldValue, newValue) -> Platform.runLater(() -> state.setText(newValue)));
    }

}
