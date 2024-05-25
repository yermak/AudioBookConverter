package uk.yermak.audiobookconverter.fx;

import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.stage.FileChooser;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.yermak.audiobookconverter.AudiobookConverter;
import uk.yermak.audiobookconverter.Platform;
import uk.yermak.audiobookconverter.Settings;
import uk.yermak.audiobookconverter.book.ArtWork;
import uk.yermak.audiobookconverter.loaders.ArtWorkImage;

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

    @FXML
    private void initialize() {
        ConversionContext context = AudiobookConverter.getContext();
        imageList.setCellFactory(param -> new ArtWorkListCell());
        imageList.setItems(context.getPosters());
        context.addContextDetachListener(observable -> context.getPosters().clear());
    }

    @FXML
    private void addImage(ActionEvent actionEvent) {
        FileChooser fileChooser = new FileChooser();
        String sourceFolder = Settings.loadSetting().getSourceFolder();
        fileChooser.setInitialDirectory(Platform.getInitialDirecotory(sourceFolder));
        fileChooser.setTitle("Select JPG or PNG file");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("jpg", "*.jpg", "*.jpeg", "*.jfif"),
                new FileChooser.ExtensionFilter("png", "*.png"),
                new FileChooser.ExtensionFilter("bmp", "*.bmp"));

        File file = fileChooser.showOpenDialog(AudiobookConverter.getEnv().getWindow());
        logger.debug("Opened dialog for art image in folder: {}", sourceFolder);
        if (file != null) {
            try (var imageStream = new FileInputStream(file.getAbsolutePath())) {
                imageList.getItems().add(new ArtWorkImage(new Image(imageStream)));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            logger.info("Added art work from file: {}", file);
        }

    }

    @FXML
    private void removeImage(ActionEvent actionEvent) {
        int toRemove = imageList.getSelectionModel().getSelectedIndex();
        if (toRemove == -1) return;
        AudiobookConverter.getContext().removePoster(toRemove);
//        imageList.getItems().remove(toRemove);
        logger.info("Removed art work #{}", toRemove);
    }

    @FXML
    private void left(ActionEvent actionEvent) {
        ObservableList<Integer> selectedIndices = imageList.getSelectionModel().getSelectedIndices();
        if (selectedIndices.size() == 1) {
            Integer selected = selectedIndices.get(0);
            if (selected > 0) {
                AudiobookConverter.getContext().movePosterUp(selected);
                imageList.getSelectionModel().clearAndSelect(selected - 1);
                logger.debug("Image {} moved left", selected);
            }
        }

    }

    @FXML
    private void right(ActionEvent actionEvent) {
        ObservableList<Integer> selectedIndices = imageList.getSelectionModel().getSelectedIndices();
        if (selectedIndices.size() == 1) {
            ObservableList<ArtWork> items = imageList.getItems();
            Integer selected = selectedIndices.get(0);
            if (selected < items.size() - 1) {
                AudiobookConverter.getContext().movePosterUp(selected + 1);
                imageList.getSelectionModel().clearAndSelect(selected + 1);
                logger.debug("Image {} moved right", selected);
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
                    AudiobookConverter.getContext().addPosterIfMissingWithDelay(new ArtWorkImage(fimage));
                } else if (transferable.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                    java.util.List<String> artFiles = (java.util.List<String>) transferable.getTransferData(DataFlavor.javaFileListFlavor);

                    artFiles.stream().filter(s -> ArrayUtils.contains(ArtWork.IMAGE_EXTENSIONS, FilenameUtils.getExtension(s))).forEach(f -> {
//                        AudiobookConverter.getContext().addPosterIfMissingWithDelay(new ArtWorkBean(Utils.tempCopy(f)));
                        try (var imageStream = new FileInputStream(f)) {
                            AudiobookConverter.getContext().addPosterIfMissingWithDelay(new ArtWorkImage(new Image(imageStream)));
                        } catch (IOException e) {
                            logger.error("failed to paste image", e);
                        }
                    });
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

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            ImageIO.write((RenderedImage) image, "png", out);
            out.flush();
            try (ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray())) {
                return new javafx.scene.image.Image(in);
            }
        }
    }
}
