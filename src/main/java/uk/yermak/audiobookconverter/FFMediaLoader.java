package uk.yermak.audiobookconverter;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import javafx.application.Platform;
import net.bramp.ffmpeg.FFprobe;
import net.bramp.ffmpeg.probe.FFmpegChapter;
import net.bramp.ffmpeg.probe.FFmpegFormat;
import net.bramp.ffmpeg.probe.FFmpegProbeResult;
import net.bramp.ffmpeg.probe.FFmpegStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.yermak.audiobookconverter.fx.ConversionContext;
import uk.yermak.audiobookconverter.fx.ConverterApplication;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.*;

/**
 * Background media info loader. Media files are processed in the same order as passed to the constructor.
 *
 * @author yermak
 */
public class FFMediaLoader {
    final static Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final List<String> fileNames;
    private final ConversionGroup conversionGroup;
    private static final ExecutorService mediaExecutor = Executors.newSingleThreadExecutor();
    private static final ScheduledExecutorService artExecutor = Executors.newScheduledThreadPool(8);
    private boolean detached;

    public FFMediaLoader(List<String> files, ConversionGroup conversionGroup) {
        this.fileNames = files;
        this.conversionGroup = conversionGroup;
    }

    public List<MediaInfo> loadMediaInfo() {
        logger.info("Loading media info");
        try {
            FFprobe ffprobe = new FFprobe(Utils.FFPROBE);
            List<MediaInfo> media = new ArrayList<>();
            for (String fileName : fileNames) {
                Future<MediaInfo> futureLoad = mediaExecutor.submit(new MediaInfoCallable(ffprobe, fileName, conversionGroup));
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

    public void detach() {

    }

    private static class MediaInfoCallable implements Callable<MediaInfo> {

        private static final Set<String> AUDIO_CODECS = ImmutableSet.of("mp3", "aac", "wmav2", "flac", "alac", "vorbis", "opus");
        private static final ImmutableMap<String, String> ART_WORK_CODECS = ImmutableMap.of("mjpeg", "jpg", "png", "png", "bmp", "bmp");
        private final String filename;
        private final ConversionGroup conversionGroup;
        private final FFprobe ffprobe;

        public MediaInfoCallable(FFprobe ffprobe, String filename, ConversionGroup conversionGroup) {
            this.ffprobe = ffprobe;
            this.filename = filename;
            this.conversionGroup = conversionGroup;
        }

        @Override
        public MediaInfo call() throws Exception {
            try {
                if (conversionGroup.isOver() || conversionGroup.isRunning())
                    throw new InterruptedException("Media Info Loading was interrupted");
                FFmpegProbeResult probeResult = ffprobe.probe(filename);
                logger.debug("Extracted ffprobe error: {}", probeResult.getError());
                FFmpegFormat format = probeResult.getFormat();
                logger.debug("Extracted track format: {}", format.format_name);
                MediaInfoBean mediaInfo = new MediaInfoBean(filename);

                List<FFmpegStream> streams = probeResult.getStreams();
                logger.debug("Found {} streams in {}", streams.size(), filename);

                Map<String, String> streamTags = null;

                for (FFmpegStream ffMpegStream : streams) {
                    if (AUDIO_CODECS.contains(ffMpegStream.codec_name)) {
                        logger.debug("Found {} audio stream in {}", ffMpegStream.codec_name, filename);
                        mediaInfo.setCodec(ffMpegStream.codec_name);
                        mediaInfo.setChannels(ffMpegStream.channels);
                        mediaInfo.setFrequency(ffMpegStream.sample_rate);
                        mediaInfo.setBitrate((int) ffMpegStream.bit_rate);
                        mediaInfo.setDuration(Math.round(ffMpegStream.duration * 1000));
                        streamTags = ffMpegStream.tags;
                    } else if (ART_WORK_CODECS.containsKey(ffMpegStream.codec_name)) {
                        logger.debug("Found {} image stream in {}", ffMpegStream.codec_name, filename);
                        if (!conversionGroup.isDetached()) {
                            Future<ArtWork> futureLoad = artExecutor.schedule(new ArtWorkCallable(mediaInfo, ART_WORK_CODECS.get(ffMpegStream.codec_name), conversionGroup), 1, TimeUnit.SECONDS);
                            ArtWorkProxy artWork = new ArtWorkProxy(futureLoad);
                            mediaInfo.setArtWork(artWork);
                        }
                    }
                }

                logger.debug("Found tags: {} in {}", format.tags, filename);
                HashMap<String, String> tags = new HashMap<>();
                if (format.tags != null) {
                    tags.putAll(format.tags);
                }
                if (streamTags != null) {
                    tags.putAll(streamTags);
                }
                AudioBookInfo bookInfo = AudioBookInfo.instance(tags);
                mediaInfo.setBookInfo(bookInfo);

                processEmbededChapter(mediaInfo, probeResult.getChapters());

                if (FilenameUtils.getExtension(filename).equalsIgnoreCase("FLAC")) {
                    parseCueChapters(mediaInfo);
                }

                logger.info("Created AudioBookInfo {}", bookInfo);

                return mediaInfo;
            } catch (IOException e) {
                logger.error("Failed to load media info", e);
                e.printStackTrace();
                throw e;
            }
        }

        private void processEmbededChapter(MediaInfoBean mediaInfo, List<FFmpegChapter> chapters) {
            AudioBookInfo bookInfo = mediaInfo.getBookInfo();
            for (int i = 0; i < chapters.size(); i++) {
                FFmpegChapter chapter = chapters.get(i);
                Track track = new Track(StringUtils.leftPad(String.valueOf(i + 1), 3, "00"));
                track.setTitle(chapter.tags.title);
                track.setStart((long) (Double.parseDouble(chapter.start_time) * 1000));
                track.setEnd((long) (Double.parseDouble(chapter.end_time) * 1000));
                bookInfo.tracks().add(track);
            }
        }
    }

    static void parseCueChapters(MediaInfoBean mediaInfo) throws IOException {
        String filename = mediaInfo.getFileName();
        File file = new File(FilenameUtils.getFullPath(filename) + FilenameUtils.getBaseName(filename) + ".cue");
        if (file.exists()) {
            String cue = FileUtils.readFileToString(file, StandardCharsets.UTF_8);

            parseCue(mediaInfo, cue);
        }
    }

    static void parseCue(MediaInfoBean mediaInfo, String cue) {
        AudioBookInfo bookInfo = mediaInfo.getBookInfo();
        String[] split = StringUtils.split(cue, "\n");
        for (String line : split) {
            int i = -1;
            if (bookInfo.tracks().isEmpty()) {
                if ((i = line.indexOf("GENRE")) != -1) bookInfo.genre().set(cleanText(line.substring(i + 5)));
                if ((i = line.indexOf("TITLE")) != -1) bookInfo.title().set(cleanText(line.substring(i + 5)));
                if ((i = line.indexOf("DATE")) != -1) bookInfo.year().set(cleanText(line.substring(i + 4)));
                if ((i = line.indexOf("PERFORMER")) != -1) bookInfo.narrator().set(cleanText(line.substring(i + 9)));
            } else {
                Track track = bookInfo.tracks().get(bookInfo.tracks().size() - 1);
                if ((i = line.indexOf("TITLE")) != -1) track.setTitle(cleanText(line.substring(i + 5)));
                if ((i = line.indexOf("PERFORMER")) != -1) track.setWriter(cleanText(line.substring(i + 9)));
            }
            if ((i = line.indexOf("TRACK")) != -1) {
                bookInfo.tracks().add(new Track(cleanText(line.substring(i + 5))));
            } else {
                if ((i = line.indexOf("INDEX 01")) != -1) {
                    long time = parseCueTime(line.substring(i + 8));
                    if (bookInfo.tracks().size() > 1) {
                        Track track = bookInfo.tracks().get(bookInfo.tracks().size() - 2);
                        track.setEnd(time);
                    }
                    Track track = bookInfo.tracks().get(bookInfo.tracks().size() - 1);
                    track.setStart(time);
                }
            }
        }
        if (!bookInfo.tracks().isEmpty()) {
            bookInfo.tracks().get(bookInfo.tracks().size() - 1).setEnd(mediaInfo.getDuration());
        }
    }

    private static long parseCueTime(String substring) {
        String cleanText = cleanText(substring);
        String[] split = cleanText.split(":");
        long time = 1000 * (Integer.parseInt(split[0]) * 60 + Integer.parseInt(split[1])) + Integer.parseInt(split[2]) * 1000 / 75;
        return time;
    }

    private static String cleanText(String text) {
        return StringUtils.remove(StringUtils.trim(text), '"');
    }


    private static class ArtWorkCallable implements Callable<ArtWork> {

        private final MediaInfoBean mediaInfo;
        private final String format;
        private final ConversionGroup conversionGroup;

        public ArtWorkCallable(MediaInfoBean mediaInfo, String format, ConversionGroup conversionGroup) {
            this.mediaInfo = mediaInfo;
            this.format = format;
            this.conversionGroup = conversionGroup;
        }

        @Override
        public ArtWork call() throws Exception {
            Process process = null;
            try {
                if (conversionGroup.isOver() || conversionGroup.isStarted() || conversionGroup.isDetached())
                    throw new InterruptedException("ArtWork loading was interrupted");
                String poster = Utils.getTmp(mediaInfo.hashCode(), mediaInfo.hashCode(), format);
                ProcessBuilder pictureProcessBuilder = new ProcessBuilder(Utils.FFMPEG,
                        "-i", mediaInfo.getFileName(),
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
                logger.debug("ArtWork Out: {}", out.toString());
                logger.error("ArtWork Error: {}", err.toString());

                ArtWorkBean artWorkBean = new ArtWorkBean(poster);
                Platform.runLater(() -> {
                    if (!conversionGroup.isOver() && !conversionGroup.isStarted() && !conversionGroup.isDetached()) {
                        ConverterApplication.getContext().addPosterIfMissingWithDelay(artWorkBean);
                    }
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

        List<File> pictures = new ArrayList<>();

        ConversionContext context = ConverterApplication.getContext();
        for (File d : searchDirs) {
            pictures.addAll(findPictures(d));
        }

        //adding artificial limit of image count to address issue #153.
        if (!pictures.isEmpty()) {
            for (int i = 0; i < 10 && i < pictures.size(); i++) {
                context.addPosterIfMissingWithDelay(new ArtWorkBean(Utils.tempCopy(pictures.get(i).getPath())));
            }
        }
    }


}
