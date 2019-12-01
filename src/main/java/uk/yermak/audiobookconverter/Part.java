package uk.yermak.audiobookconverter;

import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;

public class Part implements Organisable {
    private String title;

    private ObservableList<MediaInfo> media = new SimpleListProperty<>();
    private int number;

    public Part(int number) {
        this.number = number;
        title = "Part " + number;
    }

    public String getTitle() {
        return title;
    }

    @Override
    public String getDetails() {
        return null;
    }

    @Override
    public long getDuration() {
        return 0;
    }

}
