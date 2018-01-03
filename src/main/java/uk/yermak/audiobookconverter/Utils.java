package uk.yermak.audiobookconverter;

import com.freeipodsoftware.abc.Mp4Tags;
import com.freeipodsoftware.abc.Util;
import net.bramp.ffmpeg.FFprobe;
import net.bramp.ffmpeg.probe.FFmpegFormat;
import net.bramp.ffmpeg.probe.FFmpegProbeResult;
import net.bramp.ffmpeg.probe.FFmpegStream;

import java.io.File;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Created by Yermak on 29-Dec-17.
 */
public class Utils {
    static String determineTempFilename(String inputFilename, final String extension, String prefix, final String suffix, boolean uniqie, String folder) {
        File file = new File(inputFilename);
        File outFile = new File(folder, prefix + file.getName());
        String result = outFile.getAbsolutePath().replaceAll("(?i)\\." + extension, "." + suffix);
        if (!result.endsWith("." + suffix)) {
            result = result + "." + suffix;
        }
        if (uniqie) {
            return Util.makeFilenameUnique(result);
        }
        return result;
    }

    public static MediaInfo loadMediaInfo(String filename) {
        try {

            Future futureLoad = Executors.newWorkStealingPool().submit(new MediaInfoCallable(filename));
            MediaInfo mediaInfo = new MediaInfoProxy(filename, futureLoad);

            return mediaInfo;
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
        }
    }

    private static class MediaInfoCallable implements Callable<MediaInfo> {

        private final String filename;

        public MediaInfoCallable(String filename) {
            this.filename = filename;
        }

        @Override
        public MediaInfo call() throws Exception {
            MediaInfoBean mediaInfo = new MediaInfoBean(filename);
            FFprobe ffprobe = new FFprobe("external/x64/ffprobe.exe");
            FFmpegProbeResult probeResult = ffprobe.probe(filename);
            FFmpegFormat format = probeResult.getFormat();

            List<FFmpegStream> streams = probeResult.getStreams();
            for (int i = 0; i < streams.size(); i++) {
                FFmpegStream fFmpegStream = streams.get(i);
                if ("mp3".equals(fFmpegStream.codec_name)) {
                    mediaInfo.setChannels(fFmpegStream.channels);
                    mediaInfo.setFrequency(fFmpegStream.sample_rate);
                    mediaInfo.setBitrate((int) fFmpegStream.bit_rate);
                    mediaInfo.setDuration((long) fFmpegStream.duration * 1000);
                    break;
                }
            }
            Mp4Tags mp4Tags = new Mp4Tags(format.tags);
            mediaInfo.setMp4Tags(mp4Tags);
            return mediaInfo;
        }
    }
}
