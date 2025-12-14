package uk.yermak.audiobookconverter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.yermak.audiobookconverter.book.ArtWork;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.TimeUnit;

/**
 * Created by Yermak on 04-Jan-18.
 */
public class Mp4v2ArtBuilder {
    final static Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final ConversionJob conversionJob;
    private ProgressCallback progressCallback;

    public Mp4v2ArtBuilder(ConversionJob conversionJob, ProgressCallback progressCallback) {
        this.conversionJob = conversionJob;
        this.progressCallback = progressCallback;
    }


    public void coverArt(String outputFileName) {
        List<ArtWork> posters = conversionJob.getConversionGroup().getPosters();
        if (posters.isEmpty()) return;
        progressCallback.reset();
        ResourceBundle resources = AudiobookConverter.getBundle();
        progressCallback.setState(resources.getString("progress.state.addingArtwork"));
        long duration = conversionJob.getConvertable().getDuration();
        long step = duration / posters.size();
        for (int i = 0; i < posters.size(); i++) {
            ArtWork poster = posters.get(i);
            if (conversionJob.getStatus().isOver()) break;
            updateSinglePoster(poster, i, outputFileName);
            progressCallback.converted((i+1)*step, posters.size()*step);
        }
    }

    public void updateSinglePoster(ArtWork poster, int index, String outputFileName) {
        Process process = null;
        try {
            ProcessBuilder artProcessBuilder = new ProcessBuilder(Platform.MP4ART,
                    "--art-index", String.valueOf(index),
                    "--add", poster.getFileName(),
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
            logger.debug("mp4art out: {}", out);
            logger.warn("mp4art err: {}", err);

            if (process.exitValue() != 0) {
                throw new ConversionException("ArtWork failed with code " + process.exitValue() + "!=0", new Error(err.toString()));
            }

            if (!new File(outputFileName).exists()) {
                throw new ConversionException("ArtWork failed, no output file:" + out, new Error(err.toString()));
            }
        } catch (Exception e) {
            logger.error("Failed to apply art work", e);
            throw new ConversionException("Failed to apply art work", e);
        } finally {
            Utils.closeSilently(process);
        }
    }
}
