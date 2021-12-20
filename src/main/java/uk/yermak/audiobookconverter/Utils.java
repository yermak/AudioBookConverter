package uk.yermak.audiobookconverter;

import net.bramp.ffmpeg.progress.ProgressParser;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.stringtemplate.v4.*;

import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.text.DecimalFormat;
import java.time.Duration;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
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
        String chapterFormat = AppProperties.getProperty("chapter_format");
        if (chapterFormat == null) {
            chapterFormat = "<if(BOOK_NUMBER)><BOOK_NUMBER>. <endif>" +
                    "<if(BOOK_TITLE)><BOOK_TITLE>. <endif>" +
                    "<if(CHAPTER_TEXT)><CHAPTER_TEXT> <endif>" +
                    "<if(CHAPTER_NUMBER)><CHAPTER_NUMBER; format=\"%,03d\"> <endif>" +
                    "<if(TAG)><TAG> <endif>" +
                    "<if(CUSTOM_TITLE)><CUSTOM_TITLE> <endif>" +
                    "<if(DURATION)> - <DURATION; format=\"%02d:%02d:%02d\"><endif>";
            AppProperties.setProperty("chapter_format", chapterFormat);
        }
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
        String filenameFormat = AppProperties.getProperty("filename_format");
        if (filenameFormat == null) {
            filenameFormat = "<WRITER> <if(SERIES)> - [<SERIES><if(BOOK_NUMBER)> - <BOOK_NUMBER; format=\"%,02d\"><endif>] <endif> - <TITLE><if(NARRATOR)> (<NARRATOR>)<endif>";
            AppProperties.setProperty("filename_format", filenameFormat);
        }

        STGroup g = new STGroupString("");
        g.registerRenderer(Number.class, new NumberRenderer());
        g.registerRenderer(Duration.class, new DurationRender());

        ST filenameTemplate = new ST(g, filenameFormat);
        filenameTemplate.add("WRITER", bookInfo.writer().trimToNull());
        filenameTemplate.add("TITLE", bookInfo.title().trimToNull());
        filenameTemplate.add("SERIES", bookInfo.series().trimToNull());
        filenameTemplate.add("NARRATOR", bookInfo.narrator().trimToNull());
        filenameTemplate.add("BOOK_NUMBER", bookInfo.bookNumber().zeroToNull());
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
        String partFormat = AppProperties.getProperty("part_format");
        if (partFormat == null) {
            partFormat = "<if(WRITER)><WRITER> <endif>" +
                    "<if(SERIES)>- [<SERIES><if(BOOK_NUMBER)> -<BOOK_NUMBER><endif>] - <endif>" +
                    "<if(TITLE)><TITLE><endif>" +
                    "<if(NARRATOR)> (<NARRATOR>)<endif>" +
                    "<if(YEAR)>-<YEAR><endif>" +
                    "<if(PART)>, Part <PART; format=\"%,03d\"><endif>";
            AppProperties.setProperty("part_format", partFormat);
        }

        STGroup g = new STGroupString("");
        g.registerRenderer(Number.class, new NumberRenderer());
        ST partTemplate = new ST(g, partFormat);


        context.forEach((key, value) -> {
            partTemplate.add(key, value.apply(part));
        });

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


    public final static String FFMPEG = Environment.getPath("ffmpeg").replaceAll(" ", "\\ ");

    public static final String MP4ART = Environment.getPath("mp4art").replaceAll(" ", "\\ ");

    public static final String MP4INFO = Environment.getPath("mp4info").replaceAll(" ", "\\ ");

    public static final String FFPROBE = Environment.getPath("ffprobe");






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
