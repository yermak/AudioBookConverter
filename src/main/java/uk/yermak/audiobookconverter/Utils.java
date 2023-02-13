package uk.yermak.audiobookconverter;

import net.bramp.ffmpeg.progress.ProgressParser;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.stringtemplate.v4.*;
import uk.yermak.audiobookconverter.book.AudioBookInfo;
import uk.yermak.audiobookconverter.book.Chapter;
import uk.yermak.audiobookconverter.book.Part;

import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.text.DecimalFormat;
import java.time.Duration;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * Created by Yermak on 29-Dec-17.
 */
public class Utils {
    final static Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public static String getTmp(long jobId, long fileId, String extension) {
        return new File(System.getProperty("java.io.tmpdir"), "~" + Version.getVersionString() + "_" + jobId + "_" + fileId + "." + extension).getAbsolutePath();
    }

    public static void closeSilently(ProgressParser progressParser) {
        if (progressParser != null) {
            try {
                progressParser.stop();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void closeSilently(Process process) {
        if (process != null) {
            process.destroyForcibly();
        }
    }

    public static String renderChapter(Chapter chapter, Map<String, Function<Chapter, Object>> context) {
        String chapterFormat = AppSetting.getProperty(AppSetting.CHAPTER_FORMAT, AppSetting.CHAPTER_FORMAT_DEFAULT);
        STGroup g = new STGroupString("");
        g.registerRenderer(Number.class, new NumberRenderer());
        g.registerRenderer(Duration.class, new DurationRender());
        ST chapterTemplate = new ST(g, chapterFormat);

        context.forEach((key, value) -> {
            if (key.contains("TAG")) {
                chapterTemplate.add("TAG", value.apply(chapter));
            } else {
                chapterTemplate.add(key, value.apply(chapter));
            }
        });
        return chapterTemplate.render();
    }


    public static String getOuputFilenameSuggestion(AudioBookInfo bookInfo) {
        String filenameFormat = AppSetting.getProperty(AppSetting.FILENAME_FORMAT, AppSetting.FILENAME_FORMAT_DEFAULT);

        STGroup g = new STGroupString("");
        g.registerRenderer(Number.class, new NumberRenderer());
        g.registerRenderer(Duration.class, new DurationRender());

        ST filenameTemplate = new ST(g, filenameFormat);
        filenameTemplate.add("WRITER", bookInfo.writer().trimToNull());
        filenameTemplate.add("TITLE", bookInfo.title().trimToNull());
        filenameTemplate.add("SERIES", bookInfo.series().trimToNull());
        filenameTemplate.add("NARRATOR", bookInfo.narrator().trimToNull());
        filenameTemplate.add("BOOK_NUMBER", bookInfo.bookNumber().trimToNull());
        filenameTemplate.add("YEAR", bookInfo.year().trimToNull());

        String result = filenameTemplate.render();
        char[] toRemove = new char[]{':', '\\', '/', '>', '<', '|', '?', '*', '"'};
        for (char c : toRemove) {
            result = StringUtils.remove(result, c);
        }
        String mp3Filename;

        if (StringUtils.isBlank(result)) {
            mp3Filename = "NewBook";
        } else {
            mp3Filename = result;
        }
        return mp3Filename;
//        return mp3Filename.replaceFirst("\\.\\w*$", ".m4b");
    }

    public static long checksumCRC32(File file) {
        try {
            return FileUtils.checksumCRC32(file);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public static String formatTime(double millis) {
        return formatTime((long) millis);
    }

    public static String formatTime(long millis) {
        return String.format("%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(millis),
                TimeUnit.MILLISECONDS.toMinutes(millis) % TimeUnit.HOURS.toMinutes(1),
                TimeUnit.MILLISECONDS.toSeconds(millis) % TimeUnit.MINUTES.toSeconds(1));
    }

    public static String formatSize(long bytes) {
        if (bytes == -1L) {
            return "---";
        } else {
            DecimalFormat mbFormat = new DecimalFormat("0");
            return mbFormat.format((double) bytes / 1048576.0D) + " MB";
        }
    }

    public static String tempCopy(String fileName) {
        File destFile = new File(getTmp(System.currentTimeMillis(), 0, FilenameUtils.getExtension(fileName)));
        try {
            FileUtils.copyFile(new File(fileName), destFile);
            return destFile.getPath();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String renderPart(Part part, Map<String, Function<Part, Object>> context) {
        String partFormat = AppSetting.getProperty(AppSetting.PART_FORMAT, AppSetting.PART_FORMAT_DEFAULT);
        STGroup g = new STGroupString("");
        g.registerRenderer(Number.class, new NumberRenderer());
        ST partTemplate = new ST(g, partFormat);

        context.forEach((key, value) -> partTemplate.add(key, value.apply(part)));

        String result = partTemplate.render();
        char[] toRemove = new char[]{':', '\\', '/', '>', '<', '|', '?', '*', '"'};
        for (char c : toRemove) {
            result = StringUtils.remove(result, c);
        }
        String mp3Filename;

        if (StringUtils.isBlank(result)) {
            mp3Filename = "NewBook";
        } else {
            mp3Filename = result;
        }
        return mp3Filename;

    }

    public static String cleanText(String text) {
        return StringUtils.remove(StringUtils.trim(text), '"');
    }

    static String formatWithLeadingZeros(int size, int i) {
        int digits =  (int) (Math.log10(size) + 1);
        return String.format("%0"+digits+"d", i);
    }


    private static class DurationRender implements AttributeRenderer<Duration> {

        @Override
        public String toString(Duration duration, String format, Locale locale) {
            if (format == null) {
                format = "%02d:%02d:%02d";
            }
            return String.format(format, duration.toHoursPart(),
                    duration.toMinutesPart(),
                    duration.toSecondsPart());
        }
    }
}
