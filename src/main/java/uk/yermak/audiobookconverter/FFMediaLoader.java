package uk.yermak.audiobookconverter;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import javafx.application.Platform;
import net.bramp.ffmpeg.FFprobe;
import net.bramp.ffmpeg.probe.FFmpegFormat;
import net.bramp.ffmpeg.probe.FFmpegProbeResult;
import net.bramp.ffmpeg.probe.FFmpegStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
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

        private static final Set<String> AUDIO_CODECS = ImmutableSet.of("mp3", "aac", "wmav2", "flac", "alac");
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
                mediaInfo.setBookInfo(bookInfo);


                if (FilenameUtils.getExtension(filename).equalsIgnoreCase("FLAC")) {
                    File file = new File(FilenameUtils.getFullPath(filename) + FilenameUtils.getBaseName(filename) + ".cue");
                    if (file.exists()) {
                        String cue = FileUtils.readFileToString(file);
                        parseCueChapters(mediaInfo, cue);
                    }
                }
                logger.info("Created AudioBookInfo {}", bookInfo);

                return mediaInfo;
            } catch (IOException e) {
                logger.error("Failed to load media info", e);
                e.printStackTrace();
                throw e;
            }
        }

    }

    static void parseCueChapters(MediaInfoBean mediaInfo, String cue) {
        AudioBookInfo bookInfo = mediaInfo.getBookInfo();
        String[] split = StringUtils.split(cue, "\n");
        for (String line : split) {
            int i = -1;
            if (bookInfo.getTracks().isEmpty()) {
                if ((i = line.indexOf("GENRE")) != -1) bookInfo.setGenre(cleanText(line.substring(i + 5)));
                if ((i = line.indexOf("TITLE")) != -1) bookInfo.setTitle(cleanText(line.substring(i + 5)));
                if ((i = line.indexOf("DATE")) != -1) bookInfo.setYear(cleanText(line.substring(i + 4)));
                if ((i = line.indexOf("PERFORMER")) != -1) bookInfo.setNarrator(cleanText(line.substring(i + 9)));
            } else {
                Track track = bookInfo.getTracks().get(bookInfo.getTracks().size() - 1);
                if ((i = line.indexOf("TITLE")) != -1) track.setTitle(cleanText(line.substring(i + 5)));
                if ((i = line.indexOf("PERFORMER")) != -1) track.setWriter(cleanText(line.substring(i + 9)));
            }
            if ((i = line.indexOf("TRACK")) != -1) {
                bookInfo.getTracks().add(new Track(cleanText(line.substring(i + 5))));
            } else {
                if ((i = line.indexOf("INDEX 01")) != -1) {
                    long time = parseCueTime(line.substring(i + 8));
                    if (bookInfo.getTracks().size() > 1) {
                        Track track = bookInfo.getTracks().get(bookInfo.getTracks().size() - 2);
                        track.setEnd(time);
                    }
                    Track track = bookInfo.getTracks().get(bookInfo.getTracks().size() - 1);
                    track.setStart(time);
                }
            }
        }
        if (!bookInfo.getTracks().isEmpty()) {
            bookInfo.getTracks().get(bookInfo.getTracks().size() - 1).setEnd(mediaInfo.getDuration());
        }
    }

    private static long parseCueTime(String substring) {
        String cleanText = cleanText(substring);
        String[] split = cleanText.split(":");
        long time = 1000 * (Integer.parseInt(split[0]) * 60 + Integer.parseInt(split[1])) + Integer.parseInt(split[2])*1000 / 75;
        return time;
    }

    private static String cleanText(String text) {
        return StringUtils.remove(StringUtils.trim(text), '"');
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
