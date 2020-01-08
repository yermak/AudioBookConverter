package uk.yermak.audiobookconverter;

import java.util.concurrent.Future;

public class MediaInfoProxy implements MediaInfo {
    private final String filename;
    private final Future futureLoad;

    private MediaInfo getMediaInfo() {
        try {
            return (MediaInfo) this.futureLoad.get();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void setChannels(final int channels) {
        this.getMediaInfo().setChannels(channels);
    }

    public void setFrequency(final int frequency) {
        this.getMediaInfo().setFrequency(frequency);
    }

    public void setBitrate(final int bitrate) {
        this.getMediaInfo().setBitrate(bitrate);
    }

    public void setDuration(final long duration) {
        this.getMediaInfo().setDuration(duration);
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
    public void split() {
        this.getMediaInfo().split();
    }

    @Override
    public void remove() {
        this.getMediaInfo().remove();
    }

    public String getFileName() {
        return this.filename;
    }

    public void setBookInfo(final AudioBookInfo bookInfo) {
        this.getMediaInfo().setBookInfo(bookInfo);
    }

    public AudioBookInfo getBookInfo() {
        return this.getMediaInfo().getBookInfo();
    }

    public ArtWork getArtWork() {
        return this.getMediaInfo().getArtWork();
    }

    public void setArtWork(final ArtWork artWork) {
        this.getMediaInfo().setArtWork(artWork);
    }

    public String toString() {
        return this.filename;
    }

    public String getCodec() {
        return this.getMediaInfo().getCodec();
    }

    public void setCodec(final String codec) {
        this.getMediaInfo().setCodec(codec);
    }

    MediaInfoProxy(final String filename, final Future futureLoad) {
        this.filename = filename;
        this.futureLoad = futureLoad;
    }
}

        