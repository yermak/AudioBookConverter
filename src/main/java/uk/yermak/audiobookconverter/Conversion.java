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
    private SimpleObjectProperty<ConversionMode> mode = new SimpleObjectProperty<>(ConversionMode.PARALLEL);
    private SimpleObjectProperty<ProgressStatus> status = new SimpleObjectProperty<>(this, "status", READY);

    private AudioBookInfo bookInfo;
    private OutputParameters outputParameters;
    private String outputDestination;


    public void setMode(ConversionMode mode) {
        this.mode.set(mode);
    }

    public void setBookInfo(AudioBookInfo bookInfo) {
        this.bookInfo = bookInfo;
    }

    public ObservableList<MediaInfo> getMedia() {
        return media;
    }

    public ConversionMode getMode() {
        return mode.get();
    }

    public AudioBookInfo getBookInfo() {
        return bookInfo;
    }

    public void start(String outputDestination, Refreshable refreshable) {
        setOutputDestination(outputDestination);

        Executors.newSingleThreadExecutor().execute(refreshable);


        Map<String, ProgressCallback> progressCallbacks = new HashMap<>();
        media.forEach(mediaInfo -> progressCallbacks.put(mediaInfo.getFileName(), new ProgressCallback(mediaInfo.getFileName(), refreshable)));
        progressCallbacks.put("output", new ProgressCallback("output", refreshable));

        ConversionStrategy conversionStrategy = mode.get().createConvertionStrategy(this, progressCallbacks);


        executorService.execute(conversionStrategy);
        status.set(IN_PROGRESS);
    }


    public void addStatusChangeListener(ChangeListener<ProgressStatus> listener) {
        status.addListener(listener);
    }

    public void pause() {
        if (status.get().equals(IN_PROGRESS)) {
            status.set(PAUSED);
        }
    }

    public void stop() {
        if (!status.get().equals(FINISHED)) {
            status.set(CANCELLED);
        }
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
        if (status.get().equals(PAUSED)) {
            status.set(IN_PROGRESS);
        }
    }

    public void removeStatusChangeListener(ChangeListener<ProgressStatus> listener) {
        if (listener != null) status.removeListener(listener);
    }

    public void addModeChangeListener(ChangeListener<ConversionMode> listener) {
        mode.addListener(listener);
    }

    public void setOutputParameters(OutputParameters params) {
        outputParameters = params;
    }

    public OutputParameters getOutputParameters() {
        return outputParameters;
    }

    public ObservableList<ArtWork> getPosters() {
        return bookInfo.getPosters();
    }

    public String getOutputDestination() {
        return outputDestination;
    }

    public void setOutputDestination(String outputDestination) {
        this.outputDestination = outputDestination;
    }
}


