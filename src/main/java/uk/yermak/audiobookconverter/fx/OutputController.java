package uk.yermak.audiobookconverter.fx;

import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Slider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.yermak.audiobookconverter.Book;
import uk.yermak.audiobookconverter.ConversionContext;
import uk.yermak.audiobookconverter.MediaInfo;
import uk.yermak.audiobookconverter.OutputParameters;

import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.concurrent.Executors;

/**
 * Created by yermak on 08/09/2018.
 */
public class OutputController {
    final static Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    public static final Integer[] CHANNELS = {1, 2, 4, 6};
    public static final Integer[] CUTOFFS = {8000, 10000, 12000, 14000, 16000, 20000};
    public static final Integer[] FREQUENCIES = new Integer[]{8000, 11025, 12000, 16000, 22050, 24000, 32000, 44100, 48000, 64000, 88200, 96000};
    public static final Integer[] BITRATES = new Integer[]{8, 16, 24, 32, 40, 48, 56, 64, 80, 96, 112, 128, 144, 128, 144, 160, 192, 224, 256, 320};
    public static final Integer DEFAULT_CHANNELS = 2;
    public static final Integer DEFAULT_CUTOFF = 12000;
    public static final Integer DEFAULT_FREQUENCY = 44100;
    public static final Integer DEFAULT_BITRATE = 128;


    @FXML
    public ComboBox<Integer> cutoff;
    @FXML
    private ComboBox<String> volume;
/*
    @FXML
    private CheckBox auto;
*/

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

/*
        volume.getItems().addAll("100%", "200%", "300%");
        volume.getSelectionModel().select(0);
*/

        frequency.getItems().addAll(FREQUENCIES);
        frequency.getSelectionModel().select(DEFAULT_FREQUENCY);

        bitRate.getItems().addAll(BITRATES);
        bitRate.getSelectionModel().select(DEFAULT_BITRATE);

        channels.getItems().addAll(CHANNELS);
        channels.getSelectionModel().select(DEFAULT_CHANNELS);

        cutoff.getItems().addAll(CUTOFFS);
        cutoff.getSelectionModel().select(DEFAULT_CUTOFF);


        ConversionContext context = ConverterApplication.getContext();
        media = context.getMedia();

//        auto.selectedProperty().addListener((observable, oldValue, newValue) -> context.getOutputParameters().setAuto(newValue));
        bitRate.valueProperty().addListener((observable, oldValue, newValue) -> context.getOutputParameters().setBitRate(newValue));
        frequency.valueProperty().addListener((observable, oldValue, newValue) -> context.getOutputParameters().setFrequency(newValue));
        channels.valueProperty().addListener((observable, oldValue, newValue) -> context.getOutputParameters().setChannels(newValue));
        quality.valueProperty().addListener((observable, oldValue, newValue) -> context.getOutputParameters().setQuality((int) Math.round(newValue.doubleValue())));
        cutoff.valueProperty().addListener((observable, oldValue, newValue) -> context.getOutputParameters().setCutoff(newValue));
//        volume.valueProperty().addListener((observable, oldValue, newValue) -> context.getOutputParameters().setVolume(volume.getSelectionModel().getSelectedIndex() + 1));

/*
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

*/
        media.addListener((InvalidationListener) observable -> updateParameters(media));
        ConverterApplication.getContext().addBookChangeListener((observableValue, oldBook, newBook) -> {
            if (newBook != null) {
                newBook.addListener(observable -> updateParameters(newBook.getMedia()));
            }
        });
    }

    private void updateParameters(List<MediaInfo> media) {
        Book book = ConverterApplication.getContext().getBook();

        if (media.isEmpty() && book == null) {
            frequency.setValue(44100);
            bitRate.setValue(128);
            channels.setValue(2);
            quality.setValue(3);
            return;
        }

        Executors.newSingleThreadExecutor().submit(() -> {
            OutputParameters params = ConverterApplication.getContext().getOutputParameters();
            if (book != null) {
                params.updateAuto(book.getMedia());
//                book.addListener(observable -> updateParameters(book.getMedia()));
            } else {
                params.updateAuto(media);
            }
            Platform.runLater(() -> {
                frequency.setValue(findNearestMatch(params.getFrequency(), FREQUENCIES, DEFAULT_FREQUENCY));
                bitRate.setValue(findNearestMatch(params.getBitRate(), BITRATES, DEFAULT_BITRATE));
                channels.setValue(findNearestMatch(params.getChannels(), CHANNELS, DEFAULT_CHANNELS));
                quality.setValue(params.getQuality());
            });

        });


    }

    private static Integer findNearestMatch(int value, Integer[] array, int defaultValue) {
        for (Integer integer : array) {
            if (integer >= value)
                return integer;
        }
        return defaultValue;
    }
}


