package uk.yermak.audiobookconverter.formats;


import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.yermak.audiobookconverter.MediaInfo;

import java.lang.invoke.MethodHandles;
import java.util.Comparator;
import java.util.List;

public class OutputParameters {

    final static Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    protected Format format = Format.M4B;
    protected Integer bitRate = format.defaultBitrate();
    protected Integer frequency = format.defaultFrequency();
    protected Integer channels = format.defaultChannel();
    protected Integer vbrQuality = format.defaultVbrQuality();
    protected boolean cbr = format.defaultCBR();
    protected Integer cutoff = format.defaultCutoff();
    protected transient SimpleObjectProperty<Double> speed = new SimpleObjectProperty<>(format.defaultSpeed());

    public enum Force {Auto, Always, Avoid}

    protected Force force = Force.Auto;

    private boolean splitChapters = false;


    public OutputParameters() {
    }

    public OutputParameters(Format format, int bitRate, int frequency, int channels, int cutoff, boolean cbr, int quality, double speed, Force force, boolean splitChapters) {
        this.format = format;
        this.bitRate = bitRate;
        this.frequency = frequency;
        this.channels = channels;
        this.vbrQuality = quality;
        this.cbr = cbr;
        this.cutoff = cutoff;
        this.speed.set(speed);
        this.force = force;
        this.splitChapters = splitChapters;
    }

    public OutputParameters(OutputParameters parameters) {
        this(parameters.getFormat(),
                parameters.getBitRate(),
                parameters.getFrequency(),
                parameters.getChannels(),
                parameters.getCutoff(), parameters.isCbr(),
                parameters.getVbrQuality(),
                parameters.getSpeed(),
                parameters.getForce(),
                parameters.isSplitChapters()
        );
    }


    public boolean needReencode(String codec) {
        return switch (force) {
            case Auto -> format.needsReencode(codec);
            case Always -> true;
            case Avoid -> !format.skipReedncode(codec);
        };
    }

    public void setupFormat(Format format) {
        this.format = format;
    }

    public Integer getBitRate() {
        return bitRate;
    }

    public void setBitRate(final Integer bitRate) {
        this.bitRate = findNearestMatch(bitRate, format.bitrates(), format.defaultBitrate());
    }

    public Integer getFrequency() {
        return this.frequency;
    }

    public void setFrequency(final Integer frequency) {
        this.frequency = findNearestMatch(frequency, format.frequencies(), format.defaultFrequency());
    }

    private static Integer findNearestMatch(int value, List<Integer> list, int defaultValue) {
        //TODO: replace with binary search
        for (Integer integer : list) {
            if (integer >= value)
                return integer;
        }
        return defaultValue;
    }

    public Integer getChannels() {
        return this.channels;
    }

    public void setChannels(final Integer channels) {
        this.channels = findNearestMatch(channels, format.channels(), format.defaultChannel());
    }

    public Integer getVbrQuality() {
        return this.vbrQuality;
    }

    public void setVbrQuality(final Integer vbrQuality) {
        this.vbrQuality = findNearestMatch(vbrQuality, format.vbrQualities(), format.defaultVbrQuality());
    }

    public Boolean isCbr() {
        return this.cbr;
    }

    public void setCbr(final Boolean cbr) {
        this.cbr = cbr;
    }

    public void updateAuto(final List<MediaInfo> media) {
        if (media.isEmpty()) return;

        Integer maxChannels = media.parallelStream().map(MediaInfo::getChannels).max(Comparator.naturalOrder()).get();
        Integer maxFrequency = media.parallelStream().map(MediaInfo::getFrequency).max(Comparator.naturalOrder()).get();
        Integer maxBitRate = media.parallelStream().map(MediaInfo::getBitrate).max(Comparator.naturalOrder()).get();

        setChannels(maxChannels);
        setFrequency(maxFrequency);
        setBitRate(maxBitRate / 1000);
    }

    public Integer getCutoff() {
        return this.cutoff;
    }

    public void setCutoff(final Integer cutoff) {
        this.cutoff = cutoff;
    }

    public Format getFormat() {
        return format;
    }

    public boolean isSplitChapters() {
        return splitChapters;
    }

    public void setSplitChapters(boolean splitChapters) {
        this.splitChapters = splitChapters;
    }

    public double getSpeed() {
        return speed.get();
    }

    public void setSpeed(double speed) {
        this.speed.set(speed);
    }

    public ObservableValue<Double> getSpeedObservable() {
        return speed;
    }

    protected void initSpeed() {
        speed = new SimpleObjectProperty<>(1.0);
    }

    public void setForce(Force force) {
        this.force = force;
    }

    public Force getForce() {
        return force;
    }
}

