package uk.yermak.audiobookconverter.loaders;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.yermak.audiobookconverter.ConversionGroup;
import uk.yermak.audiobookconverter.Platform;
import uk.yermak.audiobookconverter.StreamCopier;
import uk.yermak.audiobookconverter.Utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class MP4v2ArtWorkChecker {
    final static Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private ConversionGroup conversionGroup;
    private String filename;


    public MP4v2ArtWorkChecker(ConversionGroup conversionGroup, String filename) {
        this.conversionGroup = conversionGroup;
        this.filename = filename;
    }

    public List<String> list() throws InterruptedException, IOException {
        Process process = null;
        try {
            if (conversionGroup.isOver() || conversionGroup.isStarted() || conversionGroup.isDetached())
                throw new InterruptedException("ArtWork loading was interrupted");
            ProcessBuilder pictureProcessBuilder = new ProcessBuilder(Platform.MP4ART,
                    "--list",
                    filename);
            process = pictureProcessBuilder.start();

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            StreamCopier.copy(process.getInputStream(), out);
            // not using redirectErrorStream() as sometimes error stream is not closed by process which cause feature to hang indefinitely
            ByteArrayOutputStream err = new ByteArrayOutputStream();
            StreamCopier.copy(process.getErrorStream(), err);

            boolean finished = false;
            while (!conversionGroup.isOver() && !finished) {
                finished = process.waitFor(500, TimeUnit.MILLISECONDS);
            }
            logger.debug("MP4Art Out: {}", out);
            logger.error("MP4Art Error: {}", err);

            List<String> result = parseMP4v2ArtList(out.toString());

            return result;
        } finally {
            Utils.closeSilently(process);
        }
    }

    @NotNull
    static List<String> parseMP4v2ArtList(String out) {
        ArrayList<String> result = new ArrayList<>();
        String[] lines = StringUtils.split(out, "\n");

        for (int i = 2; i < lines.length; i++) {
            String line = lines[i];
            if (StringUtils.isNotEmpty(line)) {
                String[] columns = StringUtils.split(line, " ");
                int imageId = Integer.parseInt(StringUtils.trimToEmpty(columns[0]));
                if (i - 2 == imageId) {
                    String fileType = StringUtils.trimToEmpty(columns[3]);
                    switch (fileType) {
                        case "implicit":
                            result.add("dat");
                            break;
                        case "jpeg":
                            result.add("jpg");
                            break;
                        default:
                            result.add(fileType);
                    }
                } else {
                    logger.error("Unexpected result parsing mp4art output for line :" + (i - 2) + ", received image number: " + imageId);
                }
            }
        }
        return result;
    }
}
