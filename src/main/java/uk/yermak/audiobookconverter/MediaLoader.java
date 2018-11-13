package uk.yermak.audiobookconverter;

import javafx.collections.ObservableMap;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import net.bramp.ffmpeg.FFprobe;
import net.bramp.ffmpeg.probe.FFmpegFormat;
import net.bramp.ffmpeg.probe.FFmpegProbeResult;
import net.bramp.ffmpeg.probe.FFmpegStream;
import uk.yermak.audiobookconverter.fx.ConverterApplication;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;

/**
 * Created by yermak on 1/10/2018.
 */
public class MediaLoader {

    private final StatusChangeListener listener;
    private List<String> fileNames;
    private static final String FFPROBE = new File("external/x64/ffprobe.exe").getAbsolutePath();
    private static final ExecutorService mediaExecutor = Executors.newSingleThreadExecutor();
    private static final ScheduledExecutorService artExecutor = Executors.newScheduledThreadPool(4);

    public MediaLoader(List<String> files) {
        this.fileNames = files;
        Collections.sort(fileNames);

        //TODO add latch to remove listener at the end.
        listener = new StatusChangeListener();
        ConverterApplication.getContext().getConversion().addStatusChangeListener(listener);
    }

    public List<MediaInfo> loadMediaInfo() {
        try {
            FFprobe ffprobe = new FFprobe(FFPROBE);
            List<MediaInfo> media = new ArrayList<>();
            for (String fileName : fileNames) {
                Future futureLoad = mediaExecutor.submit(new MediaInfoCallable(ffprobe, fileName));
                MediaInfo mediaInfo = new MediaInfoProxy(fileName, futureLoad);
                media.add(mediaInfo);
            }
            return media;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private static class MediaInfoCallable implements Callable<MediaInfo> {

        private final String filename;
        private FFprobe ffprobe;

        public MediaInfoCallable(FFprobe ffprobe, String filename) {
            this.ffprobe = ffprobe;
            this.filename = filename;
        }

        @Override
        public MediaInfo call() throws Exception {
            try {

                Media m = new Media(new File(filename).toURI().toASCIIString());
                MediaPlayer mediaPlayer = new MediaPlayer(m);
                mediaPlayer.setOnReady(new Runnable() {
                    @Override
                    public void run() {
                        ObservableMap<String, Object> metadata = m.getMetadata();
                        System.out.println("metadata = " + metadata);
                    }
                });


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
                        Future futureLoad = artExecutor.schedule(new ArtWorkCallable(mediaInfo, "jpg"), 1, TimeUnit.MINUTES);
//                        futureLoad.get();
                        ArtWorkProxy artWork = new ArtWorkProxy(futureLoad, "jpg");
                        mediaInfo.setArtWork(artWork);
                    }
                }
                AudioBookInfo bookInfo = new AudioBookInfo(format.tags);
                mediaInfo.setBookInfo(bookInfo);
                return mediaInfo;
            } catch (IOException e) {
                e.printStackTrace();
                throw e;
            }
        }
    }

    private static class ArtWorkCallable implements Callable<ArtWork> {

        private static final String FFMPEG = new File("external/x64/ffmpeg.exe").getAbsolutePath();
        private final StatusChangeListener listener;


        private MediaInfoBean mediaInfo;
        private String format;

        public ArtWorkCallable(MediaInfoBean mediaInfo, String format) {
            this.mediaInfo = mediaInfo;
            this.format = format;
            listener = new StatusChangeListener();
            ConverterApplication.getContext().getConversion().addStatusChangeListener(listener);
        }

        @Override
        public ArtWork call() throws Exception {
            Process process = null;
            try {
                if (listener.isCancelled()) throw new InterruptedException("ArtWork loading was cancelled");
                String poster = Utils.getTmp(mediaInfo.hashCode(), mediaInfo.hashCode(), "." + format);
                ProcessBuilder pictureProcessBuilder = new ProcessBuilder(FFMPEG,
                        "-i", mediaInfo.getFileName(),
                        poster);
                process = pictureProcessBuilder.start();

                StreamCopier.copy(process.getInputStream(), System.out);
                // not using redirectErrorStream() as sometimes error stream is not closed by process which cause feature to hang indefinitely
                StreamCopier.copy(process.getErrorStream(), System.err);
                boolean finished = false;
                while (!listener.isCancelled() && !finished) {
                    finished = process.waitFor(500, TimeUnit.MILLISECONDS);
                }
                File posterFile = new File(poster);
                long crc32 = Utils.checksumCRC32(posterFile);
                return new ArtWorkBean(poster, format, crc32);
            } finally {
                Utils.closeSilently(process);
                ConverterApplication.getContext().getConversion().removeStatusChangeListener(listener);
            }
        }

    }

}
