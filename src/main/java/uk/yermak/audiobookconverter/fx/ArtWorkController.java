package uk.yermak.audiobookconverter.fx;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import uk.yermak.audiobookconverter.ConversionContext;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

/**
 * Created by yermak on 03-Dec-18.
 */
public class ArtWorkController {

    @FXML
    private ListView imageList;
    @FXML
    private ImageView artWork;

    @FXML
    private void initialize() {
        ConversionContext context = ConverterApplication.getContext();

        ObservableList<String> items = FXCollections.observableArrayList(
                "RUBY", "APPLE"
        );
        imageList.setItems(items);

        imageList.setCellFactory(param -> new ListCell<String>() {
            private ImageView imageView = new ImageView();

            @Override
            public void updateItem(String name, boolean empty) {
                super.updateItem(name, empty);
                if (empty) {
                    setText(null);
                    setGraphic(null);
                } else {
                    try {
                        imageView.setImage(new Image(new FileInputStream("C:\\TEMP\\aclass\\NexusOne\\P1130003.JPG ")));
                        imageView.setFitWidth(120);
                        imageView.setPreserveRatio(true);
                        imageView.setSmooth(true);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    setGraphic(imageView);
                }
            }
        });

    }

    @FXML
    private void addImage(ActionEvent actionEvent) {

    }

    @FXML
    private void removeImage(ActionEvent actionEvent) {

    }

    @FXML
    private void left(ActionEvent actionEvent) {

    }

    @FXML
    private void right(ActionEvent actionEvent) {

    }
}
