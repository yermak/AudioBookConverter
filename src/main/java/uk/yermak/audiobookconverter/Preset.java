package uk.yermak.audiobookconverter;

import com.google.gson.Gson;

import java.util.*;

public class Preset extends OutputParameters {

    private String presetName;

    @Override
    public String toString() {
        return presetName;
    }


    static Map<String, OutputParameters> defaultValues = Map.of(
            "nano", new OutputParameters(Format.M4B, 64, 44100, 1, 10000, false, 2),
            "classic", new OutputParameters(Format.M4B, 96, 44100, 2, 12000, true, 3),
            "iphone", new OutputParameters(Format.M4B, 128, 44100, 2, 12000, true, 4),
            "android-5+", new OutputParameters(Format.OGG, 128, 44100, 2, 12000, true, 3),
            "android_old", new OutputParameters(Format.M4B, 96, 44100, 2, 10000, true, 3),
            "legacy", new OutputParameters(Format.MP3, 128, 44100, 2, 12000, true, 3)
    );

    public static final Preset DEFAULT_OUTPUT_PARAMETERS = new Preset("custom");


    public static List<Preset> loadPresets() {
        List<Preset> list = new ArrayList<>();
        list.add(DEFAULT_OUTPUT_PARAMETERS);
        Properties savedPresets = AppProperties.getProperties("preset");
        savedPresets.keySet().forEach(p -> list.add(new Preset((String) p)));
        Set<String> presetNames = defaultValues.keySet();
        for (String presetName : presetNames) {
            if (!savedPresets.containsKey(presetName)) {
                Gson gson = new Gson();
                String gsonString = gson.toJson(defaultValues.get(presetName));
                AppProperties.setProperty("preset." + presetName, gsonString);
                list.add(new Preset(presetName));
            }
        }
        return list;
    }

    private OutputParameters save;

    public Preset(String presetName) {
        this.presetName = presetName;
        String property = AppProperties.getProperty(presetName);
        if (property == null) {
            save = Objects.requireNonNullElseGet(defaultValues.get(presetName), OutputParameters::new);
            saveProperty();
        } else {
            Gson gson = new Gson();
            save = gson.fromJson(property, OutputParameters.class);
        }
    }

    private void saveProperty() {
        Gson gson = new Gson();
        String gsonString = gson.toJson(save);
        AppProperties.setProperty("preset." + presetName, gsonString);
    }

    @Override
    public boolean needReencode(String codec) {
        return save.needReencode(codec);
    }

    @Override
    public void setupFormat(String extension) {
        save.setupFormat(extension);
        saveProperty();
    }

    @Override
    public int getBitRate() {
        return save.getBitRate();
    }

    @Override
    public void setBitRate(int bitRate) {
        save.setBitRate(bitRate);
        saveProperty();
    }

    @Override
    public int getFrequency() {
        return save.getFrequency();
    }

    @Override
    public void setFrequency(int frequency) {
        save.setFrequency(frequency);
        saveProperty();
    }

    @Override
    public int getChannels() {
        return save.getChannels();
    }

    @Override
    public void setChannels(int channels) {
        save.setChannels(channels);
        saveProperty();
    }

    @Override
    public int getQuality() {
        return save.getQuality();
    }

    @Override
    public void setQuality(int quality) {
        save.setQuality(quality);
        saveProperty();
    }

    @Override
    public boolean isCbr() {
        return save.isCbr();
    }

    @Override
    public void setCbr(boolean cbr) {
        save.setCbr(cbr);
        saveProperty();
    }

    @Override
    public void updateAuto(List<MediaInfo> media) {
        if (presetName.equals("custom")) {
            save.updateAuto(media);
            saveProperty();
        } else {
            //Ignoring auto-update and save for all other preset
        }
    }

    @Override
    public String getFFMpegQualityParameter() {
        return save.getFFMpegQualityParameter();
    }

    @Override
    public String getFFMpegQualityValue() {
        return save.getFFMpegQualityValue();
    }

    @Override
    public String getFFMpegChannelsValue() {
        return save.getFFMpegChannelsValue();
    }

    @Override
    public String getCutoffValue() {
        return save.getCutoffValue();
    }

    @Override
    public int getCutoff() {
        return save.getCutoff();
    }

    @Override
    public void setCutoff(int cutoff) {
        save.setCutoff(cutoff);
        saveProperty();
    }

    @Override
    public String getFormat() {
        return save.getFormat();
    }

    public String getPresetName() {
        return presetName;
    }
}
