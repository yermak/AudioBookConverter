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
import org.apache.commons.lang3.StringUtils;
import uk.yermak.audiobookconverter.AudiobookConverter;
import uk.yermak.audiobookconverter.Settings;
import uk.yermak.audiobookconverter.book.AudioBookInfo;
import uk.yermak.audiobookconverter.book.MediaInfo;
import uk.yermak.audiobookconverter.fx.util.TextFieldValidator;

import java.util.ResourceBundle;
import java.util.concurrent.Executors;

import static javafx.geometry.HPos.RIGHT;

/**
 * Created by Yermak on 04-Feb-18.
 */
public class BookInfoController extends GridPane {

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

        Settings settings = Settings.loadSetting();
        genre.setItems(FXCollections.observableArrayList(settings.getGenres()));
        menuItem.setOnAction(event -> {
            String remove = genre.getItems().get(genre.getSelectionModel().getSelectedIndex());
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
        media.addListener((ListChangeListener<? super MediaInfo>) change -> updateTags(media, media.isEmpty()));

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
        bookNo.textProperty().addListener(o -> {
            if (StringUtils.isNotBlank(bookNo.getText()))
                AudiobookConverter.getContext().getBookInfo().bookNumber().set(bookNo.getText());
        });
        year.textProperty().addListener(o -> AudiobookConverter.getContext().getBookInfo().year().set(year.getText()));
        comment.textProperty().addListener(o -> AudiobookConverter.getContext().getBookInfo().comment().set(comment.getText()));

        AudiobookConverter.getContext().addContextDetachListener(observable -> Platform.runLater(this::clearTags));
    }

    private void updateTags(ObservableList<MediaInfo> media, boolean clear) {
        if (AudiobookConverter.getContext().getBook() == null) {
            if (clear) {
                clearTags();
            } else {
                Executors.newSingleThreadExecutor().submit(() -> {
                    //getBookInfo is proxied blocking method should be executed outside of UI thread,
                    // when info become available - scheduling update in UI thread.
                    AudioBookInfo info = media.get(0).getBookInfo();
                    Platform.runLater(() -> copyTags(info));

                });
            }
        }
    }


    private void copyTags(AudioBookInfo bookInfo) {

        title.setText(bookInfo.series().get());
        writer.setText(bookInfo.writer().get());
        narrator.setText(bookInfo.narrator().get());
        genre.getEditor().setText(bookInfo.genre().get());
        series.setText(bookInfo.series().get());
        bookNo.setText(bookInfo.bookNumber().get());
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
