package uk.yermak.audiobookconverter;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MetadataBuilder {

    protected static File prepareMeta(long jobId, AudioBookInfo bookInfo, List<MediaInfo> media) throws IOException {
        File metaFile = new File(System.getProperty("java.io.tmpdir"), "FFMETADATAFILE" + jobId);
        List<String> metaData = new ArrayList<>();

        metaData.add(";FFMETADATA1");
        metaData.add("major_brand=M4A");
        metaData.add("minor_version=512");
        metaData.add("compatible_brands=isomiso2");
        metaData.add("title=" + bookInfo.getTitle());
        metaData.add("artist=" + bookInfo.getWriter());
        metaData.add("album=" + (StringUtils.isNotBlank(bookInfo.getSeries()) ? bookInfo.getSeries() : bookInfo.getTitle()));
        metaData.add("composer=" + bookInfo.getNarrator());
        metaData.add("comment=" + bookInfo.getComment());
        metaData.add("track=" + bookInfo.getBookNumber() + "/" + bookInfo.getTotalTracks());
        metaData.add("media_type=2");
        metaData.add("genre=" + bookInfo.getGenre());
        metaData.add("encoder=" + "https://github.com/yermak/AudioBookConverter");

        long totalDuration = 0;
        for (int i = 0; i < media.size(); i++) {
            metaData.add("[CHAPTER]");
            metaData.add("TIMEBASE=1/1000");
            metaData.add("START=" + totalDuration);
            totalDuration += media.get(i).getDuration();
            metaData.add("END=" + totalDuration);
            metaData.add("title=" + (bookInfo.getBookNumber() != 0 ? (bookInfo.getBookNumber() + " ") : "") +
                    (bookInfo.getTitle().equals(bookInfo.getSeries()) ? "Chapter " : bookInfo.getTitle() + " ") + (i + 1));
        }
        FileUtils.writeLines(metaFile, "UTF-8", metaData);
        return metaFile;

    }
}
