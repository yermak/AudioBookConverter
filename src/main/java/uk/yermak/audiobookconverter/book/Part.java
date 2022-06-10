package uk.yermak.audiobookconverter.book;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import uk.yermak.audiobookconverter.Utils;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Part implements Organisable, Convertable {

    private final ObservableList<Chapter> chapters = FXCollections.observableArrayList();
    private final Book book;
    private final Map<String, Function<Part, Object>> renderMap = new HashMap<>();


    public Part(Book book) {
        this.book = book;
        chapters.addListener(book);
        renderMap.put("BOOK_NUMBER", part -> part.getBook().getBookInfo().series().trimToNull());
        renderMap.put("SERIES", part -> part.getBook().getBookInfo().series().trimToNull());
        renderMap.put("TITLE", part -> part.getBook().getBookInfo().title().trimToNull());
        renderMap.put("WRITER", part -> part.getBook().getBookInfo().writer().trimToNull());
        renderMap.put("NARRATOR", part -> part.getBook().getBookInfo().narrator().trimToNull());
        renderMap.put("YEAR", part -> part.getBook().getBookInfo().year().trimToNull());
        renderMap.put("PART", Part::getNumber);
        renderMap.put("DURATION", p -> Duration.ofMillis(p.getDuration()));

    }

    public void replaceMediaChapterByTracksChapters(MediaInfo mediaInfo, List<Track> tracks) {
        List<Chapter> chapters = tracks.stream().map(t -> t.toChapter(this, mediaInfo)).collect(Collectors.toList());
        Chapter chapter = mediaInfo.getChapter();
        int position = this.getChapters().indexOf(chapter);
        //ORDER of next 2 lines is important see issue 269, if only one file is added, chapter and parts are removed and there is no place to add sub-tracks. So, sub-tracks should be added first.
        this.getChapters().addAll(position, chapters);
        mediaInfo.remove();
    }

    public void construct(ObservableList<Chapter> chapters) {
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
    public int getTotalNumbers() {
        return getBook().getParts().size();
    }

    @Override
    public boolean isTheOnlyOne() {
        return book.getParts().size() == 1;
    }

    @Override
    public String getDetails() {
        return Utils.renderPart(this, renderMap);
    }

    @Override
    public long getDuration() {
        return chapters.stream().mapToLong(Chapter::getDuration).sum();
    }

    @Override
    public boolean split() {
        return false;
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
        Part part = new Part(book);
        part.construct(FXCollections.observableArrayList(chapters));
        book.getParts().add(i + 1, part);
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
