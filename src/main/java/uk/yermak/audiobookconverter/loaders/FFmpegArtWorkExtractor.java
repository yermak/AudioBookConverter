package uk.yermak.audiobookconverter.loaders;

import javafx.scene.image.Image;
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

class FFmpegArtWorkExtractor implements Callable<ArtWork> {
    final static Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final MediaInfoBean mediaInfo;
    private final String format;
    private final ConversionGroup conversionGroup;
    private int stream;

    public FFmpegArtWorkExtractor(MediaInfoBean mediaInfo, String format, ConversionGroup conversionGroup, int stream) {
        this.mediaInfo = mediaInfo;
        this.format = format;
        this.conversionGroup = conversionGroup;
        this.stream = stream;
    }

    @Override
    public ArtWork call() throws Exception {
        Process process = null;
        String poster = null;
        try {
            if (conversionGroup.isOver() || conversionGroup.isStarted() || conversionGroup.isDetached())
                throw new InterruptedException("ArtWork loading was interrupted");
            poster = Utils.getTmp(mediaInfo.getUID(), stream, format);
            ProcessBuilder pictureProcessBuilder = new ProcessBuilder(Platform.FFMPEG,
                    "-i", mediaInfo.getFileName(),
                    "-map", "0:" + stream,
                    "-y",
                    poster);
            process = pictureProcessBuilder.start();
            new File(poster).deleteOnExit();

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
            return artWorkBean;
        } catch (Exception e) {
            logger.error("Error in extracting image with FFMpeg:", e);
            throw new RuntimeException(e);
        } finally {
            Utils.closeSilently(process);
        }
    }
}
