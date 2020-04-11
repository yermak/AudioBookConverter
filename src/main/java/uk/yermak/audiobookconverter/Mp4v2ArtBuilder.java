package uk.yermak.audiobookconverter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by Yermak on 04-Jan-18.
 */
public class Mp4v2ArtBuilder {
    final static Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private static final String MP4ART = new File("app/external/x64/mp4art.exe").getAbsolutePath();
    private Conversion conversion;

    public Mp4v2ArtBuilder(Conversion conversion) {
        this.conversion = conversion;
    }


    public void coverArt(String outputFileName) throws IOException, InterruptedException {
        List<ArtWork> posters = conversion.getPosters();

        int i = 0;
        for (ArtWork poster : posters) {
            if (conversion.getStatus().isOver()) break;
            updateSinglePoster(poster, i++, outputFileName);
        }
    }

    public void updateSinglePoster(ArtWork poster, int index, String outputFileName) throws IOException, InterruptedException {
        Process process = null;
        try {
            ProcessBuilder artProcessBuilder = new ProcessBuilder(MP4ART,
                    "--art-index", String.valueOf(index),
                    "--add", "\"" + poster.getFileName() + "\"",
                    outputFileName);

            process = artProcessBuilder.start();

            StreamCopier.copy(process.getInputStream(), System.out);
            StreamCopier.copy(process.getErrorStream(), System.err);
            boolean finished = false;
            while (!conversion.getStatus().isOver() && !finished) {
                finished = process.waitFor(500, TimeUnit.MILLISECONDS);
            }
        } finally {
            Utils.closeSilently(process);
        }
    }
}
