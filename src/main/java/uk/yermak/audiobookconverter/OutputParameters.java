package uk.yermak.audiobookconverter;


import com.google.gson.Gson;

import java.util.Comparator;
import java.util.List;

public class OutputParameters {

    public static final Integer DEFAULT_CHANNELS = 2;
    public static final Integer DEFAULT_QUALITY = 3;
    public static final Integer DEFAULT_CUTOFF = 12000;
    public static final Integer DEFAULT_FREQUENCY = 44100;
    public static final Integer DEFAULT_BITRATE = 128;

    private int bitRate = DEFAULT_BITRATE;
    private int frequency = DEFAULT_FREQUENCY;
    private int channels = DEFAULT_CHANNELS;
    private int quality = DEFAULT_QUALITY;
    private boolean cbr = true;
    private int cutoff = DEFAULT_CUTOFF;
    Format format = Format.M4B;

    public static final OutputParameters customInstance = new SavableOutputParameters();

    public OutputParameters(OutputParameters parameters) {
        this.bitRate = parameters.bitRate;
        this.frequency = parameters.frequency;
        this.channels = parameters.channels;
        this.quality = parameters.quality;
        this.cbr = parameters.cbr;
        this.cutoff = parameters.cutoff;
        this.format = parameters.format;
    }

    OutputParameters() {
    }

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

    public String getFormat() {
        return format.extension;
    }

    static class SavableOutputParameters extends OutputParameters {
        private OutputParameters save;

        public SavableOutputParameters() {

            String property = AppProperties.getProperty("preset.custom");
            if (property == null) {
                save = new OutputParameters();
                saveProperty();
            } else {
                Gson gson = new Gson();
                save = gson.fromJson(property, OutputParameters.class);
            }
        }

        private void saveProperty() {
            Gson gson = new Gson();
            String gsonString = gson.toJson(save);
            AppProperties.setProperty("preset.custom", gsonString);
        }

        @Override
        public boolean needReencode(String codec) {
            return save.needReencode(codec);
        }

        @Override
        public void setupFormat(String extension) {
            save.setupFormat(extension);
            saveProperty();
        }

        @Override
        public int getBitRate() {
            return save.getBitRate();
        }

        @Override
        public void setBitRate(int bitRate) {
            save.setBitRate(bitRate);
            saveProperty();
        }

        @Override
        public int getFrequency() {
            return save.getFrequency();
        }

        @Override
        public void setFrequency(int frequency) {
            save.setFrequency(frequency);
            saveProperty();
        }

        @Override
        public int getChannels() {
            return save.getChannels();
        }

        @Override
        public void setChannels(int channels) {
            save.setChannels(channels);
            saveProperty();
        }

        @Override
        public int getQuality() {
            return save.getQuality();
        }

        @Override
        public void setQuality(int quality) {
            save.setQuality(quality);
            saveProperty();
        }

        @Override
        public boolean isCbr() {
            return save.isCbr();
        }

        @Override
        public void setCbr(boolean cbr) {
            save.setCbr(cbr);
            saveProperty();
        }

        @Override
        public void updateAuto(List<MediaInfo> media) {
            save.updateAuto(media);
            saveProperty();
        }

        @Override
        public String getFFMpegQualityParameter() {
            return save.getFFMpegQualityParameter();
        }

        @Override
        public String getFFMpegQualityValue() {
            return save.getFFMpegQualityValue();
        }

        @Override
        public String getFFMpegChannelsValue() {
            return save.getFFMpegChannelsValue();
        }

        @Override
        public String getCutoffValue() {
            return save.getCutoffValue();
        }

        @Override
        public int getCutoff() {
            return save.getCutoff();
        }

        @Override
        public void setCutoff(int cutoff) {
            save.setCutoff(cutoff);
            saveProperty();
        }

        @Override
        public String getFormat() {
            return save.getFormat();
        }
    }

}

