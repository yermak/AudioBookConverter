package uk.yermak.audiobookconverter;

import net.bramp.ffmpeg.progress.ProgressParser;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.stringtemplate.v4.ST;

import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.text.DecimalFormat;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Yermak on 29-Dec-17.
 */
public class Utils {
    final static Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    static String determineTempFilename(String inputFilename, final String extension, String prefix, final String suffix, boolean uniqie, String folder) {
        File file = new File(inputFilename);
        File outFile = new File(folder, prefix + file.getName());
        String result = outFile.getAbsolutePath().replaceAll("(?i)\\." + extension, "." + suffix);
        if (!result.endsWith("." + suffix)) {
            result = result + "." + suffix;
        }
        return result;
    }

    public static String getTmp(long jobId, int index, String extension) {
        return new File(System.getProperty("java.io.tmpdir"), "~ABC-" + Version.getVersionString() + "-" + jobId + "-" + index + extension).getAbsolutePath();
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

    public static void closeSilently(Future future) {
        if (future != null) {
            future.cancel(true);
        }
    }

    public static void closeSilently(ExecutorService executorService) {
        if (executorService != null) {
            executorService.shutdownNow();
        }
    }

    public static String formatChapter(int partNumber, Chapter chapter) {
        String chapterFormat = AppProperties.getProperty("chapter_format");
        if (chapterFormat == null) {
            chapterFormat = "<if(BOOK_NUMBER)> Book <BOOK_NUMBER>. <endif>Chapter <CHAPTER_NUMBER><if(CHAPTER_TITLE)>: <CHAPTER_TITLE><endif> - <DURATION>";
            AppProperties.setProperty("chapter_format", chapterFormat);
        }

        ST chapterTemplate = new ST(chapterFormat);
        chapterTemplate.add("BOOK_NUMBER", partNumber == 0 ? null : partNumber);
        chapterTemplate.add("CHAPTER_NUMBER", chapter.getNumber() == 0 ? null : chapter.getNumber());
        chapterTemplate.add("CHAPTER_TITLE", StringUtils.isEmpty(chapter.getCustomTitle()) ? null : chapter.getCustomTitle());
        chapterTemplate.add("DURATION", Utils.formatTime(chapter.getDuration()));
        return chapterTemplate.render();

    }


    public static String getOuputFilenameSuggestion(AudioBookInfo bookInfo) {
        String filenameFormat = AppProperties.getProperty("filename_format");
        if (filenameFormat == null) {
            filenameFormat = "<WRITER> <if(SERIES)>- [<SERIES><if(BOOK_NUMBER)> -<BOOK_NUMBER>]<endif>] <endif>- <TITLE><if(NARRATOR)> (<NARRATOR>)<endif>";
            AppProperties.setProperty("filename_format", filenameFormat);
        }

        ST filenameTemplate = new ST(filenameFormat);
        filenameTemplate.add("WRITER", StringUtils.isEmpty(bookInfo.getWriter()) ? null : bookInfo.getWriter());
        filenameTemplate.add("TITLE", StringUtils.isEmpty(bookInfo.getTitle()) ? null : bookInfo.getTitle());
        filenameTemplate.add("SERIES", StringUtils.isEmpty(bookInfo.getSeries()) ? null : bookInfo.getSeries());
        filenameTemplate.add("NARRATOR", StringUtils.isEmpty(bookInfo.getNarrator()) ? null : bookInfo.getNarrator());
        filenameTemplate.add("BOOK_NUMBER", bookInfo.getBookNumber() == 0 ? null : bookInfo.getBookNumber());

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
        return mp3Filename.replaceFirst("\\.\\w*$", ".m4b");
    }

    public static String makeFilenameUnique(String filename) {
        Pattern extPattern = Pattern.compile("\\.(\\w+)$");
        Matcher extMatcher = extPattern.matcher(filename);
        if (extMatcher.find()) {
            try {
                String extension = extMatcher.group(1);

                for (File outputFile = new File(filename); outputFile.exists(); outputFile = new File(filename)) {
                    Pattern pattern = Pattern.compile("(?i)(.*)\\((\\d+)\\)\\." + extension + "$");
                    Matcher matcher = pattern.matcher(filename);
                    if (matcher.find()) {
                        filename = matcher.group(1) + "(" + (Integer.parseInt(matcher.group(2)) + 1) + ")." + extension;
                    } else {
                        filename = filename.replaceAll("." + extension + "$", "(1)." + extension);
                    }
                }

                return filename;
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException("Cannot use filename" + " " + filename);
            }
        } else {
            throw new RuntimeException("Cannot use filename" + " " + filename + " (2)");
        }
    }

    public static long checksumCRC32(File file) {
        try {
            return FileUtils.checksumCRC32(file);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public static File getInitialDirecotory(String sourceFolder) {
        if (sourceFolder == null) {
            return new File(System.getProperty("user.home"));
        }
        File file = new File(sourceFolder);
        return file.exists() ? file : getInitialDirecotory(file.getParent());
    }

    public static String formatTime(double millis) {
        return formatTime((long) millis);
    }

    public static String formatTime(long millis) {
        String hms = String.format("%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(millis),
                TimeUnit.MILLISECONDS.toMinutes(millis) % TimeUnit.HOURS.toMinutes(1),
                TimeUnit.MILLISECONDS.toSeconds(millis) % TimeUnit.MINUTES.toSeconds(1));
        return hms;
    }

    public static String formatSize(long bytes) {
        if (bytes == -1L) {
            return "---";
        } else {
            DecimalFormat mbFormat = new DecimalFormat("0");
            return mbFormat.format((double) bytes / 1048576.0D) + " MB";
        }
    }
}
