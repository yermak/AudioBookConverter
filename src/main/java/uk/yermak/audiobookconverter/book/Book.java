package uk.yermak.audiobookconverter.book;

import com.google.common.collect.ImmutableList;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class Book implements Organisable, InvalidationListener {
    final static Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final AudioBookInfo audioBookInfo;

    private final ObservableList<Part> parts = FXCollections.observableArrayList();
    private final List<InvalidationListener> listeners = new ArrayList<>();

    public Book(AudioBookInfo audioBookInfo) {
        this.audioBookInfo = audioBookInfo;
    }

    /**
     * Adds the items as a new part. An item whose media info failed to load is left out, not the whole part.
     * @return file names of the items left out, for a warning
     */
    public List<String> construct(List<MediaInfo> items) {
        List<String> skipped = new ArrayList<>();
        Part part = new Part(this);
        for (MediaInfo item : items) {
            List<Track> tracks;
            try {
                tracks = item.getBookInfo().tracks(); // throws if the probe failed
            } catch (Exception e) {
                logger.error("Leaving {} out of the book, its media info failed to load", item.getFileName(), e);
                skipped.add(item.getFileName());
                continue;
            }
            if (tracks.isEmpty()) {
                part.getChapters().add(new Chapter(part, Collections.singletonList(item)));
            } else {
                for (Track track : tracks) {
                    part.getChapters().add(track.toChapter(part, item));
                }
            }
        }
        if (!part.getChapters().isEmpty()) parts.add(part);
        return skipped;
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
    public boolean split() {
        return false;
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

    public List<MediaInfo> getMedia() {
        return parts.stream().flatMap(part -> part.getChapters().stream().flatMap(chapter -> chapter.getMedia().stream())).collect(Collectors.toList());
    }

    public AudioBookInfo getBookInfo() {
        return audioBookInfo;
    }

    @Override
    public void invalidated(Observable observable) {
        ImmutableList<InvalidationListener> list = ImmutableList.copyOf(listeners);
        list.forEach(invalidationListener -> invalidationListener.invalidated(observable));
    }

    public void addListener(InvalidationListener listener) {
        this.listeners.add(listener);
    }

    @Override
    public int getNumber() {
        return 1;
    }

    @Override
    public int getTotalNumbers() {
        return 1;
    }
}
