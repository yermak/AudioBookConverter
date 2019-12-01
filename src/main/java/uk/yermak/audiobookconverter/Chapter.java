package uk.yermak.audiobookconverter;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;

public class Chapter implements Organisable {
    private String title;
    private ObservableList<MediaInfo> media;

    public Chapter(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    @Override
    public String getDetails() {
        return "";
    }

    @Override
    public long getDuration() {
        return 0L;
    }
}
