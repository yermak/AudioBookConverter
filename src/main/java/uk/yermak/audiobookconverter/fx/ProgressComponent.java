package uk.yermak.audiobookconverter.fx;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.GridPane;
import javafx.util.StringConverter;
import javafx.util.converter.NumberStringConverter;
import uk.yermak.audiobookconverter.ProgressStatus;

import java.io.IOException;

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
        progressBar.setMinWidth(550);
    }


    public void setConversionProgress(ConversionProgress conversionProgress) {
        this.conversionProgress = conversionProgress;
        filesCount.textProperty().bindBidirectional(conversionProgress.filesCount);
        progressBar.progressProperty().bindBidirectional(conversionProgress.progress);
        estimatedSize.textProperty().bindBidirectional(conversionProgress.size, new NumberStringConverter());
        elapsedTime.textProperty().bindBidirectional(conversionProgress.elapsed, new NumberStringConverter());
        remainingTime.textProperty().bindBidirectional(conversionProgress.remaining, new NumberStringConverter());
        state.textProperty().bindBidirectional(conversionProgress.state, new StateStringConverter());
    }

    private static class StateStringConverter extends StringConverter<ProgressStatus> {
        @Override
        public String toString(ProgressStatus status) {
            return status.toString();
        }

        @Override
        public ProgressStatus fromString(String value) {
            return ProgressStatus.valueOf(value);
        }
    }
}
