package uk.yermak.audiobookconverter;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Book implements Organisable {
    private final AudioBookInfo audioBookInfo;

    private ObservableList<Part> parts = FXCollections.observableArrayList();

    public Book(ObservableList<MediaInfo> items, AudioBookInfo audioBookInfo) {
        this.audioBookInfo = audioBookInfo;
        items.stream()
                .filter(m -> !m.getBookInfo().getTracks().isEmpty())
                .forEach(m -> {
                    ArrayList<Chapter> chapters = new ArrayList<>();
                    List<Track> tracks = m.getBookInfo().getTracks();
                    for (Track track : tracks) {
                        MediaTrackAdaptor mediaTrackAdaptor = new MediaTrackAdaptor(m, track);
                        Chapter chapter = new Chapter(mediaTrackAdaptor);
                        chapter.setCustomTitle(track.getTitle());
                        chapter.getRenderMap().clear();
                        chapter.getRenderMap().put("CUSTOM_TITLE", Chapter::getCustomTitle);

                        mediaTrackAdaptor.setChapter(chapter);
                        chapters.add(chapter);
                    }
                    Part part = new Part(this, chapters);
                    parts.add(part);
                });

        List<Chapter> chapters = items.stream()
                .filter(m -> m.getBookInfo().getTracks().isEmpty())
                .map(Chapter::new)
                .collect(Collectors.toList());
        if (!chapters.isEmpty()) {
            Part part = new Part(this, chapters);
            parts.add(part);
        }
    }

    @Override
    public String getTitle() {
        return "";
    }

    @Override
    public String getDetails() {
        return parts.size() + " Chapters";
    }

    @Override
    public long getDuration() {
        return parts.stream().mapToLong(Part::getDuration).sum();
    }

    @Override
    public void split() {

    }

    @Override
    public void remove() {

    }

    @Override
    public void moveUp() {

    }

    @Override
    public void moveDown() {

    }

    public ObservableList<Part> getParts() {
        return parts;
    }


    public List<Chapter> getChapters() {
        return parts.stream().flatMap(part -> part.getChapters().stream()).collect(Collectors.toList());
    }

    public AudioBookInfo getBookInfo() {
        return audioBookInfo;
    }
}
