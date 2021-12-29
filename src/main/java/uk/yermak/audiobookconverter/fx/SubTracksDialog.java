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

public class SubTracksDialog extends Dialog<Pair<Integer, Boolean>> {
    final static Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @FXML
    private Spinner<Integer> intervalSpinner;

    @FXML
    private ToggleSwitch autoChaptersToggle;

    public SubTracksDialog(Window window) {
        setTitle("Create sub-tracks");
        setHeaderText("Sub-tracks for every period");
        getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        getDialogPane().setContent(new GridPane());

        setResultConverter(button -> {
            if (button == ButtonType.OK) {
                return new Pair<>(intervalSpinner.getValue(), autoChaptersToggle.isSelected());
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
