package uk.yermak.audiobookconverter.fx.bind

import java.lang.invoke.MethodHandles
import java.util
import java.util.concurrent.Executors

import javafx.application.Platform
import javafx.beans.value.ObservableValue
import javafx.beans.{InvalidationListener, Observable}
import javafx.collections.ObservableList
import javafx.event.ActionEvent
import javafx.geometry.Side
import javafx.scene.Node
import javafx.scene.control.{ContextMenu, MenuItem}
import javafx.scene.input.ContextMenuEvent
import org.apache.commons.lang3.StringUtils
import org.slf4j.LoggerFactory
import uk.yermak.audiobookconverter._
import uk.yermak.audiobookconverter.fx.ConverterApplication
import uk.yermak.audiobookconverter.fx.util.TextFieldValidator

/**
 * Created by Yermak on 04-Feb-18.
 */

class BookInfoDelegate(controller: BookInfoController) extends ConversionSubscriber {
  private val logger = LoggerFactory.getLogger(MethodHandles.lookup.lookupClass)
  private var bookInfo: AudioBookInfo = _

  private[bind] def initialize(): Unit = {
    reloadGenres()
    val menuItem = new MenuItem("Remove")
    menuItem.setOnAction((event: ActionEvent) => {
      controller.genre.getItems.remove(controller.genre.getSelectionModel.getSelectedIndex)
      saveGenres()
    })
    val contextMenu = new ContextMenu(menuItem)

    controller.genre.setOnContextMenuRequested((event: ContextMenuEvent) => {
      if (!controller.genre.getSelectionModel.isEmpty) contextMenu.show(event.getSource.asInstanceOf[Node], Side.RIGHT, 0, 0)
      controller.genre.hide()
    })

    val conversion: Conversion = ConverterApplication.getContext.registerForConversion(this)
    resetForNewConversion(conversion)


    controller.bookNo.setTextFormatter(new TextFieldValidator(TextFieldValidator.ValidationModus.MAX_INTEGERS, 3).getFormatter)
    controller.year.setTextFormatter(new TextFieldValidator(TextFieldValidator.ValidationModus.MAX_INTEGERS, 4).getFormatter)
    controller.title.textProperty.addListener((o: Observable) => bookInfo.setTitle(controller.title.getText))
    controller.writer.textProperty.addListener((o: Observable) => bookInfo.setWriter(controller.writer.getText))
    controller.narrator.textProperty.addListener((o: Observable) => bookInfo.setNarrator(controller.narrator.getText))
    controller.genre.valueProperty.addListener((o: Observable) => bookInfo.setGenre(controller.genre.getValue))
    controller.genre.getEditor.textProperty.addListener((o: Observable) => bookInfo.setGenre(controller.genre.getEditor.getText))
    controller.series.textProperty.addListener((o: Observable) => bookInfo.setSeries(controller.series.getText))
    controller.bookNo.textProperty.addListener((o: Observable) => {
      if (StringUtils.isNotBlank(controller.bookNo.getText) && StringUtils.isNumeric(controller.bookNo.getText)) bookInfo.setBookNumber(controller.bookNo.getText.toInt)
    })
    controller.year.textProperty.addListener((o: Observable) => bookInfo.setYear(controller.year.getText))
    controller.comment.textProperty.addListener((o: Observable) => bookInfo.setComment(controller.comment.getText))
  }


  override def resetForNewConversion(conversion: Conversion): Unit = {
    bookInfo = new AudioBookInfo
    conversion.setBookInfo(bookInfo)
    conversion.addStatusChangeListener((observable: ObservableValue[_ <: ProgressStatus], oldValue: ProgressStatus, newValue: ProgressStatus) => {
      if (newValue == ProgressStatus.IN_PROGRESS) saveGenres()
    })
    val media = conversion.getMedia
    media.addListener(new InvalidationListener {
      override def invalidated(observable: Observable): Unit = {
        updateTags(media, media.isEmpty)
      }
    })


    conversion.addModeChangeListener((observable: ObservableValue[_ <: ConversionMode], oldValue: ConversionMode, newValue: ConversionMode) => updateTags(media, ConversionMode.BATCH == newValue))
    clearTags()
  }

  private[bind] def saveGenres(): Unit = {
    val uniqueGenres = new util.TreeSet[String](controller.genre.getItems)
    if (StringUtils.isNotEmpty(controller.genre.getEditor.getText)) uniqueGenres.add(controller.genre.getEditor.getText)
    controller.genre.getItems.clear()
    controller.genre.getItems.addAll(uniqueGenres)
    val sb = new StringBuffer
    uniqueGenres.forEach(s => sb.append(s).append("::"))
    AppProperties.setProperty("genres", sb.toString)
  }

  private[bind] def reloadGenres(): Unit = {
    val genresProperty = AppProperties.getProperty("genres")
    if (genresProperty != null) {
      val genres = genresProperty.split("::")
      genres.sorted
      genres.foreach(s => controller.genre.getItems.add(s));
    }
  }


  private[bind] def copyTags(bookInfo: AudioBookInfo): Unit = {
    controller.title.setText(bookInfo.getTitle)
    controller.writer.setText(bookInfo.getWriter)
    controller.narrator.setText(bookInfo.getNarrator)
    controller.genre.getEditor.setText(bookInfo.getGenre)
    controller.series.setText(bookInfo.getSeries)
    controller.bookNo.setText(String.valueOf(bookInfo.getBookNumber))
    controller.year.setText(bookInfo.getYear)
    controller.comment.setText(bookInfo.getComment)
  }

  private[bind] def clearTags(): Unit = {
    controller.title.setText("")
    controller.writer.setText("")
    controller.narrator.setText("")
    controller.genre.getEditor.setText("")
    controller.series.setText("")
    controller.bookNo.setText("")
    controller.year.setText("")
    controller.comment.setText("")
  }

  private[bind] def updateTags(media: ObservableList[MediaInfo], clear: Boolean): Unit = {
    if (clear) clearTags()
    else Executors.newSingleThreadExecutor.submit(new Runnable() {
      override def run(): Unit = { //getBookInfo is proxied blocking method should be executed outside of UI thread,
        // when info become available - scheduling update in UI thread.
        val info = media.get(0).getBookInfo
        Platform.runLater(() => copyTags(info))
      }
    })
  }


}