package uk.yermak.audiobookconverter

import javafx.beans.property.SimpleObjectProperty
import javafx.beans.value.{ChangeListener, ObservableValue}
import javafx.collections.FXCollections
import uk.yermak.audiobookconverter.fx.ConversionProgress

import scala.collection.mutable.ListBuffer

/**
  * Created by yermak on 06-Feb-18.
  */
class ConversionContext() {

  private val conversionQueue: ListBuffer[Conversion] = ListBuffer()
  private val conversion: SimpleObjectProperty[Conversion] = new SimpleObjectProperty[Conversion](new Conversion)
  private var paused = false
  private var subscriber: Subscriber = _
  private val selectedMedia = FXCollections.observableArrayList[MediaInfo]
  conversionQueue += conversion.get

  //TODO simplify Subscriber
  def startConversion(outputDestination: String, conversionProgress: ConversionProgress) = {
    subscriber.addConversionProgress(conversionProgress)
    conversion.get.start(outputDestination, conversionProgress)
    val newConversion = new Conversion
    conversionQueue += newConversion
    conversion.set(newConversion)
  }

  def stopConversions() = conversionQueue.foreach(_.stop)

  def subscribeForStart(subscriber: Subscriber) = this.subscriber = subscriber

  def getSelectedMedia = selectedMedia

  def registerForConversion(conversionSubscriber: ConversionSubscriber): Conversion = {
    conversion.addListener(new ConversionChangeListener(conversionSubscriber))
    conversion.get
  }

  def pauseConversions() = {
    conversionQueue.foreach(_.pause)
    paused = true
  }

  def resumeConversions() = {
    conversionQueue.foreach(_.resume)
    paused = false
  }

  def isPaused = paused
}

class ConversionChangeListener(conversionSubscriber: ConversionSubscriber) extends ChangeListener[Conversion] {

  override def changed(observable: ObservableValue[_ <: Conversion], oldValue: Conversion, newValue: Conversion) = {
    conversionSubscriber.resetForNewConversion(newValue)
  }
}