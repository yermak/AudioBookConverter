package uk.yermak.audiobookconverter;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class Book implements Organisable, InvalidationListener {
    final static Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final AudioBookInfo audioBookInfo;

    private final ObservableList<Part> parts = FXCollections.observableArrayList();
    private InvalidationListener listener;

    public Book(AudioBookInfo audioBookInfo) {
        this.audioBookInfo = audioBookInfo;
    }

    public void construct(ObservableList<MediaInfo> items) {
        try {
            Part part = new Part(this);
            for (MediaInfo item : items) {
                if (item.getBookInfo().tracks().isEmpty()) {
                    Chapter chapter = new Chapter(part, Collections.singletonList(item));
                    part.getChapters().add(chapter);
                } else {
                    List<Track> tracks = item.getBookInfo().tracks();
                    for (Track track : tracks) {
                        Chapter chapter = trackToChapter(part, item, track);
                        part.getChapters().add(chapter);
                    }
                }
            }
            parts.add(part);
        } catch (Throwable e) {
            logger.error("Error constructing book:",e);
        }
    }

    private Chapter trackToChapter(Part part, MediaInfo m, Track track) {
        MediaTrackAdaptor mediaTrackAdaptor = new MediaTrackAdaptor(m, track);
        Chapter chapter = new Chapter(part, Collections.singletonList(mediaTrackAdaptor));
        chapter.setCustomTitle(track.getTitle());
        chapter.getRenderMap().clear();
        chapter.getRenderMap().put("CUSTOM_TITLE", Chapter::getCustomTitle);
        mediaTrackAdaptor.setChapter(chapter);
        return chapter;
    }


    @Override
    public String getTitle() {
        return "";
    }

    @Override
    public String getDetails() {
        return parts.size() + " Chapters";
    }

    @Override
    public long getDuration() {
        return parts.stream().mapToLong(Part::getDuration).sum();
    }

    @Override
    public void split() {

    }

    @Override
    public void remove() {

    }

    @Override
    public void moveUp() {

    }

    @Override
    public void moveDown() {

    }

    public ObservableList<Part> getParts() {
        return parts;
    }


    public List<Chapter> getChapters() {
        return parts.stream().flatMap(part -> part.getChapters().stream()).collect(Collectors.toList());
    }

    public AudioBookInfo getBookInfo() {
        return audioBookInfo;
    }


    @Override
    public void invalidated(Observable observable) {
        if (listener!=null)  listener.invalidated(observable);
    }

    public void addListener(InvalidationListener listener) {
        this.listener = listener;
    }
}
