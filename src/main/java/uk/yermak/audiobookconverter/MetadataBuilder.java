package uk.yermak.audiobookconverter;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
            List<String> metaData = new ArrayList<>();
            metaData.add(";FFMETADATA1");
            metaData.add("major_brand=M4A");
            metaData.add("minor_version=512");
            metaData.add("compatible_brands=isomiso2");
            metaData.add("title=" + bookInfo.getTitle() + (convertable.isTheOnlyOne() ? "" : ("-" + convertable.getNumber())));
            metaData.add("artist=" + bookInfo.getWriter());
            if (StringUtils.isNotBlank(bookInfo.getSeries())) {
                metaData.add("album=" + bookInfo.getSeries());
            } else {
                metaData.add("album=" + bookInfo.getTitle());
            }
            metaData.add("composer=" + bookInfo.getNarrator());
            metaData.add("date=" + bookInfo.getYear());
            metaData.add("comment=" + bookInfo.getComment());
            metaData.add("track=" + bookInfo.getBookNumber() + "/" + bookInfo.getTotalTracks());
            metaData.add("media_type=2");
            metaData.add("genre=" + bookInfo.getGenre());
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

        if (StringUtils.isNotBlank(bookInfo.getWriter())) {
            meta.add("-metadata");
            meta.add("artist=\"" + escapeQuotes(bookInfo.getWriter()) + "\"");
        }
        if (StringUtils.isNotBlank(bookInfo.getTitle())) {
            meta.add("-metadata");
            meta.add("album=\"" + escapeQuotes(bookInfo.getTitle()) + "\"");
        }
        if (StringUtils.isNotBlank(bookInfo.getGenre())) {
            meta.add("-metadata");
            meta.add("genre=\"" + escapeQuotes(bookInfo.getGenre()) + "\"");
        }
        if (StringUtils.isNotBlank(bookInfo.getNarrator())) {
            meta.add("-metadata");
            meta.add("composer=\"" + escapeQuotes(bookInfo.getNarrator()) + "\"");
        }
        if (StringUtils.isNotBlank(bookInfo.getYear())) {
            meta.add("-metadata");
            meta.add("year=\"" + escapeQuotes(bookInfo.getYear()) + "\"");
        }
        if (StringUtils.isNotBlank(bookInfo.getComment())) {
            meta.add("-metadata");
            meta.add("comment=\"" + escapeQuotes(bookInfo.getComment()) + "\"");
        }
        meta.add("-metadata");
        meta.add("track=\"" + convertable.getNumber()+"/"+convertable.getTotalNumbers() + "\"");
        return meta;
    }

    private String escapeQuotes(String text) {
        return StringUtils.replace(text, "\"", "");
    }

    public String getArtWorkFile() {
        if (posters.isEmpty()) return null;
        return posters.get(0).getFileName();
    }
}

        