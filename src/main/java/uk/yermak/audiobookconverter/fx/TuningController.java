package uk.yermak.audiobookconverter.fx;

import javafx.fxml.FXML;
import javafx.scene.control.Slider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.yermak.audiobookconverter.Conversion;
import uk.yermak.audiobookconverter.ConversionSubscriber;
import uk.yermak.audiobookconverter.OutputParameters;

import java.lang.invoke.MethodHandles;

/**
 * Created by yermak on 08/09/2018.
 */
public class TuningController {
    final static Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @FXML
    private Slider volume;

    @FXML
    private void initialize() {
        volume.valueProperty().addListener((observable, oldValue, newValue) -> {
            logger.debug("Changed volume property to {}", newValue);
            OutputParameters params = ConverterApplication.getContext().getOutputParameters();
            params.setVolume((int) Math.round(newValue.doubleValue()));
        });
    }
}
