package uk.yermak.audiobookconverter.formats;

import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.yermak.audiobookconverter.*;
import uk.yermak.audiobookconverter.book.MediaInfo;

import java.lang.invoke.MethodHandles;
import java.util.*;

public abstract class Format {

    final static Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public static final List<Integer> CHANNELS = List.of(1, 2, 4, 6);
    public static final List<Integer> VBR_QUALITIES = List.of(1, 2, 3, 4, 5);
    public static final List<Integer> CUT_OFFS = List.of(4000, 6000, 8000, 12000, 20000);
    public static final List<Double> SPEEDS = List.of(0.5, 0.6, 0.7, 0.8, 0.9, 1.0, 1.1, 1.2, 1.3, 1.4, 1.5, 1.6, 1.7, 1.8, 1.9, 2.0);

    protected String format;
    protected String codec;
    protected String extension;
    private final String[] compatibleCodecs;

    public static Format M4B = new MP4Format("m4b");
    public static Format M4A = new MP4Format("m4a");
    public static Format MP3 = new MP3Format();
    public static Format OGG = new OGGFormat();

    Format(String format, String codec, String extension, String... compatibleCodecs) {
        this.format = format;
        this.codec = codec;
        this.extension = extension;
        this.compatibleCodecs = compatibleCodecs;
    }

    public static Format instance(String extension) {
        String ext = extension.toLowerCase();
        return switch (ext) {
            case "m4b" -> Format.M4B;
            case "m4a" -> Format.M4A;
            case "mp3" -> Format.MP3;
            case "ogg" -> Format.OGG;
            default -> Format.M4B;
        };
    }

    static String toFFMpegTime(long time) {
        return (time / 1000) + "." + time % 1000;
    }

    public List<Integer> channels() {
        return CHANNELS;
    }

    public List<Integer> vbrQualities() {
        return VBR_QUALITIES;
    }

    public List<Integer> cutoffs() {
        return CUT_OFFS;
    }

    public List<Double> speeds() {
        return SPEEDS;
    }

    public abstract List<Integer> frequencies();

    public abstract List<Integer> bitrates();

    public abstract Integer defaultBitrate();

    public abstract Double defaultSpeed();

    public abstract Integer defaultChannel();

    public abstract Integer defaultCutoff();

    public abstract Integer defaultFrequency();

    public abstract Boolean defaultCBR();


    public abstract List<String> getConcatOptions(String fileListFileName, String outputFileName, String progressUri, ConversionJob conversionJob);

    protected abstract void setBitRateOptions(List<String> options, OutputParameters outputParameters);

    public boolean needsReencode(String codec) {
        return !this.codec.equalsIgnoreCase(codec);
    }

    @Override
    public String toString() {
        return extension;
    }

    public abstract boolean mp4Compatible();

    public abstract boolean ffmpegCompatible();

    public abstract Integer defaultVbrQuality();

    public boolean skipReedncode(String codec) {
        if (compatibleCodecs == null) return false;
        return ArrayUtils.contains(compatibleCodecs, codec);
    }

    public List<String> getTranscodingOptions(MediaInfo mediaInfo, String progressUri, String outputFileName) {
        List<String> options = new ArrayList<>();
        options.add(Platform.FFMPEG);
        if (mediaInfo.getOffset() != -1) {
            options.add("-ss");
            options.add(toFFMpegTime(mediaInfo.getOffset()));
        }
        options.add("-i");
        options.add(mediaInfo.getFileName());
        options.add("-map_metadata");
        options.add("-1");
        options.add("-map_chapters");
        options.add("-1");
        options.add("-vn");
        options.add("-codec:a");
        options.add("copy");
        if (mediaInfo.getOffset() != -1) {
            options.add("-t");
            options.add(toFFMpegTime(mediaInfo.getDuration()));
        }
        options.add("-f");
        options.add(format);
        options.add("-progress");
        options.add(progressUri);
        options.add(outputFileName);
        return options;
    }

    public List<String> getReencodingOptions(MediaInfo mediaInfo, String progressUri, String outputFileName, OutputParameters outputParameters) {
        List<String> options = new ArrayList<>();
        options.add(Platform.FFMPEG);
        if (mediaInfo.getOffset() != -1) {
            options.add("-ss");
            options.add(toFFMpegTime(mediaInfo.getOffset()));
        }
        options.add("-i");
        options.add(mediaInfo.getFileName());
        options.add("-vn");
        options.add("-codec:a");
        options.add(codec);

        options.add("-map_metadata");
        options.add("-1");

        setBitRateOptions(options, outputParameters);

        options.add("-ac");
        options.add(String.valueOf(outputParameters.getChannels()));
        options.add("-ar");
        options.add(String.valueOf(outputParameters.getFrequency()));
        if (mediaInfo.getOffset() != -1) {
            options.add("-t");
            options.add(toFFMpegTime(mediaInfo.getDuration()));
        }
        if (outputParameters.getCutoff() != null) {
            options.add("-cutoff");
            options.add(Integer.toString(outputParameters.getCutoff()));
        }
        options.add("-f");
        options.add(format);

        if (outputParameters.getSpeed() != 1.0) {
            options.add("-filter:a");
            options.add("atempo=" + outputParameters.getSpeed());
        }

        options.add("-progress");
        options.add(progressUri);
        options.add(outputFileName);
        return options;
    }

}