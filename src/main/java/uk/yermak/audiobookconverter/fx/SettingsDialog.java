package uk.yermak.audiobookconverter.fx;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.HPos;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.TextArea;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.Priority;
import javafx.stage.Window;
import org.controlsfx.control.ToggleSwitch;
import uk.yermak.audiobookconverter.Settings;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

public class SettingsDialog extends Dialog<Map<String, Object>> {
    public static final String FILENAME_FORMAT = "filename_format";
    public static final String PART_FORMAT = "part_format";
    public static final String CHAPTER_FORMAT = "chapter_format";
    public static final String DARK_MODE = "dark_mode";
    public static final String SHOW_HINTS = "show_hints";
    public static final String LANGUAGE = "language";

    private static final List<LocaleOption> SUPPORTED_LOCALES = List.of(
            new LocaleOption("", "settings.language.default"),
            new LocaleOption("en", "settings.language.en"),
            new LocaleOption("de", "settings.language.de"),
            new LocaleOption("es", "settings.language.es"),
            new LocaleOption("fr", "settings.language.fr"),
            new LocaleOption("it", "settings.language.it"),
            new LocaleOption("pt", "settings.language.pt"),
            new LocaleOption("nl", "settings.language.nl"),
            new LocaleOption("ru", "settings.language.ru"),
            new LocaleOption("ua", "settings.language.ua"),
            new LocaleOption("tr", "settings.language.tr"),
            new LocaleOption("ro", "settings.language.ro"),
            new LocaleOption("pl", "settings.language.pl"),
            new LocaleOption("bg", "settings.language.bg"),
            new LocaleOption("by", "settings.language.by"),
            new LocaleOption("kz", "settings.language.kz"),
            new LocaleOption("gr", "settings.language.gr")
    );

    private final ResourceBundle resources;
    private ToggleSwitch darkMode;
    private TextArea filenameFormat;
    private TextArea partFormat;
    private TextArea chapterFormat;
    private ToggleSwitch showHints;
    private ComboBox<LocaleOption> language;

    public SettingsDialog(Window window) {
        resources = ResourceBundle.getBundle("locales/messages");
        setTitle(resources.getString("dialog.settings.title"));
        setHeaderText(resources.getString("dialog.settings.header"));
        getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        GridPane content = buildContent();
        getDialogPane().setContent(content);

        setResultConverter(button -> {
            if (button == ButtonType.OK) {
                HashMap results = new HashMap();
                results.put(DARK_MODE, darkMode.isSelected());
                results.put(FILENAME_FORMAT, filenameFormat.getText());
                results.put(PART_FORMAT, partFormat.getText());
                results.put(CHAPTER_FORMAT, chapterFormat.getText());
                results.put(SHOW_HINTS, showHints.isSelected());
                LocaleOption selectedLocale = language.getSelectionModel().getSelectedItem();
                results.put(LANGUAGE, selectedLocale != null ? selectedLocale.code() : "");
                return results;
            }
            return null;
        });
        initialize();
    }

    public void initialize() {
        Settings settings = Settings.loadSetting();
        darkMode.setSelected(settings.isDarkMode());
        filenameFormat.setText(settings.getFilenameFormat());
        partFormat.setText(settings.getPartFormat());
        chapterFormat.setText(settings.getChapterFormat());
        showHints.setSelected(settings.isShowHints());

        ObservableList<LocaleOption> options = FXCollections.observableArrayList(SUPPORTED_LOCALES);
        language.setItems(options);
        language.setCellFactory(listView -> new LocaleOptionCell());
        language.setButtonCell(new LocaleOptionCell());
        String savedLanguage = settings.getLanguage();
        LocaleOption toSelect = options.stream()
                .filter(option -> option.code().equals(savedLanguage))
                .findFirst()
                .orElse(options.get(0));
        language.getSelectionModel().select(toSelect);
    }

    private GridPane buildContent() {
        GridPane grid = new GridPane();
        grid.setHgap(5);
        grid.setVgap(10);

        ColumnConstraints firstColumn = new ColumnConstraints();
        firstColumn.setHgrow(Priority.ALWAYS);
        ColumnConstraints secondColumn = new ColumnConstraints();
        secondColumn.setHgrow(Priority.NEVER);
        grid.getColumnConstraints().addAll(firstColumn, secondColumn);

        darkMode = new ToggleSwitch();
        filenameFormat = new TextArea();
        partFormat = new TextArea();
        chapterFormat = new TextArea();
        showHints = new ToggleSwitch();
        language = new ComboBox<>();

        addToggleRow(grid, 0, resources.getString("settings.dark_theme"), darkMode);

        addSeparator(grid, 1);

        addTextAreaRow(grid, 2, resources.getString("settings.filename_template.book"), filenameFormat);
        addTextAreaRow(grid, 4, resources.getString("settings.filename_template.book_part"), partFormat);
        addTextAreaRow(grid, 6, resources.getString("settings.filename_template.chapter"), chapterFormat);

        addToggleRow(grid, 8, resources.getString("settings.show_tips"), showHints);

        Label languageLabel = new Label(resources.getString("settings.language"));
        GridPane.setHalignment(languageLabel, HPos.LEFT);
        grid.add(languageLabel, 0, 9);

        language.setPrefWidth(250);
        GridPane.setHalignment(language, HPos.RIGHT);
        grid.add(language, 1, 9);

        return grid;
    }

    private void addToggleRow(GridPane grid, int rowIndex, String labelText, ToggleSwitch toggle) {
        Label label = new Label(labelText);
        GridPane.setHalignment(label, HPos.LEFT);
        grid.add(label, 0, rowIndex);

        GridPane.setHalignment(toggle, HPos.RIGHT);
        grid.add(toggle, 1, rowIndex);
    }

    private void addTextAreaRow(GridPane grid, int startRow, String labelText, TextArea textArea) {
        Label label = new Label(labelText);
        GridPane.setHalignment(label, HPos.LEFT);
        grid.add(label, 0, startRow, 2, 1);

        textArea.setWrapText(true);
        textArea.setMaxHeight(60);
        textArea.setMinWidth(400);
        grid.add(textArea, 0, startRow + 1, 2, 1);
    }

    private void addSeparator(GridPane grid, int rowIndex) {
        javafx.scene.control.Separator separator = new javafx.scene.control.Separator();
        grid.add(separator, 0, rowIndex, 2, 1);
        RowConstraints separatorRow = new RowConstraints();
        grid.getRowConstraints().add(separatorRow);
    }

    private class LocaleOptionCell extends ListCell<LocaleOption> {
        @Override
        protected void updateItem(LocaleOption item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setText(null);
            } else {
                setText(resources.getString(item.labelKey()));
            }
        }
    }

    private record LocaleOption(String code, String labelKey) {
    }

}
