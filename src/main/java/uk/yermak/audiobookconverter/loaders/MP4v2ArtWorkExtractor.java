package uk.yermak.audiobookconverter.loaders;

import javafx.scene.image.Image;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.yermak.audiobookconverter.*;
import uk.yermak.audiobookconverter.book.ArtWork;
import uk.yermak.audiobookconverter.book.MediaInfoBean;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.lang.invoke.MethodHandles;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

class MP4v2ArtWorkExtractor implements Callable<ArtWork> {
    final static Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final MediaInfoBean mediaInfo;
    private final String format;
    private final ConversionGroup conversionGroup;
    private int index;

    public MP4v2ArtWorkExtractor(MediaInfoBean mediaInfo, String format, ConversionGroup conversionGroup, int index) {
        this.mediaInfo = mediaInfo;
        this.format = format;
        this.conversionGroup = conversionGroup;
        this.index = index;
    }

    @Override
    public ArtWork call() throws Exception {
        Process process = null;
        File poster = null;
        try {
            if (conversionGroup.isOver() || conversionGroup.isStarted() || conversionGroup.isDetached())
                throw new InterruptedException("ArtWork loading was interrupted");
            poster = new File(new File(mediaInfo.getFileName()).getParentFile(),
                    FilenameUtils.getBaseName(mediaInfo.getFileName()) + ".art[" + index + "]." + format);
            ProcessBuilder pictureProcessBuilder = new ProcessBuilder(Platform.MP4ART,
                    "--art-index", String.valueOf(index),
                    "--extract", "-o",
                    mediaInfo.getFileName());
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
            FFMediaLoader.logger.debug("ArtWork Out: {}", out);
            FFMediaLoader.logger.error("ArtWork Error: {}", err);

            try (var imageStream = new FileInputStream(poster)) {
                ArtWork artWorkBean = new ArtWorkImage(new Image(imageStream));

                javafx.application.Platform.runLater(() -> {
                    if (!conversionGroup.isOver() && !conversionGroup.isStarted() && !conversionGroup.isDetached()) {
                        AudiobookConverter.getContext().addPosterIfMissingWithDelay(artWorkBean);
                    }
                });
                return artWorkBean;
            }
        } catch (Exception e) {
            logger.error("Error in extracting image with MP4Art:", e);
            throw new RuntimeException(e);
        } finally {
            FileUtils.deleteQuietly(poster);
            Utils.closeSilently(process);
        }
    }
}
