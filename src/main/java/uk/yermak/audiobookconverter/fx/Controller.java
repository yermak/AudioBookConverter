package uk.yermak.audiobookconverter.fx;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.stage.FileChooser;
import uk.yermak.audiobookconverter.*;

import java.util.List;

/**
 * Created by Yermak on 06-Jan-18.
 */
public class Controller {

    @FXML
    public void initialize() {

    }

    public void start(ActionEvent actionEvent) {
        ConversionContext context = ConverterApplication.getContext();
        JfxEnv env = ConverterApplication.getEnv();


        List<MediaInfo> media = context.getMedia();
        if (media.size() > 0) {
            AudioBookInfo audioBookInfo = context.getBookInfo();
            MediaInfo mediaInfo = media.get(0);
            String outputDestination = null;
            boolean selected = false;
            if (context.getMode().equals(ConversionMode.BATCH)) {
             /*   BatchModeOptionsDialog options = new BatchModeOptionsDialog(ConverterApplication.getEnv().getWindow());
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

                final FileChooser fileChooser = new FileChooser();
                fileChooser.setInitialFileName(Utils.getOuputFilenameSuggestion(mediaInfo.getFileName(), audioBookInfo));
                fileChooser.setTitle("Save AudioBook");
                fileChooser.getExtensionFilters().add(
                        new FileChooser.ExtensionFilter("m4b", "*.m4b")
                );
                outputDestination = fileChooser.showSaveDialog(env.getWindow()).getName();
                selected = outputDestination != null;
            }

            if (selected) {
//                ProgressView progressView = createProgressView();
//                JobProgress jobProgress = new JobProgress(conversionStrategy, progressView, media);

//                this.setUIEnabled(false);

                context.startConversion(outputDestination);


//

            }
        }
    }
}
