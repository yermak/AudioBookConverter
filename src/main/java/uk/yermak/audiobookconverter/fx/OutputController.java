package uk.yermak.audiobookconverter.fx;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Slider;
import javafx.scene.control.Spinner;

/**
 * Created by yermak on 08/09/2018.
 */
public class OutputController {
    @FXML
    private Spinner bitRate;
    @FXML
    private Spinner frequency;
    @FXML
    private Spinner channels;
    @FXML
    private Slider quality;
    @FXML
    private RadioButton cbr;
    @FXML
    private RadioButton vbr;
    @FXML
    private CheckBox auto;
    @FXML
    private Spinner parts;

    public void cbr(ActionEvent actionEvent) {

    }

    public void vbr(ActionEvent actionEvent) {

    }

    @FXML
    private void initialize() {
        auto.setSelected(true);
        parts.getValueFactory().setValue(2);

    }
}
