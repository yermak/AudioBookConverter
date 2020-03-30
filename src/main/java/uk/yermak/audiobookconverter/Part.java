package uk.yermak.audiobookconverter;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Part implements Organisable, Convertable {
    private String title;

    private ObservableList<Chapter> chapters = FXCollections.observableArrayList();
    private Book book;

    public Part(Book book, List<Chapter> chapters) {
        this.book = book;
        chapters.forEach(c -> c.setPart(this));
        this.chapters.addAll(chapters);
    }

    public String getTitle() {
        return "Part " + getNumber();
    }

    public int getNumber() {
        return getBook().getParts().indexOf(this) + 1;
    }

    @Override
    public boolean isTheOnlyOne() {
        return book.getParts().size() == 1;
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

    @Override
    public List<MediaInfo> getMedia() {
        return chapters.stream().flatMap(chapter -> chapter.getMedia().stream()).collect(Collectors.toList());
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

    @Override
    public List<String> getMetaData(AudioBookInfo bookInfo) {
        List<String> metaData = new ArrayList<>();
        long totalDuration = 0;
        for (Chapter chapter : getChapters()) {
            metaData.add("[CHAPTER]");
            metaData.add("TIMEBASE=1/1000");
            metaData.add("START=" + totalDuration);
            totalDuration += chapter.getDuration();
            metaData.add("END=" + totalDuration);
            metaData.add("title= " + Utils.renderChapter(chapter, chapter.getRenderMap()));
        }
        return metaData;
    }
}
