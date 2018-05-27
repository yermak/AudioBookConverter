package uk.yermak.audiobookconverter.fx;

import javafx.beans.InvalidationListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import uk.yermak.audiobookconverter.AudioBookInfo;
import uk.yermak.audiobookconverter.MediaInfo;
import uk.yermak.audiobookconverter.fx.util.TextFieldValidator;

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
        AudioBookInfo bookInfo = new AudioBookInfo();
        ConverterApplication.getContext().setBookInfo(bookInfo);

        genre.getItems().addAll("Fantasy", "Sci-fi");

        bookNo.setTextFormatter(new TextFieldValidator(TextFieldValidator.ValidationModus.MAX_INTEGERS, 3).getFormatter());
        year.setTextFormatter(new TextFieldValidator(TextFieldValidator.ValidationModus.MAX_INTEGERS, 4).getFormatter());

        title.textProperty().addListener(o -> bookInfo.setTitle(title.getText()));
        writer.textProperty().addListener(o -> bookInfo.setWriter(writer.getText()));
        narrator.textProperty().addListener(o -> bookInfo.setNarrator(narrator.getText()));

        genre.valueProperty().addListener(o -> bookInfo.setGenre(genre.getValue().toString()));
        genre.getEditor().textProperty().addListener(o -> bookInfo.setGenre(genre.getEditor().getText()));

        series.textProperty().addListener(o -> bookInfo.setSeries(series.getText()));
        bookNo.textProperty().addListener(o -> bookInfo.setBookNumber(Integer.parseInt(bookNo.getText())));
        year.textProperty().addListener(o -> bookInfo.setYear(year.getText()));
        comment.textProperty().addListener(o -> bookInfo.setTitle(comment.getText()));

        ObservableList<MediaInfo> media = ConverterApplication.getContext().getConversion().getMedia();
        media.addListener((InvalidationListener) observable -> copyTags((ObservableList<MediaInfo>) observable));
    }

    private void copyTags(ObservableList<MediaInfo> media) {
        if (media.isEmpty()) return;
        MediaInfo mediaInfo = media.get(0);
        AudioBookInfo bookInfo = mediaInfo.getBookInfo();
        title.setText(bookInfo.getTitle());
        writer.setText(bookInfo.getWriter());
        narrator.setText(bookInfo.getNarrator());
        genre.getEditor().setText(bookInfo.getGenre());
        series.setText(bookInfo.getSeries());
        bookNo.setText(String.valueOf(bookInfo.getBookNumber()));
        year.setText(bookInfo.getYear());
        comment.setText(bookInfo.getComment());
    }



}
