package uk.yermak.audiobookconverter.fx;

import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.yermak.audiobookconverter.*;

import java.lang.invoke.MethodHandles;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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


    private final SimpleObjectProperty<AudioBookInfo> bookInfo = new SimpleObjectProperty<>(AudioBookInfo.instance());
    private final SimpleObjectProperty<Book> book = new SimpleObjectProperty<>();
    private final ObservableList<MediaInfo> media = FXCollections.observableArrayList();
    private final ObservableList<ArtWork> posters = FXCollections.observableArrayList();
    private final SimpleObjectProperty<OutputParameters> outputParameters = new SimpleObjectProperty<>(Preset.DEFAULT_OUTPUT_PARAMETERS);

    private final static ExecutorService executorService = Executors.newCachedThreadPool();

    private FFMediaLoader mediaLoader;
    private Set<ArtWork> removedArtWorks = new HashSet<>();


    public ConversionContext() {
//        resetForNewConversion();
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

    public void addJob(ConversionJob conversionJob) {
        conversionQueue.add(conversionJob);
        executorService.execute(conversionJob);
    }

    public ObservableList<ArtWork> getPosters() {
        return posters;
    }

    public ObservableList<MediaInfo> getMedia() {
        return media;
    }

    public ConversionGroup detach() {
        mediaLoader.detach();



        ConversionGroup conversionGroup = conversionGroupHolder.get();
        conversionGroup.setMedia(new ArrayList<>(media));
        conversionGroup.setPosters(new ArrayList<>(posters));
        conversionGroup.setBook(book.get());
        conversionGroup.setBookInfo(bookInfo.get());
        conversionGroup.setOutputParameters(new OutputParameters(outputParameters.get()));
        conversionGroup.setDetached(true);
        AppSetting.saveGenres(bookInfo.get().genre().get());

        ConversionGroup newConversionGroup = new ConversionGroup();
        conversionGroupHolder.set(newConversionGroup);

        bookInfo.set(AudioBookInfo.instance());
        book.set(null);
        posters.clear();
        media.clear();
        removedArtWorks.clear();
        mediaLoader = null;

        return conversionGroup;
    }

    public void addContextDetachListener(InvalidationListener invalidationListener) {
        conversionGroupHolder.addListener(invalidationListener);
    }

    public void setOutputParameters(OutputParameters outputParameters) {
        this.outputParameters.set(outputParameters);
    }

    public void setBookInfo(AudioBookInfo bookInfo) {
        this.bookInfo.set(bookInfo);
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
        Platform.runLater(() -> addPosterIfMissing(artWork));
    }

    public void addPosterIfMissing(ArtWork artWork) {
        if (posters.stream().mapToLong(ArtWork::getCrc32).noneMatch(artWork::matchCrc32)) {
            if (removedArtWorks.stream().mapToLong(ArtWork::getCrc32).noneMatch(artWork::matchCrc32)) {
                posters.add(artWork);
            }
        }
    }

    public void addBookInfoChangeListener(ChangeListener<AudioBookInfo> listener) {
        bookInfo.addListener(listener);
    }

    public void removePoster(int toRemove) {
        Platform.runLater(() -> {
            ArtWork remove = posters.remove(toRemove);
            removeArtWork(remove);
        });
    }

    public void addOutputParametersChangeListener(ChangeListener<OutputParameters> changeListener) {
        outputParameters.addListener(changeListener);
    }

    public void addSpeedChangeListener(ChangeListener<Double> changeListener) {
        outputParameters.get().getSpeedObservable().addListener(changeListener);
    }

    public void setBook(Book book) {
        this.book.set(book);
    }

    public void addBookChangeListener(ChangeListener<Book> listener) {
        book.addListener(listener);
    }

    public OutputParameters getOutputParameters() {
        return outputParameters.get();
    }

    public AudioBookInfo getBookInfo() {
        return bookInfo.get();
    }

    public Book getBook() {
        return book.get();
    }

    public ConversionGroup getConversionGroup() {
        return conversionGroupHolder.get();
    }

    public void addNewMedia(List<MediaInfo> addedMedia) {
        getMedia().addAll(addedMedia);
//        detached = false;
    }

    public Book constructBook(List<MediaInfo> addedMedia) {
        Book book = getBook();
        book.construct(FXCollections.observableArrayList(addedMedia));
//        detached = false;
        return book;
    }

    public void setMediaLoader(FFMediaLoader mediaLoader) {
        this.mediaLoader = mediaLoader;
    }

    public void removeArtWork(ArtWork artWork) {
        removedArtWorks.add(artWork);
    }

}
