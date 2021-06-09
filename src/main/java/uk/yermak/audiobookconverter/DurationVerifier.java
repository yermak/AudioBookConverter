package uk.yermak.audiobookconverter;

import com.google.common.collect.ImmutableSet;
import net.bramp.ffmpeg.FFprobe;
import net.bramp.ffmpeg.probe.FFmpegProbeResult;
import net.bramp.ffmpeg.probe.FFmpegStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Set;

/**
 * Created by Yermak on 04-Jan-18.
 */
public class DurationVerifier {
    final static Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());


    static long parseDuration(String info) {
        String[] lines = StringUtils.split(info, "\n");
        for (String line : lines) {
            if (StringUtils.isNotEmpty(line)) {
                String[] columns = StringUtils.split(line, ",");
                if (StringUtils.contains(columns[0], "audio")) {
                    for (int j = 1, columnsLength = columns.length; j < columnsLength; j++) {
                        String column = columns[j];
                        int k;
                        if ((k = column.indexOf(" sec")) != -1) {
                            String substring = column.substring(1, k);
                            return (long) (Double.parseDouble(substring) * 1000);
                        }
                    }
                }
            }
        }
        return 0;
    }

    public static void mp4v2UpdateDuration(MediaInfo mediaInfo, String outputFileName) throws IOException {
        Process process = null;
        try {
            ProcessBuilder infoProcessBuilder = new ProcessBuilder(Utils.MP4INFO, outputFileName);
            process = infoProcessBuilder.start();
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            StreamCopier.copy(process.getInputStream(), out);
            StreamCopier.copy(process.getErrorStream(), System.err);
            process.waitFor();
            String info = out.toString(Charset.defaultCharset());
            long duration = parseDuration(info);
            if (duration != 0) {
                mediaInfo.setDuration(duration);
            } else {
                logger.warn("Failed to detect actual duration for '" + mediaInfo.getFileName() + "' in file: '" + outputFileName + "', extracted info: " + info);
            }
        } catch (InterruptedException e) {
            logger.error(e.getMessage(), e);
        } finally {
            Utils.closeSilently(process);
        }
    }

    public static void ffMpegUpdateDuration(MediaInfo mediaInfo, String outputFileName) throws IOException {
        final Set<String> AUDIO_CODECS = ImmutableSet.of("mp3", "aac", "wmav2", "flac", "alac", "vorbis", "opus");
        FFprobe ffprobe = new FFprobe(Utils.FFPROBE);
        FFmpegProbeResult probe = ffprobe.probe(outputFileName);
        List<FFmpegStream> streams = probe.getStreams();
        for (FFmpegStream stream : streams) {
            if (AUDIO_CODECS.contains(stream.codec_name)) {
                mediaInfo.setDuration(Math.round(stream.duration * 1000));
            }
        }
    }

}
