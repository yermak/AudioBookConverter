package uk.yermak.audiobookconverter;

import java.util.List;
import java.util.Map;

public interface ConversionStrategy extends Runnable {

    void setBookInfo(AudioBookInfo audioBookInfo);

    void setMedia(List<MediaInfo> media);

    void setCallbacks(Map<String, ProgressCallback> progressCallbacks);

    void setOutputDestination(String outputDestination);

    void canceled();

    void paused();
}