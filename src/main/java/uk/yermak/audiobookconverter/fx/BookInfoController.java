package uk.yermak.audiobookconverter.fx;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import org.apache.commons.lang3.math.NumberUtils;
import uk.yermak.audiobookconverter.AudiobookConverter;
import uk.yermak.audiobookconverter.Settings;
import uk.yermak.audiobookconverter.book.AudioBookInfo;
import uk.yermak.audiobookconverter.book.MediaInfo;
import uk.yermak.audiobookconverter.fx.util.TextFieldValidator;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.Executors;

import static javafx.geometry.HPos.RIGHT;

/**
 * Created by Yermak on 04-Feb-18.
 */
public class BookInfoController extends GridPane {

    private boolean hadMedia = false;
    private final TextField title;
    private final TextField writer;
    private final TextField narrator;
    private final ComboBox<String> genre;
    private final TextField series;
    private final TextField bookNo;
    private final TextField year;
    private final TextField comment;

    public BookInfoController() {
        ResourceBundle resources = AudiobookConverter.getBundle();
        setPadding(new Insets(5, 5, 0, 5));
        setHgap(5);
        setVgap(5);

        ColumnConstraints fixed = new ColumnConstraints();
        fixed.setHalignment(RIGHT);
        ColumnConstraints grow = new ColumnConstraints();
        grow.setHgrow(javafx.scene.layout.Priority.ALWAYS);
        getColumnConstraints().addAll(fixed, grow, fixed, grow);

        title = new TextField();
        writer = new TextField();
        narrator = new TextField();
        genre = new ComboBox<>();
        genre.setEditable(true);
        series = new TextField();
        bookNo = new TextField();
        year = new TextField();
        comment = new TextField();

        addRow(0, label(resources.getString("bookinfo.label.title")), withTooltip(title, resources.getString("bookinfo.tooltip.title")),
                label(resources.getString("bookinfo.label.series")), withTooltip(series, resources.getString("bookinfo.tooltip.series")));
        addRow(1, label(resources.getString("bookinfo.label.writer")), withTooltip(writer, resources.getString("bookinfo.tooltip.author")),
                label(resources.getString("bookinfo.label.bookno")), withTooltip(bookNo, resources.getString("bookinfo.tooltip.bookno")));
        addRow(2, label(resources.getString("bookinfo.label.narrator")), withTooltip(narrator, resources.getString("bookinfo.tooltip.narrator")),
                label(resources.getString("bookinfo.label.year")), withTooltip(year, resources.getString("bookinfo.tooltip.year")));
        addRow(3, label(resources.getString("bookinfo.label.genre")), withTooltip(genre, resources.getString("bookinfo.tooltip.genre")),
                label(resources.getString("bookinfo.label.comment")), withTooltip(comment, resources.getString("bookinfo.tooltip.comment")));

        initialize();
    }

    private Label label(String text) {
        Label label = new Label(text);
        GridPane.setHalignment(label, RIGHT);
        return label;
    }

    private <T extends Node> T withTooltip(T node, String text) {
        Tooltip tooltip = new Tooltip(text);
        if (node instanceof TextField textField) {
            textField.setTooltip(tooltip);
        } else if (node instanceof ComboBox<?> comboBox) {
            comboBox.setTooltip(tooltip);
        }
        return node;
    }

    private void initialize() {

        MenuItem menuItem = new MenuItem("Remove");

        genre.setItems(FXCollections.observableArrayList(Settings.loadSetting().getGenres()));
        menuItem.setOnAction(event -> {
            String remove = genre.getItems().get(genre.getSelectionModel().getSelectedIndex());
            // fresh copy: saving the one loaded at start-up would revert every setting changed since
            Settings settings = Settings.loadSetting();
            settings.getGenres().remove(remove);
            settings.save();
            genre.getItems().remove(remove);
        });
        AudiobookConverter.getContext().addContextDetachListener(observable -> genre.setItems(FXCollections.observableArrayList(Settings.loadSetting().getGenres())));

        ContextMenu contextMenu = new ContextMenu(menuItem);

        genre.setOnContextMenuRequested(event -> {
            if (!genre.getSelectionModel().isEmpty()) {
                contextMenu.show((Node) event.getSource(), Side.RIGHT, 0, 0);
            }
            genre.hide();
        });

        ObservableList<MediaInfo> media = AudiobookConverter.getContext().getMedia();
//        media.addListener((InvalidationListener) observable -> updateTags(media, media.isEmpty()));
        // fill from tags only when the first files arrive: adding, removing or moving files later keeps what the user typed
        media.addListener((ListChangeListener<? super MediaInfo>) change -> {
            if (media.isEmpty() || !hadMedia) updateTags(media, media.isEmpty());
            hadMedia = !media.isEmpty();
        });

//        ConverterApplication.getContext().addModeChangeListener((observable, oldValue, newValue) -> updateTags(media, ConversionMode.BATCH.equals(newValue)));

//        clearTags();

        bookNo.setTextFormatter(new TextFieldValidator(TextFieldValidator.ValidationModus.MAX_INTEGERS, 3).getFormatter());
        year.setTextFormatter(new TextFieldValidator(TextFieldValidator.ValidationModus.MAX_INTEGERS, 4).getFormatter());

        title.textProperty().addListener(o -> AudiobookConverter.getContext().getBookInfo().title().set(title.getText()));

        writer.textProperty().addListener(o -> AudiobookConverter.getContext().getBookInfo().writer().set(writer.getText()));
        narrator.textProperty().addListener(o -> AudiobookConverter.getContext().getBookInfo().narrator().set(narrator.getText()));

        genre.valueProperty().addListener(o -> AudiobookConverter.getContext().getBookInfo().genre().set(genre.getValue()));
        genre.getEditor().textProperty().addListener(o -> AudiobookConverter.getContext().getBookInfo().genre().set(genre.getEditor().getText()));

        series.textProperty().addListener(o -> AudiobookConverter.getContext().getBookInfo().series().set(series.getText()));
        bookNo.textProperty().addListener(o -> AudiobookConverter.getContext().getBookInfo().bookNumber().set(bookNo.getText()));
        year.textProperty().addListener(o -> AudiobookConverter.getContext().getBookInfo().year().set(year.getText()));
        comment.textProperty().addListener(o -> AudiobookConverter.getContext().getBookInfo().comment().set(comment.getText()));

        AudiobookConverter.getContext().addContextDetachListener(observable -> Platform.runLater(this::clearTags));
    }

    private void updateTags(ObservableList<MediaInfo> media, boolean clear) {
        if (AudiobookConverter.getContext().getBook() == null) {
            if (clear) {
                clearTags();
            } else {
                List<MediaInfo> batch = List.copyOf(media);
                Executors.newSingleThreadExecutor().submit(() -> {
                    //getBookInfo is proxied blocking method should be executed outside of UI thread,
                    // when info become available - scheduling update in UI thread.
                    List<AudioBookInfo> infos = new ArrayList<>();
                    for (MediaInfo file : batch) {
                        try {
                            infos.add(file.getBookInfo());
                        } catch (Exception e) {
                            // failed to load: already logged, and left out of the book with a warning
                        }
                    }
                    if (infos.isEmpty()) return;
                    Platform.runLater(() -> copyTags(infos.getFirst(), bookNumber(infos)));
                });
            }
        }
    }


    /**
     * The book number is read from the track tag. The m4b and ogg output write the book number there, without a total,
     * so a whole book, or every part of one, carries the same number. Chapter files carry their position instead
     * (1, 2, 3 or 1/32): taken as the book number, every chapter of the result showed track 1.
     */
    static String bookNumber(List<AudioBookInfo> files) {
        if (files.stream().anyMatch(f -> NumberUtils.toInt(f.totalTracks().get()) > 1)) return "";
        List<String> numbers = files.stream().map(f -> f.bookNumber().get()).distinct().toList();
        return numbers.size() == 1 ? numbers.getFirst() : "";
    }

    private void copyTags(AudioBookInfo bookInfo, String bookNumber) {

        title.setText(bookInfo.series().get());
        writer.setText(bookInfo.writer().get());
        narrator.setText(bookInfo.narrator().get());
        genre.getEditor().setText(bookInfo.genre().get());
        series.setText(bookInfo.series().get());
        bookNo.setText(bookNumber);
        year.setText(bookInfo.year().get());
        comment.setText(bookInfo.comment().get());
    }

    private void clearTags() {
        title.setText("");
        writer.setText("");
        narrator.setText("");
        genre.getEditor().setText("");
        series.setText("");
        bookNo.setText("");
        year.setText("");
        comment.setText("");
    }

}
