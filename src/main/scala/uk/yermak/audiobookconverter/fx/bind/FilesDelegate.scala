package uk.yermak.audiobookconverter.fx.bind

import java.io.File
import java.util
import java.util.StringJoiner

import javafx.application.Platform
import javafx.beans.{InvalidationListener, Observable}
import javafx.beans.value.{ChangeListener, ObservableValue}
import javafx.collections.ObservableList
import javafx.event.ActionEvent
import javafx.geometry.Side
import javafx.scene.control.{Button, MenuItem, SelectionMode}
import javafx.scene.input.{DragEvent, TransferMode}
import javafx.stage.{DirectoryChooser, FileChooser, Window}
import org.apache.commons.io.{FileUtils, FilenameUtils}
import org.apache.commons.lang3.StringUtils
import org.controlsfx.control.Notifications
import uk.yermak.audiobookconverter.fx.{ConversionProgress, ConverterApplication}
import uk.yermak.audiobookconverter._

class FilesDelegate(controller: FilesController) extends ConversionSubscriber {
  private val M4B = "m4b"
  private val FILE_EXTENSIONS = Array[String]("mp3", "m4a", M4B, "wma")

  private var conversion: Conversion = null
  private var selectedMedia: ObservableList[MediaInfo] = null
  private var listener: MediaInfoChangeListener = null

  private[bind] def initialize(): Unit = {
    controller.fileList.setOnDragOver((event: DragEvent) => {
      def foo(event: DragEvent) = {
        if ((event.getGestureSource ne controller.fileList) && event.getDragboard.hasFiles) {
          event.acceptTransferModes(TransferMode.COPY, TransferMode.MOVE)
        }
        event.consume()
      }

      foo(event)
    })
    controller.fileList.setOnDragDropped((event: DragEvent) => {
      def foo(event: DragEvent) = {
        import scala.collection.JavaConverters._
        processFiles(event.getDragboard.getFiles.asScala.toList)
        event.setDropCompleted(true)
        event.consume()
      }

      foo(event)
    })
    //        fileList.setCellFactory(new ListViewListCellCallback());
    val item1 = new MenuItem("Files")
    item1.setOnAction((e: ActionEvent) => selectFilesDialog(ConverterApplication.getEnv.getWindow))
    val item2 = new MenuItem("Folder")
    item2.setOnAction((e: ActionEvent) => selectFolderDialog(ConverterApplication.getEnv.getWindow))
    controller.contextMenu.getItems.addAll(item1, item2)
    val context = ConverterApplication.getContext
    selectedMedia = context.getSelectedMedia
    resetForNewConversion(context.registerForConversion(this))

    selectedMedia.addListener(new InvalidationListener {
      override def invalidated(observable: Observable): Unit = {
        if (selectedMedia.isEmpty) return null
        val change = new util.ArrayList[MediaInfo](selectedMedia)
        val selection = new util.ArrayList[MediaInfo](controller.fileList.getSelectionModel.getSelectedItems)
        if (!change.containsAll(selection) || !selection.containsAll(change)) {
          controller.fileList.getSelectionModel.clearSelection()
          change.forEach((m: MediaInfo) => controller.fileList.getSelectionModel.select(conversion.getMedia.indexOf(m)))
        }
      }
    })
  }

  private[bind] def addFiles(event: ActionEvent): Unit = {
    val node = event.getSource.asInstanceOf[Button]
    controller.contextMenu.show(node, Side.RIGHT, 0, 0)
  }


  private[bind] def selectFolderDialog(window: Window): Unit = {
    val directoryChooser: DirectoryChooser = new DirectoryChooser
    val sourceFolder: String = AppProperties.getProperty("source.folder")
    directoryChooser.setInitialDirectory(Utils.getInitialDirecotory(sourceFolder))
    val filetypes: StringJoiner = new StringJoiner("/")
    FILE_EXTENSIONS.foreach((ext: String) => filetypes.add(StringUtils.upperCase(ext)))

    directoryChooser.setTitle("Select folder with " + filetypes.toString + " files for conversion")
    val selectedDirectory: File = directoryChooser.showDialog(window)
    if (selectedDirectory != null) {
      processFiles(List[File] {
        selectedDirectory
      })
      AppProperties.setProperty("source.folder", selectedDirectory.getAbsolutePath)
    }
  }

  private[bind] def processFiles(files: List[File]): Unit = {
    val fileNames = new util.ArrayList[String]
    for (file <- files) {
      if (file.isDirectory) processFiles(UIUtils.listFiles(file, FILE_EXTENSIONS))
      else if (FILE_EXTENSIONS.contains(FilenameUtils.getExtension(file.getName))) fileNames.add(file.getPath)
    }
    val addedMedia = createMediaLoader(fileNames).loadMediaInfo
    controller.fileList.getItems.addAll(addedMedia)
  }

  private[bind] def createMediaLoader(fileNames: util.List[String]) = new FFMediaLoader(fileNames, conversion)

  private[bind] def selectOutputDirectory: String = {
    val env = ConverterApplication.getEnv
    var outputDestination = null
    val directoryChooser = new DirectoryChooser
    val outputFolder = AppProperties.getProperty("output.folder")
    directoryChooser.setInitialDirectory(Utils.getInitialDirecotory(outputFolder))
    directoryChooser.setTitle("Select destination folder for encoded files")
    val selectedDirectory = directoryChooser.showDialog(env.getWindow)
    AppProperties.setProperty("output.folder", selectedDirectory.getAbsolutePath)
    selectedDirectory.getPath
  }

  private[bind] def showNotification(finalOutputDestination: String): Unit = {
    Notifications.create.title("AudioBookConverter: Conversion is completed").text(finalOutputDestination).show()
  }

  private[bind] def moveUp(event: ActionEvent): Unit = {
    val selectedIndices = controller.fileList.getSelectionModel.getSelectedIndices
    if (selectedIndices.size == 1) {
      val items = controller.fileList.getItems
      val selected = selectedIndices.get(0)
      if (selected > 0) {
        val upper = items.get(selected - 1)
        val lower = items.get(selected)
        items.set(selected - 1, lower)
        items.set(selected, upper)
        controller.fileList.getSelectionModel.clearAndSelect(selected - 1)
      }
    }
  }


  private[bind] def moveDown(event: ActionEvent): Unit = {
    val selectedIndices = controller.fileList.getSelectionModel.getSelectedIndices
    if (selectedIndices.size == 1) {
      val items = controller.fileList.getItems
      val selected = selectedIndices.get(0)
      if (selected < items.size - 1) {
        val lower = items.get(selected + 1)
        val upper = items.get(selected)
        items.set(selected, lower)
        items.set(selected + 1, upper)
        controller.fileList.getSelectionModel.clearAndSelect(selected + 1)
      }
    }
  }

  private[bind] def clear(event: ActionEvent): Unit = {
    controller.fileList.getItems.clear()
  }

  private[bind] def removeFiles(event: ActionEvent): Unit = {
    val selected = controller.fileList.getSelectionModel.getSelectedItems
    controller.fileList.getItems.removeAll(selected)
  }


  private[bind] def selectFilesDialog(window: Window): Unit = {
    val fileChooser = new FileChooser
    val sourceFolder = AppProperties.getProperty("source.folder")
    fileChooser.setInitialDirectory(UIUtils.getInitialDirecotory(sourceFolder))
    val filetypes = new StringJoiner("/")
    FILE_EXTENSIONS.foreach((ext: String) => filetypes.add(StringUtils.upperCase(ext)))
    fileChooser.setTitle("Select " + filetypes.toString + " files for conversion")
    FILE_EXTENSIONS.foreach((ext: String) => fileChooser.getExtensionFilters.add(new FileChooser.ExtensionFilter(ext, "*." + ext)))

    val files = fileChooser.showOpenMultipleDialog(window)
    if (files != null) {
      import scala.collection.JavaConverters._
      processFiles(files.asScala.toList)
      val firstFile = files.get(0)
      val parentFile = firstFile.getParentFile
      AppProperties.setProperty("source.folder", parentFile.getAbsolutePath)
    }
  }

  private[bind] def selectOutputFile(audioBookInfo: AudioBookInfo, mediaInfo: MediaInfo): String = {
    val env = ConverterApplication.getEnv
    val fileChooser = new FileChooser
    val outputFolder = AppProperties.getProperty("output.folder")
    fileChooser.setInitialDirectory(UIUtils.getInitialDirecotory(outputFolder))
    fileChooser.setInitialFileName(Utils.getOuputFilenameSuggestion(mediaInfo.getFileName, audioBookInfo))
    fileChooser.setTitle("Save AudioBook")
    fileChooser.getExtensionFilters.add(new FileChooser.ExtensionFilter(M4B, "*." + M4B))
    val file = fileChooser.showSaveDialog(env.getWindow)
    if (file == null) return null
    val parentFolder = file.getParentFile
    AppProperties.setProperty("output.folder", parentFolder.getAbsolutePath)
    file.getPath
  }

  private[bind] def pause(actionEvent: ActionEvent): Unit = {
    val context = ConverterApplication.getContext
    if (context.isPaused) {
      context.resumeConversions()
      controller.pauseButton.setText("Pause all")
    }
    else {
      context.pauseConversions()
      controller.pauseButton.setText("Resume all")
    }
  }

  private[bind] def stop(actionEvent: ActionEvent): Unit = {
    ConverterApplication.getContext.stopConversions()
  }

  private[bind] def start(actionEvent: ActionEvent): Unit = {
    val context = ConverterApplication.getContext
    val media = conversion.getMedia
    if (media.size > 0) {
      val audioBookInfo = conversion.getBookInfo
      val mediaInfo = media.get(0)
      var outputDestination: String = null
      if (conversion.getMode == ConversionMode.BATCH) outputDestination = selectOutputDirectory
      else outputDestination = selectOutputFile(audioBookInfo, mediaInfo)
      if (outputDestination != null) {
        val finalName = new File(outputDestination).getName
        conversion.addStatusChangeListener((observable: ObservableValue[_ <: ProgressStatus], oldValue: ProgressStatus, newValue: ProgressStatus) => {
          def foo(observable: ObservableValue[_ <: ProgressStatus], oldValue: ProgressStatus, newValue: ProgressStatus) = {
            if (ProgressStatus.FINISHED == newValue) Platform.runLater(() => showNotification(finalName))
          }

          foo(observable, oldValue, newValue)
        })
        import scala.collection.JavaConverters._

        val mediaList: scala.List[MediaInfo] = media.asScala.toList

        var totalDuration: Long = 0;
        mediaList.foreach(media => totalDuration += media.getDuration)
        //        val totalDuration = media.stream.mapToLong(MediaInfo.getDuration).sum
        val conversionProgress = new ConversionProgress(conversion, media.size, totalDuration, finalName)
        context.startConversion(outputDestination, conversionProgress)
      }
    }
  }

  override def resetForNewConversion(c: Conversion): Unit = {
    conversion = c
    val media = conversion.getMedia
    controller.fileList.setItems(media)
    controller.fileList.getSelectionModel.setSelectionMode(SelectionMode.MULTIPLE)
    /* TODO fix buttons behaviour
            conversion.addStatusChangeListener((observable, oldValue, newValue) ->
                    updateUI(newValue, media.isEmpty(), fileList.getSelectionModel().getSelectedIndices())
            );
    */
    //        media.addListener((ListChangeListener<MediaInfo>) c -> updateUI(this.conversion.getStatus(), c.getList().isEmpty(), fileList.getSelectionModel().getSelectedIndices()));
    if (listener != null) controller.fileList.getSelectionModel.selectedItemProperty.removeListener(listener)
    listener = new MediaInfoChangeListener(conversion)
    controller.fileList.getSelectionModel.selectedItemProperty.addListener(listener)
  }

  private[bind] class MediaInfoChangeListener(var conversion: Conversion) extends ChangeListener[MediaInfo] {
    override def changed(observable: ObservableValue[_ <: MediaInfo], oldValue: MediaInfo, newValue: MediaInfo): Unit = {
      //            updateUI(conversion.getStatus(), conversion.getMedia().isEmpty(), fileList.getSelectionModel().getSelectedIndices());
      selectedMedia.clear
      controller.fileList.getSelectionModel.getSelectedIndices.forEach((i: Integer) => selectedMedia.add(conversion.getMedia.get(i)))
    }
  }

}
