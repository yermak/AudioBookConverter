package uk.yermak.audiobookconverter.fx;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.yermak.audiobookconverter.AudiobookConverter;
import uk.yermak.audiobookconverter.MediaInfo;
import uk.yermak.audiobookconverter.Utils;

import java.io.File;
import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by yermak on 26-Oct-18.
 */
public class MediaPlayerController  {
    final static Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @FXML
    private Button playButton;
    @FXML
    private Slider timelapse;
    @FXML
    private Slider volume;
    @FXML
    private Label playTime;
    @FXML
    private Label totalTime;

    private static MediaPlayer mediaPlayer;
    private MediaInfo playingTrack = null;
    private ScheduledExecutorService executorService;
//    private ObservableList<MediaInfo> media;

    @FXML
    public void initialize() {
//        media = context.getMedia();
//        ObservableList<MediaInfo> selectedMedia = context.getSelectedMedia();
//        selectedMedia.addListener((ListChangeListener<MediaInfo>) c -> disablePlayer(selectedMedia.isEmpty() && mediaPlayer == null));

    }

    private void disablePlayer(boolean disable) {
        playButton.setDisable(disable);
        timelapse.setDisable(disable);
        volume.setDisable(disable);
    }

    public void play(ActionEvent event) {
        ConversionContext context = AudiobookConverter.getContext();

        List<MediaInfo> selectedMedia = context.getSelectedMedia();
        if (selectedMedia.size() == 1) {

            if (mediaPlayer == null) {
                playMedias(selectedMedia.get(0));
                return;
            }
            if (playingTrack != selectedMedia.get(0)) {
                executorService.shutdown();
                mediaPlayer.stop();
                mediaPlayer.dispose();
                mediaPlayer = null;
                playingTrack = null;
                playMedias(selectedMedia.get(0));
                return;
            }
            toggleMediaPlayer();
        }

    }

    private void toggleMediaPlayer() {
        MediaPlayer.Status status = mediaPlayer.getStatus();
        if (status == MediaPlayer.Status.HALTED || status == MediaPlayer.Status.UNKNOWN) {
            return;
        }

        if (status == MediaPlayer.Status.READY
                || status == MediaPlayer.Status.PAUSED
                || status == MediaPlayer.Status.STOPPED) {
            mediaPlayer.play();
        } else {
            mediaPlayer.pause();
        }
    }

    private void playMedias(MediaInfo selected) {
        playingTrack = selected;
        ConversionContext context = AudiobookConverter.getContext();
        ObservableList<MediaInfo> media = context.getMedia();

        if (media.indexOf(selected) > media.size() - 1) return;
        timelapse.setValue(0);

        Media m = new Media(new File(selected.getFileName()).toURI().toASCIIString());
        mediaPlayer = new MediaPlayer(m);
        mediaPlayer.setAutoPlay(true);
        executorService = Executors.newSingleThreadScheduledExecutor();
        mediaPlayer.setOnReady(() -> {
            Duration duration = mediaPlayer.getMedia().getDuration();
            timelapse.setMax(duration.toSeconds());
            totalTime.setText(Utils.formatTime(duration.toMillis()));
            executorService.scheduleAtFixedRate(this::updateValues, 1, 1, TimeUnit.SECONDS);
        });

        mediaPlayer.volumeProperty().bindBidirectional(volume.valueProperty());
        mediaPlayer.volumeProperty().set(1.0);

        mediaPlayer.rateProperty().bind(context.getOutputParameters().getSpeedObservable());
        mediaPlayer.rateProperty().set(context.getOutputParameters().getSpeed());

        timelapse.valueProperty().addListener(observable -> {
            if (timelapse.isValueChanging()) {
                playTime.setText(Utils.formatTime(timelapse.getValue() * 1000));
                mediaPlayer.seek(Duration.seconds(timelapse.getValue()));
            }
        });

        mediaPlayer.setOnEndOfMedia(() -> {
            executorService.shutdown();
            mediaPlayer.volumeProperty().unbindBidirectional(volume.valueProperty());
            mediaPlayer.dispose();
            mediaPlayer = null;
            totalTime.setText("00:00:00");
            playTime.setText("00:00:00");

            MediaInfo next = findNext(selected);

            context.getSelectedMedia().clear();
            context.getSelectedMedia().add(next);

            playMedias(next);
        });
        toggleMediaPlayer();

    }

    private MediaInfo findNext(MediaInfo selected) {
        ConversionContext context = AudiobookConverter.getContext();
        ObservableList<MediaInfo> media = context.getMedia();

        int i = media.indexOf(selected);
        if (i < media.size()) {
            return media.get(i + 1);
        }
        return null;
    }

    private void updateValues() {
        Platform.runLater(() -> {
            Duration currentTime = mediaPlayer.getCurrentTime();
            if (!timelapse.isValueChanging()) {
                timelapse.setValue(currentTime.toSeconds());
                playTime.setText(Utils.formatTime(currentTime.toMillis()));
            }
        });
    }


}
