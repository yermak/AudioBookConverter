package uk.yermak.audiobookconverter;

import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.Arrays;
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
    private Book book;
    private final ObservableList<MediaInfo> media = FXCollections.observableArrayList();
    private final ObservableList<ArtWork> posters = FXCollections.observableArrayList();
    private final SimpleObjectProperty<OutputParameters> outputParameters = new SimpleObjectProperty<>();


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
        if (bookInfo.get()!=null) {
            if (StringUtils.isNotEmpty(bookInfo.get().getGenre()) & genres.stream().noneMatch(s -> s.equals(bookInfo.get().getGenre()))) {
                genres.add(bookInfo.get().getGenre());
            }
            String collect = String.join("::", genres);
            AppProperties.setProperty("genres", collect);
        }
    }

    private void reloadGenres() {
        genres.clear();
        String genresProperty = AppProperties.getProperty("genres");
        if (genresProperty != null) {
            String[] genres = genresProperty.split("::");
            this.genres.addAll(Arrays.stream(genres).sorted().collect(Collectors.toList()));
        }
    }

    public void resetForNewConversion() {
        saveGenres();
        ConversionGroup newConversionGroup = new ConversionGroup();
        conversionGroupHolder.set(newConversionGroup);

        reloadGenres();
        bookInfo.set(new AudioBookInfo());
        outputParameters.set(new OutputParameters());
        book = null;
        posters.clear();
        media.clear();
    }

    public OutputParameters getOutputParameters() {
        return outputParameters.get();
    }

    public Book getBook() {
        return book;
    }

    public void setBook(Book book) {
        this.book = book;
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

    public ObservableList<String> getGenres() {
        return genres;
    }

    public void addJob(ConversionJob conversionJob) {
        conversionQueue.add(conversionJob);
        executorService.execute(conversionJob);
    }
}
