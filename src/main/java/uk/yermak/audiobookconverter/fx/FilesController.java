package uk.yermak.audiobookconverter.fx;

import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import javafx.util.Callback;
import org.apache.commons.io.FileUtils;
import org.controlsfx.control.Notifications;
import org.controlsfx.control.PopOver;
import uk.yermak.audiobookconverter.*;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by Yermak on 04-Feb-18.
 */
public class FilesController implements ConversionSubscriber {

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
    TreeView<MediaInfo> chapters;


    @FXML
    public Button startButton;
    @FXML
    public Button pauseButton;
    @FXML
    public Button stopButton;

    private Conversion conversion;
    private ObservableList<MediaInfo> selectedMedia;
    private MediaInfoChangeListener listener;

    @FXML
    public void initialize() {


//        fileList.setCellFactory(new ListViewListCellCallback());
        MenuItem item1 = new MenuItem("Files");
        item1.setOnAction(e -> selectFilesDialog(ConverterApplication.getEnv().getWindow()));
        MenuItem item2 = new MenuItem("Folder");
        item2.setOnAction(e -> selectFolderDialog(ConverterApplication.getEnv().getWindow()));
        contextMenu.getItems().addAll(item1, item2);

        ConversionContext context = ConverterApplication.getContext();
        selectedMedia = context.getSelectedMedia();

        resetForNewConversion(context.registerForConversion(this));

        selectedMedia.addListener((InvalidationListener) observable -> {
            if (selectedMedia.isEmpty()) return;
            List<MediaInfo> change = new ArrayList<>(selectedMedia);
            List<MediaInfo> selection = new ArrayList<>(fileList.getSelectionModel().getSelectedItems());
            if (!change.containsAll(selection) || !selection.containsAll(change)) {
                fileList.getSelectionModel().clearSelection();
                change.forEach(m -> fileList.getSelectionModel().select(this.conversion.getMedia().indexOf(m)));
            }
        });
    }

    private final ContextMenu contextMenu = new ContextMenu();


    @FXML
    protected void addFiles(ActionEvent event) {
        Button node = (Button) event.getSource();
        contextMenu.show(node, Side.RIGHT, 0, 0);
    }

    private void selectFolderDialog(Window window) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        String sourceFolder = AppProperties.getProperty("source.folder");
        directoryChooser.setInitialDirectory(Utils.getInitialDirecotory(sourceFolder));

        directoryChooser.setTitle("Select folder with MP3/WMA files for conversion");
        File selectedDirectory = directoryChooser.showDialog(window);
        if (selectedDirectory != null) {
            Collection<File> files = FileUtils.listFiles(selectedDirectory, new String[]{"mp3", "wma"}, true);
            processFiles(files);
            AppProperties.setProperty("source.folder", selectedDirectory.getAbsolutePath());
        }
    }

    private void processFiles(Collection<File> files) {

        List<String> fileNames = new ArrayList<>();
        files.forEach(f -> fileNames.add(f.getPath()));
        List<MediaInfo> addedMedia = createMediaLoader(fileNames).loadMediaInfo();

        fileList.getItems().addAll(addedMedia);
    }

    private MediaLoader createMediaLoader(List<String> fileNames) {
        return new FXMediaLoader(fileNames, conversion);
    }

    private void selectFilesDialog(Window window) {
        final FileChooser fileChooser = new FileChooser();
        String sourceFolder = AppProperties.getProperty("source.folder");
        fileChooser.setInitialDirectory(Utils.getInitialDirecotory(sourceFolder));
        fileChooser.setTitle("Select MP3/WMA files for conversion");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("mp3", "*.mp3"),
                new FileChooser.ExtensionFilter("wma", "*.wma")
        );
        List<File> files = fileChooser.showOpenMultipleDialog(window);
        if (files != null) {
            processFiles(files);
            File firstFile = files.get(0);
            File parentFile = firstFile.getParentFile();
            AppProperties.setProperty("source.folder", parentFile.getAbsolutePath());
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


        List<MediaInfo> media = conversion.getMedia();
        if (media.size() > 0) {
            AudioBookInfo audioBookInfo = conversion.getBookInfo();
            MediaInfo mediaInfo = media.get(0);
            String outputDestination = null;
            if (conversion.getMode().equals(ConversionMode.BATCH)) {
                outputDestination = selectOutputDirectory();
            } else {
                outputDestination = selectOutputFile(audioBookInfo, mediaInfo);
            }
            if (outputDestination != null) {
                String finalName = new File(outputDestination).getName();
                conversion.addStatusChangeListener((observable, oldValue, newValue) -> {
                    if (ProgressStatus.FINISHED.equals(newValue)) {
                        Platform.runLater(() -> showNotification(finalName));
                    }
                });

                long totalDuration = media.stream().mapToLong(MediaInfo::getDuration).sum();
                ConversionProgress conversionProgress = new ConversionProgress(conversion, media.size(), totalDuration, finalName);
                context.startConversion(outputDestination, conversionProgress);

            }
        }
    }

    private static void showNotification(String finalOutputDestination) {
        Notifications.create()
                .title("AudioBookConverter: Conversion is completed")
                .text(finalOutputDestination).show();
    }

    private String selectOutputDirectory() {
        JfxEnv env = ConverterApplication.getEnv();

        String outputDestination;
        DirectoryChooser directoryChooser = new DirectoryChooser();
        String outputFolder = AppProperties.getProperty("output.folder");
        directoryChooser.setInitialDirectory(Utils.getInitialDirecotory(outputFolder));
        directoryChooser.setTitle("Select destination folder for encoded files");
        File selectedDirectory = directoryChooser.showDialog(env.getWindow());
        AppProperties.setProperty("output.folder", selectedDirectory.getAbsolutePath());
        outputDestination = selectedDirectory.getPath();
        return outputDestination;
    }

    private String selectOutputFile(AudioBookInfo audioBookInfo, MediaInfo mediaInfo) {
        JfxEnv env = ConverterApplication.getEnv();

        final FileChooser fileChooser = new FileChooser();
        String outputFolder = AppProperties.getProperty("output.folder");
        fileChooser.setInitialDirectory(Utils.getInitialDirecotory(outputFolder));
        fileChooser.setInitialFileName(Utils.getOuputFilenameSuggestion(mediaInfo.getFileName(), audioBookInfo));
        fileChooser.setTitle("Save AudioBook");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("m4b", "*.m4b")
        );
        File file = fileChooser.showSaveDialog(env.getWindow());
        if (file == null) return null;
        File parentFolder = file.getParentFile();
        AppProperties.setProperty("output.folder", parentFolder.getAbsolutePath());
        return file.getPath();
    }


    public void pause(ActionEvent actionEvent) {
        ConversionContext context = ConverterApplication.getContext();
        if (context.isPaused()) {
            context.resumeConversions();
            pauseButton.setText("Pause all");
        } else {
            context.pauseConversions();
            pauseButton.setText("Resume all");
        }
    }

    public void stop(ActionEvent actionEvent) {
        ConverterApplication.getContext().stopConversions();
    }


    private void updateUI(ProgressStatus status, Boolean listEmpty, ObservableList<Integer> selectedIndices) {
        Platform.runLater(() -> {
            switch (status) {
                case PAUSED:
                case FINISHED:
                case CANCELLED:
                case READY:
                    addButton.setDisable(false);
                    clearButton.setDisable(listEmpty);

                    upButton.setDisable(selectedIndices.size() != 1 || selectedIndices.get(0) == 0);
                    downButton.setDisable(selectedIndices.size() != 1 || selectedIndices.get(0) == fileList.getItems().size() - 1);
                    removeButton.setDisable(selectedIndices.size() < 1);

                    startButton.setDisable(listEmpty);
                    break;
                case IN_PROGRESS:
                    addButton.setDisable(true);
                    removeButton.setDisable(true);
                    clearButton.setDisable(true);
                    upButton.setDisable(true);
                    downButton.setDisable(true);
                    startButton.setDisable(true);
                    break;
                default: {
                }
            }

        });
    }

    @Override
    public void resetForNewConversion(Conversion conversion) {
        this.conversion = conversion;

        ObservableList<MediaInfo> media = this.conversion.getMedia();
        fileList.setItems(media);
        fileList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
/* TODO fix buttons behaviour
        conversion.addStatusChangeListener((observable, oldValue, newValue) ->
                updateUI(newValue, media.isEmpty(), fileList.getSelectionModel().getSelectedIndices())
        );
*/

//        media.addListener((ListChangeListener<MediaInfo>) c -> updateUI(this.conversion.getStatus(), c.getList().isEmpty(), fileList.getSelectionModel().getSelectedIndices()));

        if (listener != null) {
            fileList.getSelectionModel().selectedItemProperty().removeListener(listener);
        }
        listener = new MediaInfoChangeListener(conversion);
        fileList.getSelectionModel().selectedItemProperty().addListener(listener);
    }

    private static class ListViewListCellCallback implements Callback<ListView<MediaInfo>, ListCell<MediaInfo>> {
        @Override
        public ListCell<MediaInfo> call(ListView<MediaInfo> param) {
            ListCell<MediaInfo> mediaCell = new DefaultListCell<>();
            mediaCell.setOnContextMenuRequested(new EventHandler<ContextMenuEvent>() {
                @Override
                public void handle(ContextMenuEvent event) {
                    GridPane content = new GridPane();
                    content.setHgap(5);
                    content.setVgap(5);
                    content.setPadding(new Insets(10, 10, 10, 10));
                    content.setPrefWidth(200);
                    content.setPrefHeight(200);
                    content.add(new Label("11111111111"), 0, 0);
                    content.add(new Label("222222222222222"), 1, 0);
                    PopOver editor = new PopOver(content);
//                    editor.setWidth(200);
//                    editor.setHeight(200);
                    editor.setArrowLocation(PopOver.ArrowLocation.RIGHT_TOP);
                    editor.setTitle(mediaCell.getItem().getBookInfo().getTitle());
                    editor.setHeaderAlwaysVisible(true);
                    editor.setDetachable(false);
                    editor.show(mediaCell);
                }

            });
            return mediaCell;
        }
    }

    static class DefaultListCell<T> extends ListCell<T> {
        @Override
        public void updateItem(T item, boolean empty) {
            super.updateItem(item, empty);

            if (empty) {
                setText(null);
                setGraphic(null);
            } else if (item instanceof Node) {
                setText(null);
                Node currentNode = getGraphic();
                Node newNode = (Node) item;
                if (currentNode == null || !currentNode.equals(newNode)) {
                    setGraphic(newNode);
                }
            } else {
                setText(item == null ? "null" : item.toString());
                setGraphic(null);
            }
        }
    }

    public void play(ActionEvent actionEvent) {

        ObservableList<Integer> selectedIndices = fileList.getSelectionModel().getSelectedIndices();

        if (selectedIndices.size() == 1) {
            ObservableList<MediaInfo> items = fileList.getItems();


            String fileName = items.get(selectedIndices.get(0)).getFileName();
            String source = new File(fileName).toURI().toString();

            Media hit = new Media(source);
            MediaPlayer mediaPlayer = new MediaPlayer(hit);
            mediaPlayer.setOnError(() -> System.out.println("Error : " + mediaPlayer.getError().toString()));
            mediaPlayer.play();
            MediaPlayer.Status status = mediaPlayer.getStatus();
            System.out.println("status = " + status);


        }

    }

    private class MediaInfoChangeListener implements ChangeListener<MediaInfo> {
        private Conversion conversion;

        public MediaInfoChangeListener(Conversion conversion) {
            this.conversion = conversion;
        }

        @Override
        public void changed(ObservableValue<? extends MediaInfo> observable, MediaInfo oldValue, MediaInfo newValue) {
//            updateUI(conversion.getStatus(), conversion.getMedia().isEmpty(), fileList.getSelectionModel().getSelectedIndices());
            selectedMedia.clear();
            fileList.getSelectionModel().getSelectedIndices().forEach(i -> selectedMedia.add(conversion.getMedia().get(i)));
        }
    }
}


