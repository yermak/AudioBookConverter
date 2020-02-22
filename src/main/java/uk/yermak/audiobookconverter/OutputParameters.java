package uk.yermak.audiobookconverter;


import java.util.List;

public class OutputParameters {
    private int bitRate = 128;
    private int frequency = 44100;
    private int channels = 2;
    private int quality = 3;
    private boolean cbr = true;
    private int cutoff = 10000;
    private int volume = 100;

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


    //TODO reconsider
    public void updateAuto(final List<MediaInfo> media) {
            int maxChannels = 0;
            int maxFrequency = 0;
            int maxBitRate = 0;

            if (getChannels() > maxChannels) {
                maxChannels = getChannels();
            }
            if (getFrequency() > maxFrequency) {
                maxFrequency = getFrequency();
            }
            if (getBitRate() > maxBitRate) {
                maxBitRate = getBitRate();
            }
            this.setChannels(maxChannels);
            this.setFrequency(maxFrequency);

            if (this.cbr) {
                this.setBitRate(maxBitRate / 1000);
            }
    }

    public String getFFMpegQualityParameter() {
        return this.cbr ? "-b:a" : "-vbr";
    }

    public String getFFMpegQualityValue() {
        return this.cbr ? this.getBitRate() + "k" : String.valueOf(this.quality);
    }

    public String getFFMpegFrequencyValue() {
        return String.valueOf(this.getFrequency());
    }

    public String getFFMpegChannelsValue() {
        return String.valueOf(this.getChannels());
    }

    public String getCutoffValue() {
        if (this.cbr) {
            return String.valueOf(this.cutoff);
        } else {
            switch (this.quality) {
                case 1:
                    return "13050";
                case 2:
                    return "13050";
                case 3:
                    return "14260";
                case 4:
                    return "15500";
                default:
                    return "0";
            }
        }
    }

    public void setCutoff(final int cutoff) {
        this.cutoff = cutoff;
    }
}

        