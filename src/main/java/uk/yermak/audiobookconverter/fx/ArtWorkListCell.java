package uk.yermak.audiobookconverter.fx;

import javafx.scene.control.ListCell;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import uk.yermak.audiobookconverter.book.ArtWork;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class ArtWorkListCell extends ListCell<ArtWork> {
    private final ImageView imageView = new ImageView();

    @Override
    public void updateItem(ArtWork artWork, boolean empty) {
        super.updateItem(artWork, empty);
        if (empty) {
            setText(null);
            setGraphic(null);
        } else {
            try {
                Image image = new Image(new FileInputStream(artWork.getFileName()));
                imageView.setImage(image);
                imageView.setFitHeight(110);
                imageView.setPreserveRatio(true);
                imageView.setSmooth(true);
                double height = image.getHeight();
                double width = image.getWidth();
                setTooltip(new Tooltip("["+Math.round(width) + "x" + Math.round(height)+"] - "+artWork.getFileName()));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            setGraphic(imageView);
        }
    }


}
