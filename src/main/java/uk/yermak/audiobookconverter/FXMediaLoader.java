package uk.yermak.audiobookconverter;

import javafx.application.Platform;
import javafx.collections.ObservableMap;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.Track;
import uk.yermak.audiobookconverter.fx.ConverterApplication;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

/**
 * Created by yermak on 1/10/2018.
 */
public class FXMediaLoader implements MediaLoader {

    private final StatusChangeListener listener;
    private List<String> fileNames;
    private static final ExecutorService mediaExecutor = Executors.newSingleThreadExecutor();
    private static final ScheduledExecutorService artExecutor = Executors.newScheduledThreadPool(4);

    public FXMediaLoader(List<String> files) {
        this.fileNames = files;
        Collections.sort(fileNames);

        listener = new StatusChangeListener();
        ConverterApplication.getContext().getConversion().addStatusChangeListener(listener);
    }

    @Override
    public List<MediaInfo> loadMediaInfo() {
        List<MediaInfo> media = new ArrayList<>();
        for (String fileName : fileNames) {

            CompletableFuture completableFuture = new CompletableFuture();

            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    Media m = new Media(new File(fileName).toURI().toASCIIString());
                    MediaPlayer mediaPlayer = new MediaPlayer(m);

                    mediaPlayer.setOnReady(new Runnable() {
                        @Override
                        public void run() {
                            ObservableMap<String, Object> metadata = m.getMetadata();

                            MediaInfoBean mediaInfo = new MediaInfoBean(fileName);

                            AudioBookInfo bookInfo = createAudioBookInfo(metadata);
                            System.out.println("bookInfo = " + bookInfo.getTitle());
                            mediaInfo.setBookInfo(bookInfo);
                            if (!m.getTracks().isEmpty()) {
                                Track track = m.getTracks().get(0);
                                track.getMetadata();
                                mediaInfo.setDuration((long) m.getDuration().toMillis());
                            }
                            completableFuture.complete(mediaInfo);

                        }
                    });
                }
            });

//            Future futureLoad = mediaExecutor.submit(new MediaInfoCallable(fileName));
            MediaInfo mediaInfo = new MediaInfoProxy(fileName, completableFuture);
            media.add(mediaInfo);
        }
        return media;
    }

    static class MediaInfoCallable implements Callable<MediaInfo> {
        private String fileName;

        public MediaInfoCallable(String fileName) {
            this.fileName = fileName;
        }

        @Override
        public MediaInfo call() throws Exception {


            boolean ready = false;
            while (!ready) {
                Media m = new Media(new File(fileName).toURI().toASCIIString());
                MediaPlayer mediaPlayer = new MediaPlayer(m);

                ready = mediaPlayer.getStatus() == MediaPlayer.Status.READY;
                if (ready) {

                } else {
                    Thread.sleep(100);
                }
            }

            return null;
//
//            mediaInfo.setChannels(fFmpegStream.channels);
//            mediaInfo.setFrequency(fFmpegStream.sample_rate);
//            mediaInfo.setBitrate((int) fFmpegStream.bit_rate);
//            mediaInfo.setDuration((long) fFmpegStream.duration * 1000);
////                 Future futureLoad = artExecutor.schedule(new FFMediaLoader.ArtWorkCallable(mediaInfo, "jpg"), 1, TimeUnit.MINUTES);
////                        futureLoad.get();
//            ArtWorkProxy artWork = new ArtWorkProxy(futureLoad, "jpg");
//            mediaInfo.setArtWork(artWork);


        }


    }

    public AudioBookInfo createAudioBookInfo(Map tags) {
        AudioBookInfo audioBookInfo = new AudioBookInfo();
        if (tags != null) {
            audioBookInfo.setTitle((String) tags.get("title"));
            audioBookInfo.setWriter((String) tags.get("artist"));
            audioBookInfo.setNarrator((String) tags.get("album artist"));
            audioBookInfo.setSeries((String) tags.get("album"));
            audioBookInfo.setYear((String.valueOf(tags.get("year"))));
            audioBookInfo.setComment((String) tags.get("comment-0"));
            audioBookInfo.setGenre((String) tags.get("genre"));
            int trackNumber = (int) tags.get("track number");
            int trackCount = (int) tags.get("track count");
            audioBookInfo.setBookNumber(trackNumber);
            audioBookInfo.setTotalTracks(trackCount);
        }
        return audioBookInfo;
    }

}
