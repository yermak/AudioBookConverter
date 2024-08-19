package uk.yermak.audiobookconverter.fx;

import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Side;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.MenuItem;
import javafx.scene.input.TransferMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.yermak.audiobookconverter.*;
import uk.yermak.audiobookconverter.book.Book;
import uk.yermak.audiobookconverter.book.Convertable;
import uk.yermak.audiobookconverter.book.MediaInfo;
import uk.yermak.audiobookconverter.book.Organisable;
import uk.yermak.audiobookconverter.loaders.FFMediaLoader;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.*;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * Created by Yermak on 04-Feb-18.
 */
public class FilesController {
    final static Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    public MenuItem removeMenu;


    @FXML
    private Button addButton;
    @FXML
    private Button clearButton;

    @FXML
    private Button importButton;

    @FXML
    private TabPane filesChapters;
    @FXML
    private Tab chaptersTab;
    @FXML
    private Tab filesTab;

    @FXML
    private Tab queueTab;


    @FXML
    private ListView<ProgressComponent> progressQueue;

    @FXML
    private TabPane tabs;


    @FXML
    private Button pauseButton;
    @FXML
    private Button stopButton;

    @FXML
    private FileListComponent fileList;

    @FXML
    BookStructureComponent bookStructure;

    @FXML
    private Button startButton;


    private final ContextMenu contextMenu = new ContextMenu();

    private final BooleanProperty chaptersMode = new SimpleBooleanProperty(false);


    //TODO move columns into BookStructureComponent
    @FXML
    private TreeTableColumn<Organisable, String> chapterColumn;
    @FXML
    private TreeTableColumn<Organisable, String> durationColumn;
    @FXML
    private TreeTableColumn<Organisable, String> detailsColumn;


    @FXML
    public void initialize() {
        addDragEvenHandlers(bookStructure);
        addDragEvenHandlers(fileList);
        addDragEvenHandlers(progressQueue);

        Settings settings = Settings.loadSetting();
        AudiobookConverter.getContext().setPresetName(settings.getPresets().get(settings.getLastUsedPreset()).getName());

        initFileOpenMenu();

        ConversionContext context = AudiobookConverter.getContext();
        ObservableList<MediaInfo> selectedMedia = context.getSelectedMedia();

        selectedMedia.addListener((InvalidationListener) observable -> {
            if (selectedMedia.isEmpty() || chaptersMode.get()) return;
            fileList.reselect();
        });

        filesChapters.getTabs().remove(filesTab);
        filesChapters.getTabs().remove(chaptersTab);


        bookStructure.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        bookStructure.getSelectionModel().getSelectedItems().addListener((ListChangeListener<TreeItem<Organisable>>) c -> {
            List<MediaInfo> list = AudiobookConverter.getContext().getSelectedMedia();
            list.clear();
            List<MediaInfo> newList = c.getList().stream().flatMap(item -> item.getValue().getMedia().stream()).collect(Collectors.toList());
            list.addAll(newList);
        });

        chapterColumn.setCellValueFactory(p -> new ReadOnlyObjectWrapper<>(p.getValue().getValue().getTitle()));
        detailsColumn.setCellValueFactory(p -> new ReadOnlyObjectWrapper<>(p.getValue().getValue().getDetails()));
        durationColumn.setCellValueFactory(p -> new ReadOnlyObjectWrapper<>(Utils.formatTime(p.getValue().getValue().getDuration())));

        importButton.setDisable(true);

        chaptersMode.addListener((observableValue, oldValue, newValue) -> importButton.setDisable(newValue || fileList.getItems().isEmpty()));
        fileList.getItems().addListener((ListChangeListener<MediaInfo>) change -> importButton.setDisable(fileList.getItems().isEmpty()));

        context.addSpeedChangeListener((observableValue, oldValue, newValue) -> {
            if (chaptersMode.get()) {
                Platform.runLater(() -> bookStructure.updateBookStructure());
            }
        });
    }

    private void initFileOpenMenu() {
        MenuItem item1 = new MenuItem("Files");
        item1.setOnAction(e -> selectFiles());
        MenuItem item2 = new MenuItem("Folder");
        item2.setOnAction(e -> selectFolder());
        contextMenu.getItems().addAll(item1, item2);
    }


    private void addDragEvenHandlers(Control control) {
        control.setOnDragOver(event -> {
            if (event.getGestureSource() != control && event.getDragboard().hasFiles()) {
                event.acceptTransferModes(TransferMode.ANY);
            }

            event.consume();
        });

        control.setOnDragDropped(event -> {
            List<File> files = event.getDragboard().getFiles();
            if (files != null && !files.isEmpty()) {
                List<String> fileNames = DialogHelper.collectFiles(files);
                processFiles(fileNames);
                event.setDropCompleted(true);
                event.consume();
                if (!chaptersMode.get()) {
                    if (!filesChapters.getTabs().contains(filesTab)) {
                        filesChapters.getTabs().add(filesTab);
                        filesChapters.getSelectionModel().select(filesTab);
                    }
                }
            }
        });
    }


    @FXML
    protected void addFiles(ActionEvent event) {
        Button node = (Button) event.getSource();
        contextMenu.show(node, Side.RIGHT, 0, 0);
    }

    public void selectFolder() {
        List<String> fileNames = DialogHelper.selectFolderDialog();
        if (fileNames != null) {
            processFiles(fileNames);
            if (!chaptersMode.get()) {
                if (!filesChapters.getTabs().contains(filesTab)) {
                    filesChapters.getTabs().add(filesTab);
                }
                filesChapters.getSelectionModel().select(filesTab);
            }
        }
    }


    private void processFiles(List<String> fileNames) {
        FFMediaLoader mediaLoader = createMediaLoader(fileNames);
        AudiobookConverter.getContext().setMediaLoader(mediaLoader);
        List<MediaInfo> addedMedia = mediaLoader.loadMediaInfo();
        if (chaptersMode.get()) {
            AudiobookConverter.getContext().constructBook(addedMedia);
            bookStructure.updateBookStructure();
        } else {
            AudiobookConverter.getContext().addNewMedia(addedMedia);
        }
    }


    private FFMediaLoader createMediaLoader(List<String> fileNames) {
        return new FFMediaLoader(fileNames, AudiobookConverter.getContext().getConversionGroup());
    }

    public void selectFiles() {
        List<String> fileNames = DialogHelper.selectFilesDialog();
        if (fileNames != null) {
            processFiles(fileNames);
            if (!chaptersMode.get()) {
                if (!filesChapters.getTabs().contains(filesTab)) {
                    filesChapters.getTabs().add(filesTab);
                }
                filesChapters.getSelectionModel().select(filesTab);
            }
        }
    }

    @FXML
    public void remove(ActionEvent event) {
        if (chaptersMode.get()) {
            bookStructure.removeChapters(event);
        } else {
            fileList.removeFiles(event);
        }
    }

    public void clear(ActionEvent event) {
        fileList.getItems().clear();
        AudiobookConverter.getContext().getConversionGroup().cancel();
        AudiobookConverter.getContext().detach();
        bookStructure.setRoot(null);
        filesChapters.getTabs().remove(filesTab);
        filesChapters.getTabs().remove(chaptersTab);
        chaptersMode.set(false);

    }

    public void moveUp(ActionEvent event) {
        if (chaptersMode.get()) {
            bookStructure.moveChapterUp(event);
        } else {
            fileList.moveFileUp(event);
        }
    }

    public void moveDown(ActionEvent event) {
        if (chaptersMode.get()) {
            bookStructure.moveChapterDown(event);
        } else {
            fileList.moveFileDown(event);
        }
    }

    public void subTracks(ActionEvent event) {
        if (chaptersMode.get()) {
            bookStructure.subTracks(event);
        }
    }

    public void editChapter(ActionEvent event) {
        if (chaptersMode.get()) {
            bookStructure.editChapter(event);
        }
    }

    public void importChapterNames(ActionEvent event) {
        if (chaptersMode.get()) {
            bookStructure.importChapterNames(event);
        }
    }


    public void start(ActionEvent actionEvent) {
        ConversionContext context = AudiobookConverter.getContext();
        if (context.getBook() == null && fileList.getItems().isEmpty()) return;

        String outputDestination = DialogHelper.selectOutputFile(AudiobookConverter.getContext().getBookInfo());

        if (outputDestination == null) {
            return;
        }

//        ObservableList<MediaInfo> mediaInfos = FXCollections.observableArrayList(fileList.getItems());


        ConversionGroup conversionGroup = AudiobookConverter.getContext().detach();

/* TODO!!!!
        conversionGroup.setOutputParameters(new OutputParameters(context.getOutputParameters()));
        conversionGroup.setBookInfo(context.getBookInfo().get());
        conversionGroup.setPosters(new ArrayList<>(context.getPosters()));
*/

        ProgressComponent placeHolderProgress = new ProgressComponent(new ConversionProgress(new ConversionJob(conversionGroup, Convertable.EMPTY, Collections.emptyMap(), outputDestination)));


        Executors.newSingleThreadExecutor().submit(() -> {
            Platform.runLater(() -> {
                progressQueue.getItems().add(0, placeHolderProgress);
                filesChapters.getSelectionModel().select(queueTab);
            });
            conversionGroup.launch(progressQueue, placeHolderProgress, outputDestination);

//            launch(conversionGroup, mediaInfos, placeHolderProgress, outputDestination);
        });

//        ConverterApplication.getContext().resetForNewConversion();
        bookStructure.setRoot(null);
        filesChapters.getTabs().remove(filesTab);
        filesChapters.getTabs().remove(chaptersTab);
        context.getMedia().clear();
        context.getPosters().clear();
//        fileList.getItems().clear();
        chaptersMode.set(false);
    }

    public void importChapters(ActionEvent actionEvent) {
        if (fileList.getItems().isEmpty()) {
            return;
        }

        startButton.setDisable(true);

        filesChapters.getTabs().add(chaptersTab);
        filesChapters.getTabs().remove(filesTab);

        bookStructure.setShowRoot(false);

        ObservableList<MediaInfo> mediaInfos = FXCollections.observableArrayList(fileList.getItems());

        Book book = new Book(AudiobookConverter.getContext().getBookInfo());

        TreeItem<Organisable> bookItem = new TreeItem<>(book);
        bookStructure.setRoot(bookItem);
        AudiobookConverter.getContext().setBook(book);

        bookStructure.updateBookStructure();

        bookItem.setExpanded(true);

        filesChapters.getSelectionModel().select(chaptersTab);
        fileList.getItems().clear();
        chaptersMode.set(true);


        long lastBookUpdate = System.currentTimeMillis();
        book.addListener(observable -> {
            logger.debug("Captured book modification");
            if (System.currentTimeMillis() - lastBookUpdate > 1000) {
                Platform.runLater(() -> bookStructure.updateBookStructure());
            }
        });

        Executors.newSingleThreadExecutor().submit(() -> {
            try {
                book.construct(mediaInfos);
                bookStructure.updateBookStructure();
            } finally {
                startButton.setDisable(false);
            }
        });
    }

    @FXML
    public void combine(ActionEvent event) {
        bookStructure.combineChapters(event);
    }

    @FXML
    public void split(ActionEvent event) {
        bookStructure.split(event);
    }


    @FXML
    public void pause(ActionEvent actionEvent) {
        ConversionContext context = AudiobookConverter.getContext();
        if (context.isPaused()) {
            context.resumeConversions();
            pauseButton.setText("Pause all");
        } else {
            context.pauseConversions();
            pauseButton.setText("Resume all");
        }
    }

    public void stop(ActionEvent actionEvent) {
        AudiobookConverter.getContext().stopConversions();
    }

    @FXML
    protected void openLink(ActionEvent event) {
        Hyperlink source = (Hyperlink) event.getSource();
        AudiobookConverter.getEnv().showDocument(source.getUserData().toString());
    }

    public void openWebSite(ActionEvent actionEvent) {
        AudiobookConverter.getEnv().showDocument("https://www.recoupler.com/products/audiobookconverter");
    }

    public void openAboutPage(ActionEvent actionEvent) {
        AudiobookConverter.getEnv().showDocument("https://www.recoupler.com/products/audiobookconverter/about");
    }

    public void openFAQ(ActionEvent actionEvent) {
        AudiobookConverter.getEnv().showDocument("https://www.recoupler.com/products/audiobookconverter/faq");
    }

    public void openDiscussions(ActionEvent actionEvent) {
        AudiobookConverter.getEnv().showDocument("https://github.com/yermak/AudioBookConverter/discussions");
    }

    public void openDonate() {
        AudiobookConverter.getEnv().showDocument("https://www.recoupler.com/products/audiobookconverter/donate");
    }

    public void checkVersion(ActionEvent actionEvent) {
        AudiobookConverter.checkNewVersion();
    }

    public void exit(ActionEvent actionEvent) {
        logger.info("Closing application");
        AudiobookConverter.getContext().stopConversions();
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.exit(0);
    }


    public void clearQueue(ActionEvent actionEvent) {
        ObservableList<ProgressComponent> items = progressQueue.getItems();
        List<ProgressComponent> dones = new ArrayList<>();
        for (ProgressComponent item : items) {
            if (item.isOver()) dones.add(item);
        }
        Platform.runLater(() -> {
            for (ProgressComponent done : dones) {
                progressQueue.getItems().remove(done);
            }
        });
    }

    public void settings(ActionEvent actionEvent) {
        SettingsDialog dialog = new SettingsDialog(AudiobookConverter.getEnv().getWindow());

        Optional<Map<String, Object>> result = dialog.showAndWait();
        result.ifPresent(r -> {
            Boolean darkMode = (Boolean) r.get(SettingsDialog.DARK_MODE);
            String filenameFormat = (String) r.get(SettingsDialog.FILENAME_FORMAT);
            String partFormat = (String) r.get(SettingsDialog.PART_FORMAT);
            String chapterFormat = (String) r.get(SettingsDialog.CHAPTER_FORMAT);
            Boolean showHints = (Boolean) r.get(SettingsDialog.SHOW_HINTS);
            Settings settings = Settings.loadSetting();
            settings.setDarkMode(darkMode);
            settings.setFilenameFormat(filenameFormat);
            settings.setPartFormat(partFormat);
            settings.setChapterFormat(chapterFormat);
            settings.setShowHints(showHints);
            settings.save();
            AudiobookConverter.getEnv().setDarkMode(darkMode);
        });
    }

    public void openIssues(ActionEvent actionEvent) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Report bug");
        alert.setContentText("Your setting will be copied into buffer and you will be redirected to GitHub issues page.\n" +
                "Please describe your problem and paste settings into the issue.\n" +
                "Note: Your settings may contain sensitive information like your user name, paths to your files, etc.\n");
        Optional<ButtonType> result = alert.showAndWait();
        if ((result.isPresent()) && (result.get() == ButtonType.OK)) {
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            Properties properties = System.getProperties();
            clipboard.setContents(new StringSelection(Utils.propertiesToString(properties) + "\n" + Settings.getRawData()), null);
            AudiobookConverter.getEnv().showDocument("https://github.com/yermak/AudioBookConverter/issues");
        }
    }

    public void repair(ActionEvent actionEvent) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Repair");
        alert.setContentText("Are you sure you want to restore settings to default?\nProgram will be closed.");
        Optional<ButtonType> result = alert.showAndWait();
        if ((result.isPresent()) && (result.get() == ButtonType.OK)) {
            Settings.clear();
            System.exit(0);
        }
    }

    public void showHints(ActionEvent actionEvent) {
        try {
            AudiobookConverter.loadHints();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
