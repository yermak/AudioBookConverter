package uk.yermak.audiobookconverter;

import net.bramp.ffmpeg.progress.ProgressParser;
import org.apache.commons.io.FileUtils;
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
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;


/**
 * Created by Yermak on 29-Dec-17.
 */
public class Utils {
    final static Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public static String getTmp(long jobId, long fileId, String extension) {
        return new File(System.getProperty("java.io.tmpdir"), "~abc_" + Version.getVersionString() + "_" + jobId + "_" + fileId + "." + extension).getAbsolutePath();
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

    /**
     * Invalid characters that cannot be used in filenames across most operating systems.
     */
    private static final char[] INVALID_FILENAME_CHARS = {':', '\\', '/', '>', '<', '|', '?', '*', '"'};

    /**
     * Creates a StringTemplate with registered renderers for Number and Duration.
     *
     * @param templateFormat the template format string
     * @return configured ST instance ready for use
     */
    private static ST createTemplate(String templateFormat) {
        STGroup group = new STGroupString("");
        group.registerRenderer(Number.class, new NumberRenderer());
        group.registerRenderer(Duration.class, new DurationRender());
        return new ST(group, templateFormat);
    }

    /**
     * Removes invalid filename characters from the given string.
     *
     * @param filename the filename to sanitize
     * @return sanitized filename safe for use across operating systems
     */
    private static String removeInvalidFilenameCharacters(String filename) {
        String result = filename;
        for (char c : INVALID_FILENAME_CHARS) {
            result = StringUtils.remove(result, c);
        }
        return result;
    }

    public static String renderChapter(Chapter chapter, Map<String, Function<Chapter, Object>> context) {
        String chapterFormat = Settings.loadSetting().getChapterFormat();
        ST chapterTemplate = createTemplate(chapterFormat);

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
        String filenameFormat = Settings.loadSetting().getFilenameFormat();
        ST filenameTemplate = createTemplate(filenameFormat);

        filenameTemplate.add("WRITER", bookInfo.writer().trimToNull());
        filenameTemplate.add("TITLE", bookInfo.title().trimToNull());
        filenameTemplate.add("SERIES", bookInfo.series().trimToNull());
        filenameTemplate.add("NARRATOR", bookInfo.narrator().trimToNull());
        filenameTemplate.add("BOOK_NUMBER", bookInfo.bookNumber().trimToNull());
        filenameTemplate.add("YEAR", bookInfo.year().trimToNull());

        String result = filenameTemplate.render();
        result = removeInvalidFilenameCharacters(result);

        if (StringUtils.isBlank(result)) {
            return "NewBook";
        } else {
            return result;
        }
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

    public static String renderPart(Part part, Map<String, Function<Part, Object>> context) {
        String partFormat = Settings.loadSetting().getPartFormat();
        ST partTemplate = createTemplate(partFormat);

        context.forEach((key, value) -> partTemplate.add(key, value.apply(part)));

        String result = partTemplate.render();
        result = removeInvalidFilenameCharacters(result);

        if (StringUtils.isBlank(result)) {
            return "NewBook";
        } else {
            return result;
        }
    }

    public static String cleanText(String text) {
        return StringUtils.remove(StringUtils.trim(text), '"');
    }

    static String formatWithLeadingZeros(int size, int i) {
        int digits = (int) (Math.log10(size) + 1);
        return String.format("%0" + digits + "d", i);
    }

    public static String propertiesToString(Properties properties) {
        StringBuilder stringBuilder = new StringBuilder();

        for (String key : properties.stringPropertyNames()) {
            String value = properties.getProperty(key);
            stringBuilder.append(key).append(": ").append(value).append(System.lineSeparator());
        }

        return stringBuilder.toString();
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
