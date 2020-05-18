package uk.yermak.audiobookconverter.fx;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.util.Pair;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import uk.yermak.audiobookconverter.Chapter;
import uk.yermak.audiobookconverter.Utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

class ChapterEditor {

    private final Chapter chapter;

    public ChapterEditor(Chapter chapter) {
        this.chapter = chapter;
    }

    void editChapter() {
        Label preview = new Label(chapter.getTitle());

        Dialog<Pair<String, Boolean>> dialog = new Dialog<>();
        dialog.setTitle("Edit chapter");
        dialog.setHeaderText("Customise chapter title");

        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));


        HBox customisationBox = new HBox(15);
        grid.add(customisationBox, 0, 0);

        Map<String, Function<Chapter, Object>> context = new HashMap<>(chapter.getRenderMap());

        CheckBox bookNo = new CheckBox("Book No");
        bookNo.setSelected(context.containsKey("BOOK_NUMBER"));
        bookNo.setOnAction(event -> {
            if (bookNo.isSelected()) {
                context.put("BOOK_NUMBER", c -> String.valueOf(c.getPart().getBook().getBookInfo().getBookNumber()));
            } else {
                context.remove("BOOK_NUMBER");
            }
            Platform.runLater(() -> preview.setText(Utils.renderChapter(chapter, context)));
        });

        customisationBox.getChildren().add(bookNo);

        CheckBox bookTitle = new CheckBox("Book title");
        bookTitle.setSelected(context.containsKey("BOOK_TITLE"));
        customisationBox.getChildren().add(bookTitle);

        bookTitle.setOnAction(event -> {
            if (bookTitle.isSelected()) {
                context.put("BOOK_TITLE", c -> c.getPart().getBook().getBookInfo().getTitle());
            } else {
                context.remove("BOOK_TITLE");
            }
            Platform.runLater(() -> preview.setText(Utils.renderChapter(chapter, context)));
        });

        CheckBox chapterText = new CheckBox("\"Chapter\"");
        chapterText.setSelected(context.containsKey("CHAPTER_TEXT"));
        customisationBox.getChildren().add(chapterText);
        chapterText.setOnAction(event -> {
            if (chapterText.isSelected()) {
                context.put("CHAPTER_TEXT", c -> "Chapter");
            } else {
                context.remove("CHAPTER_TEXT");
            }
            Platform.runLater(() -> preview.setText(Utils.renderChapter(chapter, context)));
        });

        CheckBox chapterNo = new CheckBox("Chapter No");
        chapterNo.setSelected(context.containsKey("CHAPTER_NUMBER"));
        chapterNo.setOnAction(event -> {
            if (chapterNo.isSelected()) {
                context.put("CHAPTER_NUMBER", Chapter::getNumberString);
            } else {
                context.remove("CHAPTER_NUMBER");
            }
            Platform.runLater(() -> preview.setText(Utils.renderChapter(chapter, context)));
        });

        customisationBox.getChildren().add(chapterNo);

        ComboBox<String> tagsSelection = new ComboBox<>();
        tagsSelection.getItems().addAll("None", "title", "artist", "album_artist", "album", "genre", "year", "comment-0", "file_name");
        AtomicInteger selected = new AtomicInteger();
        context.keySet().stream()
                .filter(s -> s.contains("TAG"))
                .mapToInt(value -> Integer.parseInt(value.substring(4)))
                .findAny().ifPresent(selected::set);

        tagsSelection.getSelectionModel().select(selected.get());

        customisationBox.getChildren().add(new HBox(5.0, tagsSelection, new Label("MP3 tag")));

        tagsSelection.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) -> {
            switch (newValue.intValue()) {
                case 0:
                    for (int i = 1; i < 9; i++) context.remove("TAG." + i);
                    break;
                case 1:
                    for (int i = 1; i < 9; i++) context.remove("TAG." + i);
                    context.put("TAG.1", chapter -> chapter.getMedia().get(0).getBookInfo().getTitle());
                    break;
                case 2:
                    for (int i = 1; i < 9; i++) context.remove("TAG." + i);
                    context.put("TAG.2", chapter -> chapter.getMedia().get(0).getBookInfo().getWriter());
                    break;
                case 3:
                    for (int i = 1; i < 9; i++) context.remove("TAG." + i);
                    context.put("TAG.3", chapter -> chapter.getMedia().get(0).getBookInfo().getNarrator());
                    break;
                case 4:
                    for (int i = 1; i < 9; i++) context.remove("TAG." + i);
                    context.put("TAG.4", chapter -> chapter.getMedia().get(0).getBookInfo().getSeries());
                    break;
                case 5:
                    for (int i = 1; i < 9; i++) context.remove("TAG." + i);
                    context.put("TAG.5", chapter -> chapter.getMedia().get(0).getBookInfo().getGenre());
                    break;
                case 6:
                    for (int i = 1; i < 9; i++) context.remove("TAG." + i);
                    context.put("TAG.6", chapter -> chapter.getMedia().get(0).getBookInfo().getYear());
                    break;
                case 7:
                    for (int i = 1; i < 9; i++) context.remove("TAG." + i);
                    context.put("TAG.7", chapter -> chapter.getMedia().get(0).getBookInfo().getComment());
                    break;
                case 8:
                    for (int i = 1; i < 9; i++) context.remove("TAG." + i);
                    context.put("TAG.8", chapter -> FilenameUtils.getBaseName(chapter.getMedia().get(0).getFileName()));
                    break;
            }
            Platform.runLater(() -> preview.setText(Utils.renderChapter(chapter, context)));
        });

        TextField customTitle = new TextField();
        customTitle.setPromptText("custom title");
        customTitle.setText(chapter.getCustomTitle());
        customTitle.textProperty().addListener((observable, oldValue, newValue) -> {
            chapter.setCustomTitle(newValue);
            context.put("CUSTOM_TITLE", c -> newValue);
            Platform.runLater(() -> preview.setText(Utils.renderChapter(chapter, context)));
        });
        customisationBox.getChildren().add(customTitle);

        CheckBox duration = new CheckBox("Duration");
        duration.setSelected(context.containsKey("DURATION"));
        customisationBox.getChildren().add(duration);
        duration.setOnAction(event -> {
            if (duration.isSelected()) {
                context.put("DURATION", Chapter::getDuration);
            } else {
                context.remove("DURATION");
            }
            Platform.runLater(() -> preview.setText(Utils.renderChapter(chapter, context)));
        });

        grid.add(preview, 0, 1);

        CheckBox applyForAllChapters = new CheckBox("Apply for all chapters");
        grid.add(applyForAllChapters, 0, 2);

        dialog.getDialogPane().setContent(grid);

        Platform.runLater(() -> preview.setText(Utils.renderChapter(chapter, context)));

        Optional result = dialog.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK){
            chapter.getRenderMap().clear();
            chapter.getRenderMap().putAll(context);
            if (StringUtils.isNotEmpty(customTitle.getText())) {
                chapter.setCustomTitle(customTitle.getText());
                chapter.getRenderMap().put("CUSTOM_TITLE", Chapter::getCustomTitle);
            }
            if (applyForAllChapters.isSelected()) {
                List<Chapter> chapters = chapter.getPart().getBook().getChapters();
                for (Chapter c : chapters) {
                    c.getRenderMap().clear();
                    c.getRenderMap().putAll(context);
                    if (StringUtils.isNotEmpty(customTitle.getText())) {
                        c.setCustomTitle(customTitle.getText());
                        c.getRenderMap().put("CUSTOM_TITLE", Chapter::getCustomTitle);
                    }

                }
            }
        }
    }
}
