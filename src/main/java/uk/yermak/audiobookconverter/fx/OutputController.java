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

/**
 * Created by yermak on 08/09/2018.
 */
public class OutputController {
    final static Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

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
    private Slider vbrQuality;
    private ObservableList<MediaInfo> media;

    public void cbr(ActionEvent actionEvent) {
        bitRate.setDisable(false);
        vbrQuality.setDisable(true);
        refreshBitrates();
        ConverterApplication.getContext().getOutputParameters().setCbr(true);

    }

    public void vbr(ActionEvent actionEvent) {
        bitRate.setDisable(true);
        vbrQuality.setDisable(false);
        refreshVbrQuality();
        ConverterApplication.getContext().getOutputParameters().setCbr(false);
    }

    @FXML
    private void initialize() {

        splitFileBox.getSelectionModel().select(0);
        splitFileBox.getSelectionModel().selectedItemProperty().addListener((observableValue, oldValue, newValue) -> {
            switch (newValue) {
                case "parts" -> ConverterApplication.getContext().getOutputParameters().setSplitChapters(false);
                case "chapters" -> ConverterApplication.getContext().getOutputParameters().setSplitChapters(true);
            }
        });

        outputFormatBox.getItems().addAll(Format.values());
        outputFormatBox.getSelectionModel().select(0);
        outputFormatBox.getSelectionModel().selectedItemProperty().addListener((observableValue, oldValue, newValue) -> {
            ConverterApplication.getContext().getOutputParameters().setupFormat(newValue);
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
                ConverterApplication.getContext().setOutputParameters(preset);
            } else {
                Preset preset = Preset.instance(newValue);
                ConverterApplication.getContext().setOutputParameters(preset);
            }
        });

        ConverterApplication.getContext().addOutputParametersChangeListener((observableValue, oldParams, newParams) -> {
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

        ConversionContext context = ConverterApplication.getContext();
        media = context.getMedia();

        bitRate.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) context.getOutputParameters().setBitRate(newValue);
        });
        frequency.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) context.getOutputParameters().setFrequency(newValue);
        });
        channels.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) context.getOutputParameters().setChannels(newValue);
        });
        vbrQuality.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) context.getOutputParameters().setVbrQuality((int) Math.round(newValue.doubleValue()));
        });
        cutoff.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) context.getOutputParameters().setCutoff(newValue);
        });

        context.addOutputParametersChangeListener((observableValue, oldValue, newValue) -> {
            bitRate.valueProperty().set(newValue.getBitRate());
            frequency.valueProperty().set(newValue.getFrequency());
            channels.valueProperty().set(newValue.getChannels());
            vbrQuality.valueProperty().set(newValue.getVbrQuality());
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
            Format format = ConverterApplication.getContext().getOutputParameters().getFormat();
            bitRate.setValue(findNearestMatch(newParams.getBitRate(), format.bitrates(), format.defaultBitrate()));
            frequency.setValue(findNearestMatch(newParams.getFrequency(), format.frequencies(), format.defaultFrequency()));
            channels.setValue(findNearestMatch(newParams.getChannels(), format.channels(), format.defaultChannel()));
            vbrQuality.setValue(findNearestMatch(newParams.getVbrQuality(), format.vbrQualities(), format.defaultVbrQuality()));
            cutoff.setValue(findNearestMatch(newParams.getCutoff(), format.cutoffs(), format.defaultCutoff()));
        });
    }

    private void refreshCBR() {
        Format format = ConverterApplication.getContext().getOutputParameters().getFormat();
        if (format.defaultCBR()) {
            cbr.fire();
        } else {
            vbr.fire();
        }
    }

    private void refreshVbrQuality() {
        Format format = ConverterApplication.getContext().getOutputParameters().getFormat();
        vbrQuality.setValue(format.defaultVbrQuality());
    }

    private void refreshCutoffs() {
        Format format = ConverterApplication.getContext().getOutputParameters().getFormat();
        cutoff.getItems().clear();
        cutoff.getItems().addAll(ConverterApplication.getContext().getOutputParameters().getFormat().cutoffs());
        cutoff.getSelectionModel().select(format.defaultCutoff());
    }

    private void refreshChannels() {
        Format format = ConverterApplication.getContext().getOutputParameters().getFormat();
        channels.getItems().clear();
        channels.getItems().addAll(ConverterApplication.getContext().getOutputParameters().getFormat().channels());
        channels.getSelectionModel().select(format.defaultChannel());
    }

    private void refreshBitrates() {
        Format format = ConverterApplication.getContext().getOutputParameters().getFormat();
        bitRate.getItems().clear();
        bitRate.getItems().addAll(ConverterApplication.getContext().getOutputParameters().getFormat().bitrates());
        bitRate.getSelectionModel().select(format.defaultBitrate());
    }

    private void refreshFrequencies() {
        Format format = ConverterApplication.getContext().getOutputParameters().getFormat();
        frequency.getItems().clear();
        frequency.getItems().addAll(ConverterApplication.getContext().getOutputParameters().getFormat().frequencies());
        frequency.getSelectionModel().select(format.defaultFrequency());
    }

    private void updateParameters(List<MediaInfo> media) {
        Book book = ConverterApplication.getContext().getBook();

        if (media.isEmpty() && book == null) {
            Format format = ConverterApplication.getContext().getOutputParameters().getFormat();
            frequency.setValue(format.defaultFrequency());
            bitRate.setValue(format.defaultBitrate());
            channels.setValue(format.defaultChannel());
            vbrQuality.setValue(format.defaultVbrQuality());
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
                Format format = ConverterApplication.getContext().getOutputParameters().getFormat();
                frequency.setValue(findNearestMatch(params.getFrequency(), format.frequencies(), format.defaultFrequency()));
                bitRate.setValue(findNearestMatch(params.getBitRate(), format.bitrates(), format.defaultBitrate()));
                channels.setValue(findNearestMatch(params.getChannels(), format.channels(), format.defaultChannel()));
                vbrQuality.setValue(findNearestMatch(params.getVbrQuality(), format.vbrQualities(), format.defaultVbrQuality()));
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


