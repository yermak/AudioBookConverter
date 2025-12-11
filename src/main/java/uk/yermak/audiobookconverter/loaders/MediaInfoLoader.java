package uk.yermak.audiobookconverter.loaders;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import net.bramp.ffmpeg.FFprobe;
import net.bramp.ffmpeg.probe.FFmpegChapter;
import net.bramp.ffmpeg.probe.FFmpegFormat;
import net.bramp.ffmpeg.probe.FFmpegProbeResult;
import net.bramp.ffmpeg.probe.FFmpegStream;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.yermak.audiobookconverter.ConversionGroup;
import uk.yermak.audiobookconverter.book.*;

import java.text.MessageFormat;
import java.lang.invoke.MethodHandles;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;
import java.util.ResourceBundle;

class MediaInfoLoader implements Callable<MediaInfo> {

    final static Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());


    private static final Set<String> AUDIO_CODECS = ImmutableSet.of("mp3", "aac", "wmav2", "flac", "alac", "vorbis", "opus");
    private static final ImmutableMap<String, String> ART_WORK_CODECS = ImmutableMap.of("mjpeg", "jpg", "png", "png", "bmp", "bmp");
    private static final Set<String> MP4_FILES = ImmutableSet.of("m4b", "m4a", "mp4");

    private final String filename;
    private final ConversionGroup conversionGroup;
    private final FFprobe ffprobe;
    private final ResourceBundle resources;

    private static final ScheduledExecutorService artExecutor = Executors.newScheduledThreadPool(8);


    public MediaInfoLoader(FFprobe ffprobe, String filename, ConversionGroup conversionGroup, ResourceBundle resources) {
        this.ffprobe = ffprobe;
        this.filename = filename;
        this.conversionGroup = conversionGroup;
        this.resources = resources;
    }

    @Override
    public MediaInfo call() throws Exception {
        try {
            if (conversionGroup.isOver() || conversionGroup.isRunning())
                throw new InterruptedException("Media Info Loading was interrupted");
            FFmpegProbeResult probeResult = ffprobe.probe(filename);
            FFMediaLoader.logger.debug("Extracted ffprobe error: {}", probeResult.getError());
            FFmpegFormat format = probeResult.getFormat();
            FFMediaLoader.logger.debug("Extracted track format: {}", format.format_name);
            MediaInfoBean mediaInfo = new MediaInfoBean(filename);

            List<FFmpegStream> streams = probeResult.getStreams();
            FFMediaLoader.logger.debug("Found {} streams in {}", streams.size(), filename);

            Map<String, String> streamTags = null;

            for (int i = 0; i < streams.size(); i++) {
                FFmpegStream ffMpegStream = streams.get(i);
                if (AUDIO_CODECS.contains(ffMpegStream.codec_name) || ffMpegStream.codec_name.startsWith("pcm")) {
                    FFMediaLoader.logger.debug("Found {} audio stream in {}", ffMpegStream.codec_name, filename);
                    mediaInfo.setCodec(ffMpegStream.codec_name);
                    mediaInfo.setChannels(ffMpegStream.channels);
                    mediaInfo.setFrequency(ffMpegStream.sample_rate);
                    mediaInfo.setBitrate((int) ffMpegStream.bit_rate);
                    mediaInfo.setDuration(Math.round(ffMpegStream.duration * 1000));
                    streamTags = ffMpegStream.tags;
                } else if (ART_WORK_CODECS.containsKey(ffMpegStream.codec_name) && !MP4_FILES.contains(FilenameUtils.getExtension(filename).toLowerCase())) {
                    FFMediaLoader.logger.debug("Found {} image stream in {}", ffMpegStream.codec_name, filename);
                    if (!conversionGroup.isDetached()) {
                        Future<ArtWork> futureLoad = artExecutor.schedule(new FFmpegArtWorkExtractor(mediaInfo, ART_WORK_CODECS.get(ffMpegStream.codec_name), conversionGroup, i), 100, TimeUnit.MILLISECONDS);
                        ArtWorkProxy artWork = new ArtWorkProxy(futureLoad);
                        mediaInfo.addArtWork(artWork);
                    }
                } else if (ffMpegStream.codec_name.equals("bin_data") && MP4_FILES.contains(FilenameUtils.getExtension(filename).toLowerCase())) {
                    if (!conversionGroup.isDetached()) {
                        List<String> imageFormats = new MP4v2ArtWorkChecker(conversionGroup, mediaInfo.getFileName()).list();
                        for (int j = 0; j < imageFormats.size(); j++) {
                            String imageType = imageFormats.get(j);
                            Future<ArtWork> futureLoad = artExecutor.schedule(new MP4v2ArtWorkExtractor(mediaInfo, imageType, conversionGroup, j), 1 * j, TimeUnit.MILLISECONDS);
                            ArtWorkProxy artWork = new ArtWorkProxy(futureLoad);
                            mediaInfo.addArtWork(artWork);
                        }
                    }
                }
            }

            FFMediaLoader.logger.debug("Found tags: {} in {}", format.tags, filename);
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
                FlacLoader.parseCueChapters(mediaInfo);
            }

            logger.info("Created AudioBookInfo {}", bookInfo);

            return mediaInfo;
        } catch (Exception e) {
            FFMediaLoader.logger.error("Failed to load media info", e);
            e.printStackTrace();
            throw e;
        }
    }

    private void processEmbededChapter(MediaInfoBean mediaInfo, List<FFmpegChapter> chapters) {
        AudioBookInfo bookInfo = mediaInfo.getBookInfo();
        for (int i = 0; i < chapters.size(); i++) {
            FFmpegChapter chapter = chapters.get(i);
            String trackNo = StringUtils.leftPad(String.valueOf(i + 1), 3, "00");
            Track track = new Track(trackNo);
            if (chapter.tags != null) {
                track.setTitle(chapter.tags.title);
            } else {
                track.setTitle(MessageFormat.format(resources.getString("chapter.default_title"), trackNo));
            }
            track.setStart((long) (Double.parseDouble(chapter.start_time) * 1000));
            track.setEnd((long) (Double.parseDouble(chapter.end_time) * 1000));
            bookInfo.tracks().add(track);
        }
    }
}
