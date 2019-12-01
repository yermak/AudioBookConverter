package uk.yermak.audiobookconverter;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class MediaInfoBean implements MediaInfo {
    private String fileName;
    private int channels;
    private int frequency;
    private int bitrate;
    private long duration;
    private AudioBookInfo bookInfo;
    private ArtWork artWork;
    private String codec;

    public String fileName() {
        return this.fileName;
    }

    private int channels() {
        return this.channels;
    }

    private int frequency() {
        return this.frequency;
    }

    private int bitrate() {
        return this.bitrate;
    }

    private long duration() {
        return this.duration;
    }

    private AudioBookInfo bookInfo() {
        return this.bookInfo;
    }

    private ArtWork artWork() {
        return this.artWork;
    }

    private String codec() {
        return this.codec;
    }

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

    public int getChannels() {
        return this.channels();
    }

    public int getFrequency() {
        return this.frequency();
    }

    public int getBitrate() {
        return this.bitrate();
    }

    @Override
    public String getTitle() {
        return this.getBookInfo().getTitle();
    }

    @Override
    public String getDetails() {
        return this.getFileName();
    }

    public long getDuration() {
        return this.duration();
    }

    public String getFileName() {
        return this.fileName();
    }

    public void setBookInfo(final AudioBookInfo bookInfo) {
        this.bookInfo = bookInfo;
    }

    public AudioBookInfo getBookInfo() {
        return this.bookInfo();
    }

    public ArtWork getArtWork() {
        return this.artWork();
    }

    public void setArtWork(final ArtWork artWork) {
        this.artWork = artWork;
    }

    public String getCodec() {
        return this.codec();
    }

    public void setCodec(final String codec) {
        this.codec = codec;
    }

    public MediaInfoBean(final String fileName) {
        this.fileName = fileName;
        this.channels = 2;
        this.frequency = 44100;
        this.bitrate = 128000;
        this.duration = 0L;
        this.codec = "";
    }
}

        