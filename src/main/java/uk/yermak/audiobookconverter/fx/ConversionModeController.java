package uk.yermak.audiobookconverter.fx;

import javafx.beans.property.SimpleObjectProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import uk.yermak.audiobookconverter.Conversion;
import uk.yermak.audiobookconverter.ConversionMode;
import uk.yermak.audiobookconverter.ConversionSubscriber;
import uk.yermak.audiobookconverter.ProgressStatus;

import static uk.yermak.audiobookconverter.ConversionMode.*;

/**
 * Created by Yermak on 04-Feb-18.
 */
public class ConversionModeController implements ConversionSubscriber {

    private SimpleObjectProperty<ConversionMode> mode = new SimpleObjectProperty<>(this, "mode", PARALLEL);

    @FXML
    public RadioButton parallel;
    @FXML
    public RadioButton batch;
    @FXML
    public RadioButton join;
    @FXML
    private ToggleGroup modeGroup;

    @FXML
    public void initialize() {
        Conversion conversion = ConverterApplication.getContext().registerForConversion(this);
        resetForNewConversion(conversion);
    }

    public void resetForNewConversion(Conversion conversion) {
        mode.addListener((observable, oldValue, newValue) -> conversion.setMode(newValue));
        conversion.addStatusChangeListener((observable, oldValue, newValue) -> {
            boolean disable = newValue.equals(ProgressStatus.IN_PROGRESS);
            parallel.setDisable(disable);
            batch.setDisable(disable);
            join.setDisable(disable);
        });
    }

    public void parallelMode(ActionEvent actionEvent) {
        mode.set(PARALLEL);
    }

    public void batchMode(ActionEvent actionEvent) {
        mode.set(BATCH);
    }

    public void joinMode(ActionEvent actionEvent) {
        mode.set(SINGLE);
    }
}
