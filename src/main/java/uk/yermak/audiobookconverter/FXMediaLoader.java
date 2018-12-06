package uk.yermak.audiobookconverter;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.scene.image.Image;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.Track;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * Created by yermak on 1/10/2018.
 */
public class FXMediaLoader implements MediaLoader {

    private final StatusChangeListener listener;
    private List<String> fileNames;
    private Conversion conversion;

    public FXMediaLoader(List<String> files, Conversion conversion) {
        this.fileNames = files;
        this.conversion = conversion;
        Collections.sort(fileNames);

        listener = new StatusChangeListener();
        conversion.addStatusChangeListener(listener);
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

        searchForPosters(media, conversion.getPosters());
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
            ObservableList<ArtWork> posters = conversion.getPosters();
            Platform.runLater(() -> addPosterIfMissing(artWork, posters));
        }

        completableFuture.complete(mediaInfo);
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


    static Collection<File> findPictures(File dir) {
        return FileUtils.listFiles(dir, new String[]{"jpg", "jpeg", "png", "bmp"}, true);
    }

    static void searchForPosters(List<MediaInfo> media, ObservableList<ArtWork> posters) {
        Set<File> searchDirs = new HashSet<>();
        media.forEach(mi -> searchDirs.add(new File(mi.getFileName()).getParentFile()));

        searchDirs.forEach(d -> findPictures(d).forEach(f -> addPosterIfMissing(new ArtWorkBean(f.getPath(), Utils.checksumCRC32(f)), posters)));
    }

    static void addPosterIfMissing(ArtWork artWork, ObservableList<ArtWork> posters) {
        if (!posters.stream().mapToLong(ArtWork::getCrc32).anyMatch(value -> value == artWork.getCrc32())) {
            posters.add(artWork);
        }
    }
}
