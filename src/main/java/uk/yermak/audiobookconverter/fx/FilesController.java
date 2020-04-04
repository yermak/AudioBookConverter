package uk.yermak.audiobookconverter.fx;

import javafx.beans.InvalidationListener;
import javafx.beans.property.ReadOnlyObjectWrapper;
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
import java.util.stream.Collectors;

/**
 * Created by Yermak on 04-Feb-18.
 */
public class FilesController {
    final static Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

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
    public Button importButton;

    @FXML
    public Tab chaptersTab;
    @FXML
    public TabPane filesChapters;
    @FXML
    public Tab filesTab;

    @FXML
    private CheckBox split;

    @FXML
    ListView<MediaInfo> fileList;

    @FXML
    TreeTableView<Organisable> bookStructure;
    @FXML
    private TreeTableColumn<Organisable, String> chapterColumn;
    @FXML
    private TreeTableColumn<Organisable, String> durationColumn;
    @FXML
    private TreeTableColumn<Organisable, String> detailsColumn;

    @FXML
    public Button startButton;
    @FXML
    public Button pauseButton;
    @FXML
    public Button stopButton;

    private static final String M4B = "m4b";
    private static final String M4A = "m4a";
    public static final String MP3 = "mp3";
    public static final String WMA = "wma";
    public static final String FLAC = "flac";
    public static final String OGG = "ogg";
    private final static String[] FILE_EXTENSIONS = new String[]{MP3, M4A, M4B, WMA, FLAC};

    private final ContextMenu contextMenu = new ContextMenu();
    private boolean chaptersMode = false;
//    private boolean filePerChapter;


    @FXML
    public void initialize() {

        fileList.setOnDragOver(event -> {
            if (event.getGestureSource() != fileList && event.getDragboard().hasFiles()) {
                event.acceptTransferModes(TransferMode.ANY);
            }

            event.consume();
        });

        fileList.setOnDragDropped(event -> {
            processFiles(event.getDragboard().getFiles());
            event.setDropCompleted(true);
            event.consume();
        });

        fileList.getSelectionModel().getSelectedItems().addListener(new ListChangeListener<MediaInfo>() {
            @Override
            public void onChanged(Change<? extends MediaInfo> c) {
                ConverterApplication.getContext().getSelectedMedia().clear();
                ConverterApplication.getContext().getSelectedMedia().addAll(c.getList());
            }
        });


//        fileList.setCellFactory(new ListViewListCellCallback());
        MenuItem item1 = new MenuItem("Files");
        item1.setOnAction(e -> selectFilesDialog(ConverterApplication.getEnv().getWindow()));
        MenuItem item2 = new MenuItem("Folder");
        item2.setOnAction(e -> selectFolderDialog(ConverterApplication.getEnv().getWindow()));
        contextMenu.getItems().addAll(item1, item2);

        ConversionContext context = ConverterApplication.getContext();
        ObservableList<MediaInfo> media = context.getMedia();
        fileList.setItems(media);
        fileList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);


        ObservableList<MediaInfo> selectedMedia = context.getSelectedMedia();

        selectedMedia.addListener((InvalidationListener) observable -> {
            if (selectedMedia.isEmpty()) return;
            if (!chaptersMode) {
                List<MediaInfo> change = new ArrayList<>(selectedMedia);
                List<MediaInfo> selection = new ArrayList<>(fileList.getSelectionModel().getSelectedItems());
                if (!change.containsAll(selection) || !selection.containsAll(change)) {
                    fileList.getSelectionModel().clearSelection();
                    change.forEach(m -> fileList.getSelectionModel().select(media.indexOf(m)));
                }
            } else {
//                    bookStructure.getSelectionModel().select();
            }
        });

        /* TODO fix buttons behaviour
        conversion.addStatusChangeListener((observable, oldValue, newValue) ->
                updateUI(newValue, media.isEmpty(), fileList.getSelectionModel().getSelectedIndices())
        );
*/
        //TOODO: this section may not work
  /*      media.addListener((ListChangeListener<MediaInfo>) c -> updateUI(this.conversion.getStatus(), c.getReencodingOptions().isEmpty(), fileList.getSelectionModel().getSelectedIndices()));
        if (listener != null) {
            fileList.getSelectionModel().selectedItemProperty().removeListener(listener);
        }
        listener = new MediaInfoChangeListener(conversion);
        fileList.getSelectionModel().selectedItemProperty().addListener(listener);*/

        filesChapters.getTabs().remove(chaptersTab);

        bookStructure.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        chapterColumn.setCellValueFactory(p -> new ReadOnlyObjectWrapper<>(p.getValue().getValue().getTitle()));
        detailsColumn.setCellValueFactory(p -> new ReadOnlyObjectWrapper<>(p.getValue().getValue().getDetails()));
        durationColumn.setCellValueFactory(p -> new ReadOnlyObjectWrapper<>(Utils.formatTime(p.getValue().getValue().getDuration())));
    }


    @FXML
    protected void addFiles(ActionEvent event) {
        Button node = (Button) event.getSource();
        contextMenu.show(node, Side.RIGHT, 0, 0);
    }

    private void selectFolderDialog(Window window) {
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
        }
    }


    private void processFiles(Collection<File> files) {
        List<String> fileNames = collectFiles(files);

        List<MediaInfo> addedMedia = createMediaLoader(fileNames).loadMediaInfo();
        if (chaptersMode) {
            Book book = ConverterApplication.getContext().getBook();
            Part part = new Part(book, FXCollections.observableArrayList(addedMedia.stream().map(Chapter::new).collect(Collectors.toList())));
            book.getParts().add(part);
            updateBookStructure(book, bookStructure.getRoot());
        } else {
            fileList.getItems().addAll(addedMedia);
        }
    }

    private static String[] toSuffixes(final String[] extensions) {
        final String[] suffixes = new String[extensions.length];
        for (int i = 0; i < extensions.length; i++) {
            suffixes[i] = "." + extensions[i];
        }
        return suffixes;
    }

    private List<String> collectFiles(Collection<File> files) {
        List<String> fileNames = new ArrayList<>();
        for (File file : files) {
            if (file.isDirectory()) {
                SuffixFileFilter suffixFileFilter = new SuffixFileFilter(toSuffixes(FILE_EXTENSIONS), IOCase.INSENSITIVE);
                Collection<File> nestedFiles = FileUtils.listFiles(file, suffixFileFilter, TrueFileFilter.INSTANCE);
                nestedFiles.stream().map(File::getPath).forEach(fileNames::add);
            } else {
                boolean allowedFileExtension = Arrays.asList(FILE_EXTENSIONS).contains(FilenameUtils.getExtension(file.getName()).toLowerCase());
                if (allowedFileExtension) {
                    fileNames.add(file.getPath());
                }
            }
        }
        return fileNames;
    }

    private FFMediaLoader createMediaLoader(List<String> fileNames) {
        return new FFMediaLoader(fileNames, ConverterApplication.getContext().getPlannedConversion());
    }

    private void selectFilesDialog(Window window) {
        final FileChooser fileChooser = new FileChooser();
        String sourceFolder = AppProperties.getProperty("source.folder");
        fileChooser.setInitialDirectory(Utils.getInitialDirecotory(sourceFolder));
        StringJoiner filetypes = new StringJoiner("/");

        Arrays.stream(FILE_EXTENSIONS).map(String::toUpperCase).forEach(filetypes::add);

        fileChooser.setTitle("Select " + filetypes.toString() + " files for conversion");

        for (String fileExtension : FILE_EXTENSIONS) {
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(fileExtension, "*." + fileExtension));
        }

        List<File> files = fileChooser.showOpenMultipleDialog(window);
        if (files != null) {
            processFiles(files);
            File firstFile = files.get(0);
            File parentFile = firstFile.getParentFile();
            AppProperties.setProperty("source.folder", parentFile.getAbsolutePath());
        }
    }

    public void removeFiles(ActionEvent event) {
        if (chaptersMode) {
            ObservableList<TreeTablePosition<Organisable, ?>> selectedCells = bookStructure.getSelectionModel().getSelectedCells();
            for (TreeTablePosition<Organisable, ?> selectedCell : selectedCells) {
                Organisable organisable = selectedCell.getTreeItem().getValue();
                organisable.remove();
            }
            updateBookStructure(ConverterApplication.getContext().getBook(), bookStructure.getRoot());
        } else {
            ObservableList<MediaInfo> selected = fileList.getSelectionModel().getSelectedItems();
            fileList.getItems().removeAll(selected);
        }
    }

    public void clear(ActionEvent event) {
        fileList.getItems().clear();
        ConverterApplication.getContext().resetForNewConversion();
        bookStructure.setRoot(null);
        filesChapters.getTabs().add(filesTab);
        filesChapters.getTabs().remove(chaptersTab);
        chaptersMode = false;
    }

    public void moveUp(ActionEvent event) {
        if (chaptersMode) {
            ObservableList<TreeTablePosition<Organisable, ?>> selectedCells = bookStructure.getSelectionModel().getSelectedCells();
            if (selectedCells.size() == 1) {
                Organisable organisable = selectedCells.get(0).getTreeItem().getValue();
                organisable.moveUp();
                updateBookStructure(ConverterApplication.getContext().getBook(), bookStructure.getRoot());
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
        if (chaptersMode) {
            ObservableList<TreeTablePosition<Organisable, ?>> selectedCells = bookStructure.getSelectionModel().getSelectedCells();
            if (selectedCells.size() == 1) {
                Organisable organisable = selectedCells.get(0).getTreeItem().getValue();
                organisable.moveDown();
                updateBookStructure(ConverterApplication.getContext().getBook(), bookStructure.getRoot());
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

    public void start(ActionEvent actionEvent) {
        ConversionContext context = ConverterApplication.getContext();
        if (context.getBook() == null && fileList.getItems().isEmpty()) return;

        if (context.getBook() == null) {
            context.setBook(new Book(fileList.getItems(), ConverterApplication.getContext().getBookInfo().get()));
        }

        String outputDestination = selectOutputFile(ConverterApplication.getContext().getBookInfo().get());

        if (outputDestination != null) {
            Book book = context.getBook();
            ObservableList<Part> parts = book.getParts();
            String extension = FilenameUtils.getExtension(outputDestination);
            ConverterApplication.getContext().getOutputParameters().setupFormat(extension);

            if (split.isSelected()) {
                List<Chapter> chapters = parts.stream().flatMap(p -> p.getChapters().stream()).collect(Collectors.toList());
                for (int i = 0; i < chapters.size(); i++) {
                    Chapter chapter = chapters.get(i);
                    String finalDesination = outputDestination;
                    if (chapters.size() > 1) {
                        finalDesination = finalDesination.replace("." + extension, ", Chapter " + (i + 1) + "." + extension);
                    }
                    String finalName = new File(finalDesination).getName();
                    ConversionProgress conversionProgress = new ConversionProgress(ConverterApplication.getContext().getPlannedConversion(), chapter.getMedia().size(), chapter.getDuration(), finalName);
                    context.startConversion(chapter, finalDesination, conversionProgress);
                }
            } else {
                for (int i = 0; i < parts.size(); i++) {
                    Part part = parts.get(i);
                    String finalDesination = outputDestination;
                    if (parts.size() > 1) {
                        finalDesination = finalDesination.replace("." + extension, ", Part " + (i + 1) + "." + extension);
                    }
                    String finalName = new File(finalDesination).getName();
                    ConversionProgress conversionProgress = new ConversionProgress(ConverterApplication.getContext().getPlannedConversion(), part.getMedia().size(), part.getDuration(), finalName);
                    context.startConversion(part, finalDesination, conversionProgress);
                }
            }
            ConverterApplication.getContext().resetForNewConversion();
            bookStructure.setRoot(null);
            filesChapters.getTabs().add(filesTab);
            filesChapters.getTabs().remove(chaptersTab);
            fileList.getItems().clear();
            chaptersMode = false;
        }
    }

    private static String selectOutputFile(AudioBookInfo audioBookInfo) {
        JfxEnv env = ConverterApplication.getEnv();

        final FileChooser fileChooser = new FileChooser();
        String outputFolder = AppProperties.getProperty("output.folder");
        fileChooser.setInitialDirectory(Utils.getInitialDirecotory(outputFolder));
        fileChooser.setInitialFileName(Utils.getOuputFilenameSuggestion(audioBookInfo));
        fileChooser.setTitle("Save AudioBook");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter(M4B, "*." + M4B),
                new FileChooser.ExtensionFilter(M4A, "*." + M4A),
                new FileChooser.ExtensionFilter(MP3, "*." + MP3),
                new FileChooser.ExtensionFilter(OGG, "*." + OGG)
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


    public void importChapters(ActionEvent actionEvent) {
        if (fileList.getItems().isEmpty()) {
            return;
        }
        chaptersTab.setDisable(false);
        filesChapters.getTabs().add(chaptersTab);
        filesChapters.getTabs().remove(filesTab);

        bookStructure.setShowRoot(false);


        Book book = new Book(fileList.getItems(), ConverterApplication.getContext().getBookInfo().get());


        TreeItem<Organisable> bookItem = new TreeItem<>(book);
        bookStructure.setRoot(bookItem);

        updateBookStructure(book, bookItem);

        bookItem.setExpanded(true);
        ConverterApplication.getContext().setBook(book);
        filesChapters.getSelectionModel().select(chaptersTab);
        fileList.getItems().clear();
        chaptersMode = true;
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
        organisable.split();
        updateBookStructure(ConverterApplication.getContext().getBook(), bookStructure.getRoot());
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

}