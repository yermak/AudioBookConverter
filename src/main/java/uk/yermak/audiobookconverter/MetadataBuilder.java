//decompiled from MetadataBuilder.class
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

    private Logger logger() {
        return this.logger;
    }

    public File prepareMeta(final long jobId, final AudioBookInfo bookInfo, final List<MediaInfo> inputMedia) throws IOException {
        File metaFile = new File(System.getProperty("java.io.tmpdir"), "FFMETADATAFILE" + jobId);
        List<String> metaData = new ArrayList<>();
        metaData.add(";FFMETADATA1");
        metaData.add("major_brand=M4A");
        metaData.add("minor_version=512");
        metaData.add("compatible_brands=isomiso2");
        metaData.add("title=" + bookInfo.getTitle());
        metaData.add("artist=" + bookInfo.getWriter());
        if (StringUtils.isNotBlank(bookInfo.getSeries())) {
            metaData.add("album=" + bookInfo.getSeries());
        } else {
            metaData.add("album=" + bookInfo.getTitle());
        }

        metaData.add("composer=" + bookInfo.getNarrator());
        metaData.add("comment=" + bookInfo.getComment());
        metaData.add("track=" + bookInfo.getBookNumber() + "/" + bookInfo.getTotalTracks());
        metaData.add("media_type=2");
        metaData.add("genre=" + bookInfo.getGenre());
        metaData.add("encoder=https://github.com/yermak/AudioBookConverter");
        long totalDuration = 0;
        int i = 1;

        for (MediaInfo media : inputMedia) {
            metaData.add("[CHAPTER]");
            metaData.add("TIMEBASE=1/1000");
            metaData.add("START=" + totalDuration);
            totalDuration += media.getDuration();
            metaData.add("END=" + totalDuration);
            if (bookInfo.getBookNumber() != 0) {
                String chapter = "title=" + bookInfo.getBookNumber() + ". " + bookInfo.getTitle() + ": ";
                if (media.getBookInfo().getTitle() == null) {
                    chapter = chapter + "Chapter " + i;
                } else {
                    chapter = chapter + i + ". " + media.getBookInfo().getTitle();
                }
                metaData.add(chapter);
            } else if (media.getBookInfo().getTitle() == null) {
                metaData.add("title=Chapter " + i);
            } else {
                metaData.add("title=" + i + ". " + media.getBookInfo().getTitle());
            }
            i++;
        }
        FileUtils.writeLines(metaFile, "UTF-8", metaData);
        return metaFile;
    }
}

        