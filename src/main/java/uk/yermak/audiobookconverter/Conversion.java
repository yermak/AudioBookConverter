package uk.yermak.audiobookconverter;

import java.util.List;
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

    public void start(String outputDestination) {
        ConversionStrategy convertionStrategy = mode.createConvertionStrategy();
        JobProgress jobProgress = new JobProgress(convertionStrategy, null, media);
        executorService.execute(jobProgress);

        convertionStrategy.setOutputDestination(outputDestination);
        convertionStrategy.setBookInfo(bookInfo);
        convertionStrategy.setMedia(media);

        executorService.execute(convertionStrategy);
    }
}
