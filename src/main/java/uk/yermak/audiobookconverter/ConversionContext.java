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
import java.util.stream.Collectors;

/**
 * Created by yermak on 06-Feb-18.
 */
public class ConversionContext {

    final static Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private LinkedList<ConversionGroup> conversionGroupQueue = new LinkedList<>();
    private SimpleObjectProperty<ConversionGroup> conversionHolder = new SimpleObjectProperty<>(new ConversionGroup());
    private boolean paused;

    private ObservableList<MediaInfo> selectedMedia = FXCollections.observableArrayList();
    private ObservableList<String> genres = FXCollections.observableArrayList();

    private SimpleObjectProperty<AudioBookInfo> bookInfo = new SimpleObjectProperty<>();
    private Book book;
    private ObservableList<MediaInfo> media = FXCollections.observableArrayList();
    private ObservableList<ArtWork> posters = FXCollections.observableArrayList();
    private SimpleObjectProperty<OutputParameters> outputParameters = new SimpleObjectProperty<>();

    public ConversionContext() {
        conversionGroupQueue.add(conversionHolder.get());
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
        conversionGroupQueue.add(newConversionGroup);
        conversionHolder.set(newConversionGroup);

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
        conversionGroupQueue.forEach(ConversionGroup::stop);
    }

    public ObservableList<MediaInfo> getSelectedMedia() {
        return selectedMedia;
    }

    public void pauseConversions() {
        conversionGroupQueue.forEach(ConversionGroup::pause);
        paused = true;
    }

    public void resumeConversions() {
        conversionGroupQueue.forEach(ConversionGroup::resume);
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

    public ConversionGroup getPlannedConversion() {
        return conversionHolder.get();
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
}
