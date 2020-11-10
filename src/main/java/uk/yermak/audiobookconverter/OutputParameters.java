package uk.yermak.audiobookconverter;


import java.util.Comparator;
import java.util.List;

public class OutputParameters {

    public static final Integer DEFAULT_CHANNELS = 2;
    public static final Integer DEFAULT_QUALITY = 3;
    public static final Integer DEFAULT_CUTOFF = 12000;
    public static final Integer DEFAULT_FREQUENCY = 44100;
    public static final Integer DEFAULT_BITRATE = 128;

    protected int bitRate = DEFAULT_BITRATE;
    protected int frequency = DEFAULT_FREQUENCY;
    protected int channels = DEFAULT_CHANNELS;
    protected int quality = DEFAULT_QUALITY;
    protected boolean cbr = true;
    protected int cutoff = DEFAULT_CUTOFF;
    protected Format format = Format.M4B;
    private boolean splitChapters = false;

    public OutputParameters(OutputParameters parameters) {
        this.bitRate = parameters.getBitRate();
        this.frequency = parameters.getFrequency();
        this.channels = parameters.getChannels();
        this.quality = parameters.getQuality();
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
        this.quality = quality;
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

    public int getQuality() {
        return this.quality;
    }

    public void setQuality(final int quality) {
        this.quality = quality;
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

    public String getFFMpegQualityParameter() {
        return this.cbr ? "-b:a" : "-vbr";
    }

    public String getFFMpegQualityValue() {
        return this.cbr ? this.getBitRate() + "k" : String.valueOf(this.quality);
    }

    public String getFFMpegChannelsValue() {
        return String.valueOf(this.getChannels());
    }

    public String getCutoffValue() {
        return Integer.toString(getCutoff());
    }

    public int getCutoff() {
        if (this.cbr) {
            return this.cutoff;
        } else {
            return switch (this.quality) {
                case 1 -> 13050;
                case 2 -> 13050;
                case 3 -> 14260;
                case 4 -> 15500;
                default -> 0;
            };
        }
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

