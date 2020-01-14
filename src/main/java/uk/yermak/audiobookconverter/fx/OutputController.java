package uk.yermak.audiobookconverter.fx;

import javafx.beans.InvalidationListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.yermak.audiobookconverter.ConversionContext;
import uk.yermak.audiobookconverter.MediaInfo;
import uk.yermak.audiobookconverter.OutputParameters;

import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;

/**
 * Created by yermak on 08/09/2018.
 */
public class OutputController {
    final static Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @FXML
    public ComboBox<Integer> cutoff;
    @FXML
    private ComboBox<String> volume;
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
    private ObservableList<MediaInfo> media;

    public void cbr(ActionEvent actionEvent) {
        bitRate.setDisable(false);
        cutoff.setDisable(false);
        quality.setDisable(true);
        ConverterApplication.getContext().getOutputParameters().setCbr(true);

    }

    public void vbr(ActionEvent actionEvent) {
        bitRate.setDisable(true);
        cutoff.setDisable(true);
        quality.setDisable(false);
        ConverterApplication.getContext().getOutputParameters().setCbr(false);
    }

    @FXML
    private void initialize() {

        volume.getItems().addAll("100%", "200%", "300%");
        volume.getSelectionModel().select(0);

        frequency.getItems().addAll(8000, 11025, 12000, 16000, 22050, 24000, 32000, 44100, 48000, 64000, 88200, 96000);
        frequency.getSelectionModel().select(new Integer(44100));

        bitRate.getItems().addAll(8, 16, 24, 32, 40, 48, 56, 64, 80, 96, 112, 128, 144, 128, 144, 160, 192, 224, 256, 320);
        bitRate.getSelectionModel().select(new Integer(128));

        channels.getItems().addAll(1, 2, 4, 6);
        channels.getSelectionModel().select(new Integer(2));

        cutoff.getItems().addAll(8000, 10000, 12000, 14000, 16000, 20000);
        cutoff.getSelectionModel().select(new Integer(12000));


        ConversionContext context = ConverterApplication.getContext();
        media = context.getMedia();

        auto.selectedProperty().addListener((observable, oldValue, newValue) -> context.getOutputParameters().setAuto(newValue));
        bitRate.valueProperty().addListener((observable, oldValue, newValue) -> context.getOutputParameters().setBitRate(newValue));
        frequency.valueProperty().addListener((observable, oldValue, newValue) -> context.getOutputParameters().setFrequency(newValue));
        channels.valueProperty().addListener((observable, oldValue, newValue) -> context.getOutputParameters().setChannels(newValue));
        quality.valueProperty().addListener((observable, oldValue, newValue) -> context.getOutputParameters().setQuality((int) Math.round(newValue.doubleValue())));
        cutoff.valueProperty().addListener((observable, oldValue, newValue) -> context.getOutputParameters().setCutoff(newValue));
        volume.valueProperty().addListener((observable, oldValue, newValue) -> context.getOutputParameters().setVolume(volume.getSelectionModel().getSelectedIndex() + 1));

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
            updateParameters(media, media.isEmpty());
        });

        media.addListener((InvalidationListener) observable -> updateParameters(media, media.isEmpty()));
    }

    private void updateParameters(ObservableList<MediaInfo> media, boolean empty) {
        if (!empty) {
            OutputParameters params = ConverterApplication.getContext().getOutputParameters();
//            params.updateAuto(media);
            frequency.setValue(params.getFrequency());
            bitRate.setValue(params.getBitRate());
            channels.setValue(params.getChannels());
            quality.setValue(params.getQuality());
        }
    }

}
