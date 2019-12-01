package uk.yermak.audiobookconverter;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;

public class Chapter {
    private SimpleStringProperty title = new SimpleStringProperty();
    private ObservableList<MediaInfo> media;

    public SimpleStringProperty getTitle() {
        return title;
    }
}
