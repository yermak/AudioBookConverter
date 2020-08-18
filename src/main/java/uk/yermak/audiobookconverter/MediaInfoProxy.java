package uk.yermak.audiobookconverter;

import java.util.Objects;
import java.util.concurrent.Future;

public class MediaInfoProxy implements MediaInfo {
    private final String filename;
    private final Future<MediaInfo> futureLoad;

    private MediaInfo getMediaInfo() {
        try {
            return this.futureLoad.get();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public int getChannels() {
        return this.getMediaInfo().getChannels();
    }

    public int getFrequency() {
        return this.getMediaInfo().getFrequency();
    }

    public int getBitrate() {
        return this.getMediaInfo().getBitrate();
    }

    @Override
    public String getTitle() {
        return this.getMediaInfo().getTitle();
    }

    @Override
    public String getDetails() {
        return this.getMediaInfo().getDetails();
    }

    public long getDuration() {
        return this.getMediaInfo().getDuration();
    }

    @Override
    public boolean split() {
        return this.getMediaInfo().split();
    }

    @Override
    public void remove() {
        this.getMediaInfo().remove();
    }

    @Override
    public void moveUp() {
        this.getMediaInfo().moveUp();
    }

    @Override
    public void moveDown() {
        this.getMediaInfo().moveDown();
    }

    public String getFileName() {
        return this.filename;
    }

    public AudioBookInfo getBookInfo() {
        return this.getMediaInfo().getBookInfo();
    }

    public ArtWork getArtWork() {
        return this.getMediaInfo().getArtWork();
    }

    public String toString() {
        return this.filename;
    }

    public String getCodec() {
        return this.getMediaInfo().getCodec();
    }

    @Override
    public void setChapter(Chapter chapter) {
        this.getMediaInfo().setChapter(chapter);
    }

    @Override
    public long getOffset() {
        return this.getMediaInfo().getOffset();
    }


    public void setDuration(long duration) {
        this.getMediaInfo().setDuration(duration);
    }

    MediaInfoProxy(final String filename, final Future<MediaInfo> futureLoad) {
        this.filename = filename;
        this.futureLoad = futureLoad;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MediaInfo)) return false;
        MediaInfo that = (MediaInfo) o;
        return getFileName().equals(that.getFileName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getFileName(), getDuration()) ;
    }
}

        