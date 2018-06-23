package uk.yermak.audiobookconverter;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static uk.yermak.audiobookconverter.ProgressStatus.*;

/**
 * Created by Yermak on 06-Feb-18.
 */
public class Conversion {
    private final static ExecutorService executorService = Executors.newCachedThreadPool();
    private ObservableList<MediaInfo> media = FXCollections.observableArrayList();
    private ConversionMode mode = ConversionMode.PARALLEL;
    private AudioBookInfo bookInfo;
    private SimpleObjectProperty<ProgressStatus> status = new SimpleObjectProperty<>(this, "status", READY);

    public void setMode(ConversionMode mode) {
        this.mode = mode;
    }

    public void setBookInfo(AudioBookInfo bookInfo) {
        this.bookInfo = bookInfo;
    }

    public ObservableList<MediaInfo> getMedia() {
        return media;
    }

    public ConversionMode getMode() {
        return mode;
    }

    public AudioBookInfo getBookInfo() {
        return bookInfo;
    }

    public void start(String outputDestination, Refreshable refreshable) {
        status.set(IN_PROGRESS);

        Executors.newSingleThreadExecutor().execute(refreshable);
        ConversionStrategy conversionStrategy = mode.createConvertionStrategy();

        Map<String, ProgressCallback> progressCallbacks = new HashMap<>();
        media.forEach(mediaInfo -> progressCallbacks.put(mediaInfo.getFileName(), new ProgressCallback(mediaInfo.getFileName(), refreshable)));
        progressCallbacks.put("output", new ProgressCallback("output", refreshable));

        conversionStrategy.setCallbacks(progressCallbacks);

        conversionStrategy.setOutputDestination(outputDestination);
        conversionStrategy.setBookInfo(bookInfo);
        conversionStrategy.setMedia(media);


        executorService.execute(conversionStrategy);
    }


    public void addStatusChangeListener(ChangeListener<ProgressStatus> listener) {
        status.addListener(listener);
    }

    public void pause() {
        status.set(PAUSED);
    }

    public void stop() {
        status.set(CANCELLED);
    }

    public ProgressStatus getStatus() {
        return status.get();
    }


    public void finished() {
        status.set(FINISHED);
    }

    public void error(String message) {
        status.set(ERROR);
    }

    public void resume() {
        status.set(IN_PROGRESS);
    }
}


