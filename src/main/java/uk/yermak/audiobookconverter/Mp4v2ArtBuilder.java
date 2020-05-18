package uk.yermak.audiobookconverter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by Yermak on 04-Jan-18.
 */
public class Mp4v2ArtBuilder {
    final static Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final ConversionJob conversionJob;

    public Mp4v2ArtBuilder(ConversionJob conversionJob) {
        this.conversionJob = conversionJob;
    }


    public void coverArt(String outputFileName) throws IOException, InterruptedException {
        List<ArtWork> posters = conversionJob.getConversionGroup().getPosters();

        int i = 0;
        for (ArtWork poster : posters) {
            if (conversionJob.getStatus().isOver()) break;
            updateSinglePoster(poster, i++, outputFileName);
        }
    }

    public void updateSinglePoster(ArtWork poster, int index, String outputFileName) throws IOException, InterruptedException {
        Process process = null;
        try {
            ProcessBuilder artProcessBuilder = new ProcessBuilder(Utils.MP4ART,
                    "--art-index", String.valueOf(index),
                    "--add", "\"" + poster.getFileName() + "\"",
                    outputFileName);

            process = artProcessBuilder.start();


            ByteArrayOutputStream out = new ByteArrayOutputStream();
            StreamCopier.copy(process.getInputStream(), out);
            ByteArrayOutputStream err = new ByteArrayOutputStream();
            StreamCopier.copy(process.getErrorStream(), err);

            boolean finished = false;
            while (!conversionJob.getStatus().isOver() && !finished) {
                finished = process.waitFor(500, TimeUnit.MILLISECONDS);
            }
            logger.debug("Poster Out: {}", out.toString());
            logger.error("Poster Error: {}", err.toString());
        } finally {
            Utils.closeSilently(process);
        }
    }
}
