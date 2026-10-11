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
import java.io.FileNotFoundException;
import java.lang.invoke.MethodHandles;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

class MP4v2ArtWorkExtractor implements Callable<ArtWork> {
    final static Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final MediaInfoBean mediaInfo;
    private final ConversionGroup conversionGroup;
    private int index;

    public MP4v2ArtWorkExtractor(MediaInfoBean mediaInfo, ConversionGroup conversionGroup, int index) {
        this.mediaInfo = mediaInfo;
        this.conversionGroup = conversionGroup;
        this.index = index;
    }

    /**
     * The file mp4art --extract wrote for this cover: <base>.art[index].<ext>. The extension is the image type mp4art
     * detects from the data, which for a cover --list reports as "implicit" is jpg or png, not the listed type.
     */
    static File extractedFile(String audioFile, int index) {
        File dir = new File(audioFile).getAbsoluteFile().getParentFile();
        String prefix = FilenameUtils.getBaseName(audioFile) + ".art[" + index + "].";
        File[] found = dir.listFiles((d, name) -> name.startsWith(prefix));
        return found == null || found.length == 0 ? null : found[0];
    }

    @Override
    public ArtWork call() throws Exception {
        Process process = null;
        File poster = null;
        try {
            if (conversionGroup.isOver() || conversionGroup.isStarted() || conversionGroup.isDetached())
                throw new InterruptedException("ArtWork loading was interrupted");
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

            poster = extractedFile(mediaInfo.getFileName(), index);
            if (poster == null) throw new FileNotFoundException("mp4art extracted no file for art index " + index + " of " + mediaInfo.getFileName());
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
