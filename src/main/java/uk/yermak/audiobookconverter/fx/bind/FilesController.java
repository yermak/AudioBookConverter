package uk.yermak.audiobookconverter.fx.bind;

import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.GridPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import javafx.util.Callback;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.controlsfx.control.Notifications;
import org.controlsfx.control.PopOver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.yermak.audiobookconverter.*;
import uk.yermak.audiobookconverter.fx.ConversionProgress;
import uk.yermak.audiobookconverter.fx.ConverterApplication;
import uk.yermak.audiobookconverter.fx.JfxEnv;

import java.io.File;
import java.lang.invoke.MethodHandles;
import java.util.*;

/**
 * Created by Yermak on 04-Feb-18.
 */
public class FilesController {
    final static Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private FilesDelegate delegate = new FilesDelegate(this);
    @FXML
    Button addButton;
    @FXML
    Button removeButton;
    @FXML
    Button clearButton;
    @FXML
    Button upButton;
    @FXML
    Button downButton;

    @FXML
    ListView<MediaInfo> fileList;
    TreeView<MediaInfo> chapters;


    @FXML
    Button startButton;
    @FXML
    Button pauseButton;
    @FXML
    Button stopButton;

    final ContextMenu contextMenu = new ContextMenu();

    Conversion conversion;
    ObservableList<MediaInfo> selectedMedia;
    MediaInfoChangeListener listener;

    private static final String M4B = "m4b";
    private final static String[] FILE_EXTENSIONS = new String[]{"mp3", "m4a", M4B, "wma"};

    @FXML
    private void initialize() {
        delegate.initialize();
    }

    @FXML
    protected void addFiles(ActionEvent event) {
        delegate.addFiles(event);
    }

    @FXML
    private void removeFiles(ActionEvent event) {
        delegate.removeFiles(event);
    }

    @FXML
    private void clear(ActionEvent event) {
        delegate.clear(event);
    }

    @FXML
    private void moveUp(ActionEvent event) {
        delegate.moveUp(event);
    }

    @FXML
    private void moveDown(ActionEvent event) {
        delegate.moveDown(event);
    }

    @FXML
    private void start(ActionEvent actionEvent) {
       delegate.start(actionEvent);
    }

    @FXML
    private void pause(ActionEvent actionEvent) {
        delegate.pause(actionEvent);
    }

    @FXML
    private void stop(ActionEvent actionEvent) {
        delegate.stop(actionEvent);
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

    class MediaInfoChangeListener implements ChangeListener<MediaInfo> {
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