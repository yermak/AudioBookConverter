package uk.yermak.audiobookconverter;

import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;

public class Part implements Organisable {
    private SimpleStringProperty title;

    private ObservableList<MediaInfo> media = new SimpleListProperty<>();
    private int number;

    public Part(int number) {
        this.number = number;
        title = new SimpleStringProperty("Part " + number);
    }

    public SimpleStringProperty getTitle() {
        return title;
    }

    public SimpleStringProperty titleProperty() {
        return title;
    }
}
