package uk.yermak.audiobookconverter.fx;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.stage.FileChooser;
import uk.yermak.audiobookconverter.*;

import java.util.List;
import java.util.concurrent.Executors;

/**
 * Created by Yermak on 06-Jan-18.
 */
public class Controller {

    @FXML
    public ProgressComponent progressBar;
    @FXML
    public Button startButton;
    @FXML
    public Button pauseButton;
    @FXML
    public Button stopButton;

    @FXML
    public void initialize() {
        ConversionContext context = ConverterApplication.getContext();
        context.getConversion().addMediaChangeListener(c -> updateUI(c.getList().isEmpty()));

    }

    private void updateUI(boolean disable) {
        startButton.setDisable(disable);
        pauseButton.setDisable(!disable);
        stopButton.setDisable(!disable);
    }

    public void start(ActionEvent actionEvent) {
        ConversionContext context = ConverterApplication.getContext();
        JfxEnv env = ConverterApplication.getEnv();


        List<MediaInfo> media = context.getConversion().getMedia();
        if (media.size() > 0) {
            AudioBookInfo audioBookInfo = context.getBookInfo();
            MediaInfo mediaInfo = media.get(0);
            String outputDestination = null;
            boolean selected = false;
            if (context.getMode().equals(ConversionMode.BATCH)) {

              /*  BatchModeOptionsDialog options = new BatchModeOptionsDialog(ConverterApplication.getEnv().getWindow());
                String sameFolder = this.getSameFolder(mediaInfo.getFileName());
                options.setFolder(sameFolder);
                if (options.open()) {
                    if (options.isIntoSameFolder()) {
                        selected = true;
                    } else {
                        outputDestination = options.getFolder();
                        selected = true;
                    }
                }*/
            } else {

                outputDestination = selectOutputFile(env, audioBookInfo, mediaInfo);
                selected = outputDestination != null;
            }

            if (selected) {

                updateUI(true);

                long totalDuration = media.stream().mapToLong(MediaInfo::getDuration).sum();
                ConversionProgress conversionProgress = new ConversionProgress(media.size(), totalDuration);
                Executors.newSingleThreadExecutor().execute(conversionProgress);
                progressBar.setConversionProgress(conversionProgress);
                context.startConversion(outputDestination, conversionProgress);
            }
        }
    }

    private String selectOutputFile(JfxEnv env, AudioBookInfo audioBookInfo, MediaInfo mediaInfo) {
        String outputDestination;
        final FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialFileName(Utils.getOuputFilenameSuggestion(mediaInfo.getFileName(), audioBookInfo));
        fileChooser.setTitle("Save AudioBook");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("m4b", "*.m4b")
        );
        outputDestination = fileChooser.showSaveDialog(env.getWindow()).getPath();
        return outputDestination;
    }


    public void pause(ActionEvent actionEvent) {
        ConverterApplication.getContext().pauseConversion();
    }

    public void stop(ActionEvent actionEvent) {
        updateUI(false);
        ConverterApplication.getContext().stopConversion();
    }
}
