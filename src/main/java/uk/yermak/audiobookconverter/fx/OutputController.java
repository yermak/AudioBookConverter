package uk.yermak.audiobookconverter.fx;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Slider;
import javafx.scene.control.Spinner;
import uk.yermak.audiobookconverter.OutputParameters;

/**
 * Created by yermak on 08/09/2018.
 */
public class OutputController {
    @FXML
    private Spinner<Integer> parts;
    @FXML
    private CheckBox auto;

    @FXML
    private Spinner<Integer> frequency;
    @FXML
    private Spinner<Integer> channels;
    @FXML
    private RadioButton cbr;
    @FXML
    private Spinner<Integer> bitRate;
    @FXML
    private RadioButton vbr;
    @FXML
    private Slider quality;

    public void cbr(ActionEvent actionEvent) {
        bitRate.setDisable(false);
        quality.setDisable(true);
        ConverterApplication.getContext().getOutputParameters().setCbr(true);

    }

    public void vbr(ActionEvent actionEvent) {
        bitRate.setDisable(true);
        quality.setDisable(false);
        ConverterApplication.getContext().getOutputParameters().setCbr(false);
    }

    @FXML
    private void initialize() {
        OutputParameters params = new OutputParameters();
        params.setAuto(auto.isSelected());
        params.setBitRate(bitRate.getValue());
        params.setFrequency(frequency.getValue());
        params.setChannels(channels.getValue());
        params.setCbr(cbr.isSelected());
        params.setParts(parts.getValue());

//        8000, 11025, 12000, 16000, 22050, 24000, 32000, 44100, 48000, 64000, 88200, and 96000
//        frequency.getValueFactory().


        ConverterApplication.getContext().setOutputParameters(params);
        auto.selectedProperty().addListener((observable, oldValue, newValue) -> {
            bitRate.setDisable(newValue);
            frequency.setDisable(newValue);
            channels.setDisable(newValue);
            quality.setDisable(newValue);
            cbr.setDisable(newValue);
            vbr.setDisable(newValue);

            if (!newValue) {
                if (cbr.isSelected()) {
                    bitRate.setDisable(false);
                    quality.setDisable(true);
                }
                if (vbr.isSelected()) {
                    bitRate.setDisable(true);
                    quality.setDisable(false);
                }
            }

        });

        boolean autoSelected = auto.isSelected();
        bitRate.setDisable(autoSelected);
        frequency.setDisable(autoSelected);
        channels.setDisable(autoSelected);
        quality.setDisable(autoSelected);
        cbr.setDisable(autoSelected);
        vbr.setDisable(autoSelected);

        if (!autoSelected) {
            if (cbr.isSelected()) {
                bitRate.setDisable(false);
                quality.setDisable(true);
            }
            if (vbr.isSelected()) {
                bitRate.setDisable(true);
                quality.setDisable(false);
            }
        }

        parts.getValueFactory().setValue(2);

        auto.selectedProperty().addListener(o -> params.setAuto(auto.isSelected()));
        bitRate.valueProperty().addListener(o -> params.setBitRate(bitRate.getValue()));
        frequency.valueProperty().addListener(o -> params.setFrequency(frequency.getValue()));
        channels.valueProperty().addListener(o -> params.setChannels(channels.getValue()));
        quality.valueProperty().addListener(o -> params.setQuality((int) Math.round(quality.getValue())));

    }
}
