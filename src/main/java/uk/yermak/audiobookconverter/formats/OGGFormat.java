package uk.yermak.audiobookconverter.formats;

import com.google.common.collect.ImmutableMap;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.yermak.audiobookconverter.*;
import uk.yermak.audiobookconverter.book.ArtWork;
import uk.yermak.audiobookconverter.book.AudioBookInfo;
import uk.yermak.audiobookconverter.book.Convertable;

import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class OGGFormat extends Format {

    final static Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());


    private static final Map<Integer, Integer> VBR_MAP = ImmutableMap.of(1, 8, 2, 16, 3, 32, 4, 64, 5, 128);
    public static final List<Integer> FREQUENCIES = List.of(8000, 12000, 16000, 24000, 32000, 48000);
    public static final List<Integer> BITRATES = List.of(16, 24, 32, 48, 56, 64, 96, 112, 128, 144, 160, 192, 224, 256, 320, 512);

    OGGFormat() {
        super("ogg", "libopus", "ogg", "vorbis");
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
    public Integer defaultVbrQuality() {
        return 3;
    }

    @Override
    public Integer defaultBitrate() {
        return 64;
    }

    @Override
    public Integer defaultCutoff() {
        return 8000;
    }

    @Override
    public Integer defaultFrequency() {
        return 48000;
    }

    @Override
    public Boolean defaultCBR() {
        return false;
    }

    protected void setBitRateOptions(List<String> options, OutputParameters outputParameters) {
        if (outputParameters.cbr) {
            options.addAll(List.of("-b:a", outputParameters.getBitRate() + "k"));
            options.addAll(List.of("-vbr", "off"));
        } else {
            options.addAll(List.of("-b:a", VBR_MAP.get(outputParameters.getVbrQuality()) * outputParameters.getChannels() + "k"));
            options.addAll(List.of("-vbr", "on"));
        }
        options.add("-frame_duration");
        options.add("60");
    }

    @Override
    public boolean mp4Compatible() {
        return false;
    }

    @Override
    public boolean ffmpegCompatible() {
        return true;
    }


    @Override
    public List<String> getConcatOptions(String fileListFileName, String outputFileName, String progressUri, ConversionJob conversionJob) {
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
        options.add("-i");
        options.add(prepareOggMetaFile(conversionJob));
        options.add("-map_metadata");
        options.add("1");
        options.add("-c:a");
        options.add("copy");
        options.add("-vn");
        options.add("-f");
        options.add(format);
        options.add("-progress");
        options.add(progressUri);
        options.add(outputFileName);

        return options;
    }


    private String prepareOggMetaFile(ConversionJob  conversionJob) {
        AudioBookInfo bookInfo = conversionJob.getConversionGroup().getBookInfo();
        List<ArtWork> posters = conversionJob.getConversionGroup().getPosters();
        Convertable convertable = conversionJob.getConvertable();
        long groupId = conversionJob.getConversionGroup().getGroupId();

        try {
            File metaFile = new File(System.getProperty("java.io.tmpdir"), "FFMETADATAFILE" + groupId);


            List<String> metaData = new ArrayList<>();
            metaData.add(";FFMETADATA1");
            metaData.add("major_brand=OGG");
/*
            metaData.add("minor_version=512");
            metaData.add("compatible_brands=isomiso2");
*/

            metaData.add("title=" + bookInfo.title() + (convertable.isTheOnlyOne() ? "" : ("-" + convertable.getNumber())));

            if (bookInfo.writer().isNotBlank()) {
                metaData.add("artist=" + bookInfo.writer());
            }

            if (bookInfo.series().isNotBlank()) {
                metaData.add("album=" + bookInfo.series());
            } else if (bookInfo.title().isNotBlank()) {
                metaData.add("album=" + bookInfo.title());
            }
            if (bookInfo.narrator().isNotBlank()) {
                metaData.add("composer=" + bookInfo.narrator());
            }
            if (bookInfo.year().isNotBlank()) {
                metaData.add("date=" + bookInfo.year());
            }
            if (bookInfo.comment().isNotBlank()) {
                metaData.add("comment=" + bookInfo.comment());
            }
            if (bookInfo.genre().isNotBlank()) {
                metaData.add("genre=" + bookInfo.genre());
            }

            metaData.add("track=" + bookInfo.bookNumber());

            if (!posters.isEmpty()) {
                FlacPicture picture = FlacPicture.load(posters.get(0).getFileName());
                metaData.add("metadata_block_picture=" + picture.write());
            }
            metaData.addAll(convertable.getMetaData(bookInfo));
            logger.debug("Saving metadata:" + String.join("\n", metaData));
            FileUtils.writeLines(metaFile, "UTF-8", metaData);
            return metaFile.getAbsolutePath();
        } catch (IOException e) {
            throw new ConversionException(e);
        }
    }

}
