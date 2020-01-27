package uk.yermak.audiobookconverter;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static uk.yermak.audiobookconverter.ProgressStatus.*;

/**
 * Created by Yermak on 06-Feb-18.
 */
public class Conversion {
    final static Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final static ExecutorService executorService = Executors.newCachedThreadPool();
    private SimpleObjectProperty<ProgressStatus> status = new SimpleObjectProperty<>(this, "status", READY);

    private OutputParameters outputParameters;
    private String outputDestination;
    private Convertable convertable;
    private AudioBookInfo bookInfo;


    public List<MediaInfo> getMedia() {
        return getConverable().getMedia();
    }


    public void start(Convertable convertable, String outputDestination, Refreshable refreshable, OutputParameters outputParameters, AudioBookInfo bookInfo) {
        setConverable(convertable);
        setOutputDestination(outputDestination);
        setOutputParameters(outputParameters);
        setBookInfo(bookInfo);

        Executors.newSingleThreadExecutor().execute(refreshable);

        Map<String, ProgressCallback> progressCallbacks = new HashMap<>();

        convertable.getMedia().stream().map(MediaInfo::getFileName).forEach(s -> progressCallbacks.put(s, new ProgressCallback(s, refreshable)));


        progressCallbacks.put("output", new ProgressCallback("output", refreshable));

        ConversionStrategy conversionStrategy = new ParallelConversionStrategy(this, progressCallbacks);

        executorService.execute(conversionStrategy);
        status.set(IN_PROGRESS);
    }

    private void setConverable(Convertable convertable) {
        this.convertable = convertable;
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


    public void setOutputParameters(OutputParameters params) {
        outputParameters = params;
    }

    public OutputParameters getOutputParameters() {
        return outputParameters;
    }


    public String getOutputDestination() {
        return outputDestination;
    }

    public void setOutputDestination(String outputDestination) {
        this.outputDestination = outputDestination;
    }

    public Convertable getConverable() {
        return convertable;
    }

    public void setBookInfo(AudioBookInfo bookInfo) {
        this.bookInfo = bookInfo;
    }

    public AudioBookInfo getBookInfo() {
        return bookInfo;
    }
}


