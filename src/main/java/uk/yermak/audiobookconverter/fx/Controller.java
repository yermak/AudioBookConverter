package uk.yermak.audiobookconverter.fx;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Hyperlink;

/**
 * Created by Yermak on 06-Jan-18.
 */
public class Controller {

    @FXML
    protected void openLink(ActionEvent event) {
        Hyperlink source = (Hyperlink) event.getSource();
        ConverterApplication.instance.getHostServices().showDocument(source.getTooltip().getText());
    }


}
