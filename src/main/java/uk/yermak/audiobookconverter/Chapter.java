package uk.yermak.audiobookconverter;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.function.ToLongFunction;

public class Chapter implements Organisable {
    private final int number;
    private String details;
    private ObservableList<MediaInfo> media = FXCollections.observableArrayList();
    private String customTitle;

    public Chapter(int number, MediaInfo mediaInfo) {
        this.number = number;
        this.details = mediaInfo.getTitle();
        media.add(mediaInfo);
    }

    public String getTitle() {
        if (customTitle != null) {
            return number + ":" + customTitle;
        }
        return "Chapter " + number;
    }

    public int getNumber() {
        return number;
    }

    @Override
    public String getDetails() {
        return details;
    }

    @Override
    public long getDuration() {
        return media.stream().mapToLong(MediaInfo::getDuration).sum();
    }

    public ObservableList<MediaInfo> getMedia() {
        return media;
    }

    public String getCustomTitle() {
        return customTitle;
    }

    public void setCustomTitle(String customTitle) {
        this.customTitle = customTitle;
    }
}
