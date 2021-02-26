package uk.yermak.audiobookconverter;

import java.util.Collections;

public class Track {

    private long start;
    private long end;
    private String title;
    private String writer;
    private String trackNo;

    public Track(String trackNo) {
        this.trackNo = trackNo;
    }

    public Chapter toChapter(Part part, MediaInfo m) {
        MediaTrackAdaptor mediaTrackAdaptor = new MediaTrackAdaptor(m, this);
        Chapter chapter = new Chapter(part, Collections.singletonList(mediaTrackAdaptor));
        chapter.setCustomTitle(this.getTitle());
        chapter.getRenderMap().clear();
        chapter.getRenderMap().put("CUSTOM_TITLE", Chapter::getCustomTitle);
        mediaTrackAdaptor.setChapter(chapter);
        return chapter;
    }

    public String getTrackNo() {
        return trackNo;
    }

    public void setTrackNo(String trackNo) {
        this.trackNo = trackNo;
    }

    public void setStart(long startTime) {
        this.start = startTime;
    }

    public void setEnd(long endTime) {
        this.end = endTime;
    }

    public long getStart() {
        return start;
    }

    public long getEnd() {
        return end;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public void setWriter(String writer) {
        this.writer = writer;
    }

    public String getWriter() {
        return writer;
    }

    public long getDuration() {
        return end - start;
    }
}
