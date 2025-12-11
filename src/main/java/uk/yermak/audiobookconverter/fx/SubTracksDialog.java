package uk.yermak.audiobookconverter.fx;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Spinner;
import javafx.scene.layout.GridPane;
import javafx.stage.Window;
import javafx.util.Pair;
import org.controlsfx.control.ToggleSwitch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

public class SubTracksDialog extends Dialog<Map<String, Object>> {
    final static Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    public static final String INTERVAL = "interval";
    public static final String AUTO_CHAPTERS = "autoChapters";
    public static final String REPEAT = "repeat";

    @FXML
    private Spinner<Integer> intervalSpinner;

    @FXML
    private ToggleSwitch autoChaptersToggle;

    @FXML
    private ToggleSwitch splitOnceOrRepeat;

    public SubTracksDialog(Window window) {
        ResourceBundle resources = ResourceBundle.getBundle("locales/messages");
        setTitle(resources.getString("dialog.subtracks.title"));
        setHeaderText("Split once after number of seconds \nor cut every number of seconds");
        getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        getDialogPane().setContent(new GridPane());

        setResultConverter(button -> {
            if (button == ButtonType.OK) {
                HashMap<String, Object> results = new HashMap<>();
                results.put(INTERVAL, intervalSpinner.getValue());
                results.put(AUTO_CHAPTERS, autoChaptersToggle.isSelected());
                results.put(REPEAT, splitOnceOrRepeat.isSelected());
                return results;
            }
            return null;
        });
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("subtracks.fxml"));

        fxmlLoader.setRoot(getDialogPane().getContent());
        fxmlLoader.setController(this);
        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }



}
