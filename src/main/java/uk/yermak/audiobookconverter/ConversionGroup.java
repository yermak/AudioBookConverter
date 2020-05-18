package uk.yermak.audiobookconverter;

import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.yermak.audiobookconverter.fx.ConverterApplication;

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
public class ConversionGroup {
    final static Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final static ExecutorService executorService = Executors.newCachedThreadPool();
    private SimpleObjectProperty<ProgressStatus> status = new SimpleObjectProperty<>(this, "status", READY);

    private OutputParameters outputParameters;
    private Convertable convertable;
    private AudioBookInfo bookInfo;
    private List<ArtWork> posters;

    public List<MediaInfo> getMedia() {
        return getConverable().getMedia();
    }

    public void start(Convertable convertable, Refreshable refreshable, String outputDestination) {
        this.convertable = convertable;

        addStatusChangeListener((observable, oldValue, newValue) -> {
            if (ProgressStatus.FINISHED.equals(newValue)) {
                Platform.runLater(() -> ConverterApplication.showNotification(outputDestination));
            }
        });

        Executors.newSingleThreadExecutor().execute(refreshable);

        Map<String, ProgressCallback> progressCallbacks = new HashMap<>();

        convertable.getMedia().stream().map(m -> (m.getFileName() + "-" + m.getDuration())).forEach(key -> progressCallbacks.put(key, new ProgressCallback(key, refreshable)));


        progressCallbacks.put("output", new ProgressCallback("output", refreshable));
        ConversionStrategy conversionStrategy = new ConversionJob(this, progressCallbacks, outputDestination);

        executorService.execute(conversionStrategy);
        status.set(IN_PROGRESS);
    }

/*
    public void start(Convertable convertable, String outputDestination, Refreshable refreshable, OutputParameters outputParameters, AudioBookInfo bookInfo, ObservableList<ArtWork> posters) {
        this.convertable = convertable;
        this.outputDestination = outputDestination;
        this.outputParameters = outputParameters;
        this.bookInfo = bookInfo;
        this.posters = new ArrayList<>(posters);

        Executors.newSingleThreadExecutor().execute(refreshable);

        Map<String, ProgressCallback> progressCallbacks = new HashMap<>();

        convertable.getMedia().stream().map(m -> (m.getFileName() + "-" + m.getDuration())).forEach(key -> progressCallbacks.put(key, new ProgressCallback(key, refreshable)));


        progressCallbacks.put("output", new ProgressCallback("output", refreshable));

        ConversionStrategy conversionStrategy = new ParallelConversionStrategy(this, progressCallbacks);

        executorService.execute(conversionStrategy);
        status.set(IN_PROGRESS);
    }

*/

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

    public OutputParameters getOutputParameters() {
        return outputParameters;
    }

    public Convertable getConverable() {
        return convertable;
    }

    public AudioBookInfo getBookInfo() {
        return bookInfo;
    }

    public List<ArtWork> getPosters() {
        return posters;
    }

    public String getWorkfileExtension() {
        return outputParameters.format.extension;
    }

    public void setOutputParameters(OutputParameters outputParameters) {
        this.outputParameters = outputParameters;
    }

    public void setBookInfo(AudioBookInfo bookInfo) {
        this.bookInfo = bookInfo;
    }

    public void setPosters(List<ArtWork> posters) {
        this.posters = posters;
    }
}


