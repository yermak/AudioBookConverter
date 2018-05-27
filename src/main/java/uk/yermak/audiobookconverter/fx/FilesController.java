package uk.yermak.audiobookconverter.fx;

import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Side;
import javafx.scene.control.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import org.apache.commons.io.FileUtils;
import uk.yermak.audiobookconverter.*;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by Yermak on 04-Feb-18.
 */
public class FilesController {

    @FXML
    public Button addButton;
    @FXML
    public Button removeButton;
    @FXML
    public Button clearButton;
    @FXML
    public Button upButton;
    @FXML
    public Button downButton;
    @FXML
    ListView<MediaInfo> fileList;


    @FXML
    public Button startButton;
    @FXML
    public Button pauseButton;
    @FXML
    public Button stopButton;
    @FXML
//    public ProgressComponent progressBar;

    private final ContextMenu contextMenu = new ContextMenu();

    @FXML
    public void initialize() {
        ConversionContext context = ConverterApplication.getContext();


        MenuItem item1 = new MenuItem("Files");
        item1.setOnAction(e -> selectFilesDialog(ConverterApplication.getEnv().getWindow()));
        MenuItem item2 = new MenuItem("Folder");
        item2.setOnAction(e -> selectFolderDialog(ConverterApplication.getEnv().getWindow()));
        contextMenu.getItems().addAll(item1, item2);

        ObservableList<MediaInfo> media = context.getConversion().getMedia();
        fileList.setItems(media);
        fileList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        context.getConversion().addStatusChangeListener((observable, oldValue, newValue) -> {
            switch (newValue) {
                case IN_PROGRESS:
                case STARTED:
                    updateFilesUI(true);
                    break;
                default:
                    updateFilesUI(false);
            }
        });

        media.addListener(new MediaInfoListChangeListener());
        fileList.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            ObservableList<Integer> selectedIndices = fileList.getSelectionModel().getSelectedIndices();
            boolean disableMovement = selectedIndices.size() != 1;
            upButton.setDisable(disableMovement);
            downButton.setDisable(disableMovement);
        });

        context.getConversion().addMediaChangeListener(c -> updateProcessUI(c.getList().isEmpty()));
    }

    private void updateFilesUI(boolean disable) {
        addButton.setDisable(disable);
        removeButton.setDisable(disable);
        clearButton.setDisable(disable);
        upButton.setDisable(disable);
        downButton.setDisable(disable);
    }

    @FXML
    protected void addFiles(ActionEvent event) {
        Button node = (Button) event.getSource();
        contextMenu.show(node, Side.RIGHT, 0, 0);
    }

    private void selectFolderDialog(Window window) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select folder with MP3 files for conversion");
        File selectedDirectory = directoryChooser.showDialog(window);
        if (selectedDirectory != null) {
            Collection<File> files = FileUtils.listFiles(selectedDirectory, new String[]{"mp3"}, true);
            processFiles(files);
        }
    }

    private void processFiles(Collection<File> files) {

        List<String> fileNames = new ArrayList<>();
        files.forEach(f -> fileNames.add(f.getPath()));
        List<MediaInfo> addedMedia = new MediaLoader(fileNames).loadMediaInfo();

        fileList.getItems().addAll(addedMedia);
    }

    private void selectFilesDialog(Window window) {
        final FileChooser fileChooser = new FileChooser();
//        fileChooser.setInitialDirectory();
        fileChooser.setTitle("Select MP3 files for conversion");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("mp3", "*.mp3")
        );
        List<File> files = fileChooser.showOpenMultipleDialog(window);
        if (files != null) {
            processFiles(files);
        }
    }

    public void removeFiles(ActionEvent event) {
        ObservableList<MediaInfo> selected = fileList.getSelectionModel().getSelectedItems();
        fileList.getItems().removeAll(selected);
    }

    public void clear(ActionEvent event) {
        fileList.getItems().clear();
    }

    public void moveUp(ActionEvent event) {
        ObservableList<Integer> selectedIndices = fileList.getSelectionModel().getSelectedIndices();
        if (selectedIndices.size() == 1) {
            ObservableList<MediaInfo> items = fileList.getItems();
            int selected = selectedIndices.get(0);
            if (selected > 0) {
                MediaInfo upper = items.get(selected - 1);
                MediaInfo lower = items.get(selected);
                items.set(selected - 1, lower);
                items.set(selected, upper);
                fileList.getSelectionModel().clearAndSelect(selected - 1);
            }
        }
    }

    public void moveDown(ActionEvent event) {
        ObservableList<Integer> selectedIndices = fileList.getSelectionModel().getSelectedIndices();
        if (selectedIndices.size() == 1) {
            ObservableList<MediaInfo> items = fileList.getItems();
            int selected = selectedIndices.get(0);
            if (selected < items.size() - 1) {
                MediaInfo lower = items.get(selected + 1);
                MediaInfo upper = items.get(selected);
                items.set(selected, lower);
                items.set(selected + 1, upper);
                fileList.getSelectionModel().clearAndSelect(selected + 1);
            }
        }

    }

    private class MediaInfoListChangeListener implements ListChangeListener<MediaInfo> {
        @Override
        public void onChanged(Change<? extends MediaInfo> c) {
            boolean disable = c.getList().isEmpty();
            removeButton.setDisable(disable);
            clearButton.setDisable(disable);
            upButton.setDisable(disable);
            downButton.setDisable(disable);
        }
    }

    private void updateProcessUI(boolean disable) {
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
                updateProcessUI(true);
                long totalDuration = media.stream().mapToLong(MediaInfo::getDuration).sum();
                ConversionProgress conversionProgress = new ConversionProgress(media.size(), totalDuration);


                conversionProgress.state.addListener((observable, oldValue, newValue) -> {
                    if (newValue.equals(ProgressStatus.FINISHED) || newValue.equals(ProgressStatus.CANCELLED)) {
                        updateProcessUI(false);
                        updateFilesUI(false);
                    }
                });

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
        updateProcessUI(false);
        ConverterApplication.getContext().stopConversion();
    }


}
