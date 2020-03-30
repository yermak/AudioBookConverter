package uk.yermak.audiobookconverter;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import javafx.application.Platform;
import net.bramp.ffmpeg.FFprobe;
import net.bramp.ffmpeg.probe.FFmpegFormat;
import net.bramp.ffmpeg.probe.FFmpegProbeResult;
import net.bramp.ffmpeg.probe.FFmpegStream;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.yermak.audiobookconverter.fx.ConverterApplication;

import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.*;
import java.util.concurrent.*;

/**
 * Created by yermak on 1/10/2018.
 */
public class FFMediaLoader {
    final static Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private List<String> fileNames;
    private Conversion conversion;
    private static final String FFPROBE = new File("external/x64/ffprobe.exe").getAbsolutePath();
    private static final ExecutorService mediaExecutor = Executors.newSingleThreadExecutor();
    private static final ScheduledExecutorService artExecutor = Executors.newScheduledThreadPool(4);

    public FFMediaLoader(List<String> files, Conversion conversion) {
        this.fileNames = files;
        this.conversion = conversion;
        Collections.sort(fileNames);
    }

    public List<MediaInfo> loadMediaInfo() {
        logger.info("Loading media info");
        try {
            FFprobe ffprobe = new FFprobe(FFPROBE);
            List<MediaInfo> media = new ArrayList<>();
            for (String fileName : fileNames) {
                Future<MediaInfo> futureLoad = mediaExecutor.submit(new MediaInfoCallable(ffprobe, fileName, conversion));
                MediaInfo mediaInfo = new MediaInfoProxy(fileName, futureLoad);
                media.add(mediaInfo);
            }

            searchForPosters(media);

            return media;
        } catch (Exception e) {
            logger.error("Error during loading media info", e);
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private static class MediaInfoCallable implements Callable<MediaInfo> {

        private static final Set<String> AUDIO_CODECS = ImmutableSet.of("mp3", "aac", "wmav2", "flac");
        private static final ImmutableMap<String, String> ART_WORK_CODECS = ImmutableMap.of("mjpeg", "jpg", "png", "png", "bmp", "bmp");
        private final String filename;
        private Conversion conversion;
        private FFprobe ffprobe;

        public MediaInfoCallable(FFprobe ffprobe, String filename, Conversion conversion) {
            this.ffprobe = ffprobe;
            this.filename = filename;
            this.conversion = conversion;
        }

        @Override
        public MediaInfo call() throws Exception {
            try {
                if (conversion.getStatus().isOver())
                    throw new InterruptedException("Media Info Loading was interrupted");
                FFmpegProbeResult probeResult = ffprobe.probe(filename);
                logger.debug("Extracted ffprobe error: {}", probeResult.getError());
                FFmpegFormat format = probeResult.getFormat();
                logger.debug("Extracted track format: {}", format.format_name);
                MediaInfoBean mediaInfo = new MediaInfoBean(filename);

                List<FFmpegStream> streams = probeResult.getStreams();
                logger.debug("Found {} streams in {}", streams.size(), filename);

                for (FFmpegStream ffMpegStream : streams) {
                    if (AUDIO_CODECS.contains(ffMpegStream.codec_name)) {
                        logger.debug("Found {} audio stream in {}", ffMpegStream.codec_name, filename);
                        mediaInfo.setCodec(ffMpegStream.codec_name);
                        mediaInfo.setChannels(ffMpegStream.channels);
                        mediaInfo.setFrequency(ffMpegStream.sample_rate);
                        mediaInfo.setBitrate((int) ffMpegStream.bit_rate);
                        mediaInfo.setDuration(Math.round(ffMpegStream.duration * 1000));
                    } else if (ART_WORK_CODECS.containsKey(ffMpegStream.codec_name)) {
                        logger.debug("Found {} image stream in {}", ffMpegStream.codec_name, filename);
                        Future<ArtWork> futureLoad = artExecutor.schedule(new ArtWorkCallable(mediaInfo, ART_WORK_CODECS.get(ffMpegStream.codec_name), conversion), 1, TimeUnit.SECONDS);
                        ArtWorkProxy artWork = new ArtWorkProxy(futureLoad);
                        mediaInfo.setArtWork(artWork);
                    }
                }
                logger.debug("Found tags: {} in {}", format.tags, filename);

                AudioBookInfo bookInfo = new AudioBookInfo(format.tags);

                logger.info("Created AudioBookInfo {}", bookInfo);

                mediaInfo.setBookInfo(bookInfo);
                return mediaInfo;
            } catch (IOException e) {
                logger.error("Failed to load media info", e);
                e.printStackTrace();
                throw e;
            }
        }
    }

    private static class ArtWorkCallable implements Callable<ArtWork> {

        private static final String FFMPEG = new File("external/x64/ffmpeg.exe").getAbsolutePath();

        private MediaInfoBean mediaInfo;
        private String format;
        private Conversion conversion;

        public ArtWorkCallable(MediaInfoBean mediaInfo, String format, Conversion conversion) {
            this.mediaInfo = mediaInfo;
            this.format = format;
            this.conversion = conversion;
        }

        @Override
        public ArtWork call() throws Exception {
            Process process = null;
            try {
                if (conversion.getStatus().isOver()) throw new InterruptedException("ArtWork loading was interrupted");
                String poster = Utils.getTmp(mediaInfo.hashCode(), mediaInfo.hashCode(), "." + format);
                ProcessBuilder pictureProcessBuilder = new ProcessBuilder(FFMPEG,
                        "-i", mediaInfo.getFileName(),
                        poster);
                process = pictureProcessBuilder.start();

                StreamCopier.copy(process.getInputStream(), System.out);
                // not using redirectErrorStream() as sometimes error stream is not closed by process which cause feature to hang indefinitely
                StreamCopier.copy(process.getErrorStream(), System.err);
                boolean finished = false;
                while (!conversion.getStatus().isOver() && !finished) {
                    finished = process.waitFor(500, TimeUnit.MILLISECONDS);
                }
                ArtWorkBean artWorkBean = new ArtWorkBean(poster);
                Platform.runLater(() -> {
                    if (!conversion.getStatus().isOver())
                        ConverterApplication.getContext().addPosterIfMissingWithDelay(artWorkBean);
                });
                return artWorkBean;
            } finally {
                Utils.closeSilently(process);
            }
        }
    }

    static Collection<File> findPictures(File dir) {
        return FileUtils.listFiles(dir, ArtWork.IMAGE_EXTENSIONS, true);
    }

    static void searchForPosters(List<MediaInfo> media) {
        Set<File> searchDirs = new HashSet<>();
        media.forEach(mi -> searchDirs.add(new File(mi.getFileName()).getParentFile()));

        searchDirs.forEach(d -> findPictures(d).forEach(f -> ConverterApplication.getContext().addPosterIfMissingWithDelay(new ArtWorkBean(Utils.tempCopy(f.getPath())))));

    }


}
