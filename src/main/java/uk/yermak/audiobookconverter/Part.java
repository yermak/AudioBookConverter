package uk.yermak.audiobookconverter;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class Part implements Organisable {
    private String title;
    private int number;

    private ObservableList<Chapter> chapters = FXCollections.observableArrayList();


    public Part(int number, ObservableList<MediaInfo> media) {
        this.number = number;
        for (int i = 0; i < media.size(); i++) {
            MediaInfo mediaInfo = media.get(i);
            chapters.add(new Chapter(i + 1, mediaInfo));
        }
    }

    public String getTitle() {
        return "Part " + number;
    }

    @Override
    public String getDetails() {
        return title;
    }

    @Override
    public long getDuration() {
        return chapters.stream().mapToLong(Chapter::getDuration).sum();

    }

    public ObservableList<Chapter> getChapters() {
        return chapters;
    }

}
