package uk.yermak.audiobookconverter.fx.bind

import java.io.File
import java.lang.invoke.MethodHandles

import javafx.stage.FileChooser
import org.slf4j.{Logger, LoggerFactory}
import uk.yermak.audiobookconverter.AppProperties
import uk.yermak.audiobookconverter.fx.ConverterApplication

object UIUtils {
  private val logger: Logger = LoggerFactory.getLogger(MethodHandles.lookup.lookupClass)


  def createFileChooser(title: String) = {
    val fileChooser = new FileChooser
    val sourceFolder = AppProperties.getProperty("source.folder")
    fileChooser.setInitialDirectory(getInitialDirecotory(sourceFolder))
    fileChooser.setTitle(title)
    fileChooser.getExtensionFilters.addAll(
      new FileChooser.ExtensionFilter("jpg", "*.jpg", "*.jpeg", "*.jfif"),
      new FileChooser.ExtensionFilter("png", "*.png"),
      new FileChooser.ExtensionFilter("bmp", "*.bmp"))
    val file = fileChooser.showOpenDialog(ConverterApplication.getEnv.getWindow)
    logger.debug("Opened dialog in folder: {}", sourceFolder)
    if (file != null) {
      logger.debug("Opened dialog in folder: {}", sourceFolder)
      file
    }
  }


  def getInitialDirecotory(sourceFolder: String): File = {
    if (sourceFolder == null) return new File(System.getProperty("user.home"))
    val file = new File(sourceFolder)
    if (file.exists) file
    else getInitialDirecotory(file.getParent)
  }

}
