package uk.yermak.audiobookconverter;

import java.util.List;

public class OutputParameters {

    private int bitRate = 128;
    private int frequency = 44100;
    private int channels = 2;
    private int quality = 3;
    private boolean cbr = true;
    private boolean auto = true;
    private int parts = 1;
    private int cutoff = 10000;

    public int getBitRate() {
        return bitRate;
    }

    public void setBitRate(int bitRate) {
        this.bitRate = bitRate;
    }

    public int getFrequency() {
        return frequency;
    }

    public void setFrequency(int frequency) {
        this.frequency = frequency;
    }

    public int getChannels() {
        return channels;
    }

    public void setChannels(int channels) {
        this.channels = channels;
    }

    public int getQuality() {
        return quality;
    }

    public void setQuality(int quality) {
        this.quality = quality;
    }

    public boolean isCbr() {
        return cbr;
    }

    public void setCbr(boolean cbr) {
        this.cbr = cbr;
    }

    public boolean isAuto() {
        return auto;
    }

    public void setAuto(boolean auto) {
        this.auto = auto;
    }

    public int getParts() {
        return parts;
    }

    public void setParts(int parts) {
        this.parts = parts;
    }

    public void updateAuto(List<MediaInfo> media) {
        if (!auto) return;

        int maxChannels = 0;
        int maxFrequency = 0;
        int maxBitrate = 0;

        //TODO try to get source media quality parameter
        /*
        for (MediaInfo mediaInfo : media) {
            if (mediaInfo.getChannels() > maxChannels) maxChannels = mediaInfo.getChannels();
            if (mediaInfo.getFrequency() > maxFrequency) maxFrequency = mediaInfo.getFrequency();
            if (mediaInfo.getBitrate() > maxBitrate) maxBitrate = mediaInfo.getBitrate();
        }
        setChannels(maxChannels);
        setFrequency(maxFrequency);

        if (cbr) {
            setBitRate(maxBitrate / 1000);
        }
        */
    }

    public String getFFMpegQualityParameter() {
        return cbr ? "-b:a" : "-vbr";
    }

    public String getFFMpegQualityValue() {
        return cbr ? String.valueOf(getBitRate()) + "k" : String.valueOf(quality);
    }

    public String getFFMpegFrequencyValue() {
        return String.valueOf(getFrequency());
    }

    public String getFFMpegChannelsValue() {
        return String.valueOf(getChannels());
    }

    public String getCutoffValue() {
        if (cbr) {
            return String.valueOf(cutoff);
        }
        switch (quality) {
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

    public void setCutoff(int cutoff) {
        this.bitRate = cutoff;
    }

    public int getCutoff() {
        return bitRate;
    }
}
