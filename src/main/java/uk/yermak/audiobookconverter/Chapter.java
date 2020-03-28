package uk.yermak.audiobookconverter;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Chapter implements Organisable, Convertable {
    private String details;
    private ObservableList<MediaInfo> media = FXCollections.observableArrayList();
    private Part part;


    public Chapter(Part part, List<MediaInfo> nextMedia) {
        this.part = part;
        this.details = nextMedia.get(0).getTitle();
        nextMedia.forEach(mediaInfo -> mediaInfo.setChapter(this));
        media.addAll(nextMedia);
    }

    public String getTitle() {
        return "Chapter " + getNumber();
    }

    public int getNumber() {
        return part.getChapters().indexOf(this) + 1;
    }

    @Override
    public boolean isTheOnlyOne() {
        return part.getChapters().size() == 1;
    }

    @Override
    public String getDetails() {
        return details;
    }

    @Override
    public long getDuration() {
        return media.stream().mapToLong(MediaInfo::getDuration).sum();
    }

    @Override
    public void split() {
        List<Chapter> currentChapters = new ArrayList<>(part.getChapters().subList(0, getNumber() - 1));
        List<Chapter> nextChapters = new ArrayList<>(part.getChapters().subList(getNumber() - 1, part.getChapters().size()));
        part.getChapters().clear();
        part.getChapters().addAll(currentChapters);
        part.createNextPart(nextChapters);
    }

    @Override
    public void remove() {
        getMedia().clear();
        part.getChapters().remove(this);
        if (part.getChapters().isEmpty()) {
            part.remove();
        }
    }

    @Override
    public void moveUp() {
        if (getNumber() < 2) return;
        Collections.swap(part.getChapters(), getNumber() - 1, getNumber() - 2);
    }

    @Override
    public void moveDown() {
        if (getNumber() > part.getChapters().size()) return;
        Collections.swap(part.getMedia(), getNumber() - 1, getNumber());
    }

    @Override
    public ObservableList<MediaInfo> getMedia() {
        return media;
    }

    @Override
    public List<String> getMetaData(AudioBookInfo bookInfo) {
        List<String> metaData = new ArrayList<>();
        metaData.add("[CHAPTER]");
        metaData.add("TIMEBASE=1/1000");
        metaData.add("START=" + 0);
        metaData.add("END=" + getDuration());
        metaData.add("title= " + Utils.formatChapter(bookInfo.getBookNumber(), this));
        return metaData;
    }

    public void setPart(Part part) {
        this.part = part;
    }

    public void createNextChapter(List<MediaInfo> nextMedia) {
        int i = part.getChapters().indexOf(this);
        part.getChapters().add(i + 1, new Chapter(part, nextMedia));
    }

    public void combine(List<Chapter> mergers) {
        mergers.stream().flatMap(c -> c.getMedia().stream()).forEach(m -> {
            m.setChapter(this);
            getMedia().add(m);
        });
        mergers.forEach(Chapter::remove);
    }

    public void setDetails(String details) {
        this.details = details;
    }
}
