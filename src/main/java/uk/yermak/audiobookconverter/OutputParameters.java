package uk.yermak.audiobookconverter;


import java.util.Comparator;
import java.util.List;

public class OutputParameters {

    private int bitRate = 128;
    private int frequency = 44100;
    private int channels = 2;
    private int quality = 3;
    private boolean cbr = true;
    private int cutoff = 10000;
    private final int volume = 100;
    public Format format = Format.M4B;


    public boolean needReencode(String codec) {
        return format.needsReencode(codec);
    }

    public void setupFormat(String extension) {
        format = Format.instance(extension);
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
        if (this.cbr) {
            return String.valueOf(this.cutoff);
        } else {
            return switch (this.quality) {
                case 1 -> "13050";
                case 2 -> "13050";
                case 3 -> "14260";
                case 4 -> "15500";
                default -> "0";
            };
        }
    }

    public void setCutoff(final int cutoff) {
        this.cutoff = cutoff;
    }


    public int getCutoff() {
        return cutoff;
    }

    public boolean getCbr() {
        return cbr;
    }
}

