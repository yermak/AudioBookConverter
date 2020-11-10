package uk.yermak.audiobookconverter.fx;

import com.google.common.collect.ImmutableSet;
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
import javafx.scene.input.TransferMode;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOCase;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.yermak.audiobookconverter.*;

import java.io.File;
import java.lang.invoke.MethodHandles;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * Created by Yermak on 04-Feb-18.
 */
public class FilesController {
    final static Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @FXML
    public ComboBox<String> outputFormatBox;
    @FXML
    public ComboBox<String> presetBox;

    @FXML
    private ComboBox<String> splitFileBox;

    @FXML
    private Button addButton;
    @FXML
    private Button removeButton;
    @FXML
    private Button clearButton;
    @FXML
    private Button upButton;
    @FXML
    private Button downButton;

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
    private ListView<MediaInfo> fileList;

    @FXML
    TreeTableView<Organisable> bookStructure;
    @FXML
    private TreeTableColumn<Organisable, String> chapterColumn;
    @FXML
    private TreeTableColumn<Organisable, String> durationColumn;
    @FXML
    private TreeTableColumn<Organisable, String> detailsColumn;

    @FXML
    private Button startButton;


    private static final String M4B = "m4b";
    private static final String M4A = "m4a";
    public static final String MP3 = "mp3";
    public static final String WMA = "wma";
    public static final String FLAC = "flac";
    public static final String AAC = "aac";
    public static final String OGG = "ogg";
    private final static String[] FILE_EXTENSIONS = {MP3, M4A, M4B, WMA, FLAC, OGG, AAC};

    private final ContextMenu contextMenu = new ContextMenu();

    private final BooleanProperty chaptersMode = new SimpleBooleanProperty(false);
    private boolean split;

    @FXML
    public void initialize() {
        ConversionContext context = ConverterApplication.getContext();

        addDragEvenHandlers(bookStructure);
        addDragEvenHandlers(fileList);
        addDragEvenHandlers(progressQueue);

        fileList.getSelectionModel().getSelectedItems().addListener((ListChangeListener<MediaInfo>) c -> {
            ConverterApplication.getContext().getSelectedMedia().clear();
            ConverterApplication.getContext().getSelectedMedia().addAll(c.getList());
        });

        splitFileBox.getSelectionModel().select(0);
        splitFileBox.getSelectionModel().selectedItemProperty().addListener((observableValue, oldValue, newValue) -> {
            switch (newValue) {
                case "parts" -> split = false;
                case "chapters" -> split = true;
            }
        });

//        outputFormatBox.getItems().addAll(Format.values());
        outputFormatBox.getSelectionModel().select(0);
        outputFormatBox.getSelectionModel().selectedItemProperty().addListener((observableValue, oldValue, newValue) -> {
            ConverterApplication.getContext().setOutputFormat(newValue.toString());

        });

        List<Preset> presets = Preset.loadPresets();
//        String savedPreset = Objects.requireNonNullElse(AppProperties.getProperty("last.preset"), "custom");
//        Preset lastPreset = presets.stream().filter(preset -> preset.getPresetName().equals(Preset.LAST_USED)).findFirst().get();

        presetBox.getItems().addAll(presets.stream().map(Preset::getPresetName).collect(Collectors.toList()));

        presetBox.getSelectionModel().select(Preset.LAST_USED);
        presetBox.getSelectionModel().selectedItemProperty().addListener((observableValue, oldValue, newValue) -> {
            if (!presetBox.getItems().contains(newValue)) {
                presetBox.getItems().add(newValue);
                Preset preset = Preset.copy(newValue, Preset.instance(oldValue));
                ConverterApplication.getContext().setOutputParameters(preset);
            } else {
                Preset preset = Preset.instance(newValue);
                ConverterApplication.getContext().setOutputParameters(preset);
            }
        });

        ConverterApplication.getContext().addOutputParametersChangeListener((observableValue, oldParams, newParams) -> outputFormatBox.setValue(newParams.getFormat()));

//        fileList.setCellFactory(new ListViewListCellCallback());
        MenuItem item1 = new MenuItem("Files");
        item1.setOnAction(e -> selectFilesDialog());
        MenuItem item2 = new MenuItem("Folder");
        item2.setOnAction(e -> selectFolderDialog());
        contextMenu.getItems().addAll(item1, item2);

        ObservableList<MediaInfo> media = context.getMedia();
        fileList.setItems(media);
        fileList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);


        ObservableList<MediaInfo> selectedMedia = context.getSelectedMedia();

        selectedMedia.addListener((InvalidationListener) observable -> {
            if (selectedMedia.isEmpty()) return;
            if (!chaptersMode.get()) {
                List<MediaInfo> change = new ArrayList<>(selectedMedia);
                List<MediaInfo> selection = new ArrayList<>(fileList.getSelectionModel().getSelectedItems());
                if (!change.containsAll(selection) || !selection.containsAll(change)) {
                    fileList.getSelectionModel().clearSelection();
                    change.forEach(m -> fileList.getSelectionModel().select(media.indexOf(m)));
                }
            }
        });

        filesChapters.getTabs().remove(filesTab);
        filesChapters.getTabs().remove(chaptersTab);

        bookStructure.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        chapterColumn.setCellValueFactory(p -> new ReadOnlyObjectWrapper<>(p.getValue().getValue().getTitle()));
        detailsColumn.setCellValueFactory(p -> new ReadOnlyObjectWrapper<>(p.getValue().getValue().getDetails()));
        durationColumn.setCellValueFactory(p -> new ReadOnlyObjectWrapper<>(Utils.formatTime(p.getValue().getValue().getDuration())));


        importButton.setDisable(true);

        chaptersMode.addListener((observableValue, oldValue, newValue) -> importButton.setDisable(newValue || fileList.getItems().isEmpty()));
        fileList.getItems().addListener((ListChangeListener<MediaInfo>) change -> importButton.setDisable(fileList.getItems().isEmpty()));

    }

    private void addDragEvenHandlers(Control control) {
        control.setOnDragOver(event -> {
            if (event.getGestureSource() != control && event.getDragboard().hasFiles()) {
                event.acceptTransferModes(TransferMode.ANY);
            }

            event.consume();
        });

        control.setOnDragDropped(event -> {
            processFiles(event.getDragboard().getFiles());
            event.setDropCompleted(true);
            event.consume();
            if (!chaptersMode.get()) {
                if (!filesChapters.getTabs().contains(filesTab)) {
                    filesChapters.getTabs().add(filesTab);
                    filesChapters.getSelectionModel().select(filesTab);
                }
            }
        });
    }


    @FXML
    protected void addFiles(ActionEvent event) {
        Button node = (Button) event.getSource();
        contextMenu.show(node, Side.RIGHT, 0, 0);
    }

    public void selectFolderDialog() {
        Window window = ConverterApplication.getEnv().getWindow();
        DirectoryChooser directoryChooser = new DirectoryChooser();
        String sourceFolder = AppProperties.getProperty("source.folder");
        directoryChooser.setInitialDirectory(Utils.getInitialDirecotory(sourceFolder));

        StringJoiner filetypes = new StringJoiner("/");

        Arrays.stream(FILE_EXTENSIONS).map(String::toUpperCase).forEach(filetypes::add);

        directoryChooser.setTitle("Select folder with " + filetypes.toString() + " files for conversion");
        File selectedDirectory = directoryChooser.showDialog(window);
        if (selectedDirectory != null) {
            processFiles(Collections.singleton(selectedDirectory));
            AppProperties.setProperty("source.folder", selectedDirectory.getAbsolutePath());
            if (!chaptersMode.get()) {
                if (!filesChapters.getTabs().contains(filesTab)) {
                    filesChapters.getTabs().add(filesTab);
                }
                filesChapters.getSelectionModel().select(filesTab);
            }
        }
    }


    private void processFiles(Collection<File> files) {
        List<String> fileNames = collectFiles(files);

        List<MediaInfo> addedMedia = createMediaLoader(fileNames).loadMediaInfo();
        if (chaptersMode.get()) {
            Book book = ConverterApplication.getContext().getBook();
            book.construct(FXCollections.observableArrayList(addedMedia));
            updateBookStructure(book, bookStructure.getRoot());
        } else {
            fileList.getItems().addAll(addedMedia);
        }
    }

    private static String[] toSuffixes(String prefix, final String[] extensions) {
        final String[] suffixes = new String[extensions.length];
        for (int i = 0; i < extensions.length; i++) {
            suffixes[i] = prefix + extensions[i];
        }
        return suffixes;
    }

    private List<String> collectFiles(Collection<File> files) {
        List<String> fileNames = new ArrayList<>();
        ImmutableSet<String> extensions = ImmutableSet.copyOf(FILE_EXTENSIONS);

        for (File file : files) {
            if (file.isDirectory()) {
                SuffixFileFilter suffixFileFilter = new SuffixFileFilter(toSuffixes(".", FILE_EXTENSIONS), IOCase.INSENSITIVE);
                Collection<File> nestedFiles = FileUtils.listFiles(file, suffixFileFilter, TrueFileFilter.INSTANCE);
                nestedFiles.stream().map(File::getPath).forEach(fileNames::add);
            } else {
                boolean allowedFileExtension = extensions.contains(FilenameUtils.getExtension(file.getName()).toLowerCase());
                if (allowedFileExtension) {
                    fileNames.add(file.getPath());
                }
            }
        }
        return fileNames;
    }

    private FFMediaLoader createMediaLoader(List<String> fileNames) {
        return new FFMediaLoader(fileNames, ConverterApplication.getContext().getPlannedConversionGroup());
    }

    public void selectFilesDialog() {
        Window window = ConverterApplication.getEnv().getWindow();
        final FileChooser fileChooser = new FileChooser();
        String sourceFolder = AppProperties.getProperty("source.folder");
        fileChooser.setInitialDirectory(Utils.getInitialDirecotory(sourceFolder));
        StringJoiner filetypes = new StringJoiner("/");

        Arrays.stream(FILE_EXTENSIONS).map(String::toUpperCase).forEach(filetypes::add);

        fileChooser.setTitle("Select " + filetypes.toString() + " files for conversion");

        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Audio", Arrays.asList(toSuffixes("*.", FILE_EXTENSIONS))));


        List<File> files = fileChooser.showOpenMultipleDialog(window);
        if (files != null) {
            processFiles(files);
            File firstFile = files.get(0);
            File parentFile = firstFile.getParentFile();
            AppProperties.setProperty("source.folder", parentFile.getAbsolutePath());
            if (!chaptersMode.get()) {
                if (!filesChapters.getTabs().contains(filesTab)) {
                    filesChapters.getTabs().add(filesTab);
                }
                filesChapters.getSelectionModel().select(filesTab);
            }
        }
    }

    public void removeFiles(ActionEvent event) {
        if (chaptersMode.get()) {
            ObservableList<TreeTablePosition<Organisable, ?>> selectedCells = bookStructure.getSelectionModel().getSelectedCells();
//            int min = bookStructure.getExpandedItemCount();
            for (TreeTablePosition<Organisable, ?> selectedCell : selectedCells) {
                Organisable organisable = selectedCell.getTreeItem().getValue();
                organisable.remove();
//                min = Math.min(selectedCell.getRow(), min - 1);
            }


//            int finalMin = min;
            Platform.runLater(() -> {
                updateBookStructure(ConverterApplication.getContext().getBook(), bookStructure.getRoot());
                //TODO temp hack - can't selection on previous row causes NPE on next removal...
                bookStructure.getSelectionModel().clearSelection();
                if (bookStructure.getRoot().getChildren().isEmpty()) {
                    clear(event);
                }
            });

        } else {
            ObservableList<MediaInfo> selected = fileList.getSelectionModel().getSelectedItems();
            fileList.getItems().removeAll(selected);
            if (fileList.getItems().isEmpty()) {
                filesChapters.getTabs().remove(filesTab);
            }
        }
    }

    public void clear(ActionEvent event) {

        fileList.getItems().clear();
        ConverterApplication.getContext().getPlannedConversionGroup().cancel();
        ConverterApplication.getContext().resetForNewConversion();
        bookStructure.setRoot(null);
        filesChapters.getTabs().remove(filesTab);
        filesChapters.getTabs().remove(chaptersTab);
        chaptersMode.set(false);

    }

    public void moveUp(ActionEvent event) {
        if (chaptersMode.get()) {
            ObservableList<TreeTablePosition<Organisable, ?>> selectedCells = bookStructure.getSelectionModel().getSelectedCells();
            if (selectedCells.size() == 1) {
                Organisable organisable = selectedCells.get(0).getTreeItem().getValue();
                organisable.moveUp();
                Platform.runLater(() -> updateBookStructure(ConverterApplication.getContext().getBook(), bookStructure.getRoot()));
            }
        } else {
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
    }

    public void moveDown(ActionEvent event) {
        if (chaptersMode.get()) {
            ObservableList<TreeTablePosition<Organisable, ?>> selectedCells = bookStructure.getSelectionModel().getSelectedCells();
            if (selectedCells.size() == 1) {
                Organisable organisable = selectedCells.get(0).getTreeItem().getValue();
                organisable.moveDown();
                Platform.runLater(() -> updateBookStructure(ConverterApplication.getContext().getBook(), bookStructure.getRoot()));
            }
        } else {
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
    }

    private void launch(ConversionGroup conversionGroup, Book book, ObservableList<MediaInfo> mediaInfos, ProgressComponent progressComponent, String outputDestination) {

        if (book == null) {
            book = new Book(ConverterApplication.getContext().getBookInfo().get());
            book.construct(mediaInfos);
        }

        ObservableList<Part> parts = book.getParts();
        String extension = ConverterApplication.getContext().getOutputFormat();
//        String extension = FilenameUtils.getExtension(outputDestination);
        conversionGroup.getOutputParameters().setupFormat(extension);

        if (split) {
            List<Chapter> chapters = parts.stream().flatMap(p -> p.getChapters().stream()).collect(Collectors.toList());
            logger.debug("Found {} chapters in the book", chapters.size());
            for (int i = 0; i < chapters.size(); i++) {
                Chapter chapter = chapters.get(i);
                String finalDesination = outputDestination;
                if (chapters.size() > 1) {
                    finalDesination = finalDesination.replace("." + extension, ", Chapter " + (i + 1) + "." + extension);
                }
                String finalName = new File(finalDesination).getName();
                logger.debug("Adding conversion for chapter {}", finalName);

                ConversionProgress conversionProgress = conversionGroup.start(chapter, finalDesination);
                Platform.runLater(() -> {
                    progressQueue.getItems().add(0, new ProgressComponent(conversionProgress));
                });

            }
        } else {
            logger.debug("Found {} parts in the book", parts.size());
            for (int i = 0; i < parts.size(); i++) {
                Part part = parts.get(i);
                String finalDesination = outputDestination;
                if (parts.size() > 1) {
                    finalDesination = finalDesination.replace("." + extension, ", Part " + (i + 1) + "." + extension);
                }
                String finalName = new File(finalDesination).getName();
                logger.debug("Adding conversion for part {}", finalName);

                ConversionProgress conversionProgress = conversionGroup.start(part, finalDesination);
                Platform.runLater(() -> {
                    progressQueue.getItems().add(0, new ProgressComponent(conversionProgress));
                });
            }
        }

        Platform.runLater(() -> progressQueue.getItems().remove(progressComponent));
    }

    public void start(ActionEvent actionEvent) {
        ConversionContext context = ConverterApplication.getContext();
        if (context.getBook() == null && fileList.getItems().isEmpty()) return;

        String outputDestination = selectOutputFile(ConverterApplication.getContext().getBookInfo().get());

        if (outputDestination == null) {
            return;
        }

        ObservableList<MediaInfo> mediaInfos = FXCollections.observableArrayList(fileList.getItems());


        ConversionGroup conversionGroup = ConverterApplication.getContext().getPlannedConversionGroup();

        conversionGroup.setOutputParameters(new OutputParameters(context.getOutputParameters()));
        conversionGroup.setBookInfo(context.getBookInfo().get());
        conversionGroup.setPosters(new ArrayList<>(context.getPosters()));

        ProgressComponent placeHolderProgress = new ProgressComponent(new ConversionProgress(new ConversionJob(context.getPlannedConversionGroup(), Convertable.EMPTY, Collections.emptyMap(), outputDestination)));


        Executors.newSingleThreadExecutor().submit(() -> {
            Platform.runLater(() -> {
                progressQueue.getItems().add(0, placeHolderProgress);
                filesChapters.getSelectionModel().select(queueTab);
            });
            launch(conversionGroup, context.getBook(), mediaInfos, placeHolderProgress, outputDestination);
        });

        ConverterApplication.getContext().resetForNewConversion();
        bookStructure.setRoot(null);
        filesChapters.getTabs().remove(filesTab);
        filesChapters.getTabs().remove(chaptersTab);
        fileList.getItems().clear();
        chaptersMode.set(false);
    }

    private String selectOutputFile(AudioBookInfo audioBookInfo) {
        JfxEnv env = ConverterApplication.getEnv();

        final FileChooser fileChooser = new FileChooser();
        String outputFolder = AppProperties.getProperty("output.folder");
        fileChooser.setInitialDirectory(Utils.getInitialDirecotory(outputFolder));
        fileChooser.setInitialFileName(Utils.getOuputFilenameSuggestion(audioBookInfo));
        fileChooser.setTitle("Save AudioBook");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter(ConverterApplication.getContext().getOutputFormat(), "*." + ConverterApplication.getContext().getOutputFormat())
/*
                new FileChooser.ExtensionFilter(M4A, "*." + M4A),
                new FileChooser.ExtensionFilter(MP3, "*." + MP3),
                new FileChooser.ExtensionFilter(OGG, "*." + OGG)
*/
        );
/*
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter(M4B, "*." + M4B),
                new FileChooser.ExtensionFilter(M4A, "*." + M4A),
                new FileChooser.ExtensionFilter(MP3, "*." + MP3),
                new FileChooser.ExtensionFilter(OGG, "*." + OGG)
        );
*/
        File file = fileChooser.showSaveDialog(env.getWindow());
        if (file == null) return null;
        File parentFolder = file.getParentFile();
        AppProperties.setProperty("output.folder", parentFolder.getAbsolutePath());
        return file.getPath();
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

        Book book = new Book(ConverterApplication.getContext().getBookInfo().get());

        TreeItem<Organisable> bookItem = new TreeItem<>(book);
        bookStructure.setRoot(bookItem);

        updateBookStructure(book, bookItem);

        bookItem.setExpanded(true);
        ConverterApplication.getContext().setBook(book);
        filesChapters.getSelectionModel().select(chaptersTab);
        fileList.getItems().clear();
        chaptersMode.set(true);


        long lastBookUpdate = System.currentTimeMillis();
        book.addListener(observable -> {
            logger.debug("Captured book modification");
            if (System.currentTimeMillis() - lastBookUpdate > 1000) {
                Platform.runLater(() -> updateBookStructure(book, bookItem));
            }
        });

        Executors.newSingleThreadExecutor().submit(() -> {
            try {
                book.construct(mediaInfos);
                updateBookStructure(book, bookItem);
            } finally {
                startButton.setDisable(false);
            }
        });
    }

    private void updateBookStructure(Book book, TreeItem<Organisable> bookItem) {
        bookStructure.getRoot().getChildren().clear();
        book.getParts().forEach(p -> {
            TreeItem<Organisable> partItem = new TreeItem<>(p);
            bookItem.getChildren().add(partItem);
            p.getChapters().forEach(c -> {
                TreeItem<Organisable> chapterItem = new TreeItem<>(c);
                partItem.getChildren().add(chapterItem);
                c.getMedia().forEach(m -> chapterItem.getChildren().add(new TreeItem<>(m)));
            });
        });
        bookStructure.getRoot().getChildren().forEach(t -> t.setExpanded(true));
        bookStructure.refresh();
        ConverterApplication.getContext().getOutputParameters().updateAuto(book.getMedia());
    }

    public void combine(ActionEvent actionEvent) {
        ObservableList<TreeTablePosition<Organisable, ?>> selectedCells = bookStructure.getSelectionModel().getSelectedCells();
        if (selectedCells.isEmpty()) return;
        List<Part> partMergers = selectedCells.stream().map(s -> s.getTreeItem().getValue()).filter(v -> (v instanceof Part)).map(c -> (Part) c).collect(Collectors.toList());
        if (partMergers.size() > 1) {
            Part recipient = partMergers.remove(0);
            recipient.combine(partMergers);
        } else {
            List<Chapter> chapterMergers = selectedCells.stream().map(s -> s.getTreeItem().getValue()).filter(v -> (v instanceof Chapter)).map(c -> (Chapter) c).collect(Collectors.toList());
            if (chapterMergers.size() > 1) {
                Chapter recipient = chapterMergers.remove(0);
                recipient.combine(chapterMergers);
            }
        }
        updateBookStructure(ConverterApplication.getContext().getBook(), bookStructure.getRoot());
    }

    public void split(ActionEvent actionEvent) {
        ObservableList<TreeTablePosition<Organisable, ?>> selectedCells = bookStructure.getSelectionModel().getSelectedCells();
        if (selectedCells.size() != 1) return;
        Organisable organisable = selectedCells.get(0).getTreeItem().getValue();
        boolean split = organisable.split();
        if (split) updateBookStructure(ConverterApplication.getContext().getBook(), bookStructure.getRoot());
    }

    public void edit(ActionEvent actionEvent) {
        ObservableList<TreeTablePosition<Organisable, ?>> selectedCells = bookStructure.getSelectionModel().getSelectedCells();
        if (selectedCells.size() != 1) return;
        Organisable organisable = selectedCells.get(0).getTreeItem().getValue();
        if (organisable instanceof Chapter) {
            new ChapterEditor((Chapter) organisable).editChapter();
            bookStructure.refresh();
        }
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

    @FXML
    protected void openLink(ActionEvent event) {
        Hyperlink source = (Hyperlink) event.getSource();
        ConverterApplication.getEnv().showDocument(source.getUserData().toString());
    }

    public void openWebSite(ActionEvent actionEvent) {
        ConverterApplication.getEnv().showDocument("https://www.recoupler.com/products/audiobookconverter");
    }

    public void openAboutPage(ActionEvent actionEvent) {
        ConverterApplication.getEnv().showDocument("https://www.recoupler.com/products/audiobookconverter/about");
    }

    public void openFAQ(ActionEvent actionEvent) {
        ConverterApplication.getEnv().showDocument("https://www.recoupler.com/products/audiobookconverter/faq");
    }

    public void openDonate() {
        ConverterApplication.getEnv().showDocument("https://www.recoupler.com/products/audiobookconverter/donate");
    }

    public void checkVersion(ActionEvent actionEvent) {
        ConverterApplication.checkNewVersion();
    }

    public void exit(ActionEvent actionEvent) {
        logger.info("Closing application");
        ConverterApplication.getContext().stopConversions();
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

    public void changeFormat(ActionEvent actionEvent) {

    }
}