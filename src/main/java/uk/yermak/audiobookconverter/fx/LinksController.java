package uk.yermak.audiobookconverter.fx;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Hyperlink;

/**
 * Created by Yermak on 04-Feb-18.
 */
public class LinksController {
    @FXML
    protected void openLink(ActionEvent event) {
        Hyperlink source = (Hyperlink) event.getSource();
        ConverterApplication.getEnv().showDocument(source.getTooltip().getText());
    }
}
