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
import uk.yermak.audiobookconverter.AudiobookConverter;
import uk.yermak.audiobookconverter.Preset;
import uk.yermak.audiobookconverter.Settings;
import uk.yermak.audiobookconverter.book.Book;
import uk.yermak.audiobookconverter.book.MediaInfo;
import uk.yermak.audiobookconverter.formats.Format;
import uk.yermak.audiobookconverter.formats.OutputParameters;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;


/**
 * Created by yermak on 08/09/2018.
 */
public class OutputController {
    final static Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    public static final String DISABLED = "Disabled";
//    public static final String FORCE = "Always";


    @FXML
    public ComboBox<Format> outputFormatBox;
    @FXML
    public ComboBox<String> presetBox;

    public ComboBox<String> forceBox;

    @FXML
    private ComboBox<String> splitFileBox;

    @FXML
    private ComboBox<String> speedBox;


    @FXML
    public ComboBox<String> cutoff;

    @FXML
    private ComboBox<String> frequencyBox;
    @FXML
    private ComboBox<String> channelsBox;
    @FXML
    private RadioButton cbr;
    @FXML
    private ComboBox<String> bitRateBox;
    @FXML
    private RadioButton vbr;
    @FXML
    private Slider vbrQuality;

    public void cbr(ActionEvent actionEvent) {
        bitRateBox.setDisable(false);
        vbrQuality.setDisable(true);
        refreshBitrates();
        AudiobookConverter.getContext().getOutputParameters().setCbr(true);

    }

    public void vbr(ActionEvent actionEvent) {
        bitRateBox.setDisable(true);
        vbrQuality.setDisable(false);
        refreshVbrQuality();
        AudiobookConverter.getContext().getOutputParameters().setCbr(false);
    }

    @FXML
    private void initialize() {
        splitFileBox.getSelectionModel().select(0);
        splitFileBox.getSelectionModel().selectedItemProperty().addListener((observableValue, oldValue, newValue) -> {
            switch (newValue) {
                case "parts" -> AudiobookConverter.getContext().getOutputParameters().setSplitChapters(false);
                case "chapters" -> AudiobookConverter.getContext().getOutputParameters().setSplitChapters(true);
            }
        });

        speedBox.valueProperty().addListener((observableValue, oldValue, newValue) -> {
            if (newValue == null) return;
            AudiobookConverter.getContext().getOutputParameters().setSpeed(Double.parseDouble(newValue));
        });
        forceBox.getSelectionModel().select(0);
        forceBox.valueProperty().addListener((observableValue, oldValue, newValue) -> {
            if (newValue == null) return;
            AudiobookConverter.getContext().getOutputParameters().setForce(OutputParameters.Force.valueOf(newValue));
        });

        outputFormatBox.getItems().addAll(Format.M4B, Format.M4A, Format.MP3, Format.OGG);
        outputFormatBox.getSelectionModel().select(0);
        outputFormatBox.getSelectionModel().selectedItemProperty().addListener((observableValue, oldValue, newValue) -> {
            AudiobookConverter.getContext().getOutputParameters().setupFormat(newValue);
            refreshFrequencies();
            refreshBitrates();
            refreshChannels();
            refreshCutoffs();
            refreshVbrQuality();
            refreshCBR();
        });

        List<Preset> presets = Settings.loadSetting().getPresets();

//        presetBox.getItems().add(Preset.DEFAULT);
        presetBox.getItems().addAll(presets.stream().map(Preset::getName).toList());

//        presetBox.getSelectionModel().select(Preset.DEFAULT);
        presetBox.getSelectionModel().select(0);
        presetBox.getSelectionModel().selectedItemProperty().addListener((observableValue, oldValue, newValue) -> {
            Settings settings = Settings.loadSetting();
            if (presetBox.getItems().contains(newValue)) {
                Preset preset = settings.findPreset(newValue);
                AudiobookConverter.getContext().setOutputParameters(preset);
            } else {
                presetBox.getItems().add(newValue);
                Preset preset = Preset.copy(newValue, settings.findPreset(oldValue));
                ArrayList<Preset> newPresets = new ArrayList<>(settings.getPresets());
                newPresets.add(preset);
                settings.setPresets(newPresets);
                settings.save();
                AudiobookConverter.getContext().setOutputParameters(preset);
            }
        });

        refreshFrequencies();
        refreshBitrates();
        refreshChannels();
        refreshCutoffs();
        refreshVbrQuality();
        refreshCBR();
        refreshSpeeds();

        ConversionContext context = AudiobookConverter.getContext();
        ObservableList<MediaInfo> media = context.getMedia();


        bitRateBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                context.getOutputParameters().setBitRate(Integer.valueOf(newValue));
            }
        });
        frequencyBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                context.getOutputParameters().setFrequency(Integer.valueOf(newValue));
            }
        });
        channelsBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                context.getOutputParameters().setChannels(Integer.valueOf(newValue));
            }
        });
        vbrQuality.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                context.getOutputParameters().setVbrQuality((int) Math.round(newValue.doubleValue()));
            }
        });
        cutoff.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null) return;
            if (DISABLED.equals(newValue)) {
                context.getOutputParameters().setCutoff(0);
            } else {
                context.getOutputParameters().setCutoff(Integer.valueOf(newValue));
            }
        });

        context.addOutputParametersChangeListener((observableValue, oldValue, newValue) -> {
            outputFormatBox.valueProperty().set(newValue.getFormat());
            bitRateBox.valueProperty().set(String.valueOf(newValue.getBitRate()));
            frequencyBox.valueProperty().set(String.valueOf(newValue.getFrequency()));
            channelsBox.valueProperty().set(String.valueOf(newValue.getChannels()));
            vbrQuality.valueProperty().set(newValue.getVbrQuality());
            cutoff.valueProperty().set(String.valueOf(newValue.getCutoff()));
            if (newValue.isCbr()) {
                cbr.fire();
            } else {
                vbr.fire();
            }
            speedBox.valueProperty().set(Double.toString(newValue.getSpeed()));
            forceBox.getSelectionModel().select(newValue.getForce().toString());
            splitFileBox.getSelectionModel().select(newValue.isSplitChapters() ? "chapters" : "parts");
        });

        media.addListener((InvalidationListener) observable -> updateParameters(media));
        AudiobookConverter.getContext().addBookChangeListener((observableValue, oldBook, newBook) -> {
            if (newBook != null) {
                newBook.addListener(observable -> updateParameters(newBook.getMedia()));
            }
        });
    }

    private void refreshCBR() {
        Format format = AudiobookConverter.getContext().getOutputParameters().getFormat();
        if (format.defaultCBR()) {
            cbr.fire();
        } else {
            vbr.fire();
        }
    }

    private void refreshVbrQuality() {
        Format format = AudiobookConverter.getContext().getOutputParameters().getFormat();
        vbrQuality.setValue(format.defaultVbrQuality());
    }

    private void refreshCutoffs() {
        Format format = AudiobookConverter.getContext().getOutputParameters().getFormat();
        cutoff.getItems().clear();
        cutoff.getItems().add(DISABLED);
        cutoff.getItems().addAll(AudiobookConverter.getContext().getOutputParameters().getFormat().cutoffs().stream().map(String::valueOf).toList());
        cutoff.getSelectionModel().select(String.valueOf(format.defaultCutoff()));
    }

    private void refreshChannels() {
        Format format = AudiobookConverter.getContext().getOutputParameters().getFormat();
        channelsBox.getItems().clear();
        channelsBox.getItems().addAll(AudiobookConverter.getContext().getOutputParameters().getFormat().channels().stream().map(String::valueOf).toList());
        channelsBox.getSelectionModel().select(String.valueOf(format.defaultChannel()));
    }

    private void refreshBitrates() {
        Format format = AudiobookConverter.getContext().getOutputParameters().getFormat();
        bitRateBox.getItems().clear();
        bitRateBox.getItems().addAll(AudiobookConverter.getContext().getOutputParameters().getFormat().bitrates().stream().map(String::valueOf).toList());
        bitRateBox.getSelectionModel().select(String.valueOf(format.defaultBitrate()));
    }

    private void refreshSpeeds() {
        Format format = AudiobookConverter.getContext().getOutputParameters().getFormat();
        speedBox.getItems().clear();
        speedBox.getItems().addAll(AudiobookConverter.getContext().getOutputParameters().getFormat().speeds().stream().map(String::valueOf).toList());
        speedBox.getSelectionModel().select(String.valueOf(format.defaultSpeed()));
    }

    private void refreshFrequencies() {
        OutputParameters outputParameters = AudiobookConverter.getContext().getOutputParameters();
        Format format = outputParameters.getFormat();
        frequencyBox.getItems().clear();
        frequencyBox.getItems().addAll(outputParameters.getFormat().frequencies().stream().map(String::valueOf).toList());
        frequencyBox.getSelectionModel().select(String.valueOf(format.defaultFrequency()));
    }

    private void updateParameters(List<MediaInfo> media) {
        Book book = AudiobookConverter.getContext().getBook();

        if (media.isEmpty() && book == null) {
            Format format = AudiobookConverter.getContext().getOutputParameters().getFormat();
            frequencyBox.setValue(String.valueOf(format.defaultFrequency()));
            bitRateBox.setValue(String.valueOf(format.defaultBitrate()));
            channelsBox.setValue(String.valueOf(format.defaultChannel()));
            vbrQuality.setValue(format.defaultVbrQuality());
            return;
        }

        Executors.newSingleThreadExecutor().submit(() -> {
            OutputParameters params = AudiobookConverter.getContext().getOutputParameters();
            if (book != null) {
                params.updateAuto(book.getMedia());
            } else {
                params.updateAuto(media);
            }
            Platform.runLater(() -> {
                Format format = AudiobookConverter.getContext().getOutputParameters().getFormat();
                frequencyBox.getSelectionModel().select(String.valueOf(params.getFrequency()));
                bitRateBox.getSelectionModel().select(String.valueOf(params.getBitRate()));
                channelsBox.getSelectionModel().select(String.valueOf(params.getChannels()));
                vbrQuality.setValue(params.getVbrQuality());
                vbrQuality.setValue(findNearestMatch(params.getVbrQuality(), format.vbrQualities(), format.defaultVbrQuality()));
            });
        });
    }

    private static Integer findNearestMatch(int value, List<Integer> list, int defaultValue) {
        for (Integer integer : list) {
            if (integer >= value)
                return integer;
        }
        return defaultValue;
    }

    public void savePreset(ActionEvent actionEvent) {
        Preset preset = new Preset(presetBox.getSelectionModel().getSelectedItem(), AudiobookConverter.getContext().getOutputParameters());
        Settings settings = Settings.loadSetting();
        Preset remove = settings.findPreset(preset.getName());
        settings.getPresets().remove(remove);
        settings.getPresets().add(preset);
    }
}


