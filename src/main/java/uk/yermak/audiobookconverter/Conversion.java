package uk.yermak.audiobookconverter;

import java.util.List;

/**
 * Created by Yermak on 06-Feb-18.
 */
public class Conversion {
    private List<MediaInfo> media;
    private ConversionMode mode;
    private AudioBookInfo bookInfo;

    public void setMedia(List<MediaInfo> media) {
        this.media = media;
    }

    public void setMode(ConversionMode mode) {
        this.mode = mode;
    }

    public void setBookInfo(AudioBookInfo bookInfo) {
        this.bookInfo = bookInfo;
    }

    public List<MediaInfo> getMedia() {
        return media;
    }

    public ConversionMode getMode() {
        return mode;
    }

    public AudioBookInfo getBookInfo() {
        return bookInfo;
    }
}
