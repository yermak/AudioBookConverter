package uk.yermak.audiobookconverter.fx;

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
import uk.yermak.audiobookconverter.formats.Format;
import uk.yermak.audiobookconverter.formats.OutputParameters;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * Created by yermak on 08/09/2018.
 */
public class OutputController {
    final static Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    public static final String DISABLED = "Disabled";

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
    public ComboBox<String> cutoffBox;

    @FXML
    private ComboBox<String> frequencyBox;
    @FXML
    private ComboBox<String> channelsBox;
    @FXML
    private RadioButton cbrRadio;
    @FXML
    private ComboBox<String> bitRateBox;
    @FXML
    private RadioButton vbrRadio;
    @FXML
    private Slider vbrQualitySlider;

    public void cbr(ActionEvent actionEvent) {
        bitRateBox.setDisable(false);
        vbrQualitySlider.setDisable(true);
        Settings settings = Settings.loadSetting();
        Preset preset = currentPreset(settings);
        preset.setCbr(true);
        settings.save();
    }

    public void vbr(ActionEvent actionEvent) {
        bitRateBox.setDisable(true);
        vbrQualitySlider.setDisable(false);
        Settings settings = Settings.loadSetting();
        Preset preset = currentPreset(settings);
        preset.setCbr(false);
        settings.save();
    }

    @FXML
    private void initialize() {
        initPresetBox();

        outputFormatBox.getItems().addAll(Format.M4B, Format.M4A, Format.MP3, Format.OGG);
        outputFormatBox.getSelectionModel().selectedItemProperty().addListener((observableValue, oldValue, newValue) -> {
            Settings settings = Settings.loadSetting();
            Preset preset = currentPreset(settings);
            preset.setupFormat(newValue);


            refreshFrequencies(newValue, newValue.defaultFrequency());
            refreshBitrates(newValue, newValue.defaultBitrate());
            refreshChannels(newValue, newValue.defaultChannel());
            refreshCutoffs(newValue, newValue.defaultCutoff());
            refreshSpeeds(newValue, newValue.defaultSpeed());
            updateVbrQuality(newValue.defaultVbrQuality());
            updateCBR(newValue.defaultCBR());

            settings.save();
        });
        outputFormatBox.getSelectionModel().select(currentPreset(Settings.loadSetting()).getFormat());

        bitRateBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                Settings settings = Settings.loadSetting();
                Preset preset = currentPreset(settings);
                preset.setBitRate(Integer.valueOf(newValue));
                settings.save();
            }
        });
        frequencyBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                Settings settings = Settings.loadSetting();
                Preset preset = currentPreset(settings);
                preset.setFrequency(Integer.valueOf(newValue));
                settings.save();
            }
        });
        channelsBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                Settings settings = Settings.loadSetting();
                Preset preset = currentPreset(settings);
                preset.setChannels(Integer.valueOf(newValue));
                settings.save();
            }
        });

        vbrQualitySlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                Settings settings = Settings.loadSetting();
                Preset preset = currentPreset(settings);
                preset.setVbrQuality((int) Math.round(newValue.doubleValue()));
                settings.save();
            }
        });
        cutoffBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null) return;
            Settings settings = Settings.loadSetting();
            Preset preset = currentPreset(settings);
            if (DISABLED.equals(newValue)) {
                preset.setCutoff(0);
            } else {
                preset.setCutoff(Integer.valueOf(newValue));
            }
            settings.save();
        });

        speedBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null) return;
            Settings settings = Settings.loadSetting();
            Preset preset = currentPreset(settings);
            preset.setSpeed(Double.parseDouble(newValue));
            settings.save();
        });

        splitFileBox.getSelectionModel().select(0);
        splitFileBox.getSelectionModel().selectedItemProperty().addListener((observableValue, oldValue, newValue) -> {
            switch (newValue) {
                case "parts" -> {
                    Settings settings = Settings.loadSetting();
                    Preset preset = currentPreset(settings);
                    preset.setSplitChapters(false);
                    settings.save();
                }
                case "chapters" -> {
                    Settings settings = Settings.loadSetting();
                    Preset preset = currentPreset(settings);
                    preset.setSplitChapters(true);
                    settings.save();
                }
            }
        });

        forceBox.getSelectionModel().select(0);
        forceBox.valueProperty().addListener((observableValue, oldValue, newValue) -> {
            if (newValue == null) return;
            Settings settings = Settings.loadSetting();
            Preset preset = currentPreset(settings);
            preset.setForce(OutputParameters.Force.valueOf(newValue));
            settings.save();
        });


        int selectedIndex = presetBox.getSelectionModel().getSelectedIndex();
        Settings settings = Settings.loadSetting();
        Preset preset = settings.getPresets().get(selectedIndex);
        updateOutputSettingsFromPreset(preset);
    }

    private void initPresetBox() {
        Settings settings = Settings.loadSetting();
        presetBox.getItems().addAll(settings.getPresets().stream().map(Preset::getName).toList());
        presetBox.getSelectionModel().select(settings.getLastUsedPreset());
        presetBox.getSelectionModel().selectedItemProperty().addListener((observableValue, oldValue, newValue) -> {
            if (newValue == null) return;
            Settings s = Settings.loadSetting();
            int i = -1;
            if ((i = presetBox.getItems().indexOf(newValue)) != -1) {
                Preset preset = s.findPreset(newValue);
                s.setLastUsedPreset(i);
                s.save();
                AudiobookConverter.getContext().setOutputParameters(preset);
                updateOutputSettingsFromPreset(preset);
            } else {
                presetBox.getItems().add(newValue);
                Preset preset = Preset.copy(newValue, s.findPreset(oldValue));
                AudiobookConverter.getContext().setOutputParameters(preset);
                ArrayList<Preset> newPresets = new ArrayList<>(s.getPresets());
                newPresets.add(preset);
                s.setPresets(newPresets);
                s.setLastUsedPreset(newPresets.size() - 1);
                s.save();
                updateOutputSettingsFromPreset(preset);
            }
        });
    }

    private Preset currentPreset(Settings settings) {
        List<Preset> presets = settings.getPresets();
        int selectedIndex = presetBox.getSelectionModel().getSelectedIndex();
        Preset preset = presets.get(selectedIndex);
        return preset;
    }

    private void updateCBR(Boolean cbr) {
        if (cbr) {
            cbrRadio.fire();
        } else {
            vbrRadio.fire();
        }
    }

    private void updateVbrQuality(Integer quality) {
        vbrQualitySlider.setValue(quality);
    }

    private void refreshCutoffs(Format format, Integer cutoff) {
        cutoffBox.getItems().clear();
        cutoffBox.getItems().add(DISABLED);
        cutoffBox.getItems().addAll(format.cutoffs().stream().map(String::valueOf).toList());
        cutoffBox.getSelectionModel().select(String.valueOf(findNearestMatch(cutoff, format.cutoffs(), format.defaultCutoff())));
    }

    private void refreshChannels(Format format, Integer channel) {
        channelsBox.getItems().clear();
        channelsBox.getItems().addAll(format.channels().stream().map(String::valueOf).toList());
        channelsBox.getSelectionModel().select(String.valueOf(findNearestMatch(channel, format.channels(), format.defaultChannel())));
    }

    private void refreshBitrates(Format format, Integer bitrate) {
        bitRateBox.getItems().clear();
        bitRateBox.getItems().addAll(format.bitrates().stream().map(String::valueOf).toList());
        bitRateBox.getSelectionModel().select(String.valueOf(findNearestMatch(bitrate, format.bitrates(), format.defaultBitrate())));
    }

    private void refreshSpeeds(Format format, Double speed) {
        speedBox.getItems().clear();
        speedBox.getItems().addAll(format.speeds().stream().map(String::valueOf).toList());
        speedBox.getSelectionModel().select(String.valueOf(speed));
    }

    private void refreshFrequencies(Format format, Integer frequency) {
        frequencyBox.getItems().clear();
        frequencyBox.getItems().addAll(format.frequencies().stream().map(String::valueOf).toList());
        frequencyBox.getSelectionModel().select(String.valueOf(findNearestMatch(frequency, format.frequencies(), format.defaultFrequency())));
    }


    public void deletePreset(ActionEvent actionEvent) {
        if (presetBox.getItems().size() == 1) {
            return;
        }
        Settings settings = Settings.loadSetting();
        Preset presetToRemove = currentPreset(settings);
        settings.getPresets().remove(presetToRemove);
        settings.setLastUsedPreset(0);
        settings.save();


        Preset preset = settings.getPresets().get(0);
        updateOutputSettingsFromPreset(preset);
        AudiobookConverter.getContext().setOutputParameters(preset);

        presetBox.getItems().remove(presetToRemove.getName());
        presetBox.getSelectionModel().select(0);
    }

    private void updateForceSpeed(Preset preset) {
        forceBox.getSelectionModel().select(preset.getForce().toString());
    }

    private void updateChapterSplit(Preset preset) {
        splitFileBox.getSelectionModel().select(preset.isSplitChapters() ? "chapters" : "parts");
    }

    private void updateOutputSettingsFromPreset(Preset preset) {
        outputFormatBox.getSelectionModel().select(preset.getFormat());
        refreshFrequencies(preset.getFormat(), preset.getFrequency());
        refreshBitrates(preset.getFormat(), preset.getBitRate());
        refreshChannels(preset.getFormat(), preset.getChannels());
        refreshCutoffs(preset.getFormat(), preset.getCutoff());
        refreshSpeeds(preset.getFormat(), preset.getSpeed());
        updateVbrQuality(preset.getVbrQuality());
        updateCBR(preset.isCbr());
        updateChapterSplit(preset);
        updateForceSpeed(preset);
    }

    private static Integer findNearestMatch(int value, List<Integer> list, int defaultValue) {
        for (Integer integer : list) {
            if (integer >= value)
                return integer;
        }
        return defaultValue;
    }
}


