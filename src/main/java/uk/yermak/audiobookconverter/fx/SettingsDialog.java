package uk.yermak.audiobookconverter.fx;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Spinner;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Window;
import javafx.util.Pair;
import org.controlsfx.control.PropertySheet;
import org.controlsfx.control.ToggleSwitch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.yermak.audiobookconverter.AppSetting;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.Collections;
import java.util.Map;

public class SettingsDialog extends Dialog<Map<String, Object>> {
    final static Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @FXML
    private ToggleSwitch darkMode;


    public SettingsDialog(Window window) {
        setTitle("AudioBookConverter Settings");
        setHeaderText("Customize AudioBookConverter");
        getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        getDialogPane().setContent(new GridPane());



        setResultConverter(button -> {
            if (button == ButtonType.OK) {
                return Collections.singletonMap(AppSetting.DARK_MODE, darkMode.isSelected());
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
        darkMode.setSelected(Boolean.parseBoolean(AppSetting.getProperty(AppSetting.DARK_MODE, Boolean.FALSE.toString())));
    }

}
