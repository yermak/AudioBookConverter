package uk.yermak.audiobookconverter.fx;

import javafx.application.Platform;
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

import static uk.yermak.audiobookconverter.ProgressStatus.PAUSED;

/**
 * Created by Yermak on 04-Feb-18.
 */
public class FilesController {

    private final ButtonStateMachineComposite buttonStateMachineComposite;
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

    private final ContextMenu contextMenu = new ContextMenu();

    public FilesController() {
        buttonStateMachineComposite = new ButtonStateMachineComposite();
    }

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

        context.getConversion().addStatusChangeListener((observable, oldValue, newValue) ->
                buttonStateMachineComposite.updateUI(newValue, media.isEmpty(), fileList.getSelectionModel().getSelectedIndices().size())
        );

        media.addListener((ListChangeListener<MediaInfo>) c -> buttonStateMachineComposite.updateUI(context.getConversion().getStatus(), c.getList().isEmpty(), fileList.getSelectionModel().getSelectedIndices().size()));

        fileList.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) ->
                buttonStateMachineComposite.updateUI(context.getConversion().getStatus(), media.isEmpty(), fileList.getSelectionModel().getSelectedIndices().size())
        );

       /* context.getConversion().addMediaChangeListener(c ->
                buttonStateMachineComposite.updateUI(null, null, c.getList().isEmpty(), null)
        );*/
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
                long totalDuration = media.stream().mapToLong(MediaInfo::getDuration).sum();
                ConversionProgress conversionProgress = new ConversionProgress(media.size(), totalDuration);

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
        File file = fileChooser.showSaveDialog(env.getWindow());
        if (file == null) return null;
        outputDestination = file.getPath();
        return outputDestination;
    }


    public void pause(ActionEvent actionEvent) {
        if (ConverterApplication.getContext().getConversion().getStatus().equals(PAUSED)) {
            ConverterApplication.getContext().resumeConversion();
        } else {
            ConverterApplication.getContext().pauseConversion();
        }
    }

    public void stop(ActionEvent actionEvent) {
        ConverterApplication.getContext().stopConversion();
    }

    class ButtonStateMachineComposite {
        void updateUI(ProgressStatus status, Boolean listEmpty, Integer selecteFiles) {

            Platform.runLater(() -> {
                switch (status) {
                    case PAUSED:
                        pauseButton.setText("Resume");
                        break;
                    case FINISHED:
                    case CANCELLED:
                    case READY:
                        pauseButton.setText("Pause");

                        addButton.setDisable(false);
//                        if (listEmpty != null) {
                        clearButton.setDisable(listEmpty);
                        startButton.setDisable(listEmpty);
//                        }
//                        if (selecteFiles != null) {
                        upButton.setDisable(selecteFiles != 1);
                        downButton.setDisable(selecteFiles != 1);
                        removeButton.setDisable(selecteFiles < 1);
//                        }
                        startButton.setDisable(listEmpty);
                        pauseButton.setDisable(true);
                        stopButton.setDisable(true);
                        break;
                    case IN_PROGRESS:
                        pauseButton.setText("Pause");
                        addButton.setDisable(true);
                        removeButton.setDisable(true);
                        clearButton.setDisable(true);
                        upButton.setDisable(true);
                        downButton.setDisable(true);
                        startButton.setDisable(true);
                        pauseButton.setDisable(false);
                        stopButton.setDisable(false);
                        break;
                    default: {
                    }
                }

            });
        }
    }
}


