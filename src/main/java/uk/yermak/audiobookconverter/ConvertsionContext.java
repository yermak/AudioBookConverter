package uk.yermak.audiobookconverter;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by yermak on 06-Feb-18.
 */
public class ConvertsionContext {

    private LinkedList<Conversion> conversions = new LinkedList<>();

    public ConvertsionContext() {
        conversions.add(new Conversion());
    }

    public void setMedia(List<MediaInfo> media) {
        conversions.getLast().setMedia(media);
    }

    public void setMode(ConversionMode mode) {
        conversions.getLast().setMode(mode);
    }

    public void setBookInfo(AudioBookInfo bookInfo) {
        conversions.getLast().setBookInfo(bookInfo);
    }

    public List<MediaInfo> getMedia() {
        return conversions.getLast().getMedia();
    }
}
