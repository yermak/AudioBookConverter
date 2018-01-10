package uk.yermak.audiobookconverter;

import java.io.File;

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

    static String getTmp(long jobId, int index, String extension) {
        return new File(System.getProperty("java.io.tmpdir"), "~ABC-v2-" + jobId + "-" + index + extension).getAbsolutePath();
    }

}
