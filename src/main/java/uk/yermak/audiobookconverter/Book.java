package uk.yermak.audiobookconverter;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class Book implements Organisable {
    private ObservableList<Part> parts = FXCollections.observableArrayList();

    public Book(ObservableList<MediaInfo> items) {
        Part firstPart = new Part(1, items);
        parts.add(firstPart);
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

    public ObservableList<Part> getParts() {
        return parts;
    }
}
