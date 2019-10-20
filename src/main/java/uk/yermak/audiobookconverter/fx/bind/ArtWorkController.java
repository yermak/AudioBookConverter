package uk.yermak.audiobookconverter.fx.bind;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import uk.yermak.audiobookconverter.ArtWork;
import uk.yermak.audiobookconverter.fx.ArtWorkListCell;

/**
 * Created by yermak on 03-Dec-18.
 */
public class ArtWorkController {

    @FXML
    ListView<ArtWork> imageList;

    private ArtWorkDelegate delegate = new ArtWorkDelegate(this);

    @FXML
    private void initialize() {
        delegate.initialize();
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
