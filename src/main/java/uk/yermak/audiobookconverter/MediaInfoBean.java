package uk.yermak.audiobookconverter;

import java.util.List;
import java.util.Objects;

public class MediaInfoBean extends MediaInfoOrganiser implements MediaInfo {

    private final String fileName;
    private int channels;
    private int frequency;
    private int bitrate;
    private long duration;
    private AudioBookInfo bookInfo;
    private ArtWork artWork;
    private String codec;

    public void setChannels(final int channels) {
        this.channels = channels;
    }

    public void setFrequency(final int frequency) {
        this.frequency = frequency;
    }

    public void setBitrate(final int bitrate) {
        this.bitrate = bitrate;
    }

    public void setDuration(final long duration) {
        this.duration = duration;
    }

    @Override
    public int getUID() {
        return Objects.hash(fileName);
    }

    @Override
    public String getReference() {
        return getFileName();
    }

    public int getChannels() {
        return this.channels;
    }

    public int getFrequency() {
        return this.frequency;
    }

    public int getBitrate() {
        return this.bitrate;
    }

    @Override
    public String getTitle() {
        return this.getBookInfo().title().get();
    }

    @Override
    public String getDetails() {
        return this.getFileName();
    }

    public long getDuration() {
        return this.duration;
    }

    @Override
    public List<MediaInfo> getMedia() {
        return List.of(this);
    }

    public String getFileName() {
        return this.fileName;
    }

    public void setBookInfo(final AudioBookInfo bookInfo) {
        this.bookInfo = bookInfo;
    }

    public AudioBookInfo getBookInfo() {
        return this.bookInfo;
    }

    public ArtWork getArtWork() {
        return this.artWork;
    }

    public void setArtWork(final ArtWork artWork) {
        this.artWork = artWork;
    }

    public String getCodec() {
        return this.codec;
    }

    public void setCodec(final String codec) {
        this.codec = codec;
    }

    @Override
    public long getOffset() {
        return -1;
    }

    public MediaInfoBean(final String fileName) {
        this.fileName = fileName;
        this.channels = 2;
        this.frequency = 44100;
        this.bitrate = 128000;
        this.duration = 0L;
        this.codec = "";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MediaInfo)) return false;
        MediaInfo that = (MediaInfo) o;
        return getReference().equals(that.getReference());
    }

}

