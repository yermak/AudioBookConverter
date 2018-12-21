package uk.yermak.audiobookconverter.fx;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import uk.yermak.audiobookconverter.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

/**
 * Created by yermak on 03-Dec-18.
 */
public class ArtWorkController implements ConversionSubscriber {

    @FXML
    private ListView imageList;

    @FXML
    private void initialize() {
        imageList.setCellFactory(param -> new ArtWorkListCell());

        ConversionContext context = ConverterApplication.getContext();
        resetForNewConversion(context.registerForConversion(this));
    }

    public void resetForNewConversion(Conversion conversion) {
        ObservableList<ArtWork> posters = FXCollections.observableArrayList();
        conversion.getBookInfo().setPosters(posters);
        imageList.setItems(posters);
    }

    @FXML
    private void addImage(ActionEvent actionEvent) {
        final FileChooser fileChooser = new FileChooser();
        String sourceFolder = AppProperties.getProperty("source.folder");
        fileChooser.setInitialDirectory(Utils.getInitialDirecotory(sourceFolder));
        fileChooser.setTitle("Select JPG or PNG file");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("jpg", "*.jpg"),
                new FileChooser.ExtensionFilter("jpg", "*.jpeg"),
                new FileChooser.ExtensionFilter("png", "*.png"),
                new FileChooser.ExtensionFilter("bmp", "*.bmp")
        );
        File file = fileChooser.showOpenDialog(ConverterApplication.getEnv().getWindow());
        if (file != null) {
            try {
                imageList.getItems().add(new ArtWorkImage(new Image(new FileInputStream(file))));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void removeImage(ActionEvent actionEvent) {
        int toRemove = imageList.getSelectionModel().getSelectedIndex();
        imageList.getItems().remove(toRemove);
    }

    @FXML
    private void left(ActionEvent actionEvent) {
        ObservableList<Integer> selectedIndices = imageList.getSelectionModel().getSelectedIndices();
        if (selectedIndices.size() == 1) {
            ObservableList<ArtWork> items = imageList.getItems();
            int selected = selectedIndices.get(0);
            if (selected > 0) {
                ArtWork upper = items.get(selected - 1);
                ArtWork lower = items.get(selected);
                items.set(selected - 1, lower);
                items.set(selected, upper);
                imageList.getSelectionModel().clearAndSelect(selected - 1);
            }
        }
    }

    @FXML
    private void right(ActionEvent actionEvent) {
        ObservableList<Integer> selectedIndices = imageList.getSelectionModel().getSelectedIndices();
        if (selectedIndices.size() == 1) {
            ObservableList<ArtWork> items = imageList.getItems();
            int selected = selectedIndices.get(0);
            if (selected < items.size() - 1) {
                ArtWork lower = items.get(selected + 1);
                ArtWork upper = items.get(selected);
                items.set(selected, lower);
                items.set(selected + 1, upper);
                imageList.getSelectionModel().clearAndSelect(selected + 1);
            }
        }
    }

    private static class ArtWorkListCell extends ListCell<ArtWork> {
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
}
