package uk.yermak.audiobookconverter

import java.util.concurrent.Executors

import javafx.beans.property.SimpleObjectProperty
import javafx.beans.value.ChangeListener
import javafx.collections.{FXCollections, ObservableList}
import uk.yermak.audiobookconverter.ConversionMode.ConversionMode
import uk.yermak.audiobookconverter.ProgressStatus._

/**
  * Created by rYermak on 06-Feb-18.
  */
object Conversion {
  private val executorService = Executors.newCachedThreadPool
}

class Conversion {
  private val media = FXCollections.observableArrayList[MediaInfo]
  private val mode = new SimpleObjectProperty[ConversionMode](ConversionMode.PARALLEL)
  private val status = new SimpleObjectProperty[ProgressStatus](this, "status", READY)
  private var bookInfo: AudioBookInfo = _
  private var outputParameters: OutputParameters = _
  private var outputDestination: String = _

  def setMode(mode: ConversionMode) = this.mode.set(mode)

  def setBookInfo(bookInfo: AudioBookInfo) = this.bookInfo = bookInfo

  def getMedia: ObservableList[MediaInfo] = media

  def getMode: ConversionMode = mode.get

  def getBookInfo: AudioBookInfo = bookInfo

  def start(outputDestination: String, refreshable: Refreshable) = {
    setOutputDestination(outputDestination)
    Executors.newSingleThreadExecutor.execute(refreshable)
    val progressCallbacks: Map[String, ProgressCallback] = Map()
    media.forEach((mediaInfo: MediaInfo) => progressCallbacks + (mediaInfo.getFileName -> new ProgressCallback(mediaInfo.getFileName, refreshable)))
    progressCallbacks + ("output" -> new ProgressCallback("output", refreshable))
    val conversionStrategy: ConversionStrategy = ConversionMode.createConvertionStrategy(this, progressCallbacks)
    Conversion.executorService.execute(conversionStrategy)
    status.set(IN_PROGRESS)
  }

  def addStatusChangeListener(listener: ChangeListener[ProgressStatus]) = status.addListener(listener)

  def pause() = if (status.get == IN_PROGRESS) status.set(PAUSED)

  def stop() = if (!(status.get == FINISHED)) status.set(CANCELLED)

  def getStatus = status.get

  def finished() = status.set(FINISHED)

  def error(message: String) = status.set(ERROR)

  def resume() = if (status.get == PAUSED) status.set(IN_PROGRESS)

  def addModeChangeListener(listener: ChangeListener[ConversionMode]) = mode.addListener(listener)

  def setOutputParameters(params: OutputParameters) = outputParameters = params

  def getOutputParameters = outputParameters

  def getPosters = bookInfo.getPosters

  def getOutputDestination = outputDestination

  def setOutputDestination(outputDestination: String) = this.outputDestination = outputDestination
}