package uk.yermak.audiobookconverter;


import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;

import java.util.Comparator;
import java.util.List;

public class OutputParameters {

    protected Format format = Format.M4B;
    protected Integer bitRate = format.defaultBitrate();
    protected Integer frequency = format.defaultFrequency();
    protected Integer channels = format.defaultChannel();
    protected Integer vbrQuality = format.defaultVbrQuality();
    protected boolean cbr = format.defaultCBR();
    protected Integer cutoff = format.defaultCutoff();
    protected transient SimpleObjectProperty<Double> speed = new SimpleObjectProperty(1.0);

    private boolean splitChapters = false;


    public OutputParameters(OutputParameters parameters) {
        this.bitRate = parameters.getBitRate();
        this.frequency = parameters.getFrequency();
        this.channels = parameters.getChannels();
        this.vbrQuality = parameters.getVbrQuality();
        this.cbr = parameters.isCbr();
        this.cutoff = parameters.getCutoff();
        this.format = parameters.getFormat();
    }

    OutputParameters() {
    }

    OutputParameters(Format format, int bitRate, int frequency, int channels, int cutoff, boolean cbr, int quality) {
        this.format = format;
        this.bitRate = bitRate;
        this.frequency = frequency;
        this.channels = channels;
        this.vbrQuality = quality;
        this.cbr = cbr;
        this.cutoff = cutoff;
    }


    public boolean needReencode(String codec) {
        return format.needsReencode(codec);
    }

    public void setupFormat(Format format) {
        this.format = format;
    }

    public Integer getBitRate() {
        return bitRate;
    }

    public void setBitRate(final Integer bitRate) {
        this.bitRate = bitRate;
    }

    public Integer getFrequency() {
        return this.frequency;
    }

    public void setFrequency(final Integer frequency) {
        this.frequency = frequency;
    }

    public Integer getChannels() {
        return this.channels;
    }

    public void setChannels(final Integer channels) {
        this.channels = channels;
    }

    public Integer getVbrQuality() {
        return this.vbrQuality;
    }

    public void setVbrQuality(final Integer vbrQuality) {
        this.vbrQuality = vbrQuality;
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
}

