package uk.yermak.audiobookconverter;

import com.google.common.collect.ImmutableSet;
import net.bramp.ffmpeg.FFprobe;
import net.bramp.ffmpeg.probe.FFmpegProbeResult;
import net.bramp.ffmpeg.probe.FFmpegStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.yermak.audiobookconverter.book.MediaInfo;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.Set;

/**
 * Created by Yermak on 04-Jan-18.
 */
public class DurationVerifier {
    final static Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private static final Set<String> AUDIO_CODECS = ImmutableSet.of("mp3", "aac", "wmav2", "flac", "alac", "vorbis", "opus");

    // Singleton FFprobe instance - reused across all calls to avoid creating new process wrappers
    private static volatile FFprobe ffprobeInstance;

    /**
     * Get or create the singleton FFprobe instance.
     * Uses double-checked locking for thread-safe lazy initialization.
     */
    private static FFprobe getFFprobeInstance() throws IOException {
        if (ffprobeInstance == null) {
            synchronized (DurationVerifier.class) {
                if (ffprobeInstance == null) {
                    ffprobeInstance = new FFprobe(Platform.FFPROBE);
                    logger.debug("Created FFprobe instance at: {}", Platform.FFPROBE);
                }
            }
        }
        return ffprobeInstance;
    }

    public static void ffMpegUpdateDuration(MediaInfo mediaInfo, String outputFileName) throws IOException {
        FFprobe ffprobe = getFFprobeInstance();

        // Synchronize probe() call because FFprobe is NOT thread-safe:
        // - FFcommon has unsynchronized mutable processOutputStream/processErrorStream
        // - RunProcessFunction has unsynchronized mutable workingDirectory field
        // This is called from parallel conversion jobs (WorkStealingPool), so synchronization required
        FFmpegProbeResult probe;
        synchronized (ffprobe) {
            probe = ffprobe.probe(outputFileName);
        }

        List<FFmpegStream> streams = probe.getStreams();
        for (FFmpegStream stream : streams) {
            if (AUDIO_CODECS.contains(stream.codec_name)) {
                mediaInfo.setDuration(Math.round(stream.duration * 1000));
            }
        }
    }

}
