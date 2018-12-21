package uk.yermak.audiobookconverter.fx;

import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import org.apache.commons.lang3.StringUtils;
import uk.yermak.audiobookconverter.*;
import uk.yermak.audiobookconverter.fx.util.TextFieldValidator;

import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.Executors;

/**
 * Created by Yermak on 04-Feb-18.
 */
public class BookInfoController implements ConversionSubscriber {

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
    private AudioBookInfo bookInfo;


    @FXML
    private void initialize() {
        reloadGenres();

        MenuItem menuItem = new MenuItem("Remove");
        menuItem.setOnAction(event -> {
            genre.getItems().remove(genre.getSelectionModel().getSelectedIndex());
            saveGenres();
        });
        ContextMenu contextMenu = new ContextMenu(menuItem);

        genre.setOnContextMenuRequested(event -> {
            if (!genre.getSelectionModel().isEmpty()) {
                contextMenu.show((Node) event.getSource(), Side.RIGHT, 0, 0);
            }
            genre.hide();
        });

        Conversion conversion = ConverterApplication.getContext().registerForConversion(this);
        resetForNewConversion(conversion);

        bookNo.setTextFormatter(new TextFieldValidator(TextFieldValidator.ValidationModus.MAX_INTEGERS, 3).getFormatter());
        year.setTextFormatter(new TextFieldValidator(TextFieldValidator.ValidationModus.MAX_INTEGERS, 4).getFormatter());

        title.textProperty().addListener(o -> bookInfo.setTitle(title.getText()));
        writer.textProperty().addListener(o -> bookInfo.setWriter(writer.getText()));
        narrator.textProperty().addListener(o -> bookInfo.setNarrator(narrator.getText()));

        genre.valueProperty().addListener(o -> bookInfo.setGenre(genre.getValue()));
        genre.getEditor().textProperty().addListener(o -> bookInfo.setGenre(genre.getEditor().getText()));

        series.textProperty().addListener(o -> bookInfo.setSeries(series.getText()));
        bookNo.textProperty().addListener(o -> {
            if (StringUtils.isNotBlank(bookNo.getText())) bookInfo.setBookNumber(Integer.parseInt(bookNo.getText()));
        });
        year.textProperty().addListener(o -> bookInfo.setYear(year.getText()));
        comment.textProperty().addListener(o -> bookInfo.setComment(comment.getText()));

    }

    public void resetForNewConversion(Conversion conversion) {
        bookInfo = new AudioBookInfo();
        conversion.setBookInfo(bookInfo);

        conversion.addStatusChangeListener((observable, oldValue, newValue) -> {
            if (newValue.equals(ProgressStatus.IN_PROGRESS)) {
                saveGenres();
            }
        });

        ObservableList<MediaInfo> media = conversion.getMedia();
        media.addListener((InvalidationListener) observable -> updateTags(media, media.isEmpty()));

        conversion.addModeChangeListener((observable, oldValue, newValue) -> updateTags(media, ConversionMode.BATCH.equals(newValue)));

        clearTags();
    }

    private void saveGenres() {
        Set<String> uniqueGenres = new TreeSet<>(genre.getItems());
        if (StringUtils.isNotEmpty(genre.getEditor().getText())) {
            uniqueGenres.add(genre.getEditor().getText());
        }
        genre.getItems().clear();
        genre.getItems().addAll(uniqueGenres);
        StringBuffer sb = new StringBuffer();
        uniqueGenres.forEach(s -> sb.append(s).append("::"));
        AppProperties.setProperty("genres", sb.toString());
    }

    private void reloadGenres() {
        String genresProperty = AppProperties.getProperty("genres");
        if (genresProperty != null) {
            String[] genres = genresProperty.split("::");
            Arrays.sort(genres);
            genre.getItems().addAll(Arrays.asList(genres));
        }
    }

    private void updateTags(ObservableList<MediaInfo> media, boolean clear) {
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
