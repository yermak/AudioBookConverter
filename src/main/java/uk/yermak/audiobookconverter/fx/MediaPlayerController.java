package uk.yermak.audiobookconverter.fx;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.Screen;
import javafx.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.yermak.audiobookconverter.AudiobookConverter;
import uk.yermak.audiobookconverter.Utils;
import uk.yermak.audiobookconverter.book.MediaInfo;

import java.io.File;
import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by yermak on 26-Oct-18.
 */
public class MediaPlayerController extends GridPane {
    final static Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final Button playButton;
    private final Slider timelapse;
    private final Slider volume;
    private final Label playTime;
    private final Label totalTime;

    private static MediaPlayer mediaPlayer;
    private MediaInfo playingTrack = null;
    private ScheduledExecutorService executorService;

    public MediaPlayerController() {
        ResourceBundle bundle = AudiobookConverter.getBundle();

        setHgap(10);
        setPadding(new Insets(0, 10, 0, 10));

        ColumnConstraints fixed = new ColumnConstraints();
        ColumnConstraints fixed2 = new ColumnConstraints();
        ColumnConstraints grow = new ColumnConstraints();
        grow.setHgrow(javafx.scene.layout.Priority.ALWAYS);
        ColumnConstraints fixed3 = new ColumnConstraints();
        ColumnConstraints fixed4 = new ColumnConstraints();
        ColumnConstraints fixed5 = new ColumnConstraints();
        getColumnConstraints().addAll(fixed, fixed2, grow, fixed3, fixed4, fixed5);

        playButton = new Button("▶⏸");
        playButton.setStyle("-fx-font-size: 15px;");
        playButton.setOnAction(this::play);
        playButton.setTooltip(new Tooltip(bundle.getString("mediaplayer.tooltip.playpause")));
        add(playButton, 0, 0, 1, 2);

        double minWidth = Screen.getPrimary().getVisualBounds().getWidth() * 0.35;
        timelapse = new Slider();
        timelapse.setMinorTickCount(4);
        timelapse.setMajorTickUnit(1);
        timelapse.setMinWidth(minWidth);
        timelapse.setTooltip(new Tooltip(bundle.getString("mediaplayer.tooltip.timeslider")));
        setValignment(timelapse, VPos.BOTTOM);
        add(timelapse, 1, 0, 3, 1);

        Label volumeSign = new Label("🔊");
        volumeSign.setStyle("-fx-font-size: 24px;");
        setHalignment(volumeSign, HPos.RIGHT);
        add(volumeSign, 4, 0, 1, 2);

        volume = new Slider();
        volume.setOrientation(javafx.geometry.Orientation.VERTICAL);
        volume.setMaxHeight(24);
        volume.setMin(0);
        volume.setMax(1.0);
        volume.setValue(1.0);
        volume.setTooltip(new Tooltip(bundle.getString("mediaplayer.tooltip.volumeslider")));
        add(volume, 5, 0, 1, 2);

        playTime = new Label("00:00:00");
        setHalignment(playTime, HPos.LEFT);
        playTime.setTooltip(new Tooltip(bundle.getString("mediaplayer.tooltip.playtime")));
        add(playTime, 1, 1);

        totalTime = new Label("00:00:00");
        setHalignment(totalTime, HPos.RIGHT);
        totalTime.setTooltip(new Tooltip(bundle.getString("mediaplayer.tooltip.totaltime")));
        add(totalTime, 3, 1);
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

        mediaPlayer.rateProperty().set(context.getSpeed());
        mediaPlayer.rateProperty().bind(context.getSpeedObservable());

        timelapse.valueProperty().addListener(observable -> {
            if (timelapse.isValueChanging()) {
                boolean wasPlaying = false;
                if (mediaPlayer.getStatus() == MediaPlayer.Status.PLAYING) {
                    wasPlaying = true;
                    mediaPlayer.pause();
                }
                playTime.setText(Utils.formatTime(timelapse.getValue() * 1000));
                mediaPlayer.seek(Duration.seconds(timelapse.getValue()));
                if (wasPlaying) {
                    mediaPlayer.play();
                }
            }
        });

        mediaPlayer.setOnEndOfMedia(() -> {
            executorService.shutdown();
            mediaPlayer.volumeProperty().unbindBidirectional(volume.valueProperty());
            mediaPlayer.dispose();
            mediaPlayer
                    = null;
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
        if (i >= 0 && i < media.size() - 1) {
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
