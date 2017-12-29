package uk.yermak.audiobookconverter;

import com.freeipodsoftware.abc.Util;

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
        if (uniqie) {
            return Util.makeFilenameUnique(result);
        }
        return result;
    }
}
