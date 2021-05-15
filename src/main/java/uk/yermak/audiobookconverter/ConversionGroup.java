package uk.yermak.audiobookconverter;

import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.yermak.audiobookconverter.fx.ConversionProgress;
import uk.yermak.audiobookconverter.fx.ConverterApplication;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

/**
 * Created by Yermak on 06-Feb-18.
 */
public class ConversionGroup {
    final static Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final List<ConversionJob> jobs = new ArrayList<>();
    private boolean cancelled;

    private final SimpleObjectProperty<AudioBookInfo> bookInfo = new SimpleObjectProperty<>(AudioBookInfo.instance());
    private final SimpleObjectProperty<Book> book = new SimpleObjectProperty<>();
    private final ObservableList<MediaInfo> media = FXCollections.observableArrayList();
    private final ObservableList<ArtWork> posters = FXCollections.observableArrayList();
    private final SimpleObjectProperty<OutputParameters> outputParameters = new SimpleObjectProperty<>(Preset.DEFAULT_OUTPUT_PARAMETERS);
//    private final SimpleObjectProperty<Double> speed = new SimpleObjectProperty<>(1.0);

    public ConversionProgress start(Convertable convertable, String outputDestination) {

        Map<String, ProgressCallback> progressCallbacks = new HashMap<>();
        ConversionJob conversionJob = new ConversionJob(this, convertable, progressCallbacks, outputDestination);

        ConversionProgress conversionProgress = new ConversionProgress(conversionJob);

        progressCallbacks.put("output", new ProgressCallback("output", conversionProgress));
        convertable.getMedia().stream().map(m -> (m.getFileName() + "-" + m.getDuration())).forEach(key -> progressCallbacks.put(key, new ProgressCallback(key, conversionProgress)));

        jobs.add(conversionJob);
        Executors.newSingleThreadExecutor().execute(conversionProgress);
        ConverterApplication.getContext().addJob(conversionJob);
        return conversionProgress;
    }

    public OutputParameters getOutputParameters() {
        return outputParameters.get();
    }

    public AudioBookInfo getBookInfo() {
        return bookInfo.get();
    }

    public ObservableList<ArtWork> getPosters() {
        return posters;
    }

    public String getWorkfileExtension() {
        return outputParameters.get().format.extension;
    }

    public void setOutputParameters(OutputParameters outputParameters) {
        this.outputParameters.set(outputParameters);
    }

    public void setBookInfo(AudioBookInfo bookInfo) {
        this.bookInfo.set(bookInfo);
    }

    public boolean isOver() {
        if (cancelled) return true;
        if (jobs.isEmpty()) return false;
        for (ConversionJob job : jobs) {
            if (!job.getStatus().isOver()) return false;
        }
        return true;
    }

    public void cancel() {
        cancelled = true;
    }


    public void movePosterUp(final Integer selected) {
        Platform.runLater(() -> {
            ArtWork lower = posters.get(selected);
            ArtWork upper = posters.get(selected - 1);
            posters.set(selected - 1, lower);
            posters.set(selected, upper);
        });
    }

    public void addPosterIfMissingWithDelay(ArtWork artWork) {
        Platform.runLater(() -> {
            addPosterIfMissing(artWork);
        });
    }

    void addPosterIfMissing(ArtWork artWork) {
        if (posters.stream().mapToLong(ArtWork::getCrc32).noneMatch(artWork::matchCrc32)) {
            posters.add(artWork);
        }
    }

    public void addBookInfoChangeListener(ChangeListener<AudioBookInfo> listener) {
        bookInfo.addListener(listener);
    }

    public void removePoster(int toRemove) {
        Platform.runLater(() -> posters.remove(toRemove));
    }

    public void addOutputParametersChangeListener(ChangeListener<OutputParameters> changeListener) {
        outputParameters.addListener(changeListener);
    }


    public void addSpeedChangeListener(ChangeListener<Double> changeListener) {
        outputParameters.get().speed.addListener(changeListener);
    }

    public void setSpeed(Double speed) {
        this.outputParameters.get().setSpeed(speed);
    }

    public Book getBook() {
        return book.get();
    }

    public void setBook(Book book) {
        this.book.set(book);
    }

    public ObservableList<MediaInfo> getMedia() {
        return media;
    }

    public Double getSpeed() {
        return outputParameters.get().getSpeed();
    }

    public ObservableValue<Double> getSpeedObservable() {
        return outputParameters.get().speed;
    }

    public void addBookChangeListener(ChangeListener<Book> listener) {
        book.addListener(listener);
    }

}


