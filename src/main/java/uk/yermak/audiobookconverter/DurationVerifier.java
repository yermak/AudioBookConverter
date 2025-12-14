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

    public static void ffMpegUpdateDuration(MediaInfo mediaInfo, String outputFileName) throws IOException {
        final Set<String> AUDIO_CODECS = ImmutableSet.of("mp3", "aac", "wmav2", "flac", "alac", "vorbis", "opus");
        FFprobe ffprobe = new FFprobe(Platform.FFPROBE);
        FFmpegProbeResult probe = ffprobe.probe(outputFileName);
        List<FFmpegStream> streams = probe.getStreams();
        for (FFmpegStream stream : streams) {
            if (AUDIO_CODECS.contains(stream.codec_name)) {
                mediaInfo.setDuration(Math.round(stream.duration * 1000));
            }
        }
    }

}
