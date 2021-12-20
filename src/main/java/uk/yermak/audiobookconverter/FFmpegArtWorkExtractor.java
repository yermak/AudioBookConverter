package uk.yermak.audiobookconverter;

import javafx.application.Platform;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

class FFmpegArtWorkExtractor implements Callable<ArtWork> {

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
        try {
            if (conversionGroup.isOver() || conversionGroup.isStarted() || conversionGroup.isDetached())
                throw new InterruptedException("ArtWork loading was interrupted");
            String poster = Utils.getTmp(mediaInfo.hashCode(), stream, format);
            ProcessBuilder pictureProcessBuilder = new ProcessBuilder(Environment.FFMPEG,
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

            ArtWorkBean artWorkBean = new ArtWorkBean(poster);
            Platform.runLater(() -> {
                if (!conversionGroup.isOver() && !conversionGroup.isStarted() && !conversionGroup.isDetached()) {
                    AudiobookConverter.getContext().addPosterIfMissingWithDelay(artWorkBean);
                }
            });
            return artWorkBean;
        } finally {
            Utils.closeSilently(process);
        }
    }
}
