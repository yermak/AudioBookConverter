package uk.yermak.audiobookconverter.fx.bind

import java.io.{FileInputStream, FileNotFoundException}
import java.lang.invoke.MethodHandles

import javafx.collections.{FXCollections, ObservableList}
import javafx.event.ActionEvent
import javafx.scene.image.Image
import javafx.stage.FileChooser
import org.slf4j.{Logger, LoggerFactory}
import uk.yermak.audiobookconverter._
import uk.yermak.audiobookconverter.fx.ConverterApplication

class ArtWorkDelegate(var controller: ArtWorkController) extends ConversionSubscriber {
  private val logger: Logger = LoggerFactory.getLogger(MethodHandles.lookup.lookupClass)

  private[bind] def initialize(): Unit = {
    val context = ConverterApplication.getContext
    resetForNewConversion(context.registerForConversion(this))
  }

  override def resetForNewConversion(conversion: Conversion): Unit = {
    val posters = FXCollections.observableArrayList[ArtWork]
    conversion.getBookInfo.setPosters(posters)
    controller.imageList.setItems(posters)
  }

  private[bind] def addImage(actionEvent: ActionEvent): Unit = {
    val fileChooser = new FileChooser
    val sourceFolder = AppProperties.getProperty("source.folder")
    fileChooser.setInitialDirectory(Utils.getInitialDirecotory(sourceFolder))
    fileChooser.setTitle("Select JPG or PNG file")
    fileChooser.getExtensionFilters.addAll(
      new FileChooser.ExtensionFilter("jpg", "*.jpg", "*.jpeg", "*.jfif"),
      new FileChooser.ExtensionFilter("png", "*.png"),
      new FileChooser.ExtensionFilter("bmp", "*.bmp"))
    val file = fileChooser.showOpenDialog(ConverterApplication.getEnv.getWindow)
    logger.debug("Opened dialog for art image in folder: {}", sourceFolder)
    if (file != null) try {
      controller.imageList.getItems.add(new ArtWorkImage(new Image(new FileInputStream(file))))
      logger.info("Added art work from file: {}", file)
    } catch {
      case e: FileNotFoundException =>
        logger.error("Error during building artwork", e)
        e.printStackTrace()
    }
  }

  private[bind] def removeImage(actionEvent: ActionEvent): Unit = {
    val toRemove = controller.imageList.getSelectionModel.getSelectedIndex
    controller.imageList.getItems.remove(toRemove)
    logger.info("Removed art work #{}", toRemove)
  }

  private[bind] def left(actionEvent: ActionEvent): Unit = {
    val selectedIndices = controller.imageList.getSelectionModel.getSelectedIndices
    if (selectedIndices.size == 1) {
      val items = controller.imageList.getItems
      val selected = selectedIndices.get(0)
      if (selected > 0) {
        moveLeft(items, selected)
        controller.imageList.getSelectionModel.clearAndSelect(selected - 1)
        logger.debug("Image {} moved left", selected)
      }
    }
  }

  private def moveLeft(items: ObservableList[ArtWork], selected: Integer) = {
    val lower = items.get(selected)
    val upper = items.get(selected - 1)
    items.set(selected - 1, lower)
    items.set(selected, upper)
  }

  private[bind] def right(actionEvent: ActionEvent): Unit = {
    val selectedIndices = controller.imageList.getSelectionModel.getSelectedIndices
    if (selectedIndices.size == 1) {
      val items = controller.imageList.getItems
      val selected = selectedIndices.get(0)
      if (selected < items.size - 1) {
        moveLeft(items, selected + 1)
        controller.imageList.getSelectionModel.clearAndSelect(selected + 1)
        logger.debug("Image {} moved right", selected)
      }
    }
  }

}