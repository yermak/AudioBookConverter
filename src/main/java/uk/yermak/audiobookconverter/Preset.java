package uk.yermak.audiobookconverter;

import java.util.*;

public class Preset extends OutputParameters {

    public static final String DEFAULT = "default";
    private final String name;


    @Override
    public String toString() {
        return name;
    }


    static List<Preset> defaultValues = List.of(
            new Preset("ipod nano", new OutputParameters(Format.M4B, 64, 44100, 1, 10000, false, 2)),
            new Preset("ipod classic", new OutputParameters(Format.M4B, 96, 44100, 2, 12000, true, 3)),
            new Preset("iphone", new OutputParameters(Format.M4B, 128, 44100, 2, 12000, true, 4)),
            new Preset("android 5+", new OutputParameters(Format.OGG, 64, 44100, 2, 12000, false, 3)),
            new Preset("android old", new OutputParameters(Format.M4B, 96, 44100, 2, 10000, true, 3)),
            new Preset("legacy", new OutputParameters(Format.MP3, 128, 44100, 2, 12000, true, 3))
    );

    public static final Preset DEFAULT_OUTPUT_PARAMETERS = new Preset(Preset.DEFAULT);


    public static List<Preset> loadPresets() {
        List<Preset> presets = AppSetting.loadPresets();
        ArrayList<Preset> toStore = new ArrayList<>(defaultValues);
        toStore.removeAll(presets);
        for (Preset preset : toStore) {
            AppSetting.savePreset(preset);
        }
        return AppSetting.loadPresets();

    }

//    private final OutputParameters save;

    Preset(String name, OutputParameters preset) {
        this.name = name;
        this.bitRate = preset.getBitRate();
        this.frequency = preset.getFrequency();
        this.channels = preset.getChannels();
        this.vbrQuality = preset.getVbrQuality();
        this.cbr = preset.isCbr();
        this.cutoff = preset.getCutoff();
        this.format = preset.getFormat();
//        savePreset();
    }


    private Preset(String name) {
        this.name = name;
    }

    public static Preset copy(String presetName, Preset copy) {
        return new Preset(presetName, copy);
    }

    public static Preset instance(String presetName) {
        Preset preset = AppSetting.loadPreset(presetName);
        if (preset != null) return preset;

        return new Preset(presetName, new OutputParameters());
    }

    private void savePreset() {
        AppSetting.savePreset(this);
    }


    @Override
    public void setupFormat(Format format) {
        super.setupFormat(format);
        savePreset();
    }


    @Override
    public void setBitRate(Integer bitRate) {
        super.setBitRate(bitRate);
        savePreset();
    }

    @Override
    public void setFrequency(Integer frequency) {
        super.setFrequency(frequency);
        savePreset();
    }


    @Override
    public void setChannels(Integer channels) {
        super.setChannels(channels);
        savePreset();
    }

    @Override
    public void setVbrQuality(Integer vbrQuality) {
        super.setVbrQuality(vbrQuality);
        savePreset();
    }

    @Override
    public void setCbr(Boolean cbr) {
        super.setCbr(cbr);
        savePreset();
    }

    @Override
    public void updateAuto(List<MediaInfo> media) {
        if (!defaultValues.contains(this)) {
            super.updateAuto(media);
//            savePreset();
        } else {
            //Ignoring auto-update and save for all other preset
        }
    }

    @Override
    public void setCutoff(Integer cutoff) {
        super.setCutoff(cutoff);
        savePreset();
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Preset preset = (Preset) o;
        return Objects.equals(name, preset.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
