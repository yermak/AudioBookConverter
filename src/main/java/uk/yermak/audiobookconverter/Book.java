package uk.yermak.audiobookconverter;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.List;
import java.util.stream.Collectors;

public class Book implements Organisable {
    private final AudioBookInfo audioBookInfo;
    private ObservableList<Part> parts = FXCollections.observableArrayList();

    public Book(ObservableList<MediaInfo> items, AudioBookInfo audioBookInfo) {
        this.audioBookInfo = audioBookInfo;
        Part part = new Part(this, items);
        parts.add(part);
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
}
