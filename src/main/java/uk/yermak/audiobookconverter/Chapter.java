package uk.yermak.audiobookconverter;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.time.Duration;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Chapter implements Organisable, Convertable {
    private String customTitle;
    private final ObservableList<MediaInfo> media = FXCollections.observableArrayList();
    private Part part;
    private final Map<String, Function<Chapter, Object>> renderMap = new HashMap<>();


    public Chapter(Part part, List<MediaInfo> media) {
        this.part = part;
        this.media.addListener(part.getBook());
        media.forEach(mediaInfo -> mediaInfo.setChapter(this));
        this.media.addAll(media);
        renderMap.put("CHAPTER_NUMBER", Chapter::getNumber);
        renderMap.put("CHAPTER_TEXT", c -> "Chapter");
        renderMap.put("DURATION", c->Duration.ofMillis(c.getDuration()));
    }

    public void replaceMediaWithTracks(MediaInfo mediaInfo, List<Track> tracks) {
        List<MediaTrackAdaptor> adaptors = tracks.stream().map(t -> new MediaTrackAdaptor(mediaInfo, t)).collect(Collectors.toList());
        int position = this.getMedia().indexOf(mediaInfo);
        this.getMedia().remove(position);
        this.getMedia().addAll(position, adaptors);
    }

    public Chapter(MediaInfo mediaInfo) {
        this(null, Collections.singletonList(mediaInfo));
    }

    public String getTitle() {
        return "Chapter " + getNumber();
    }

    public int getNumber() {
        return part.getChapters().indexOf(this) + 1;
    }

    public int getTotalNumbers() {
        return part.getChapters().size();
    }

    @Override
    public boolean isTheOnlyOne() {
        return part.getChapters().size() == 1;
    }

    @Override
    public String getDetails() {
        return Utils.renderChapter(this, renderMap);
    }

    @Override
    public long getDuration() {
        return media.stream().mapToLong(MediaInfo::getDuration).sum();
    }

    @Override
    public boolean split() {
        if (part.getChapters().size() == 1) {
            return false;
        }
        if (getNumber() == 1) {
            return false;
        }
        List<Chapter> currentChapters = new ArrayList<>(part.getChapters().subList(0, getNumber() - 1));
        List<Chapter> nextChapters = new ArrayList<>(part.getChapters().subList(getNumber() - 1, part.getChapters().size()));
        part.getChapters().clear();
        part.getChapters().addAll(currentChapters);
        part.createNextPart(nextChapters);
        return true;
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
        Collections.swap(part.getChapters(), getNumber() - 1, getNumber());
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
        metaData.add("title= " + Utils.renderChapter(this, renderMap));
        return metaData;
    }

    public void setPart(Part part) {
        this.part = part;
        this.media.addListener(part.getBook());
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

    public Part getPart() {
        return part;
    }

    public Map<String, Function<Chapter, Object>> getRenderMap() {
        return renderMap;
    }

    public void setCustomTitle(String customTitle) {
        this.customTitle = customTitle;
    }

    public String getCustomTitle() {
        return customTitle;
    }
}
