package uk.yermak.audiobookconverter.fx;

import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.scene.layout.HBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.util.Duration;
import uk.yermak.audiobookconverter.ConversionContext;
import uk.yermak.audiobookconverter.MediaInfo;

import java.io.File;
import java.util.List;

/**
 * Created by yermak on 26-Oct-18.
 */
public class MediaPlayerController {
    @FXML
    public Button playButton;
    @FXML
    public Slider timelapse;
    @FXML
    public Slider volume;

    private static MediaPlayer mediaPlayer;
    private MediaInfo playingTrack = null;

    @FXML
    public void initialize() {

        timelapse.valueProperty().addListener(o -> {
            if (mediaPlayer != null) {
                mediaPlayer.seek(new Duration(timelapse.getValue()));
            }
        });

        ConversionContext context = ConverterApplication.getContext();

        ObservableList<MediaInfo> selectedMedia = context.getSelectedMedia();
        selectedMedia.addListener((InvalidationListener) observable -> {
            updateUI(selectedMedia.isEmpty() && mediaPlayer == null);
        });

    }

    private void updateUI(boolean disable) {
        playButton.setDisable(disable);
        timelapse.setDisable(disable);
        volume.setDisable(disable);
    }

    public void play(ActionEvent event) {
        ConversionContext context = ConverterApplication.getContext();

        List<MediaInfo> selectedMedia = context.getSelectedMedia();
        if (selectedMedia.size() == 1) {

            if (mediaPlayer == null) {
                playMedias(selectedMedia.get(0));
                return;
            }
            if (playingTrack != selectedMedia.get(0)) {
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
            mediaPlayer.seek(new Duration(timelapse.getValue() * 1000));
            mediaPlayer.play();
        } else {
            mediaPlayer.pause();
        }
    }

    private void playMedias(MediaInfo selected) {
        playingTrack = selected;
        ConversionContext context = ConverterApplication.getContext();

        ObservableList<MediaInfo> media = context.getConversion().getMedia();
        if (media.indexOf(selected) > media.size() - 1) return;
        timelapse.setValue(0);

        Media m = new Media(new File(selected.getFileName()).toURI().toASCIIString());
        mediaPlayer = new MediaPlayer(m);

        MediaView mediaView = new MediaView(mediaPlayer);
        HBox box = (HBox) playButton.getParent();
        box.getChildren().add(mediaView);


        mediaPlayer.setOnReady(() -> {
            updateValues();
            timelapse.setMax(mediaPlayer.getMedia().getDuration().toSeconds());
        });

        mediaPlayer.volumeProperty().bindBidirectional(volume.valueProperty());
        mediaPlayer.volumeProperty().set(0.2);

        mediaPlayer.currentTimeProperty().addListener(observable -> updateValues());


        timelapse.valueProperty().addListener(observable -> {
            if (timelapse.isValueChanging()) {
                Duration duration = mediaPlayer.getMedia().getDuration();
                mediaPlayer.seek(duration.multiply(timelapse.getValue() / 100.0));
            }
        });

        mediaPlayer.setOnEndOfMedia(() -> {
            mediaPlayer.volumeProperty().unbindBidirectional(volume.valueProperty());
            mediaPlayer.dispose();
            mediaPlayer = null;
            playMedias(findNext(selected));
        });
        toggleMediaPlayer();

    }

    private MediaInfo findNext(MediaInfo selected) {
        ObservableList<MediaInfo> media = ConverterApplication.getContext().getConversion().getMedia();
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
            }
        });
    }


}
