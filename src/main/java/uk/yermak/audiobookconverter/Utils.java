package uk.yermak.audiobookconverter;

import net.bramp.ffmpeg.progress.ProgressParser;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Yermak on 29-Dec-17.
 */
public class Utils {
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
        return new File(System.getProperty("java.io.tmpdir"), "~ABC-v2-" + jobId + "-" + index + extension).getAbsolutePath();
    }

    public static void closeSilently(ProgressParser progressParser) {
        if (progressParser != null) {
            try {
                progressParser.stop();
            } catch (IOException e) {
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

    public static String getOuputFilenameSuggestion(String fileName, AudioBookInfo bookInfo) {
        StringBuilder builder = new StringBuilder();
        if (StringUtils.isNotBlank(bookInfo.getWriter())) {
            builder
                    .append(StringUtils.trim(bookInfo.getWriter()));

        }
        if (StringUtils.isNotBlank(bookInfo.getSeries()) && !StringUtils.equals(bookInfo.getSeries(), bookInfo.getTitle())) {
            builder
                    .append(" - [")
                    .append(StringUtils.trim(bookInfo.getSeries()));
            if (bookInfo.getBookNumber() > 0) {
                builder
                        .append(" - ")
                        .append(bookInfo.getBookNumber());
            }
            builder.append("] ");
        }
        if (StringUtils.isNotBlank(bookInfo.getTitle())) {
            builder
                    .append(" - ")
                    .append(StringUtils.trim(bookInfo.getTitle()));
        }
        if (StringUtils.isNotBlank(bookInfo.getNarrator())) {
            builder
                    .append(" (")
                    .append(StringUtils.trim(bookInfo.getNarrator()))
                    .append(")");
        }
        String result = builder.toString();
        String mp3Filename;

        if (StringUtils.isBlank(result)) {
            mp3Filename = fileName;
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
            } catch (Exception var7) {
                throw new RuntimeException("Cannot use filename" + " " + filename);
            }
        } else {
            throw new RuntimeException("Cannot use filename"+ " " + filename + " (2)");
        }
    }

    public static long checksumCRC32(File file) {
        try {
            return FileUtils.checksumCRC32(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
