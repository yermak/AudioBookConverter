package uk.yermak.audiobookconverter;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by yermak on 06-Feb-18.
 */
public class ConversionContext {

    private LinkedList<Conversion> conversionQueue = new LinkedList<>();
    private Conversion conversion = new Conversion();

    public ConversionContext() {
    }

    public void setMedia(List<MediaInfo> media) {
        conversion.setMedia(media);
    }

    public void setMode(ConversionMode mode) {
        conversion.setMode(mode);
    }

    public void setBookInfo(AudioBookInfo bookInfo) {
        conversion.setBookInfo(bookInfo);
    }

    public List<MediaInfo> getMedia() {
        return conversion.getMedia();
    }

    public AudioBookInfo getBookInfo() {
        return conversion.getBookInfo();
    }


    public ConversionMode getMode() {
        return conversion.getMode();
    }

    public void startConversion(String outputDestination, ConversionProgress conversionProgress) {
        conversion.start(outputDestination, conversionProgress);

    }
}
