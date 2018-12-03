package uk.yermak.audiobookconverter;

import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.scene.image.Image;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.Track;
import org.apache.commons.io.FileUtils;
import uk.yermak.audiobookconverter.fx.ConverterApplication;

import java.io.File;
import java.util.*;
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
            MediaInfo mediaInfo = new MediaInfoProxy(fileName, completableFuture);
            media.add(mediaInfo);

            Media m = new Media(new File(fileName).toURI().toASCIIString());
            MediaPlayer mediaPlayer = new MediaPlayer(m);
            mediaPlayer.setOnReady(() -> loadMetadata(m, fileName, completableFuture));
        }

        searchForPosters(media, ConverterApplication.getContext().getConversion().getPosters());
        return media;
    }

    private void loadMetadata(Media m, String fileName, CompletableFuture completableFuture) {
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
        if (metadata.get("image") != null) {
            Image image = (Image) metadata.get("image");

            ArtWorkImage artWork = new ArtWorkImage(image);
            mediaInfo.setArtWork(artWork);

            ObservableList<ArtWork> posters = ConverterApplication.getContext().getConversion().getPosters();

            addPosterIfMissing(artWork, posters);


        }

        completableFuture.complete(mediaInfo);
    }

    static void addPosterIfMissing(ArtWork artWork, ObservableList<ArtWork> posters) {
        if (!posters.stream().mapToLong(ArtWork::getCrc32).anyMatch(value -> value == artWork.getCrc32())) {
            posters.add(artWork);
        }
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
            Object trackNumber = tags.get("track number");
            if (trackNumber != null) {
                audioBookInfo.setBookNumber((int) trackNumber);
            }
            Object trackCount = tags.get("track count");
            if (trackCount != null) {
                audioBookInfo.setTotalTracks((int) trackCount);
            }
        }
        return audioBookInfo;
    }


    private Collection<File> findPictures(File dir) {
        return FileUtils.listFiles(dir, new String[]{"jpg", "jpeg", "png", "bmp"}, true);
    }

    private void searchForPosters(List<MediaInfo> media, ObservableList<ArtWork> posters) {
        Set<File> searchDirs = new HashSet<>();
        media.forEach(mi -> searchDirs.add(new File(mi.getFileName()).getParentFile()));

        searchDirs.forEach(d -> findPictures(d).forEach(f -> addPosterIfMissing(new ArtWorkBean(f.getPath(), "png", Utils.checksumCRC32(f)), posters)));
    }
}
