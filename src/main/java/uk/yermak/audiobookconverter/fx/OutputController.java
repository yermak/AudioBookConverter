package uk.yermak.audiobookconverter.fx;

import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
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

/**
 * Created by yermak on 08/09/2018.
 */
public class OutputController {
    final static Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    public static final String DISABLED = "Disabled";
    public static final String FORCE = "Always";


    @FXML
    public ComboBox<Format> outputFormatBox;
    @FXML
    public ComboBox<String> presetBox;

    public ComboBox<String> force;

    @FXML
    private ComboBox<String> splitFileBox;

    @FXML
    private ComboBox<String> speedBox;


    @FXML
    public ComboBox<String> cutoff;

    @FXML
    private ComboBox<String> frequency;
    @FXML
    private ComboBox<String> channels;
    @FXML
    private RadioButton cbr;
    @FXML
    private ComboBox<String> bitRate;
    @FXML
    private RadioButton vbr;
    @FXML
    private Slider vbrQuality;
//    private ObservableList<MediaInfo> media;

    public void cbr(ActionEvent actionEvent) {
        bitRate.setDisable(false);
        vbrQuality.setDisable(true);
        refreshBitrates();
        AudiobookConverter.getContext().getOutputParameters().setCbr(true);

    }

    public void vbr(ActionEvent actionEvent) {
        bitRate.setDisable(true);
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
            AudiobookConverter.getContext().setSpeed(Double.valueOf(newValue));
        });
        force.getSelectionModel().select(0);
        force.valueProperty().addListener((observableValue, oldValue, newValue) -> {
            if (newValue == null) return;
            AudiobookConverter.getContext().getOutputParameters().setForce(FORCE.equals(newValue));
        });

        outputFormatBox.getItems().addAll(Format.values());
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

        List<Preset> presets = Preset.loadPresets();
//        String savedPreset = Objects.requireNonNullElse(AppProperties.getProperty("last.preset"), "custom");
//        Preset lastPreset = presets.stream().filter(preset -> preset.getPresetName().equals(Preset.LAST_USED)).findFirst().get();

        presetBox.getItems().addAll(presets.stream().map(Preset::getName).collect(Collectors.toList()));

        presetBox.getSelectionModel().select(Preset.DEFAULT);
        presetBox.getSelectionModel().selectedItemProperty().addListener((observableValue, oldValue, newValue) -> {
            if (!presetBox.getItems().contains(newValue)) {
                presetBox.getItems().add(newValue);
                Preset preset = Preset.copy(newValue, Preset.instance(oldValue));
                AudiobookConverter.getContext().setOutputParameters(preset);
            } else {
                Preset preset = Preset.instance(newValue);
                AudiobookConverter.getContext().setOutputParameters(preset);
            }
        });

        AudiobookConverter.getContext().addOutputParametersChangeListener((observableValue, oldParams, newParams) -> {
            outputFormatBox.setValue(newParams.getFormat());
            if (!oldParams.getFormat().equals(newParams.getFormat())) {
                refreshFrequencies();
                refreshBitrates();
                refreshChannels();
                refreshCutoffs();
                refreshVbrQuality();
                refreshCBR();
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


        bitRate.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null) return;
            context.getOutputParameters().setBitRate(Integer.valueOf(newValue));
        });
        frequency.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null) return;
            context.getOutputParameters().setFrequency(Integer.valueOf(newValue));
        });
        channels.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null) return;
            context.getOutputParameters().setChannels(Integer.valueOf(newValue));
        });
        vbrQuality.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null) return;
            context.getOutputParameters().setVbrQuality((int) Math.round(newValue.doubleValue()));
        });
        cutoff.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null) return;
            if (DISABLED.equals(newValue)) {
                context.getOutputParameters().setCutoff(null);
            } else {
                context.getOutputParameters().setCutoff(Integer.valueOf(newValue));
            }
        });

        context.addOutputParametersChangeListener((observableValue, oldValue, newValue) -> {
            bitRate.valueProperty().set(String.valueOf(newValue.getBitRate()));
            frequency.valueProperty().set(String.valueOf(newValue.getFrequency()));
            channels.valueProperty().set(String.valueOf(newValue.getChannels()));
            vbrQuality.valueProperty().set(newValue.getVbrQuality());
            cutoff.valueProperty().set(String.valueOf(newValue.getCutoff()));
            if (newValue.isCbr()) {
                cbr.fire();
            } else {
                vbr.fire();
            }
        });

        media.addListener((InvalidationListener) observable -> updateParameters(media));
        AudiobookConverter.getContext().addBookChangeListener((observableValue, oldBook, newBook) -> {
            if (newBook != null) {
                newBook.addListener(observable -> updateParameters(newBook.getMedia()));
            }
        });

        AudiobookConverter.getContext().addOutputParametersChangeListener((observableValue, oldParams, newParams) -> {
            Format format = AudiobookConverter.getContext().getOutputParameters().getFormat();
            bitRate.setValue(String.valueOf(findNearestMatch(newParams.getBitRate(), format.bitrates(), format.defaultBitrate())));
            frequency.setValue(String.valueOf(findNearestMatch(newParams.getFrequency(), format.frequencies(), format.defaultFrequency())));
            channels.setValue(String.valueOf(findNearestMatch(newParams.getChannels(), format.channels(), format.defaultChannel())));
            vbrQuality.setValue(findNearestMatch(newParams.getVbrQuality(), format.vbrQualities(), format.defaultVbrQuality()));
            cutoff.setValue(String.valueOf(findNearestMatch(newParams.getCutoff(), format.cutoffs(), format.defaultCutoff())));
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
        cutoff.getItems().addAll(AudiobookConverter.getContext().getOutputParameters().getFormat().cutoffs().stream().map(String::valueOf).collect(Collectors.toList()));
        cutoff.getSelectionModel().select(String.valueOf(format.defaultCutoff()));
    }

    private void refreshChannels() {
        Format format = AudiobookConverter.getContext().getOutputParameters().getFormat();
        channels.getItems().clear();
        channels.getItems().addAll(AudiobookConverter.getContext().getOutputParameters().getFormat().channels().stream().map(String::valueOf).collect(Collectors.toList()));
        channels.getSelectionModel().select(String.valueOf(format.defaultChannel()));
    }

    private void refreshBitrates() {
        Format format = AudiobookConverter.getContext().getOutputParameters().getFormat();
        bitRate.getItems().clear();
        bitRate.getItems().addAll(AudiobookConverter.getContext().getOutputParameters().getFormat().bitrates().stream().map(String::valueOf).collect(Collectors.toList()));
        bitRate.getSelectionModel().select(String.valueOf(format.defaultBitrate()));
    }

    private void refreshSpeeds() {
        Format format = AudiobookConverter.getContext().getOutputParameters().getFormat();
        speedBox.getItems().clear();
        speedBox.getItems().addAll(AudiobookConverter.getContext().getOutputParameters().getFormat().speeds().stream().map(String::valueOf).collect(Collectors.toList()));
        speedBox.getSelectionModel().select(String.valueOf(format.defaultSpeed()));
    }

    private void refreshFrequencies() {
        OutputParameters outputParameters = AudiobookConverter.getContext().getOutputParameters();
        Format format = outputParameters.getFormat();
        frequency.getItems().clear();
        frequency.getItems().addAll(outputParameters.getFormat().frequencies().stream().map(String::valueOf).collect(Collectors.toList()));
        frequency.getSelectionModel().select(String.valueOf(format.defaultFrequency()));
    }

    private void updateParameters(List<MediaInfo> media) {
        Book book = AudiobookConverter.getContext().getBook();

        if (media.isEmpty() && book == null) {
            Format format = AudiobookConverter.getContext().getOutputParameters().getFormat();
            frequency.setValue(String.valueOf(format.defaultFrequency()));
            bitRate.setValue(String.valueOf(format.defaultBitrate()));
            channels.setValue(String.valueOf(format.defaultChannel()));
            vbrQuality.setValue(format.defaultVbrQuality());
            return;
        }

        Executors.newSingleThreadExecutor().submit(() -> {
            OutputParameters params = AudiobookConverter.getContext().getOutputParameters();
            if (book != null) {
                params.updateAuto(book.getMedia());
//                book.addListener(observable -> updateParameters(book.getMedia()));
            } else {
                params.updateAuto(media);
            }
            Platform.runLater(() -> {
                Format format = AudiobookConverter.getContext().getOutputParameters().getFormat();
                frequency.setValue(String.valueOf(findNearestMatch(params.getFrequency(), format.frequencies(), format.defaultFrequency())));
                bitRate.setValue(String.valueOf(findNearestMatch(params.getBitRate(), format.bitrates(), format.defaultBitrate())));
                channels.setValue(String.valueOf(findNearestMatch(params.getChannels(), format.channels(), format.defaultChannel())));
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
}


