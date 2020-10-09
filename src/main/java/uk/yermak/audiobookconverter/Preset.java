package uk.yermak.audiobookconverter;

public enum Preset {
    CUSTOM("custom", "m4b", 128, 44100, 2, 20000, true, 3),
    NANO ("ipod nano", "m4b", 64, 44100, 1, 10000, true, 3),
    CLASSIC ("ipod classic", "m4b", 96, 44100, 2, 12000, true, 3),
    IPHONE("iphone", "m4b", 128, 44100, 2, 14000, true, 3),
    OLD_ANDROID ("old android", "m4b", 64, 44100, 2, 10000, true, 3),
    ANDROID ("android 5+", "ogg", 96, 44100, 2, 12000, true, 3),
    LEGACY ("legacy", "mp3", 128, 44100, 2, 12000, true, 3);

    private final OutputParameters parameters = new OutputParameters();
    private String presetName;

    Preset(String name, String format, int bitRate, int frequency, int channels, int cutoff, boolean cbr, int quality) {
        presetName = name;
        parameters.setupFormat(format);
        parameters.setBitRate(bitRate);
        parameters.setFrequency(frequency);
        parameters.setChannels(channels);
        parameters.setCutoff(cutoff);
        parameters.setCbr(cbr);
        parameters.setQuality(quality);
    }

    public OutputParameters getOutputParameters() {
        return parameters;
    }

    public String presetName() {
        return presetName;
    }

    @Override
    public String toString() {
        return presetName;
    }
}
