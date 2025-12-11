package uk.yermak.audiobookconverter.fx;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.stage.Window;
import org.controlsfx.control.ToggleSwitch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.yermak.audiobookconverter.Settings;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

public class SettingsDialog extends Dialog<Map<String, Object>> {
    final static Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    public static final String FILENAME_FORMAT = "filename_format";
    public static final String PART_FORMAT = "part_format";
    public static final String CHAPTER_FORMAT = "chapter_format";
    public static final String DARK_MODE = "dark_mode";
    public static final String SHOW_HINTS = "show_hints";

    @FXML
    private ToggleSwitch darkMode;
    @FXML
    private TextArea filenameFormat;
    @FXML
    private TextArea partFormat;
    @FXML
    private TextArea chapterFormat;
    @FXML
    private ToggleSwitch showHints;


    public SettingsDialog(Window window) {
        ResourceBundle resources = ResourceBundle.getBundle("locales/messages");
        setTitle(resources.getString("dialog.settings.title"));
        setHeaderText("Customize AudioBookConverter");
        getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        getDialogPane().setContent(new GridPane());

        setResultConverter(button -> {
            if (button == ButtonType.OK) {
                HashMap results = new HashMap();
                results.put(DARK_MODE, darkMode.isSelected());
                results.put(FILENAME_FORMAT, filenameFormat.getText());
                results.put(PART_FORMAT, partFormat.getText());
                results.put(CHAPTER_FORMAT, chapterFormat.getText());
                results.put(SHOW_HINTS, showHints.isSelected());
                return results;
            }
            return null;
        });
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("settings.fxml"));

        fxmlLoader.setRoot(getDialogPane().getContent());
        fxmlLoader.setController(this);
        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    @FXML
    public void initialize() {
        Settings settings = Settings.loadSetting();
        darkMode.setSelected(settings.isDarkMode());
        filenameFormat.setText(settings.getFilenameFormat());
        partFormat.setText(settings.getPartFormat());
        chapterFormat.setText(settings.getChapterFormat());
        showHints.setSelected(settings.isShowHints());
    }

}
