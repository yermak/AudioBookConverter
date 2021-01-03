package uk.yermak.audiobookconverter;

import com.google.gson.Gson;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * Created by yermak on 06-Feb-18.
 */
public class ConversionContext {

    final static Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final LinkedList<ConversionJob> conversionQueue = new LinkedList<>();
    private final SimpleObjectProperty<ConversionGroup> conversionGroupHolder = new SimpleObjectProperty<>(new ConversionGroup());
    private boolean paused;

    private final ObservableList<MediaInfo> selectedMedia = FXCollections.observableArrayList();
    private final ObservableList<String> genres = FXCollections.observableArrayList();

    private final SimpleObjectProperty<AudioBookInfo> bookInfo = new SimpleObjectProperty<>();
    private final SimpleObjectProperty<Book> book = new SimpleObjectProperty<>();
    private final ObservableList<MediaInfo> media = FXCollections.observableArrayList();
    private final ObservableList<ArtWork> posters = FXCollections.observableArrayList();
    private final SimpleObjectProperty<OutputParameters> outputParameters = new SimpleObjectProperty<>(Preset.DEFAULT_OUTPUT_PARAMETERS);

    private final static ExecutorService executorService = Executors.newCachedThreadPool();


    public ConversionContext() {
        resetForNewConversion();
    }

    //TODO move somewhere
    public void movePosterLeft(final Integer selected) {
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

    public void saveGenres() {
        if (bookInfo.get() != null) {
            if (StringUtils.isNotEmpty(bookInfo.get().genre().get()) & genres.stream().noneMatch(s -> s.equals(bookInfo.get().genre().get()))) {
                genres.add(bookInfo.get().genre().get());
            }
            Gson gson = new Gson();
            String genresString = gson.toJson(new ArrayList<>(genres));
            AppProperties.setProperty("genres", genresString);
        }
    }

    public ObservableList<String> loadGenres() {
        genres.clear();
        String genresProperty = AppProperties.getProperty("genres");
        if (genresProperty != null) {
            Gson gson = new Gson();
            ArrayList<String> list = gson.fromJson(genresProperty, ArrayList.class);
            this.genres.addAll(list.stream().sorted().collect(Collectors.toList()));
        }
        return genres;
    }

    public void resetForNewConversion() {
        saveGenres();
        ConversionGroup newConversionGroup = new ConversionGroup();
        conversionGroupHolder.set(newConversionGroup);
        bookInfo.set(AudioBookInfo.instance());
        book.set(null);
        posters.clear();
        media.clear();
    }

    public OutputParameters getOutputParameters() {
        return outputParameters.get();
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

    public void stopConversions() {
        conversionQueue.forEach(ConversionJob::stop);
    }

    public ObservableList<MediaInfo> getSelectedMedia() {
        return selectedMedia;
    }

    public void pauseConversions() {
        conversionQueue.forEach(ConversionJob::pause);
        paused = true;
    }

    public void resumeConversions() {
        conversionQueue.forEach(ConversionJob::resume);
        paused = false;
    }

    public boolean isPaused() {
        return paused;
    }

    public SimpleObjectProperty<AudioBookInfo> getBookInfo() {
        return bookInfo;
    }

    public ObservableList<ArtWork> getPosters() {
        return posters;
    }

    public ConversionGroup getPlannedConversionGroup() {
        return conversionGroupHolder.get();
    }

    public void addBookInfoChangeListener(ChangeListener<AudioBookInfo> listener) {
        bookInfo.addListener(listener);
    }

    public void removePoster(int toRemove) {
        Platform.runLater(() -> posters.remove(toRemove));
    }

    public void addJob(ConversionJob conversionJob) {
        conversionQueue.add(conversionJob);
        executorService.execute(conversionJob);
    }

    public void addBookChangeListener(ChangeListener<Book> listener) {
        book.addListener(listener);
    }

    public void addOutputParametersChangeListener(ChangeListener<OutputParameters> changeListener) {
        outputParameters.addListener(changeListener);
    }

    public void setOutputParameters(OutputParameters outputParameters) {
        this.outputParameters.set(outputParameters);
    }
}
