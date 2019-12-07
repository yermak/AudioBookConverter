package uk.yermak.audiobookconverter.fx;

import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import org.apache.commons.lang3.StringUtils;
import uk.yermak.audiobookconverter.AudioBookInfo;
import uk.yermak.audiobookconverter.ConversionMode;
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
        menuItem.setOnAction(event -> {
            genre.getItems().remove(genre.getSelectionModel().getSelectedIndex());
            ConverterApplication.getContext().saveGenres();
        });
        ContextMenu contextMenu = new ContextMenu(menuItem);

        genre.setOnContextMenuRequested(event -> {
            if (!genre.getSelectionModel().isEmpty()) {
                contextMenu.show((Node) event.getSource(), Side.RIGHT, 0, 0);
            }
            genre.hide();
        });

        ObservableList<MediaInfo> media = ConverterApplication.getContext().getMedia();
        media.addListener((InvalidationListener) observable -> updateTags(media, media.isEmpty()));

        ConverterApplication.getContext().addModeChangeListener((observable, oldValue, newValue) -> updateTags(media, ConversionMode.BATCH.equals(newValue)));

//        clearTags();

        SimpleObjectProperty<AudioBookInfo> bookInfo = ConverterApplication.getContext().getBookInfo();

        bookNo.setTextFormatter(new TextFieldValidator(TextFieldValidator.ValidationModus.MAX_INTEGERS, 3).getFormatter());
        year.setTextFormatter(new TextFieldValidator(TextFieldValidator.ValidationModus.MAX_INTEGERS, 4).getFormatter());

        title.textProperty().addListener(o -> bookInfo.get().setTitle(title.getText()));
        writer.textProperty().addListener(o -> bookInfo.get().setWriter(writer.getText()));
        narrator.textProperty().addListener(o -> bookInfo.get().setNarrator(narrator.getText()));

        genre.valueProperty().addListener(o -> bookInfo.get().setGenre(genre.getValue()));
        genre.getEditor().textProperty().addListener(o -> bookInfo.get().setGenre(genre.getEditor().getText()));

        series.textProperty().addListener(o -> bookInfo.get().setSeries(series.getText()));
        bookNo.textProperty().addListener(o -> {
            if (StringUtils.isNotBlank(bookNo.getText()))
                bookInfo.get().setBookNumber(Integer.parseInt(bookNo.getText()));
        });
        year.textProperty().addListener(o -> bookInfo.get().setYear(year.getText()));
        comment.textProperty().addListener(o -> bookInfo.get().setComment(comment.getText()));

        ConverterApplication.getContext().addBookInfoChangeListener((observable, oldValue, newValue) -> {
            Platform.runLater(() -> copyTags(bookInfo.get()));
        });

    }

    private void updateTags(ObservableList<MediaInfo> media, boolean clear) {
        if (ConverterApplication.getContext().getBook() == null) {
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
        title.setText(bookInfo.getTitle());
        writer.setText(bookInfo.getWriter());
        narrator.setText(bookInfo.getNarrator());
        genre.getEditor().setText(bookInfo.getGenre());
        series.setText(bookInfo.getSeries());
        bookNo.setText(String.valueOf(bookInfo.getBookNumber()));
        year.setText(bookInfo.getYear());
        comment.setText(bookInfo.getComment());
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
