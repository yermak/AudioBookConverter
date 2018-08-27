package uk.yermak.audiobookconverter.fx;

import javafx.beans.InvalidationListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import uk.yermak.audiobookconverter.MediaInfo;
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
    private ComboBox<Integer> frequency;
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
        ConverterApplication.getContext().setOutputParameters(params);


        frequency.getItems().addAll( 8000, 11025, 12000, 16000, 22050, 24000, 32000, 44100, 48000, 64000, 88200, 96000);
        frequency.getSelectionModel().select(8);


        auto.selectedProperty().addListener(o -> params.setAuto(auto.isSelected()));
        bitRate.valueProperty().addListener(o -> params.setBitRate(bitRate.getValue()));
        frequency.valueProperty().addListener(o -> params.setFrequency(frequency.getValue()));
        channels.valueProperty().addListener(o -> params.setChannels(channels.getValue()));
        quality.valueProperty().addListener(o -> params.setQuality((int) Math.round(quality.getValue())));


//        params.setAuto(auto.isSelected());
//        params.setBitRate(bitRate.getValue());
//        if (frequency.getValue()!=null) {
//            params.setFrequency(frequency.getValue());
//        }
//        params.setChannels(channels.getValue());
//        params.setCbr(cbr.isSelected());
//        params.setParts(parts.getValue());



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

        ObservableList<MediaInfo> media = ConverterApplication.getContext().getConversion().getMedia();
        media.addListener((InvalidationListener) observable -> updateParameters(media, media.isEmpty()));
    }

    private void updateParameters(ObservableList<MediaInfo> media, boolean empty) {
        if (empty){

        }else {
            OutputParameters outputParameters = ConverterApplication.getContext().getConversion().getOutputParameters();
            outputParameters.updateAuto(media);
            copyParameters(outputParameters);
        }
    }

    private void copyParameters(OutputParameters params) {
        frequency.setValue(params.getFrequency());
        bitRate.getValueFactory().setValue(params.getBitRate());
        channels.getValueFactory().setValue(params.getChannels());
        quality.setValue(params.getQuality());
    }
}
