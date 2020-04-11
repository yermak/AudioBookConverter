package uk.yermak.audiobookconverter;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.nio.charset.Charset;

/**
 * Created by Yermak on 04-Jan-18.
 */
public class Mp4v2InfoLoader {
    final static Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private static final String MP4INFO = new File("app/external/x64/mp4info.exe").getAbsolutePath();

    static long parseDuration(String info) {
        String[] lines = StringUtils.split(info, "\n");
        for (String line : lines) {
            if (StringUtils.isNotEmpty(line)) {
                String[] columns = StringUtils.split(line, ",");
                if (StringUtils.contains(columns[0], "audio")) {
                    for (int j = 1, columnsLength = columns.length; j < columnsLength; j++) {
                        String column = columns[j];
                        int k;
                        if ((k = column.indexOf(" sec")) != -1) {
                            String substring = column.substring(1, k);
                            return (long) (Double.parseDouble(substring) * 1000);
                        }
                    }
                }
            }
        }
        return 0;
    }

    public static void updateDuration(MediaInfo mediaInfo, String outputFileName) throws IOException {
        Process process = null;
        try {
            ProcessBuilder infoProcessBuilder = new ProcessBuilder(MP4INFO, outputFileName);
            process = infoProcessBuilder.start();
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            StreamCopier.copy(process.getInputStream(), out);
            StreamCopier.copy(process.getErrorStream(), System.err);
            String info = out.toString(Charset.defaultCharset());
            long duration = parseDuration(info);
            if (duration != 0) {
                mediaInfo.setDuration(duration);
            }
            System.out.println("info = " + info);
        } finally {
            Utils.closeSilently(process);
        }
    }
}
