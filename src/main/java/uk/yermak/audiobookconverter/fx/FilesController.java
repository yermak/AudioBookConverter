package uk.yermak.audiobookconverter.fx;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
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
import uk.yermak.audiobookconverter.ConversionContext;
import uk.yermak.audiobookconverter.MediaInfo;
import uk.yermak.audiobookconverter.MediaLoader;
import uk.yermak.audiobookconverter.ProgressStatus;

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
                    updateUI(true);
                    break;
                default:
                    updateUI(false);
            }
        });

        media.addListener(new MediaInfoListChangeListener());
        fileList.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<MediaInfo>() {
            @Override
            public void changed(ObservableValue<? extends MediaInfo> observable, MediaInfo oldValue, MediaInfo newValue) {
                ObservableList<Integer> selectedIndices = fileList.getSelectionModel().getSelectedIndices();
                boolean disableMovement = selectedIndices.size() != 1;
                upButton.setDisable(disableMovement);
                downButton.setDisable(disableMovement);
            }
        });
    }

    private void updateUI(boolean disable) {
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
}
