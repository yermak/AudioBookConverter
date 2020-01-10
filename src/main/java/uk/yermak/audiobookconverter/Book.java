package uk.yermak.audiobookconverter;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class Book implements Organisable {
    private ObservableList<Part> parts = FXCollections.observableArrayList();

    public Book(ObservableList<MediaInfo> items) {
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
}
