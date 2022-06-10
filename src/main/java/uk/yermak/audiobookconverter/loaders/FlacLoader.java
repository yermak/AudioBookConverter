package uk.yermak.audiobookconverter.loaders;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import uk.yermak.audiobookconverter.Utils;
import uk.yermak.audiobookconverter.book.AudioBookInfo;
import uk.yermak.audiobookconverter.book.MediaInfoBean;
import uk.yermak.audiobookconverter.book.Track;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class FlacLoader {

    static void parseCueChapters(MediaInfoBean mediaInfo) throws IOException {
        String filename = mediaInfo.getFileName();
        File file = new File(FilenameUtils.getFullPath(filename) + FilenameUtils.getBaseName(filename) + ".cue");
        if (file.exists()) {
            String cue = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
            parseCue(mediaInfo, cue);
        }
    }

    static void parseCue(MediaInfoBean mediaInfo, String cue) {
        AudioBookInfo bookInfo = mediaInfo.getBookInfo();
        String[] split = StringUtils.split(cue, "\n");
        for (String line : split) {
            int i = -1;
            if (bookInfo.tracks().isEmpty()) {
                if ((i = line.indexOf("GENRE")) != -1) bookInfo.genre().set(Utils.cleanText(line.substring(i + 5)));
                if ((i = line.indexOf("TITLE")) != -1) bookInfo.title().set(Utils.cleanText(line.substring(i + 5)));
                if ((i = line.indexOf("DATE")) != -1) bookInfo.year().set(Utils.cleanText(line.substring(i + 4)));
                if ((i = line.indexOf("PERFORMER")) != -1)
                    bookInfo.narrator().set(Utils.cleanText(line.substring(i + 9)));
            } else {
                Track track = bookInfo.tracks().get(bookInfo.tracks().size() - 1);
                if ((i = line.indexOf("TITLE")) != -1) track.setTitle(Utils.cleanText(line.substring(i + 5)));
                if ((i = line.indexOf("PERFORMER")) != -1) track.setWriter(Utils.cleanText(line.substring(i + 9)));
            }
            if ((i = line.indexOf("TRACK")) != -1) {
                bookInfo.tracks().add(new Track(Utils.cleanText(line.substring(i + 5))));
            } else {
                if ((i = line.indexOf("INDEX 01")) != -1) {
                    long time = parseCueTime(line.substring(i + 8));
                    if (bookInfo.tracks().size() > 1) {
                        Track track = bookInfo.tracks().get(bookInfo.tracks().size() - 2);
                        track.setEnd(time);
                    }
                    Track track = bookInfo.tracks().get(bookInfo.tracks().size() - 1);
                    track.setStart(time);
                }
            }
        }
        if (!bookInfo.tracks().isEmpty()) {
            bookInfo.tracks().get(bookInfo.tracks().size() - 1).setEnd(mediaInfo.getDuration());
        }
    }

    private static long parseCueTime(String substring) {
        String cleanText = Utils.cleanText(substring);
        String[] split = cleanText.split(":");
        return 1000 * (Integer.parseInt(split[0]) * 60 + Integer.parseInt(split[1])) + Integer.parseInt(split[2]) * 1000 / 75;
    }
}
