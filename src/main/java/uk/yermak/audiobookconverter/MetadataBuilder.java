package uk.yermak.audiobookconverter;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.yermak.audiobookconverter.fx.util.SmartStringProperty;

import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;

public class MetadataBuilder {
    private final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final long jobId;
    private final AudioBookInfo bookInfo;
    private final Convertable convertable;
    private final List<ArtWork> posters;


    public MetadataBuilder(final long jobId, ConversionGroup conversionGroup, Convertable convertable) {
        this.jobId = jobId;
        this.bookInfo = conversionGroup.getBookInfo();
        this.posters = conversionGroup.getPosters();
        this.convertable = convertable;
    }

    public File prepareMp4MetaFile() {
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
            metaData.add("track=" + bookInfo.bookNumber() + "/" + bookInfo.totalTracks());
            metaData.add("media_type=2");
            metaData.add("genre=" + bookInfo.genre());
            metaData.addAll(convertable.getMetaData(bookInfo));
            logger.debug("Saving metadata:" + String.join("\n", metaData));
            FileUtils.writeLines(metaFile, "UTF-8", metaData);
            return metaFile;
        } catch (IOException e) {
            throw new ConversionException(e);
        }
    }

    public File prepareOggMetaFile() {
        try {
            File metaFile = new File(System.getProperty("java.io.tmpdir"), "FFMETADATAFILE" + jobId);

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

            metaData.add("track=" + bookInfo.bookNumber() + "/" + bookInfo.totalTracks());

            if (!posters.isEmpty()){
                FlacPicture picture = FlacPicture.load(posters.get(0).getFileName());
                metaData.add("metadata_block_picture="+picture.write());
            }
            metaData.addAll(convertable.getMetaData(bookInfo));
            logger.debug("Saving metadata:" + String.join("\n", metaData));
            FileUtils.writeLines(metaFile, "UTF-8", metaData);
            return metaFile;
        } catch (IOException e) {
            throw new ConversionException(e);
        }
    }

    public List<String> prepareId3v2Meta() {
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
        meta.add("track=\"" + convertable.getNumber() + "/" + convertable.getTotalNumbers() + "\"");
        return meta;
    }

    private String escapeQuotes(String text) {
        return StringUtils.replace(text, "\"", "");
    }

    private String escapeQuotes(SmartStringProperty text) {
        return escapeQuotes(text.getValueSafe());
    }

    public String getArtWorkFile() {
        if (posters.isEmpty()) return null;
        return posters.get(0).getFileName();
    }
}

        