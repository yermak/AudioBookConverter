package uk.yermak.audiobookconverter.fx;

import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.yermak.audiobookconverter.AudiobookConverter;
import uk.yermak.audiobookconverter.Preset;
import uk.yermak.audiobookconverter.Settings;
import uk.yermak.audiobookconverter.formats.Format;
import uk.yermak.audiobookconverter.formats.OutputParameters;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;


/**
 * Created by yermak on 08/09/2018.
 */
public class OutputController extends GridPane {
    final static Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    public static final String DISABLED = "Disabled";

    public final ComboBox<Format> outputFormatBox;
    public final ComboBox<String> presetBox;
    public final ComboBox<String> forceBox;
    private final ComboBox<String> splitFileBox;
    private final ComboBox<String> speedBox;
    public final ComboBox<String> cutoffBox;
    private final ComboBox<String> frequencyBox;
    private final ComboBox<String> channelsBox;
    private final RadioButton cbrRadio;
    private final ComboBox<String> bitRateBox;
    private final RadioButton vbrRadio;
    private final Slider vbrQualitySlider;

    public OutputController() {
        ResourceBundle resources = AudiobookConverter.getBundle();
        setPadding(new Insets(5, 5, 0, 5));
        setHgap(5);
        setVgap(5);

        for (int i = 0; i < 8; i++) {
            ColumnConstraints cc = new ColumnConstraints();
            if (i % 2 == 0) {
                cc.setHgrow(Priority.ALWAYS);
            }
            getColumnConstraints().add(cc);
        }

        presetBox = new ComboBox<>();
        outputFormatBox = new ComboBox<>();
        splitFileBox = new ComboBox<>();
        speedBox = new ComboBox<>();
        frequencyBox = new ComboBox<>();
        channelsBox = new ComboBox<>();
        cutoffBox = new ComboBox<>();
        forceBox = new ComboBox<>();
        cbrRadio = new RadioButton(resources.getString("output.radio.cbr"));
        bitRateBox = new ComboBox<>();
        vbrRadio = new RadioButton(resources.getString("output.radio.vbr"));
        vbrQualitySlider = new Slider(1, 5, 4);
        vbrQualitySlider.setMajorTickUnit(1);
        vbrQualitySlider.setMinorTickCount(0);
        vbrQualitySlider.setShowTickMarks(true);
        vbrQualitySlider.setShowTickLabels(true);
        vbrQualitySlider.setSnapToTicks(true);
        vbrQualitySlider.setDisable(true);

        addRow(0,
                new Label(resources.getString("output.label.preset")),
                comboWithTooltip(presetBox, resources.getString("output.tooltip.presets")),
                spacer(),
                new Label(resources.getString("output.label.sampling_frequency")),
                comboWithTooltip(frequencyBox, resources.getString("output.tooltip.sampling_frequency")),
                spacer(),
                radioWithTooltip(cbrRadio, resources.getString("output.tooltip.cbr")),
                comboWithTooltip(bitRateBox, resources.getString("output.tooltip.bitrate")));
        addRow(1,
                new Label(resources.getString("output.label.format")),
                comboWithTooltip2(outputFormatBox, resources.getString("output.tooltip.formats")),
                spacer(),
                new Label(resources.getString("output.label.channels")),
                comboWithTooltip(channelsBox, resources.getString("output.tooltip.channels")),
                spacer(),
                radioWithTooltip(vbrRadio, resources.getString("output.tooltip.vbr")),
                new Label());
        addRow(2,
                new Label(resources.getString("output.label.split")),
                comboWithTooltip(splitFileBox, resources.getString("output.tooltip.split")),
                spacer(),
                new Label(resources.getString("output.label.cut_off")),
                comboWithTooltip(cutoffBox, resources.getString("output.tooltip.cut_off")),
                spacer(),
                sliderWithTooltip(vbrQualitySlider, resources.getString("output.tooltip.vbr_quality")),
                new Label());
        Button deletePreset = new Button(resources.getString("output.button.delete_preset"));
        deletePreset.setOnAction(this::deletePreset);
        addRow(3,
                new Label(resources.getString("output.label.speed")),
                comboWithTooltip(speedBox, resources.getString("output.tooltip.speed")),
                spacer(),
                new Label(resources.getString("output.label.reencoding")),
                comboWithTooltip(forceBox, resources.getString("output.tooltip.reencode")),
                spacer(),
                new Label(),
                deletePreset);

        initialize();
    }

    private ComboBox<String> comboWithTooltip(ComboBox<String> comboBox, String text) {
        comboBox.setTooltip(new Tooltip(text));
        return comboBox;
    }

    private ComboBox<Format> comboWithTooltip2(ComboBox<Format> comboBox, String text) {
        comboBox.setTooltip(new Tooltip(text));
        return comboBox;
    }

    private RadioButton radioWithTooltip(RadioButton radioButton, String text) {
        radioButton.setTooltip(new Tooltip(text));
        return radioButton;
    }

    private Slider sliderWithTooltip(Slider slider, String text) {
        slider.setTooltip(new Tooltip(text));
        return slider;
    }

    private Label spacer() {
        Label spacer = new Label();
        spacer.setMinWidth(50);
        return spacer;
    }

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

    private void initialize() {
        initPresetBox();

        outputFormatBox.getItems().addAll(Format.M4B, Format.M4A, Format.MP3, Format.OGG);
        outputFormatBox.getSelectionModel().selectedItemProperty().addListener((_, oldValue, newValue) -> {
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

        bitRateBox.valueProperty().addListener((_, _, newValue) -> {
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
            AudiobookConverter.getContext().setSpeed(Double.parseDouble(newValue));
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
//                AudiobookConverter.getContext().setOutputParameters(preset);
                AudiobookConverter.getContext().setPresetName(preset.getName());
                updateOutputSettingsFromPreset(preset);
            } else {
                presetBox.getItems().add(newValue);
                Preset preset = Preset.copy(newValue, s.findPreset(oldValue));
//                AudiobookConverter.getContext().setOutputParameters(preset);
                AudiobookConverter.getContext().setPresetName(preset.getName());
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
        if (cutoff == 0) {
            cutoffBox.getSelectionModel().select(0);
        } else {
            cutoffBox.getSelectionModel().select(String.valueOf(findNearestMatch(cutoff, format.cutoffs(), format.defaultCutoff())));
        }
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
        AudiobookConverter.getContext().setPresetName(preset.getName());

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


