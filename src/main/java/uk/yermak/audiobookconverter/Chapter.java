package uk.yermak.audiobookconverter;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.apache.commons.io.FilenameUtils;

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
        initRenderMap();
    }

    private void initRenderMap() {
        String[] contextArray = AppSetting.getProperty(AppSetting.CHAPTER_CONTEXT, "CHAPTER_NUMBER:CHAPTER_TEXT:DURATION").split(":");

        Set<String> context = new HashSet<>(Arrays.asList(contextArray));

        if (context.contains("CHAPTER_NUMBER")) {
            renderMap.put("CHAPTER_NUMBER", Chapter::getNumber);
        }
        if (context.contains("CHAPTER_TEXT")) {
            renderMap.put("CHAPTER_TEXT", c -> "Chapter");
        }
        if (context.contains("DURATION")) {
            renderMap.put("DURATION", c -> Duration.ofMillis(c.getDuration()));
        }
        if (context.contains("BOOK_NUMBER")) {
            renderMap.put("BOOK_NUMBER", c -> String.valueOf(c.getPart().getBook().getBookInfo().bookNumber()));
        }
        if (context.contains("BOOK_TITLE")) {
            renderMap.put("BOOK_TITLE", c -> c.getPart().getBook().getBookInfo().title());
        }
        if (context.contains("TAG.1")) {
            renderMap.put("TAG.1", chapter -> chapter.getMedia().get(0).getBookInfo().title());
        }
        if (context.contains("TAG.2")) {
            renderMap.put("TAG.2", chapter -> chapter.getMedia().get(0).getBookInfo().writer());
        }
        if (context.contains("TAG.3")) {
            renderMap.put("TAG.3", chapter -> chapter.getMedia().get(0).getBookInfo().narrator());
        }
        if (context.contains("TAG.4")) {
            renderMap.put("TAG.4", chapter -> chapter.getMedia().get(0).getBookInfo().series());
        }
        if (context.contains("TAG.5")) {
            renderMap.put("TAG.5", chapter -> chapter.getMedia().get(0).getBookInfo().genre());
        }
        if (context.contains("TAG.6")) {
            renderMap.put("TAG.6", chapter -> chapter.getMedia().get(0).getBookInfo().year());
        }
        if (context.contains("TAG.7")) {
            renderMap.put("TAG.7", chapter -> chapter.getMedia().get(0).getBookInfo().comment());
        }
        if (context.contains("TAG.8")) {
            renderMap.put("TAG.8", chapter -> FilenameUtils.getBaseName(chapter.getMedia().get(0).getFileName()));
        }
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
