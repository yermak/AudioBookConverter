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
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;
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
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.lang.invoke.MethodHandles;
import java.util.*;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * Created by Yermak on 04-Feb-18.
 */
public class FilesController extends VBox {
    final static Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    public MenuItem removeMenu;


    private final Button addButton;
    private final Button clearButton;

    private final Button importButton;

    private final TabPane filesChapters;
    private final Tab chaptersTab;
    private final Tab filesTab;

    private final Tab queueTab;


    private final ListView<ProgressComponent> progressQueue;

    private final TabPane tabs;


    private final Button pauseButton;
    private final Button stopButton;

    private final StackPane mediaPlayerContainer;

    private final StackPane artworkContainer;

    private final FileListComponent fileList;

    BookStructureComponent bookStructure;

    private final Button startButton;

    private final ResourceBundle resources;


    private final ContextMenu contextMenu = new ContextMenu();

    private final BooleanProperty chaptersMode = new SimpleBooleanProperty(false);


    //TODO move columns into BookStructureComponent
    private final TreeTableColumn<Organisable, String> chapterColumn;
    private final TreeTableColumn<Organisable, String> durationColumn;
    private final TreeTableColumn<Organisable, String> detailsColumn;

    public FilesController() {
        this(AudiobookConverter.getBundle());
    }

    public FilesController(ResourceBundle resources) {
        this.resources = resources;

        Screen screen = Screen.getPrimary();
        setPadding(new Insets(10, 10, 10, 10));
        setAlignment(Pos.BOTTOM_CENTER);
        setPrefWidth(screen.getVisualBounds().getWidth() * 0.70);
        setPrefHeight(screen.getVisualBounds().getHeight() * 0.80);

        MenuBar menuBar = buildMenuBar();

        filesChapters = new TabPane();
        VBox.setVgrow(filesChapters, Priority.ALWAYS);
        filesChapters.setPadding(new Insets(10));

        queueTab = new Tab(resources.getString("tab.queue"));
        queueTab.setClosable(false);
        filesTab = new Tab(resources.getString("tab.files"));
        filesTab.setClosable(false);
        chaptersTab = new Tab(resources.getString("tab.chapters"));
        chaptersTab.setClosable(false);

        pauseButton = new Button(resources.getString("queue.button.pause_all"));
        stopButton = new Button(resources.getString("queue.button.stop_all"));
        clearButton = new Button(resources.getString("files.button.clear_all"));
        addButton = new Button(resources.getString("files.button.add"));
        importButton = new Button(resources.getString("files.button.chapters"));
        startButton = new Button(resources.getString("files.button.start"));

        fileList = new FileListComponent();
        progressQueue = new ListView<>();
        progressQueue.setTooltip(new Tooltip(resources.getString("queue.tooltip.list")));
        VBox.setVgrow(progressQueue, Priority.ALWAYS);

        bookStructure = new BookStructureComponent();
        bookStructure.setEditable(true);
        VBox.setVgrow(bookStructure, Priority.ALWAYS);
        chapterColumn = new TreeTableColumn<>(resources.getString("column.chapter.title"));
        durationColumn = new TreeTableColumn<>(resources.getString("column.chapter.duration"));
        detailsColumn = new TreeTableColumn<>(resources.getString("column.chapter.details"));
        bookStructure.getColumns().addAll(List.of(chapterColumn, durationColumn, detailsColumn));

        mediaPlayerContainer = new StackPane();
        artworkContainer = new StackPane();

        tabs = new TabPane();
        tabs.setPadding(new Insets(5, 10, 10, 10));

        buildQueueTab(screen);
        buildFilesTab(screen);
        buildChaptersTab(screen);

        filesChapters.getTabs().add(queueTab);

        Tab bookInfoTab = new Tab(resources.getString("tab.book_info"));
        bookInfoTab.setClosable(false);
        bookInfoTab.setTooltip(new Tooltip(resources.getString("bookinfo.tooltip.tab")));
        bookInfoTab.setContent(new BookInfoController());

        Tab artTab = new Tab(resources.getString("tab.art_work"));
        artTab.setClosable(false);
        artTab.setTooltip(new Tooltip(resources.getString("artwork.tooltip.tab")));
        artTab.setContent(artworkContainer);

        Tab qualityTab = new Tab(resources.getString("tab.quality"));
        qualityTab.setClosable(false);
        qualityTab.setTooltip(new Tooltip(resources.getString("output.tooltip.tab")));
        qualityTab.setContent(new OutputController());

        tabs.getTabs().addAll(bookInfoTab, artTab, qualityTab);

        mediaPlayerContainer.setPrefHeight(Region.USE_COMPUTED_SIZE);

        getChildren().addAll(menuBar, filesChapters, mediaPlayerContainer, tabs);

        initialize();
    }

    private MenuBar buildMenuBar() {
        MenuBar menuBar = new MenuBar();

        Menu fileMenu = new Menu(resources.getString("menu.file"));
        Menu addMenu = new Menu(resources.getString("menu.file.add"));
        MenuItem addFiles = menuItem(resources.getString("menu.file.add.files"), this::selectFiles,
                new KeyCodeCombination(KeyCode.INSERT));
        MenuItem addFolder = menuItem(resources.getString("menu.file.add.folder"), this::selectFolder,
                new KeyCodeCombination(KeyCode.INSERT, KeyCombination.CONTROL_DOWN));
        addMenu.getItems().addAll(addFiles, addFolder);
        fileMenu.getItems().add(addMenu);
        fileMenu.getItems().add(new SeparatorMenuItem());
        removeMenu = menuItem(resources.getString("menu.file.remove"), this::remove,
                new KeyCodeCombination(KeyCode.DELETE));
        fileMenu.getItems().add(removeMenu);
        fileMenu.getItems().add(menuItem(resources.getString("menu.file.clear"), this::clear,
                new KeyCodeCombination(KeyCode.DELETE, KeyCombination.CONTROL_DOWN)));
        fileMenu.getItems().add(new SeparatorMenuItem());
        fileMenu.getItems().add(menuItem(resources.getString("menu.file.move_up"), this::moveUp,
                new KeyCodeCombination(KeyCode.UP)));
        fileMenu.getItems().add(menuItem(resources.getString("menu.file.move_down"), this::moveDown,
                new KeyCodeCombination(KeyCode.DOWN)));
        fileMenu.getItems().add(new SeparatorMenuItem());
        fileMenu.getItems().add(menuItem(resources.getString("menu.file.exit"), this::exit,
                new KeyCodeCombination(KeyCode.F4, KeyCombination.ALT_DOWN)));

        Menu chapterMenu = new Menu(resources.getString("menu.chapter"));
        chapterMenu.getItems().add(menuItem(resources.getString("menu.chapter.import"), this::importChapters,
                new KeyCodeCombination(KeyCode.I, KeyCombination.CONTROL_DOWN)));
        chapterMenu.getItems().add(menuItem(resources.getString("menu.chapter.edit"), this::editChapter,
                new KeyCodeCombination(KeyCode.F2, KeyCombination.CONTROL_DOWN)));
        chapterMenu.getItems().add(new SeparatorMenuItem());
        chapterMenu.getItems().add(menuItem(resources.getString("menu.chapter.combine"), this::combine,
                new KeyCodeCombination(KeyCode.MULTIPLY, KeyCombination.CONTROL_DOWN)));
        chapterMenu.getItems().add(menuItem(resources.getString("menu.chapter.split"), this::split,
                new KeyCodeCombination(KeyCode.DIVIDE, KeyCombination.CONTROL_DOWN)));
        chapterMenu.getItems().add(new SeparatorMenuItem());
        chapterMenu.getItems().add(menuItem(resources.getString("menu.chapter.move_up"), this::moveUp,
                new KeyCodeCombination(KeyCode.UP)));
        chapterMenu.getItems().add(menuItem(resources.getString("menu.chapter.move_down"), this::moveDown,
                new KeyCodeCombination(KeyCode.DOWN)));

        Menu convertMenu = new Menu(resources.getString("menu.convert"));
        convertMenu.getItems().add(menuItem(resources.getString("menu.convert.start"), this::start,
                new KeyCodeCombination(KeyCode.ENTER, KeyCombination.CONTROL_DOWN)));
        convertMenu.getItems().add(new SeparatorMenuItem());
        convertMenu.getItems().add(menuItem(resources.getString("menu.convert.pause_all"), this::pause,
                new KeyCodeCombination(KeyCode.HOME, KeyCombination.CONTROL_DOWN)));
        convertMenu.getItems().add(menuItem(resources.getString("menu.convert.stop_all"), this::stop,
                new KeyCodeCombination(KeyCode.END, KeyCombination.CONTROL_DOWN)));
        convertMenu.getItems().add(menuItem(resources.getString("menu.convert.clear_queue"), this::clearQueue,
                new KeyCodeCombination(KeyCode.BACK_SPACE, KeyCombination.CONTROL_DOWN)));

        Menu systemMenu = new Menu(resources.getString("menu.system"));
        systemMenu.getItems().add(menuItem(resources.getString("menu.system.settings"), this::settings, null));
        systemMenu.getItems().add(menuItem(resources.getString("menu.system.repair"), this::repair, null));
        systemMenu.getItems().add(menuItem(resources.getString("menu.system.check_version"), this::checkVersion, null));

        Menu aboutMenu = new Menu(resources.getString("menu.about"));
        aboutMenu.getItems().add(menuItem(resources.getString("menu.about.show_hints"), this::showHints, null));
        aboutMenu.getItems().add(menuItem(resources.getString("menu.about.faq"), this::openFAQ, null));
        aboutMenu.getItems().add(menuItem(resources.getString("menu.about.report_bug"), this::openIssues, null));
        aboutMenu.getItems().add(menuItem(resources.getString("menu.about.discussions"), this::openDiscussions, null));
        aboutMenu.getItems().add(menuItem(resources.getString("menu.about.website"), this::openWebSite, null));
        aboutMenu.getItems().add(menuItem(resources.getString("menu.about.about"), this::openAboutPage, null));

        menuBar.getMenus().addAll(fileMenu, chapterMenu, convertMenu, systemMenu, aboutMenu);
        return menuBar;
    }

    private MenuItem menuItem(String title, EventHandler<ActionEvent> handler, KeyCombination accelerator) {
        MenuItem item = new MenuItem(title);
        item.setOnAction(handler);
        if (accelerator != null) {
            item.setAccelerator(accelerator);
        }
        return item;
    }

    private void buildQueueTab(Screen screen) {
        ToolBar queueBar = new ToolBar();

        Button newBook = new Button(resources.getString("queue.button.newbook"));
        newBook.setMinWidth(screen.getVisualBounds().getWidth() * 0.04);
        newBook.setStyle("-fx-font-size: 15;");
        newBook.setOnAction(this::addFiles);
        newBook.setTooltip(new Tooltip(resources.getString("queue.tooltip.newbook")));
        queueBar.getItems().addAll(newBook, new Separator());

        pauseButton.setMinWidth(screen.getVisualBounds().getWidth() * 0.04);
        pauseButton.setOnAction(this::pause);
        pauseButton.setTooltip(new Tooltip(resources.getString("queue.tooltip.pause_all")));
        stopButton.setMinWidth(screen.getVisualBounds().getWidth() * 0.04);
        stopButton.setOnAction(this::stop);
        stopButton.setTooltip(new Tooltip(resources.getString("queue.tooltip.stop_all")));
        Button clearQueueButton = new Button(resources.getString("queue.button.clear"));
        clearQueueButton.setMinWidth(screen.getVisualBounds().getWidth() * 0.04);
        clearQueueButton.setOnAction(this::clearQueue);
        clearQueueButton.setTooltip(new Tooltip(resources.getString("queue.tooltip.clear")));
        queueBar.getItems().addAll(pauseButton, stopButton, new Separator(), clearQueueButton);

        VBox queueBox = new VBox(queueBar, progressQueue);
        queueTab.setContent(queueBox);
    }

    private void buildFilesTab(Screen screen) {
        ToolBar filesBar = new ToolBar();
        addButton.setMinWidth(screen.getVisualBounds().getWidth() * 0.04);
        addButton.setOnAction(this::addFiles);
        addButton.setTooltip(new Tooltip(resources.getString("files.tooltip.button_add")));

        Button removeButton = new Button(resources.getString("files.button.remove"));
        removeButton.setMinWidth(screen.getVisualBounds().getWidth() * 0.04);
        removeButton.setOnAction(this::remove);
        removeButton.setTooltip(new Tooltip(resources.getString("files.tooltip.button_remove")));

        clearButton.setMinWidth(screen.getVisualBounds().getWidth() * 0.04);
        clearButton.setOnAction(this::clear);
        clearButton.setTooltip(new Tooltip(resources.getString("files.tooltip.button_clear")));

        Button moveUp = new Button(resources.getString("files.button.move_up"));
        moveUp.setMinWidth(screen.getVisualBounds().getWidth() * 0.04);
        moveUp.setOnAction(this::moveUp);
        moveUp.setTooltip(new Tooltip(resources.getString("files.tooltip.button_up")));

        Button moveDown = new Button(resources.getString("files.button.move_down"));
        moveDown.setMinWidth(screen.getVisualBounds().getWidth() * 0.04);
        moveDown.setOnAction(this::moveDown);
        moveDown.setTooltip(new Tooltip(resources.getString("files.tooltip.button_down")));

        importButton.setMinWidth(screen.getVisualBounds().getWidth() * 0.04);
        importButton.setOnAction(this::importChapters);
        importButton.setStyle("-fx-font-size: 15;");
        importButton.setTooltip(new Tooltip(resources.getString("files.tooltip.button_chapters")));

        startButton.setMinWidth(screen.getVisualBounds().getWidth() * 0.04);
        startButton.setOnAction(this::start);
        startButton.setStyle("-fx-font-size: 15;");
        startButton.setTooltip(new Tooltip(resources.getString("files.tooltip.button_start")));

        filesBar.getItems().addAll(addButton, removeButton, new Separator(), clearButton, new Separator(), moveUp, moveDown,
                new Separator(), importButton, new Separator(), startButton);

        fileList.setPrefHeight(screen.getVisualBounds().getHeight() * 0.25);
        VBox filesBox = new VBox(filesBar, fileList);
        filesTab.setContent(filesBox);
    }

    private void buildChaptersTab(Screen screen) {
        ToolBar chaptersBar = new ToolBar();
        Button add = new Button(resources.getString("chapters.button.add"));
        add.setMinWidth(screen.getVisualBounds().getWidth() * 0.04);
        add.setOnAction(this::addFiles);
        add.setTooltip(new Tooltip(resources.getString("chapters.tooltip.button_add")));

        Button remove = new Button(resources.getString("chapters.button.remove"));
        remove.setMinWidth(screen.getVisualBounds().getWidth() * 0.04);
        remove.setOnAction(this::remove);
        remove.setTooltip(new Tooltip(resources.getString("chapters.tooltip.button_remove")));

        Button clear = new Button(resources.getString("chapters.button.clear"));
        clear.setMinWidth(screen.getVisualBounds().getWidth() * 0.04);
        clear.setOnAction(this::clear);
        clear.setTooltip(new Tooltip(resources.getString("chapters.tooltip.button_clear")));

        Button moveUp = new Button(resources.getString("chapters.button.move_up"));
        moveUp.setMinWidth(screen.getVisualBounds().getWidth() * 0.04);
        moveUp.setOnAction(this::moveUp);
        moveUp.setTooltip(new Tooltip(resources.getString("chatpers.tooltip.button_up")));

        Button moveDown = new Button(resources.getString("chapters.button.move_down"));
        moveDown.setMinWidth(screen.getVisualBounds().getWidth() * 0.04);
        moveDown.setOnAction(this::moveDown);
        moveDown.setTooltip(new Tooltip(resources.getString("chapters.tooltip.button_down")));

        Button edit = new Button(resources.getString("chapters.button.edit"));
        edit.setMinWidth(screen.getVisualBounds().getWidth() * 0.04);
        edit.setOnAction(this::editChapter);
        edit.setTooltip(new Tooltip(resources.getString("chapters.tooltip.button_edit")));

        Button split = new Button(resources.getString("chapters.button.split"));
        split.setMinWidth(screen.getVisualBounds().getWidth() * 0.04);
        split.setOnAction(this::split);
        split.setTooltip(new Tooltip(resources.getString("chapters.tooltip.button_split")));

        Button combine = new Button(resources.getString("chapters.button.combine"));
        combine.setMinWidth(screen.getVisualBounds().getWidth() * 0.04);
        combine.setOnAction(this::combine);
        combine.setTooltip(new Tooltip(resources.getString("chapters.tooltip.button_combine")));

        Button subTracks = new Button(resources.getString("chapters.button.subtracks"));
        subTracks.setMinWidth(screen.getVisualBounds().getWidth() * 0.04);
        subTracks.setOnAction(this::subTracks);
        subTracks.setTooltip(new Tooltip(resources.getString("chapters.tooltip.button_subtrack")));

        Button startChapters = new Button(resources.getString("chapters.button.start"));
        startChapters.setMinWidth(screen.getVisualBounds().getWidth() * 0.04);
        startChapters.setOnAction(this::start);
        startChapters.setStyle("-fx-font-size: 15;");
        startChapters.setTooltip(new Tooltip(resources.getString("chapters.tooltip.button_start")));

        chaptersBar.getItems().addAll(add, remove, new Separator(), clear, new Separator(), moveUp, moveDown, new Separator(),
                edit, new Separator(), split, combine, new Separator(), subTracks, new Separator(), startChapters);

        bookStructure.setPrefHeight(screen.getVisualBounds().getHeight() * 0.025);
        VBox chaptersBox = new VBox(chaptersBar, bookStructure);
        chaptersTab.setContent(chaptersBox);
    }

    public void initialize() {
        addDragEvenHandlers(bookStructure);
        addDragEvenHandlers(fileList);
        addDragEvenHandlers(progressQueue);

        mediaPlayerContainer.getChildren().add(new MediaPlayerController());
        artworkContainer.getChildren().add(new ArtWorkController());

        Settings settings = Settings.loadSetting();
        AudiobookConverter.getContext().setPresetName(settings.getPresets().get(settings.getLastUsedPreset()).getName());

        initFileOpenMenu();

        ConversionContext context = AudiobookConverter.getContext();
        ObservableList<MediaInfo> selectedMedia = context.getSelectedMedia();

        selectedMedia.addListener((InvalidationListener) _ -> {
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
        ResourceBundle bundle = Objects.requireNonNull(resources, "Resource bundle not initialized");
        MenuItem item1 = new MenuItem(bundle.getString("context.files"));
        item1.setOnAction(this::selectFiles);
        MenuItem item2 = new MenuItem(bundle.getString("context.folder"));
        item2.setOnAction(this::selectFolder);
        contextMenu.getItems().addAll(item1, item2);
    }


    private void addDragEvenHandlers(Control control) {
        try {
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
                        }
                        filesChapters.getSelectionModel().select(filesTab);
                    }
                }
            });
        } catch (Exception e) {
            showError(e);
            throw new RuntimeException(e);
        }
    }


    protected void addFiles(ActionEvent event) {
        Button node = (Button) event.getSource();
        contextMenu.show(node, Side.RIGHT, 0, 0);
    }

    public void selectFolder(ActionEvent actionEvent) {
        try {
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
        } catch (Exception e) {
            showError(e);
            throw new RuntimeException(e);
        }
    }


    private void processFiles(List<String> fileNames) {
        FFMediaLoader mediaLoader = new FFMediaLoader(fileNames, AudiobookConverter.getContext().getConversionGroup());
        AudiobookConverter.getContext().setMediaLoader(mediaLoader);
        List<MediaInfo> addedMedia = mediaLoader.loadMediaInfo();
        if (chaptersMode.get()) {
            AudiobookConverter.getContext().constructBook(addedMedia);
            bookStructure.updateBookStructure();
        } else {
            AudiobookConverter.getContext().addNewMedia(addedMedia);
        }
    }

    private void showError(Exception e) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(Objects.requireNonNull(resources, "Resource bundle not initialized").getString("dialog.unexpected_error.title"));
        alert.setHeaderText(e.toString());
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        e.printStackTrace(new PrintStream(out));
        TextArea errorStack = new TextArea();
        alert.getDialogPane().setContent(errorStack);
        errorStack.setMinHeight(200);
        errorStack.setMinWidth(500);
        errorStack.setEditable(false);
        errorStack.setFocusTraversable(false);
        errorStack.setWrapText(false);
        errorStack.setText(out.toString());
        alert.showAndWait();
    }


    public void selectFiles(ActionEvent actionEvent) {
        try {
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
        } catch (Exception e) {
            showError(e);
            throw new RuntimeException(e);
        }
    }

    public void remove(ActionEvent event) {
        try {
            if (chaptersMode.get()) {
                bookStructure.removeChapters(event);
            } else {
                fileList.removeFiles(event);
            }
        } catch (Exception e) {
            showError(e);
            throw new RuntimeException(e);
        }
    }

    public void clear(ActionEvent event) {
        try {
            fileList.getItems().clear();
            AudiobookConverter.getContext().getConversionGroup().cancel();
            AudiobookConverter.getContext().detach();
            bookStructure.setRoot(null);
            filesChapters.getTabs().remove(filesTab);
            filesChapters.getTabs().remove(chaptersTab);
            chaptersMode.set(false);
        } catch (Exception e) {
            showError(e);
            throw new RuntimeException(e);
        }
    }

    public void moveUp(ActionEvent event) {
        try {
            if (chaptersMode.get()) {
                bookStructure.moveChapterUp(event);
            } else {
                fileList.moveFileUp(event);
            }
        } catch (Exception e) {
            showError(e);
            throw new RuntimeException(e);
        }
    }

    public void moveDown(ActionEvent event) {
        try {
            if (chaptersMode.get()) {
                bookStructure.moveChapterDown(event);
            } else {
                fileList.moveFileDown(event);
            }
        } catch (Exception e) {
            showError(e);
            throw new RuntimeException(e);
        }
    }

    public void subTracks(ActionEvent event) {
        try {
            if (chaptersMode.get()) {
                bookStructure.subTracks(event);
            }
        } catch (Exception e) {
            showError(e);
            throw new RuntimeException(e);
        }
    }

    public void editChapter(ActionEvent event) {
        try {
            if (chaptersMode.get()) {
                bookStructure.editChapter(event);
            }
        } catch (Exception e) {
            showError(e);
            throw new RuntimeException(e);
        }
    }


    public void start(ActionEvent actionEvent) {
        try {
            ConversionContext context = AudiobookConverter.getContext();
            if (context.getBook() == null && fileList.getItems().isEmpty()) return;

            String outputDestination = DialogHelper.selectOutputFile(AudiobookConverter.getContext().getBookInfo());

            if (outputDestination == null) {
                return;
            }

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
            });
            bookStructure.setRoot(null);
            filesChapters.getTabs().remove(filesTab);
            filesChapters.getTabs().remove(chaptersTab);
            context.getMedia().clear();
            context.getPosters().clear();
            chaptersMode.set(false);
        } catch (Exception e) {
            showError(e);
            throw new RuntimeException(e);
        }
    }

    public void importChapters(ActionEvent actionEvent) {
        try {
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
        } catch (Exception e) {
            showError(e);
            throw new RuntimeException(e);
        }
    }

    public void combine(ActionEvent event) {
        try {
            bookStructure.combineChapters(event);
        } catch (Exception e) {
            showError(e);
            throw new RuntimeException(e);
        }
    }

    public void split(ActionEvent event) {
        try {
            bookStructure.split(event);
        } catch (Exception e) {
            showError(e);
            throw new RuntimeException(e);
        }
    }


    public void pause(ActionEvent actionEvent) {
        try {
            ConversionContext context = AudiobookConverter.getContext();
            if (context.isPaused()) {
                context.resumeConversions();
                pauseButton.setText(resources.getString("button.pause_all"));
            } else {
                context.pauseConversions();
                pauseButton.setText(resources.getString("button.resume_all"));
            }
        } catch (Exception e) {
            showError(e);
            throw new RuntimeException(e);
        }
    }

    public void stop(ActionEvent actionEvent) {
        try {
            AudiobookConverter.getContext().stopConversions();
        } catch (Exception e) {
            showError(e);
            throw new RuntimeException(e);
        }
    }

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
        AudiobookConverter.checkNewVersion(resources);
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
        try {
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
        } catch (Exception e) {
            showError(e);
            throw new RuntimeException(e);
        }
    }

    public void settings(ActionEvent actionEvent) {
        try {
            SettingsDialog dialog = new SettingsDialog(AudiobookConverter.getEnv().getWindow());

            Optional<Map<String, Object>> result = dialog.showAndWait();
            result.ifPresent(r -> {
                Boolean darkMode = (Boolean) r.get(SettingsDialog.DARK_MODE);
                String filenameFormat = (String) r.get(SettingsDialog.FILENAME_FORMAT);
                String partFormat = (String) r.get(SettingsDialog.PART_FORMAT);
                String chapterFormat = (String) r.get(SettingsDialog.CHAPTER_FORMAT);
                Boolean showHints = (Boolean) r.get(SettingsDialog.SHOW_HINTS);
                String language = (String) r.get(SettingsDialog.LANGUAGE);
                Settings settings = Settings.loadSetting();
                settings.setDarkMode(darkMode);
                settings.setFilenameFormat(filenameFormat);
                settings.setPartFormat(partFormat);
                settings.setChapterFormat(chapterFormat);
                settings.setShowHints(showHints);
                settings.setLanguage(language);
                settings.save();
                AudiobookConverter.getEnv().setDarkMode(darkMode);
            });
        } catch (Exception e) {
            showError(e);
            throw new RuntimeException(e);
        }
    }

    public void openIssues(ActionEvent actionEvent) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(resources.getString("dialog.report_bug.title"));
        alert.setContentText(resources.getString("dialog.report_bug.message"));
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
        alert.setTitle(resources.getString("dialog.repair.title"));
        alert.setContentText(resources.getString("dialog.repair.message"));
        Optional<ButtonType> result = alert.showAndWait();
        if ((result.isPresent()) && (result.get() == ButtonType.OK)) {
            Settings.clear();
            System.exit(0);
        }
    }

    public void showHints(ActionEvent actionEvent) {
        try {
            AudiobookConverter.loadHints();
        } catch (Exception e) {
            showError(e);
        }
    }
}