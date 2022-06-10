package uk.yermak.audiobookconverter.formats;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.yermak.audiobookconverter.*;
import uk.yermak.audiobookconverter.book.AudioBookInfo;
import uk.yermak.audiobookconverter.book.Convertable;

import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MP4Format extends Format {

    final static Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public static final List<Integer> FREQUENCIES = List.of(8000, 11025, 12000, 16000, 22050, 24000, 32000, 44100, 48000, 88200, 96000);
    public static final List<Integer> BITRATES = List.of(8, 16, 24, 32, 40, 48, 56, 64, 80, 96, 112, 128, 144, 160, 192, 224, 256, 320);

    public MP4Format(String extension) {
        super("ipod", "aac", extension, "alac");
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
    public Integer defaultBitrate() {
        return 128;
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
        return 12000;
    }

    @Override
    public Integer defaultFrequency() {
        return 44100;
    }

    @Override
    public Integer defaultVbrQuality() {
        return 3;
    }

    @Override
    public Boolean defaultCBR() {
        return true;
    }


    @Override
    public boolean mp4Compatible() {
        return true;
    }

    @Override
    public boolean ffmpegCompatible() {
        return false;
    }

    @Override
    protected void setBitRateOptions(List<String> options, OutputParameters outputParameters) {
        options.addAll(outputParameters.cbr
                ? List.of("-b:a", outputParameters.getBitRate() + "k")
                : List.of("-q:a", String.valueOf(0.5 + outputParameters.vbrQuality * outputParameters.vbrQuality * 0.06))
        );
    }

    @Override
    public List<String> getConcatOptions(String fileListFileName, String outputFileName, String progressUri, ConversionJob conversionJob) {

        String[] strings = {Platform.FFMPEG,
                "-protocol_whitelist", "file,pipe,concat",
                "-vn",
                "-f", "concat",
                "-safe", "0",
                "-i", fileListFileName,
                "-i", prepareMp4MetaFile(conversionJob),
                "-map_metadata", "1",
                "-f", format,
                "-c:a", "copy",
                "-progress", progressUri,
                outputFileName};
        return Arrays.asList(strings);
    }


    private String prepareMp4MetaFile(ConversionJob conversionJob) {
        AudioBookInfo bookInfo = conversionJob.getConversionGroup().getBookInfo();
        long jobId = conversionJob.getConversionGroup().getGroupId();
        Convertable convertable = conversionJob.getConvertable();

        try {
            File metaFile = new File(System.getProperty("java.io.tmpdir"), "FFMETADATAFILE" + jobId);
            metaFile.deleteOnExit();
            List<String> metaData = new ArrayList<>();
            metaData.add(";FFMETADATA1");
            metaData.add("major_brand=M4A");
            metaData.add("minor_version=512");
            metaData.add("compatible_brands=isomiso2");
            metaData.add("title=" + bookInfo.title() + (convertable.isTheOnlyOne() ? "" : ("-" + convertable.getNumber())));
            metaData.add("artist=" + bookInfo.writer());
            if (StringUtils.isNotBlank(bookInfo.series().get())) {
                metaData.add("album=" + bookInfo.series());
            } else {
                metaData.add("album=" + bookInfo.title());
            }
            metaData.add("composer=" + bookInfo.narrator());
            metaData.add("date=" + bookInfo.year());
            metaData.add("comment=" + bookInfo.comment());
            metaData.add("track=" + bookInfo.bookNumber());
            metaData.add("media_type=2");
            metaData.add("genre=" + bookInfo.genre());
            metaData.addAll(convertable.getMetaData(bookInfo));
            logger.debug("Saving metadata:" + String.join("\n", metaData));
            FileUtils.writeLines(metaFile, "UTF-8", metaData);
            return metaFile.getAbsolutePath();
        } catch (IOException e) {
            throw new ConversionException(e);
        }
    }

}
