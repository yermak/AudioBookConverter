package uk.yermak.audiobookconverter.fx;

import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.stage.FileChooser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.yermak.audiobookconverter.*;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.*;
import java.lang.invoke.MethodHandles;

/**
 * Created by yermak on 03-Dec-18.
 */
public class ArtWorkController {

    @FXML
    ListView<ArtWork> imageList;

    final static Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());


    private static final String[] IMAGE_EXTENSIONS = new String[]{"jpg", "jpeg", "jfif", "png", "bmp"};

    @FXML
    private void initialize() {
        ConversionContext context = ConverterApplication.getContext();
        imageList.setCellFactory(param -> new ArtWorkListCell());
        imageList.setItems(context.getPosters());
    }

    @FXML
    private void addImage(ActionEvent actionEvent) {
        FileChooser fileChooser = new FileChooser();
        String sourceFolder = AppProperties.getProperty("source.folder");
        fileChooser.setInitialDirectory(Utils.getInitialDirecotory(sourceFolder));
        fileChooser.setTitle("Select JPG or PNG file");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("jpg", "*.jpg", "*.jpeg", "*.jfif"),
                new FileChooser.ExtensionFilter("png", "*.png"),
                new FileChooser.ExtensionFilter("bmp", "*.bmp"));

        File file = fileChooser.showOpenDialog(ConverterApplication.getEnv().getWindow());
        logger.debug("Opened dialog for art image in folder: {}", new Object[]{sourceFolder});
        if (file != null) {
            try {
                imageList.getItems().add(new ArtWorkImage(new Image(new FileInputStream(file))));
                logger.info("Added art work from file: {}", new Object[]{file});
            } catch (FileNotFoundException e) {
                logger.error("Error during building artwork", e);
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void removeImage(ActionEvent actionEvent) {
        int toRemove = imageList.getSelectionModel().getSelectedIndex();
        imageList.getItems().remove(toRemove);
        logger.info("Removed art work #{}", toRemove);
    }

    @FXML
    private void left(ActionEvent actionEvent) {
        ObservableList selectedIndices = imageList.getSelectionModel().getSelectedIndices();
        if (selectedIndices.size() == 1) {
            ObservableList items = imageList.getItems();
            Integer selected = (Integer) selectedIndices.get(0);
            if (selected > 0) {
                moveLeft(items, selected);
                imageList.getSelectionModel().clearAndSelect(selected - 1);
                logger.debug("Image {} moved left", new Object[]{selected});
            }
        }

    }

    private ArtWork moveLeft(final ObservableList items, final Integer selected) {
        ArtWork lower = (ArtWork) items.get(selected);
        ArtWork upper = (ArtWork) items.get(selected - 1);
        items.set(selected - 1, lower);
        return (ArtWork) items.set(selected, upper);
    }

    @FXML
    private void right(ActionEvent actionEvent) {
        ObservableList selectedIndices = imageList.getSelectionModel().getSelectedIndices();
        if (selectedIndices.size() == 1) {
            ObservableList items = imageList.getItems();
            Integer selected = (Integer) selectedIndices.get(0);
            if (selected < items.size() - 1) {
                moveLeft(items, selected + 1);
                imageList.getSelectionModel().clearAndSelect(selected + 1);
                logger.debug("Image {} moved right", new Object[]{selected});
            }
        }
    }


    public void pasteImage(ActionEvent actionEvent) {
        try {
            Transferable transferable = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);
            if (transferable != null) {
                if (transferable.isDataFlavorSupported(DataFlavor.imageFlavor)) {
                    java.awt.Image image = (java.awt.Image) transferable.getTransferData(DataFlavor.imageFlavor);
                    Image fimage = awtImageToFX(image);
                    imageList.getItems().add(new ArtWorkImage(fimage));
                } else if (transferable.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                    java.util.List<String> artFiles = (java.util.List<String>) transferable.getTransferData(DataFlavor.javaFileListFlavor);
                    for (String artFile : artFiles) {
                        imageList.getItems().add(new ArtWorkImage(new Image(new FileInputStream(artFile))));
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Failed to load from clipboard", e);
            e.printStackTrace();
        }
    }

    private static javafx.scene.image.Image awtImageToFX(java.awt.Image image) throws Exception {
        if (!(image instanceof RenderedImage)) {
            BufferedImage bufferedImage = new BufferedImage(image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_INT_ARGB);
            Graphics g = bufferedImage.createGraphics();
            g.drawImage(image, 0, 0, null);
            g.dispose();
            image = bufferedImage;
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write((RenderedImage) image, "png", out);
        out.flush();
        ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
        return new javafx.scene.image.Image(in);
    }
}
