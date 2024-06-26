package uk.yermak.audiobookconverter;

import uk.yermak.audiobookconverter.formats.Format;
import uk.yermak.audiobookconverter.formats.OutputParameters;

import java.util.List;
import java.util.Objects;

public class Preset extends OutputParameters {


    private final String name;


    @Override
    public String toString() {
        return name;
    }


    static List<Preset> defaultValues = List.of(
            new Preset("iphone", new OutputParameters(Format.M4B, 128, 44100, 2, 12000, true, 4, 1.0, Force.Auto, false)),
            new Preset("ipod nano", new OutputParameters(Format.M4B, 64, 44100, 1, 10000, false, 2, 1.0, Force.Auto, false)),
            new Preset("ipod classic", new OutputParameters(Format.M4B, 96, 44100, 2, 12000, true, 3, 1.0, Force.Auto, false)),
            new Preset("android 5+", new OutputParameters(Format.OGG, 64, 48000, 2, 12000, false, 3, 1.0, Force.Auto, false)),
            new Preset("android old", new OutputParameters(Format.M4B, 96, 44100, 2, 10000, true, 3, 1.0, Force.Auto, false)),
            new Preset("legacy", new OutputParameters(Format.MP3, 128, 44100, 2, 12000, true, 3, 1.0, Force.Auto, true))
    );


    public Preset(String name, OutputParameters preset) {
        super(preset.getFormat(), preset.getBitRate(), preset.getFrequency(), preset.getChannels(), preset.getCutoff(), preset.isCbr(), preset.getVbrQuality(),
                preset.getSpeed(), preset.getForce(), preset.isSplitChapters());
        this.name = name;
    }


    private Preset(String name) {
        this.name = name;
    }

    public static Preset copy(String presetName, OutputParameters copy) {
        return new Preset(presetName, copy);
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
