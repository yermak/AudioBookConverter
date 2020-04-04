package uk.yermak.audiobookconverter;


import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class OutputParameters {

    public List<String> getConcatOptions(String fileListFileName, String metaDataFileName, String progressUri, String outputFileName) {
        return format.getConcatOptions(fileListFileName, metaDataFileName, progressUri, outputFileName);
    }

    public List<String> getTranscodingOptions(MediaInfo mediaInfo, String progressUri, String outputFileName) {
        return format.getTranscodingOptions(mediaInfo, progressUri, outputFileName);
    }

    public List<String> getReencodingOptions(MediaInfo mediaInfo, String progressUri, String outputFileName) {
        return format.getReencodingOptions(mediaInfo, progressUri, outputFileName, this);
    }

    public boolean needReencode(String codec) {
        return format.needsReencode(codec);
    }

    public void setupFormat(String extension) {
        format = Format.instance(extension);
    }


    public enum Format {
        M4B("ipod", "aac") {
        }, MP3("mp3", "libmp3lame") {
            public List<String> getConcatOptions(String fileListFileName, String metaDataFileName, String progressUri, String outputFileName) {
                String[] strings = {FFMPEG,
                        "-protocol_whitelist", "file,pipe,concat",
                        "-vn",
                        "-f", "concat",
                        "-safe", "0",
                        "-i", fileListFileName,
                        "-f", format,
                        "-c:a", "copy",
                        "-progress", progressUri,
                        outputFileName};
                return Arrays.asList(strings);
            }
        }, OGG("ogg", "libopus") {

            @Override
            public List<String> getReencodingOptions(MediaInfo mediaInfo, String progressUri, String outputFileName, OutputParameters outputParameters) {
                List<String> options = new ArrayList<>();
                options.add(FFMPEG);
                options.add("-ss");
                options.add(toFFMpegTime(mediaInfo.getOffset()));
                options.add("-i");
                options.add(mediaInfo.getFileName());
                options.add("-vn");
                options.add("-codec:a");
                options.add(codec);
                options.add("-f");
                options.add(format);
                options.add(outputParameters.getFFMpegQualityParameter());
                options.add(outputParameters.getFFMpegQualityValue());
                options.add("-ac");
                options.add(String.valueOf(outputParameters.getFFMpegChannelsValue()));
                options.add("-t");
                options.add(toFFMpegTime(mediaInfo.getDuration()));
                options.add("-cutoff");
                options.add(outputParameters.getCutoffValue());
                options.add("-progress");
                options.add(progressUri);
                options.add(outputFileName);
                return options;
            }
        };

        protected String format;
        protected String codec;

        Format(String format, String codec) {
            this.format = format;
            this.codec = codec;
        }

        static String toFFMpegTime(long time) {
            return (time / 1000) + "." + time % 1000;
        }

        public static Format instance(String extension) {
            String ext = extension.toLowerCase();
            switch (ext) {
                case "m4b":
                    return Format.M4B;
                case "m4a":
                    return Format.M4B;
                case "mp3":
                    return Format.MP3;
                case "ogg":
                    return Format.OGG;
            }
            return Format.M4B;
        }

        public List<String> getReencodingOptions(MediaInfo mediaInfo, String progressUri, String outputFileName, OutputParameters outputParameters) {
            List<String> options = new ArrayList<>();
            options.add(FFMPEG);
            options.add("-ss");
            options.add(toFFMpegTime(mediaInfo.getOffset()));
            options.add("-i");
            options.add(mediaInfo.getFileName());
            options.add("-vn");
            options.add("-codec:a");
            options.add(codec);
            options.add("-f");
            options.add(format);
            options.add(outputParameters.getFFMpegQualityParameter());
            options.add(outputParameters.getFFMpegQualityValue());
            options.add("-ac");
            options.add(String.valueOf(outputParameters.getFFMpegChannelsValue()));
            options.add("-t");
            options.add(toFFMpegTime(mediaInfo.getDuration()));
            options.add("-cutoff");
            options.add(outputParameters.getCutoffValue());
            options.add("-progress");
            options.add(progressUri);
            options.add(outputFileName);
            return options;
        }

        public List<String> getTranscodingOptions(MediaInfo mediaInfo, String progressUri, String outputFileName) {
            String[] strings = {FFMPEG,
                    "-ss", toFFMpegTime(mediaInfo.getOffset()),
                    "-i", mediaInfo.getFileName(),
                    "-map_metadata", "-1",
                    "-map_chapters", "-1",
                    "-vn",
                    "-codec:a", "copy",
                    "-t", toFFMpegTime(mediaInfo.getDuration()),
                    "-f", format,
                    "-progress", progressUri,
                    outputFileName};
            return Arrays.asList(strings);
        }

        public List<String> getConcatOptions(String fileListFileName, String metaDataFileName, String progressUri, String outputFileName) {
            String[] strings = {FFMPEG,
                    "-protocol_whitelist", "file,pipe,concat",
                    "-vn",
                    "-f", "concat",
                    "-safe", "0",
                    "-i", fileListFileName,
                    "-i", metaDataFileName,
                    "-map_metadata", "1",
                    "-f", format,
                    "-c:a", "copy",
                    "-movflags", "+faststart",
                    "-progress", progressUri,
                    outputFileName};
            return Arrays.asList(strings);
        }


        public boolean needsReencode(String codec) {
            return !this.codec.equalsIgnoreCase(codec);
        }

        @Override
        public String toString() {
            return format + "[" + codec + "]";
        }
    }

    private final static String FFMPEG = new File("external/x64/ffmpeg.exe").getAbsolutePath();

    private int bitRate = 128;
    private int frequency = 44100;
    private int channels = 2;
    private int quality = 3;
    private boolean cbr = true;
    private int cutoff = 10000;
    private int volume = 100;
    Format format = Format.M4B;


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

        