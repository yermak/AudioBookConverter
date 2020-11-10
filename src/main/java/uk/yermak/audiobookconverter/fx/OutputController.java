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
import uk.yermak.audiobookconverter.*;

import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import static uk.yermak.audiobookconverter.OutputParameters.*;

/**
 * Created by yermak on 08/09/2018.
 */
public class OutputController {
    final static Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    public static final Integer[] CHANNELS = {1, 2, 4, 6};
    public static final Integer[] CUTOFFS = {8000, 10000, 12000, 14000, 16000, 20000};
    public static final Integer[] FREQUENCIES = new Integer[]{8000, 11025, 12000, 16000, 22050, 24000, 32000, 44100, 48000, 64000, 88200, 96000};
    public static final Integer[] BITRATES = new Integer[]{8, 16, 24, 32, 40, 48, 56, 64, 80, 96, 112, 128, 144, 128, 144, 160, 192, 224, 256, 320};


    @FXML
    public ComboBox<Format> outputFormatBox;
    @FXML
    public ComboBox<String> presetBox;

    @FXML
    private ComboBox<String> splitFileBox;


    @FXML
    public ComboBox<Integer> cutoff;

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

        splitFileBox.getSelectionModel().select(0);
        splitFileBox.getSelectionModel().selectedItemProperty().addListener((observableValue, oldValue, newValue) -> {
            switch (newValue) {
                case "parts" -> ConverterApplication.getContext().setSplit(false);
                case "chapters" -> ConverterApplication.getContext().setSplit(true);
            }
        });

        outputFormatBox.getItems().addAll(Format.values());
        outputFormatBox.getSelectionModel().select(0);
        outputFormatBox.getSelectionModel().selectedItemProperty().addListener((observableValue, oldValue, newValue) -> {
            ConverterApplication.getContext().setOutputFormat(newValue);
        });

        List<Preset> presets = Preset.loadPresets();
//        String savedPreset = Objects.requireNonNullElse(AppProperties.getProperty("last.preset"), "custom");
//        Preset lastPreset = presets.stream().filter(preset -> preset.getPresetName().equals(Preset.LAST_USED)).findFirst().get();

        presetBox.getItems().addAll(presets.stream().map(Preset::getName).collect(Collectors.toList()));

        presetBox.getSelectionModel().select(Preset.LAST_USED);
        presetBox.getSelectionModel().selectedItemProperty().addListener((observableValue, oldValue, newValue) -> {
            if (!presetBox.getItems().contains(newValue)) {
                presetBox.getItems().add(newValue);
                Preset preset = Preset.copy(newValue, Preset.instance(oldValue));
                ConverterApplication.getContext().setOutputParameters(preset);
            } else {
                Preset preset = Preset.instance(newValue);
                ConverterApplication.getContext().setOutputParameters(preset);
            }
        });

        ConverterApplication.getContext().addOutputParametersChangeListener((observableValue, oldParams, newParams) -> {
            outputFormatBox.setValue(newParams.getFormat());
        });

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

        bitRate.valueProperty().addListener((observable, oldValue, newValue) -> context.getOutputParameters().setBitRate(newValue));
        frequency.valueProperty().addListener((observable, oldValue, newValue) -> context.getOutputParameters().setFrequency(newValue));
        channels.valueProperty().addListener((observable, oldValue, newValue) -> context.getOutputParameters().setChannels(newValue));
        quality.valueProperty().addListener((observable, oldValue, newValue) -> context.getOutputParameters().setQuality((int) Math.round(newValue.doubleValue())));
        cutoff.valueProperty().addListener((observable, oldValue, newValue) -> context.getOutputParameters().setCutoff(newValue));

        context.addOutputParametersChangeListener((observableValue, oldValue, newValue) -> {
            bitRate.valueProperty().set(newValue.getBitRate());
            frequency.valueProperty().set(newValue.getFrequency());
            channels.valueProperty().set(newValue.getChannels());
            quality.valueProperty().set(newValue.getQuality());
            cutoff.valueProperty().set(newValue.getCutoff());
            if (newValue.isCbr()) {
                cbr.fire();
            } else {
                vbr.fire();
            }
        });

        media.addListener((InvalidationListener) observable -> updateParameters(media));
        ConverterApplication.getContext().addBookChangeListener((observableValue, oldBook, newBook) -> {
            if (newBook != null) {
                newBook.addListener(observable -> updateParameters(newBook.getMedia()));
            }
        });

        ConverterApplication.getContext().addOutputParametersChangeListener((observableValue, oldParams, newParams) -> {
            bitRate.setValue(findNearestMatch(newParams.getBitRate(), BITRATES, DEFAULT_BITRATE));
            frequency.setValue(findNearestMatch(newParams.getFrequency(), FREQUENCIES, DEFAULT_FREQUENCY));
            channels.setValue(findNearestMatch(newParams.getChannels(), CHANNELS, DEFAULT_CHANNELS));
            quality.setValue(newParams.getQuality());
            cutoff.setValue(findNearestMatch(newParams.getCutoff(), CUTOFFS, DEFAULT_CUTOFF));
        });
    }

    private void updateParameters(List<MediaInfo> media) {
        Book book = ConverterApplication.getContext().getBook();

        if (media.isEmpty() && book == null) {
            frequency.setValue(DEFAULT_FREQUENCY);
            bitRate.setValue(DEFAULT_BITRATE);
            channels.setValue(DEFAULT_CHANNELS);
            quality.setValue(DEFAULT_QUALITY);
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


