package uk.yermak.audiobookconverter;

import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.LinkedList;
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
    private final GenresManager genresManager = new GenresManager();

/*
    private final SimpleObjectProperty<AudioBookInfo> bookInfo = new SimpleObjectProperty<>();
    private final SimpleObjectProperty<Book> book = new SimpleObjectProperty<>();
    private final ObservableList<MediaInfo> media = FXCollections.observableArrayList();
    private final ObservableList<ArtWork> posters = FXCollections.observableArrayList();
    private final SimpleObjectProperty<OutputParameters> outputParameters = new SimpleObjectProperty<>(Preset.DEFAULT_OUTPUT_PARAMETERS);
    private final SimpleObjectProperty<Double> speed = new SimpleObjectProperty<>(1.0);
*/

    private final static ExecutorService executorService = Executors.newCachedThreadPool();
    private DelegatedObservableList<ArtWork> uiPosters;


    public ConversionContext() {
        resetForNewConversion();
    }


    public void saveGenres() {
        genresManager.saveGenres(conversionGroupHolder.get().getBookInfo());
    }

    public ObservableList<String> loadGenres() {
        return genresManager.loadGenres();
    }

    public void resetForNewConversion() {
        saveGenres();
        ConversionGroup newConversionGroup = new ConversionGroup();
        conversionGroupHolder.set(newConversionGroup);
        if (uiPosters != null) {
            uiPosters.setDelegate(newConversionGroup.getPosters());
        } else {
            uiPosters = new DelegatedObservableList<>(newConversionGroup.getPosters());
        }
//        newConversionGroup.setBookInfo(AudioBookInfo.instance());
/*
        bookInfo.set(AudioBookInfo.instance());
        book.set(null);
        posters.clear();
        media.clear();
*/
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

    public ConversionGroup next() {
        return conversionGroupHolder.get();
    }

    public void addJob(ConversionJob conversionJob) {
        conversionQueue.add(conversionJob);
        executorService.execute(conversionJob);
    }


    public ObservableList<ArtWork> getPosters() {
        if (uiPosters == null) {
            uiPosters = new DelegatedObservableList<>(next().getPosters());
        }
        return uiPosters;
    }
}
