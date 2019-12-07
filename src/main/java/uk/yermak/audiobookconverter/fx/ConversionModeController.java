package uk.yermak.audiobookconverter.fx;

import javafx.beans.property.SimpleObjectProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.yermak.audiobookconverter.ConversionMode;

import java.lang.invoke.MethodHandles;

import static uk.yermak.audiobookconverter.ConversionMode.*;

/**
 * Created by Yermak on 04-Feb-18.
 */
public class ConversionModeController {
    final static Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

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
        mode.addListener((observable, oldValue, newValue) -> ConverterApplication.getContext().setMode(newValue));
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
