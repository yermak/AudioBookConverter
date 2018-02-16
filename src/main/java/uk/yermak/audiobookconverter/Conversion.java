package uk.yermak.audiobookconverter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Yermak on 06-Feb-18.
 */
public class Conversion {
    private final static ExecutorService executorService = Executors.newCachedThreadPool();
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

    public void start(String outputDestination, ConversionProgress conversionProgress) {
        ConversionStrategy conversionStrategy = mode.createConvertionStrategy();


        Map<String, ProgressCallback> progressCallbacks = new HashMap<>();
        media.forEach(mediaInfo -> progressCallbacks.put(mediaInfo.getFileName(), new ProgressCallback2(mediaInfo.getFileName(), conversionProgress)));
        progressCallbacks.put("output", new ProgressCallback2("output", conversionProgress));

        conversionStrategy.setCallbacks(progressCallbacks);

        executorService.execute(conversionProgress);

        conversionStrategy.setOutputDestination(outputDestination);
        conversionStrategy.setBookInfo(bookInfo);
        conversionStrategy.setMedia(media);

        executorService.execute(conversionStrategy);
    }
}
