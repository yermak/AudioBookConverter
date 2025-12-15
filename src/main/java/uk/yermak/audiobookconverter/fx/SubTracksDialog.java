package uk.yermak.audiobookconverter.fx;

import javafx.geometry.HPos;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.stage.Window;
import org.controlsfx.control.ToggleSwitch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

public class SubTracksDialog extends Dialog<Map<String, Object>> {
    final static Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    public static final String INTERVAL = "interval";
    public static final String AUTO_CHAPTERS = "autoChapters";
    public static final String REPEAT = "repeat";

    private Spinner<Integer> intervalSpinner;
    private ToggleSwitch autoChaptersToggle;
    private ToggleSwitch splitOnceOrRepeat;

    public SubTracksDialog(Window window) {
        ResourceBundle resources = ResourceBundle.getBundle("locales/messages");
        setTitle(resources.getString("dialog.subtracks.title"));
        setHeaderText(resources.getString("dialog.subtracks.header"));
        getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        GridPane content = createContent(resources);
        getDialogPane().setContent(content);

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
    }

    private GridPane createContent(ResourceBundle resources) {
        GridPane gridPane = new GridPane();
        gridPane.setHgap(5);
        gridPane.setVgap(2);

        ColumnConstraints firstColumn = new ColumnConstraints();
        firstColumn.setHgrow(Priority.ALWAYS);
        ColumnConstraints secondColumn = new ColumnConstraints();
        secondColumn.setHgrow(Priority.NEVER);
        gridPane.getColumnConstraints().addAll(firstColumn, secondColumn);

        Label intervalLabel = new Label(resources.getString("dialog.subtracks.interval"));
        intervalSpinner = new Spinner<>(1, 86400, 600);
        intervalSpinner.setEditable(true);
        GridPane.setHalignment(intervalSpinner, HPos.RIGHT);

        Label repeatLabel = new Label(resources.getString("dialog.subtracks.multiple_segments"));
        splitOnceOrRepeat = new ToggleSwitch();
        splitOnceOrRepeat.setSelected(true);
        GridPane.setHalignment(splitOnceOrRepeat, HPos.RIGHT);

        Label autoChaptersLabel = new Label(resources.getString("dialog.subtracks.auto_chapters"));
        autoChaptersToggle = new ToggleSwitch();
        autoChaptersToggle.setSelected(true);
        GridPane.setHalignment(autoChaptersToggle, HPos.RIGHT);

        gridPane.add(intervalLabel, 0, 0);
        gridPane.add(intervalSpinner, 1, 0);
        gridPane.add(repeatLabel, 0, 1);
        gridPane.add(splitOnceOrRepeat, 1, 1);
        gridPane.add(autoChaptersLabel, 0, 2);
        gridPane.add(autoChaptersToggle, 1, 2);

        return gridPane;
    }
}
