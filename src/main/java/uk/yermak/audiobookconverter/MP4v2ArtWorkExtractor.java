package uk.yermak.audiobookconverter;

import javafx.scene.image.Image;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
        try {
            if (conversionGroup.isOver() || conversionGroup.isStarted() || conversionGroup.isDetached())
                throw new InterruptedException("ArtWork loading was interrupted");
            File poster = new File(new File(mediaInfo.getFileName()).getParentFile(),
                    FilenameUtils.getBaseName(mediaInfo.getFileName()) + ".art[" + index + "]." + format);
            poster.deleteOnExit();

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


            ArtWork artWorkBean = new ArtWorkImage(new Image(new FileInputStream(poster)));
            javafx.application.Platform.runLater(() -> {
                if (!conversionGroup.isOver() && !conversionGroup.isStarted() && !conversionGroup.isDetached()) {
                    AudiobookConverter.getContext().addPosterIfMissingWithDelay(artWorkBean);
                }
            });
            poster.delete();
            return artWorkBean;
        } finally {
            Utils.closeSilently(process);
        }
    }
}
