package uk.yermak.audiobookconverter.fx;

import javafx.beans.property.SimpleObjectProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import uk.yermak.audiobookconverter.Conversion;
import uk.yermak.audiobookconverter.ConversionMode;
import uk.yermak.audiobookconverter.ConversionSubscriber;

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

    private Conversion conversion;

    @FXML
    public void initialize() {
        Conversion conversion = ConverterApplication.getContext().registerForConversion(this);
        resetForNewConversion(conversion);
        mode.addListener((observable, oldValue, newValue) -> conversion.setMode(newValue));
    }

    public void resetForNewConversion(Conversion conversion) {
        this.conversion = conversion;
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
