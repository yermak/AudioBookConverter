package uk.yermak.audiobookconverter

/**
  * Created by yermak on 06-Dec-18.
  */
trait ConversionSubscriber {
  def resetForNewConversion(conversion: Conversion)
}
