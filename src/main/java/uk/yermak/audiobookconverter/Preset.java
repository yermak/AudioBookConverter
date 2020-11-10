package uk.yermak.audiobookconverter;

import com.google.gson.Gson;

import java.util.*;

public class Preset extends OutputParameters {

    public static final String LAST_USED = "last used";
    private final String name;


    @Override
    public String toString() {
        return name;
    }


    static Map<String, OutputParameters> defaultValues = Map.of(
            "ipod nano", new Preset("ipod nano", new OutputParameters(Format.M4B, 64, 44100, 1, 10000, false, 2)),
            "ipod classic", new Preset("ipod classic", new OutputParameters(Format.M4B, 96, 44100, 2, 12000, true, 3)),
            "iphone", new Preset("iphone", new OutputParameters(Format.M4B, 128, 44100, 2, 12000, true, 4)),
            "android 5+", new Preset("android 5+", new OutputParameters(Format.OGG, 64, 44100, 2, 12000, true, 3)),
            "android old", new Preset("android old", new OutputParameters(Format.M4B, 96, 44100, 2, 10000, true, 3)),
            "legacy", new Preset("legacy", new OutputParameters(Format.MP3, 128, 44100, 2, 12000, true, 3))
    );

    public static final Preset DEFAULT_OUTPUT_PARAMETERS = new Preset(Preset.LAST_USED);


    public static List<Preset> loadPresets() {
        List<Preset> list = new ArrayList<>();
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

//    private final OutputParameters save;

    private Preset(String name, OutputParameters preset) {
        this.name = name;
        this.bitRate = preset.getBitRate();
        this.frequency = preset.getFrequency();
        this.channels = preset.getChannels();
        this.quality = preset.getQuality();
        this.cbr = preset.isCbr();
        this.cutoff = preset.getCutoff();
        this.format = preset.getFormat();
        saveProperty();
    }


    private Preset(String name) {
        this.name = name;
    }

    public static Preset copy(String presetName, Preset copy) {
        return new Preset(presetName, copy);
    }

    public static Preset instance(String presetName) {
        String property = AppProperties.getProperty("preset." + presetName);
        if (property != null) {
            Gson gson = new Gson();
            return gson.fromJson(property, Preset.class);
        }
        return new Preset(presetName, Objects.requireNonNullElseGet(defaultValues.get(presetName), OutputParameters::new));
    }

    private void saveProperty() {
        Gson gson = new Gson();
        String gsonString = gson.toJson(this);
        AppProperties.setProperty("preset." + name, gsonString);
    }


    @Override
    public void setupFormat(Format format) {
        super.setupFormat(format);
        saveProperty();
    }


    @Override
    public void setBitRate(int bitRate) {
        super.setBitRate(bitRate);
        saveProperty();
    }

    @Override
    public void setFrequency(int frequency) {
        super.setFrequency(frequency);
        saveProperty();
    }


    @Override
    public void setChannels(int channels) {
        super.setChannels(channels);
        saveProperty();
    }

    @Override
    public void setQuality(int quality) {
        super.setQuality(quality);
        saveProperty();
    }

    @Override
    public void setCbr(boolean cbr) {
        super.setCbr(cbr);
        saveProperty();
    }

    @Override
    public void updateAuto(List<MediaInfo> media) {
        if (!defaultValues.containsKey(name)) {
            super.updateAuto(media);
            saveProperty();
        } else {
            //Ignoring auto-update and save for all other preset
        }
    }

    @Override
    public void setCutoff(int cutoff) {
        super.setCutoff(cutoff);
        saveProperty();
    }

    public String getName() {
        return name;
    }
}
