package uk.yermak.audiobookconverter.formats;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.yermak.audiobookconverter.*;
import uk.yermak.audiobookconverter.fx.util.SmartStringProperty;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;

public class MP3Format extends Format {

    final static Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public static final List<Integer> CHANNELS = List.of(1, 2, 6);
    public static final List<Integer> FREQUENCIES = List.of(8000, 11025, 16000, 22050, 24000, 32000, 44100, 48000);
    public static final List<Integer> BITRATES = List.of(32, 48, 64, 96, 112, 128, 160, 192, 224, 256, 320);

    MP3Format() {
        super("mp3", "libmp3lame", "mp3");
    }

    @Override
    public List<Integer> channels() {
        return CHANNELS;
    }

    @Override
    public List<Integer> frequencies() {
        return FREQUENCIES;
    }

    @Override
    public List<Integer> bitrates() {
        return BITRATES;
    }

    @Override
    public Double defaultSpeed() {
        return 1.0;
    }

    @Override
    public Integer defaultChannel() {
        return 2;
    }

    @Override
    public Integer defaultCutoff() {
        return 8000;
    }

    @Override
    public Boolean defaultCBR() {
        return true;
    }

    @Override
    public Integer defaultBitrate() {
        return 96;
    }

    @Override
    public Integer defaultFrequency() {
        return 22050;
    }

    @Override
    public Integer defaultVbrQuality() {
        return 3;
    }



    @Override
    public boolean mp4Compatible() {
        return false;
    }

    @Override
    public boolean ffmpegCompatible() {
        return false;
    }

    protected void setBitRateOptions(List<String> options, OutputParameters outputParameters) {
        options.addAll(outputParameters.cbr
                ? List.of("-b:a", outputParameters.getBitRate() + "k")
                : List.of("-q:a", String.valueOf(10 - outputParameters.vbrQuality * 2))
        );
    }

    @Override
    public List<String> getConcatOptions(String fileListFileName, String outputFileName, String progressUri, ConversionJob conversionJob) {
        Convertable convertable = conversionJob.getConvertable();
        ConversionGroup conversionGroup = conversionJob.getConversionGroup();

        List<String> options = new ArrayList<>();
        options.add(Platform.FFMPEG);
        options.add("-protocol_whitelist");
        options.add("file,pipe,concat");
        options.add("-f");
        options.add("concat");
        options.add("-safe");
        options.add("0");
        options.add("-i");
        options.add(fileListFileName);

        if (conversionGroup.getPosters().isEmpty()) {
            options.add("-vn");
        } else {
            options.add("-i");
            options.add(conversionGroup.getPosters().get(0).getFileName());
            options.add("-c:v");
            options.add("copy");
            options.add("-map");
            options.add("0:0");
            options.add("-map");
            options.add("1:0");
        }
        options.add("-c:a");
        options.add("copy");
        options.add("-id3v2_version");
        options.add("4");

        options.addAll(prepareId3v2Meta(convertable, conversionGroup.getBookInfo()));
        options.add("-f");
        options.add(format);
        options.add("-progress");
        options.add(progressUri);
        options.add(outputFileName);

        return options;
    }


    private List<String> prepareId3v2Meta(Convertable convertable, AudioBookInfo bookInfo) {
        List<String> meta = new ArrayList<>();
        if (StringUtils.isNotBlank(convertable.getDetails())) {
            meta.add("-metadata");

            meta.add("title=\"" + escapeQuotes(convertable.getDetails()) + "\"");
        }

        if (bookInfo.writer().isNotBlank()) {
            meta.add("-metadata");
            meta.add("artist=\"" + escapeQuotes(bookInfo.writer()) + "\"");
        }
        if (bookInfo.title().isNotBlank()) {
            meta.add("-metadata");
            meta.add("album=\"" + escapeQuotes(bookInfo.title()) + "\"");
        }
        if (bookInfo.genre().isNotBlank()) {
            meta.add("-metadata");
            meta.add("genre=\"" + escapeQuotes(bookInfo.genre()) + "\"");
        }
        if (bookInfo.narrator().isNotBlank()) {
            meta.add("-metadata");
            meta.add("composer=\"" + escapeQuotes(bookInfo.narrator()) + "\"");
        }
        if (bookInfo.year().isNotBlank()) {
            meta.add("-metadata");
            meta.add("year=\"" + escapeQuotes(bookInfo.year()) + "\"");
        }
        if (bookInfo.comment().isNotBlank()) {
            meta.add("-metadata");
            meta.add("comment=\"" + escapeQuotes(bookInfo.comment()) + "\"");
        }
        meta.add("-metadata");
        meta.add("track=\"" + convertable.getNumber() + "\"");
        return meta;
    }

    private static String escapeQuotes(String text) {
        return StringUtils.replace(text, "\"", "");
    }

    private static String escapeQuotes(SmartStringProperty text) {
        return escapeQuotes(text.getValueSafe());
    }


}


