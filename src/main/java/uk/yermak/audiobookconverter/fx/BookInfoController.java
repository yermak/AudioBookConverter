package uk.yermak.audiobookconverter.fx;

import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import org.apache.commons.lang3.StringUtils;
import uk.yermak.audiobookconverter.AppProperties;
import uk.yermak.audiobookconverter.AudioBookInfo;
import uk.yermak.audiobookconverter.AudiobookConverter;
import uk.yermak.audiobookconverter.MediaInfo;
import uk.yermak.audiobookconverter.fx.util.TextFieldValidator;

import java.util.concurrent.Executors;

/**
 * Created by Yermak on 04-Feb-18.
 */
public class BookInfoController {

    @FXML
    private TextField title;
    @FXML
    private TextField writer;
    @FXML
    private TextField narrator;
    @FXML
    private ComboBox<String> genre;
    @FXML
    private TextField series;
    @FXML
    private TextField bookNo;
    @FXML
    private TextField year;
    @FXML
    private TextField comment;

    @FXML
    private void initialize() {

        MenuItem menuItem = new MenuItem("Remove");

        genre.setItems(AppProperties.loadGenres());
        menuItem.setOnAction(event -> {
            String remove = genre.getItems().get(genre.getSelectionModel().getSelectedIndex());
            AppProperties.removeGenre(remove);
            genre.setItems(AppProperties.loadGenres());
        });
        AudiobookConverter.getContext().addContextDetachListener(observable -> genre.setItems(AppProperties.loadGenres()));

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
        if (bookInfo.bookNumber().get() != 0) {
            bookNo.setText(bookInfo.bookNumber().toString());
        } else{
            bookNo.setText("");
        }
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
