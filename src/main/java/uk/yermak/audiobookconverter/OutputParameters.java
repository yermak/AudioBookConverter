package uk.yermak.audiobookconverter;


import java.util.Comparator;
import java.util.List;

public class OutputParameters {

    protected Format format = Format.M4B;
    protected int bitRate = format.defaultBitrate();
    protected int frequency = format.defaultFrequency();
    protected int channels = format.defaultChannel();
    protected int vbrQuality = format.defaultVbrQuality();
    protected boolean cbr = format.defaultCBR();
    protected int cutoff = format.defaultCutoff();

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

    public int getBitRate() {
        return bitRate;
    }

    public void setBitRate(final int bitRate) {
        this.bitRate = bitRate;
    }

    public int getFrequency() {
        return this.frequency;
    }

    public void setFrequency(final int frequency) {
        this.frequency = frequency;
    }

    public int getChannels() {
        return this.channels;
    }

    public void setChannels(final int channels) {
        this.channels = channels;
    }

    public int getVbrQuality() {
        return this.vbrQuality;
    }

    public void setVbrQuality(final int vbrQuality) {
        this.vbrQuality = vbrQuality;
    }

    public boolean isCbr() {
        return this.cbr;
    }

    public void setCbr(final boolean cbr) {
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

    public String getCutoffValue() {
        return Integer.toString(getCutoff());
    }

    public int getCutoff() {
        return this.cutoff;
    }

    public void setCutoff(final int cutoff) {
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
}

