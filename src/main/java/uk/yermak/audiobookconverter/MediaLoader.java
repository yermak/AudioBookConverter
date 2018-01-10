package uk.yermak.audiobookconverter;

import com.freeipodsoftware.abc.Mp4Tags;
import net.bramp.ffmpeg.FFprobe;
import net.bramp.ffmpeg.probe.FFmpegFormat;
import net.bramp.ffmpeg.probe.FFmpegProbeResult;
import net.bramp.ffmpeg.probe.FFmpegStream;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * Created by yermak on 1/10/2018.
 */
public class MediaLoader {

    private List<String> fileNames;
    private ExecutorService executorService = Executors.newWorkStealingPool();


    public MediaLoader(List<String> files) {
        this.fileNames = files;
    }

    public List<MediaInfo> loadMediaInfo() {
        try {
            String path = new File("external/x64/ffprobe.exe").getAbsolutePath();
            FFprobe ffprobe = new FFprobe(path);
            List<MediaInfo> media = new ArrayList<>();
            for (String fileName : fileNames) {
                Future futureLoad = executorService.submit(new MediaInfoCallable(ffprobe, fileName));
                MediaInfo mediaInfo = new MediaInfoProxy(fileName, futureLoad);
                media.add(mediaInfo);
            }
            return media;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static class MediaInfoCallable implements Callable<MediaInfo> {

        private final String filename;
        private FFprobe ffprobe;
        private final static Semaphore mutex = new Semaphore(Runtime.getRuntime().availableProcessors() * 2);

        public MediaInfoCallable(FFprobe ffprobe, String filename) {
            this.ffprobe = ffprobe;
            this.filename = filename;
        }

        @Override
        public MediaInfo call() throws Exception {
            try {
                mutex.acquire();
                FFmpegProbeResult probeResult = ffprobe.probe(filename);
                FFmpegFormat format = probeResult.getFormat();
                MediaInfoBean mediaInfo = new MediaInfoBean(filename);

                List<FFmpegStream> streams = probeResult.getStreams();
                for (int i = 0; i < streams.size(); i++) {
                    FFmpegStream fFmpegStream = streams.get(i);
                    if ("mp3".equals(fFmpegStream.codec_name)) {
                        mediaInfo.setChannels(fFmpegStream.channels);
                        mediaInfo.setFrequency(fFmpegStream.sample_rate);
                        mediaInfo.setBitrate((int) fFmpegStream.bit_rate);
                        mediaInfo.setDuration((long) fFmpegStream.duration * 1000);
                    } else if ("mjpeg".equals(fFmpegStream.codec_name)) {
                        mediaInfo.setPictureFormat("jpg");
                    }
                }
                Mp4Tags mp4Tags = new Mp4Tags(format.tags);
                mediaInfo.setMp4Tags(mp4Tags);
                return mediaInfo;
            } catch (IOException e) {
                throw e;
            } finally {
                mutex.release();
            }
        }
    }
}
