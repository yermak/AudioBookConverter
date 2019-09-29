package uk.yermak.audiobookconverter.fx;

import javafx.scene.control.ListCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import uk.yermak.audiobookconverter.ArtWork;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class ArtWorkListCell extends ListCell<ArtWork> {
    private ImageView imageView = new ImageView();

    @Override
    public void updateItem(ArtWork artWork, boolean empty) {
        super.updateItem(artWork, empty);
        if (empty) {
            setText(null);
            setGraphic(null);
        } else {
            try {
                imageView.setImage(new Image(new FileInputStream(artWork.getFileName())));
                imageView.setFitHeight(110);
                imageView.setPreserveRatio(true);
                imageView.setSmooth(true);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            setGraphic(imageView);
        }
    }
}
