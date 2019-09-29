package uk.yermak.audiobookconverter.fx.bind;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.stage.FileChooser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.yermak.audiobookconverter.*;
import uk.yermak.audiobookconverter.fx.ArtWorkListCell;
import uk.yermak.audiobookconverter.fx.ConverterApplication;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.lang.invoke.MethodHandles;

/**
 * Created by yermak on 03-Dec-18.
 */
public class ArtWorkController {

    @FXML
    ListView<ArtWork> imageList;
    private ArtWorkDelegate delegate = new ArtWorkDelegate(this);

    @FXML
    private void initialize() {
        imageList.setCellFactory(param -> new ArtWorkListCell());
    }

    @FXML
    private void addImage(ActionEvent actionEvent) {
        delegate.addImage(actionEvent);
    }

    @FXML
    private void removeImage(ActionEvent actionEvent) {
        delegate.removeImage(actionEvent);
    }

    @FXML
    private void left(ActionEvent actionEvent) {
        delegate.left(actionEvent);
    }

    @FXML
    private void right(ActionEvent actionEvent) {
        delegate.right(actionEvent);
    }

}
