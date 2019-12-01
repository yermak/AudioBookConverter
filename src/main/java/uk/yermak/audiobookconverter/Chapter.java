package uk.yermak.audiobookconverter;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.function.ToLongFunction;

public class Chapter implements Organisable {
    private final int number;
    private String details;
    private ObservableList<MediaInfo> media = FXCollections.observableArrayList();

    public Chapter(int number, MediaInfo mediaInfo) {
        this.number = number;
        this.details = mediaInfo.getTitle();
        media.add(mediaInfo);
    }

    public String getTitle() {
        return "Chapter " + number;
    }

    @Override
    public String getDetails() {
        return details;
    }

    @Override
    public long getDuration() {
        return media.stream().mapToLong(MediaInfo::getDuration).sum();
    }
}
