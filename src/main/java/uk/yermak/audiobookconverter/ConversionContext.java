package uk.yermak.audiobookconverter;

import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.yermak.audiobookconverter.fx.ConversionProgress;
import uk.yermak.audiobookconverter.fx.ConverterApplication;

import java.lang.invoke.MethodHandles;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * Created by yermak on 06-Feb-18.
 */
public class ConversionContext {

    final static Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private LinkedList<Conversion> conversionQueue = new LinkedList<>();
    private SimpleObjectProperty<Conversion> conversionHolder = new SimpleObjectProperty<>(new Conversion());
    private boolean paused;

    private Subscriber subscriber;
    private ObservableList<MediaInfo> selectedMedia = FXCollections.observableArrayList();
    private ObservableList<String> genres = FXCollections.observableArrayList();

    private AudioBookInfo bookInfo;
    private Book book;
    private ObservableList<MediaInfo> media;
    private ObservableList<ArtWork> posters;


    private OutputParameters outputParameters = new OutputParameters();
    private SimpleObjectProperty<ConversionMode> mode = new SimpleObjectProperty<>(ConversionMode.PARALLEL);


    public ConversionContext() {
        conversionQueue.add(conversionHolder.get());
        reloadGenres();
        resetForNewConversion();
    }

    public void saveGenres() {
        if (StringUtils.isNotEmpty(bookInfo.getGenre()) & genres.stream().noneMatch(s -> s.equals(bookInfo.getGenre()))) {
            genres.add(bookInfo.getGenre());
        }
        String collect = genres.stream().collect(Collectors.joining("::"));
        AppProperties.setProperty("genres", collect);
    }

    private void reloadGenres() {
        genres.clear();
        String genresProperty = AppProperties.getProperty("genres");
        if (genresProperty != null) {
            String[] genres = genresProperty.split("::");
            this.genres.addAll(Arrays.stream(genres).sorted().collect(Collectors.toList()));
        }
    }

    public void setMode(ConversionMode mode) {
        this.mode.set(mode);
    }

    public ConversionMode getMode() {
        return mode.get();
    }


    public void startConversion(Part part, String output, ConversionProgress conversionProgress) {
        subscriber.addConversionProgress(conversionProgress);

        conversionHolder.get().addStatusChangeListener((observable, oldValue, newValue) -> {
            if (ProgressStatus.FINISHED.equals(newValue)) {
                Platform.runLater(() -> ConverterApplication.showNotification(output));
            }
        });
        conversionHolder.get().start(part, output, conversionProgress, outputParameters.copy());

        Conversion newConversion = new Conversion();
        conversionQueue.add(newConversion);
        conversionHolder.set(newConversion);

        saveGenres();
        resetForNewConversion();
    }

    private void resetForNewConversion() {
        reloadGenres();
        bookInfo = new AudioBookInfo();
        posters = FXCollections.observableArrayList();
        media = FXCollections.observableArrayList();
    }

    public OutputParameters getOutputParameters() {
        return outputParameters;
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

    public void setMedia(ObservableList<MediaInfo> media) {
        this.media = media;
    }

    public void stopConversions() {
        conversionQueue.forEach(Conversion::stop);
    }

    public void subscribeForStart(Subscriber subscriber) {
        this.subscriber = subscriber;
    }

    public ObservableList<MediaInfo> getSelectedMedia() {
        return selectedMedia;
    }

    public Conversion registerForConversion(ConversionSubscriber conversionSubscriber) {
        conversionHolder.addListener((observable, oldValue, newValue) -> conversionSubscriber.resetForNewConversion(newValue));
        return conversionHolder.get();
    }

    public void pauseConversions() {
        conversionQueue.forEach(Conversion::pause);
        paused = true;
    }

    public void resumeConversions() {
        conversionQueue.forEach(Conversion::resume);
        paused = false;
    }

    public boolean isPaused() {
        return paused;
    }

    public void addModeChangeListener(ChangeListener<ConversionMode> listener) {
        mode.addListener(listener);
    }


    public void setBookInfo(AudioBookInfo bookInfo) {
        this.bookInfo = bookInfo;
    }

    public AudioBookInfo getBookInfo() {
        return bookInfo;
    }

    public ObservableList<ArtWork> getPosters() {
        return posters;
    }


    public Conversion getPlannedConversion() {
        return conversionHolder.get();
    }
}
