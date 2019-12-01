package uk.yermak.audiobookconverter;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;

public class Chapter implements Organisable {
    private String title;
    private ObservableList<MediaInfo> media;

    public String getTitle() {
        return title;
    }

    @Override
    public String getDetails() {
        return "";
    }

    @Override
    public String getDuration() {
        return "0";
    }
}
