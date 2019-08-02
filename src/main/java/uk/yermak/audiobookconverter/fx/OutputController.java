package uk.yermak.audiobookconverter.fx;

import javafx.beans.InvalidationListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import uk.yermak.audiobookconverter.Conversion;
import uk.yermak.audiobookconverter.ConversionSubscriber;
import uk.yermak.audiobookconverter.MediaInfo;
import uk.yermak.audiobookconverter.OutputParameters;

/**
 * Created by yermak on 08/09/2018.
 */
public class OutputController implements ConversionSubscriber {
    @FXML
    public ComboBox<Integer> cutoff;
    @FXML
    private Spinner<Integer> parts;
    @FXML
    private CheckBox auto;

    @FXML
    private ComboBox<Integer> frequency;
    @FXML
    private ComboBox<Integer> channels;
    @FXML
    private RadioButton cbr;
    @FXML
    private ComboBox<Integer> bitRate;
    @FXML
    private RadioButton vbr;
    @FXML
    private Slider quality;
    private OutputParameters params = new OutputParameters();
    private ObservableList<MediaInfo> media;

    public void cbr(ActionEvent actionEvent) {
        bitRate.setDisable(false);
        cutoff.setDisable(false);
        quality.setDisable(true);
        params.setCbr(true);

    }

    public void vbr(ActionEvent actionEvent) {
        bitRate.setDisable(true);
        cutoff.setDisable(true);
        quality.setDisable(false);
        params.setCbr(false);
    }

    @FXML
    private void initialize() {

        frequency.getItems().addAll(8000, 11025, 12000, 16000, 22050, 24000, 32000, 44100, 48000, 64000, 88200, 96000);
        frequency.getSelectionModel().select(new Integer(44100));

        bitRate.getItems().addAll(8, 16, 24, 32, 40, 48, 56, 64, 80, 96, 112, 128, 144, 128, 144, 160, 192, 224, 256, 320);
        bitRate.getSelectionModel().select(new Integer(128));

        channels.getItems().addAll(1, 2, 4, 6);
        channels.getSelectionModel().select(new Integer(2));

        cutoff.getItems().addAll(8000, 10000, 12000, 14000, 16000, 20000);
        cutoff.getSelectionModel().select(new Integer(12000));

        resetForNewConversion(ConverterApplication.getContext().registerForConversion(this));

        auto.selectedProperty().addListener((observable, oldValue, newValue) -> params.setAuto(newValue));
        bitRate.valueProperty().addListener((observable, oldValue, newValue) -> params.setBitRate(newValue));
        frequency.valueProperty().addListener((observable, oldValue, newValue) -> params.setFrequency(newValue));
        channels.valueProperty().addListener((observable, oldValue, newValue) -> params.setChannels(newValue));
        quality.valueProperty().addListener((observable, oldValue, newValue) -> params.setQuality((int) Math.round(newValue.doubleValue())));
        cutoff.valueProperty().addListener((observable, oldValue, newValue) -> params.setCutoff(newValue));


        auto.selectedProperty().addListener((observable, oldValue, newValue) -> {

            bitRate.setDisable(newValue);
            frequency.setDisable(newValue);
            channels.setDisable(newValue);
            quality.setDisable(newValue);
            cbr.setDisable(newValue);
            vbr.setDisable(newValue);
            cutoff.setDisable(newValue);

            if (!newValue) {
                if (cbr.isSelected()) {
                    bitRate.setDisable(false);
                    cutoff.setDisable(false);
                    quality.setDisable(true);

                }
                if (vbr.isSelected()) {
                    bitRate.setDisable(true);
                    cutoff.setDisable(false);
                    quality.setDisable(false);
                }
            }
        });

        media.addListener((InvalidationListener) observable -> updateParameters(media, media.isEmpty()));
    }

    private void updateParameters(ObservableList<MediaInfo> media, boolean empty) {
        if (!empty) {
            params.updateAuto(media);
            copyParameters(params);
        }
    }

    private void copyParameters(OutputParameters params) {
        frequency.setValue(params.getFrequency());
        bitRate.setValue(params.getBitRate());
        channels.setValue(params.getChannels());
        quality.setValue(params.getQuality());
    }

    @Override
    public void resetForNewConversion(Conversion conversion) {
        conversion.setOutputParameters(params);
        media = conversion.getMedia();
        media.addListener((InvalidationListener) observable -> updateParameters(media, media.isEmpty()));
    }
}
