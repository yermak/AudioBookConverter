package uk.yermak.audiobookconverter.fx;

import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.VPos;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import javafx.stage.Screen;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.yermak.audiobookconverter.AudiobookConverter;
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
import java.text.MessageFormat;
import java.util.ResourceBundle;

/**
 * Created by yermak on 03-Dec-18.
 */
public class ArtWorkController extends GridPane {

    final static Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final ListView<ArtWork> imageList;
    private final ResourceBundle resources;

    public ArtWorkController() {
        resources = AudiobookConverter.getBundle();

        setPadding(new Insets(5, 5, 0, 5));
        setVgap(5);
        setPrefHeight(Screen.getPrimary().getVisualBounds().getHeight() * 0.03);

        ColumnConstraints grow = new ColumnConstraints();
        grow.setHgrow(javafx.scene.layout.Priority.ALWAYS);
        ColumnConstraints fixed = new ColumnConstraints();
        ColumnConstraints fixed2 = new ColumnConstraints();
        getColumnConstraints().addAll(grow, fixed, fixed2);

        ConversionContext context = AudiobookConverter.getContext();

        imageList = new ListView<>();
        imageList.setOrientation(Orientation.HORIZONTAL);
        imageList.setCellFactory(param -> new ArtWorkListCell());
        imageList.setItems(context.getPosters());
        imageList.setTooltip(new Tooltip(resources.getString("artwork.tooltip.list")));
        add(imageList, 0, 0, 1, 4);
        context.addContextDetachListener(observable -> context.getPosters().clear());

        double screenWidth = Screen.getPrimary().getVisualBounds().getWidth();

        Button addButton = new Button(resources.getString("artwork.button.add"));
        addButton.setMinWidth(screenWidth * 0.05);
        addButton.setTooltip(new Tooltip(resources.getString("artwork.tooltip.add")));
        addButton.setOnAction(this::addImage);
        setHalignment(addButton, HPos.CENTER);
        add(addButton, 1, 0, 2, 1);

        Button pasteButton = new Button(resources.getString("artwork.button.paste"));
        pasteButton.setMinWidth(screenWidth * 0.05);
        pasteButton.setTooltip(new Tooltip(resources.getString("artwork.tooltip.paste")));
        pasteButton.setOnAction(this::pasteImage);
        setHalignment(pasteButton, HPos.CENTER);
        add(pasteButton, 1, 1, 2, 1);

        Button removeButton = new Button(resources.getString("artwork.button.remove"));
        removeButton.setMinWidth(screenWidth * 0.05);
        removeButton.setTooltip(new Tooltip(resources.getString("artwork.tooltip.remove")));
        removeButton.setOnAction(this::removeImage);
        setHalignment(removeButton, HPos.CENTER);
        add(removeButton, 1, 2, 2, 1);

        Button left = new Button("⬅");
        left.setMinWidth(screenWidth * 0.025);
        left.setTooltip(new Tooltip(resources.getString("artwork.tooltip.moveleft")));
        left.setOnAction(this::left);
        setHalignment(left, HPos.LEFT);
        setValignment(left, VPos.CENTER);
        add(left, 1, 3);

        Button right = new Button("➡");
        right.setMinWidth(screenWidth * 0.025);
        right.setTooltip(new Tooltip(resources.getString("artwork.tooltip.moveright")));
        right.setOnAction(this::right);
        setHalignment(right, HPos.RIGHT);
        setValignment(right, VPos.CENTER);
        add(right, 2, 3);
    }

    private void addImage(ActionEvent actionEvent) {
        FileChooser fileChooser = new FileChooser();
        File sourceFolder = Settings.loadSetting().getSourceFolder();
        fileChooser.setInitialDirectory(sourceFolder);
        String fileTypes = "JPG, PNG, BMP";
        String title = resources != null
                ? MessageFormat.format(resources.getString("chooser.select_image"), fileTypes)
                : MessageFormat.format("Select {0} file", fileTypes);
        fileChooser.setTitle(title);
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

    private void removeImage(ActionEvent actionEvent) {
        int toRemove = imageList.getSelectionModel().getSelectedIndex();
        if (toRemove == -1) return;
        AudiobookConverter.getContext().removePoster(toRemove);
//        imageList.getItems().remove(toRemove);
        logger.info("Removed art work #{}", toRemove);
    }

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
