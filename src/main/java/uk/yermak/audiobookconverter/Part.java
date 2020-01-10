package uk.yermak.audiobookconverter;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class Part implements Organisable {
    private String title;

    private ObservableList<Chapter> chapters = FXCollections.observableArrayList();
    private Book book;


    public Part(Book book, ObservableList<MediaInfo> media) {
        this.book = book;
        media.forEach(m -> chapters.add(new Chapter(this, Collections.singletonList(m))));
    }

    public Part(Book book, List<Chapter> chapters) {
        this.book = book;
        chapters.forEach(c -> c.setPart(this));
        this.chapters.addAll(chapters);
    }

    public String getTitle() {
        return "Part " + getNumber();
    }

    private Object getNumber() {
        return getBook().getParts().indexOf(this) + 1;
    }

    @Override
    public String getDetails() {
        return title;
    }

    @Override
    public long getDuration() {
        return chapters.stream().mapToLong(Chapter::getDuration).sum();
    }

    @Override
    public void split() {

    }

    @Override
    public void remove() {
        getChapters().clear();
        book.getParts().remove(this);
    }

    @Override
    public void moveUp() {

    }

    @Override
    public void moveDown() {

    }

    public ObservableList<Chapter> getChapters() {
        return chapters;
    }

    public List<MediaInfo> getChaptersMedia() {
        List<MediaInfo> media = chapters.stream().flatMap(chapter -> chapter.getMedia().stream()).collect(Collectors.toList());
        return media;
    }

    public Book getBook() {
        return this.book;
    }

    public void createNextPart(List<Chapter> chapters) {
        int i = book.getParts().indexOf(this);
        book.getParts().add(i + 1, new Part(book, chapters));
    }

    public void combine(List<Part> mergers) {
        mergers.stream().flatMap(p -> p.getChapters().stream()).forEach(c -> {
            c.setPart(this);
            getChapters().add(c);
        });
        mergers.forEach(Part::remove);
    }
}
