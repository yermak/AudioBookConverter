package uk.yermak.audiobookconverter.fx;


import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;

import java.io.IOException;

/**
 * Created by yermak on 23-Feb-18.
 */
public class FolderDialog extends Dialog {


    public FolderDialog() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(
                "folder_dialog.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);
        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }

        ButtonType cancel = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        ButtonType ok = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);

        this.getDialogPane().getButtonTypes().add(cancel);
        this.getDialogPane().getButtonTypes().add(ok);
    }

    public void selectSourceDirectory(ActionEvent actionEvent) {

    }

    public void selectOtherDirectory(ActionEvent actionEvent) {

    }
}
